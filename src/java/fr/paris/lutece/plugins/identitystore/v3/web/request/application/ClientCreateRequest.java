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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeStatusType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

/**
 * This class represents a create request for IdentityStoreRestServive
 */
public class ClientCreateRequest extends AbstractIdentityStoreRequest
{
    private final ClientApplicationDto _clientApplicationDto;

    /**
     * Constructor of IdentityStoreCreateRequest
     *
     * @param clientApplicationDto
     *            the dto of identity's change
     */
    public ClientCreateRequest( ClientApplicationDto clientApplicationDto, String strClientAppCode )
    {
        super( strClientAppCode );
        this._clientApplicationDto = clientApplicationDto;
    }

    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkClientApplicationDto( _clientApplicationDto );
    }

    @Override
    public ClientChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final ClientChangeResponse response = new ClientChangeResponse( );
        final ClientApplication clientApplication = ClientApplicationHome.findByCode( _clientApplicationDto.getClientCode( ) );
        if ( clientApplication != null )
        {
            response.setStatus( ClientChangeStatusType.CONFLICT );
            response.setMessage( "A client exists with the code " + _clientApplicationDto.getClientCode( ) );
        }
        else
        {
            final ClientApplication client = DtoConverter.convertDtoToClient( _clientApplicationDto );
            ClientApplicationHome.create( client );
            response.setClientApplication( DtoConverter.convertClientToDto( client ) );
            response.setStatus( ClientChangeStatusType.SUCCESS );
        }

        return response;
    }

}
