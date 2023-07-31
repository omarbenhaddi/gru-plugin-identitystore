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
package fr.paris.lutece.plugins.identitystore.v3.web.request.identity;

import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

/**
 * This class represents a delete request for IdentityStoreRestServive
 *
 */
public class IdentityStoreDeleteRequest extends AbstractIdentityStoreRequest
{

    private final String _strCustomerId;
    private final IdentityChangeRequest _identityChangeRequest;

    /**
     * Constructor of IdentityStoreDeleteRequest
     * 
     * @param strCustomerId
     *            the customer id of the identity
     * @param strClientAppCode
     *            the application code provided by the client
     */
    public IdentityStoreDeleteRequest( String strCustomerId, String strClientAppCode, IdentityChangeRequest identityChangeRequest )
    {
        super( strClientAppCode );
        _strCustomerId = strCustomerId;
        _identityChangeRequest = identityChangeRequest;
    }

    /**
     * Valid the delete request
     * 
     * @throws IdentityStoreException
     *             if there is an exception during the treatment
     */
    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkClientApplication( _strClientCode );
        IdentityRequestValidator.instance( ).checkOrigin( _identityChangeRequest.getOrigin( ) );
    }

    /**
     * Deletes the identity
     * 
     * @throws IdentityStoreException
     *             if there is an exception during the treatment
     */
    @Override
    protected IdentityChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {

        IdentityChangeResponse response = new IdentityChangeResponse( );

        IdentityService.instance( ).deleteRequest( _strCustomerId, _strClientCode, _identityChangeRequest, response );

        return response;
    }
}
