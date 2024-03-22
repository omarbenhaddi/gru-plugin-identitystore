/*
 * Copyright (c) 2002-2024, City of Paris
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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Responses;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * The Class ElasticConnexion.
 */
public final class ElasticConnexion
{
    private static final int CONNECTION_POOL_MAX_PER_ROUTE = AppPropertiesService.getPropertyInt( "identitystore.elastic.client.connection.pool.max.per.route",
            20 );
    private static final int CONNECTION_POOL_MAX_TOTAL = AppPropertiesService.getPropertyInt( "identitystore.elastic.client.connection.pool.max.total", 20 );
    private static final long RESPONSE_TIMEOUT = AppPropertiesService.getPropertyInt( "identitystore.elastic.client.response.timeout", 30 );
    private static final long CONNECT_TIMEOUT = AppPropertiesService.getPropertyInt( "identitystore.elastic.client.connect.timeout", 30 );
    private static final ObjectMapper _mapper = new ObjectMapper( ).disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    private final CloseableHttpClient _httpClient;
    private final AbstractHttpClientResponseHandler<String> _simpleResponseHandler = buildSimpleResponseHandler( );
    private final AbstractHttpClientResponseHandler<Response> _searchResponseHandler = buildSearchResponseHandler( );
    private final AbstractHttpClientResponseHandler<Responses> _mSearchResponseHandler = buildSearchesResponseHandler( );
    private String _userLogin;
    private String _userPassword;

    /**
     * Basic Authentification constructor
     *
     * @param userLogin
     *            Login
     * @param userPassword
     *            Password
     */
    public ElasticConnexion( final String userLogin, final String userPassword )
    {
        _userLogin = userLogin;
        _userPassword = userPassword;
        _httpClient = buildHttpClient( );
    }

    /**
     * Constructor
     */
    public ElasticConnexion( )
    {
        _httpClient = buildHttpClient( );
    }

    /**
     * Send a GET request to Elastic Search server
     *
     * @param strURI
     *            The URI
     * @return The response
     */
    public String GET( final String strURI ) throws IOException
    {
        return _httpClient.execute( new HttpGet( strURI ), _simpleResponseHandler );
    }

    /**
     * Send a PUT request to Elastic Search server
     *
     * @param strURI
     *            the uri
     * @param strJSON
     *            the json
     * @return the string
     */
    public void PUT( final String strURI, final String strJSON ) throws IOException
    {
        final HttpPut request = new HttpPut( strURI );
        request.setEntity( new StringEntity( strJSON, ContentType.APPLICATION_JSON, null, false ) );
        this._httpClient.execute( request );
    }

    /**
     * Send a POST request to Elastic Search server
     *
     * @param strURI
     *            the uri
     * @param strJSON
     *            the json
     * @return the string
     */
    public void POST( final String strURI, final String strJSON ) throws IOException
    {
        final HttpPost request = new HttpPost( strURI );
        request.setEntity( new StringEntity( strJSON, ContentType.APPLICATION_JSON, null, false ) );
        this._httpClient.execute( request );
    }

    /**
     * Send a POST request to Elastic Search server
     *
     * @param strURI
     *            the uri
     * @param strJSON
     *            the json
     * @return the string
     */
    public Response SEARCH( final String strURI, final String strJSON ) throws IOException
    {
        final HttpGet request = new HttpGet( strURI );
        request.setEntity( new StringEntity( strJSON, ContentType.APPLICATION_JSON, null, false ) );
        return this._httpClient.execute( request, _searchResponseHandler );
    }

    /**
     * Send a POST request to Elastic Search server
     *
     * @param strURI
     *            the uri
     * @param strJSON
     *            the json
     * @return the string
     */
    public Responses MSEARCH( final String strURI, final String strJSON ) throws IOException
    {
        final HttpGet request = new HttpGet( strURI );
        request.setEntity( new StringEntity( strJSON, ContentType.APPLICATION_JSON, null, false ) );
        return this._httpClient.execute( request, _mSearchResponseHandler );
    }

    /**
     * Send a DELETE request to Elastic Search server
     *
     * @param strURI
     *            the uri
     * @return the string
     */
    public void DELETE( final String strURI ) throws IOException
    {
        _httpClient.execute( new HttpDelete( strURI ) );
    }

    private static AbstractHttpClientResponseHandler<String> buildSimpleResponseHandler( )
    {
        return new AbstractHttpClientResponseHandler<String>( )
        {
            @Override
            public String handleEntity( HttpEntity httpEntity ) throws IOException
            {

                try
                {
                    final String response = EntityUtils.toString( httpEntity );
                    EntityUtils.consume( httpEntity );
                    return response;
                }
                catch( ParseException var3 )
                {
                    throw new ClientProtocolException( var3 );
                }
            }
        };
    }

    private static AbstractHttpClientResponseHandler<Response> buildSearchResponseHandler( )
    {
        return new AbstractHttpClientResponseHandler<Response>( )
        {
            @Override
            public Response handleEntity( HttpEntity httpEntity ) throws IOException
            {
                final Response response = _mapper.readValue( httpEntity.getContent( ), Response.class );
                EntityUtils.consume( httpEntity );
                return response;
            }
        };
    }

    private static AbstractHttpClientResponseHandler<Responses> buildSearchesResponseHandler( )
    {
        return new AbstractHttpClientResponseHandler<Responses>( )
        {
            @Override
            public Responses handleEntity( HttpEntity httpEntity ) throws IOException
            {
                final Responses response = _mapper.readValue( httpEntity.getContent( ), Responses.class );
                EntityUtils.consume( httpEntity );
                return response;
            }
        };
    }

    private CloseableHttpClient buildHttpClient( )
    {
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager( );
        connManager.setDefaultMaxPerRoute( CONNECTION_POOL_MAX_PER_ROUTE );
        connManager.setMaxTotal( CONNECTION_POOL_MAX_TOTAL );
        final RequestConfig requestConfig = RequestConfig.custom( ).setResponseTimeout( Timeout.ofSeconds( RESPONSE_TIMEOUT ) )
                .setConnectTimeout( Timeout.ofSeconds( CONNECT_TIMEOUT ) ).setConnectionRequestTimeout( Timeout.ofSeconds( CONNECT_TIMEOUT ) ).build( );
        return HttpClients.custom( ).setConnectionManager( connManager ).setConnectionManagerShared( true ).setDefaultHeaders( this.buildDefaultHeaders( ) )
                .setDefaultRequestConfig( requestConfig ).build( );
    }

    private List<Header> buildDefaultHeaders( )
    {
        final List<Header> defaultHeaders = new ArrayList<>( );
        if ( StringUtils.isNoneEmpty( _userLogin, _userPassword ) )
        {
            final String auth = _userLogin + ":" + _userPassword;
            final byte [ ] encodedAuth = Base64.getEncoder( ).encode( auth.getBytes( StandardCharsets.ISO_8859_1 ) );
            final String authHeader = "Basic " + new String( encodedAuth );
            defaultHeaders.add( new BasicHeader( HttpHeaders.AUTHORIZATION, authHeader ) );
        }
        return defaultHeaders;
    }
}
