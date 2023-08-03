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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class BasicSearchRequest extends ASearchRequest
{
    private boolean connected = false;

    public BasicSearchRequest( final List<SearchAttribute> attributes, final boolean connected )
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
        final ArrayList<AbstractContainer> must = new ArrayList<>( );
        this.getSearchAttributes( ).forEach( searchAttribute -> {
            switch( searchAttribute.getKey( ) )
            {
                case Constants.PARAM_FIRST_NAME:
                    if ( AttributeTreatmentType.STRICT.equals( searchAttribute.getTreatmentType( ) ) )
                    {
                        must.add( new MatchPhraseContainer( getMatchPhrase( searchAttribute ) ) );
                    }
                    else
                    {
                        must.add( new MatchContainer( getMatch( searchAttribute ) ) );
                    }

                default:
                    if ( searchAttribute.getOutputKeys( ).size( ) == 1 )
                    {
                        must.add( new MatchContainer( getMatch( searchAttribute ) ) );
                    }
                    else
                    {
                        must.add( new MultiMatchContainer( getMultiMatch( searchAttribute ) ) );
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

        bool.setMust( must );
        query.setBool( bool );
        body.setQuery( query );
        return body;
    }

    public boolean isConnected( )
    {
        return connected;
    }
}
