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
package fr.paris.lutece.plugins.identitystore.service.duplicate;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentityHome;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleAttributeTreatment;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityQualityService;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DuplicateService implements IDuplicateService
{
    /**
     * Identity Search service
     */
    protected ISearchIdentityService _searchIdentityService;

    /**
     * Prefix of the group of rules
     */
    protected String _group;

    public DuplicateService( ISearchIdentityService _searchIdentityService, String _group )
    {
        this._searchIdentityService = _searchIdentityService;
        this._group = _group;
    }

    /**
     * Performs a search request on the given {@link ISearchIdentityService}. <br>
     * The rules are defined in properties file with :
     * <ul>
     * <li>a prefix specified in {@link DuplicateService} constructor, which defines the group of rules to load</li>
     * <li>a mode of checking which can be strict or fuzzy (if not specified, default mode is strict)</li>
     * <li>an id which is used to specify several rules to process, and their order</li>
     * </ul>
     * E.g: For creation action: {prefix}.{id}={attribute_key_1},{attribute_key_2},{attribute_key_3}:fuzzy,{attribute_key_4}
     * <ul>
     * <li>identitystore.identity.duplicates.import.suspicion.0=family_name,first_name,birthdate</li>
     * <li>identitystore.identity.duplicates.import.suspicion.1=family_name:fuzzy,first_name:fuzzy,birthdate</li>
     * </ul>
     *
     * @param attributeValues
     *            a {@link Map} of attribute key and attribute value
     * @return a {@link DuplicateSearchResponse} that contains the result of the search request
     */
    @Override
    public DuplicateSearchResponse findDuplicates( final Map<String, String> attributeValues )
    {
        final List<String> keys = AppPropertiesService.getKeys( _group );

        if ( CollectionUtils.isNotEmpty( keys ) )
        {
            final List<String> sortedRules = keys.stream( )
                    .sorted( ( o1, o2 ) -> StringUtils.compare( StringUtils.substringAfterLast( o1, "." ), StringUtils.substringAfterLast( o2, "." ) ) )
                    .collect( Collectors.toList( ) );
            for ( final String rule : sortedRules )
            {
                final String property = AppPropertiesService.getProperty( rule );
                final List<SearchAttributeDto> searchAttributes = this.mapAttributes( property, attributeValues );

                final List<QualifiedIdentity> resultIdentities = _searchIdentityService
                        .getQualifiedIdentities( searchAttributes, null, searchAttributes.size( ), null, 0, false ).stream( )
                        .filter( qualifiedIdentity -> !qualifiedIdentity.isMerged( ) ).collect( Collectors.toList( ) );
                if ( CollectionUtils.isNotEmpty( resultIdentities ) )
                {
                    final DuplicateSearchResponse response = new DuplicateSearchResponse( );
                    response.setMessage( "Une ou plusieurs identités existent avec cette règle : " + property );
                    response.setIdentities( resultIdentities );
                    return response;
                }
            }
        }

        return null;
    }

    /**
     * Performs a search request on the given {@link ISearchIdentityService} applying the given {@link DuplicateRule} <br>
     * 
     * @see DuplicateRule DuplicateRule documentation
     *
     * @param attributeValues
     *            a {@link Map} of attribute key and attribute value of the base {@link Identity}
     * @param customerId
     *            the customerId of the base {@link Identity}
     * @param ruleCode
     *            the code of the {@link DuplicateRule} to be applied
     * @return a {@link DuplicateSearchResponse} that contains the result of the search request, with a list of {@link QualifiedIdentity} that matches the
     *         {@link DuplicateRule} definition and the given list of attributes.
     */
    @Override
    public DuplicateSearchResponse findDuplicates( final Map<String, String> attributeValues, final String customerId, final String ruleCode )
            throws IdentityStoreException
    {
        final DuplicateRule duplicateRule = DuplicateRuleService.instance( ).get( ruleCode );
        if ( duplicateRule == null )
        {
            throw new IdentityStoreException( "Could not find duplicate rule with code " + ruleCode );
        }
        if ( CollectionUtils.isNotEmpty( duplicateRule.getCheckedAttributes( ) ) )
        {
            final List<SearchAttributeDto> searchAttributes = this.mapBaseAttributes( attributeValues, duplicateRule );
            final List<List<SearchAttributeDto>> specialTreatmentAttributes = this.mapSpecialTreatmentAttributes( attributeValues, duplicateRule );
            final List<QualifiedIdentity> qualifiedIdentities = _searchIdentityService.getQualifiedIdentities( searchAttributes, specialTreatmentAttributes,
                    duplicateRule.getNbEqualAttributes( ), duplicateRule.getNbMissingAttributes( ), 0, false );
            final List<String> allCuids = qualifiedIdentities.stream( ).map( QualifiedIdentity::getCustomerId ).collect( Collectors.toList( ) );
            final List<QualifiedIdentity> results = qualifiedIdentities.stream( )
                    .filter( qualifiedIdentity -> !SuspiciousIdentityHome.excluded( qualifiedIdentity.getCustomerId( ),
                            allCuids.stream( ).filter( cuid -> !Objects.equals( cuid, qualifiedIdentity.getCustomerId( ) ) ).collect( Collectors.toList( ) ) ) )
                    .filter( qualifiedIdentity -> !qualifiedIdentity.isMerged( ) && !Objects.equals( qualifiedIdentity.getCustomerId( ), customerId ) )
                    .peek( qualifiedIdentity -> {
                        try
                        {
                            IdentityQualityService.instance( ).computeQuality( qualifiedIdentity );
                        }
                        catch( IdentityAttributeNotFoundException e )
                        {
                            throw new RuntimeException( e );
                        }
                    } ).collect( Collectors.toList( ) );
            if ( CollectionUtils.isNotEmpty( results ) )
            {
                final DuplicateSearchResponse response = new DuplicateSearchResponse( );
                response.setMessage( "Un ou plusieurs doublon existent pour l'indentité " + customerId + " avec la règle : " + duplicateRule.getCode( ) );
                response.setIdentities( results );
                return response;
            }
        }
        final DuplicateSearchResponse response = new DuplicateSearchResponse( );
        response.setMessage( "No potential duplicate found for identity " + customerId + " with the rule : " + duplicateRule.getCode( ) );
        response.setIdentities( Collections.emptyList( ) );
        return response;
    }

    private boolean hasMissingField( final QualifiedIdentity qualifiedIdentity, final DuplicateRule duplicateRule )
    {
        final Set<String> identityAttrKeys = qualifiedIdentity.getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                .map( CertifiedAttribute::getKey ).collect( Collectors.toSet( ) );
        final Set<String> ruleAttrKeys = duplicateRule.getCheckedAttributes( ).stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toSet( ) );
        if ( duplicateRule.getNbMissingAttributes( ) == 0 )
        {
            return identityAttrKeys.containsAll( ruleAttrKeys );
        }
        else
        {
            int missingField = 0;
            for ( final String ruleAttrKey : ruleAttrKeys )
            {
                if ( !identityAttrKeys.contains( ruleAttrKey ) )
                {
                    missingField++;
                }
            }
            return missingField <= duplicateRule.getNbMissingAttributes( );
        }
    }

    private List<SearchAttributeDto> mapBaseAttributes( final Map<String, String> attributeValues, final DuplicateRule duplicateRule )
    {
        final List<SearchAttributeDto> searchAttributes = new ArrayList<>( );

        for ( final AttributeKey key : duplicateRule.getCheckedAttributes( ) )
        {
            final Optional<String> attributeKey = attributeValues.keySet( ).stream( ).filter( attKey -> attKey.equals( key.getKeyName( ) ) ).findFirst( );
            if ( attributeKey.isPresent( ) )
            {
                final SearchAttributeDto searchAttribute = new SearchAttributeDto( );
                searchAttribute.setKey( key.getKeyName( ) );
                searchAttribute.setValue( attributeValues.get( key.getKeyName( ) ) );
                searchAttribute.setTreatmentType( AttributeTreatmentType.STRICT );
                searchAttributes.add( searchAttribute );
            }
        }
        return searchAttributes;
    }

    private List<List<SearchAttributeDto>> mapSpecialTreatmentAttributes( final Map<String, String> attributeValues, final DuplicateRule duplicateRule )
    {
        final List<List<SearchAttributeDto>> specialAttributesTreatment = new ArrayList<>( );

        for ( final DuplicateRuleAttributeTreatment attributeTreatment : duplicateRule.getAttributeTreatments( ) )
        {
            final List<SearchAttributeDto> searchAttributes = new ArrayList<>( );
            for ( final AttributeKey key : attributeTreatment.getAttributes( ) )
            {
                final Optional<String> attributeKey = attributeValues.keySet( ).stream( ).filter( attKey -> attKey.equals( key.getKeyName( ) ) ).findFirst( );
                if ( attributeKey.isPresent( ) )
                {
                    final SearchAttributeDto searchAttribute = new SearchAttributeDto( );
                    searchAttribute.setKey( key.getKeyName( ) );
                    searchAttribute.setValue( attributeValues.get( key.getKeyName( ) ) );
                    searchAttribute.setTreatmentType( attributeTreatment.getType( ) );
                    searchAttributes.add( searchAttribute );
                }
            }
            specialAttributesTreatment.add( searchAttributes );
        }
        return specialAttributesTreatment;
    }

    private List<SearchAttributeDto> mapAttributes( final String property, final Map<String, String> attributeValues )
    {
        final List<SearchAttributeDto> attributeDtoList = new ArrayList<>( );
        Arrays.asList( StringUtils.split( property, "," ) ).forEach( attributeKey -> {
            final String [ ] attributeAndMode = StringUtils.split( attributeKey, ":" );
            final String key = attributeAndMode [0];
            final boolean fuzzy = attributeAndMode.length == 2 && StringUtils.equals( attributeAndMode [1], "fuzzy" );
            final String value = attributeValues.get( key );
            if ( StringUtils.isNotEmpty( value ) )
            {
                attributeDtoList.add( new SearchAttributeDto( key, value, fuzzy ? AttributeTreatmentType.APPROXIMATED : AttributeTreatmentType.STRICT ) );
            }
        } );
        return attributeDtoList;
    }
}
