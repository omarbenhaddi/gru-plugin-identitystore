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

import fr.paris.lutece.plugins.identitystore.business.Attribute;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.sql.Timestamp;

import java.util.Date;
import java.util.List;


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

/**
 * IdentityStoreService
 */
public final class IdentityStoreService
{
    private static final String BEAN_LISTENERS_LIST = "identitystore.changelisteners.list";
    private static List<AttributeChangeListener> _listListeners;

    /**
     * private constructor
     */
    private IdentityStoreService(  )
    {
    }

    /**
     * returns attributes from connection id
     * @param strConnectionId  connection id
     * @return full attributes list for user identified by connection id
     */
    public static List<Attribute> getAttributesByConnectionId( String strConnectionId )
    {
        Identity identity = IdentityHome.findByConnectionId( strConnectionId );

        if ( identity != null )
        {
            return IdentityAttributeHome.getAttributesList( identity.getId(  ) );
        }

        return null;
    }

    /**
     * returns attributes from connection id
     * @param strConnectionId  connection id
     * @param strClientApplicationCode application code who requested attributes
     * @return attributes list according to application rights  for user identified by connection id
     */
    public static List<Attribute> getAttributesByConnectionId( String strConnectionId, String strClientApplicationCode )
    {
        Identity identity = IdentityHome.findByConnectionId( strConnectionId );

        if ( identity != null )
        {
            return IdentityAttributeHome.getAttributesList( identity.getId(  ), strClientApplicationCode );
        }

        return null;
    }

    /**
     * returns identity from connection id
     * @param strConnectionId  connection id
     * @param strClientApplicationCode application code who requested identity
     * @return identity filled according to application rights  for user identified by connection id
     */
    public static Identity getIdentity( String strConnectionId, String strClientApplicationCode )
    {
        Identity identity = IdentityHome.findByConnectionId( strConnectionId, strClientApplicationCode );

        if ( identity != null )
        {
            return identity;
        }

        return null;
    }

    /**
     * Set an attribute value associated to an identity
     * @param strConnectionId The connection ID
     * @param strKey The key to set
     * @param strValue The value
     * @param author The author of the change
     * @param certifier The certifier. May be null
     */
    public static void setAttribute( String strConnectionId, String strKey, String strValue, ChangeAuthor author,
        AttributeCertifier certifier )
    {
        int nAttributeId = AttributeKeyHome.findByKey( strKey );

        if ( nAttributeId < 0 )
        {
            throw new AppException( "Invalid attribute key : " + strKey );
        }

        Identity identity = IdentityHome.findByConnectionId( strConnectionId );

        boolean bCreate = false;

        IdentityAttribute attribute = IdentityAttributeHome.findByPrimaryKey( identity.getId(  ), nAttributeId );

        if ( attribute == null )
        {
            attribute = new IdentityAttribute(  );
            attribute.setIdAttribute( nAttributeId );
            attribute.setIdIdentity( identity.getId(  ) );
            bCreate = true;
        }

        if ( certifier != null )
        {
            AttributeCertificate certificate = new AttributeCertificate(  );
            certificate.setCertificateDate( new Timestamp( ( new Date(  ) ).getTime(  ) ) );
            certificate.setExpirationDate( new Timestamp( ( new Date(  ) ).getTime(  ) + 80000000000L ) ); // FIXME
            certificate.setIdCertifier( certifier.getId(  ) );
            certificate.setCertificateLevel( 2 ); // FIXME 
            AttributeCertificateHome.create( certificate );
            attribute.setIdCertificate( certificate.getId(  ) );
        }

        attribute.setAttributeValue( strValue );

        AttributeChange change = new AttributeChange(  );
        change.setIdentityId( identity.getConnectionId(  ) );
        change.setIdentityName( identity.getGivenName(  ) + " " + identity.getFamilyName(  ) );
        change.setChangedKey( strKey );
        change.setNewValue( strValue );
        change.setAuthorName( author.getUserName(  ) );
        change.setAuthorId( author.getUserId(  ) );
        change.setAuthorService( author.getApplication(  ) );
        change.setAuthorType( author.getType(  ) );
        change.setDateChange( new Timestamp( ( new Date(  ) ).getTime(  ) ) );

        if ( bCreate )
        {
            IdentityAttributeHome.create( attribute );
            change.setChangeType( AttributeChange.TYPE_CREATE );
        }
        else
        {
            IdentityAttributeHome.update( attribute );
            change.setChangeType( AttributeChange.TYPE_UPDATE );
        }

        notifyListeners( change );
    }

    /**
     * Notify a change to all registered listeners
     * @param change The change
     */
    private static void notifyListeners( AttributeChange change )
    {
        if ( _listListeners == null )
        {
            _listListeners = SpringContextService.getBean( BEAN_LISTENERS_LIST );

            StringBuilder sbLog = new StringBuilder(  );
            sbLog.append( "IdentityStore - loading listeners  : " );

            for ( AttributeChangeListener listener : _listListeners )
            {
                sbLog.append( "\n\t\t\t\t - " ).append( listener.getName(  ) );
            }

            AppLogService.info( sbLog.toString(  ) );
        }

        for ( AttributeChangeListener listener : _listListeners )
        {
            listener.processAttributeChange( change );
        }
    }
}
