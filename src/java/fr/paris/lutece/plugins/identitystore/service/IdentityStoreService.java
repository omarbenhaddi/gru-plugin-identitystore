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

import fr.paris.lutece.plugins.identitystore.business.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.KeyType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    // Beans
    private static final String BEAN_LISTENERS_LIST = "identitystore.changelisteners.list";
    private static final String BEAN_APPLICATION_CODE_DELETE_AUTHORIZED_LIST = "identitystore.application.code.delete.authorized.list";

    // Other constants
    private static final String ERROR_DELETE_UNAUTHORIZED = "Provided application code is not authorized to delete an identity";
    private static List<AttributeChangeListener> _listListeners;
    private static List<String> _listDeleteAuthorizedApplicationCodes;

    /**
     * private constructor
     */
    private IdentityStoreService( )
    {
    }

    /**
     * create identity
     *
     * @param identity
     *            identity to create
     */
    public static void createIdentity( Identity identity )
    {
        IdentityHome.create( identity );
    }

    /**
     * returns attributes from connection id
     *
     * @param strConnectionId
     *            connection id
     * @return full attributes list for user identified by connection id
     */
    public static Map<String, IdentityAttribute> getAttributesByConnectionId( String strConnectionId )
    {
        Identity identity = IdentityHome.findByConnectionId( strConnectionId );

        if ( identity != null )
        {
            return IdentityAttributeHome.getAttributes( identity.getId( ) );
        }

        return null;
    }

    /**
     * returns attributes from connection id
     *
     * @param strConnectionId
     *            connection id
     * @param strClientApplicationCode
     *            application code who requested attributes
     * @return attributes list according to application rights for user identified by connection id
     */
    public static Map<String, IdentityAttribute> getAttributesByConnectionId( String strConnectionId, String strClientApplicationCode )
    {
        Identity identity = IdentityHome.findByConnectionId( strConnectionId );

        if ( identity != null )
        {
            return IdentityAttributeHome.getAttributes( identity.getId( ), strClientApplicationCode );
        }

        return null;
    }

    /**
     * returns attributes from connection id
     *
     * @param strConnectionId
     *            connection id
     * @param strAttributeKey
     *            attribute key
     * @param strClientApplicationCode
     *            application code who requested attributes
     * @return attributes list according to application rights for user identified by connection id
     */
    public static IdentityAttribute getAttribute( String strConnectionId, String strAttributeKey, String strClientApplicationCode )
    {
        Identity identity = IdentityHome.findByConnectionId( strConnectionId );

        if ( identity != null )
        {
            return IdentityAttributeHome.getAttribute( identity.getId( ), strAttributeKey, strClientApplicationCode );
        }

        return null;
    }

    /**
     * returns identity from connection id
     *
     * @param strConnectionId
     *            connection id
     * @param strClientApplicationCode
     *            application code who requested identity
     * @return identity filled according to application rights for user identified by connection id
     */
    public static Identity getIdentityByConnectionId( String strConnectionId, String strClientApplicationCode )
    {
        return IdentityHome.findByConnectionId( strConnectionId, strClientApplicationCode );
    }

    /**
     * returns identity from customer id
     *
     * @param strCustomerId
     *            customer id
     * @param strClientApplicationCode
     *            application code who requested identity
     * @return identity filled according to application rights for user identified by connection id
     */
    public static Identity getIdentityByCustomerId( String strCustomerId, String strClientApplicationCode )
    {
        return IdentityHome.findByCustomerId( strCustomerId, strClientApplicationCode );
    }

    /**
     * Removes an identity from the specified connection id
     * @param strConnectionId the connection id
     * @param strClientApplicationCode the application code
     */
    public static void removeIdentity( String strConnectionId, String strClientApplicationCode )
    {
        if ( _listDeleteAuthorizedApplicationCodes == null )
        {
            _listDeleteAuthorizedApplicationCodes = SpringContextService.getBean( BEAN_APPLICATION_CODE_DELETE_AUTHORIZED_LIST );
        }

        if ( !_listDeleteAuthorizedApplicationCodes.contains( strClientApplicationCode ) )
        {
            throw new IdentityStoreException( ERROR_DELETE_UNAUTHORIZED );
        }

        int nIdentityId = IdentityHome.removeByConnectionId( strConnectionId );

        if ( nIdentityId < 0 )
        {
            throw new IdentityNotFoundException(  );
        }
    }

    /**
     * Set an attribute value associated to an identity
     *
     * @param identity
     *            identity
     * @param strKey
     *            The key to set
     * @param strValue
     *            The value
     * @param author
     *            The author of the change
     * @param certificate
     *            The certificate. May be null
     */
    public static void setAttribute( Identity identity, String strKey, String strValue, ChangeAuthor author, AttributeCertificate certificate )
    {
        setAttribute( identity, strKey, strValue, null, author, certificate );
    }

    /**
     * Set an attribute value associated to an identity
     *
     * @param identity
     *            identity
     * @param strKey
     *            The key to set
     * @param strValue
     *            The value
     * @param file
     *            file to upload, null if attribute type is not file
     * @param author
     *            The author of the change
     * @param certificate
     *            The certificate. May be null
     */
    public static void setAttribute( Identity identity, String strKey, String strValue, File file, ChangeAuthor author, AttributeCertificate certificate )
    {
        AttributeKey attributeKey = AttributeKeyHome.findByKey( strKey );
        boolean bValueUnchanged = false;

        if ( attributeKey == null )
        {
            throw new AppException( "Invalid attribute key : " + strKey );
        }

        String strCorrectValue = ( strValue == null ) ? StringUtils.EMPTY : strValue;

        boolean bCreate = false;

        IdentityAttribute attribute = IdentityAttributeHome.findByPrimaryKey( identity.getId( ), attributeKey.getId( ) );
        String strAttrOldValue = StringUtils.EMPTY;

        if ( attribute == null )
        {
            attribute = new IdentityAttribute( );
            attribute.setAttributeKey( attributeKey );
            attribute.setIdIdentity( identity.getId( ) );
            bCreate = true;
        }
        else
        {
            strAttrOldValue = attribute.getValue( );

            if ( attribute.getValue( ).equals( strCorrectValue ) && ( attributeKey.getKeyType( ) != KeyType.FILE ) )
            {
                AppLogService.debug( "no change on attribute key=" + strKey + " value=" + strCorrectValue + " for Id=" + identity.getId( ) );
                bValueUnchanged = true;
            }
        }

        AttributeCertificate attributeCertifPrev = null;

        if ( attribute.getIdCertificate( ) != 0 )
        {
            attributeCertifPrev = AttributeCertificateHome.findByPrimaryKey( attribute.getIdCertificate( ) );
        }

        // attribute value changed or attribute has new certification
        if ( !bValueUnchanged
                || ( ( certificate != null ) && ( ( attributeCertifPrev == null ) || ( certificate.getIdCertifier( ) != attributeCertifPrev.getIdCertifier( ) ) ) ) )
        {
            if ( certificate != null )
            {
                AttributeCertificateHome.create( certificate );
                attribute.setIdCertificate( certificate.getId( ) );
            }
            else
            {
                if ( attributeCertifPrev != null )
                {
                    attribute.setIdCertificate( 0 );
                    AttributeCertificateHome.remove( attributeCertifPrev.getId( ) );
                }
            }

            attribute.setValue( strCorrectValue );

            if ( attributeKey.getKeyType( ) == KeyType.FILE )
            {
                handleFile( attribute, file );
            }

            AttributeChange change = getAttributeChange( identity, strKey, strCorrectValue, strAttrOldValue, author, certificate, bCreate );

            if ( bCreate )
            {
                IdentityAttributeHome.create( attribute );
            }
            else
            {
                IdentityAttributeHome.update( attribute );
            }

            notifyListeners( change );
        }

        identity.getAttributes( ).put( attributeKey.getKeyName( ), attribute );
    }

    /**
     * create and return an AttributeChange from input params
     *
     * @param identity
     *            modified identity
     * @param strKey
     *            attribute key which is modified
     * @param strValue
     *            attribute new value
     * @param strOldValue
     *            attribute old value
     * @param author
     *            author of change
     * @param certificate
     *            attribute certificate if it s a certification case
     * @param bIsCreation
     *            true if attribute is a new one, false if it s an update
     * @return AttributeChange from input params
     */
    private static AttributeChange getAttributeChange( Identity identity, String strKey, String strValue, String strOldValue, ChangeAuthor author,
            AttributeCertificate certificate, boolean bIsCreation )
    {
        AttributeChange change = new AttributeChange( );
        change.setIdentityId( identity.getId( ) );
        change.setIdentityConnectionId( identity.getConnectionId( ) );
        change.setCustomerId( identity.getCustomerId( ) );
        change.setIdentityName( identity.getGivenName( ) + " " + identity.getFamilyName( ) );
        change.setChangedKey( strKey );
        change.setOldValue( strOldValue );
        change.setNewValue( strValue );
        change.setAuthorName( author.getUserName( ) );
        change.setAuthorId( author.getEmail( ) );
        change.setAuthorService( author.getApplication( ) );
        change.setAuthorType( author.getType( ) );
        change.setDateChange( new Timestamp( ( new Date( ) ).getTime( ) ) );

        if ( certificate != null )
        {
            change.setCertifier( certificate.getCertifier( ) );
        }

        if ( bIsCreation )
        {
            change.setChangeType( AttributeChangeType.CREATE );
        }
        else
        {
            change.setChangeType( AttributeChangeType.UPDATE );
        }

        return change;
    }

    /**
     * handle file param
     *
     * @param attribute
     *            attribute describing file
     * @param file
     *            uploaded file
     *
     */
    private static void handleFile( IdentityAttribute attribute, File file )
    {
        if ( file != null )
        {
            if ( attribute.getFile( ) != null )
            {
                FileHome.remove( attribute.getFile( ).getIdFile( ) );
            }

            file.setIdFile( FileHome.create( file ) );
            attribute.setFile( file );
            attribute.setValue( file.getTitle( ) );
        }
        else
        {
            // remove file
            if ( attribute.getFile( ) != null )
            {
                FileHome.remove( attribute.getFile( ).getIdFile( ) );
            }

            attribute.setFile( null );
            attribute.setValue( StringUtils.EMPTY );
        }
    }

    /**
     * Notify a change to all registered listeners
     *
     * @param change
     *            The change
     */
    private static void notifyListeners( AttributeChange change )
    {
        if ( _listListeners == null )
        {
            _listListeners = SpringContextService.getBean( BEAN_LISTENERS_LIST );

            StringBuilder sbLog = new StringBuilder( );
            sbLog.append( "IdentityStore - loading listeners  : " );

            for ( AttributeChangeListener listener : _listListeners )
            {
                sbLog.append( "\n\t\t\t\t - " ).append( listener.getName( ) );
            }

            AppLogService.info( sbLog.toString( ) );
        }

        for ( AttributeChangeListener listener : _listListeners )
        {
            listener.processAttributeChange( change );
        }
    }
}
