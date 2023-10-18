/*
 * Copyright (c) 2002-2023, City of Paris
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

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityConstants;
import fr.paris.lutece.plugins.identitystore.business.security.SecureMode;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.jwt.service.JWTUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * IdentityStoreService
 */
public final class IdentityStoreService
{
    /**
     * private constructor
     */
    private IdentityStoreService( )
    {
    }

    /**
     * Get the application code to use.<br/>
     *
     * @param strHeaderClientCode
     *            The application code in HTTP request header
     * @param strParamClientCode
     *            The application code provided by the client
     * @return The application code to use
     */
    public static String getTrustedClientCode( final String strHeaderClientCode, final String strParamClientCode ) throws IdentityStoreException
    {
        return getTrustedClientCode( strHeaderClientCode, strParamClientCode, StringUtils.EMPTY );
    }

    /**
     * Get the application code to use.<br/>
     * If the <code>strHeaderAppCode</code> is provided, the correlation between the resulting trusted client code and this application code is verified.
     *
     * @param strHeaderClientCode
     *            The application code in HTTP request header
     * @param strParamClientCode
     *            The application code provided by the client
     * @param strHeaderAppCode
     *            The application code header provided by the API manager
     * @return The application code to use
     * @see IdentityStoreService#verifyClientAndAppCodeCorrelation
     * @throws IdentityStoreException
     *             if the correlation between the resulting trusted client code and the application code was not verified
     */
    public static String getTrustedClientCode( final String strHeaderClientCode, final String strParamClientCode, final String strHeaderAppCode )
            throws IdentityStoreException
    {
        String trustedClientCode = StringUtils.EMPTY;
        // Secure mode
        switch( getSecureMode( ) )
        {
            case JWT:
            {
                if ( StringUtils.isNotBlank( strHeaderClientCode ) )
                {
                    String strJwtClaimAppCode = AppPropertiesService.getProperty( IdentityConstants.PROPERTY_JWT_CLAIM_APP_CODE );
                    trustedClientCode = JWTUtil.getPayloadValue( strHeaderClientCode.trim( ), strJwtClaimAppCode );
                }
                break;
            }
            case NONE:
            {
                if ( StringUtils.isNotBlank( strHeaderClientCode ) )
                {
                    trustedClientCode = strHeaderClientCode.trim( );
                }
                else
                {
                    if ( StringUtils.isNotBlank( strParamClientCode ) )
                    {
                        trustedClientCode = strParamClientCode.trim( );
                    }
                }
            }
        }
        verifyClientAndAppCodeCorrelation( trustedClientCode, strHeaderAppCode );
        return trustedClientCode;
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
     * Verify if the trusted client code is part of the provided client application.<br/>
     * If the application code is not provided, the verification is skipped.
     * 
     * @param strTrustedClientCode
     *            the trusted client code
     * @param strHeaderAppCode
     *            the app code
     * @throws IdentityStoreException
     *             if the validation is not passing.
     */
    private static void verifyClientAndAppCodeCorrelation( final String strTrustedClientCode, final String strHeaderAppCode ) throws IdentityStoreException
    {
        if ( StringUtils.isBlank( strHeaderAppCode ) )
        {
            return;
        }
        final List<ClientApplication> clientApplicationList = ClientApplicationHome.findByApplicationCode( strHeaderAppCode );
        if ( clientApplicationList.stream( ).map( ClientApplication::getClientCode ).noneMatch( clientCode -> clientCode.equals( strTrustedClientCode ) ) )
        {
            throw new IdentityStoreException( "The provided client code and application code are not correlating." );
        }
    }
}
