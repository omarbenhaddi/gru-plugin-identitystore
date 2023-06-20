/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitystore.service.identity;

import fr.paris.lutece.plugins.geocodes.business.City;
import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.service.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRuleHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.SearchRuleType;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeCache;
import fr.paris.lutece.plugins.identitystore.service.IdentityChange;
import fr.paris.lutece.plugins.identitystore.service.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.RefAttributeCertificationDefinitionNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.duplicate.IDuplicateService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task.FullIndexTask;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreDeleteRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchMessage;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentityService
{
    private static final String ATTR_KEY_BIRTHPLACE = "birthplace";
    private static final String ATTR_KEY_BIRTHPLACE_CODE = "birthplace_code";
    private static final String ATTR_KEY_BIRTHCOUNTRY = "birthcountry";
    private static final String ATTR_KEY_BIRTHCOUNTRY_CODE = "birthcountry_code";

    private final AttributeCertificationDefinitionService _attributeCertificationDefinitionService = AttributeCertificationDefinitionService.instance( );
    private final IdentityStoreNotifyListenerService _identityStoreNotifyListenerService = IdentityStoreNotifyListenerService.instance( );
    private final ServiceContractService _serviceContractService = ServiceContractService.instance( );
    private final IDuplicateService _duplicateServiceCreation = SpringContextService.getBean( "identitystore.duplicateService.creation" );
    private final IDuplicateService _duplicateServiceUpdate = SpringContextService.getBean( "identitystore.duplicateService.update" );
    private final IDuplicateService _duplicateServiceImportCertitude = SpringContextService.getBean( "identitystore.duplicateService.import.certitude" );
    private final IDuplicateService _duplicateServiceImportSuspicion = SpringContextService.getBean( "identitystore.duplicateService.import.suspicion" );
    private final IdentityAttributeCache _cache = SpringContextService.getBean( "identitystore.identityAttributeCache" );
    protected ISearchIdentityService _searchIdentityService = SpringContextService.getBean( "identitystore.searchIdentityService" );
    protected ISearchIdentityService _searchDbIdentityService = SpringContextService.getBean( "identitystore.db.searchIdentityService" );

    private static IdentityService _instance;

    public static IdentityService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    /**
     * Creates a new {@link Identity} according to the given {@link IdentityChangeRequest}
     *
     * @param identityChangeRequest
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the created {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity create( final IdentityChangeRequest identityChangeRequest, final String clientCode, final IdentityChangeResponse response )
            throws IdentityStoreException
    {
        if ( !_serviceContractService.canCreateIdentity( clientCode ) )
        {
            response.setStatus( IdentityChangeStatus.FAILURE );
            response.setMessage( "The client application is not authorized to create an identity." );
            return null;
        }

        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getCustomerId( ) ) )
        {
            throw new IdentityStoreException( "You cannot specify a CUID when requesting for a creation" );
        }

        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getConnectionId( ) )
        		&& !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            throw new IdentityStoreException( "You cannot specify a GUID when requesting for a creation" );
        }

        // check if all mandatory attributes are present
        final List<String> mandatoryAttributes = _serviceContractService.getMandatoryAttributes( clientCode,
                AttributeKeyHome.getMandatoryForCreationAttributeKeyList( ) );
        if ( CollectionUtils.isNotEmpty( mandatoryAttributes ) )
        {
            final Set<String> providedKeySet = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                    .filter( a -> StringUtils.isNotBlank( a.getValue( ) ) ).map( CertifiedAttribute::getKey ).collect( Collectors.toSet( ) );
            if ( !providedKeySet.containsAll( mandatoryAttributes ) )
            {
                response.setStatus( IdentityChangeStatus.FAILURE );
                response.setMessage( "All mandatory attributes must be provided : " + mandatoryAttributes );
                return null;
            }
        }

        // check if can set "mon_paris_active" flag to true
        if ( Boolean.TRUE.equals( identityChangeRequest.getIdentity( ).getMonParisActive( ) )
        		&& !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
        	throw new IdentityStoreException( "You cannot set the 'mon_paris_active' flag when requesting for a creation" );
        }

        // check if GUID is already in use
        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getConnectionId( ) )
        		&& IdentityHome.findByCustomerId( identityChangeRequest.getIdentity( ).getConnectionId( ) ) != null  )
        {
            throw new IdentityStoreException( "GUID is already in use." );
        }

        final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.toMap( CertifiedAttribute::getKey, CertifiedAttribute::getValue ) );
        final DuplicateDto duplicates = _duplicateServiceCreation.findDuplicates( attributes );
        if ( duplicates != null )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setMessage( duplicates.getMessage( ) );
            // response.setDuplicates( duplicates ); //TODO voir si on renvoie le CUID
            return null;
        }

        final Identity identity = new Identity( );
        identity.setMonParisActive(
                identityChangeRequest.getIdentity( ).getMonParisActive( ) != null ? identityChangeRequest.getIdentity( ).getMonParisActive( ) : false );
        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getConnectionId( ) ) )
        {
            identity.setConnectionId( identityChangeRequest.getIdentity( ).getConnectionId( ) );
        }
        IdentityHome.create( identity, _serviceContractService.getDataRetentionPeriodInMonths( clientCode ) );

        final List<CertifiedAttribute> attributesToCreate = identityChangeRequest.getIdentity( ).getAttributes( );
        processCountryForCreate( identity, attributesToCreate, clientCode, response );
        processCityForCreate( identity, attributesToCreate, clientCode, response );

        for ( final CertifiedAttribute certifiedAttribute : attributesToCreate )
        {
            // TODO vérifier que la clef d'attribut existe dans le référentiel
            final AttributeStatus attributeStatus = createAttribute( certifiedAttribute, identity, clientCode );
            response.getAttributeStatuses( ).add( attributeStatus );
        }

        response.setCustomerId( identity.getCustomerId( ) );
        response.setCreationDate( identity.getCreationDate( ) );
        response.setStatus( IdentityChangeStatus.CREATE_SUCCESS );

        /* Historique des modifications */
        response.getAttributeStatuses( ).forEach( attributeStatus -> {
            AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.CREATE, identity, attributeStatus,
                    identityChangeRequest.getOrigin( ), clientCode );
            _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
        } );

        /* Notify listeners for indexation, history, ...  */
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( identity, IdentityChangeType.CREATE, identityChangeRequest.getOrigin( ) ) );

        return identity;
    }

    /**
     * Updates an existing {@link Identity} according to the given {@link IdentityChangeRequest} and following the given rules: <br>
     * <ul>
     * <li>The {@link Identity} must exist in te database. If not, NOT_FOUND status is returned in the execution response</li>
     * <li>The {@link Identity} must not be merged or deleted. In case of merged/deleted identity, the update is not performed and the customer ID of the
     * primary identity is returned in the execution response with a CONFLICT status</li>
     * <li>If the {@link Identity} can be updated, its {@link IdentityAttribute} list is updated following the given rule:
     * <ul>
     * <li>If the {@link IdentityAttribute} exists, it is updated if the value is different, and if the process level given in the request is higher than the
     * existing one. If the value cannot be updated, the NOT_UPDATED status, associated with the attribute key, is returned in the execution response.</li>
     * <li>If the {@link IdentityAttribute} does not exist, it is created. The CREATED status, associated with the attribute key, is returned in the execution
     * response.</li>
     * <li>CUID and GUID attributes cannot be modified.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param customerId
     *            the id of the updated {@link Identity}
     * @param identityChangeRequest
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the updated {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity update( final String customerId, final IdentityChangeRequest identityChangeRequest, final String clientCode,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {
        if ( !_serviceContractService.canUpdateIdentity( clientCode ) )
        {
            response.setStatus( IdentityChangeStatus.FAILURE );
            response.setCustomerId( customerId );
            response.setMessage( "The client application is not authorized to update an identity." );
            return null;
        }

        final Identity identity = IdentityHome.findByCustomerId( customerId );

        // check if identity exists
        if ( identity == null )
        {
            response.setStatus( IdentityChangeStatus.NOT_FOUND );
            response.setMessage( "No matching identity could be found" );
            return null;
        }

        // check if identity is not merged
        if ( identity.isMerged( ) )
        {
            final Identity masterIdentity = IdentityHome.findMasterIdentityByCustomerId( identityChangeRequest.getIdentity( ).getCustomerId( ) );
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setCustomerId( masterIdentity.getCustomerId( ) );
            response.setMessage( "Cannot update a merged Identity. Master identity customerId is provided in the response." );
            return identity;
        }

        // check if identity is active
        if ( identity.isDeleted( ) )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setCustomerId( identity.getCustomerId( ) );
            response.setMessage( "Cannot update a deleted Identity." );
            return identity;
        }

        // if the identity is connected, check if the service contract allow the update
        // check if can update "mon_paris_active" flag
        if ( !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            if ( identity.isConnected( ) )
            {
                response.setStatus( IdentityChangeStatus.CONFLICT );
                response.setCustomerId( identity.getCustomerId( ) );
                response.setMessage( "The client application is not authorized to update a connected identity." );
                return null;
            }
            if ( identityChangeRequest.getIdentity( ).getMonParisActive( ) != null )
            {
                response.setStatus( IdentityChangeStatus.CONFLICT );
                response.setCustomerId( identity.getCustomerId( ) );
                response.setMessage( "The client application is not authorized to update the 'mon_paris_active' flag." );
                return null;
            }
        }

        // check if update does not create duplicates
        // TODO : check only "strict siblings" rule
        /*
         * final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( ) .collect( Collectors.toMap(
         * CertifiedAttribute::getKey, CertifiedAttribute::getValue ) ); identity.getAttributes().forEach((key, value) -> attributes.putIfAbsent(key,
         * value.getValue())); final DuplicateDto duplicates = _duplicateServiceUpdate.findDuplicates( attributes ); if ( duplicates != null ) { // remove the
         * processed identity duplicates.getIdentities( ).removeIf( qualifiedIdentity -> StringUtils.equals( qualifiedIdentity.getCustomerId( ), customerId ) );
         * } if ( duplicates != null && CollectionUtils.isNotEmpty( duplicates.getIdentities( ) ) ) { response.setStatus( IdentityChangeStatus.CONFLICT );
         * response.setMessage( duplicates.getMessage( ) ); // response.setDuplicates( duplicates ); //TODO voir si on renvoie le CUID return null; }
         */

        // If GUID is updated, check if the new GUID does not exists in database
        if ( _serviceContractService.canModifyConnectedIdentity( clientCode )
                && !StringUtils.equals( identity.getConnectionId( ), identityChangeRequest.getIdentity( ).getConnectionId( ) )
                && identityChangeRequest.getIdentity( ).getConnectionId( ) != null )
        {
            final Identity byConnectionId = IdentityHome.findByConnectionId( identityChangeRequest.getIdentity( ).getConnectionId( ) );
            if ( byConnectionId != null )
            {
                response.setStatus( IdentityChangeStatus.CONFLICT );
                response.setCustomerId( byConnectionId.getCustomerId( ) );
                response.setMessage( "An identity already exists with the given connection ID. The customer ID of that identity is provided in the response." );
                return null;
            }
            else
            {
                identity.setConnectionId( identityChangeRequest.getIdentity( ).getConnectionId( ) );
            }
        }

        // => process update :

        /* Récupération des attributs déja existants ou non */
        final Map<Boolean, List<CertifiedAttribute>> sortedAttributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.partitioningBy( a -> identity.getAttributes( ).containsKey( a.getKey( ) ) ) );
        final List<CertifiedAttribute> existingWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( true ) ) ? sortedAttributes.get( true )
                : new ArrayList<>( );
        final List<CertifiedAttribute> newWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( false ) ) ? sortedAttributes.get( false )
                : new ArrayList<>( );

        processCountryForUpdate( identity, newWritableAttributes, existingWritableAttributes, clientCode, response );
        processCityForUpdate( identity, newWritableAttributes, existingWritableAttributes, clientCode, response );

        /* Create new attributes */
        for ( final CertifiedAttribute attributeToWrite : newWritableAttributes )
        {
            final AttributeStatus attributeStatus = createAttribute( attributeToWrite, identity, clientCode );
            response.getAttributeStatuses( ).add( attributeStatus );
        }

        /* Update existing attributes */
        for ( final CertifiedAttribute attributeToUpdate : existingWritableAttributes )
        {
            final AttributeStatus attributeStatus = updateAttribute( attributeToUpdate, identity, clientCode );
            response.getAttributeStatuses( ).add( attributeStatus );
        }

        if ( identityChangeRequest.getIdentity( ).getMonParisActive( ) != null )
        {
            identity.setMonParisActive( identityChangeRequest.getIdentity( ).getMonParisActive( ) );
        }
        IdentityHome.update( identity );

        response.setCustomerId( identity.getCustomerId( ) );
        response.setConnectionId( identity.getConnectionId( ) );
        response.setCreationDate( identity.getCreationDate( ) );
        response.setLastUpdateDate( identity.getLastUpdateDate( ) );
        boolean notAllAttributesCreatedOrUpdated = response.getAttributeStatuses( ).stream( )
                .anyMatch( attributeStatus -> AttributeChangeStatus.INSUFFICIENT_CERTIFICATION_LEVEL.equals( attributeStatus.getStatus( ) )
                        || AttributeChangeStatus.NOT_UPDATED.equals( attributeStatus.getStatus( ) )
                        || AttributeChangeStatus.INSUFFICIENT_RIGHTS.equals( attributeStatus.getStatus( ) )
                        || AttributeChangeStatus.UNAUTHORIZED.equals( attributeStatus.getStatus( ) )
                        || AttributeChangeStatus.NOT_REMOVED.equals( attributeStatus.getStatus( ) )
                        || AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL.equals( attributeStatus.getStatus( ) )
                        || AttributeChangeStatus.UNKNOWN_GEOCODES_CODE.equals( attributeStatus.getStatus( ) )
                        || AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL.equals( attributeStatus.getStatus( ) ) );
        response.setStatus( notAllAttributesCreatedOrUpdated ? IdentityChangeStatus.UPDATE_INCOMPLETE_SUCCESS : IdentityChangeStatus.UPDATE_SUCCESS );

        /* Historique des modifications */
        response.getAttributeStatuses( ).forEach( attributeStatus -> {
            AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.UPDATE, identity, attributeStatus,
                    identityChangeRequest.getOrigin( ), clientCode );
            _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
        } );

        /* Indexation */
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( identity, IdentityChangeType.UPDATE, identityChangeRequest.getOrigin( ) ) );

        return identity;
    }

    /**
     * Private methode used to process both "birthcountry_code" and "birthcountry" attributes during an create process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    private void processCountryForCreate( final Identity identity, final List<CertifiedAttribute> attrToCreate, final String clientCode,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {

        CertifiedAttribute countryCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHCOUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToCreate != null )
        {
            attrToCreate.remove( countryCodeToCreate );
        }
        CertifiedAttribute countryLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHCOUNTRY ) ).findFirst( ).orElse( null );
        if ( countryLabelToCreate != null )
        {
            attrToCreate.remove( countryLabelToCreate );
        }

        // Country code to CREATE
        if ( countryCodeToCreate != null )
        {
            final Country country = GeoCodesService.getCountryByCode( countryCodeToCreate.getValue( ) ).orElse( null );
            if ( country == null )
            {
                // Country doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( countryCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // Country exists in Geocodes for provided code
                // create country code attribute
                final AttributeStatus codeStatus = createAttribute( countryCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create country label attribute
                // Geocodes label value is used, regardless if a label is provided or not
                final String countryGeocodesLabel = country.getValue( );
                final AttributeChangeStatus labelStatus = ( countryLabelToCreate == null || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) )
                        ? AttributeChangeStatus.CREATED
                        : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                if ( countryLabelToCreate == null )
                {
                    countryLabelToCreate = new CertifiedAttribute( );
                    countryLabelToCreate.setKey( ATTR_KEY_BIRTHCOUNTRY );
                }
                countryLabelToCreate.setValue( countryGeocodesLabel );
                countryLabelToCreate.setCertificationProcess( countryCodeToCreate.getCertificationProcess( ) );
                countryLabelToCreate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                final AttributeStatus attributeStatus = createAttribute( countryLabelToCreate, identity, clientCode );
                attributeStatus.setStatus( labelStatus );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
        }
        // No country code sent, checking if label was sent
        else
        {
            if ( countryLabelToCreate != null )
            {
                final List<Country> countries = GeoCodesService.getCountriesListByName( countryLabelToCreate.getValue( ) );
                if ( CollectionUtils.isEmpty( countries ) )
                {
                    // Country doesn't exist in Geocodes for provided label
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                else
                    if ( countries.size( ) > 1 )
                    {
                        // Multiple countries exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                    {
                        // One country exists in Geocodes for provided label
                        // Create country label attribute
                        final AttributeStatus labelStatus = createAttribute( countryLabelToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( labelStatus );

                        // create country code attribute
                        final String countryGeocodesCode = countries.get( 0 ).getCode( );
                        countryCodeToCreate = new CertifiedAttribute( );
                        countryCodeToCreate.setKey( ATTR_KEY_BIRTHCOUNTRY_CODE );
                        countryCodeToCreate.setValue( countryGeocodesCode );
                        countryCodeToCreate.setCertificationProcess( countryLabelToCreate.getCertificationProcess( ) );
                        countryCodeToCreate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                        final AttributeStatus codeStatus = createAttribute( countryCodeToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( codeStatus );
                    }
            }
        }
    }

    /**
     * Private methode used to process both "birthplace_code" and "birthplace" attributes during an create process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    private void processCityForCreate( final Identity identity, final List<CertifiedAttribute> attrToCreate, final String clientCode,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {

        CertifiedAttribute cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHPLACE_CODE ) ).findFirst( ).orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
        }
        CertifiedAttribute cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHPLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
        }

        // City code to CREATE
        if ( cityCodeToCreate != null )
        {
            final City city = GeoCodesService.getCityByCode( cityCodeToCreate.getValue( ) ).orElse( null );
            if ( city == null )
            {
                // city doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( cityCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // city exists in Geocodes for provided code
                // create city code attribute
                final AttributeStatus codeStatus = createAttribute( cityCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create city label attribute
                // Geocodes label value is used, regardless if a label is provided or not
                final String cityGeocodesLabel = city.getValue( );
                final AttributeChangeStatus labelStatus = ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                        ? AttributeChangeStatus.CREATED
                        : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                if ( cityLabelToCreate == null )
                {
                    cityLabelToCreate = new CertifiedAttribute( );
                    cityLabelToCreate.setKey( ATTR_KEY_BIRTHPLACE );
                }
                cityLabelToCreate.setValue( cityGeocodesLabel );
                cityLabelToCreate.setCertificationProcess( cityCodeToCreate.getCertificationProcess( ) );
                cityLabelToCreate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                final AttributeStatus attributeStatus = createAttribute( cityLabelToCreate, identity, clientCode );
                attributeStatus.setStatus( labelStatus );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
        }
        // No city code sent, checking if label was sent
        else
        {
            if ( cityLabelToCreate != null )
            {
                final List<City> cities = GeoCodesService.getCitiesListByName( cityLabelToCreate.getValue( ) );
                if ( CollectionUtils.isEmpty( cities ) )
                {
                    // city doesn't exist in Geocodes for provided label
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                else
                    if ( cities.size( ) > 1 )
                    {
                        // Multiple cities exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                    {
                        // One city exists in Geocodes for provided label
                        // Create city label attribute
                        final AttributeStatus labelStatus = createAttribute( cityLabelToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( labelStatus );

                        // create city code attribute
                        final String countryGeocodesCode = cities.get( 0 ).getCode( );
                        cityCodeToCreate = new CertifiedAttribute( );
                        cityCodeToCreate.setKey( ATTR_KEY_BIRTHPLACE_CODE );
                        cityCodeToCreate.setValue( countryGeocodesCode );
                        cityCodeToCreate.setCertificationProcess( cityLabelToCreate.getCertificationProcess( ) );
                        cityCodeToCreate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                        final AttributeStatus codeStatus = createAttribute( cityCodeToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( codeStatus );
                    }
            }
        }
    }

    /**
     * Private methode used to process both "birthcountry_code" and "birthcountry" attributes during an update process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param attrToUpdate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    private void processCountryForUpdate( final Identity identity, final List<CertifiedAttribute> attrToCreate, final List<CertifiedAttribute> attrToUpdate,
            final String clientCode, final IdentityChangeResponse response ) throws IdentityStoreException
    {

        final CertifiedAttribute countryCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHCOUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToCreate != null )
        {
            attrToCreate.remove( countryCodeToCreate );
        }
        CertifiedAttribute countryLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHCOUNTRY ) ).findFirst( ).orElse( null );
        if ( countryLabelToCreate != null )
        {
            attrToCreate.remove( countryLabelToCreate );
        }
        final CertifiedAttribute countryCodeToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHCOUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToUpdate != null )
        {
            attrToUpdate.remove( countryCodeToUpdate );
        }
        CertifiedAttribute countryLabelToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHCOUNTRY ) ).findFirst( ).orElse( null );
        if ( countryLabelToUpdate != null )
        {
            attrToUpdate.remove( countryLabelToUpdate );
        }

        // Country code to CREATE
        if ( countryCodeToCreate != null )
        {
            final Country country = GeoCodesService.getCountryByCode( countryCodeToCreate.getValue( ) ).orElse( null );
            if ( country == null )
            {
                // Country doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( countryCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // Country exists in Geocodes for provided code
                // create country code attribute
                final AttributeStatus codeStatus = createAttribute( countryCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create country label attribute if it doesn't already exist in the identity
                // Geocodes label value is used, regardless if a label is provided or not
                final String countryGeocodesLabel = country.getValue( );
                if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHCOUNTRY ) )
                {
                    final AttributeChangeStatus labelStatus = ( countryLabelToCreate == null
                            || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) ) ? AttributeChangeStatus.CREATED
                                    : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                    if ( countryLabelToCreate == null )
                    {
                        countryLabelToCreate = new CertifiedAttribute( );
                        countryLabelToCreate.setKey( ATTR_KEY_BIRTHCOUNTRY );
                    }
                    countryLabelToCreate.setValue( countryGeocodesLabel );
                    countryLabelToCreate.setCertificationProcess( countryCodeToCreate.getCertificationProcess( ) );
                    countryLabelToCreate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                    final AttributeStatus attributeStatus = createAttribute( countryLabelToCreate, identity, clientCode );
                    attributeStatus.setStatus( labelStatus );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                // update country label if attribute exists, and value is different from existing
                else
                    if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHCOUNTRY ).getValue( ).equals( countryGeocodesLabel ) )
                    {
                        final boolean override = ( countryLabelToUpdate != null && !countryLabelToUpdate.getValue( ).equals( countryGeocodesLabel ) );
                        if ( countryLabelToUpdate == null )
                        {
                            countryLabelToUpdate = new CertifiedAttribute( );
                            countryLabelToUpdate.setKey( ATTR_KEY_BIRTHCOUNTRY );
                        }
                        countryLabelToUpdate.setValue( countryGeocodesLabel );
                        countryLabelToUpdate.setCertificationProcess( countryCodeToCreate.getCertificationProcess( ) );
                        countryLabelToUpdate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = updateAttribute( countryLabelToUpdate, identity, clientCode );
                        if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                        {
                            attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                        }
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
            }
        }
        // Country code to UPDATE
        else
            if ( countryCodeToUpdate != null )
            {
                final Country country = GeoCodesService.getCountryByCode( countryCodeToUpdate.getValue( ) ).orElse( null );
                if ( country == null )
                {
                    // Country doesn't exist in Geocodes for provided code
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( countryCodeToUpdate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                else
                {
                    // Country exists in Geocodes for provided code
                    // update country code attribute
                    final AttributeStatus codeStatus = updateAttribute( countryCodeToUpdate, identity, clientCode );
                    response.getAttributeStatuses( ).add( codeStatus );

                    // create country label attribute if it doesn't already exist in the identity
                    // Geocodes label value is used, regardless if a label is provided or not
                    final String countryGeocodesLabel = country.getValue( );
                    if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHCOUNTRY ) )
                    {
                        final AttributeChangeStatus labelStatus = ( countryLabelToCreate == null
                                || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) ) ? AttributeChangeStatus.CREATED
                                        : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                        if ( countryLabelToCreate == null )
                        {
                            countryLabelToCreate = new CertifiedAttribute( );
                            countryLabelToCreate.setKey( ATTR_KEY_BIRTHCOUNTRY );
                        }
                        countryLabelToCreate.setValue( countryGeocodesLabel );
                        countryLabelToCreate.setCertificationProcess( countryCodeToUpdate.getCertificationProcess( ) );
                        countryLabelToCreate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = createAttribute( countryLabelToCreate, identity, clientCode );
                        attributeStatus.setStatus( labelStatus );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    // update country label if attribute exists, and value is different from existing
                    else
                        if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHCOUNTRY ).getValue( ).equals( countryGeocodesLabel ) )
                        {
                            final boolean override = ( countryLabelToUpdate != null && !countryLabelToUpdate.getValue( ).equals( countryGeocodesLabel ) );
                            if ( countryLabelToUpdate == null )
                            {
                                countryLabelToUpdate = new CertifiedAttribute( );
                                countryLabelToUpdate.setKey( ATTR_KEY_BIRTHCOUNTRY );
                            }
                            countryLabelToUpdate.setValue( countryGeocodesLabel );
                            countryLabelToUpdate.setCertificationProcess( countryCodeToUpdate.getCertificationProcess( ) );
                            countryLabelToUpdate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );

                            final AttributeStatus attributeStatus = updateAttribute( countryLabelToUpdate, identity, clientCode );
                            if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                            {
                                attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                            }
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                }
            }
            // No country code sent, checking if label was sent
            else
            {
                if ( countryLabelToCreate != null )
                {
                    final List<Country> countries = GeoCodesService.getCountriesListByName( countryLabelToCreate.getValue( ) );
                    if ( CollectionUtils.isEmpty( countries ) )
                    {
                        // Country doesn't exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                        if ( countries.size( ) > 1 )
                        {
                            // Multiple countries exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        else
                        {
                            // One country exists in Geocodes for provided label
                            // Create country label attribute
                            final AttributeStatus labelStatus = createAttribute( countryLabelToCreate, identity, clientCode );
                            response.getAttributeStatuses( ).add( labelStatus );

                            // create country code attribute if it doesn't already exist in the identity
                            final String countryGeocodesCode = countries.get( 0 ).getCode( );
                            if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHCOUNTRY_CODE ) )
                            {
                                final CertifiedAttribute codeToCreate = new CertifiedAttribute( );
                                codeToCreate.setKey( ATTR_KEY_BIRTHCOUNTRY_CODE );
                                codeToCreate.setValue( countryGeocodesCode );
                                codeToCreate.setCertificationProcess( countryLabelToCreate.getCertificationProcess( ) );
                                codeToCreate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                                final AttributeStatus codeStatus = createAttribute( codeToCreate, identity, clientCode );
                                response.getAttributeStatuses( ).add( codeStatus );
                            }
                            // update country code if attribute exists, and value is different from existing
                            else
                                if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHCOUNTRY_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                {
                                    final CertifiedAttribute codeToUpdate = new CertifiedAttribute( );
                                    codeToUpdate.setKey( ATTR_KEY_BIRTHCOUNTRY_CODE );
                                    codeToUpdate.setValue( countryGeocodesCode );
                                    codeToUpdate.setCertificationProcess( countryLabelToCreate.getCertificationProcess( ) );
                                    codeToUpdate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = updateAttribute( codeToUpdate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( codeStatus );
                                }
                        }
                }
                else
                    if ( countryLabelToUpdate != null )
                    {
                        final List<Country> countries = GeoCodesService.getCountriesListByName( countryLabelToUpdate.getValue( ) );
                        if ( CollectionUtils.isEmpty( countries ) )
                        {
                            // Country doesn't exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( countryLabelToUpdate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        else
                            if ( countries.size( ) > 1 )
                            {
                                // Multiple countries exist in Geocodes for provided label
                                final AttributeStatus attributeStatus = new AttributeStatus( );
                                attributeStatus.setKey( countryLabelToUpdate.getKey( ) );
                                attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                                response.getAttributeStatuses( ).add( attributeStatus );
                            }
                            else
                            {
                                // One country exists in Geocodes for provided label
                                // Update country label attribute
                                final AttributeStatus labelStatus = updateAttribute( countryLabelToUpdate, identity, clientCode );
                                response.getAttributeStatuses( ).add( labelStatus );

                                // create country code attribute if it doesn't already exist in the identity
                                final String countryGeocodesCode = countries.get( 0 ).getCode( );
                                if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHCOUNTRY_CODE ) )
                                {
                                    final CertifiedAttribute codeToCreate = new CertifiedAttribute( );
                                    codeToCreate.setKey( ATTR_KEY_BIRTHCOUNTRY_CODE );
                                    codeToCreate.setValue( countryGeocodesCode );
                                    codeToCreate.setCertificationProcess( countryLabelToUpdate.getCertificationProcess( ) );
                                    codeToCreate.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = createAttribute( codeToCreate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( codeStatus );
                                }
                                // update country code if attribute exists, and value is different from existing
                                else
                                    if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHCOUNTRY_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                    {
                                        final CertifiedAttribute codeToUpdate = new CertifiedAttribute( );
                                        codeToUpdate.setKey( ATTR_KEY_BIRTHCOUNTRY_CODE );
                                        codeToUpdate.setValue( countryGeocodesCode );
                                        codeToUpdate.setCertificationProcess( countryLabelToUpdate.getCertificationProcess( ) );
                                        codeToUpdate.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );

                                        final AttributeStatus codeStatus = updateAttribute( codeToUpdate, identity, clientCode );
                                        response.getAttributeStatuses( ).add( codeStatus );
                                    }
                            }
                    }
            }
    }

    /**
     * Private methode used to process both "birthplace_code" and "birthplace" attributes during an update process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param attrToUpdate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    private void processCityForUpdate( final Identity identity, final List<CertifiedAttribute> attrToCreate, final List<CertifiedAttribute> attrToUpdate,
            final String clientCode, final IdentityChangeResponse response ) throws IdentityStoreException
    {

        final CertifiedAttribute cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHPLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
        }
        CertifiedAttribute cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHPLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
        }
        final CertifiedAttribute cityCodeToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHPLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToUpdate != null )
        {
            attrToUpdate.remove( cityCodeToUpdate );
        }
        CertifiedAttribute cityLabelToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( ATTR_KEY_BIRTHPLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToUpdate != null )
        {
            attrToUpdate.remove( cityLabelToUpdate );
        }

        // City code to CREATE
        if ( cityCodeToCreate != null )
        {
            final City city = GeoCodesService.getCityByCode( cityCodeToCreate.getValue( ) ).orElse( null );
            if ( city == null )
            {
                // city doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( cityCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // city exists in Geocodes for provided code
                // create city code attribute
                final AttributeStatus codeStatus = createAttribute( cityCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create city label attribute if it doesn't already exist in the identity
                // Geocodes label value is used, regardless if a label is provided or not
                final String cityGeocodesLabel = city.getValue( );
                if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHPLACE ) )
                {
                    final AttributeChangeStatus labelStatus = ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                            ? AttributeChangeStatus.CREATED
                            : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                    if ( cityLabelToCreate == null )
                    {
                        cityLabelToCreate = new CertifiedAttribute( );
                        cityLabelToCreate.setKey( ATTR_KEY_BIRTHPLACE );
                    }
                    cityLabelToCreate.setValue( cityGeocodesLabel );
                    cityLabelToCreate.setCertificationProcess( cityCodeToCreate.getCertificationProcess( ) );
                    cityLabelToCreate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                    final AttributeStatus attributeStatus = createAttribute( cityLabelToCreate, identity, clientCode );
                    attributeStatus.setStatus( labelStatus );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                // update city label if attribute exists, and value is different from existing
                else
                    if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHPLACE ).getValue( ).equals( cityGeocodesLabel ) )
                    {
                        final boolean override = ( cityLabelToUpdate != null && !cityLabelToUpdate.getValue( ).equals( cityGeocodesLabel ) );
                        if ( cityLabelToUpdate == null )
                        {
                            cityLabelToUpdate = new CertifiedAttribute( );
                            cityLabelToUpdate.setKey( ATTR_KEY_BIRTHPLACE );
                        }
                        cityLabelToUpdate.setValue( cityGeocodesLabel );
                        cityLabelToUpdate.setCertificationProcess( cityCodeToCreate.getCertificationProcess( ) );
                        cityLabelToUpdate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = updateAttribute( cityLabelToUpdate, identity, clientCode );
                        if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                        {
                            attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                        }
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
            }
        }
        // city code to UPDATE
        else
            if ( cityCodeToUpdate != null )
            {
                final City city = GeoCodesService.getCityByCode( cityCodeToUpdate.getValue( ) ).orElse( null );
                if ( city == null )
                {
                    // city doesn't exist in Geocodes for provided code
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( cityCodeToUpdate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                else
                {
                    // city exists in Geocodes for provided code
                    // update city code attribute
                    final AttributeStatus codeStatus = updateAttribute( cityCodeToUpdate, identity, clientCode );
                    response.getAttributeStatuses( ).add( codeStatus );

                    // create city label attribute if it doesn't already exist in the identity
                    // Geocodes label value is used, regardless if a label is provided or not
                    final String cityGeocodesLabel = city.getValue( );
                    if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHPLACE ) )
                    {
                        final AttributeChangeStatus labelStatus = ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                                ? AttributeChangeStatus.CREATED
                                : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                        if ( cityLabelToCreate == null )
                        {
                            cityLabelToCreate = new CertifiedAttribute( );
                            cityLabelToCreate.setKey( ATTR_KEY_BIRTHPLACE );
                        }
                        cityLabelToCreate.setValue( cityGeocodesLabel );
                        cityLabelToCreate.setCertificationProcess( cityCodeToUpdate.getCertificationProcess( ) );
                        cityLabelToCreate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = createAttribute( cityLabelToCreate, identity, clientCode );
                        attributeStatus.setStatus( labelStatus );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    // update city label if attribute exists, and value is different from existing
                    else
                        if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHPLACE ).getValue( ).equals( cityGeocodesLabel ) )
                        {
                            final boolean override = ( cityLabelToUpdate != null && !cityLabelToUpdate.getValue( ).equals( cityGeocodesLabel ) );
                            if ( cityLabelToUpdate == null )
                            {
                                cityLabelToUpdate = new CertifiedAttribute( );
                                cityLabelToUpdate.setKey( ATTR_KEY_BIRTHPLACE );
                            }
                            cityLabelToUpdate.setValue( cityGeocodesLabel );
                            cityLabelToUpdate.setCertificationProcess( cityCodeToUpdate.getCertificationProcess( ) );
                            cityLabelToUpdate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );

                            final AttributeStatus attributeStatus = updateAttribute( cityLabelToUpdate, identity, clientCode );
                            if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                            {
                                attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                            }
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                }
            }
            // No city code sent, checking if label was sent
            else
            {
                if ( cityLabelToCreate != null )
                {
                    final List<City> cities = GeoCodesService.getCitiesListByName( cityLabelToCreate.getValue( ) );
                    if ( CollectionUtils.isEmpty( cities ) )
                    {
                        // city doesn't exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                        if ( cities.size( ) > 1 )
                        {
                            // Multiple cities exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        else
                        {
                            // One city exists in Geocodes for provided label
                            // Create city label attribute
                            final AttributeStatus labelStatus = createAttribute( cityLabelToCreate, identity, clientCode );
                            response.getAttributeStatuses( ).add( labelStatus );

                            // create city code attribute if it doesn't already exist in the identity
                            final String cityGeocodesCode = cities.get( 0 ).getCode( );
                            if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHPLACE_CODE ) )
                            {
                                final CertifiedAttribute codeToCreate = new CertifiedAttribute( );
                                codeToCreate.setKey( ATTR_KEY_BIRTHPLACE_CODE );
                                codeToCreate.setValue( cityGeocodesCode );
                                codeToCreate.setCertificationProcess( cityLabelToCreate.getCertificationProcess( ) );
                                codeToCreate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                                final AttributeStatus codeStatus = createAttribute( codeToCreate, identity, clientCode );
                                response.getAttributeStatuses( ).add( codeStatus );
                            }
                            // update city code if attribute exists, and value is different from existing
                            else
                                if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHPLACE_CODE ).getValue( ).equals( cityGeocodesCode ) )
                                {
                                    final CertifiedAttribute codeToUpdate = new CertifiedAttribute( );
                                    codeToUpdate.setKey( ATTR_KEY_BIRTHPLACE_CODE );
                                    codeToUpdate.setValue( cityGeocodesCode );
                                    codeToUpdate.setCertificationProcess( cityLabelToCreate.getCertificationProcess( ) );
                                    codeToUpdate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = updateAttribute( codeToUpdate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( codeStatus );
                                }
                        }
                }
                else
                    if ( cityLabelToUpdate != null )
                    {
                        final List<City> cities = GeoCodesService.getCitiesListByName( cityLabelToUpdate.getValue( ) );
                        if ( CollectionUtils.isEmpty( cities ) )
                        {
                            // city doesn't exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( cityLabelToUpdate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        else
                            if ( cities.size( ) > 1 )
                            {
                                // Multiple cities exist in Geocodes for provided label
                                final AttributeStatus attributeStatus = new AttributeStatus( );
                                attributeStatus.setKey( cityLabelToUpdate.getKey( ) );
                                attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                                response.getAttributeStatuses( ).add( attributeStatus );
                            }
                            else
                            {
                                // One city exists in Geocodes for provided label
                                // Update city label attribute
                                final AttributeStatus labelStatus = updateAttribute( cityLabelToUpdate, identity, clientCode );
                                response.getAttributeStatuses( ).add( labelStatus );

                                // create city code attribute if it doesn't already exist in the identity
                                final String countryGeocodesCode = cities.get( 0 ).getCode( );
                                if ( !identity.getAttributes( ).containsKey( ATTR_KEY_BIRTHPLACE_CODE ) )
                                {
                                    final CertifiedAttribute codeToCreate = new CertifiedAttribute( );
                                    codeToCreate.setKey( ATTR_KEY_BIRTHPLACE_CODE );
                                    codeToCreate.setValue( countryGeocodesCode );
                                    codeToCreate.setCertificationProcess( cityLabelToUpdate.getCertificationProcess( ) );
                                    codeToCreate.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = createAttribute( codeToCreate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( codeStatus );
                                }
                                // update city code if attribute exists, and value is different from existing
                                else
                                    if ( !identity.getAttributes( ).get( ATTR_KEY_BIRTHPLACE_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                    {
                                        final CertifiedAttribute codeToUpdate = new CertifiedAttribute( );
                                        codeToUpdate.setKey( ATTR_KEY_BIRTHPLACE_CODE );
                                        codeToUpdate.setValue( countryGeocodesCode );
                                        codeToUpdate.setCertificationProcess( cityLabelToUpdate.getCertificationProcess( ) );
                                        codeToUpdate.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );

                                        final AttributeStatus codeStatus = updateAttribute( codeToUpdate, identity, clientCode );
                                        response.getAttributeStatuses( ).add( codeStatus );
                                    }
                            }
                    }
            }
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
    private AttributeStatus createAttribute( final CertifiedAttribute attributeToCreate, final Identity identity, final String clientCode )
            throws IdentityStoreException
    {
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
    private AttributeStatus updateAttribute( final CertifiedAttribute attributeToUpdate, final Identity identity, final String clientCode )
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

    /**
     * Merges two existing {@link Identity} specified in the given {@link IdentityMergeRequest} and following the given rules: <br>
     * <ul>
     * <li>Both {@link Identity} must exist and not be merged or deleted in te database. If not, FAILURE status is returned in the execution response</li>
     * <li>The {@link IdentityAttribute}(s) of the secondary {@link Identity} are processed, according to the list of keys specified in the
     * {@link IdentityMergeRequest}.
     * <ul>
     * <li>If the {@link IdentityAttribute} is not present in the primary {@link Identity}, it is created.</li>
     * <li>If the {@link IdentityAttribute} is present in the primary {@link Identity}, it is updated if the certification process is higher and the value is
     * different.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param identityMergeRequest
     *            the {@link IdentityMergeRequest} holding the parameters of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityMergeResponse} holding the status of the execution of the request
     * @return the merged {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity merge( final IdentityMergeRequest identityMergeRequest, final String clientCode, final IdentityMergeResponse response )
            throws IdentityStoreException
    {
        final Identity primaryIdentity = IdentityHome.findByCustomerId( identityMergeRequest.getIdentities( ).getPrimaryCuid( ) );
        if ( primaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.getMessage( ).add( "No identity could be found with customer_id " + identityMergeRequest.getIdentities( ).getPrimaryCuid( ) );
        }
        else
            if ( primaryIdentity.isDeleted( ) )
            {
                response.setStatus( IdentityMergeStatus.FAILURE );
                response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getPrimaryCuid( ) + " is deleted" );
            }
            else
                if ( primaryIdentity.isMerged( ) )
                {
                    response.setStatus( IdentityMergeStatus.FAILURE );
                    response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getPrimaryCuid( ) + " is merged" );
                }

        final Identity secondaryIdentity = IdentityHome.findByCustomerId( identityMergeRequest.getIdentities( ).getSecondaryCuid( ) );
        if ( secondaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.getMessage( ).add( "No identity could be found with customer_id " + identityMergeRequest.getIdentities( ).getSecondaryCuid( ) );
        }
        else
            if ( secondaryIdentity.isDeleted( ) )
            {
                response.setStatus( IdentityMergeStatus.FAILURE );
                response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getSecondaryCuid( ) + " is deleted" );
            }
            else
                if ( secondaryIdentity.isMerged( ) )
                {
                    response.setStatus( IdentityMergeStatus.FAILURE );
                    response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getSecondaryCuid( ) + " is merged" );
                }

        /* Return a FAIL status in case of problem */
        if ( IdentityMergeStatus.FAILURE.equals( response.getStatus( ) ) )
        {
            return null;
        }

        final List<Map.Entry<String, IdentityAttribute>> entriesToProcess = secondaryIdentity.getAttributes( ).entrySet( ).stream( )
                .filter( e -> identityMergeRequest.getIdentities( ).getAttributeKeys( ).contains( e.getKey( ) ) ).collect( Collectors.toList( ) );
        for ( final Map.Entry<String, IdentityAttribute> entry : entriesToProcess )
        {
            final String key = entry.getKey( );
            final IdentityAttribute secondaryAttribute = entry.getValue( );
            IdentityAttribute primaryAttribute = primaryIdentity.getAttributes( ).get( key );
            if ( primaryAttribute == null )
            { // l'attribut n'existe pas, on le crée
                primaryAttribute = new IdentityAttribute( );
                primaryAttribute.setIdIdentity( primaryIdentity.getId( ) );
                primaryAttribute.setAttributeKey( secondaryAttribute.getAttributeKey( ) );
                primaryAttribute.setValue( secondaryAttribute.getValue( ) );
                primaryAttribute.setLastUpdateClientCode( clientCode );

                if ( secondaryAttribute.getCertificate( ) != null )
                {
                    final AttributeCertificate certificate = new AttributeCertificate( );
                    certificate.setCertificateDate( new Timestamp( secondaryAttribute.getCertificate( ).getCertificateDate( ).getTime( ) ) );
                    certificate.setCertifierCode( secondaryAttribute.getCertificate( ).getCertifierCode( ) );
                    certificate.setCertifierName( secondaryAttribute.getCertificate( ).getCertifierName( ) );
                    primaryAttribute.setCertificate( AttributeCertificateHome.create( certificate ) );
                    primaryAttribute.setIdCertificate( primaryAttribute.getCertificate( ).getId( ) );
                }
                IdentityAttributeHome.create( primaryAttribute );
                primaryIdentity.getAttributes( ).put( primaryAttribute.getAttributeKey( ).getKeyName( ), primaryAttribute );

                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( primaryAttribute.getAttributeKey( ).getKeyName( ) );
                attributeStatus.setStatus( AttributeChangeStatus.CREATED );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            { // l'attribut existe, on le met à jour si le niveau de certification est plus élevé
                final int primaryLevel = _attributeCertificationDefinitionService.getLevelAsInteger( primaryAttribute.getCertificate( ).getCertifierCode( ),
                        primaryAttribute.getAttributeKey( ).getKeyName( ) );
                final int secondaryLevel = _attributeCertificationDefinitionService.getLevelAsInteger( secondaryAttribute.getCertificate( ).getCertifierCode( ),
                        secondaryAttribute.getAttributeKey( ).getKeyName( ) );

                if ( secondaryLevel > primaryLevel )
                {
                    primaryAttribute.setValue( secondaryAttribute.getValue( ) );
                    primaryAttribute.setLastUpdateClientCode( clientCode );
                    if ( secondaryAttribute.getCertificate( ) != null )
                    {
                        final AttributeCertificate certificate = new AttributeCertificate( );
                        certificate.setCertificateDate( new Timestamp( secondaryAttribute.getCertificate( ).getCertificateDate( ).getTime( ) ) );
                        certificate.setCertifierCode( secondaryAttribute.getCertificate( ).getCertifierCode( ) );
                        certificate.setCertifierName( secondaryAttribute.getCertificate( ).getCertifierName( ) );
                        primaryAttribute.setCertificate( AttributeCertificateHome.create( certificate ) );
                        primaryAttribute.setIdCertificate( primaryAttribute.getCertificate( ).getId( ) );
                    }
                    IdentityAttributeHome.update( primaryAttribute );
                    primaryIdentity.getAttributes( ).put( primaryAttribute.getAttributeKey( ).getKeyName( ), primaryAttribute );

                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( key );
                    attributeStatus.setStatus( AttributeChangeStatus.UPDATED );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
            }
        }

        /* Tag de l'identité secondaire */
        secondaryIdentity.setMerged( true );
        secondaryIdentity.setMasterIdentityId( primaryIdentity.getId( ) );
        IdentityHome.merge( secondaryIdentity );

        response.setStatus( IdentityMergeStatus.SUCCESS );
        response.setCustomerId( primaryIdentity.getCustomerId( ) );

        /* Historique des modifications */
        response.getAttributeStatuses( ).forEach( attributeStatus -> {
            AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.MERGE, primaryIdentity,
                    attributeStatus, identityMergeRequest.getOrigin( ), clientCode );
            _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
        } );

        /* Indexation, journalisation, ... */
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( secondaryIdentity, IdentityChangeType.MERGED, identityMergeRequest.getOrigin( ) ) );
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( primaryIdentity, IdentityChangeType.CONSOLIDATED, identityMergeRequest.getOrigin( ) ) );

        return primaryIdentity;
    }

    /**
     * Imports an {@link Identity} according to the given {@link IdentityChangeRequest}
     *
     * @param identityChangeRequest
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the imported {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity importIdentity( final IdentityChangeRequest identityChangeRequest, final String clientCode, final IdentityChangeResponse response )
            throws IdentityStoreException
    {
        final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.toMap( CertifiedAttribute::getKey, CertifiedAttribute::getValue ) );

        final DuplicateDto certitudeDuplicates = _duplicateServiceImportCertitude.findDuplicates( attributes );
        if ( certitudeDuplicates != null && CollectionUtils.isNotEmpty( certitudeDuplicates.getIdentities( ) ) )
        {
            if ( certitudeDuplicates.getIdentities( ).size( ) == 1 )
            {
                return this.update( certitudeDuplicates.getIdentities( ).get( 0 ).getCustomerId( ), identityChangeRequest, clientCode, response );
            }
        }

        final DuplicateDto suspicionDuplicates = _duplicateServiceImportSuspicion.findDuplicates( attributes );
        if ( suspicionDuplicates != null && CollectionUtils.isNotEmpty( suspicionDuplicates.getIdentities( ) ) )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            // response.setDuplicates( suspicionDuplicates );
            response.setMessage( "Found duplicates" );
        }
        else
        {
            return this.create( identityChangeRequest, clientCode, response );
        }

        return null;
    }

    public void fullIndexing( )
    {
        new FullIndexTask( ).run( );
    }

    public AttributeKey getAttributeKey( final String keyName ) throws IdentityAttributeNotFoundException
    {
        return _cache.get( keyName );
    }

    public List<AttributeKey> getCommonAttributeKeys( final String keyName )
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
     * Perform an identity research over a list of attributes (key and values) specified in the {@link IdentitySearchRequest}
     *
     * @param identitySearchRequest
     *            the {@link IdentitySearchRequest} holding the parameters of the research
     * @param response
     *            the {@link IdentitySearchResponse} holding the status of the execution status and the results of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @throws ServiceContractNotFoundException
     *             in case of {@link ServiceContract} management error
     * @throws IdentityAttributeNotFoundException
     *             in case of {@link AttributeKey} management error
     */
    public void search( final IdentitySearchRequest identitySearchRequest, final IdentitySearchResponse response, final String clientCode )
            throws ServiceContractNotFoundException, IdentityAttributeNotFoundException, RefAttributeCertificationDefinitionNotFoundException
    {
        final List<SearchAttributeDto> providedAttributes = identitySearchRequest.getSearch().getAttributes();
        final Set<String> providedKeys = providedAttributes.stream().map(SearchAttributeDto::getKey).collect(Collectors.toSet());

        boolean hasRequirements = false;
        final List<IdentitySearchRule> searchRules = IdentitySearchRuleHome.findAll();
        final Iterator<IdentitySearchRule> iterator = searchRules.iterator();
        while(!hasRequirements && iterator.hasNext()){
            final IdentitySearchRule searchRule = iterator.next();
            final Set<String> requiredKeys = searchRule.getAttributes().stream().map(AttributeKey::getKeyName).collect(Collectors.toSet());
            if (searchRule.getType() == SearchRuleType.AND) {
                if(providedKeys.containsAll(requiredKeys)) {
                    hasRequirements = true;
                }
            } else if (searchRule.getType() == SearchRuleType.OR) {
                if(providedKeys.stream().anyMatch(requiredKeys::contains)) {
                    hasRequirements = true;
                }
            }
        }

        if(!hasRequirements){
            final StringBuilder sb = new StringBuilder();
            final Iterator<IdentitySearchRule> ruleIt = searchRules.iterator();
            while(ruleIt.hasNext()) {
                final IdentitySearchRule rule = ruleIt.next();
                sb.append("( ");
                final Iterator<AttributeKey> attrIt = rule.getAttributes().iterator();
                while(attrIt.hasNext()){
                    final AttributeKey attr = attrIt.next();
                    sb.append(attr.getKeyName()).append(" ");
                    if(attrIt.hasNext()) {
                        sb.append(rule.getType().name()).append(" ");
                    }
                }
                sb.append(")");
                if(ruleIt.hasNext()){
                    sb.append(" OR ");
                }
            }
            final IdentitySearchMessage alert = new IdentitySearchMessage( );
            alert.setAttributeName( sb.toString() );
            alert.setMessage( "Please provide those required attributes to be able to search identities." );
            response.getAlerts( ).add( alert );
            response.setStatus(IdentitySearchStatusType.FAILURE);
            return;
        }

        final List<QualifiedIdentity> qualifiedIdentities = _searchIdentityService.getQualifiedIdentities(providedAttributes, identitySearchRequest.getMax( ), identitySearchRequest.isConnected( ) );
        if ( CollectionUtils.isNotEmpty( qualifiedIdentities ) )
        {
            final List<QualifiedIdentity> filteredIdentities = this.getFilteredQualifiedIdentities( identitySearchRequest, clientCode, qualifiedIdentities );
            response.setIdentities( filteredIdentities );
            if ( CollectionUtils.isNotEmpty( response.getIdentities( ) ) )
            {
                response.setStatus( IdentitySearchStatusType.SUCCESS );
            }
            else
            {
                response.setStatus( IdentitySearchStatusType.NOT_FOUND );
            }
        }
        else
        {
            response.setStatus( IdentitySearchStatusType.NOT_FOUND );
        }
    }

    /**
     * Perform an identity research by customer or connection ID.
     *
     * @param customerId
     * @param connectionId
     * @param response
     * @param clientCode
     * @throws IdentityAttributeNotFoundException
     * @throws ServiceContractNotFoundException
     */
    public void search( final String customerId, final String connectionId, final IdentitySearchResponse response, final String clientCode )
            throws IdentityAttributeNotFoundException, ServiceContractNotFoundException, RefAttributeCertificationDefinitionNotFoundException
    {
        final Identity identity = StringUtils.isNotEmpty( customerId ) ? IdentityHome.findMasterIdentityByCustomerId( customerId )
                : StringUtils.isNotEmpty( connectionId ) ? IdentityHome.findMasterIdentityByConnectionId( connectionId ) : null;
        if ( identity == null )
        {
            response.setStatus( IdentitySearchStatusType.NOT_FOUND );
        }
        else
        {
            final QualifiedIdentity qualifiedIdentity = DtoConverter.convertIdentityToDto( identity );
            final List<QualifiedIdentity> filteredIdentities = this.getFilteredQualifiedIdentities( null, clientCode, Arrays.asList( qualifiedIdentity ) );
            response.setIdentities( filteredIdentities );
            if ( CollectionUtils.isNotEmpty( response.getIdentities( ) ) )
            {
                response.setStatus( IdentitySearchStatusType.SUCCESS );
            }
            else
            {
                response.setStatus( IdentitySearchStatusType.NOT_FOUND );
            }
        }
    }

    private List<QualifiedIdentity> getFilteredQualifiedIdentities( IdentitySearchRequest identitySearchRequest, String clientCode,
            List<QualifiedIdentity> qualifiedIdentities ) throws ServiceContractNotFoundException, IdentityAttributeNotFoundException
    {
        final ServiceContract serviceContract = _serviceContractService.getActiveServiceContract( clientCode );
        final Comparator<QualifiedIdentity> comparator = Comparator.comparingDouble( QualifiedIdentity::getScoring )
                .thenComparingDouble( QualifiedIdentity::getQuality ).reversed( );

        final List<QualifiedIdentity> filteredIdentities = new ArrayList<>( );
        for ( final QualifiedIdentity qualifiedIdentity : qualifiedIdentities )
        {
            if ( CollectionUtils.isNotEmpty( qualifiedIdentity.getAttributes( ) ) && !qualifiedIdentity.isMerged( ) )
            {
                IdentityQualityService.instance( ).computeCoverage( qualifiedIdentity, serviceContract );
                // TODO gérer la qualité max dans la requête ?
                if ( identitySearchRequest != null )
                {
                    IdentityQualityService.instance( ).computeMatchScore( qualifiedIdentity, identitySearchRequest.getSearch( ).getAttributes( ) );
                }
                else
                {
                    qualifiedIdentity.setScoring( 1.0 );
                }
                IdentityQualityService.instance( ).computeQuality( qualifiedIdentity );
                final List<fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute> filteredAttributeValues = qualifiedIdentity
                        .getAttributes( ).stream( )
                        .filter( certifiedAttribute -> serviceContract.getAttributeRights( ).stream( )
                                .anyMatch( attributeRight -> StringUtils.equals( attributeRight.getAttributeKey( ).getKeyName( ), certifiedAttribute.getKey( ) )
                                        && attributeRight.isReadable( ) ) )
                        .collect( Collectors.toList( ) );
                qualifiedIdentity.getAttributes( ).clear( );
                qualifiedIdentity.getAttributes( ).addAll( filteredAttributeValues );
                filteredIdentities.add( qualifiedIdentity );
            }
        }
        filteredIdentities.sort( comparator );
        return filteredIdentities;
    }

    /**
     * Gets a list of identities on which to search potential duplicates.<br/>
     * Returned identities must have all attributes checked by the provided rule, and must also not be already merged nor be tagged as suspicious.
     *
     * @param rule
     *            the rule used to get matching identities
     * @return the list of identities
     */
    public List<QualifiedIdentity> getIdentitiesBatchForPotentialDuplicate( final DuplicateRule rule, final int limit )
    {
        if ( rule == null )
        {
            return Collections.emptyList( );
        }
        return _searchDbIdentityService.getQualifiedIdentitiesHavingAttributes( rule.getCheckedAttributes( ), limit, true, true );
    }

    /**
     * Search and returns the number of potential duplicates of the identity corresponding to the provided customer ID, according the the provided rule.
     *
     * @param customerId
     *            the customer ID
     * @param rule
     *            the duplicate rule
     * @return number of potential duplicates found
     */
    public int getNumberOfPotentialDuplicates( final String customerId, final DuplicateRule rule )
    {
        // TODO NOT IMPLEMENTED YET
        return 0;
    }


    public void deleteRequest( final String customerId, final String clientCode,
    		IdentityChangeRequest identityChangeRequest,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {
        if ( !_serviceContractService.canDeleteIdentity( clientCode ) )
        {
            response.setStatus( IdentityChangeStatus.FAILURE );
            response.setCustomerId( customerId );
            response.setMessage( "The client application is not authorized to request the deletion of an identity." );
            return;
        }

        // check identity
        Identity identity = IdentityHome.findByCustomerId( customerId );
        if ( identity == null )
        {
        	response.setStatus( IdentityChangeStatus.FAILURE );
            response.setCustomerId( customerId );
            response.setMessage( "Identity not found." );
            return;
        }
        if ( identity.isDeleted( ) )
        {
        	response.setStatus( IdentityChangeStatus.FAILURE );
            response.setCustomerId( customerId );
            response.setMessage( "Identity  allready in deleted state." );
            return;
        }
        if ( identity.isMerged( ) )
        {
        	response.setStatus( IdentityChangeStatus.FAILURE );
            response.setCustomerId( customerId );
            response.setMessage( "Identity in merged state can not be deleted." );
            return;
        }

        // expire identity (the  deletion is managed by the dedicated Daemon)
	    IdentityHome.softRemove( customerId );

	    response.setStatus( IdentityChangeStatus.DELETE_SUCCESS );

	    /* Notify listeners for indexation, history, ...  */
	    _identityStoreNotifyListenerService.notifyListenersIdentityChange(
	    		new IdentityChange( identity, IdentityChangeType.DELETE, identityChangeRequest.getOrigin() ) );


    }
}
