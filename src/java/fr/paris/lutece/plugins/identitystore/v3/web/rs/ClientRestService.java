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
package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientsGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientsSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.swagger.SwaggerConstants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static fr.paris.lutece.plugins.identitystore.v3.web.rs.error.UncaughtServiceContractNotFoundExceptionMapper.ERROR_NO_SERVICE_CONTRACT_FOUND;
import static fr.paris.lutece.plugins.rest.service.mapper.GenericUncaughtExceptionMapper.ERROR_DURING_TREATMENT;

/**
 * ClientRest
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
@Api( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
public class ClientRestService
{
    /**
     * Get Clients
     *
     * @param applicationCode
     *            application code
     * @return the Client
     */
    @Path( Constants.CLIENTS_PATH + "/{" + Constants.PARAM_APPLICATION_CODE + "}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get the active service contract associated to the given application client code", response = ClientSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response getClients(
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.CLIENT_APPLICATION_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_APPLICATION_CODE ) String applicationCode,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ClientsGetRequest request = new ClientsGetRequest( trustedClientCode, applicationCode, authorName, authorType );
        final ClientsSearchResponse entity = (ClientsSearchResponse) request.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Get Client
     *
     * @param targetClientCode
     *            target client code
     * @return the Client
     */
    @Path( Constants.CLIENT_PATH + "/{" + Constants.PARAM_TARGET_CLIENT_CODE + "}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get a client by its client code", response = ClientSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getClient(
            @ApiParam( name = Constants.PARAM_TARGET_CLIENT_CODE, value = SwaggerConstants.PARAM_TARGET_CLIENT_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_TARGET_CLIENT_CODE ) String targetClientCode,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String headerClientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedCode = IdentityStoreService.getTrustedClientCode( headerClientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ClientGetRequest request = new ClientGetRequest( targetClientCode, trustedCode, authorName, authorType );
        final ClientSearchResponse entity = (ClientSearchResponse) request.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Creates a service contract.<br/>
     * The service contract is created from the provided {@link ClientApplicationDto}.<br/>
     * <br/>
     * </ul>
     *
     * @param clientDto
     *            the service contract to create
     * @return http 200 if creation is ok with {@link ClientChangeResponse}
     */
    @POST
    @Path( Constants.CLIENT_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Create a new Service Contract", response = ClientChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response createClient( @ApiParam( name = "Request body", value = "An Identity Change Request" ) ClientApplicationDto clientDto,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ClientCreateRequest identityStoreRequest = new ClientCreateRequest( clientDto, trustedClientCode, authorName, authorType );
        final ClientChangeResponse entity = (ClientChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Updates a service contract.<br/>
     * The service contract is updated from the provided {@link ClientApplicationDto}.<br/>
     * <br/>
     * </ul>
     *
     * @param clientDto
     *            the service contract to update
     * @param clientCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link ClientChangeResponse}
     */
    @PUT
    @Path( Constants.CLIENT_PATH + "/{" + Constants.PARAM_CLIENT_CODE + "}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Update an existing Service Contract", response = ClientChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response updateClient( @ApiParam( name = "Request body", value = "An Identity Change Request" ) ClientApplicationDto clientDto,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final ClientUpdateRequest identityStoreRequest = new ClientUpdateRequest( clientDto, clientCode, authorName, authorType );
        final ClientChangeResponse entity = (ClientChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }
}
