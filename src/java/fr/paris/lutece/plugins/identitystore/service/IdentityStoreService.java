/*
 * Copyright (c) 2002-2018, Mairie de Paris
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.identitystore.business.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.IdentityConstants;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.KeyType;
import fr.paris.lutece.plugins.identitystore.business.security.SecureMode;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.plugins.identitystore.service.certifier.IGenerateAutomaticCertifierAttribute;
import fr.paris.lutece.plugins.identitystore.service.encryption.IdentityEncryptionService;
import fr.paris.lutece.plugins.identitystore.service.external.IdentityInfoExternalService;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AppRightDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.ApplicationRightsDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeStatusDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.CertificateDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.service.Constants;
import fr.paris.lutece.plugins.identitystore.v2.web.service.AuthorType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityDeletedException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.jwt.service.JWTUtil;

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
    private static final String ERROR_DELETE_UNAUTHORIZED = "Provided application code is not authorized to delete an identity";
    private static List<String> _listDeleteAuthorizedApplicationCodes;

    private static IdentityEncryptionService _identityEncryptionService = IdentityEncryptionService.getInstance( );

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
    public static IdentityDto getOrCreateIdentity( IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles )
    {
        String strClientAppCode = identityChangeDto.getAuthor( ).getApplicationCode( );
        ClientApplication clientApplication = fetchClientApplication( strClientAppCode );
        IdentityDto identityDtoEncrypted = identityChangeDto.getIdentity( );
        IdentityDto identityDtoDecrypted = _identityEncryptionService.decrypt( identityDtoEncrypted, clientApplication );
        String strCustomerId = identityDtoDecrypted.getCustomerId( );
        String strConnectionId = identityDtoDecrypted.getConnectionId( );
        Identity identity = null;
        boolean bIdentityCreated = false;

        if ( StringUtils.isNotEmpty( strCustomerId ) )
        {
            identity = getIdentityByCustomerId( strCustomerId, strClientAppCode );

            if ( identity == null )
            {
                throw new IdentityNotFoundException( "No identity found for " + Constants.PARAM_ID_CUSTOMER + "(" + strCustomerId + ")" );
            }
            if ( identity.isDeleted( ) )
            {
                String strMessage = "identity is already deleted";
                StringBuilder stringBuilder = new StringBuilder( strMessage );
                stringBuilder.append( " customer id = " ).append( strCustomerId );
                AppLogService.error( stringBuilder.toString( ) );
                throw new IdentityDeletedException( strMessage );
            }
        }
        else
        {
            if ( StringUtils.isNotEmpty( strConnectionId ) )
            {
                identity = getIdentityByConnectionId( strConnectionId, strClientAppCode );

                if ( identity == null )
                {
                    identity = createIdentity( strConnectionId, strClientAppCode );
                    bIdentityCreated = true;
                    identity = completeIdentity( identity, identityChangeDto, mapAttachedFiles, clientApplication.getCode( ),
                            clientApplication.getIsAuthorizedDeleteValue( ) );
                }

            }
            else
            {
                identity = new Identity( );
                identity = IdentityHome.create( identity );
                bIdentityCreated = true;
                identity = completeIdentity( identity, identityChangeDto, mapAttachedFiles, clientApplication.getCode( ),
                        clientApplication.getIsAuthorizedDeleteValue( ) );
            }
        }

        if ( bIdentityCreated )
        {
            IdentityChange identityChange = new IdentityChange( );
            identityChange.setIdentity( identity );
            identityChange.setChangeType( IdentityChangeType.CREATE );
            IdentityStoreNotifyListenerService.instance( ).notifyListenersIdentityChange( identityChange );
        }

        identityDtoDecrypted = DtoConverter.convertToDto( identity, strClientAppCode );

        return _identityEncryptionService.encrypt( identityDtoDecrypted, clientApplication );
    }

    /**
     * Creates a basic identityChange
     *
     * @param strClientAppCode
     *            client application code
     * @return the identityChange
     * @throws AppException
     *             if the connectionId and customerId are empty
     */
    public static IdentityChangeDto buildIdentityChange( String strClientAppCode )
    {
        IdentityDto identityDto = new IdentityDto( );

        AuthorDto authorDto = new AuthorDto( );
        authorDto.setApplicationCode( strClientAppCode );
        authorDto.setType( AuthorType.TYPE_APPLICATION.getTypeValue( ) );
        authorDto.setId( strClientAppCode );

        IdentityChangeDto identityChangeDto = new IdentityChangeDto( );
        identityChangeDto.setAuthor( authorDto );
        identityChangeDto.setIdentity( identityDto );

        return identityChangeDto;
    }

    /**
     * <p>
     * Gets an identity
     * </p>
     *
     * @param identityDto
     *            the identity to find
     * @param strClientAppCode
     *            client application code
     * @return the identity
     * @throws AppException
     *             if the connectionId and customerId of the provided identity are not consistent
     * @throws IdentityNotFoundException
     *             if the identity cannot be found
     */
    private static Identity getExistingIdentity( IdentityDto identityDto, String strClientAppCode )
    {
        String strConnectionId = identityDto.getConnectionId( );
        String strCustomerId = identityDto.getCustomerId( );
        Identity identity = null;

        if ( StringUtils.isNotBlank( strConnectionId ) )
        {
            identity = getIdentityByConnectionId( strConnectionId, strClientAppCode );

            if ( ( identity != null ) && ( StringUtils.isNotEmpty( strCustomerId ) ) && ( !identity.getCustomerId( ).equals( strCustomerId ) ) )
            {
                String strMessage = "inconsistency between the connection id and the customer id";
                StringBuilder sb = new StringBuilder( strMessage );
                sb.append( " : connection id = " ).append( strConnectionId ).append( " AND customer id = " ).append( strCustomerId ).append( ")" );

                AppLogService.error( sb.toString( ) );
                throw new AppException( strMessage );
            }
        }
        else
        {
            identity = getIdentityByCustomerId( strCustomerId, strClientAppCode );
        }

        if ( identity == null )
        {
            String strMessage = "No identity found for the provided connection id and customer id";
            StringBuilder sb = new StringBuilder( strMessage );
            sb.append( " : connection id = " ).append( strConnectionId ).append( " AND customer id = " ).append( strCustomerId ).append( ")" );
            AppLogService.error( sb.toString( ) );
            throw new IdentityNotFoundException( strMessage );
        }
        if ( identity.isDeleted( ) )
        {
            String strMessage = "identity is already deleted";
            StringBuilder stringBuilder = new StringBuilder( strMessage );
            stringBuilder.append( " customer id = " ).append( strCustomerId );
            AppLogService.error( stringBuilder.toString( ) );
            throw new IdentityDeletedException( strMessage );
        }

        return identity;
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
     * returns a list of identity from combination of attributes
     *
     * @param mapAttributeValues
     *            a map that associates list of values to search for some attributes
     * @param listAttributeKeyNames
     *            a list of attributes to retrieve in identities
     * @param strClientApplicationCode
     *            application code who requested identities
     * @return identity filled according to application rights for user identified by connection id
     */
    public static List<IdentityDto> getIdentities( Map<String, List<String>> mapAttributeValues, List<String> listAttributeKeyNames, String strClientApplicationCode )
    {
        List<IdentityDto> listIdentityDto = new ArrayList<>( );

        List<Identity> listIdentity = IdentityHome.findByAttributesValueForApiSearch( mapAttributeValues );
        if ( listIdentity == null || listIdentity.isEmpty( ) )
        {
            return listIdentityDto;
        }

        List<IdentityAttribute> listIdentityAttribute = IdentityAttributeHome.getAttributesByIdentityList(listIdentity, listAttributeKeyNames, strClientApplicationCode );

        for ( Identity identity : listIdentity )
        {
            for ( IdentityAttribute identityAttribute : listIdentityAttribute )
            {
                if ( identity.getId( ) == identityAttribute.getIdIdentity( ) )
                {
                    Map<String,IdentityAttribute> mapIdentityAttributes = identity.getAttributes( );

                    if ( mapIdentityAttributes == null)
                    {
                        mapIdentityAttributes = new HashMap<>( );
                    }

                    mapIdentityAttributes.put( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute );
                    identity.setAttributes( mapIdentityAttributes );
                }
            }

            listIdentityDto.add( DtoConverter.convertToDto( identity, strClientApplicationCode ) );
        }

        return listIdentityDto;
    }

    /**
     * Initializes an identity from an external source by using the specified connection id
     *
     * @param strConnectionId
     *            the connection id used to initialize the identity
     * @param strClientAppCode
     *            , the application code chich requires creation
     * @return the initialized identity
     * @throws IdentityNotFoundException
     *             if no identity can be retrieve from external source
     */
    private static Identity createIdentity( String strConnectionId, String strClientAppCode ) throws IdentityNotFoundException
    {
        IdentityChangeDto identityChangeDtoInitialized = IdentityInfoExternalService.instance( ).getIdentityInfo( strConnectionId );

        Identity identity = new Identity( );
        identity.setConnectionId( strConnectionId );
        IdentityHome.create( identity );

        if ( identityChangeDtoInitialized != null && identityChangeDtoInitialized.getIdentity( ) != null
                && MapUtils.isNotEmpty( identityChangeDtoInitialized.getIdentity( ).getAttributes( ) ) )
        {
            // Update has to be done only if external info has something, elsewhere ERROR_NO_IDENTITY_PROVIDED will be thrown by update
            // IdentityInfoExternalService impl HAVE TO throw an IDENTITY_NOT_FOUND in case of identity doesn't exist
            identity = completeIdentity( identity, identityChangeDtoInitialized, new HashMap<String, File>( ), strClientAppCode, true );
        }

        if ( AppLogService.isDebugEnabled( ) )
        {
            AppLogService.debug( "New identity created with provided guid (" + strConnectionId + ". Associated customer id is : " + identity.getCustomerId( )
                    + ". Associated attributes are : " + identity.getAttributes( ) );
        }

        return identity;
    }

    /**
     * Updates an existing identity. Manage certification as defined in api documentation
     *
     * @param identityChangeDto
     *            the object {@code IdentityChangeDto} containing the information to perform the creation
     * @param mapAttachedFiles
     *            the files to create
     * @return the updated identity with status on attribute
     */
    public static IdentityDto updateIdentity( IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles )
    {
        // init dtos
        AuthorDto authorDto = identityChangeDto.getAuthor( );
        String strClientAppCode = authorDto.getApplicationCode( );
        ClientApplication clientApplication = fetchClientApplication( strClientAppCode );
        IdentityDto identityDtoEncrypted = identityChangeDto.getIdentity( );
        IdentityDto identityDtoDecrypted = _identityEncryptionService.decrypt( identityDtoEncrypted, clientApplication );
        Identity identity = getExistingIdentity( identityDtoDecrypted, strClientAppCode );
        // identity can't be null here, getExistingIdentity throw exception in this case

        Map<String, AttributeDto> mapAttributes = identityDtoDecrypted.getAttributes( );

        if ( ( mapAttributes != null ) && !mapAttributes.isEmpty( ) )
        {
            IdentityRequestValidator.instance( ).checkIdentityChange( identityChangeDto );
            IdentityRequestValidator.instance( ).checkAttributes( identityDtoDecrypted, strClientAppCode, mapAttachedFiles );
            IdentityRequestValidator.instance( ).checkCertification( identityDtoDecrypted, strClientAppCode );
            //add automatically certification attribute 
            addAutomaticalyCertificationAttributes(identityDtoDecrypted);
            
            updateAttributes( identity, identityDtoDecrypted, authorDto, mapAttachedFiles, clientApplication.getCode( ),
                    clientApplication.getIsAuthorizedDeleteValue( ) );
        }

        // listener
        IdentityChange identityChange = new IdentityChange( );
        identityChange.setIdentity( identity );
        identityChange.setChangeType( IdentityChangeType.valueOf( IdentityChangeType.UPDATE.getValue( ) ) );
        IdentityStoreNotifyListenerService.instance( ).notifyListenersIdentityChange( identityChange );

        // return
        identityDtoDecrypted = DtoConverter.convertToDto( identity, strClientAppCode );
        return _identityEncryptionService.encrypt( identityDtoDecrypted, clientApplication );
    }

    /**
     * create Attributes from an identityChange of creation process
     *
     * @param identity
     *            the identity to complete.
     * @param identityChangeDto
     *            the object {@code IdentityChangeDto} containing the information to perform the creation
     * @param mapAttachedFiles
     *            the files to create
     * @param bAppCanDelete
     *            true if application can delete a non certified attribute
     * @param strClientAppCode
     *            client application code
     * @return the updated identity
     */
    private static Identity completeIdentity( Identity identity, IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles,
            String strClientAppCode, boolean bAppCanDelete )
    {
        IdentityDto identityDto = identityChangeDto.getIdentity( );
        AuthorDto authorDto = identityChangeDto.getAuthor( );
        Map<String, AttributeDto> mapAttributes = identityDto.getAttributes( );

        if ( ( mapAttributes != null ) && !mapAttributes.isEmpty( ) )
        {
            IdentityRequestValidator.instance( ).checkIdentityChange( identityChangeDto );
            IdentityRequestValidator.instance( ).checkAttributes( identityDto, authorDto.getApplicationCode( ), mapAttachedFiles );

            createAttributes( identity, identityDto, authorDto, mapAttachedFiles, strClientAppCode );
        }

        return identity;
    }

    /**
     * check if new identity attributes have errors and returns them. Manage certification as defined in api documentation
     *
     * @param identity
     *            the identity
     * @param identityDto
     *            new identity to update connectionId of identity which will be updated
     * @param authorDto
     *            author responsible for modification
     * @param mapAttachedFiles
     *            map containing File matching key attribute name
     * @param strClientAppCode
     *            client application code
     */
    private static void createAttributes( Identity identity, IdentityDto identityDto, AuthorDto authorDto, Map<String, File> mapAttachedFiles,
            String strClientAppCode )
    {
        StringBuilder sb = new StringBuilder( "Fields create result : " );
        ChangeAuthor author = DtoConverter.getAuthor( authorDto );

        for ( AttributeDto attributeDto : identityDto.getAttributes( ).values( ) )
        {
            File file = null;
            AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey( ) );
            if ( attributeKey.getKeyType( ).equals( KeyType.FILE ) && StringUtils.isNotBlank( attributeDto.getValue( ) ) )
            {
                file = mapAttachedFiles.get( attributeDto.getValue( ) );
            }

            IdentityAttribute newAttribute = new IdentityAttribute( );
            newAttribute.setAttributeKey( attributeKey );
            newAttribute.setValue( attributeDto.getValue( ) );
            newAttribute.setCertificate( null );

            AttributeStatusDto attrStatus = setAttribute( identity, newAttribute, file, author, strClientAppCode, true );
            sb.append( attributeDto.getKey( ) + "[" + attrStatus.getStatusCode( ) + "], " );
        }

        AppLogService.debug( sb.toString( ) );
    }

    /**
     * check if new identity attributes have errors and returns them. Manage certification as defined in api documentation
     *
     * @param identity
     *            the identity
     * @param identityDto
     *            new identity to update connectionId of identity which will be updated
     * @param authorDto
     *            author responsible for modification
     * @param mapAttachedFiles
     *            map containing File matching key attribute name
     * @param strClientAppCode
     *            client application code
     * @param bAppCanDelete
     *            true if application can delete a non certified attribute
     */
    private static void updateAttributes( Identity identity, IdentityDto identityDto, AuthorDto authorDto, Map<String, File> mapAttachedFiles,
            String strClientAppCode, boolean bAppCanDelete )
    {
        StringBuilder sb = new StringBuilder( "Fields update result : " );
        ChangeAuthor author = DtoConverter.getAuthor( authorDto );

        for ( AttributeDto attributeDto : identityDto.getAttributes( ).values( ) )
        {
            File file = null;
            AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey( ) );

            if ( attributeKey.getKeyType( ).equals( KeyType.FILE ) && StringUtils.isNotBlank( attributeDto.getValue( ) ) )
            {
                file = mapAttachedFiles.get( attributeDto.getValue( ) );
            }
            AttributeCertificate certificate = null;
            try
            {
                AttributeCertificate requestCertificate = DtoConverter.getCertificate( attributeDto.getCertificate( ) );
                AbstractCertifier certifier = CertifierRegistry.instance( ).getCertifier( requestCertificate.getCertifierCode( ) );
                certificate = certifier.generateCertificate( );
            }
            catch( Exception e )
            {
                // Unable to get the certificate from the Dto; set the updateAttribute with empty certificate
                AppLogService
                        .debug( "Unable to retrieve certificate for attribute [" + attributeDto.getKey( ) + "] of identity [" + identity.getId( ) + "]", e );
            }

            IdentityAttribute updateAttribute = new IdentityAttribute( );
            updateAttribute.setAttributeKey( attributeKey );
            updateAttribute.setValue( attributeDto.getValue( ) );
            updateAttribute.setCertificate( certificate );

            AttributeStatusDto attrStatus = setAttribute( identity, updateAttribute, file, author, strClientAppCode, bAppCanDelete );
            sb.append( attributeDto.getKey( ) + "[" + attrStatus.getStatusCode( ) + "], " );
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

        ClientApplication clientApplication = fetchClientApplication( strClientApplicationCode );
        IdentityDto identityDtoEncrypted = new IdentityDto( );
        identityDtoEncrypted.setConnectionId( strConnectionId );
        IdentityDto identityDtoDecrypted = _identityEncryptionService.decrypt( identityDtoEncrypted, clientApplication );
        String strConnectionIdDecrypted = identityDtoDecrypted.getConnectionId( );

        if ( _listDeleteAuthorizedApplicationCodes == null )
        {
            _listDeleteAuthorizedApplicationCodes = SpringContextService.getBean( BEAN_APPLICATION_CODE_DELETE_AUTHORIZED_LIST );
        }

        if ( !_listDeleteAuthorizedApplicationCodes.contains( strClientApplicationCode ) )
        {
            throw new IdentityStoreException( ERROR_DELETE_UNAUTHORIZED );
        }

        Identity identity = getIdentityByConnectionId( strConnectionIdDecrypted, strClientApplicationCode );

        if ( identity == null )
        {
            throw new IdentityNotFoundException( "No identity found for " + Constants.PARAM_ID_CONNECTION + "(" + strConnectionId + ")" );
        }
        else
        {
            IdentityHome.removeByConnectionId( strConnectionIdDecrypted );
            IdentityChange identityChange = new IdentityChange( );
            identityChange.setIdentity( identity );
            identityChange.setChangeType( IdentityChangeType.valueOf( IdentityChangeType.DELETE.getValue( ) ) );
            IdentityStoreNotifyListenerService.instance( ).notifyListenersIdentityChange( identityChange );
        }
    }

    /**
     * generate the ApplicationRightsDto of a given application
     * 
     * @param strClientAppCode
     *            the code application
     * @return ApplicationRightsDto filled
     */
    public static ApplicationRightsDto getApplicationRights( String strClientAppCode )
    {
        ClientApplication clientApp = fetchClientApplication( strClientAppCode );
        List<AttributeRight> listAttributeRight = ClientApplicationHome.selectApplicationRights( clientApp );
        List<AbstractCertifier> listCertifier = ClientApplicationHome.getCertifiers( clientApp );
        ApplicationRightsDto appRightsDto = new ApplicationRightsDto( );
        appRightsDto.setApplicationCode( clientApp.getCode( ) );
        for ( AttributeRight attrRight : listAttributeRight )
        {
            if ( attrRight.isReadable( ) || attrRight.isWritable( ) )
            {
                AppRightDto appRightDto = new AppRightDto( );
                appRightDto.setAttributeKey( attrRight.getAttributeKey( ).getKeyName( ) );
                appRightDto.setReadable( attrRight.isReadable( ) );
                appRightDto.setWritable( attrRight.isWritable( ) );
                if ( attrRight.isCertifiable( ) )
                {
                    for ( AbstractCertifier certifier : listCertifier )
                    {
                        if ( certifier.getCertifiableAttributesList( ).contains( attrRight.getAttributeKey( ).getKeyName( ) ) )
                        {
                            appRightDto.addCertifier( certifier.getCode( ) );
                        }
                    }
                }
                appRightsDto.addAppRight( appRightDto );
            }
        }
        return appRightsDto;
    }

    /**
     * Get the application code to use
     * 
     * @param strHeaderClientAppCode
     *            The application code in HTTP request header
     * @param strParamAppCode
     *            The application code provided by the client
     * @return The application code to use
     */
    public static String getTrustedApplicationCode( String strHeaderClientAppCode, String strParamAppCode )
    {
        // Secure mode
        switch( getSecureMode( ) )
        {
            case JWT:
            {
                if ( !StringUtils.isEmpty( strHeaderClientAppCode ) )
                {
                    String strJwtClaimAppCode = AppPropertiesService.getProperty( IdentityConstants.PROPERTY_JWT_CLAIM_APP_CODE );
                    return JWTUtil.getPayloadValue( strHeaderClientAppCode, strJwtClaimAppCode );
                }
                break;
            }
            case NONE:
            {
                if ( !StringUtils.isEmpty( strHeaderClientAppCode ) )
                {
                    return strHeaderClientAppCode;
                }
                else
                {
                    if ( !StringUtils.isEmpty( strParamAppCode ) )
                    {
                        return strParamAppCode;
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get the application code to use
     * 
     * @param strHeaderClientAppCode
     *            The application code in HTTP request header
     * @param identityChange
     *            The identity change
     * @return The application code to use
     */
    public static String getTrustedApplicationCode( String strHeaderClientAppCode, IdentityChangeDto identityChange )
    {
        return getTrustedApplicationCode( strHeaderClientAppCode, identityChange.getAuthor( ).getApplicationCode( ) );
    }

    /**
     * Get the secure Mode of the identitystore
     * 
     * @return the secure mode
     */
    public static SecureMode getSecureMode( )
    {
        switch( AppPropertiesService.getProperty( IdentityConstants.PROPERTY_SECURE_MODE, StringUtils.EMPTY ) )
        {
            case "jwt":
                return SecureMode.JWT;
        }
        return SecureMode.NONE;

    }

    /**
     * Set an attribute value associated to an identity. Manage certification as defined in api documentation
     *
     * @param identity
     *            identity
     * @param requestAttribute
     *            The attribute to create or update
     * @param file
     *            file to upload, null if attribute type is not file
     * @param author
     *            The author of the change
     * @param strClientAppCode
     *            client application code
     * @param bAppCanDelete
     *            true if application can delete a non certified attribute
     * @return AttributStatusDto with statusCode according to the resulted operation
     */
    private static AttributeStatusDto setAttribute( Identity identity, IdentityAttribute requestAttribute, File file, ChangeAuthor author,
            String strClientAppCode, boolean bAppCanDelete )
    {
        String strCorrectValue = ( requestAttribute.getValue( ) == null ) ? StringUtils.EMPTY : requestAttribute.getValue( );

        boolean bCreate = false;
        boolean bValueUnchanged = false;

        IdentityAttribute dbAttribute = IdentityAttributeHome.findByPrimaryKey( identity.getId( ), requestAttribute.getAttributeKey( ).getId( ) );
        String strAttrOldValue = StringUtils.EMPTY;

        if ( dbAttribute == null )
        {
            dbAttribute = new IdentityAttribute( );
            dbAttribute.setAttributeKey( requestAttribute.getAttributeKey( ) );
            dbAttribute.setIdIdentity( identity.getId( ) );
            bCreate = true;
        }
        else
        {
            strAttrOldValue = dbAttribute.getValue( );

            if ( strCorrectValue.equals( strAttrOldValue ) && ( requestAttribute.getAttributeKey( ).getKeyType( ) != KeyType.FILE ) )
            {
                AppLogService.debug( "no change on attribute key=" + requestAttribute.getAttributeKey( ).getKeyName( ) + " value=" + strCorrectValue
                        + " for Id=" + identity.getId( ) );
                bValueUnchanged = true;
            }
        }

        AttributeCertificate attributeCertifPrev = dbAttribute.getCertificate( );

        AttributeStatusDto attrStatus;
        if ( attributeCertifPrev == null && requestAttribute.getCertificate( ) == null )
        {
            attrStatus = new AttributeStatusDto( );
            attrStatus.setStatusCode( AttributeStatusDto.OK_CODE );
            if ( bValueUnchanged )
            {
                attrStatus.setStatusCode( AttributeStatusDto.INFO_NO_CHANGE_REQUEST_CODE );
            }
            else
                if ( StringUtils.isEmpty( strCorrectValue ) && !bAppCanDelete )
                {
                    attrStatus.setStatusCode( AttributeStatusDto.INFO_DELETE_NOT_ALLOW_CODE );
                }
        }
        else
        {
            attrStatus = buildAttributeStatus( attributeCertifPrev, requestAttribute.getCertificate( ), bValueUnchanged );
        }

        if ( AttributeStatusDto.OK_CODE.equals( attrStatus.getStatusCode( ) ) )
        {
            dbAttribute.setLastUpdateApplicationCode( strClientAppCode );
            // create certificate
            if ( attrStatus.getNewCertifier( ) != null )
            {
                AttributeCertificateHome.create( requestAttribute.getCertificate( ) );
                dbAttribute.setIdCertificate( requestAttribute.getCertificate( ).getId( ) );
                dbAttribute.setCertificate( requestAttribute.getCertificate( ) );
            }

            // update or create attribute
            if ( !bValueUnchanged )
            {
                attrStatus.setNewValue( strCorrectValue );
                dbAttribute.setValue( strCorrectValue );
            }
            if ( bCreate )
            {
                IdentityAttributeHome.create( dbAttribute );
            }
            else
            {
                IdentityAttributeHome.update( dbAttribute );
            }

            if ( requestAttribute.getAttributeKey( ).getKeyType( ) == KeyType.FILE )
            {
                handleFile( dbAttribute, file );
            }

            AttributeChange change = IdentityStoreNotifyListenerService.buildAttributeChange( identity, requestAttribute.getAttributeKey( ).getKeyName( ),
                    strCorrectValue, strAttrOldValue, author, dbAttribute.getCertificate( ), bCreate );
            IdentityStoreNotifyListenerService.instance( ).notifyListenersAttributeChange( change );
        }
        else
            // no change request code in case of certificate non exists or is expired, request certificate doesn't exists and value unchanged
            if ( attributeCertifPrev != null )
            {
                dbAttribute.setCertificate( attributeCertifPrev );
                dbAttribute.setIdCertificate( attributeCertifPrev.getId( ) );
            }

        dbAttribute.setStatus( attrStatus );
        identity.getAttributes( ).put( requestAttribute.getAttributeKey( ).getKeyName( ), dbAttribute );

        return attrStatus;
    }

    /**
     * build Attribute status according to certificate in db and request
     * 
     * @param certificateDB
     *            The certificate from database, may be null
     * @param certificateRequest
     *            The certificate generated by certifier, may be null
     * @return AttributStatusDto with statusCode according to certificates
     */
    private static AttributeStatusDto buildAttributeStatus( AttributeCertificate certificateDB, AttributeCertificate certificateRequest, boolean valueUnchanged )
    {
        AttributeStatusDto attrStatus = new AttributeStatusDto( );
        attrStatus.setStatusCode( AttributeStatusDto.OK_CODE );

        if ( certificateDB == null )
        {
            attrStatus = handleNoCertificateDbOrExpired( certificateRequest, attrStatus, valueUnchanged );
        }
        else
        {
            if ( isCertificateExpired( certificateDB ) )
            {
                attrStatus = handleNoCertificateDbOrExpired( certificateRequest, attrStatus, valueUnchanged );
            }
            else
            {
                attrStatus = handleCertificateRequest( certificateDB, certificateRequest, attrStatus, valueUnchanged );
            }
        }
        return attrStatus;
    }

    /**
     * Handle certificates to update or return attribue status code in terms of certificates expiration dates
     * 
     * @param certificateDB
     * @param certificateRequest
     * @param attrStatus
     * @param valueUnchanged
     * @return attribuste status with setted code or certificate updated
     */
    private static AttributeStatusDto handleCertificatesExpirationDates( AttributeCertificate certificateDB, AttributeCertificate certificateRequest,
            AttributeStatusDto attrStatus, boolean valueUnchanged )
    {
        if ( certificateDB.getExpirationDate( ) == null && certificateRequest.getExpirationDate( ) == null )
        {
            updateAttributeStatusNewCertificateFromRequest( certificateRequest, attrStatus );
        }
        else
        {
            if ( certificateRequest.getExpirationDate( ) == null
                    || ( certificateDB.getExpirationDate( ) != null && certificateRequest.getExpirationDate( ).after( certificateDB.getExpirationDate( ) ) ) )
            {
                updateAttributeStatusNewCertificateFromRequest( certificateRequest, attrStatus );
            }
            else
            {
                // already certified with a certificate finish later
                attrStatus.setStatusCode( AttributeStatusDto.INFO_LONGER_CERTIFIER_CODE );
            }
        }
        return attrStatus;
    }

    /**
     * handle certificates's update or return attribue status code in terms of certificates levels
     * 
     * @param certificateDB
     * @param certificateRequest
     * @param attrStatus
     * @param valueUnchanged
     * @return attribute status updated or AS code updates
     */
    private static AttributeStatusDto handleCertificatesLevels( AttributeCertificate certificateDB, AttributeCertificate certificateRequest,
            AttributeStatusDto attrStatus, boolean valueUnchanged )
    {
        if ( isCertificateLevelRequestIsEqualOrHighterThanDb( certificateDB, certificateRequest ) )
        {
            if ( isCertificateLevelRequestIsEqualDb( certificateDB, certificateRequest ) )
            {
                attrStatus = handleCertificatesExpirationDates( certificateDB, certificateRequest, attrStatus, valueUnchanged );
            }
            else
            {
                updateAttributeStatusNewCertificateFromRequest( certificateRequest, attrStatus );
            }
        }
        else
        {
            attrStatus.setStatusCode( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE );
        }
        return attrStatus;
    }

    /**
     * Update the attribute status new certifier/certificate
     * 
     * @param certificateRequest
     * @param attrStatus
     */
    private static void updateAttributeStatusNewCertificateFromRequest( AttributeCertificate certificateRequest, AttributeStatusDto attrStatus )
    {
        attrStatus.setNewCertifier( certificateRequest.getCertifierCode( ) );
        attrStatus.setNewCertificateExpirationDate( certificateRequest.getExpirationDate( ) );
    }

    /**
     * handle certificates's update or return attribue status code in terms of certificate request
     * 
     * @param certificateDB
     * @param certificateRequest
     * @param attrStatus
     * @param valueUnchanged
     * @return attribute status updated or AS code updates
     */
    private static AttributeStatusDto handleCertificateRequest( AttributeCertificate certificateDB, AttributeCertificate certificateRequest,
            AttributeStatusDto attrStatus, boolean valueUnchanged )
    {
        if ( certificateRequest != null )
        {
            attrStatus = handleCertificatesLevels( certificateDB, certificateRequest, attrStatus, valueUnchanged );
        }
        else
        {
            attrStatus.setStatusCode( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE );
        }
        return attrStatus;
    }

    /**
     * handle certificates's update or return attribue status code in terms of certificateDB is expired or not
     * 
     * @param certificateDB
     * @param certificateRequest
     * @param attrStatus
     * @param valueUnchanged
     * @return attribute status updated or AS code updates
     */
    private static AttributeStatusDto handleNoCertificateDbOrExpired( AttributeCertificate certificateRequest, AttributeStatusDto attrStatus,
            boolean valueUnchanged )
    {
        if ( certificateRequest != null )
        {
            updateAttributeStatusNewCertificateFromRequest( certificateRequest, attrStatus );
        }
        else
        {
            // If unchanged value NO change request code
            if ( valueUnchanged )
            {
                attrStatus.setStatusCode( AttributeStatusDto.INFO_NO_CHANGE_REQUEST_CODE );
            }
            // Else go 200 : OK update value from request
        }
        return attrStatus;
    }

    /**
     * Is the the certificateLevel from request equals Db ?
     * 
     * @param certificateDB
     * @param certificateRequest
     * @return true if the certificateLevel from request is equal Db
     */
    private static boolean isCertificateLevelRequestIsEqualDb( AttributeCertificate certificateDB, AttributeCertificate certificateRequest )
    {
        return certificateRequest.getCertificateLevel( ) == certificateDB.getCertificateLevel( );
    }

    /**
     * Is the the certificateLevel from request is highter or equal than Db ?
     * 
     * @param certificateDB
     * @param certificateRequest
     * @return true if the certificateLevel from request is highter or equal than Db
     */
    private static boolean isCertificateLevelRequestIsEqualOrHighterThanDb( AttributeCertificate certificateDB, AttributeCertificate certificateRequest )
    {
        return certificateRequest.getCertificateLevel( ) >= certificateDB.getCertificateLevel( );
    }

    /**
     * Return true if the certificate has an expirationDate still valid else return false
     * 
     * @param certificateDB
     * @return true if the certificate is not expired and not null
     */
    private static boolean isCertificateExpired( AttributeCertificate certificateDB )
    {
        return certificateDB.getExpirationDate( ) != null && certificateDB.getExpirationDate( ).before( new Date( ) );
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
     * Finds the client application with the specified code
     *
     * @param strClientApplicationCode
     *            the client application code
     * @return the client application
     * @throws AppException
     *             if the client application cannot be found
     */
    public static ClientApplication fetchClientApplication( String strClientApplicationCode ) throws AppException
    {
        ClientApplication clientApplication = ClientApplicationHome.findByCode( strClientApplicationCode );

        if ( clientApplication == null )
        {
            throw new AppException( "The client application " + strClientApplicationCode + " is unknown " );
        }

        return clientApplication;
    }
    
	/**
	 * Method call for adding automatically attributes associated to the certifier
	 * used
	 * 
	 * @param identityDto the identity dto informations
	 */
	private static void addAutomaticalyCertificationAttributes(IdentityDto identityDto) {

		if (identityDto.getAttributes() != null) {
			Optional<AttributeDto> attributeDTO = identityDto.getAttributes().entrySet().stream()
					.filter(x -> x.getValue().getCertificate() != null
							&& x.getValue().getCertificate().getCertifierCode() != null)
					.map(Map.Entry::getValue).findFirst();
			//find the first attribute which contains a certifier
			if (attributeDTO.isPresent()) {
				try {

					AbstractCertifier certifier = CertifierRegistry.instance()
							.getCertifier(attributeDTO.get().getCertificate().getCertifierCode());
					//Test if the certifier contains attributes who must be generated automatically
					if (certifier.getGenerateAutomaticCertifierAttribute() != null
							&& certifier.getGenerateAutomaticCertifierAttribute().size() > 0) {

						certifier.getGenerateAutomaticCertifierAttribute().forEach((k, v) -> {
							//test if the identity DTO contains all informations necessary for adding  attribute
							if (v.mustBeGenerated(identityDto, certifier.getCode())) {
								identityDto.getAttributes().put(k,
										getAutomaticCertificateAttribute(identityDto, certifier.getCode(), k, v));
							}
						});

					}

				} catch (CertifierNotFoundException ex) {
					AppLogService.error("Error getting  certifier"
							+ attributeDTO.get().getCertificate().getCertifierCode() + ex.getMessage(), ex);
				}

			}
		}

	}
    
	/**
	 * return the attribute who must be add automatically to the identity DTO informations 
	 * @param identityDO the identity dto informations
	 * @param certifierCode the certifier code 
	 * @param strAttributeKey the attribute code of the generated attribute
	 * @param generateAutomaticCertifierAttribute implementation of IGenerateAutomaticCertifierAttribute for getting the attribute value
	 * @return the attribute who must be add automatically to the identity DTO informations 
	 */
    private static AttributeDto getAutomaticCertificateAttribute(IdentityDto identityDto,String certifierCode,String strAttributeKey,IGenerateAutomaticCertifierAttribute generateAutomaticCertifierAttribute)
    {
    	
    	AttributeDto automaticAttribute = new AttributeDto();
    	automaticAttribute.setKey(strAttributeKey);
    	automaticAttribute.setValue(generateAutomaticCertifierAttribute.getValue(identityDto));
    	automaticAttribute.setCertified(true);
		CertificateDto certificateDto = new CertificateDto();
	    certificateDto.setCertifierCode(certifierCode);
	    automaticAttribute.setCertificate(certificateDto);
		
		return automaticAttribute;
    	
    }
    
}
