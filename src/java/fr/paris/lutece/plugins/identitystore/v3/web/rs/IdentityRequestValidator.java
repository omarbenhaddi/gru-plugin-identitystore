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
package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * util class used to validate identity store request
 *
 */
public final class IdentityRequestValidator
{
    /**
     * singleton
     */
    private static IdentityRequestValidator _singleton;

    /**
     * private constructor
     */
    private IdentityRequestValidator( )
    {

    }

    /**
     * return singleton's instance
     * 
     * @return IdentityRequestValidator
     */
    public static IdentityRequestValidator instance( )
    {
        if ( _singleton == null )
        {
            try
            {
                _singleton = new IdentityRequestValidator( );
            }
            catch( Exception e )
            {
                AppLogService.error( "Error when instantiating IdentityRequestValidator instance" + e.getMessage( ), e );
            }
        }

        return _singleton;
    }

    /**
     * check whether the parameters related to the application are valid or not
     *
     * @param strClientCode
     *            client application code
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkClientApplication( String strClientCode ) throws IdentityStoreException
    {
        if ( StringUtils.isBlank( strClientCode ) )
        {
            throw new IdentityStoreException( Constants.PARAM_CLIENT_CODE + " is missing" );
        }
    }

    /**
     * check whether the parameters related to the application are valid or not
     *
     * @param strApplicationCode
     *            client application code
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkClientApplicationCode( String strApplicationCode ) throws IdentityStoreException
    {
        if ( StringUtils.isBlank( strApplicationCode ) )
        {
            throw new IdentityStoreException( Constants.PARAM_APPLICATION_CODE + " is missing" );
        }
    }

    /**
     * check whether the parameters related to the identity are valid or not
     *
     * @param strCustomerId
     *            the customer id
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkCustomerId( String strCustomerId ) throws IdentityStoreException
    {
        if ( StringUtils.isBlank( strCustomerId ) )
        {
            throw new IdentityStoreException( Constants.PARAM_ID_CONNECTION + "is missing." );
        }
    }

    /**
     * check whether the parameters related to the service contract are valid or not
     *
     * @param contractId
     *            the service contract id
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkContractId( Integer contractId ) throws IdentityStoreException
    {
        if ( contractId == null )
        {
            throw new IdentityStoreException( Constants.PARAM_ID_SERVICE_CONTRACT + " is missing." );
        }
    }

    /**
     * check whether the parameters related to the identity update are valid or not
     *
     * @param strConnectionId
     *            the connection id
     * @param strCustomerId
     *            the customer id
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkIdentityForUpdate( String strConnectionId, String strCustomerId ) throws IdentityStoreException
    {
        if ( StringUtils.isNotEmpty( strConnectionId ) && StringUtils.isBlank( strCustomerId ) )
        {
            throw new IdentityStoreException( "You cannot update an identity providing its connection_id. You must specify " + Constants.PARAM_ID_CUSTOMER );
        }

        if ( StringUtils.isBlank( strCustomerId ) )
        {
            throw new IdentityStoreException( Constants.PARAM_ID_CUSTOMER + " is missing" );
        }
    }

    /**
     * check whether the parameters related to the identity are valid or not
     * 
     * @param identityChange
     * @throws AppException
     */
    public void checkIdentityChange( IdentityChangeRequest identityChange ) throws IdentityStoreException
    {
        if ( identityChange == null || identityChange.getIdentity( ) == null
                || ( identityChange.getIdentity( ).getMonParisActive( ) == null && CollectionUtils.isEmpty( identityChange.getIdentity( ).getAttributes( ) ) ) )
        {
            throw new IdentityStoreException( "Provided Identity Change request is null or empty" );
        }

        if ( identityChange.getOrigin( ) == null || StringUtils.isEmpty( identityChange.getOrigin( ).getName( ) )
                || identityChange.getOrigin( ).getType( ) == null )
        {
            throw new IdentityStoreException( "Provided Author is null or empty" );
        }

        if ( identityChange.getIdentity( ).getAttributes( ).stream( ).anyMatch( a -> !a.isCertified( ) ) )
        {
            throw new IdentityStoreException( "Provided attributes shall be fully certified (process + date)" );
        }
    }

    public void checkIdentitySearch( IdentitySearchRequest identitySearch ) throws IdentityStoreException
    {
        if ( StringUtils.isNotEmpty( identitySearch.getConnectionId( ) ) && identitySearch.getSearch( ) != null )
        {
            throw new IdentityStoreException( "You cannot provide a connection_id and a Search at the same time." );
        }
        if ( StringUtils.isEmpty( identitySearch.getConnectionId( ) ) && ( identitySearch.getSearch( ) == null
                || identitySearch.getSearch( ).getAttributes( ) == null || identitySearch.getSearch( ).getAttributes( ).isEmpty( ) ) )
        {
            throw new IdentityStoreException( "Provided Identity Search request is null or empty" );
        }
    }

    /**
     * Check whether the parameters related to the identity merge request are valid or not
     *
     * @param identityMergeRequest
     *            the identity merge request
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkMergeRequest( IdentityMergeRequest identityMergeRequest ) throws IdentityStoreException
    {
        if ( identityMergeRequest.getOrigin( ) == null || StringUtils.isEmpty( identityMergeRequest.getOrigin( ).getName( ) )
                || identityMergeRequest.getOrigin( ).getType( ) == null )
        {
            throw new IdentityStoreException( "Provided Author is null or empty" );
        }

        if ( identityMergeRequest == null || identityMergeRequest.getIdentities( ) == null
                || StringUtils.isEmpty( identityMergeRequest.getIdentities( ).getPrimaryCuid( ) )
                || StringUtils.isEmpty( identityMergeRequest.getIdentities( ).getSecondaryCuid( ) ) )
        {
            throw new IdentityStoreException( "Provided Identity Merge request is null or incomplete" );
        }

        // TODO : nécessité du contrôle à revalider, un rapprochement peut se faire sans récupération d'attribut
        /*if ( CollectionUtils.isEmpty( identityMergeRequest.getIdentities( ).getAttributeKeys( ) ) )
        {
            throw new IdentityStoreException( "Provided Identity Merge request must define at least one attribute key to be merged" );
        }*/
    }

    public void checkServiceContract( ServiceContractDto serviceContractDto ) throws IdentityStoreException
    {
        if ( serviceContractDto == null )
        {
            throw new IdentityStoreException( "Provided service contract is null" );
        }

        if ( serviceContractDto.getStartingDate( ) == null )
        {
            throw new IdentityStoreException( "Provided ServiceContractDto must specify a starting date" );
        }
    }

    public void checkClosingServiceContract( ServiceContractDto serviceContractDto ) throws IdentityStoreException
    {
        if ( serviceContractDto == null )
        {
            throw new IdentityStoreException( "Provided service contract is null" );
        }

        if ( serviceContractDto.getEndingDate( ) == null )
        {
            throw new IdentityStoreException( "Provided ServiceContractDto must specify an ending date" );
        }
    }

    public void checkClientApplicationDto( ClientApplicationDto clientApplicationDto ) throws IdentityStoreException
    {
        if ( clientApplicationDto == null )
        {
            throw new IdentityStoreException( "Provided client is null" );
        }

        if ( clientApplicationDto.getClientCode( ) == null )
        {
            throw new IdentityStoreException( "Provided client must specify a client code" );
        }
    }
    
    /**
     * check whether the parameters related to the origin are valid or not
     * 
     * @param identityChange
     * @throws AppException
     */
    public void checkOrigin( IdentityChangeRequest identityChange ) throws IdentityStoreException
    {

        if ( identityChange.getOrigin( ) == null || StringUtils.isEmpty( identityChange.getOrigin( ).getName( ) )
                || identityChange.getOrigin( ).getType( ) == null )
        {
            throw new IdentityStoreException( "Provided Author or Type is null or empty" );
        }

    }
    
}
