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
import fr.paris.lutece.plugins.identitystore.service.external.IdentityInfoExternalService;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.service.Constants;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
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
    private static final String BEAN_APPLICATION_CODE_DELETE_AUTHORIZED_LIST = "identitystore.application.code.delete.authorized.list";

    // Other constants
    private static final String ERROR_NO_IDENTITY_PROVIDED = "Neither the guid, nor the cid, nor the identity attributes are provided !!!";
    private static final String ERROR_DELETE_UNAUTHORIZED = "Provided application code is not authorized to delete an identity";
    private static List<String> _listDeleteAuthorizedApplicationCodes;

    /**
     * private constructor
     */
    private IdentityStoreService( )
    {
    }

    /**
     * Creates an identity. If the provided identity is new, a creation of the object {@code Identity} is done, otherwise, the provided identity is used.
     *
     * @param identityChangeDto
     *            the object {@code IdentityChangeDto} containing the information to perform the creation
     * @param mapAttachedFiles
     *            the files to create
     * @return the created identity
     */
    public static Identity createIdentity( IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles )
    {
        String strCustomerId = identityChangeDto.getIdentity( ).getCustomerId( );
        String strConnectionId = identityChangeDto.getIdentity( ).getConnectionId( );
        String strClientAppCode = identityChangeDto.getAuthor( ).getApplicationCode( );
        Identity identity = null;

        if ( StringUtils.isNotEmpty( strCustomerId ) )
        {
            identity = IdentityStoreService.getIdentityByCustomerId( strCustomerId, strClientAppCode );

            if ( identity == null )
            {
                throw new IdentityNotFoundException( "No identity found for " + Constants.PARAM_ID_CUSTOMER + "(" + strCustomerId + ")" );
            }
        }
        else
        {
            if ( StringUtils.isNotEmpty( strConnectionId ) )
            {
                identity = IdentityStoreService.getIdentityByConnectionId( strConnectionId, strClientAppCode );

                if ( identity == null )
                {
                    identity = IdentityStoreService.createIdentity( strConnectionId );
                    identity = IdentityStoreService.updateIdentity( identity, identityChangeDto, mapAttachedFiles );
                }
            }
            else
            {
                identity = new Identity( );
                IdentityHome.create( identity );
                updateIdentity( identity, identityChangeDto, mapAttachedFiles );
            }
        }

        IdentityChange identityChange = new IdentityChange( );
        identityChange.setIdentity( identity );
        identityChange.setChangeType( IdentityChangeType.CREATE );
        IdentityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );

        return identity;
    }

    /**
     * get identity from connectionId or customerId If no identity is found then a new one be created with attributes based on external provider data
     *
     * @param strConnectionId
     *            connection id
     * @param strCustomerId
     *            customer id
     * @param strClientAppCode
     *            client application code
     * @return identity , null if no identity found
     * @throws AppException
     *             if provided connectionId and customerId are not consistent
     */
    public static Identity getOrCreateIdentity( String strConnectionId, String strCustomerId, String strClientAppCode )
    {
        Identity identity = null;

        if ( StringUtils.isNotBlank( strConnectionId ) )
        {
            identity = IdentityStoreService.getIdentityByConnectionId( strConnectionId, strClientAppCode );

            if ( ( identity != null ) && ( StringUtils.isNotEmpty( strCustomerId ) ) && ( !identity.getCustomerId( ).equals( strCustomerId ) ) )
            {
                throw new AppException( "inconsistent " + Constants.PARAM_ID_CONNECTION + "(" + strConnectionId + ")" + " AND " + Constants.PARAM_ID_CUSTOMER
                        + "(" + strCustomerId + ")" + " params provided " );
            }

            if ( identity == null )
            {
                try
                {
                    identity = IdentityStoreService.createIdentity( strConnectionId );

                    IdentityChange identityChange = new IdentityChange( );
                    identityChange.setIdentity( identity );
                    identityChange.setChangeType( IdentityChangeType.valueOf( IdentityChangeType.CREATE.getValue( ) ) );
                    IdentityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );
                }
                catch( IdentityNotFoundException e )
                {
                    // Identity not found in External provider : creation is aborted
                    AppLogService.info( "Could not create an identity from external source" );
                }
            }
        }
        else
        {
            identity = IdentityStoreService.getIdentityByCustomerId( strCustomerId, strClientAppCode );
        }

        if ( identity == null )
        {
            throw new IdentityNotFoundException( "No identity found for " + Constants.PARAM_ID_CONNECTION + "(" + strConnectionId + ")" + " AND "
                    + Constants.PARAM_ID_CUSTOMER + "(" + strCustomerId + ")" );
        }

        return identity;
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
     * Initializes an identity from an external source by using the specified connection id
     *
     * @param strConnectionId
     *            the connection id used to initialize the identity
     * @return the initialized identity
     * @throws IdentityNotFoundException
     *             if no identity can be retrieve from external source
     */
    private static Identity createIdentity( String strConnectionId ) throws IdentityNotFoundException
    {
        IdentityChangeDto identityChangeDtoInitialized = IdentityInfoExternalService.instance( ).getIdentityInfo( strConnectionId );

        Identity identity = new Identity( );
        identity.setConnectionId( strConnectionId );
        IdentityHome.create( identity );

        identity = updateIdentity( identity, identityChangeDtoInitialized, new HashMap<String, File>( ) );

        if ( AppLogService.isDebugEnabled( ) )
        {
            AppLogService.debug( "New identity created with provided guid (" + strConnectionId + ". Associated customer id is : " + identity.getCustomerId( )
                    + ". Associated attributes are : " + identity.getAttributes( ) );
        }

        return identity;
    }

    /**
     * Updates an existing identity.
     *
     * @param identity
     *            the identity to complete.
     * @param identityChangeDto
     *            the object {@code IdentityChangeDto} containing the information to perform the creation
     * @param mapAttachedFiles
     *            the files to create
     * @return the updated identity
     */
    public static Identity updateIdentity( IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles )
    {
        Identity identity = getOrCreateIdentity( identityChangeDto.getIdentity( ).getConnectionId( ), identityChangeDto.getIdentity( ).getCustomerId( ),
                identityChangeDto.getAuthor( ).getApplicationCode( ) );

        if ( identity == null )
        {
            throw new IdentityNotFoundException( "no identity found for " + Constants.PARAM_ID_CONNECTION + "("
                    + identityChangeDto.getIdentity( ).getConnectionId( ) + ")" + " AND " + Constants.PARAM_ID_CUSTOMER + "("
                    + identityChangeDto.getIdentity( ).getCustomerId( ) + ")" );
        }

        IdentityDto identityDto = identityChangeDto.getIdentity( );
        AuthorDto authorDto = identityChangeDto.getAuthor( );
        Map<String, AttributeDto> mapAttributes = identityDto.getAttributes( );

        if ( ( mapAttributes != null ) && !mapAttributes.isEmpty( ) )
        {
            IdentityRequestValidator.instance( ).checkIdentityChange( identityChangeDto );
            IdentityRequestValidator.instance( ).checkAttributes( identityDto, authorDto.getApplicationCode( ), mapAttachedFiles );

            updateAttributes( identity, identityDto, authorDto, mapAttachedFiles );
        }
        else
        {
            throw new IdentityStoreException( ERROR_NO_IDENTITY_PROVIDED );
        }

        IdentityChange identityChange = new IdentityChange( );
        identityChange.setIdentity( identity );
        identityChange.setChangeType( IdentityChangeType.valueOf( IdentityChangeType.UPDATE.getValue( ) ) );
        IdentityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );

        return identity;
    }

    /**
     * Updates an existing identity.
     *
     * @param identity
     *            the identity to complete.
     * @param identityChangeDto
     *            the object {@code IdentityChangeDto} containing the information to perform the creation
     * @param mapAttachedFiles
     *            the files to create
     * @return the updated identity
     */
    private static Identity updateIdentity( Identity identity, IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles )
    {
        IdentityDto identityDto = identityChangeDto.getIdentity( );
        AuthorDto authorDto = identityChangeDto.getAuthor( );
        Map<String, AttributeDto> mapAttributes = identityDto.getAttributes( );

        if ( ( mapAttributes != null ) && !mapAttributes.isEmpty( ) )
        {
            IdentityRequestValidator.instance( ).checkIdentityChange( identityChangeDto );
            IdentityRequestValidator.instance( ).checkAttributes( identityDto, authorDto.getApplicationCode( ), mapAttachedFiles );

            updateAttributes( identity, identityDto, authorDto, mapAttachedFiles );
        }
        else
        {
            throw new IdentityStoreException( ERROR_NO_IDENTITY_PROVIDED );
        }

        return identity;
    }

    /**
     * check if new identity attributes have errors and returns them
     *
     * @param identity
     *            the identity
     * @param identityDto
     *            new identity to update connectionId of identity which will be updated
     * @param authorDto
     *            author responsible for modification
     * @param mapAttachedFiles
     *            map containing File matching key attribute name
     *
     */
    private static void updateAttributes( Identity identity, IdentityDto identityDto, AuthorDto authorDto, Map<String, File> mapAttachedFiles )
    {
        StringBuilder sb = new StringBuilder( "Fields successfully updated : " );
        ChangeAuthor author = DtoConverter.getAuthor( authorDto );

        for ( AttributeDto attributeDto : identityDto.getAttributes( ).values( ) )
        {
            File file = null;
            AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey( ) );

            if ( attributeKey.getKeyType( ).equals( KeyType.FILE ) && StringUtils.isNotBlank( attributeDto.getValue( ) ) )
            {
                file = mapAttachedFiles.get( attributeDto.getValue( ) );
            }

            AttributeCertificate certificate = DtoConverter.getCertificate( attributeDto.getCertificate( ) );
            setAttribute( identity, attributeDto.getKey( ), attributeDto.getValue( ), file, author, certificate );
            sb.append( attributeDto.getKey( ) + "," );
        }

        AppLogService.debug( sb.toString( ) );
    }

    /**
     * Removes an identity from the specified connection id
     * 
     * @param strConnectionId
     *            the connection id
     * @param strClientApplicationCode
     *            the application code
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

        Identity identity = IdentityStoreService.getIdentityByConnectionId( strConnectionId, strClientApplicationCode );

        if ( identity == null )
        {
            throw new IdentityNotFoundException( "No identity found for " + Constants.PARAM_ID_CONNECTION + "(" + strConnectionId + ")" );
        }
        else
        {
            IdentityHome.removeByConnectionId( strConnectionId );

            IdentityChange identityChange = new IdentityChange( );
            identityChange.setIdentity( identity );
            identityChange.setChangeType( IdentityChangeType.valueOf( IdentityChangeType.DELETE.getValue( ) ) );
            IdentityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );
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
    private static void setAttribute( Identity identity, String strKey, String strValue, File file, ChangeAuthor author, AttributeCertificate certificate )
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

            AttributeChange change = IdentityStoreNotifyListenerService.buildAttributeChange( identity, strKey, strCorrectValue, strAttrOldValue, author,
                    certificate, bCreate );

            if ( bCreate )
            {
                IdentityAttributeHome.create( attribute );
            }
            else
            {
                IdentityAttributeHome.update( attribute );
            }

            IdentityStoreNotifyListenerService.notifyListenersAttributeChange( change );
        }

        identity.getAttributes( ).put( attributeKey.getKeyName( ), attribute );
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
}
