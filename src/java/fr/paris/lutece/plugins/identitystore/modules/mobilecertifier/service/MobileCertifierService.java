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


package fr.paris.lutece.plugins.identitystore.modules.mobilecertifier.service;

import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifierHome;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.RandomStringUtils;

/**
 * Mobile Certifier Service
 */
public class MobileCertifierService 
{
    
    private static final String MESSAGE_CODE_VALIDATION_OK = "identitystore.mobilecertifier.message.validation.ok";
    private static final String MESSAGE_CODE_VALIDATION_INVALID = "identitystore.mobilecertifier.message.validation.invalidCode";
    private static final String MESSAGE_SESSION_EXPIRED = "identitystore.mobilecertifier.message.validation.sessionExpired";
    private static final String MESSAGE_CODE_EXPIRED = "identitystore.mobilecertifier.message.validation.codeExpired";
    private static final String MESSAGE_TOO_MANY_ATTEMPS = "identitystore.mobilecertifier.message.validation.tooManyAttempts";
    
    private static final String PROPERTY_CODE_LENGTH = "identitystore.mobilecertifier.codeLength";
    private static final String PROPERTY_EXPIRES_DELAY = "identitystore.mobilecertifier.expiresDelay";
    private static final String PROPERTY_MAX_ATTEMPTS = "identitystore.mobilecertifier.maxAttempts";
    private static final String PROPERTY_MOKED_CONNECTION_ID = "identitystore.mobilecertifier.mokedConnectionId";
    private static final String PROPERTY_ATTRIBUTE = "identitystore.mobilecertifier.attribute";

    private static final String DEFAULT_ATTRIBUTE_NAME = "mobile_phone";
    private static final String DEFAULT_CONNECTION_ID = "1";
    private static final int DEFAULT_LENGTH = 6;
    private static final int DEFAULT_EXPIRES_DELAY = 5;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    
    private static final String ATTRIBUTE_NAME = AppPropertiesService.getProperty( PROPERTY_ATTRIBUTE , DEFAULT_ATTRIBUTE_NAME );
    private static final String MOKED_USER_CONNECTION_ID = AppPropertiesService.getProperty( PROPERTY_MOKED_CONNECTION_ID , DEFAULT_CONNECTION_ID );
    private static final int EXPIRES_DELAY =  AppPropertiesService.getPropertyInt( PROPERTY_EXPIRES_DELAY , DEFAULT_EXPIRES_DELAY );
    private static final int CODE_LENGTH = AppPropertiesService.getPropertyInt( PROPERTY_CODE_LENGTH , DEFAULT_LENGTH );
    private static final int MAX_ATTEMPTS = AppPropertiesService.getPropertyInt( PROPERTY_MAX_ATTEMPTS , DEFAULT_MAX_ATTEMPTS );

    private static Map <String, ValidationInfos>_mapValidationCodes = new HashMap<String, ValidationInfos>();
    
    /**
     * Starts the validation process by generating and sending a validation code
     * @param request The HTTP request
     * @param strMobileNumber The mobile phone number
     * @throws fr.paris.lutece.portal.service.security.UserNotSignedException if no user found
     */
    public static void startValidation( HttpServletRequest request, String strMobileNumber ) throws UserNotSignedException
    {
        String strValidationCode = generateValidationCode();
        System.out.println( "\nVALIDATION CODE : " + strValidationCode );
        HttpSession session = request.getSession( true );
        ValidationInfos infos = new ValidationInfos();
        infos.setValidationCode( strValidationCode );
        infos.setExpiresTime( getExpiresTime() );
        infos.setMobileNumber( strMobileNumber );
        infos.setUserConnectionId( getUserConnectionId( request ) );
        
        _mapValidationCodes.put( session.getId() , infos );
    }
    
    /**
     * Validate a validation code
     * @param request The request
     * @param strValidationCode The validation code
     * @return A validation result
     */
    public static ValidationResult validate( HttpServletRequest request, String strValidationCode )
    {
        HttpSession session = request.getSession();
        if( session == null )
        {
            return ValidationResult.SESSION_EXPIRED;
        }
        String strKey = session.getId();
        ValidationInfos infos = _mapValidationCodes.get( strKey );
        if( infos == null )
        {
            return ValidationResult.SESSION_EXPIRED;
        }
        
        if( infos.getInvalidAttempts() > MAX_ATTEMPTS )
        {
            return ValidationResult.TOO_MANY_ATTEMPS;
        }
        if( strValidationCode == null || ! strValidationCode.equals( infos.getValidationCode() ))
        {
            infos.setInvalidAttempts( infos.getInvalidAttempts() + 1 );
            return ValidationResult.INVALID_CODE;
        }
        if( infos.getExpiresTime() < now() )
        {
            _mapValidationCodes.remove(  strKey );
            return ValidationResult.CODE_EXPIRED;
        }
        _mapValidationCodes.remove(  strKey );
        certify( infos );
        return ValidationResult.OK;
    }
    
    private static void certify( ValidationInfos infos )
    {
        AttributeCertifier certifier = AttributeCertifierHome.findByPrimaryKey( 2 ); // FIXME
        IdentityStoreService.setAttribute( infos.getUserConnectionId() , ATTRIBUTE_NAME , infos.getMobileNumber() , certifier );
    }
    
    /**
     * Enumeration of all validation results
     */
    public enum ValidationResult 
    { 
        OK( MESSAGE_CODE_VALIDATION_OK ), 
        INVALID_CODE ( MESSAGE_CODE_VALIDATION_INVALID), 
        SESSION_EXPIRED ( MESSAGE_SESSION_EXPIRED ), 
        CODE_EXPIRED ( MESSAGE_CODE_EXPIRED ), 
        TOO_MANY_ATTEMPS ( MESSAGE_TOO_MANY_ATTEMPS ) ;
        
        private String _strMessageKey;
        
        /** 
         * Constructor
         * @param strMessageKey The i18n message key 
         */
        ValidationResult( String strMessageKey )
        {
            _strMessageKey = strMessageKey;
        }
        
        /**
         * Return the i18n message key
         * @return the i18n message key 
         */
        public String getMessageKey()
        {
            return _strMessageKey;
        }
    
    
    };

    private static String getUserConnectionId( HttpServletRequest request ) throws UserNotSignedException
    {
        if( SecurityService.isAuthenticationEnable() )
        {
            LuteceUser user = SecurityService.getInstance().getRegisteredUser( request );
            if( user != null )
            {
                return user.getName();
            }
            else
            {
                throw new UserNotSignedException();
            }
        }
        else
        {
            return MOKED_USER_CONNECTION_ID;
        }
        
    }
    /**
     * Generate a random alphanumeric code
     * @return The code
     */
    private static String generateValidationCode()
    {
        return RandomStringUtils.randomAlphanumeric( CODE_LENGTH ).toUpperCase();
    }

    /**
     * Calculate an expiration time 
     * @return the time as a long value
     */
    private static long getExpiresTime()
    {
        return now() + (long) EXPIRES_DELAY * 60000L;
    }
    
    /**
     * The current time as a long value
     * @return current time as a long value
     */
    private static long now()
    {
        return ( new Date()).getTime();
    }
    
}
