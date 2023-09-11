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
package fr.paris.lutece.plugins.identitystore.v3.web.request.application;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientsSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class ClientsGetRequest extends AbstractIdentityStoreRequest
{

    private String _strApplicationCode;

    /**
     * Constructor of IdentityStoreGetRequest
     *
     * @param strClientCode
     *            the client application Code
     */
    public ClientsGetRequest( String strClientCode, String strApplicationCode )
    {
        super( strClientCode );
        this._strApplicationCode = strApplicationCode;
    }

    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkClientApplicationCode( _strApplicationCode );
    }

    /**
     * get the identity
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    public ClientsSearchResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final ClientsSearchResponse response = new ClientsSearchResponse( );

        final List<ClientApplication> clientApplications = ClientApplicationHome.findByApplicationCode( _strApplicationCode );

        if ( clientApplications == null || CollectionUtils.isEmpty( clientApplications ) )
        {
            response.setStatus( ResponseStatus.notFound( ).setMessageKey( Constants.PROPERTY_REST_ERROR_NO_CLIENT_FOUND ) );
        }
        else
        {
            for ( final ClientApplication clientApplication : clientApplications )
            {
                response.getClientApplications( ).add( DtoConverter.convertClientToDto( clientApplication ) );
            }
            response.setStatus( ResponseStatus.ok( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
        }

        return response;
    }

}
