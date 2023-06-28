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

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRuleHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.SearchRuleType;
import fr.paris.lutece.plugins.identitystore.service.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.RefAttributeCertificationDefinitionNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.duplicate.IDuplicateService;
import fr.paris.lutece.plugins.identitystore.service.geocodes.GeocodesService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task.FullIndexTask;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.*;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateSearchResponse;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class IdentityService
{
    private final AttributeCertificationDefinitionService _attributeCertificationDefinitionService = AttributeCertificationDefinitionService.instance( );
    private final IdentityStoreNotifyListenerService _identityStoreNotifyListenerService = IdentityStoreNotifyListenerService.instance( );
    private final ServiceContractService _serviceContractService = ServiceContractService.instance( );
    private final IdentityAttributeService _identityAttributeService = IdentityAttributeService.instance( );
    private final IDuplicateService _duplicateServiceCreation = SpringContextService.getBean( "identitystore.duplicateService.creation" );
    private final IDuplicateService _duplicateServiceUpdate = SpringContextService.getBean( "identitystore.duplicateService.update" );
    private final IDuplicateService _duplicateServiceImportCertitude = SpringContextService.getBean( "identitystore.duplicateService.import.certitude" );
    private final IDuplicateService _duplicateServiceImportSuspicion = SpringContextService.getBean( "identitystore.duplicateService.import.suspicion" );
    protected ISearchIdentityService _searchIdentityService = SpringContextService.getBean( "identitystore.searchIdentityService" );
    protected ISearchIdentityService _searchDbIdentityService = SpringContextService.getBean( "identitystore.db.searchIdentityService" );

    private static IdentityService _instance;

    public static IdentityService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityService( );
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

        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getConnectionId( ) ) )
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
        if ( !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            if ( Boolean.TRUE.equals( identityChangeRequest.getIdentity( ).getMonParisActive( ) ) )
            {
                response.setStatus( IdentityChangeStatus.CONFLICT );
                response.setMessage( "The client application is not authorized to initialize the 'mon_paris_active' flag." );
                return null;
            }
        }

        final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .filter( a -> StringUtils.isNotBlank( a.getValue( ) ) ).collect( Collectors.toMap( CertifiedAttribute::getKey, CertifiedAttribute::getValue ) );
        final DuplicateSearchResponse duplicates = _duplicateServiceCreation.findDuplicates( attributes );
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
        GeocodesService.processCountryForCreate( identity, attributesToCreate, clientCode, response );
        GeocodesService.processCityForCreate( identity, attributesToCreate, clientCode, response );

        for ( final CertifiedAttribute certifiedAttribute : attributesToCreate )
        {
            // TODO vérifier que la clef d'attribut existe dans le référentiel
            final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( certifiedAttribute, identity, clientCode );
            response.getAttributeStatuses( ).add( attributeStatus );
        }

        response.setCustomerId( identity.getCustomerId( ) );
        response.setCreationDate( identity.getCreationDate( ) );
        final boolean incompleteCreation = response.getAttributeStatuses( ).stream( )
                .anyMatch( s -> s.getStatus( ).equals( AttributeChangeStatus.NOT_CREATED ) );
        response.setStatus( incompleteCreation ? IdentityChangeStatus.CREATE_INCOMPLETE_SUCCESS : IdentityChangeStatus.CREATE_SUCCESS );

        /* Historique des modifications */
        response.getAttributeStatuses( ).stream( ).filter( s -> s.getStatus( ).equals( AttributeChangeStatus.CREATED ) ).forEach( attributeStatus -> {
            AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.CREATE, identity, attributeStatus,
                    identityChangeRequest.getOrigin( ), clientCode );
            _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
        } );

        /* Indexation */
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.CREATE,
                identity, response.getStatus( ).getCode( ).toString( ), response.getMessage( ), identityChangeRequest.getOrigin( ), clientCode ) );

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

        GeocodesService.processCountryForUpdate( identity, newWritableAttributes, existingWritableAttributes, clientCode, response );
        GeocodesService.processCityForUpdate( identity, newWritableAttributes, existingWritableAttributes, clientCode, response );

        /* Create new attributes */
        for ( final CertifiedAttribute attributeToWrite : newWritableAttributes )
        {
            final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( attributeToWrite, identity, clientCode );
            response.getAttributeStatuses( ).add( attributeStatus );
        }

        /* Update existing attributes */
        for ( final CertifiedAttribute attributeToUpdate : existingWritableAttributes )
        {
            final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( attributeToUpdate, identity, clientCode );
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
                        || AttributeChangeStatus.NOT_CREATED.equals( attributeStatus.getStatus( ) )
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
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.UPDATE,
                identity, response.getStatus( ).getCode( ).toString( ), response.getMessage( ), identityChangeRequest.getOrigin( ), clientCode ) );

        return identity;
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

        /* Indexation */

        _identityStoreNotifyListenerService
                .notifyListenersIdentityChange( IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.DELETE, secondaryIdentity,
                        response.getStatus( ).getCode( ).toString( ), response.getStatus( ).getLabel( ), identityMergeRequest.getOrigin( ), clientCode ) );

        _identityStoreNotifyListenerService
                .notifyListenersIdentityChange( IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.UPDATE, primaryIdentity,
                        response.getStatus( ).getCode( ).toString( ), response.getStatus( ).getLabel( ), identityMergeRequest.getOrigin( ), clientCode ) );

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

        final DuplicateSearchResponse certitudeDuplicates = _duplicateServiceImportCertitude.findDuplicates( attributes );
        if ( certitudeDuplicates != null && CollectionUtils.isNotEmpty( certitudeDuplicates.getIdentities( ) ) )
        {
            if ( certitudeDuplicates.getIdentities( ).size( ) == 1 )
            {
                return this.update( certitudeDuplicates.getIdentities( ).get( 0 ).getCustomerId( ), identityChangeRequest, clientCode, response );
            }
        }

        final DuplicateSearchResponse suspicionDuplicates = _duplicateServiceImportSuspicion.findDuplicates( attributes );
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
        final List<SearchAttributeDto> providedAttributes = identitySearchRequest.getSearch( ).getAttributes( );
        final Set<String> providedKeys = providedAttributes.stream( ).map( SearchAttributeDto::getKey ).collect( Collectors.toSet( ) );

        boolean hasRequirements = false;
        final List<IdentitySearchRule> searchRules = IdentitySearchRuleHome.findAll( );
        final Iterator<IdentitySearchRule> iterator = searchRules.iterator( );
        while ( !hasRequirements && iterator.hasNext( ) )
        {
            final IdentitySearchRule searchRule = iterator.next( );
            final Set<String> requiredKeys = searchRule.getAttributes( ).stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toSet( ) );
            if ( searchRule.getType( ) == SearchRuleType.AND )
            {
                if ( providedKeys.containsAll( requiredKeys ) )
                {
                    hasRequirements = true;
                }
            }
            else
                if ( searchRule.getType( ) == SearchRuleType.OR )
                {
                    if ( providedKeys.stream( ).anyMatch( requiredKeys::contains ) )
                    {
                        hasRequirements = true;
                    }
                }
        }

        if ( !hasRequirements )
        {
            final StringBuilder sb = new StringBuilder( );
            final Iterator<IdentitySearchRule> ruleIt = searchRules.iterator( );
            while ( ruleIt.hasNext( ) )
            {
                final IdentitySearchRule rule = ruleIt.next( );
                sb.append( "( " );
                final Iterator<AttributeKey> attrIt = rule.getAttributes( ).iterator( );
                while ( attrIt.hasNext( ) )
                {
                    final AttributeKey attr = attrIt.next( );
                    sb.append( attr.getKeyName( ) ).append( " " );
                    if ( attrIt.hasNext( ) )
                    {
                        sb.append( rule.getType( ).name( ) ).append( " " );
                    }
                }
                sb.append( ")" );
                if ( ruleIt.hasNext( ) )
                {
                    sb.append( " OR " );
                }
            }
            final IdentitySearchMessage alert = new IdentitySearchMessage( );
            alert.setAttributeName( sb.toString( ) );
            alert.setMessage( "Please provide those required attributes to be able to search identities." );
            response.getAlerts( ).add( alert );
            response.setStatus( IdentitySearchStatusType.FAILURE );
            return;
        }

        final List<QualifiedIdentity> qualifiedIdentities = _searchIdentityService.getQualifiedIdentities( providedAttributes, null, null,
                identitySearchRequest.getMax( ), identitySearchRequest.isConnected( ) );
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

    public DuplicateSearchResponse findDuplicates( Identity identity, Integer ruleId )
    {
        return this._duplicateServiceImportSuspicion.findDuplicates( identity, ruleId );
    }
}
