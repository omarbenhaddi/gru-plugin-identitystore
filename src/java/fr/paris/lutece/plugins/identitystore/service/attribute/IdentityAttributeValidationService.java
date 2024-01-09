/*
 * Copyright (c) 2002-2024, City of Paris
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

import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.service.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeValidationCache;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class used to validate attribute values in requests
 */
public class IdentityAttributeValidationService
{

    private final IdentityAttributeValidationCache _cache = SpringContextService.getBean( "identitystore.identityAttributeValidationCache" );
    private final String pivotCertificationProcessCodeThreshold = AppPropertiesService
            .getProperty( "identitystore.identity.attribute.pivot.certification.process.threshold" );
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
     * Validates all attribute values stored in the provided identity, according to each attribute validation regex. Adds validation error statuses in the
     * response in case of invalid values, and put the status to FAILURE.
     * 
     * @param identity
     *            the identity
     * @param response
     *            the response
     */
    public void validateIdentityAttributeValues( final IdentityDto identity, final ChangeResponse response ) throws IdentityAttributeNotFoundException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        if ( identity != null )
        {
            for ( final AttributeDto attribute : identity.getAttributes( ) )
            {
                if ( StringUtils.isNotBlank( attribute.getValue( ) ) )
                {
                    final Pattern validationPattern = _cache.get( attribute.getKey( ) );
                    if ( validationPattern != null )
                    {
                        if ( !validationPattern.matcher( attribute.getValue( ) ).matches( ) )
                        {
                            attrStatusList.add( this.buildAttributeValueValidationErrorStatus( attribute.getKey( ) ) );
                        }
                    }
                }
            }
        }
        if ( !attrStatusList.isEmpty( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setAttributeStatuses( attrStatusList )
                    .setMessage( "Some attribute values are not passing validation. Please check in the attribute statuses for details." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_FAIL_ATTRIBUTE_VALIDATION ) );
        }
    }

    /**
     * Builds an attribute status for invalid value.
     * 
     * @param attrStrKey
     *            the attribute key
     * @return the status
     */
    private AttributeStatus buildAttributeValueValidationErrorStatus( final String attrStrKey ) throws IdentityAttributeNotFoundException
    {
        final AttributeKey attributeKey = IdentityAttributeService.instance( ).getAttributeKey( attrStrKey );
        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attrStrKey );
        attributeStatus.setStatus( AttributeChangeStatus.INVALID_VALUE );
        attributeStatus.setMessage( attributeKey.getValidationErrorMessage( ) );
        attributeStatus.setMessageKey( attributeKey.getValidationErrorMessageKey( ) );

        return attributeStatus;
    }

    /**
     * Validates the integrity of pivot attributes stored in the provided identity.<br/>
     * If a pivot attribute is present and certified with a process with a greater or equal level to the defined process in configuration, then ALL the pivot
     * attributes need to be present and certified with the same certification process.<br/>
     * If the birthcountry code attribute is set and doesn't correspond to FRANCE, then the birthplace code attribute is allowed to be missing.<br/>
     * If the above rules are not fullfiled, the response status is put to FAILURE.
     *
     * @param cuid
     *            the cuid
     * @param identityRequest
     *            the identity request
     * @param response
     *            the response
     */
    public void validatePivotAttributesIntegrity( final String cuid, final IdentityDto identityRequest, final ChangeResponse response )
            throws IdentityStoreException
    {
        final List<String> pivotKeys = IdentityAttributeService.instance( ).getPivotAttributeKeys( ).stream( ).map( AttributeKey::getKeyName )
                .collect( Collectors.toList( ) );
        final List<AttributeDto> pivotAttrs = identityRequest.getAttributes( ).stream( ).filter( a -> pivotKeys.contains( a.getKey( ) ) )
                .collect( Collectors.toList( ) );
        pivotAttrs.forEach( a -> {
            if ( a.getCertificationLevel( ) == null && StringUtils.isNotBlank( a.getCertifier( ) ) )
            {
                a.setCertificationLevel( AttributeCertificationDefinitionService.instance( ).getLevelAsInteger( a.getCertifier( ), a.getKey( ) ) );
            }
        } );
        if ( StringUtils.isNotBlank( cuid ) )
        {
            final IdentityDto existingIdentityDto = DtoConverter.convertIdentityToDto( IdentityHome.findByCustomerId( cuid ) );
            final List<AttributeDto> existingPivotAttrs = existingIdentityDto.getAttributes( ).stream( ).filter( a -> pivotKeys.contains( a.getKey( ) ) )
                    .collect( Collectors.toList( ) );
            for ( final AttributeDto existingPivotAttr : existingPivotAttrs )
            {
                if ( !pivotAttrs.contains( existingPivotAttr ) )
                {
                    pivotAttrs.add( existingPivotAttr );
                }
            }
        }

        final AttributeDto highestCertifiedPivot = pivotAttrs.stream( ).max( Comparator.comparing( AttributeDto::getCertificationLevel ) ).orElse( null );
        if ( highestCertifiedPivot != null )
        {
            final Integer thresholdLevel = AttributeCertificationDefinitionService.instance( ).getLevelAsInteger( pivotCertificationProcessCodeThreshold,
                    highestCertifiedPivot.getKey( ) );
            if ( thresholdLevel != null && highestCertifiedPivot.getCertificationLevel( ) >= thresholdLevel )
            {
                if ( pivotAttrs.stream( ).anyMatch( a -> !a.getCertifier( ).equals( highestCertifiedPivot.getCertifier( ) ) ) )
                {
                    response.setStatus( ResponseStatusFactory.failure( )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION ).setMessage(
                                    "All pivot attributes must be set and certified with the '" + highestCertifiedPivot.getCertifier( ) + "' certifier" ) );
                }
                else
                    if ( pivotKeys.size( ) != pivotAttrs.size( ) )
                    {
                        final AttributeDto countryCodeAttr = pivotAttrs.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
                                .findFirst( ).orElse( null );
                        // Pays de naissance étranger, on accepte que le code commune de naissance ne soit pas renseigné
                        final String franceCode = GeoCodesService.getInstance( ).getCountriesListByName( "FRANCE" ).stream( )
                                .filter( c -> c.getValue( ).equalsIgnoreCase( "FRANCE" ) ).map( Country::getCode ).findFirst( ).orElse( null );
                        final boolean acceptNoCityCode = countryCodeAttr != null && !countryCodeAttr.getValue( ).equals( franceCode );
                        for ( final String pivotKey : pivotKeys )
                        {
                            if ( acceptNoCityCode && pivotKey.equals( Constants.PARAM_BIRTH_PLACE_CODE ) )
                            {
                                continue;
                            }
                            final AttributeDto pivotAttr = pivotAttrs.stream( ).filter( a -> a.getKey( ).equals( pivotKey ) ).findFirst( ).orElse( null );
                            if ( pivotAttr == null )
                            {
                                response.setStatus( ResponseStatusFactory.failure( )
                                        .setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION )
                                        .setMessage( "All pivot attributes must be set and certified with the '" + highestCertifiedPivot.getCertifier( )
                                                + "' certifier" ) );
                                break;
                            }
                        }
                    }
            }
        }
    }

}
