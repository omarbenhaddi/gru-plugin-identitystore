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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClient;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.ASearchRequest;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.ComplexSearchRequest;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.CustomerIdSearchRequest;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.InnerSearchRequest;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.MultiSearchAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.MultiSearchActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Hit;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Responses;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Result;
import fr.paris.lutece.plugins.identitystore.utils.Combinations;
import fr.paris.lutece.plugins.identitystore.utils.Maps;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IdentitySearcher implements IIdentitySearcher
{

    public static final String IDENTITYSTORE_SEARCH_OFFSET = "identitystore.search.offset";
    private static final String INDEX = "identities-alias";
    final private static int propertySize = AppPropertiesService.getPropertyInt( IDENTITYSTORE_SEARCH_OFFSET, 10 );
    private final ElasticClient _elasticClient;
    private final ObjectMapper objectMapper = new ObjectMapper( );

    public IdentitySearcher( String strServerUrl, String strLogin, String strPassword )
    {
        this._elasticClient = new ElasticClient( strServerUrl, strLogin, strPassword );
    }

    public IdentitySearcher( String strServerUrl )
    {
        this._elasticClient = new ElasticClient( strServerUrl );
    }

    /**
     * Perform a multi search by generating several requests according to parameters.
     * 
     * @param attributes
     *            the complete list of {@link SearchAttribute} defined in the client request
     * @param specialTreatmentAttributes
     *            a list of particular treatment on given attributes over attributes param
     * @param nbEqualAttributes
     *            number of attributes that must be strictly equal
     * @param nbMissingAttributes
     *            number of attributes that can be missing
     * @param max
     *            maximum number of identities in the response
     * @param connected
     *            search for a connected {@link IdentityDto} if true
     * @return a list of {@link IdentityDto} matching the requests
     */
    @Override
    public Response multiSearch( final List<SearchAttribute> attributes, final List<List<SearchAttribute>> specialTreatmentAttributes,
            final Integer nbEqualAttributes, final Integer nbMissingAttributes, final int max, final boolean connected ) throws IdentityStoreException
    {
        final List<ASearchRequest> requests = new ArrayList<>( );
        /* Split attribute list into N combinations (list) of nbEqualAttributes */
        final List<List<SearchAttribute>> combinationsOfEqualAttributes = Combinations.combinations( attributes, nbEqualAttributes );
        for ( final List<SearchAttribute> currentCombinationOfEqualAttributes : combinationsOfEqualAttributes )
        {
            /* if Attribute treatments are defined, generate a request for each */
            if ( !CollectionUtils.isEmpty( specialTreatmentAttributes ) )
            {
                final List<List<SearchAttribute>> eligibleNuples = specialTreatmentAttributes
                        .stream( ).filter(
                                nuple -> nuple.stream( )
                                        .noneMatch( nupleAttribute -> currentCombinationOfEqualAttributes.stream( )
                                                .anyMatch( equal -> Objects.equals( equal.getKey( ), nupleAttribute.getKey( ) ) ) ) )
                        .collect( Collectors.toList( ) );
                for ( final List<SearchAttribute> eligibleNuple : eligibleNuples )
                {
                    final List<SearchAttribute> allAttributes = Stream.concat( currentCombinationOfEqualAttributes.stream( ), eligibleNuple.stream( ) )
                            .collect( Collectors.toList( ) );
                    requests.addAll( this.generateRequestsWithOrWithoutMissingAttributes( attributes, nbMissingAttributes, connected, allAttributes ) );
                }
            }
            else
            {
                requests.addAll( this.generateRequestsWithOrWithoutMissingAttributes( attributes, nbMissingAttributes, connected,
                        currentCombinationOfEqualAttributes ) );
            }
        }
        return this.getResponse( requests, max );
    }

    /**
     * Handle the possibility of having missing attributes.<br>
     * If @param nbMissingAttributes is > 0, generate all combined requests, else, generate single request.
     * 
     * @param baseAttributes
     *            the complete list of {@link SearchAttribute} defined in the client request
     * @param nbMissingAttributes
     *            can be >= 0
     * @param connected
     *            search for a connected {@link IdentityDto}
     * @param workingAttributes
     *            the current list of attributes considered for the requests
     * @return
     */
    private List<ASearchRequest> generateRequestsWithOrWithoutMissingAttributes( final List<SearchAttribute> baseAttributes, final Integer nbMissingAttributes,
            final boolean connected, final List<SearchAttribute> workingAttributes )
    {
        final List<ASearchRequest> requests = new ArrayList<>( );
        if ( nbMissingAttributes != null && nbMissingAttributes > 0 )
        {
            final List<SearchAttribute> missingAttributes = baseAttributes.stream( )
                    .filter( attribute -> workingAttributes.stream( ).noneMatch( a -> Objects.equals( a.getKey( ), attribute.getKey( ) ) ) )
                    .collect( Collectors.toList( ) );
            final List<List<SearchAttribute>> combinationsOfMissingAttributes = Combinations.combinations( missingAttributes, nbMissingAttributes );
            for ( final List<SearchAttribute> combinationOfMissingAttributes : combinationsOfMissingAttributes )
            {
                final List<SearchAttribute> missingSearchAttributes = combinationOfMissingAttributes.stream( )
                        .map( attribute -> new SearchAttribute( attribute.getKey( ), attribute.getValue( ), attribute.getOutputKeys( ),
                                AttributeTreatmentType.ABSENT ) )
                        .collect( Collectors.toList( ) );
                final List<SearchAttribute> complete = Stream.concat( missingSearchAttributes.stream( ), workingAttributes.stream( ) )
                        .collect( Collectors.toList( ) );
                requests.add( new ComplexSearchRequest( complete, connected ) );
            }
        }
        else
        {
            requests.add( new ComplexSearchRequest( workingAttributes, connected ) );
        }
        return requests;
    }

    @Override
    public Response search( final List<SearchAttribute> attributes, final int max, final boolean connected ) throws IdentityStoreException
    {
        final ASearchRequest request = new ComplexSearchRequest( attributes, connected );
        return this.getResponse( request, max );
    }

    @Override
    public Response search( final String customerId ) throws IdentityStoreException
    {
        final ASearchRequest request = new CustomerIdSearchRequest( customerId );
        return this.getResponse( request, 0 );
    }

    @Override
    public Response search( final List<String> customerId ) throws IdentityStoreException
    {
        final List<ASearchRequest> request = customerId.stream( ).map( CustomerIdSearchRequest::new ).collect( Collectors.toList( ) );
        return this.getResponse( request, 0 );
    }

    private Response getResponse( final ASearchRequest request, final int max ) throws IdentityStoreException
    {
        try
        {
            final InnerSearchRequest initialRequest = request.body( );
            final int size = ( max == 0 ) ? propertySize : Math.min( max, propertySize );
            initialRequest.setFrom( 0 );
            initialRequest.setSize( size );
            final String response = this._elasticClient.search( INDEX, initialRequest );
            final Response innerResponse = objectMapper.readValue( response, Response.class );
            int total = innerResponse.getResult( ).getTotal( ).getValue( );
            int limit = max == 0 ? total : Math.min( max, total );
            if ( size < limit )
            {
                int offset = initialRequest.getFrom( );
                while ( offset < limit )
                {
                    offset += size;
                    initialRequest.setFrom( offset );
                    final String subResponse = this._elasticClient.search( INDEX, initialRequest );
                    final Response pagedResponse = objectMapper.readValue( subResponse, Response.class );
                    innerResponse.getResult( ).getHits( ).addAll( pagedResponse.getResult( ).getHits( ) );
                    if ( pagedResponse.getResult( ).getMaxScore( ).compareTo( innerResponse.getResult( ).getMaxScore( ) ) > 0 )
                    {
                        innerResponse.getResult( ).setMaxScore( pagedResponse.getResult( ).getMaxScore( ) );
                    }
                }
            }
            return innerResponse;
        }
        catch( ElasticClientException | JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    private Response getResponse( final List<ASearchRequest> requests, final int max ) throws IdentityStoreException
    {
        try
        {
            if ( CollectionUtils.isNotEmpty( requests ) )
            {
                final int size = ( max == 0 ) ? propertySize : Math.min( max, propertySize );
                final List<MultiSearchAction> searchActions = requests.stream( ).map( aSearchRequest -> {
                    final InnerSearchRequest innerSearchRequest = aSearchRequest.body( );
                    innerSearchRequest.setFrom( 0 );
                    innerSearchRequest.setSize( size );
                    return new MultiSearchAction( innerSearchRequest, MultiSearchActionType.QUERY, INDEX );
                } ).collect( Collectors.toList( ) );
                final String response = this._elasticClient.multiSearch( INDEX, searchActions );
                final Responses innerResponses = objectMapper.readValue( response, Responses.class );
                final Response globalResponse = new Response( );
                globalResponse.setResult( new Result( ) );
                globalResponse.getResult( ).setHits( new ArrayList<>( ) );
                this.computeResponseMetadata( globalResponse, innerResponses, searchActions );

                final Map<String, Hit> hits = innerResponses.getResponses( ).stream( ).flatMap( r -> r.getResult( ).getHits( ).stream( ) ).distinct( )
                        .collect( Collectors.toMap( hit -> hit.getSource( ).getCustomerId( ), hit -> hit ) );
                final Map<String, Hit> distinctHits = new HashMap<>( hits );

                final int maxTotal = innerResponses.getResponses( ).stream( ).map( r -> r.getResult( ).getTotal( ).getValue( ) ).mapToInt( value -> value )
                        .max( ).orElseThrow( ( ) -> new IdentityStoreException( "Cannot compute total of hits" ) );
                final int limit = Math.min( max, maxTotal );
                int offset = searchActions.get( 0 ).getQuery( ).getFrom( );
                while ( offset < limit )
                {
                    offset += size;
                    int finalOffset = offset;
                    searchActions.forEach( multiSearchAction -> multiSearchAction.getQuery( ).setFrom( finalOffset ) );
                    final String pagedResponse = this._elasticClient.multiSearch( INDEX, searchActions );
                    final Responses pagedResponses = objectMapper.readValue( pagedResponse, Responses.class );
                    pagedResponses.getResponses( ).stream( ).flatMap( r -> r.getResult( ).getHits( ).stream( ) ).distinct( )
                            .forEach( hit -> distinctHits.putIfAbsent( hit.getSource( ).getCustomerId( ), hit ) );
                    final BigDecimal maxScore = pagedResponses.getResponses( ).stream( ).filter( r -> r.getResult( ).getMaxScore( ) != null )
                            .map( r -> r.getResult( ).getMaxScore( ) ).max( Comparator.comparingDouble( BigDecimal::doubleValue ) )
                            .orElseThrow( ( ) -> new IdentityStoreException( "Cannot compute max score" ) );
                    globalResponse.getResult( ).setMaxScore( maxScore );
                    this.computeResponseMetadata( globalResponse, pagedResponses, searchActions );
                }
                globalResponse.getResult( ).getHits( ).addAll( distinctHits.values( ) );
                return globalResponse;
            }
        }
        catch( final ElasticClientException | JsonProcessingException | IdentityStoreException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
        return emptyResponse( );
    }

    /**
     * Compute metada of global response to give information about what kind of requests had a match.
     * 
     * @param globalResponse
     *            the global {@link Response} that holds the entire multi search matches
     * @param innerResponses
     *            the multi search {@link Responses} in the same order as {@link MultiSearchAction} (ES gives responses in the same order as bulk requests)
     * @param searchActions
     *            the {@link MultiSearchAction} requests that we want to determine if they have matches or not
     */
    private void computeResponseMetadata( final Response globalResponse, final Responses innerResponses, final List<MultiSearchAction> searchActions )
    {
        for ( int index = 0; index < searchActions.size( ); index++ )
        {
            final Response response = innerResponses.getResponses( ).get( index );
            if ( response.getResult( ) != null && !response.getResult( ).getHits( ).isEmpty( ) )
            {
                Maps.mergeStringMap( globalResponse.getMetadata( ), searchActions.get( index ).getQuery( ).getMetadata( ) );
            }
        }
    }

}
