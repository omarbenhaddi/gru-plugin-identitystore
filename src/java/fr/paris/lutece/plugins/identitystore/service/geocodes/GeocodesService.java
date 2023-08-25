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
package fr.paris.lutece.plugins.identitystore.service.geocodes;

import fr.paris.lutece.plugins.geocodes.business.City;
import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.service.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class GeocodesService
{

    private final static IdentityAttributeService _identityAttributeService = IdentityAttributeService.instance( );

    /**
     * Private methode used to process both "birthcountry_code" and "birthcountry" attributes during an update process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param attrToUpdate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    public static void processCountryForUpdate( final Identity identity, final List<AttributeDto> attrToCreate, final List<AttributeDto> attrToUpdate,
            final String clientCode, final ChangeResponse response ) throws IdentityStoreException
    {
        final AttributeDto countryCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToCreate != null )
        {
            attrToCreate.remove( countryCodeToCreate );
        }
        AttributeDto countryLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ).findFirst( )
                .orElse( null );
        if ( countryLabelToCreate != null )
        {
            attrToCreate.remove( countryLabelToCreate );
        }
        final AttributeDto countryCodeToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToUpdate != null )
        {
            attrToUpdate.remove( countryCodeToUpdate );
        }
        AttributeDto countryLabelToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ).findFirst( )
                .orElse( null );
        if ( countryLabelToUpdate != null )
        {
            attrToUpdate.remove( countryLabelToUpdate );
        }

        // Country code to CREATE
        if ( countryCodeToCreate != null )
        {
            final Country country = GeoCodesService.getCountryByCode( countryCodeToCreate.getValue( ) ).orElse( null );
            if ( country == null )
            {
                // Country doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( countryCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // Country exists in Geocodes for provided code
                // create country code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( countryCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create country label attribute if it doesn't already exist in the identity
                // Geocodes label value is used, regardless if a label is provided or not
                final String countryGeocodesLabel = country.getValue( );
                if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY ) )
                {
                    final AttributeChangeStatus labelStatus = ( countryLabelToCreate == null
                            || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) ) ? AttributeChangeStatus.CREATED
                                    : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                    if ( countryLabelToCreate == null )
                    {
                        countryLabelToCreate = new AttributeDto( );
                        countryLabelToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                    }
                    countryLabelToCreate.setValue( countryGeocodesLabel );
                    countryLabelToCreate.setCertifier( countryCodeToCreate.getCertifier( ) );
                    countryLabelToCreate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                    final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                    attributeStatus.setStatus( labelStatus );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                // update country label if attribute exists, and value is different from existing
                else
                    if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY ).getValue( ).equals( countryGeocodesLabel ) )
                    {
                        final boolean override = ( countryLabelToUpdate != null && !countryLabelToUpdate.getValue( ).equals( countryGeocodesLabel ) );
                        if ( countryLabelToUpdate == null )
                        {
                            countryLabelToUpdate = new AttributeDto( );
                            countryLabelToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                        }
                        countryLabelToUpdate.setValue( countryGeocodesLabel );
                        countryLabelToUpdate.setCertifier( countryCodeToCreate.getCertifier( ) );
                        countryLabelToUpdate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                        if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                        {
                            attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                        }
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
            }
        }
        // Country code to UPDATE
        else
            if ( countryCodeToUpdate != null )
            {
                if ( StringUtils.isBlank( countryCodeToUpdate.getValue( ) ) )
                {
                    // Remove code & label attributes
                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( countryCodeToUpdate, identity, clientCode );
                    response.getAttributeStatuses( ).add( codeStatus );
                    if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY ) != null )
                    {
                        if ( countryLabelToUpdate == null )
                        {
                            countryLabelToUpdate = new AttributeDto( );
                            countryLabelToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                        }
                        countryLabelToUpdate.setValue( "" );
                        countryLabelToUpdate.setCertifier( countryCodeToUpdate.getCertifier( ) );
                        countryLabelToUpdate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );
                        final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                        response.getAttributeStatuses( ).add( labelStatus );
                    }
                }
                else
                {
                    final Country country = GeoCodesService.getCountryByCode( countryCodeToUpdate.getValue( ) ).orElse( null );
                    if ( country == null )
                    {
                        // Country doesn't exist in Geocodes for provided code
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryCodeToUpdate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                    {
                        // Country exists in Geocodes for provided code
                        // update country code attribute
                        final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( countryCodeToUpdate, identity, clientCode );
                        response.getAttributeStatuses( ).add( codeStatus );

                        // create country label attribute if it doesn't already exist in the identity
                        // Geocodes label value is used, regardless if a label is provided or not
                        final String countryGeocodesLabel = country.getValue( );
                        if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY ) )
                        {
                            final AttributeChangeStatus labelStatus = ( countryLabelToCreate == null
                                    || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) ) ? AttributeChangeStatus.CREATED
                                            : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                            if ( countryLabelToCreate == null )
                            {
                                countryLabelToCreate = new AttributeDto( );
                                countryLabelToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                            }
                            countryLabelToCreate.setValue( countryGeocodesLabel );
                            countryLabelToCreate.setCertifier( countryCodeToUpdate.getCertifier( ) );
                            countryLabelToCreate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );

                            final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                            attributeStatus.setStatus( labelStatus );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        // update country label if attribute exists, and value is different from existing
                        else
                        {
                            if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY ).getValue( ).equals( countryGeocodesLabel ) )
                            {
                                final boolean override = ( countryLabelToUpdate != null && !countryLabelToUpdate.getValue( ).equals( countryGeocodesLabel ) );
                                if ( countryLabelToUpdate == null )
                                {
                                    countryLabelToUpdate = new AttributeDto( );
                                    countryLabelToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                                }
                                countryLabelToUpdate.setValue( countryGeocodesLabel );
                                countryLabelToUpdate.setCertifier( countryCodeToUpdate.getCertifier( ) );
                                countryLabelToUpdate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );

                                final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                                if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                                {
                                    attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                                }
                                response.getAttributeStatuses( ).add( attributeStatus );
                            }
                        }
                    }
                }
            }
            // No country code sent, checking if label was sent
            else
            {
                if ( countryLabelToCreate != null )
                {
                    final List<Country> countries = GeoCodesService.getCountriesListByName( countryLabelToCreate.getValue( ) );
                    if ( CollectionUtils.isEmpty( countries ) )
                    {
                        // Country doesn't exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                        if ( countries.size( ) > 1 )
                        {
                            // Multiple countries exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        else
                        {
                            // One country exists in Geocodes for provided label
                            // Create country label attribute
                            final AttributeStatus labelStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                            response.getAttributeStatuses( ).add( labelStatus );

                            // create country code attribute if it doesn't already exist in the identity
                            final String countryGeocodesCode = countries.get( 0 ).getCode( );
                            if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
                            {
                                final AttributeDto codeToCreate = new AttributeDto( );
                                codeToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                codeToCreate.setValue( countryGeocodesCode );
                                codeToCreate.setCertifier( countryLabelToCreate.getCertifier( ) );
                                codeToCreate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                response.getAttributeStatuses( ).add( codeStatus );
                            }
                            // update country code if attribute exists, and value is different from existing
                            else
                                if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                {
                                    final AttributeDto codeToUpdate = new AttributeDto( );
                                    codeToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                    codeToUpdate.setValue( countryGeocodesCode );
                                    codeToUpdate.setCertifier( countryLabelToCreate.getCertifier( ) );
                                    codeToUpdate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( codeStatus );
                                }
                        }
                }
                else
                    if ( countryLabelToUpdate != null )
                    {
                        if ( StringUtils.isBlank( countryLabelToUpdate.getValue( ) ) )
                        {
                            // Attempt to remove attribute
                            final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                            response.getAttributeStatuses( ).add( labelStatus );
                            if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE ) != null )
                            {
                                final AttributeDto countryCodeToDelete = new AttributeDto( );
                                countryCodeToDelete.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                countryCodeToDelete.setValue( "" );
                                countryCodeToDelete.setCertifier( countryLabelToUpdate.getCertifier( ) );
                                countryCodeToDelete.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );
                                final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( countryCodeToDelete, identity, clientCode );
                                response.getAttributeStatuses( ).add( codeStatus );
                            }
                        }
                        else
                        {
                            final List<Country> countries = GeoCodesService.getCountriesListByName( countryLabelToUpdate.getValue( ) );
                            if ( CollectionUtils.isEmpty( countries ) )
                            {
                                // Country doesn't exist in Geocodes for provided label
                                final AttributeStatus attributeStatus = new AttributeStatus( );
                                attributeStatus.setKey( countryLabelToUpdate.getKey( ) );
                                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                                response.getAttributeStatuses( ).add( attributeStatus );
                            }
                            else
                            {
                                if ( countries.size( ) > 1 )
                                {
                                    // Multiple countries exist in Geocodes for provided label
                                    final AttributeStatus attributeStatus = new AttributeStatus( );
                                    attributeStatus.setKey( countryLabelToUpdate.getKey( ) );
                                    attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                                    response.getAttributeStatuses( ).add( attributeStatus );
                                }
                                else
                                {
                                    // One country exists in Geocodes for provided label
                                    // Update country label attribute
                                    final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( labelStatus );

                                    // create country code attribute if it doesn't already exist in the identity
                                    final String countryGeocodesCode = countries.get( 0 ).getCode( );
                                    if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
                                    {
                                        final AttributeDto codeToCreate = new AttributeDto( );
                                        codeToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                        codeToCreate.setValue( countryGeocodesCode );
                                        codeToCreate.setCertifier( countryLabelToUpdate.getCertifier( ) );
                                        codeToCreate.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );

                                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                        response.getAttributeStatuses( ).add( codeStatus );
                                    }
                                    // update country code if attribute exists, and value is different from existing
                                    else
                                    {
                                        if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                        {
                                            final AttributeDto codeToUpdate = new AttributeDto( );
                                            codeToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                            codeToUpdate.setValue( countryGeocodesCode );
                                            codeToUpdate.setCertifier( countryLabelToUpdate.getCertifier( ) );
                                            codeToUpdate.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );

                                            final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                            response.getAttributeStatuses( ).add( codeStatus );
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
    }

    /**
     * Private methode used to process both "birthplace_code" and "birthplace" attributes during an update process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param attrToUpdate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    public static void processCityForUpdate( final Identity identity, final List<AttributeDto> attrToCreate, final List<AttributeDto> attrToUpdate,
            final String clientCode, final ChangeResponse response ) throws IdentityStoreException
    {

        final AttributeDto cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
        }
        AttributeDto cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
        }
        final AttributeDto cityCodeToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToUpdate != null )
        {
            attrToUpdate.remove( cityCodeToUpdate );
        }
        AttributeDto cityLabelToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToUpdate != null )
        {
            attrToUpdate.remove( cityLabelToUpdate );
        }

        // City code to CREATE
        if ( cityCodeToCreate != null )
        {
            final City city = GeoCodesService.getCityByCode( cityCodeToCreate.getValue( ) ).orElse( null );
            if ( city == null )
            {
                // city doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( cityCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // city exists in Geocodes for provided code
                // create city code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( cityCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create city label attribute if it doesn't already exist in the identity
                // Geocodes label value is used, regardless if a label is provided or not
                final String cityGeocodesLabel = city.getValue( );
                if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE ) )
                {
                    final AttributeChangeStatus labelStatus = ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                            ? AttributeChangeStatus.CREATED
                            : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                    if ( cityLabelToCreate == null )
                    {
                        cityLabelToCreate = new AttributeDto( );
                        cityLabelToCreate.setKey( Constants.PARAM_BIRTH_PLACE );
                    }
                    cityLabelToCreate.setValue( cityGeocodesLabel );
                    cityLabelToCreate.setCertifier( cityCodeToCreate.getCertifier( ) );
                    cityLabelToCreate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                    final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                    attributeStatus.setStatus( labelStatus );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                // update city label if attribute exists, and value is different from existing
                else
                    if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE ).getValue( ).equals( cityGeocodesLabel ) )
                    {
                        final boolean override = ( cityLabelToUpdate != null && !cityLabelToUpdate.getValue( ).equals( cityGeocodesLabel ) );
                        if ( cityLabelToUpdate == null )
                        {
                            cityLabelToUpdate = new AttributeDto( );
                            cityLabelToUpdate.setKey( Constants.PARAM_BIRTH_PLACE );
                        }
                        cityLabelToUpdate.setValue( cityGeocodesLabel );
                        cityLabelToUpdate.setCertifier( cityCodeToCreate.getCertifier( ) );
                        cityLabelToUpdate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                        if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                        {
                            attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                        }
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
            }
        }
        // city code to UPDATE
        else
            if ( cityCodeToUpdate != null )
            {
                if ( StringUtils.isBlank( cityCodeToUpdate.getValue( ) ) )
                {
                    // Remove code & label attributes
                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( cityCodeToUpdate, identity, clientCode );
                    response.getAttributeStatuses( ).add( codeStatus );
                    if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE ) != null )
                    {
                        if ( cityLabelToUpdate == null )
                        {
                            cityLabelToUpdate = new AttributeDto( );
                            cityLabelToUpdate.setKey( Constants.PARAM_BIRTH_PLACE );
                        }
                        cityLabelToUpdate.setValue( "" );
                        cityLabelToUpdate.setCertifier( cityCodeToUpdate.getCertifier( ) );
                        cityLabelToUpdate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );
                        final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                        response.getAttributeStatuses( ).add( labelStatus );
                    }
                }
                else
                {
                    final City city = GeoCodesService.getCityByCode( cityCodeToUpdate.getValue( ) ).orElse( null );
                    if ( city == null )
                    {
                        // city doesn't exist in Geocodes for provided code
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityCodeToUpdate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                    {
                        // city exists in Geocodes for provided code
                        // update city code attribute
                        final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( cityCodeToUpdate, identity, clientCode );
                        response.getAttributeStatuses( ).add( codeStatus );

                        // create city label attribute if it doesn't already exist in the identity
                        // Geocodes label value is used, regardless if a label is provided or not
                        final String cityGeocodesLabel = city.getValue( );
                        if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE ) )
                        {
                            final AttributeChangeStatus labelStatus = ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                                    ? AttributeChangeStatus.CREATED
                                    : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                            if ( cityLabelToCreate == null )
                            {
                                cityLabelToCreate = new AttributeDto( );
                                cityLabelToCreate.setKey( Constants.PARAM_BIRTH_PLACE );
                            }
                            cityLabelToCreate.setValue( cityGeocodesLabel );
                            cityLabelToCreate.setCertifier( cityCodeToUpdate.getCertifier( ) );
                            cityLabelToCreate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );

                            final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                            attributeStatus.setStatus( labelStatus );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        // update city label if attribute exists, and value is different from existing
                        else
                            if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE ).getValue( ).equals( cityGeocodesLabel ) )
                            {
                                final boolean override = ( cityLabelToUpdate != null && !cityLabelToUpdate.getValue( ).equals( cityGeocodesLabel ) );
                                if ( cityLabelToUpdate == null )
                                {
                                    cityLabelToUpdate = new AttributeDto( );
                                    cityLabelToUpdate.setKey( Constants.PARAM_BIRTH_PLACE );
                                }
                                cityLabelToUpdate.setValue( cityGeocodesLabel );
                                cityLabelToUpdate.setCertifier( cityCodeToUpdate.getCertifier( ) );
                                cityLabelToUpdate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );

                                final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                                if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                                {
                                    attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                                }
                                response.getAttributeStatuses( ).add( attributeStatus );
                            }
                    }
                }
            }
            // No city code sent, checking if label was sent
            else
            {
                if ( cityLabelToCreate != null )
                {
                    final List<City> cities = GeoCodesService.getCitiesListByName( cityLabelToCreate.getValue( ) );
                    if ( CollectionUtils.isEmpty( cities ) )
                    {
                        // city doesn't exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                        if ( cities.size( ) > 1 )
                        {
                            // Multiple cities exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }
                        else
                        {
                            // One city exists in Geocodes for provided label
                            // Create city label attribute
                            final AttributeStatus labelStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                            response.getAttributeStatuses( ).add( labelStatus );

                            // create city code attribute if it doesn't already exist in the identity
                            final String cityGeocodesCode = cities.get( 0 ).getCode( );
                            if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) )
                            {
                                final AttributeDto codeToCreate = new AttributeDto( );
                                codeToCreate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                codeToCreate.setValue( cityGeocodesCode );
                                codeToCreate.setCertifier( cityLabelToCreate.getCertifier( ) );
                                codeToCreate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                response.getAttributeStatuses( ).add( codeStatus );
                            }
                            // update city code if attribute exists, and value is different from existing
                            else
                                if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE_CODE ).getValue( ).equals( cityGeocodesCode ) )
                                {
                                    final AttributeDto codeToUpdate = new AttributeDto( );
                                    codeToUpdate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                    codeToUpdate.setValue( cityGeocodesCode );
                                    codeToUpdate.setCertifier( cityLabelToCreate.getCertifier( ) );
                                    codeToUpdate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( codeStatus );
                                }
                        }
                }
                else
                    if ( cityLabelToUpdate != null )
                    {
                        if ( StringUtils.isBlank( cityLabelToUpdate.getValue( ) ) )
                        {
                            // Attempt to remove attribute
                            final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                            response.getAttributeStatuses( ).add( labelStatus );
                            if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE_CODE ) != null )
                            {
                                final AttributeDto cityCodeToDelete = new AttributeDto( );
                                cityCodeToDelete.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                cityCodeToDelete.setValue( "" );
                                cityCodeToDelete.setCertifier( cityLabelToUpdate.getCertifier( ) );
                                cityCodeToDelete.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );
                                final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( cityCodeToDelete, identity, clientCode );
                                response.getAttributeStatuses( ).add( codeStatus );
                            }
                        }
                        else
                        {
                            final List<City> cities = GeoCodesService.getCitiesListByName( cityLabelToUpdate.getValue( ) );
                            if ( CollectionUtils.isEmpty( cities ) )
                            {
                                // city doesn't exist in Geocodes for provided label
                                final AttributeStatus attributeStatus = new AttributeStatus( );
                                attributeStatus.setKey( cityLabelToUpdate.getKey( ) );
                                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                                response.getAttributeStatuses( ).add( attributeStatus );
                            }
                            else
                                if ( cities.size( ) > 1 )
                                {
                                    // Multiple cities exist in Geocodes for provided label
                                    final AttributeStatus attributeStatus = new AttributeStatus( );
                                    attributeStatus.setKey( cityLabelToUpdate.getKey( ) );
                                    attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                                    response.getAttributeStatuses( ).add( attributeStatus );
                                }
                                else
                                {
                                    // One city exists in Geocodes for provided label
                                    // Update city label attribute
                                    final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                                    response.getAttributeStatuses( ).add( labelStatus );

                                    // create city code attribute if it doesn't already exist in the identity
                                    final String countryGeocodesCode = cities.get( 0 ).getCode( );
                                    if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) )
                                    {
                                        final AttributeDto codeToCreate = new AttributeDto( );
                                        codeToCreate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                        codeToCreate.setValue( countryGeocodesCode );
                                        codeToCreate.setCertifier( cityLabelToUpdate.getCertifier( ) );
                                        codeToCreate.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );

                                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                        response.getAttributeStatuses( ).add( codeStatus );
                                    }
                                    // update city code if attribute exists, and value is different from existing
                                    else
                                        if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                        {
                                            final AttributeDto codeToUpdate = new AttributeDto( );
                                            codeToUpdate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                            codeToUpdate.setValue( countryGeocodesCode );
                                            codeToUpdate.setCertifier( cityLabelToUpdate.getCertifier( ) );
                                            codeToUpdate.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );

                                            final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                            response.getAttributeStatuses( ).add( codeStatus );
                                        }
                                }
                        }
                    }
            }
    }

    /**
     * Private methode used to process both "birthcountry_code" and "birthcountry" attributes during an create process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    public static void processCountryForCreate( final Identity identity, final List<AttributeDto> attrToCreate, final String clientCode,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {

        AttributeDto countryCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToCreate != null )
        {
            attrToCreate.remove( countryCodeToCreate );
        }
        AttributeDto countryLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ).findFirst( )
                .orElse( null );
        if ( countryLabelToCreate != null )
        {
            attrToCreate.remove( countryLabelToCreate );
        }

        // Country code to CREATE
        if ( countryCodeToCreate != null )
        {
            final Country country = GeoCodesService.getCountryByCode( countryCodeToCreate.getValue( ) ).orElse( null );
            if ( country == null )
            {
                // Country doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( countryCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // Country exists in Geocodes for provided code
                // create country code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( countryCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create country label attribute
                // Geocodes label value is used, regardless if a label is provided or not
                final String countryGeocodesLabel = country.getValue( );
                final AttributeChangeStatus labelStatus = ( countryLabelToCreate == null || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) )
                        ? AttributeChangeStatus.CREATED
                        : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                if ( countryLabelToCreate == null )
                {
                    countryLabelToCreate = new AttributeDto( );
                    countryLabelToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                }
                countryLabelToCreate.setValue( countryGeocodesLabel );
                countryLabelToCreate.setCertifier( countryCodeToCreate.getCertifier( ) );
                countryLabelToCreate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                attributeStatus.setStatus( labelStatus );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
        }
        // No country code sent, checking if label was sent
        else
        {
            if ( countryLabelToCreate != null )
            {
                final List<Country> countries = GeoCodesService.getCountriesListByName( countryLabelToCreate.getValue( ) );
                if ( CollectionUtils.isEmpty( countries ) )
                {
                    // Country doesn't exist in Geocodes for provided label
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                else
                    if ( countries.size( ) > 1 )
                    {
                        // Multiple countries exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                    {
                        // One country exists in Geocodes for provided label
                        // Create country label attribute
                        final AttributeStatus labelStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( labelStatus );

                        // create country code attribute
                        final String countryGeocodesCode = countries.get( 0 ).getCode( );
                        countryCodeToCreate = new AttributeDto( );
                        countryCodeToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                        countryCodeToCreate.setValue( countryGeocodesCode );
                        countryCodeToCreate.setCertifier( countryLabelToCreate.getCertifier( ) );
                        countryCodeToCreate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( countryCodeToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( codeStatus );
                    }
            }
        }
    }

    /**
     * Private methode used to process both "birthplace_code" and "birthplace" attributes during an create process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @param response
     * @throws IdentityAttributeNotFoundException
     */
    public static void processCityForCreate( final Identity identity, final List<AttributeDto> attrToCreate, final String clientCode,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {

        AttributeDto cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
        }
        AttributeDto cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
        }

        // City code to CREATE
        if ( cityCodeToCreate != null )
        {
            final City city = GeoCodesService.getCityByCode( cityCodeToCreate.getValue( ) ).orElse( null );
            if ( city == null )
            {
                // city doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( cityCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            {
                // city exists in Geocodes for provided code
                // create city code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( cityCodeToCreate, identity, clientCode );
                response.getAttributeStatuses( ).add( codeStatus );

                // create city label attribute
                // Geocodes label value is used, regardless if a label is provided or not
                final String cityGeocodesLabel = city.getValue( );
                final AttributeChangeStatus labelStatus = ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                        ? AttributeChangeStatus.CREATED
                        : AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                if ( cityLabelToCreate == null )
                {
                    cityLabelToCreate = new AttributeDto( );
                    cityLabelToCreate.setKey( Constants.PARAM_BIRTH_PLACE );
                }
                cityLabelToCreate.setValue( cityGeocodesLabel );
                cityLabelToCreate.setCertifier( cityCodeToCreate.getCertifier( ) );
                cityLabelToCreate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                attributeStatus.setStatus( labelStatus );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
        }
        // No city code sent, checking if label was sent
        else
        {
            if ( cityLabelToCreate != null )
            {
                final List<City> cities = GeoCodesService.getCitiesListByName( cityLabelToCreate.getValue( ) );
                if ( CollectionUtils.isEmpty( cities ) )
                {
                    // city doesn't exist in Geocodes for provided label
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
                else
                    if ( cities.size( ) > 1 )
                    {
                        // Multiple cities exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        response.getAttributeStatuses( ).add( attributeStatus );
                    }
                    else
                    {
                        // One city exists in Geocodes for provided label
                        // Create city label attribute
                        final AttributeStatus labelStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( labelStatus );

                        // create city code attribute
                        final String countryGeocodesCode = cities.get( 0 ).getCode( );
                        cityCodeToCreate = new AttributeDto( );
                        cityCodeToCreate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                        cityCodeToCreate.setValue( countryGeocodesCode );
                        cityCodeToCreate.setCertifier( cityLabelToCreate.getCertifier( ) );
                        cityCodeToCreate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( cityCodeToCreate, identity, clientCode );
                        response.getAttributeStatuses( ).add( codeStatus );
                    }
            }
        }
    }
}
