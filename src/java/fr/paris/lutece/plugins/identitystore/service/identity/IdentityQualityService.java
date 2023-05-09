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
package fr.paris.lutece.plugins.identitystore.service.identity;

import com.google.common.util.concurrent.AtomicDouble;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRequirement;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeCache;
import fr.paris.lutece.plugins.identitystore.cache.QualityBaseCache;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IdentityQualityService
{
    private static final IdentityAttributeCache _identityAttributeCache = SpringContextService.getBean( "identitystore.identityAttributeCache" );
    private static final QualityBaseCache _qualityBaseCache = SpringContextService.getBean( "identitystore.qualityBaseCache" );

    private static IdentityQualityService _instance;

    public static IdentityQualityService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityQualityService( );
            _instance._qualityBaseCache.refresh( );
        }
        return _instance;
    }

    private IdentityQualityService( )
    {
    }

    /**
     * Compute the {@link QualifiedIdentity} coverage of the {@link ServiceContract} requirements.
     *
     * @param qualifiedIdentity
     * @param serviceContract
     */
    public void computeCoverage( final QualifiedIdentity qualifiedIdentity, final ServiceContract serviceContract )
    {
        // Check that all attributes required by the contract are present in the identity
        final List<String> reqKeys = serviceContract.getAttributeRequirements( ).stream( )
                .filter( req -> req.getRefCertificationLevel( ) != null && StringUtils.isNotEmpty( req.getRefCertificationLevel( ).getLevel( ) ) )
                .map( AttributeRequirement::getAttributeKey ).map( AttributeKey::getKeyName ).collect( Collectors.toList( ) );
        final List<String> identityKeys = qualifiedIdentity.getAttributes( ).stream( ).map( CertifiedAttribute::getKey ).collect( Collectors.toList( ) );
        reqKeys.removeAll( identityKeys );
        if ( reqKeys.size( ) > 0 )
        {
            qualifiedIdentity.setCoverage( 0 );
        }
        else
        {
            // Check that no attribute of the identity has its certification level lower than the minimum defined in the contract
            boolean noneMatch = qualifiedIdentity.getAttributes( ).stream( ).noneMatch( certifiedAttribute -> {
                final AttributeRequirement requirement = serviceContract.getAttributeRequirements( ).stream( )
                        .filter( req -> Objects.equals( req.getAttributeKey( ).getKeyName( ), certifiedAttribute.getKey( ) ) ).findFirst( ).orElse( null );
                final int attributeLevel = certifiedAttribute.getCertificationLevel( ) != null ? certifiedAttribute.getCertificationLevel( ) : 0;
                final int minLevel = ( requirement != null && requirement.getRefCertificationLevel( ) != null
                        && requirement.getRefCertificationLevel( ).getLevel( ) != null )
                                ? Integer.valueOf( requirement.getRefCertificationLevel( ).getLevel( ) )
                                : 0;
                return minLevel > attributeLevel;
            } );
            qualifiedIdentity.setCoverage( noneMatch ? 1 : 0 );
        }
    }

    public void computeQuality( final QualifiedIdentity qualifiedIdentity ) throws IdentityAttributeNotFoundException
    {
        final AtomicInteger levels = new AtomicInteger( );
        for ( final CertifiedAttribute attribute : qualifiedIdentity.getAttributes( ) )
        {
            final AttributeKey attributeKey = _identityAttributeCache.get( attribute.getKey( ) );
            if ( attributeKey.getKeyWeight( ) > 0 )
            {
                final Integer certificateLevel = attribute.getCertificationLevel( ) != null ? attribute.getCertificationLevel( ) : 0;
                levels.addAndGet( attributeKey.getKeyWeight( ) * certificateLevel );
            }
        }
        qualifiedIdentity.setQuality( levels.doubleValue( ) / _qualityBaseCache.get( ) );
    }

    public void computeMatchScore( final QualifiedIdentity qualifiedIdentity, final List<SearchAttributeDto> searchAttributes )
    {
        final AtomicDouble levels = new AtomicDouble( );
        final AtomicDouble base = new AtomicDouble( );
        final Map<SearchAttributeDto, List<AttributeKey>> attributesToProcess = new HashMap<>( );
        for ( final SearchAttributeDto searchAttribute : searchAttributes )
        {
            AttributeKey refKey = null;
            try {
                refKey = IdentityService.instance().getAttributeKey(searchAttribute.getKey());
            } catch (IdentityAttributeNotFoundException e) {
                //do nothing, we check if attribute exists
            }
            if ( refKey != null )
            {
                attributesToProcess.put( searchAttribute, Arrays.asList( refKey ) );
            }
            else
            {
                // In this case we have a common search key in the request, so retrieve the attribute
                final List<AttributeKey> commonAttributes = IdentityService.instance().getCommonAttributeKeys(searchAttribute.getKey());
                attributesToProcess.put( searchAttribute, commonAttributes );
            }
        }

        for ( final Map.Entry<SearchAttributeDto, List<AttributeKey>> entry : attributesToProcess.entrySet( ) )
        {
            for ( final AttributeKey attributeKey : entry.getValue( ) )
            {
                final CertifiedAttribute certifiedAttribute = qualifiedIdentity.getAttributes( ).stream( )
                        .filter( attribute -> Objects.equals( attribute.getKey( ), attributeKey.getKeyName( ) ) ).findFirst( ).orElse( null );
                base.addAndGet( attributeKey.getKeyWeight( ) );
                if ( certifiedAttribute.getValue( ).equals( entry.getKey( ).getValue( ) ) )
                {
                    levels.addAndGet( attributeKey.getKeyWeight( ) );
                }
                else
                {
                    final Double penalty = Double.valueOf( AppPropertiesService.getProperty( "identitystore.identity.scoring.penalty", "0.3" ) );
                    levels.addAndGet( attributeKey.getKeyWeight( ) - ( attributeKey.getKeyWeight( ) * penalty ) );
                }
            }
        }

        qualifiedIdentity.setScoring( levels.doubleValue( ) / base.doubleValue( ) );
    }
}
