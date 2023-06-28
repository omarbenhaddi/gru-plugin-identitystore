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
package fr.paris.lutece.plugins.identitystore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;

import java.sql.Timestamp;

/**
 * AttributeChange
 */
public class IdentityChange
{
    // Variables declarations
    private Identity _identity;
    private IdentityChangeType _identityChangeType;
    protected String changeSatus;
    protected String changeMessage;
    protected AuthorType authorType;
    protected String authorName;
    protected String clientCode;
    protected Timestamp modificationDate;

    public IdentityChange( )
    {
    }

    public IdentityChange( Identity _identity, IdentityChangeType _identityChangeType )
    {
        this._identity = _identity;
        this._identityChangeType = _identityChangeType;
    }

    /**
     * Returns the Identity
     *
     * @return The Identity
     */
    public Identity getIdentity( )
    {
        return _identity;
    }

    /**
     * Sets the Identity
     *
     * @param identity
     *            The Identity
     */
    public void setIdentity( Identity identity )
    {
        _identity = identity;
    }

    /**
     * Returns the type of the IdentityChangeType
     *
     * @return The IdentityChangeType
     */
    public IdentityChangeType getChangeType( )
    {
        return _identityChangeType;
    }

    /**
     * Sets the type of the IdentityChange
     *
     * @param identityChangeType
     *            The IdentityChangeType
     */
    public void setChangeType( IdentityChangeType identityChangeType )
    {
        _identityChangeType = identityChangeType;
    }

    public String getChangeSatus( )
    {
        return changeSatus;
    }

    public void setChangeSatus( String changeSatus )
    {
        this.changeSatus = changeSatus;
    }

    public String getChangeMessage( )
    {
        return changeMessage;
    }

    public void setChangeMessage( String changeMessage )
    {
        this.changeMessage = changeMessage;
    }

    public AuthorType getAuthorType( )
    {
        return authorType;
    }

    public void setAuthorType( AuthorType authorType )
    {
        this.authorType = authorType;
    }

    public String getAuthorName( )
    {
        return authorName;
    }

    public void setAuthorName( String authorName )
    {
        this.authorName = authorName;
    }

    public String getClientCode( )
    {
        return clientCode;
    }

    public void setClientCode( String clientCode )
    {
        this.clientCode = clientCode;
    }

    public Timestamp getModificationDate( )
    {
        return modificationDate;
    }

    public void setModificationDate( Timestamp modificationDate )
    {
        this.modificationDate = modificationDate;
    }
}
