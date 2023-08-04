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

public class ComplexSearchRequest extends ASearchRequest
{
    private boolean connected = false;

    public ComplexSearchRequest( final List<SearchAttribute> attributes, final boolean connected )
    {
        this.getSearchAttributes( ).addAll( attributes );
        this.connected = connected;
    }

    @Override
    public InnerSearchRequest body( )
    {
        final InnerSearchRequest body = new InnerSearchRequest( );
        final Query query = new Query( );
        final Bool bool = new Bool( );
        final List<AbstractContainer> must = new ArrayList<>( );
        final List<AbstractContainer> mustNot = new ArrayList<>( );

        this.getSearchAttributes( ).forEach( searchAttribute -> {
            switch( searchAttribute.getKey( ) )
            {
                case Constants.PARAM_FAMILY_NAME:
                    switch( searchAttribute.getTreatmentType( ) )
                    {
                        case STRICT:
                            must.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                            break;
                        case APPROXIMATED:
                            final String multipleHyphenToOne = searchAttribute.getValue( ).trim( ).replaceAll( "(-)\\1+", "$1" );
                            final String multipleSpacesToONe = multipleHyphenToOne.replaceAll( " +", " " );
                            final String trimmedHyphens = multipleSpacesToONe.replaceAll( " - ", "-" );
                            searchAttribute.setValue( trimmedHyphens );
                            must.add( new MatchContainer( getMatch( searchAttribute ) ) );
                            break;
                        case DIFFERENT:
                            mustNot.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                            break;
                        case ABSENT:
                            mustNot.add( new ExistsContainer( getExists( searchAttribute ) ) );
                        default:
                            break;
                    }
                    break;
                case Constants.PARAM_FIRST_NAME:
                    switch( searchAttribute.getTreatmentType( ) )
                    {
                        case STRICT:
                            must.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                            break;
                        case APPROXIMATED:
                            searchAttribute.setValue( searchAttribute.getValue( ).trim( ).replaceAll( " +", " " ).toLowerCase( ) );
                            final SpanNear spanNear = new SpanNear( );
                            final String [ ] splitSearchValue = searchAttribute.getValue( ).split( " " );
                            if ( splitSearchValue.length > 1 )
                            {
                                spanNear.setSlop( splitSearchValue.length - 1 );
                                Arrays.stream( splitSearchValue ).forEach( word -> spanNear.getClauses( ).add( new SpanMultiContainer(
                                        new SpanMulti( new SpanMultiFuzzyMatchContainer( getSpanMultiFuzzyMatch( searchAttribute, word ) ) ) ) ) );
                                spanNear.setInOrder( true );
                                spanNear.setBoost( 1 );
                                must.add( new SpanNearContainer( spanNear ) );
                            }
                            else
                            {
                                must.add( new MatchContainer( getMatch( searchAttribute ) ) );
                            }
                            break;
                        case DIFFERENT:
                            mustNot.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                            break;
                        case ABSENT:
                            mustNot.add( new ExistsContainer( getExists( searchAttribute ) ) );
                        default:
                            break;
                    }
                    break;
                case Constants.PARAM_PREFERRED_USERNAME:
                    switch( searchAttribute.getTreatmentType( ) )
                    {
                        case STRICT:
                            must.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                            break;
                        case APPROXIMATED:
                            must.add( new MatchContainer( getMatch( searchAttribute ) ) );
                            break;
                        case DIFFERENT:
                            mustNot.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                            break;
                        case ABSENT:
                            mustNot.add( new ExistsContainer( getExists( searchAttribute ) ) );
                        default:
                            break;
                    }
                    break;
                default:
                    if ( searchAttribute.getOutputKeys( ).size( ) == 1 )
                    {
                        switch( searchAttribute.getTreatmentType( ) )
                        {
                            case DIFFERENT:
                                mustNot.add( new MatchContainer( getMatch( searchAttribute ) ) );
                                break;
                            case STRICT:
                            case APPROXIMATED:
                                must.add( new MatchContainer( getMatch( searchAttribute ) ) );
                                break;
                            case ABSENT:
                                mustNot.add( new ExistsContainer( getExists( searchAttribute ) ) );
                            default:
                                break;
                        }
                    }
                    else
                    {
                        switch( searchAttribute.getTreatmentType( ) )
                        {
                            case DIFFERENT:
                                mustNot.add( new MultiMatchContainer( getMultiMatch( searchAttribute ) ) );
                                break;
                            case STRICT:
                            case APPROXIMATED:
                                must.add( new MultiMatchContainer( getMultiMatch( searchAttribute ) ) );
                                break;
                            case ABSENT:
                                mustNot.add( new ExistsContainer( getExists( searchAttribute ) ) );
                            default:
                                break;
                        }
                    }
                    break;
            }
        } );

        if ( this.isConnected( ) )
        {
            final Exists connectionId = new Exists( );
            connectionId.setField( "connectionId" );
            must.add( new ExistsContainer( connectionId ) );
            final Exists login = new Exists( );
            login.setField( "attributes.login.value" );
            must.add( new ExistsContainer( login ) );
        }

        if ( !must.isEmpty( ) )
        {
            bool.setMust( must );
        }
        if ( !mustNot.isEmpty( ) )
        {
            bool.setMustNot( mustNot );
        }

        query.setBool( bool );
        body.setQuery( query );
        return body;
    }

    public boolean isConnected( )
    {
        return connected;
    }
}
