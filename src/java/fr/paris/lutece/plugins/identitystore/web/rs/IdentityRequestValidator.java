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
package fr.paris.lutece.plugins.identitystore.web.rs;

import fr.paris.lutece.plugins.identitystore.business.Attribute;
import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.KeyType;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.service.Constants;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;


/**
 *
 * util class used to validate identity store request
 *
 */
public final class IdentityRequestValidator
{
    private static final String BEAN_IDENTITYSERVICE_PROVIDER = "identitystore.authenticationKeyValidator";

    /**
     * singleton
     */
    private static IdentityRequestValidator _singleton;
    private IAuthenticationKeyValidator _keyValidator;

    /**
     * private constructor
     */
    private IdentityRequestValidator(  )
    {
        _keyValidator = SpringContextService.getBean( BEAN_IDENTITYSERVICE_PROVIDER );
    }

    /**
     * return singleton's instance
     * @return IdentityRequestValidator
     */
    public static IdentityRequestValidator instance(  )
    {
        if ( _singleton == null )
        {
            try
            {
                _singleton = new IdentityRequestValidator(  );
            }
            catch ( Exception e )
            {
                AppLogService.error( "Error when instantiating IdentityRequestValidator instance" + e.getMessage(  ), e );
            }
        }

        return _singleton;
    }

    /**
     * check that client application code exists in identitystore
     *
     * @param strClientCode
     *          client application code
     * @param strAuthenticationKey
     *          client application hash
     * @throws AppException
     *           thrown if null
     */
    private void checkClientApplication( String strClientCode, String strAuthenticationKey )
        throws AppException
    {
        ClientApplication clientApp = ClientApplicationHome.findByCode( strClientCode );

        if ( clientApp == null )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " : " + strClientCode + " is unknown " );
        }

        if ( !_keyValidator.isAuthenticationKeyValid( strClientCode, strAuthenticationKey ) )
        {
            throw new AppException( "Authentication error" );
        }
    }

    /**
     * check input parameters
     *
     * @param strConnectionId
     *          connection id of identity to update
     * @param nCustomerId
     *          customerId
     * @param strClientAppCode
     *          client application code asking for modif
     * @param strAuthenticationKey
     *          client application hash
     * @throws AppException
     *           if request is not correct
     */
    public void checkInputParams( String strConnectionId, int nCustomerId, String strClientAppCode,
        String strAuthenticationKey ) throws AppException
    {
        if ( StringUtils.isBlank( strConnectionId ) && ( nCustomerId != 0 ) )
        {
            throw new AppException( Constants.PARAM_ID_CONNECTION + " AND " + Constants.PARAM_ID_CUSTOMER +
                " are missing, at least one must be provided" );
        }

        if ( StringUtils.isBlank( strClientAppCode ) )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " is missing" );
        }

        checkClientApplication( strClientAppCode, strAuthenticationKey );
    }

    /**
     * check input parameters
     *
     * @param identityChange identity change
     * @param strAuthenticationKey
     *          client application hash
     * @throws AppException
     *           if request is not correct
     */
    public void checkInputParams( IdentityChangeDto identityChange, String strAuthenticationKey )
        throws AppException
    {
        if ( ( identityChange == null ) || ( identityChange.getIdentity(  ) == null ) )
        {
            throw new AppException( "Provided IdentityChange / Identity is null" );
        }

        if ( identityChange.getAuthor(  ) == null )
        {
            throw new AppException( "Provided Author is null" );
        }

        checkInputParams( identityChange.getIdentity(  ).getConnectionId(  ),
            identityChange.getIdentity(  ).getCustomerId(  ), identityChange.getAuthor(  ).getApplicationCode(  ),
            strAuthenticationKey );
    }

    /**
     * @param strConnectionId
     *          connectionId (must not be empty)
     * @param strClientCode
     *          client application code (must not be empty)
     * @param strAttributeKey
     *          attribute key containing file (must not be empty)
     * @param strAuthenticationKey
     *          client application hash
     * @throws AppException
     *           thrown if input parameters are invalid or no file is found
     */
    public void checkDownloadFileAttributeParams( String strConnectionId, String strClientCode, String strAttributeKey,
        String strAuthenticationKey ) throws AppException
    {
        checkClientApplication( strClientCode, strAuthenticationKey );

        if ( StringUtils.isBlank( strConnectionId ) )
        {
            throw new AppException( Constants.PARAM_ID_CONNECTION + " is null or empty" );
        }

        if ( StringUtils.isBlank( strClientCode ) )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " is null or empty" );
        }

        if ( StringUtils.isBlank( strAttributeKey ) )
        {
            throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " is null or empty" );
        }
    }

    /**
     * check attached files are present in identity Dto and that attributes to
     * update exist and are writable (or not writable AND unchanged)
     *
     * @param identityDto
     *          identityDto with list of attributes
     * @param strClientAppCode
     *          application code to check right
     * @param mapAttachedFiles
     *          map of attached files
     * @throws AppException
     *           thrown if provided attributes are not valid
     */
    public void checkAttributes( IdentityDto identityDto, String strClientAppCode, Map<String, File> mapAttachedFiles )
        throws AppException
    {
        if ( ( mapAttachedFiles != null ) && !mapAttachedFiles.isEmpty(  ) )
        {
            // check if input files is present in identity DTO attributes
            for ( Map.Entry<String, File> entry : mapAttachedFiles.entrySet(  ) )
            {
                boolean bFound = false;

                for ( AttributeDto attributeDto : identityDto.getAttributes(  ).values(  ) )
                {
                    AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey(  ) );

                    if ( attributeKey == null )
                    {
                        throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeDto.getKey(  ) +
                            " is provided but does not exist" );
                    }

                    // check that attribute is file type and that its name is matching
                    if ( attributeKey.getKeyType(  ).equals( KeyType.FILE ) &&
                            StringUtils.isNotBlank( attributeDto.getValue(  ) ) &&
                            attributeDto.getValue(  ).equals( entry.getKey(  ) ) )
                    {
                        bFound = true;

                        break;
                    }
                }

                if ( !bFound )
                {
                    throw new AppException( Constants.PARAM_FILE + " " + entry.getKey(  ) +
                        " is provided but its attribute is missing" );
                }
            }
        }

        if ( identityDto.getAttributes(  ) != null )
        {
            List<AttributeRight> lstRights = ClientApplicationHome.selectApplicationRights( ClientApplicationHome.findByCode( 
                        strClientAppCode ) );

            // check that all file attribute type provided with filename in dto have
            // matching attachements
            for ( AttributeDto attributeDto : identityDto.getAttributes(  ).values(  ) )
            {
                AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey(  ) );

                if ( attributeKey == null )
                {
                    throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeDto.getKey(  ) +
                        " is provided but does not exist" );
                }

                for ( AttributeRight attRight : lstRights )
                {
                    Attribute attribute = IdentityStoreService.getAttribute( identityDto.getConnectionId(  ),
                            attRight.getAttributeKey(  ).getKeyName(  ), strClientAppCode );

                    if ( attRight.getAttributeKey(  ).getId(  ) == attributeKey.getId(  ) )
                    {
                        // if provided attribute is writable, or if no change => ok
                        if ( attRight.isWritable(  ) || attributeDto.getValue(  ).equals( attribute.getValue(  ) ) )
                        {
                            break;
                        }
                        else
                        {
                            throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeKey.getKeyName(  ) +
                                " is provided but is not writable" );
                        }
                    }
                }

                if ( attributeKey.getKeyType(  ).equals( KeyType.FILE ) &&
                        StringUtils.isNotBlank( attributeDto.getValue(  ) ) &&
                        ( ( mapAttachedFiles == null ) || ( mapAttachedFiles.get( attributeDto.getValue(  ) ) == null ) ) )
                {
                    throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeKey.getKeyName(  ) +
                        " is provided with filename=" + attributeDto.getValue(  ) + " but no file is attached" );
                }
            }
        }
    }
}
