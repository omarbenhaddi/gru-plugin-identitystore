/*
 * Copyright (c) 2002-2016, Mairie de Paris
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

import java.sql.Timestamp;


/**
 * AttributeChange
 */
public class AttributeChange
{
    public static final int TYPE_CREATE = 0;
    public static final int TYPE_UPDATE = 1;
    public static final int TYPE_DELETE = 2;

    // Variables declarations
    private Timestamp _dateChange;
    // Variables declarations 
    private String _strAuthorId;
    private String _strAuthorName;
    private String _strAuthorService;
    private int _nAuthorType;
    private String _strIdentityConnectionId;
    private String _strIdentityName;
    private String _strChangedKey;
    private String _strOldValue;
    private String _strNewValue;
    private String _strCertifier;
    private int _nChangeType;

    /**
     * Returns the DateChange
     *
     * @return The DateChange
     */
    public Timestamp getDateChange(  )
    {
        return _dateChange;
    }

    /**
     * Sets the DateChange
     *
     * @param DateChange
     *          The DateChange
     */
    public void setDateChange( Timestamp DateChange )
    {
        _dateChange = DateChange;
    }

    /**
     * Returns the AuthorId
     *
     * @return The AuthorId
     */
    public String getAuthorId(  )
    {
        return _strAuthorId;
    }

    /**
     * Sets the AuthorId
     *
     * @param strAuthorId The AuthorId
     */
    public void setAuthorId( String strAuthorId )
    {
        _strAuthorId = strAuthorId;
    }

    /**
     * Returns the AuthorName
     *
     * @return The AuthorName
     */
    public String getAuthorName(  )
    {
        return _strAuthorName;
    }

    /**
     * Sets the AuthorName
     *
     * @param strAuthorName The AuthorName
     */
    public void setAuthorName( String strAuthorName )
    {
        _strAuthorName = strAuthorName;
    }

    /**
     * Returns the AuthorService
     *
     * @return The AuthorService
     */
    public String getAuthorService(  )
    {
        return _strAuthorService;
    }

    /**
     * Sets the AuthorService
     *
     * @param strAuthorService The AuthorService
     */
    public void setAuthorService( String strAuthorService )
    {
        _strAuthorService = strAuthorService;
    }

    /**
     * Returns the AuthorType
     *
     * @return The AuthorType
     */
    public int getAuthorType(  )
    {
        return _nAuthorType;
    }

    /**
     * Sets the AuthorType
     *
     * @param nAuthorType The AuthorType
     */
    public void setAuthorType( int nAuthorType )
    {
        _nAuthorType = nAuthorType;
    }

    /**
     * Returns the IdentityId
     *
     * @return The IdentityId
     */
    public String getIdentityConnectionId(  )
    {
        return _strIdentityConnectionId;
    }

    /**
     * Sets the IdentityId
     *
     * @param strIdentityId
     *          The IdentityId
     */
    public void setIdentityConnectionId( String strIdentityId )
    {
        _strIdentityConnectionId = strIdentityId;
    }

    /**
     * Returns the IdentityName
     *
     * @return The IdentityName
     */
    public String getIdentityName(  )
    {
        return _strIdentityName;
    }

    /**
     * Sets the IdentityName
     *
     * @param strIdentityName The IdentityName
     */
    public void setIdentityName( String strIdentityName )
    {
        _strIdentityName = strIdentityName;
    }

    /**
     * Returns the ChangedKey
     *
     * @return The ChangedKey
     */
    public String getChangedKey(  )
    {
        return _strChangedKey;
    }

    /**
     * Sets the ChangedKey
     *
     * @param strChangedKey The ChangedKey
     */
    public void setChangedKey( String strChangedKey )
    {
        _strChangedKey = strChangedKey;
    }

    /**
     * Returns the OldValue
     *
     * @return The OldValue
     */
    public String getOldValue(  )
    {
        return _strOldValue;
    }

    /**
     * Sets the OldValue
     *
     * @param strOldValue The OldValue
     */
    public void setOldValue( String strOldValue )
    {
        _strOldValue = strOldValue;
    }

    /**
     * Returns the NewValue
     *
     * @return The NewValue
     */
    public String getNewValue(  )
    {
        return _strNewValue;
    }

    /**
     * Sets the NewValue
     *
     * @param strNewValue The NewValue
     */
    public void setNewValue( String strNewValue )
    {
        _strNewValue = strNewValue;
    }

    /**
     * Returns the Certifier
     *
     * @return The Certifier
     */
    public String getCertifier(  )
    {
        return _strCertifier;
    }

    /**
     * Sets the Certifier
     *
     * @param strCertifier The Certifier
     */
    public void setCertifier( String strCertifier )
    {
        _strCertifier = strCertifier;
    }

    /**
     * Returns the ChangeType
     *
     * @return The ChangeType
     */
    public int getChangeType(  )
    {
        return _nChangeType;
    }

    /**
     * Sets the ChangeType
     *
     * @param nChangeType The ChangeType
     */
    public void setChangeType( int nChangeType )
    {
        _nChangeType = nChangeType;
    }
}
