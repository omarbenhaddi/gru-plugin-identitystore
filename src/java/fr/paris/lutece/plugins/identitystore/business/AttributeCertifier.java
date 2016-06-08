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

import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

import javax.validation.constraints.Size;


/**
 * This is the business class for the object AttributeCertifier
 */
public class AttributeCertifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;
    @NotEmpty( message = "#i18n{identitystore.validation.attributecertifier.Name.notEmpty}" )
    @Size( max = 50, message = "#i18n{identitystore.validation.attributecertifier.Name.size}" )
    private String _strName;
    @NotEmpty( message = "#i18n{identitystore.validation.attributecertifier.Code.notEmpty}" )
    @Size( max = 50, message = "#i18n{identitystore.validation.attributecertifier.Code.size}" )
    private String _strCode;
    @NotEmpty( message = "#i18n{identitystore.validation.attributecertifier.Description.notEmpty}" )
    @Size( max = 255, message = "#i18n{identitystore.validation.attributecertifier.Description.size}" )
    private String _strDescription;
    private byte[] _logo;
    private String _strLogoMimeType;

    /**
     * Returns the Id
     *
     * @return The Id
     */
    public int getId(  )
    {
        return _nId;
    }

    /**
     * Sets the Id
     *
     * @param nId
     *          The Id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the Name
     *
     * @return The Name
     */
    public String getName(  )
    {
        return _strName;
    }

    /**
     * Sets the Name
     *
     * @param strName
     *          The Name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the Code
     *
     * @return The Code
     */
    public String getCode(  )
    {
        return _strCode;
    }

    /**
     * Sets the Code
     *
     * @param strCode
     *          The Code
     */
    public void setCode( String strCode )
    {
        _strCode = strCode;
    }

    /**
     * Returns the Description
     *
     * @return The Description
     */
    public String getDescription(  )
    {
        return _strDescription;
    }

    /**
     * Sets the Description
     *
     * @param strDescription
     *          The Description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the Logo
     *
     * @return The Logo
     */
    public byte[] getLogo(  )
    {
        return _logo;
    }

    /**
     * Sets the Logo
     *
     * @param logo
     *          The Logo
     */
    public void setLogo( byte[] logo )
    {
        _logo = logo;
    }

    /**
     * Returns the LogoMimeType
     *
     * @return The LogoMimeType
     */
    public String getLogoMimeType(  )
    {
        return _strLogoMimeType;
    }

    /**
     * Sets the LogoMimeType
     *
     * @param strLogoMimeType
     *          The LogoMimeType
     */
    public void setLogoMimeType( String strLogoMimeType )
    {
        _strLogoMimeType = strLogoMimeType;
    }
}
