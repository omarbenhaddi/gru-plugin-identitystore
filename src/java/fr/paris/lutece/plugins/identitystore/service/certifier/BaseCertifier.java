/*
 * Copyright (c) 2002-2015, Mairie de Paris
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
package fr.paris.lutece.plugins.identitystore.service.certifier;

import fr.paris.lutece.plugins.identitystore.business.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.service.AuthorType;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * BaseCertifier
 */
public class BaseCertifier implements Certifier
{
    private static final int NO_CERTIFICATE_EXPIRATION_DELAY = -1;

    protected String _strCode;
    protected String _strName;
    protected String _strDescription;
    protected int _nCertificateLevel;
    protected String _strIconUrl;
    protected int _nExpirationDelay;
    protected static List<String> _listCertifiableAttributes;

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName()
    {
        return _strName;
    }

    /**
     * @{@inheritDoc}
     */
    public String getCode()
    {
        return _strCode;
    }

    /**
     * Sets the Code
     *
     * @param strCode The Code
     */
    public void setCode( String strCode )
    {
        _strCode = strCode;
    }

    /**
     * Sets the Name
     *
     * @param strName The Name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the Description
     *
     * @return The Description
     */
    public String getDescription()
    {
        return _strDescription;
    }

    /**
     * Sets the Description
     *
     * @param strDescription The Description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the CertificateLevel
     *
     * @return The CertificateLevel
     */
    public int getCertificateLevel()
    {
        return _nCertificateLevel;
    }

    /**
     * Sets the CertificateLevel
     *
     * @param nCertificateLevel The CertificateLevel
     */
    public void setCertificateLevel( int nCertificateLevel )
    {
        _nCertificateLevel = nCertificateLevel;
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public String getIconUrl()
    {
        return _strIconUrl;
    }

    /**
     * Sets the IconUrl
     *
     * @param strIconUrl The IconUrl
     */
    public void setIconUrl( String strIconUrl )
    {
        _strIconUrl = strIconUrl;
    }

    /**
     * Sets the ExpirationDelay
     *
     * @param nExpirationDelay The ExpirationDelay
     */
    public void setExpirationDelay( int nExpirationDelay )
    {
        _nExpirationDelay = nExpirationDelay;
    }

    /**
     * Setter for Spring Context
     *
     * @param list The list
     */
    public void setCertifiableAttributesList( List list )
    {
        _listCertifiableAttributes = list;
    }
    
    /**
     * @{@inheritDoc}
     */
    @Override
    public List<String> getCertifiableAttributesList( )
    {
        return _listCertifiableAttributes;
    }

    /**
     * Certify the attribute change
     *
     * @param identityDto The identity data
     * @param strClientCode the client code
     */
    @Override
    public void certify( IdentityDto identityDto, String strClientCode )
    {
        AttributeCertificate certificate = new AttributeCertificate();
        certificate.setCertificateDate( new Timestamp( new Date().getTime() ) );
        certificate.setCertificateLevel( _nCertificateLevel );
        certificate.setCertifierName( _strName );
        certificate.setCertifierCode( _strCode );

        if( _nExpirationDelay != NO_CERTIFICATE_EXPIRATION_DELAY )
        {
            Calendar c = Calendar.getInstance();
            c.setTime( new Date() );
            c.add( Calendar.DATE, _nExpirationDelay );
            certificate.setExpirationDate( new Timestamp( c.getTime().getTime() ) );
        }

        ChangeAuthor author = new ChangeAuthor();
        author.setApplication( _strName );
        author.setType( AuthorType.TYPE_USER_OWNER.getTypeValue() );

        Identity identity = IdentityHome.findByConnectionId( identityDto.getConnectionId() );
        for( String strField : _listCertifiableAttributes )
        {
            AttributeDto attribute = identityDto.getAttributes().get( strField );
            if( ( attribute != null ) && ( attribute.getValue() != null ) )
            {
                IdentityStoreService.setAttribute( identity, strField, attribute.getValue(), author, certificate );
            }
        }

    }

}