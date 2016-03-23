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
import java.sql.Date;

/**
 * This is the business class for the object Identity
 */ 
public class Identity implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations 
    private int _nId;
    
    @Size( max = 50 , message = "#i18n{identitystore.validation.identity.ConnectionId.size}" ) 
    private String _strConnectionId;
    
    @Size( max = 50 , message = "#i18n{identitystore.validation.identity.CustomerId.size}" ) 
    private String _strCustomerId;
    
    @NotEmpty( message = "#i18n{identitystore.validation.identity.GivenName.notEmpty}" )
    @Size( max = 50 , message = "#i18n{identitystore.validation.identity.GivenName.size}" ) 
    private String _strGivenName;
    
    @NotEmpty( message = "#i18n{identitystore.validation.identity.FamilyName.notEmpty}" )
    @Size( max = 50 , message = "#i18n{identitystore.validation.identity.FamilyName.size}" ) 
    private String _strFamilyName;
    
    private int _nGender;
    
    private Date _dateBirthdate;
    
    @Size( max = 50 , message = "#i18n{identitystore.validation.identity.Birthplace.size}" ) 
    private String _strBirthplace;
    @Email(message = "#i18n{portal.validation.message.email}")
    @Size( max = 255 , message = "#i18n{identitystore.validation.identity.Email.size}" ) 
    private String _strEmail;
    
    @Size( max = 50 , message = "#i18n{identitystore.validation.identity.PreferredUsername.size}" ) 
    private String _strPreferredUsername;
    
    @Size( max = 255 , message = "#i18n{identitystore.validation.identity.Address.size}" ) 
    private String _strAddress;
    
    @Size( max = 50 , message = "#i18n{identitystore.validation.identity.Phone.size}" ) 
    private String _strPhone;

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
     * Returns the ConnectionId
     * @return The ConnectionId
     */
    public String getConnectionId( )
    {
        return _strConnectionId;
    }

    /**
     * Sets the ConnectionId
     * @param strConnectionId The ConnectionId
     */ 
    public void setConnectionId( String strConnectionId )
    {
        _strConnectionId = strConnectionId;
    }
    
    /**
     * Returns the CustomerId
     * @return The CustomerId
     */
    public String getCustomerId( )
    {
        return _strCustomerId;
    }

    /**
     * Sets the CustomerId
     * @param strCustomerId The CustomerId
     */ 
    public void setCustomerId( String strCustomerId )
    {
        _strCustomerId = strCustomerId;
    }
    
    /**
     * Returns the GivenName
     * @return The GivenName
     */
    public String getGivenName( )
    {
        return _strGivenName;
    }

    /**
     * Sets the GivenName
     * @param strGivenName The GivenName
     */ 
    public void setGivenName( String strGivenName )
    {
        _strGivenName = strGivenName;
    }
    
    /**
     * Returns the FamilyName
     * @return The FamilyName
     */
    public String getFamilyName( )
    {
        return _strFamilyName;
    }

    /**
     * Sets the FamilyName
     * @param strFamilyName The FamilyName
     */ 
    public void setFamilyName( String strFamilyName )
    {
        _strFamilyName = strFamilyName;
    }
    
    /**
     * Returns the Gender
     * @return The Gender
     */
    public int getGender( )
    {
        return _nGender;
    }

    /**
     * Sets the Gender
     * @param nGender The Gender
     */ 
    public void setGender( int nGender )
    {
        _nGender = nGender;
    }
    
    /**
     * Returns the Birthdate
     * @return The Birthdate
     */
    public Date getBirthdate( )
    {
        return _dateBirthdate;
    }

    /**
     * Sets the Birthdate
     * @param dateBirthdate The Birthdate
     */ 
    public void setBirthdate( Date dateBirthdate )
    {
        _dateBirthdate = dateBirthdate;
    }
    
    /**
     * Returns the Birthplace
     * @return The Birthplace
     */
    public String getBirthplace( )
    {
        return _strBirthplace;
    }

    /**
     * Sets the Birthplace
     * @param strBirthplace The Birthplace
     */ 
    public void setBirthplace( String strBirthplace )
    {
        _strBirthplace = strBirthplace;
    }
    
    /**
     * Returns the Email
     * @return The Email
     */
    public String getEmail( )
    {
        return _strEmail;
    }

    /**
     * Sets the Email
     * @param strEmail The Email
     */ 
    public void setEmail( String strEmail )
    {
        _strEmail = strEmail;
    }
    
    /**
     * Returns the PreferredUsername
     * @return The PreferredUsername
     */
    public String getPreferredUsername( )
    {
        return _strPreferredUsername;
    }

    /**
     * Sets the PreferredUsername
     * @param strPreferredUsername The PreferredUsername
     */ 
    public void setPreferredUsername( String strPreferredUsername )
    {
        _strPreferredUsername = strPreferredUsername;
    }
    
    /**
     * Returns the Address
     * @return The Address
     */
    public String getAddress( )
    {
        return _strAddress;
    }

    /**
     * Sets the Address
     * @param strAddress The Address
     */ 
    public void setAddress( String strAddress )
    {
        _strAddress = strAddress;
    }
    
    /**
     * Returns the Phone
     * @return The Phone
     */
    public String getPhone( )
    {
        return _strPhone;
    }

    /**
     * Sets the Phone
     * @param strPhone The Phone
     */ 
    public void setPhone( String strPhone )
    {
        _strPhone = strPhone;
    }
}
