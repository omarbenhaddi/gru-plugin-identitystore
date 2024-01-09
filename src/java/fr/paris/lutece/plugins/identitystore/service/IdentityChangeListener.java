/*
 * Copyright (c) 2002-2024, City of Paris
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
package fr.paris.lutece.plugins.identitystore.service;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.LuteceService;

import java.util.Map;

/**
 * IdentityChangeListener
 */
public interface IdentityChangeListener extends LuteceService
{
    /**
     * Register an identity change
     * 
     * @param identityChangeType
     *            the type of change
     * @param identity
     *            the identity that changed
     * @param statusCode
     *            the status code of the change
     * @param statusMessage
     *            the message
     * @param author
     *            the author of the change
     * @param clientCode
     *            the client code that triggered the change
     * @param metadata
     *            additional data
     * @throws IdentityStoreException
     */
    void processIdentityChange( IdentityChangeType identityChangeType, Identity identity, String statusCode, String statusMessage, RequestAuthor author,
            String clientCode, Map<String, String> metadata ) throws IdentityStoreException;

    default IdentityChange buildIdentityChange( IdentityChangeType identityChangeType, Identity identity, String statusCode, String statusMessage,
            RequestAuthor author, String clientCode, Map<String, String> metadata )
    {

        final IdentityChange identityChange = new IdentityChange( );
        identityChange.setChangeType( identityChangeType );
        identityChange.setChangeStatus( statusCode );
        identityChange.setChangeMessage( statusMessage );
        identityChange.setAuthor( author );
        identityChange.setCustomerId( identity.getCustomerId( ) );
        identityChange.setConnectionId( identity.getConnectionId( ) );
        identityChange.setMonParisActive( identity.isMonParisActive( ) );
        identityChange.setCreationDate( identity.getCreationDate( ) );
        identityChange.setLastUpdateDate( identity.getLastUpdateDate( ) );
        identityChange.setId( identity.getId( ) );
        identityChange.setClientCode( clientCode );
        identityChange.getMetadata( ).putAll( metadata );
        return identityChange;
    }
}
