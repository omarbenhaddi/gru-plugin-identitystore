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
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.ExcludedIdentities;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentity;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentityHome;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRuleHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.SearchRuleType;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.contract.RefAttributeCertificationDefinitionNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.duplicate.IDuplicateService;
import fr.paris.lutece.plugins.identitystore.service.geocodes.GeocodesService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.listener.IndexIdentityChange;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.service.user.InternalUserService;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.IdentityDuplicateDefintion;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.IdentityDuplicateExclusion;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.IdentityDuplicateSuspicion;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.*;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.security.AccessLogService;
import fr.paris.lutece.portal.service.security.AccessLoggerConstants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.sql.TransactionManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class IdentityService
{
    // EVENTS FOR ACCESS LOGGING
    public static final String CREATE_IDENTITY_EVENT_CODE = "CREATE_IDENTITY";
    public static final String UPDATE_IDENTITY_EVENT_CODE = "UPDATE_IDENTITY";
    public static final String GET_IDENTITY_EVENT_CODE = "GET_IDENTITY";
    public static final String SEARCH_IDENTITY_EVENT_CODE = "SEARCH_IDENTITY";
    public static final String DELETE_IDENTITY_EVENT_CODE = "DELETE_IDENTITY";
    public static final String MERGE_IDENTITY_EVENT_CODE = "MERGE_IDENTITY";
    public static final String UNMERGE_IDENTITY_EVENT_CODE = "UNMERGE_IDENTITY";
    public static final String SPECIFIC_ORIGIN = "BO";

    // PROPERTIES
    private static final String PROPERTY_DUPLICATES_IMPORT_RULES_SUSPICION = "identitystore.identity.duplicates.import.rules.suspicion";
    private static final String PROPERTY_DUPLICATES_IMPORT_RULES_STRICT = "identitystore.identity.duplicates.import.rules.strict";
    private static final String PROPERTY_DUPLICATES_CREATION_RULES = "identitystore.identity.duplicates.creation.rules";
    private static final String PROPERTY_DUPLICATES_CHECK_DATABASE_ACTIVATED = "identitystore.identity.duplicates.check.database";

    // SERVICES
    private final IdentityStoreNotifyListenerService _identityStoreNotifyListenerService = IdentityStoreNotifyListenerService.instance( );
    private final ServiceContractService _serviceContractService = ServiceContractService.instance( );
    private final IdentityAttributeService _identityAttributeService = IdentityAttributeService.instance( );
    private final InternalUserService _internalUserService = InternalUserService.getInstance( );
    private final IDuplicateService _duplicateServiceDatabase = SpringContextService.getBean( "identitystore.duplicateService.database" );
    private final IDuplicateService _duplicateServiceElasticSearch = SpringContextService.getBean( "identitystore.duplicateService.elasticsearch" );
    protected ISearchIdentityService _elasticSearchIdentityService = SpringContextService.getBean( "identitystore.searchIdentityService.elasticsearch" );

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
     * @param request
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the created {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity create( final IdentityChangeRequest request, final String clientCode, final IdentityChangeResponse response ) throws IdentityStoreException
    {
        if ( !_serviceContractService.canCreateIdentity( clientCode ) )
        {
            response.setStatus( IdentityChangeStatus.FAILURE );
            response.setMessage( "The client application is not authorized to create an identity." );
            return null;
        }

        if ( StringUtils.isNotEmpty( request.getIdentity( ).getCustomerId( ) ) )
        {
            throw new IdentityStoreException( "You cannot specify a CUID when requesting for a creation" );
        }

        if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) ) && !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            throw new IdentityStoreException( "You cannot specify a GUID when requesting for a creation" );
        }

        // check if all mandatory attributes are present
        final List<String> mandatoryAttributes = _serviceContractService.getMandatoryAttributes( clientCode,
                AttributeKeyHome.getMandatoryForCreationAttributeKeyList( ) );
        if ( CollectionUtils.isNotEmpty( mandatoryAttributes ) )
        {
            final Set<String> providedKeySet = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                    .map( CertifiedAttribute::getKey ).collect( Collectors.toSet( ) );
            if ( !providedKeySet.containsAll( mandatoryAttributes ) )
            {
                response.setStatus( IdentityChangeStatus.FAILURE );
                response.setMessage( "All mandatory attributes must be provided : " + mandatoryAttributes );
                return null;
            }
        }

        // check if can set "mon_paris_active" flag to true
        if ( Boolean.TRUE.equals( request.getIdentity( ).getMonParisActive( ) ) && !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            throw new IdentityStoreException( "You cannot set the 'mon_paris_active' flag when requesting for a creation" );
        }

        // check if GUID is already in use
        if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) )
                && IdentityHome.findByCustomerId( request.getIdentity( ).getConnectionId( ) ) != null )
        {
            throw new IdentityStoreException( "GUID is already in use." );
        }

        final Map<String, String> attributes = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                .collect( Collectors.toMap( CertifiedAttribute::getKey, CertifiedAttribute::getValue ) );
        final DuplicateSearchResponse duplicateSearchResponse = this.checkDuplicates( attributes, PROPERTY_DUPLICATES_CREATION_RULES );
        if ( duplicateSearchResponse != null && CollectionUtils.isNotEmpty( duplicateSearchResponse.getIdentities( ) ) )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setMessage( duplicateSearchResponse.getMessage( ) );
            return null;
        }

        final Identity identity = new Identity( );
        TransactionManager.beginTransaction( null );
        try
        {
            identity.setMonParisActive( request.getIdentity( ).getMonParisActive( ) != null ? request.getIdentity( ).getMonParisActive( ) : false );
            if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) ) )
            {
                identity.setConnectionId( request.getIdentity( ).getConnectionId( ) );
            }
            IdentityHome.create( identity, _serviceContractService.getDataRetentionPeriodInMonths( clientCode ) );

            final List<CertifiedAttribute> attributesToCreate = request.getIdentity( ).getAttributes( );
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
                AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.CREATE, identity,
                        attributeStatus, request.getOrigin( ), clientCode );
                _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
            } );

            /* Indexation et historique */
            final IndexIdentityChange identityChange = new IndexIdentityChange( IdentityStoreNotifyListenerService.buildIdentityChange(
                    IdentityChangeType.CREATE, identity, response.getStatus( ).name( ), response.getMessage( ), request.getOrigin( ), clientCode ), identity );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );
            TransactionManager.commitTransaction( null );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_CREATE, CREATE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( request, clientCode ), request, SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus( IdentityChangeStatus.FAILURE );
            response.setMessage( e.getMessage( ) );
            TransactionManager.rollBack( null );
        }

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
     * @param request
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the updated {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity update( final String customerId, final IdentityChangeRequest request, final String clientCode, final IdentityChangeResponse response )
            throws IdentityStoreException
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

        // check if identity hasn't been updated between when the user retreived the identity, and this request
        if ( !Objects.equals( identity.getLastUpdateDate( ), request.getIdentity( ).getLastUpdateDate( ) ) )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setCustomerId( identity.getCustomerId( ) );
            response.setMessage( "This identity has been updated recently, please load the latest data before updating." );
            return identity;
        }

        // check if identity is not merged
        if ( identity.isMerged( ) )
        {
            final Identity masterIdentity = IdentityHome.findMasterIdentityByCustomerId( request.getIdentity( ).getCustomerId( ) );
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
            if ( request.getIdentity( ).getMonParisActive( ) != null )
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
        TransactionManager.beginTransaction( null );
        try
        {
            if ( _serviceContractService.canModifyConnectedIdentity( clientCode )
                    && !StringUtils.equals( identity.getConnectionId( ), request.getIdentity( ).getConnectionId( ) )
                    && request.getIdentity( ).getConnectionId( ) != null )
            {
                final Identity byConnectionId = IdentityHome.findByConnectionId( request.getIdentity( ).getConnectionId( ) );
                if ( byConnectionId != null )
                {
                    response.setStatus( IdentityChangeStatus.CONFLICT );
                    response.setCustomerId( byConnectionId.getCustomerId( ) );
                    response.setMessage(
                            "An identity already exists with the given connection ID. The customer ID of that identity is provided in the response." );
                    return null;
                }
                else
                {
                    identity.setConnectionId( request.getIdentity( ).getConnectionId( ) );
                }
            }

            // => process update :

            this.updateIdentity( request.getIdentity( ), clientCode, response, identity );

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
                AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.UPDATE, identity,
                        attributeStatus, request.getOrigin( ), clientCode );
                _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
            } );

            /* Indexation et historique */
            final IndexIdentityChange identityChange = new IndexIdentityChange( IdentityStoreNotifyListenerService.buildIdentityChange(
                    IdentityChangeType.UPDATE, identity, response.getStatus( ).name( ), response.getMessage( ), request.getOrigin( ), clientCode ), identity );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );
            TransactionManager.commitTransaction( null );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, UPDATE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( request, clientCode ), request, SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus( IdentityChangeStatus.FAILURE );
            response.setMessage( e.getMessage( ) );
            TransactionManager.rollBack( null );
        }

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
     * @param request
     *            the {@link IdentityMergeRequest} holding the parameters of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityMergeResponse} holding the status of the execution of the request
     * @return the merged {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    // TODO: récupérer la plus haute date d'expiration des deux identités
    public Identity merge( final IdentityMergeRequest request, final String clientCode, final IdentityMergeResponse response ) throws IdentityStoreException
    {
        final Identity primaryIdentity = IdentityHome.findByCustomerId( request.getPrimaryCuid( ) );
        if ( primaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Could not find primary identity with customer_id " + request.getPrimaryCuid( ) );
            return null;
        }

        if ( primaryIdentity.isDeleted( ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Primary identity found with customer_id " + request.getPrimaryCuid( ) + " is deleted" );
            return null;
        }

        if ( primaryIdentity.isMerged( ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Primary identity found with customer_id " + request.getPrimaryCuid( ) + " is merged" );
            return null;
        }

        if ( !Objects.equals( primaryIdentity.getLastUpdateDate( ), request.getPrimaryLastUpdateDate( ) ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "The primary identity has been updated recently, please load the latest data before merging." );
            return null;
        }

        final Identity secondaryIdentity = IdentityHome.findByCustomerId( request.getSecondaryCuid( ) );
        if ( secondaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Could not find secondary identity with customer_id " + request.getSecondaryCuid( ) );
            return null;
        }

        if ( secondaryIdentity.isDeleted( ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( ) + " is deleted" );
            return null;
        }

        if ( secondaryIdentity.isMerged( ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( ) + " is merged" );
            return null;
        }

        if ( !Objects.equals( secondaryIdentity.getLastUpdateDate( ), request.getSecondaryLastUpdateDate( ) ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "The secondary identity has been updated recently, please load the latest data before merging." );
            return null;
        }

        TransactionManager.beginTransaction( null );
        try
        {
            if ( request.getIdentity( ) != null )
            {
                this.updateIdentity( request.getIdentity( ), clientCode, response, primaryIdentity );
            }

            /* Tag de l'identité secondaire */
            secondaryIdentity.setMerged( true );
            secondaryIdentity.setMasterIdentityId( primaryIdentity.getId( ) );
            IdentityHome.merge( secondaryIdentity );
            IdentityAttributeHome.removeAllAttributes( secondaryIdentity.getId( ) );

            response.setCustomerId( primaryIdentity.getCustomerId( ) );
            response.setConnectionId( primaryIdentity.getConnectionId( ) );
            response.setLastUpdateDate( primaryIdentity.getLastUpdateDate( ) );
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
            response.setStatus( notAllAttributesCreatedOrUpdated ? IdentityMergeStatus.INCOMPLETE_SUCCESS : IdentityMergeStatus.SUCCESS );

            /* Historique des modifications */
            response.getAttributeStatuses( ).forEach( attributeStatus -> {
                AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.MERGE, primaryIdentity,
                        attributeStatus, request.getOrigin( ), clientCode );
                _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
            } );

            /* Indexation */
            final IndexIdentityChange secondaryIdentityChange = new IndexIdentityChange(
                    IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.MERGED, secondaryIdentity, response.getStatus( ).name( ),
                            response.getStatus( ).getLabel( ), request.getOrigin( ), clientCode ),
                    secondaryIdentity );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( secondaryIdentityChange );

            final IndexIdentityChange primaryIdentityChange = new IndexIdentityChange(
                    IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.CONSOLIDATED, primaryIdentity, response.getStatus( ).name( ),
                            response.getStatus( ).getLabel( ), request.getOrigin( ), clientCode ),
                    primaryIdentity );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( primaryIdentityChange );
            TransactionManager.commitTransaction( null );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, MERGE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( request, clientCode ), request, SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( e.getMessage( ) );
            TransactionManager.rollBack( null );
        }

        return primaryIdentity;
    }

    /**
     * Detach a merged {@link Identity} from its master {@link Identity}
     * 
     * @param request
     *            the unmerge request
     * @param clientCode
     *            the client code of the calling application
     * @param response
     *            the status of the execution
     */
    public void cancelMerge( final IdentityMergeRequest request, final String clientCode, final IdentityMergeResponse response )
    {
        final Identity primaryIdentity = IdentityHome.findByCustomerId( request.getPrimaryCuid( ) );
        if ( primaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Could not find primary identity with customer_id " + request.getPrimaryCuid( ) );
            return;
        }

        if ( !Objects.equals( primaryIdentity.getLastUpdateDate( ), request.getPrimaryLastUpdateDate( ) ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "The primary identity has been updated recently, please load the latest data before canceling merge." );
            return;
        }

        final Identity secondaryIdentity = IdentityHome.findByCustomerId( request.getSecondaryCuid( ) );
        if ( secondaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Could not find secondary identity with customer_id " + request.getSecondaryCuid( ) );
            return;
        }

        if ( !secondaryIdentity.isMerged( ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( ) + " is not merged" );
            return;
        }

        if ( secondaryIdentity.getMasterIdentityId( ) != primaryIdentity.getMasterIdentityId( ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( )
                    + " is not merged to Primary identity found with customer ID " + request.getPrimaryCuid( ) );
            return;
        }

        if ( !Objects.equals( secondaryIdentity.getLastUpdateDate( ), request.getSecondaryLastUpdateDate( ) ) )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( "The secondary identity has been updated recently, please load the latest data before canceling merge." );
            return;
        }

        TransactionManager.beginTransaction( null );
        try
        {
            /* Tag de l'identité secondaire */
            IdentityHome.cancelMerge( secondaryIdentity );
            response.setStatus( IdentityMergeStatus.SUCCESS );

            /* Indexation */
            final IndexIdentityChange secondaryIdentityChange = new IndexIdentityChange(
                    IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.MERGE_CANCELLED, secondaryIdentity,
                            response.getStatus( ).name( ), response.getStatus( ).getLabel( ), request.getOrigin( ), clientCode ),
                    secondaryIdentity );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( secondaryIdentityChange );

            final IndexIdentityChange primaryIdentityChange = new IndexIdentityChange(
                    IdentityStoreNotifyListenerService.buildIdentityChange( IdentityChangeType.CONSOLIDATION_CANCELLED, primaryIdentity,
                            response.getStatus( ).name( ), response.getStatus( ).getLabel( ), request.getOrigin( ), clientCode ),
                    primaryIdentity );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( primaryIdentityChange );
            TransactionManager.commitTransaction( null );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, UNMERGE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( request, clientCode ), request, SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.setMessage( e.getMessage( ) );
            TransactionManager.rollBack( null );
        }
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

        final DuplicateSearchResponse certitudeDuplicates = this.checkDuplicates( attributes, PROPERTY_DUPLICATES_IMPORT_RULES_STRICT );
        if ( certitudeDuplicates != null && CollectionUtils.isNotEmpty( certitudeDuplicates.getIdentities( ) ) )
        {
            if ( certitudeDuplicates.getIdentities( ).size( ) == 1 )
            {
                return this.update( certitudeDuplicates.getIdentities( ).get( 0 ).getCustomerId( ), identityChangeRequest, clientCode, response );
            }
        }

        final DuplicateSearchResponse suspicionDuplicates = this.checkDuplicates( attributes, PROPERTY_DUPLICATES_IMPORT_RULES_SUSPICION );
        if ( suspicionDuplicates != null && CollectionUtils.isNotEmpty( suspicionDuplicates.getIdentities( ) ) )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setMessage( suspicionDuplicates.getMessage( ) );
        }
        else
        {
            return this.create( identityChangeRequest, clientCode, response );
        }

        return null;
    }

    public QualifiedIdentity getQualifiedIdentity( final String customerId ) throws IdentityStoreException
    {
        final Identity identity = IdentityHome.findByCustomerId( customerId );
        final QualifiedIdentity qualifiedIdentity = DtoConverter.convertIdentityToDto( identity );
        IdentityQualityService.instance( ).computeQuality( qualifiedIdentity );
        return qualifiedIdentity;
    }

    /**
     * Perform an identity research over a list of attributes (key and values) specified in the {@link IdentitySearchRequest}
     *
     * @param request
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
    public void search( final IdentitySearchRequest request, final IdentitySearchResponse response, final String clientCode )
            throws ServiceContractNotFoundException, IdentityAttributeNotFoundException, RefAttributeCertificationDefinitionNotFoundException
    {
        AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_READ, SEARCH_IDENTITY_EVENT_CODE,
                _internalUserService.getApiUser( request, clientCode ), request, SPECIFIC_ORIGIN );
        final List<SearchAttribute> providedAttributes = request.getSearch( ).getAttributes( );
        final Set<String> providedKeys = providedAttributes.stream( ).map( SearchAttribute::getKey ).collect( Collectors.toSet( ) );

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

        final List<QualifiedIdentity> qualifiedIdentities = _elasticSearchIdentityService.getQualifiedIdentities( providedAttributes, request.getMax( ),
                request.isConnected( ) );
        if ( CollectionUtils.isNotEmpty( qualifiedIdentities ) )
        {
            final List<QualifiedIdentity> filteredIdentities = this.getFilteredQualifiedIdentities( request, clientCode, qualifiedIdentities );
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
        AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_READ, GET_IDENTITY_EVENT_CODE, _internalUserService.getApiUser( clientCode ),
                customerId != null ? customerId : connectionId, SPECIFIC_ORIGIN );
        final Identity identity = StringUtils.isNotEmpty( customerId ) ? IdentityHome.findMasterIdentityByCustomerId( customerId )
                : StringUtils.isNotEmpty( connectionId ) ? IdentityHome.findMasterIdentityByConnectionId( connectionId ) : null;
        if ( identity == null )
        {
            response.setStatus( IdentitySearchStatusType.NOT_FOUND );
        }
        else
        {
            final QualifiedIdentity qualifiedIdentity = DtoConverter.convertIdentityToDto( identity );
            final List<QualifiedIdentity> filteredIdentities = this.getFilteredQualifiedIdentities( null, clientCode,
                    Collections.singletonList( qualifiedIdentity ) );
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

    /**
     * Filter a list of search results over {@link ServiceContract} defined for the given clientCode. Also complete identities with additional information
     * (quality, duplicates, ...).
     * 
     * @param identitySearchRequest
     *            la requête de recherche
     * @param clientCode
     *            le code client du demandeur
     * @param qualifiedIdentities
     *            la liste de résultats à traiter
     * @return the list of filtered and completed {@link QualifiedIdentity}
     * @throws ServiceContractNotFoundException
     *             in case of error
     * @throws IdentityAttributeNotFoundException
     *             in case of errorj
     */
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

            final SuspiciousIdentity suspiciousIdentity = SuspiciousIdentityHome.selectByCustomerID( qualifiedIdentity.getCustomerId( ) );
            if ( suspiciousIdentity != null )
            {
                qualifiedIdentity.setDuplicateDefintion( new IdentityDuplicateDefintion( ) );
                final IdentityDuplicateSuspicion duplicateSuspicion = new IdentityDuplicateSuspicion( );
                qualifiedIdentity.getDuplicateDefintion( ).setDuplicateSuspicion( duplicateSuspicion );
                duplicateSuspicion.setDuplicateRuleCode( suspiciousIdentity.getDuplicateRuleCode( ) );
                duplicateSuspicion.setCreationDate( suspiciousIdentity.getCreationDate( ) );
            }

            final List<ExcludedIdentities> excludedIdentitiesList = SuspiciousIdentityHome.getExcludedIdentitiesList( qualifiedIdentity.getCustomerId( ) );
            if ( CollectionUtils.isNotEmpty( excludedIdentitiesList ) )
            {
                if ( qualifiedIdentity.getDuplicateDefintion( ) == null )
                {
                    qualifiedIdentity.setDuplicateDefintion( new IdentityDuplicateDefintion( ) );
                }
                qualifiedIdentity.getDuplicateDefintion( ).getDuplicateExclusions( ).addAll( excludedIdentitiesList.stream( ).map( excludedIdentities -> {
                    final IdentityDuplicateExclusion exclusion = new IdentityDuplicateExclusion( );
                    exclusion.setExclusionDate( excludedIdentities.getExclusionDate( ) );
                    exclusion.setAuthorName( excludedIdentities.getAuthorName( ) );
                    exclusion.setAuthorType( excludedIdentities.getAuthorType( ) );
                    final String excludedCustomerId = Objects.equals( excludedIdentities.getFirstCustomerId( ), qualifiedIdentity.getCustomerId( ) )
                            ? excludedIdentities.getSecondCustomerId( )
                            : excludedIdentities.getFirstCustomerId( );
                    exclusion.setExcludedCustomerId( excludedCustomerId );
                    return exclusion;
                } ).collect( Collectors.toList( ) ) );
            }

        }
        filteredIdentities.sort( comparator );
        return filteredIdentities;
    }

    private void updateIdentity( final fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity requestIdentity, final String clientCode,
            final ChangeResponse response, final Identity identity ) throws IdentityStoreException
    {
        /* Récupération des attributs déja existants ou non */
        final Map<Boolean, List<CertifiedAttribute>> sortedAttributes = requestIdentity.getAttributes( ).stream( )
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

        if ( requestIdentity.getMonParisActive( ) != null )
        {
            identity.setMonParisActive( requestIdentity.getMonParisActive( ) );
        }
        IdentityHome.update( identity );
    }

    /**
     * Gets a list of customer IDs on which to search potential duplicates.<br/>
     * Returned customer IDS belong to identities that must have all attributes checked by the provided rule, and must also not be already merged nor be tagged
     * as suspicious.
     *
     * @param rule
     *            the rule used to get matching identities
     * @return the list of identities
     */
    public Batch<String> getIdentitiesBatchForPotentialDuplicate( final DuplicateRule rule, final int batchSize )
    {
        if ( rule == null )
        {
            return Batch.ofSize( new ArrayList<>( ), 0 );
        }
        final List<Integer> attributes = rule.getCheckedAttributes( ).stream( ).map( AttributeKey::getId ).collect( Collectors.toList( ) );
        final List<String> customerIdsList = IdentityHome.findByAttributeExisting( attributes, rule.getNbFilledAttributes( ), true, true );
        return Batch.ofSize( customerIdsList, batchSize );
    }

    /**
     * request a deletion of identity .
     *
     * @param customerId
     *            the customer ID
     * @param clientCode
     *            the client code
     * @param request
     *            the identityChangeRequest
     */
    public void deleteRequest( final String customerId, final String clientCode, final IdentityChangeRequest request, final IdentityChangeResponse response )
            throws IdentityStoreException
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

        TransactionManager.beginTransaction( null );
        try
        {
            // expire identity (the deletion is managed by the dedicated Daemon)
            IdentityHome.softRemove( customerId );

            response.setStatus( IdentityChangeStatus.DELETE_SUCCESS );

            /* Notify listeners for indexation, history, ... */
            final IndexIdentityChange identityChange = new IndexIdentityChange( IdentityStoreNotifyListenerService.buildIdentityChange(
                    IdentityChangeType.DELETE, identity, response.getStatus( ).name( ), response.getMessage( ), request.getOrigin( ), clientCode ), identity );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );
            TransactionManager.commitTransaction( null );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_DELETE, DELETE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( clientCode ), customerId, SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
        }

    }

    /**
     * Check if duplicates exist for a set of attributes
     * 
     * @param attributes
     *            the set of attributes
     * @param ruleCodeProperty
     *            the properties that defines the list of rules to check
     * @return a {@link DuplicateSearchResponse} that holds the execution result
     * @throws IdentityStoreException
     *             in case of error
     */
    private DuplicateSearchResponse checkDuplicates( final Map<String, String> attributes, final String ruleCodeProperty ) throws IdentityStoreException
    {
        final List<String> ruleCodes = Arrays.asList( AppPropertiesService.getProperty( ruleCodeProperty ).split( "," ) );
        final DuplicateSearchResponse esDuplicates = _duplicateServiceElasticSearch.findDuplicates( attributes, "", ruleCodes );
        if ( esDuplicates != null )
        {
            return esDuplicates;
        }
        final boolean checkDatabase = AppPropertiesService.getPropertyBoolean( PROPERTY_DUPLICATES_CHECK_DATABASE_ACTIVATED, false );
        if ( checkDatabase )
        {
            return _duplicateServiceDatabase.findDuplicates( attributes, "", ruleCodes );
        }
        return null;
    }
}
