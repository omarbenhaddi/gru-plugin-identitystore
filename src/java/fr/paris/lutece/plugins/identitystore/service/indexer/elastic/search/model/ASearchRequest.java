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

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.InnerSearchRequest;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.Match;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.MatchPhrase;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.MultiMatch;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ASearchRequest
{
    protected List<SearchAttribute> searchAttributes = new ArrayList<>( );

    public abstract InnerSearchRequest body( );

    protected Match getMatch( final SearchAttribute attribute )
    {
        final Match match = new Match( );
        match.setName( "attributes." + attribute.getOutputKeys( ).get( 0 ) + ".value" );
        match.setQuery( attribute.getValue( ) );
        if ( AttributeTreatmentType.APPROXIMATED.equals( attribute.getTreatmentType( ) ) )
        {
            match.setFuzziness( "1" );
        }
        return match;
    }

    protected MultiMatch getMultiMatch( final SearchAttribute attribute )
    {
        final MultiMatch match = new MultiMatch( );
        match.setFields( attribute.getOutputKeys( ).stream( ).map( outputKey -> "attributes." + outputKey + ".value" ).collect( Collectors.toList( ) ) );
        match.setQuery( attribute.getValue( ) );
        if ( AttributeTreatmentType.APPROXIMATED.equals( attribute.getTreatmentType( ) ) )
        {
            match.setFuzziness( "1" );
        }
        return match;
    }

    protected MatchPhrase getMatchPhrase( final SearchAttribute attribute )
    {
        final MatchPhrase match = new MatchPhrase( );
        match.setName( "attributes." + attribute.getOutputKeys( ).get( 0 ) + ".value" );
        match.setQuery( attribute.getValue( ) );
        return match;
    }

    public List<SearchAttribute> getSearchAttributes( )
    {
        return searchAttributes;
    }
}
