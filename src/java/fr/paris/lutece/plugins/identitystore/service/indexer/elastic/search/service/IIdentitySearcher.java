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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.service;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Result;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Shard;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Total;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IIdentitySearcher
{
    Response multiSearch( final List<SearchAttribute> attributes, final List<List<SearchAttribute>> specialTreatmentAttributes, final Integer nbEqualAttributes,
            final Integer nbMissingAttributes, int max, boolean connected ) throws IdentityStoreException;

    Response search( final List<SearchAttribute> attributes, final int max, final boolean connected ) throws IdentityStoreException;

    Response search( final String customerId ) throws IdentityStoreException;

    default Response emptyResponse( )
    {
        final Response response = new Response( );
        final Result result = new Result( );
        result.setHits( new ArrayList<>( ) );
        result.setMaxScore( BigDecimal.ZERO );
        final Total total = new Total( );
        total.setValue( 0 );
        total.setRelation( "" );
        result.setTotal( total );
        response.setResult( result );
        final Shard shards = new Shard( );
        shards.setFailed( 0 );
        shards.setTotal( 0 );
        shards.setSkipped( 0 );
        shards.setSuccessful( 0 );
        response.setShards( shards );
        response.setTook( "" );
        response.setTimedOut( false );
        response.setStatus( 0 );
        response.setMetadata( new HashMap<>( ) );
        return response;
    }

    Response search( List<String> customerId ) throws IdentityStoreException;
}
