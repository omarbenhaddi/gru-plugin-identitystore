package fr.paris.lutece.plugins.identitystore.service;

import fr.paris.lutece.plugins.identitystore.business.Attribute;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.portal.service.util.AppException;
import java.sql.Date;
import java.util.List;

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


/**
 * IdentityStoreService
 */
public class IdentityStoreService 
{
    
    public static List<Attribute> getAttributesByConnectionId( String strConnectionId )
    {
        Identity identity = IdentityHome.findByConnectionId( strConnectionId );
        if( identity != null )
        {
            return IdentityAttributeHome.getAttributesList( identity.getId() );
        }
        return null;
    }
    
    public static void setAttribute( String strConnectionId, String strKey, String strValue, AttributeCertifier certifier )
    {
        int nAttributeId = AttributeKeyHome.findByKey( strKey ); 
        if( nAttributeId < 0 )
        {
            throw new AppException( "Invalid attribute key : " + strKey );
        }
        Identity identity = IdentityHome.findByConnectionId( strConnectionId );
        
        IdentityAttribute attribute = new IdentityAttribute();
        attribute.setIdAttribute( nAttributeId );
        attribute.setIdIdentity( identity.getId());
        if( certifier != null )
        {
            AttributeCertificate certificate = new AttributeCertificate();
            certificate.setCertificateDate( new Date( (new java.util.Date()).getTime()) );
            certificate.setIdCertifier( certifier.getId() );
            certificate.setCertificateLevel( 1 );
            AttributeCertificateHome.create( certificate );
            attribute.setIdCertificate( certificate.getId() );
        }
        attribute.setAttributeValue( strValue );
        IdentityAttributeHome.create( attribute );
    }
}
