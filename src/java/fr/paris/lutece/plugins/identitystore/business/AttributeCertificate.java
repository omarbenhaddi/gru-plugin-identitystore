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

import fr.paris.lutece.plugins.identitystore.service.certifier.Certifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import java.io.Serializable;

import java.sql.Timestamp;

/**
 * This is the business class for the object AttributeCertificate
 */
public class AttributeCertificate implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;
    private String _strCertifierCode;
    private String _strCertifierName;
    private Timestamp _dateCertificateDate;
    private int _nCertificateLevel;
    private Timestamp _dateExpirationDate;

    /**
     * Returns the Id
     *
     * @return The Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the Id
     *
     * @param nId
     *            The Id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the CertificateTimestamp
     *
     * @return The CertificateDate
     */
    public Timestamp getCertificateDate( )
    {
        return _dateCertificateDate;
    }

    /**
     * Sets the CertificateDate
     *
     * @param dateCertificateDate
     *            The CertificateDate
     */
    public void setCertificateDate( Timestamp dateCertificateDate )
    {
        _dateCertificateDate = dateCertificateDate;
    }

    /**
     * Returns the CertificateLevel
     *
     * @return The CertificateLevel
     */
    public int getCertificateLevel( )
    {
        return _nCertificateLevel;
    }

    /**
     * Sets the CertificateLevel
     *
     * @param nCertificateLevel
     *            The CertificateLevel
     */
    public void setCertificateLevel( int nCertificateLevel )
    {
        _nCertificateLevel = nCertificateLevel;
    }

    /**
     * Returns the ExpirationDate
     *
     * @return The ExpirationDate
     */
    public Timestamp getExpirationDate( )
    {
        return _dateExpirationDate;
    }

    /**
     * Sets the ExpirationDate
     *
     * @param dateExpirationDate
     *            The ExpirationDate
     */
    public void setExpirationDate( Timestamp dateExpirationDate )
    {
        _dateExpirationDate = dateExpirationDate;
    }

    /**
     * Returns the Certifier code
     *
     * @return The Certifier code
     */
    public String getCertifierCode( )
    {
        return _strCertifierCode;
    }

    /**
     * Sets the Certifier code
     *
     * @param strCertifierCode
     *            The Certifier code
     */
    public void setCertifierCode( String strCertifierCode )
    {
        _strCertifierCode = strCertifierCode;
    }

    /**
     * Returns the Certifier name
     *
     * @return The Certifier name
     */
    public String getCertifierName( )
    {
        return _strCertifierName;
    }

    /**
     * Sets the Certifier name
     *
     * @param strCertifierName
     *            The Certifier name
     */
    public void setCertifierName( String strCertifierName )
    {
        _strCertifierName = strCertifierName;
    }

    /**
     * Get the Icon URL
     * 
     * @return the Icon URL
     */
    public String getCertifierIconUrl( )
    {
        try
        {
            Certifier certifier = CertifierRegistry.instance( ).getCertifier( _strCertifierCode );
            return certifier.getIconUrl( );
        }
        catch( CertifierNotFoundException ex )
        {
            return "";
        }

    }
}
