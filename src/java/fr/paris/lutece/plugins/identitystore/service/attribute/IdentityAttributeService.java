package fr.paris.lutece.plugins.identitystore.service.attribute;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeCache;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class IdentityAttributeService {
    private final IdentityAttributeCache _cache = SpringContextService.getBean( "identitystore.identityAttributeCache" );
    private final AttributeCertificationDefinitionService _attributeCertificationDefinitionService = AttributeCertificationDefinitionService.instance( );
    private final ServiceContractService _serviceContractService = ServiceContractService.instance( );

    private static IdentityAttributeService _instance;

    public static IdentityAttributeService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityAttributeService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    public AttributeKey getAttributeKey(final String keyName ) throws IdentityAttributeNotFoundException
    {
        return _cache.get( keyName );
    }

    public List<AttributeKey> getCommonAttributeKeys(final String keyName )
    {
        if ( _cache.getKeys( ).isEmpty( ) )
        {
            _cache.refresh( );
        }
        return _cache.getKeys( ).stream( ).map( key -> {
                    try
                    {
                        return _cache.get( key );
                    }
                    catch( IdentityAttributeNotFoundException e )
                    {
                        throw new RuntimeException( e );
                    }
                } ).filter( attributeKey -> attributeKey.getCommonSearchKeyName( ) != null && Objects.equals( attributeKey.getCommonSearchKeyName( ), keyName ) )
                .collect( Collectors.toList( ) );
    }

    public void createAttributeKey( final AttributeKey attributeKey )
    {
        AttributeKeyHome.create( attributeKey );
        _cache.put( attributeKey.getKeyName( ), attributeKey );
    }

    public void updateAttributeKey( final AttributeKey attributeKey )
    {
        AttributeKeyHome.update( attributeKey );
        _cache.put( attributeKey.getKeyName( ), attributeKey );
    }

    public void deleteAttributeKey( final AttributeKey attributeKey )
    {
        AttributeKeyHome.remove( attributeKey.getId( ) );
        _cache.removeKey( attributeKey.getKeyName( ) );
    }

    /**
     * Private method used to create an attribute for an identity.
     *
     * @param attributeToCreate
     * @param identity
     * @param clientCode
     * @return AttributeStatus
     * @throws IdentityAttributeNotFoundException
     */
    public AttributeStatus createAttribute( final CertifiedAttribute attributeToCreate, final Identity identity, final String clientCode )
            throws IdentityStoreException
    {
        if ( StringUtils.isBlank( attributeToCreate.getValue( ) ) )
        {
            final AttributeStatus attributeStatus = new AttributeStatus( );
            attributeStatus.setKey( attributeToCreate.getKey( ) );
            attributeStatus.setStatus( AttributeChangeStatus.NOT_CREATED );
            return attributeStatus;
        }

        final IdentityAttribute attribute = new IdentityAttribute( );
        attribute.setIdIdentity( identity.getId( ) );
        attribute.setAttributeKey( getAttributeKey( attributeToCreate.getKey( ) ) ); // ?
        attribute.setValue( attributeToCreate.getValue( ) );
        attribute.setLastUpdateClientCode( clientCode );

        if ( attributeToCreate.getCertificationProcess( ) != null )
        {
            final AttributeCertificate certificate = new AttributeCertificate( );
            certificate.setCertificateDate( new Timestamp( attributeToCreate.getCertificationDate( ).getTime( ) ) );
            certificate.setCertifierCode( attributeToCreate.getCertificationProcess( ) );
            certificate.setCertifierName( attributeToCreate.getCertificationProcess( ) ); // ?
            attribute.setCertificate( AttributeCertificateHome.create( certificate ) );
            attribute.setIdCertificate( attribute.getCertificate( ).getId( ) );
        }

        IdentityAttributeHome.create( attribute );
        identity.getAttributes( ).put( attribute.getAttributeKey( ).getKeyName( ), attribute );

        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attribute.getAttributeKey( ).getKeyName( ) );
        attributeStatus.setStatus( AttributeChangeStatus.CREATED );

        return attributeStatus;
    }

    /**
     * private method used to update an attribute of an identity
     *
     * @param attributeToUpdate
     * @param identity
     * @param clientCode
     * @return AttributeStatus
     * @throws IdentityStoreException
     */
    public AttributeStatus updateAttribute(final CertifiedAttribute attributeToUpdate, final Identity identity, final String clientCode )
            throws IdentityStoreException
    {
        final IdentityAttribute existingAttribute = identity.getAttributes( ).get( attributeToUpdate.getKey( ) );

        int attributeToUpdateLevelInt = _attributeCertificationDefinitionService.getLevelAsInteger( attributeToUpdate.getCertificationProcess( ),
                attributeToUpdate.getKey( ) );
        int existingAttributeLevelInt = _attributeCertificationDefinitionService.getLevelAsInteger( existingAttribute.getCertificate( ).getCertifierCode( ),
                existingAttribute.getAttributeKey( ).getKeyName( ) );
        if ( attributeToUpdateLevelInt == existingAttributeLevelInt && StringUtils.equals( attributeToUpdate.getValue( ), existingAttribute.getValue( ) )
                && ( attributeToUpdate.getCertificationDate( ).equals( existingAttribute.getCertificate( ).getCertificateDate( ) )
                || attributeToUpdate.getCertificationDate( ).before( existingAttribute.getCertificate( ).getCertificateDate( ) ) ) )
        {
            final AttributeStatus attributeStatus = new AttributeStatus( );
            attributeStatus.setKey( attributeToUpdate.getKey( ) );
            attributeStatus.setStatus( AttributeChangeStatus.NOT_UPDATED );
            return attributeStatus;
        }
        if ( attributeToUpdateLevelInt >= existingAttributeLevelInt )
        {
            if ( StringUtils.isBlank( attributeToUpdate.getValue( ) ) )
            {
                // #232 : remove attribute if :
                // - attribute is not mandatory
                // - new value is null or blank
                // - sent certification level is >= to existing one
                final Optional<AttributeRight> right = _serviceContractService.getActiveServiceContract( clientCode ).getAttributeRights( ).stream( )
                        .filter( ar -> ar.getAttributeKey( ).getKeyName( ).equals( attributeToUpdate.getKey( ) ) ).findAny( );
                if ( right.isPresent( ) && right.get( ).isMandatory( ) )
                {
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( attributeToUpdate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.NOT_REMOVED );
                    return attributeStatus;
                }
                IdentityAttributeHome.remove( identity.getId( ), existingAttribute.getAttributeKey( ).getId( ) );
                identity.getAttributes( ).remove( existingAttribute.getAttributeKey( ).getKeyName( ) );

                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( attributeToUpdate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.REMOVED );
                return attributeStatus;
            }

            existingAttribute.setValue( attributeToUpdate.getValue( ) );
            existingAttribute.setLastUpdateClientCode( clientCode );

            if ( attributeToUpdate.getCertificationProcess( ) != null )
            {
                final AttributeCertificate certificate = new AttributeCertificate( );
                certificate.setCertificateDate( new Timestamp( attributeToUpdate.getCertificationDate( ).getTime( ) ) );
                certificate.setCertifierCode( attributeToUpdate.getCertificationProcess( ) );
                certificate.setCertifierName( attributeToUpdate.getCertificationProcess( ) );

                existingAttribute.setCertificate( AttributeCertificateHome.create( certificate ) ); // TODO supprime-t-on l'ancien certificat ?
                existingAttribute.setIdCertificate( existingAttribute.getCertificate( ).getId( ) );
            }

            IdentityAttributeHome.update( existingAttribute );
            identity.getAttributes( ).put( existingAttribute.getAttributeKey( ).getKeyName( ), existingAttribute );

            final AttributeStatus attributeStatus = new AttributeStatus( );
            attributeStatus.setKey( attributeToUpdate.getKey( ) );
            attributeStatus.setStatus( AttributeChangeStatus.UPDATED );
            return attributeStatus;
        }

        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attributeToUpdate.getKey( ) );
        attributeStatus.setStatus( AttributeChangeStatus.INSUFFICIENT_CERTIFICATION_LEVEL );
        return attributeStatus;
    }
}
