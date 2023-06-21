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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearSearchRequest extends ASearchRequest
{
    private boolean connected = false;
    private Integer minimalShouldMatch;
    private Integer maxMissingAttributes;

    public NearSearchRequest( final List<SearchAttribute> attributes, Integer minimalShouldMatch, final Integer maxMissingAttributes, final boolean connected )
    {
        this.getSearchAttributes( ).addAll( attributes );
        this.connected = connected;
        this.minimalShouldMatch = minimalShouldMatch;
        this.maxMissingAttributes = maxMissingAttributes;
    }

    @Override
    public InnerSearchRequest body( )
    {
        final InnerSearchRequest body = new InnerSearchRequest( );
        final Query query = new Query( );
        final Bool bool = new Bool( );
        if ( this.minimalShouldMatch != null )
        {
            bool.setMinimumShouldMatch( minimalShouldMatch );
        }
        final ArrayList<AbstractContainer> shouldOrMust = new ArrayList<>( );
        this.getSearchAttributes( ).forEach( searchAttribute -> {
            switch( searchAttribute.getInputKey( ) )
            {
                case Constants.PARAM_FAMILY_NAME:
                    if ( searchAttribute.isStrict( ) )
                    {
                        shouldOrMust.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                    }
                    else
                    {
                        shouldOrMust.add( new MatchContainer( getMatch( searchAttribute ) ) );
                    }
                    break;
                case Constants.PARAM_FIRST_NAME:
                    searchAttribute.setValue(searchAttribute.getValue( ).toLowerCase());
                    if ( searchAttribute.isStrict( ) )
                    {
                        shouldOrMust.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                    }
                    else
                    {
                        SpanNear spanNear = new SpanNear( );
                        String [ ] splitSearchValue = searchAttribute.getValue( ).split( " " );
                        if ( splitSearchValue.length > 1 )
                        {
                            spanNear.setSlop( splitSearchValue.length - 1 );
                            Arrays.stream( splitSearchValue ).forEach( word -> {
                                spanNear.getClauses( ).add( new SpanMultiContainer(
                                        new SpanMulti( new SpanMultiFuzzyMatchContainer( getSpanMultiFuzzyMatch( searchAttribute, word ) ) ) ) );
                            } );
                            spanNear.setInOrder( true );
                            spanNear.setBoost( 1 );
                            shouldOrMust.add( new SpanNearContainer( spanNear ) );
                        }
                        else
                        {
                            shouldOrMust.add( new MatchContainer( getMatch( searchAttribute ) ) );
                        }

                    }
                    break;
                case Constants.PARAM_PREFERRED_USERNAME:
                    if ( searchAttribute.isStrict( ) )
                    {
                        shouldOrMust.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                    }
                    else
                    {
                        shouldOrMust.add( new MatchContainer( getMatch( searchAttribute ) ) );
                    }
                    break;
                default:
                    if ( searchAttribute.getOutputKeys( ).size( ) == 1 )
                    {
                        shouldOrMust.add( new MatchContainer( getMatch( searchAttribute ) ) );
                    }
                    else
                    {
                        shouldOrMust.add( new MultiMatchContainer( getMultiMatch( searchAttribute ) ) );
                    }
                    break;
            }
        } );

        if ( this.isConnected( ) )
        {
            final Exists connectionId = new Exists( );
            connectionId.setField( "connectionId" );
            shouldOrMust.add( new ExistsContainer( connectionId ) );
            final Exists login = new Exists( );
            login.setField( "attributes.login.value" );
            shouldOrMust.add( new ExistsContainer( login ) );
        }

        if ( this.minimalShouldMatch != null )
        {
            bool.setShould( shouldOrMust );
        }
        else
        {
            bool.setMust( shouldOrMust );
        }
        query.setBool( bool );
        body.setQuery( query );
        return body;
    }

    private SpanMultiFuzzyMatch getSpanMultiFuzzyMatch( SearchAttribute searchAttribute, String value )
    {
        SpanMultiFuzzyMatch miltiMatch = new SpanMultiFuzzyMatch( );
        miltiMatch.setName( "attributes." + searchAttribute.getInputKey( ) + ".value" );
        miltiMatch.setFuzziness( "1" );
        miltiMatch.setValue( value );
        return miltiMatch;
    }

    public boolean isConnected( )
    {
        return connected;
    }
}
