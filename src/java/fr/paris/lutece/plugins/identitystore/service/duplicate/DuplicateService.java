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
import fr.paris.lutece.plugins.identitystore.utils.Maps;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentitySearchResult;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DuplicateService implements IDuplicateService
{
    /**
     * Identity Search service
     */
    protected final ISearchIdentityService _searchIdentityService;

    public DuplicateService( final ISearchIdentityService _searchIdentityService )
    {
        this._searchIdentityService = _searchIdentityService;
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
     * @param ruleCodes
     *            the list of {@link DuplicateRule} codes to be applied
     * @return a {@link DuplicateSearchResponse} that contains the result of the search request, with a list of {@link IdentityDto} that matches the
     *         {@link DuplicateRule} definition and the given list of attributes.
     */
    @Override
    public DuplicateSearchResponse findDuplicates( final Map<String, String> attributeValues, final String customerId, final List<String> ruleCodes )
            throws IdentityStoreException
    {
        final DuplicateSearchResponse response = new DuplicateSearchResponse( );
        final List<String> matchingRuleCodes = new ArrayList<>( );
        if ( CollectionUtils.isNotEmpty( ruleCodes ) )
        {
            for ( final String ruleCode : ruleCodes )
            {
                final DuplicateRule duplicateRule = DuplicateRuleService.instance( ).get( ruleCode );
                if ( duplicateRule == null )
                {
                    final DuplicateSearchResponse errorResponse = new DuplicateSearchResponse( );
                    errorResponse.setStatus( ResponseStatusFactory.notFound( ).setMessage( "Duplicate rule not found for code " + ruleCode )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_NO_DUPLICATE_RULE_FOUND ) );
                    return errorResponse;
                }
                final QualifiedIdentitySearchResult identitySearchResult = this.findDuplicates( attributeValues, customerId, duplicateRule );
                if ( !identitySearchResult.getQualifiedIdentities( ).isEmpty( ) )
                {
                    matchingRuleCodes.add( ruleCode );
                    response.getIdentities( ).addAll( identitySearchResult.getQualifiedIdentities( ) );
                    Maps.mergeStringMap( response.getMetadata( ), identitySearchResult.getMetadata( ) );
                }
            }
        }

        if ( CollectionUtils.isNotEmpty( response.getIdentities( ) ) )
        {
            response.setStatus( ResponseStatusFactory.ok( ).setMessage( "Potential duplicate(s) found with rule(s) : " + String.join( ",", matchingRuleCodes ) )
                    .setMessageKey( Constants.PROPERTY_REST_INFO_POTENTIAL_DUPLICATE_FOUND ) );
        }
        else
        {
            response.setStatus(
                    ResponseStatusFactory.notFound( ).setMessage( "No potential duplicate found with the rule(s) : " + String.join( ",", ruleCodes ) )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_NO_POTENTIAL_DUPLICATE_FOUND ) );
            response.setIdentities( Collections.emptyList( ) );
        }

        return response;
    }

    private QualifiedIdentitySearchResult findDuplicates( final Map<String, String> attributeValues, final String customerId,
            final DuplicateRule duplicateRule ) throws IdentityStoreException
    {
        if ( CollectionUtils.isNotEmpty( duplicateRule.getCheckedAttributes( ) ) && this.canApplyRule( attributeValues, duplicateRule ) )
        {
            final List<SearchAttribute> searchAttributes = this.mapBaseAttributes( attributeValues, duplicateRule );
            final List<List<SearchAttribute>> specialTreatmentAttributes = this.mapSpecialTreatmentAttributes( attributeValues, duplicateRule );
            final QualifiedIdentitySearchResult result = _searchIdentityService.getQualifiedIdentities( searchAttributes, specialTreatmentAttributes,
                    duplicateRule.getNbEqualAttributes( ), duplicateRule.getNbMissingAttributes( ), 0, false );
            result.getQualifiedIdentities( ).removeIf( qualifiedIdentity -> SuspiciousIdentityHome.excluded( qualifiedIdentity.getCustomerId( ), customerId ) );
            result.getQualifiedIdentities( ).removeIf( identity -> ( identity.getMerge( ) != null && identity.getMerge( ).isMerged( ) )
                    || Objects.equals( identity.getCustomerId( ), customerId ) );
            result.getQualifiedIdentities( ).forEach( qualifiedIdentity -> {
                try
                {
                    IdentityQualityService.instance( ).computeQuality( qualifiedIdentity );
                }
                catch( IdentityAttributeNotFoundException e )
                {
                    throw new RuntimeException( e );
                }
            } );
            return result;
        }
        return new QualifiedIdentitySearchResult( );
    }

    /**
     * A rule can be applying on a set of Attributes only when it contains nbFilledAttributes among checkedAttributes ({@link DuplicateRule} definition).
     * 
     * @param attributeValues
     *            the set of Attributes that must be checked against the {@link DuplicateRule}.
     * @param duplicateRule
     *            the {@link DuplicateRule} definition.
     * @return true if the set of Attributes matches the {@link DuplicateRule} requirements.
     */
    private boolean canApplyRule( final Map<String, String> attributeValues, final DuplicateRule duplicateRule )
    {
        if ( duplicateRule.getNbFilledAttributes( ) <= attributeValues.size( ) )
        {
            final List<String> ruleCheckedAttributeKeys = duplicateRule.getCheckedAttributes( ).stream( ).map( AttributeKey::getKeyName )
                    .collect( Collectors.toList( ) );
            final List<String> attributeKeys = new ArrayList<>( attributeValues.keySet( ) );
            attributeKeys.removeIf( key -> !ruleCheckedAttributeKeys.contains( key ) );
            return duplicateRule.getNbFilledAttributes( ) <= attributeKeys.size( );
        }
        return false;
    }

    private List<SearchAttribute> mapBaseAttributes( final Map<String, String> attributeValues, final DuplicateRule duplicateRule )
    {
        final List<SearchAttribute> searchAttributes = new ArrayList<>( );

        for ( final AttributeKey key : duplicateRule.getCheckedAttributes( ) )
        {
            final Optional<String> attributeKey = attributeValues.keySet( ).stream( ).filter( attKey -> attKey.equals( key.getKeyName( ) ) ).findFirst( );
            if ( attributeKey.isPresent( ) )
            {
                final SearchAttribute searchAttribute = new SearchAttribute( );
                searchAttribute.setKey( key.getKeyName( ) );
                searchAttribute.setValue( attributeValues.get( key.getKeyName( ) ) );
                searchAttribute.setTreatmentType( AttributeTreatmentType.STRICT );
                searchAttributes.add( searchAttribute );
            }
        }
        return searchAttributes;
    }

    private List<List<SearchAttribute>> mapSpecialTreatmentAttributes( final Map<String, String> attributeValues, final DuplicateRule duplicateRule )
    {
        final List<List<SearchAttribute>> specialAttributesTreatment = new ArrayList<>( );

        for ( final DuplicateRuleAttributeTreatment attributeTreatment : duplicateRule.getAttributeTreatments( ) )
        {
            final List<SearchAttribute> searchAttributes = new ArrayList<>( );
            for ( final AttributeKey key : attributeTreatment.getAttributes( ) )
            {
                final Optional<String> attributeKey = attributeValues.keySet( ).stream( ).filter( attKey -> attKey.equals( key.getKeyName( ) ) ).findFirst( );
                if ( attributeKey.isPresent( ) )
                {
                    final SearchAttribute searchAttribute = new SearchAttribute( );
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
}
