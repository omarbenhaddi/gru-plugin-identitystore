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
package fr.paris.lutece.plugins.identitystore.service.attribute;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeValidationCache;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.regex.Pattern;

/**
 * Service class used to validate attribute values in requests
 */
public class IdentityAttributeValidationService
{

    private final IdentityAttributeValidationCache _cache = SpringContextService.getBean( "identitystore.identityAttributeValidationCache" );
    private static IdentityAttributeValidationService _instance;

    public static IdentityAttributeValidationService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityAttributeValidationService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    /**
     * @see IdentityAttributeValidationService#validateIdentityAttributeValues(IdentityDto, ChangeResponse)
     */
    public void validateMergeRequestAttributeValues( final IdentityMergeRequest request, final IdentityMergeResponse response )
            throws IdentityAttributeNotFoundException
    {
        final boolean passedValidation = this.validateIdentityAttributeValues( request.getIdentity( ), response );
        if ( !passedValidation )
        {
            response.setStatus( ResponseStatusType.FAILURE );
            response.setMessage( "Some attribute values are not passing validation. Please check in the attribute statuses for details." );
            response.setI18nMessageKey( Constants.PROPERTY_REST_ERROR_FAIL_ATTRIBUTE_VALIDATION );
        }
    }

    /**
     * @see IdentityAttributeValidationService#validateIdentityAttributeValues(IdentityDto, ChangeResponse)
     */
    public void validateChangeRequestAttributeValues( final IdentityChangeRequest request, final IdentityChangeResponse response )
            throws IdentityAttributeNotFoundException
    {
        final boolean passedValidation = this.validateIdentityAttributeValues( request.getIdentity( ), response );
        if ( !passedValidation )
        {
            response.setStatus( ResponseStatusType.FAILURE );
            response.setMessage( "Some attribute values are not passing validation. Please check in the attribute statuses for details." );
            response.setI18nMessageKey( Constants.PROPERTY_REST_ERROR_FAIL_ATTRIBUTE_VALIDATION );
        }
    }

    /**
     * Validates all attribute values stored in the provided identity, according to each attribute validation regex. Adds validation error statuses in the
     * response in case of invalid values.
     * 
     * @param identity
     *            the identity
     * @param response
     *            the response
     * @return true if all values are valid, false otherwise.
     */
    private boolean validateIdentityAttributeValues( final IdentityDto identity, final ChangeResponse response ) throws IdentityAttributeNotFoundException
    {
        boolean passedValidation = true;
        if ( identity != null )
        {
            for ( final AttributeDto attribute : identity.getAttributes( ) )
            {
                final Pattern validationPattern = _cache.get( attribute.getKey( ) );
                if ( validationPattern != null )
                {
                    if ( !validationPattern.matcher( attribute.getValue( ) ).matches( ) )
                    {
                        passedValidation = false;
                        response.getAttributeStatuses( ).add( this.buildAttributeValidationErrorStatus( attribute.getKey( ) ) );
                    }
                }
            }
        }
        return passedValidation;
    }

    /**
     * Builds an attribute status for invalid value.
     * 
     * @param attrStrKey
     *            the attribute key
     * @return the status
     */
    private AttributeStatus buildAttributeValidationErrorStatus( final String attrStrKey ) throws IdentityAttributeNotFoundException
    {
        final AttributeKey attributeKey = IdentityAttributeService.instance( ).getAttributeKey( attrStrKey );
        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attrStrKey );
        attributeStatus.setStatus( AttributeChangeStatus.INVALID_VALUE );
        attributeStatus.setMessage( attributeKey.getValidationErrorMessage( ) );

        return attributeStatus;
    }

}
