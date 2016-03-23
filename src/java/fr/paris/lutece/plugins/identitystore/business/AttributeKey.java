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
package fr.paris.lutece.plugins.identitystore.business;

import javax.validation.constraints.*;
import org.hibernate.validator.constraints.*;
import java.io.Serializable;

/**
 * This is the business class for the object AttributeKey
 */ 
public class AttributeKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations 
    private int _nId;
    
    @NotEmpty( message = "#i18n{identitystore.validation.attributekey.KeyName.notEmpty}" )
    @Size( max = 50 , message = "#i18n{identitystore.validation.attributekey.KeyName.size}" ) 
    private String _strKeyName;
    
    @NotEmpty( message = "#i18n{identitystore.validation.attributekey.KeyDescription.notEmpty}" )
    private String _strKeyDescription;
    
    private int _nKeyType;

    /**
     * Returns the Id
     * @return The Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the Id
     * @param nId The Id
     */ 
    public void setId( int nId )
    {
        _nId = nId;
    }
    
    /**
     * Returns the KeyName
     * @return The KeyName
     */
    public String getKeyName( )
    {
        return _strKeyName;
    }

    /**
     * Sets the KeyName
     * @param strKeyName The KeyName
     */ 
    public void setKeyName( String strKeyName )
    {
        _strKeyName = strKeyName;
    }
    
    /**
     * Returns the KeyDescription
     * @return The KeyDescription
     */
    public String getKeyDescription( )
    {
        return _strKeyDescription;
    }

    /**
     * Sets the KeyDescription
     * @param strKeyDescription The KeyDescription
     */ 
    public void setKeyDescription( String strKeyDescription )
    {
        _strKeyDescription = strKeyDescription;
    }
    
    /**
     * Returns the KeyType
     * @return The KeyType
     */
    public int getKeyType( )
    {
        return _nKeyType;
    }

    /**
     * Sets the KeyType
     * @param nKeyType The KeyType
     */ 
    public void setKeyType( int nKeyType )
    {
        _nKeyType = nKeyType;
    }
}
