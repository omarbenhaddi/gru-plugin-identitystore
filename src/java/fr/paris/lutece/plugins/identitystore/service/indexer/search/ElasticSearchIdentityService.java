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
package fr.paris.lutece.plugins.identitystore.service.indexer.search;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.RefAttributeCertificationDefinitionNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.AttributeObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.service.IIdentitySearcher;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticSearchIdentityService implements ISearchIdentityService
{
    private IIdentitySearcher _identitySearcher;

    /**
     * private constructor
     */
    private ElasticSearchIdentityService( IIdentitySearcher _identitySearcher )
    {
        this._identitySearcher = _identitySearcher;
    }

    private List<SearchAttribute> getSearchAttributes( List<SearchAttributeDto> attributes )
    {
        final List<SearchAttribute> searchAttributes = new ArrayList<>( );
        for ( final SearchAttributeDto dto : attributes )
        {
            AttributeKey refKey = null;
            try
            {
                refKey = IdentityAttributeService.instance( ).getAttributeKey( dto.getKey( ) );
            }
            catch( IdentityAttributeNotFoundException e )
            {
                // do nothing, we want to identify if the key exists
            }
            if ( refKey != null )
            {
                searchAttributes.add( new SearchAttribute( dto.getKey( ), Arrays.asList( dto.getKey( ) ), dto.getValue( ), dto.isStrict( ) ) );
            }
            else
            {
                // In this case we have a common search key in the request, so map it
                final List<AttributeKey> commonAttributeKeys = IdentityAttributeService.instance( ).getCommonAttributeKeys( dto.getKey( ) );
                final List<String> commonAttributeKeyNames = commonAttributeKeys.stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toList( ) );
                searchAttributes.add( new SearchAttribute( dto.getKey( ), commonAttributeKeyNames, dto.getValue( ), dto.isStrict( ) ) );
            }
        }
        return searchAttributes;
    }

    private List<QualifiedIdentity> getEntities( Response search )
    {
        final List<QualifiedIdentity> identities = new ArrayList<>( );

        if ( search != null )
        {
            search.getResult( ).getHits( ).forEach( hit -> {
                try
                {
                    identities.add( this.toQualifiedIdentity( hit.getSource( ) ) );
                }
                catch( IdentityAttributeNotFoundException | RefAttributeCertificationDefinitionNotFoundException e )
                {
                    AppLogService.error( "An error occurred during search: ", e );
                }
            } );
        }
        return identities;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<QualifiedIdentity> getQualifiedIdentities( final List<SearchAttributeDto> attributes, final Integer minimalShouldMatch,
            final Integer maxMissingAttributes, final int max, final boolean connected )
    {
        final List<SearchAttribute> searchAttributes = this.getSearchAttributes( attributes );

        final Response search = _identitySearcher.search( searchAttributes, minimalShouldMatch, maxMissingAttributes, max, connected );

        return getEntities( search );
    }

    @Override
    public List<QualifiedIdentity> getQualifiedIdentities( List<SearchAttributeDto> attributes, int max, boolean connected )
    {
        final List<SearchAttribute> searchAttributes = this.getSearchAttributes( attributes );

        final Response search = _identitySearcher.search( searchAttributes, max, connected );

        return getEntities( search );
    }

    private QualifiedIdentity toQualifiedIdentity( final IdentityObject identityObject )
            throws IdentityAttributeNotFoundException, RefAttributeCertificationDefinitionNotFoundException
    {
        final QualifiedIdentity identity = new QualifiedIdentity( );
        identity.setConnectionId( identityObject.getConnectionId( ) );
        identity.setCustomerId( identityObject.getCustomerId( ) );
        identity.setCreationDate( identityObject.getCreationDate( ) );
        identity.setLastUpdateDate( identityObject.getLastUpdateDate( ) );
        for ( final Map.Entry<String, AttributeObject> entry : identityObject.getAttributes( ).entrySet( ) )
        {
            final String s = entry.getKey( );
            AttributeObject attributeObject = entry.getValue( );
            final CertifiedAttribute attribute = new CertifiedAttribute( );
            attribute.setKey( s );
            attribute.setValue( attributeObject.getValue( ) );
            attribute.setType( attributeObject.getType( ) );
            attribute.setCertifier( attributeObject.getCertifierCode( ) );
            attribute.setCertificationDate( attributeObject.getCertificateDate( ) );
            attribute.setCertificationLevel( AttributeCertificationDefinitionService.instance( ).getLevelAsInteger( attribute.getCertifier( ), s ) );
            identity.getAttributes( ).add( attribute );
        }
        return identity;
    }
}
