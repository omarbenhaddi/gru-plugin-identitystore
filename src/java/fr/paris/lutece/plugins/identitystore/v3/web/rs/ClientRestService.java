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
package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.service.application.ClientNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientsGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientsSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.swagger.SwaggerConstants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * ClientRest
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
@Api( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
public class ClientRestService
{
    private static final String ERROR_NO_SERVICE_CONTRACT_FOUND = "No service contract found";
    private static final String ERROR_DURING_TREATMENT = "An error occurred during the treatment.";

    /**
     * Get Clients
     *
     * @param applicationCode
     *            client code
     * @return the Client
     */
    @Path( Constants.CLIENTS_PATH + "/{" + Constants.PARAM_APPLICATION_CODE + "}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get the active service contract associated to the given application client code", response = ClientSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getClients(
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.CLIENT_APPLICATION_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_APPLICATION_CODE ) String applicationCode )
    {
        try
        {
            final ClientsGetRequest request = new ClientsGetRequest( null, applicationCode );
            final ClientsSearchResponse entity = (ClientsSearchResponse) request.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Get Client
     *
     * @param clientCode
     *            client code
     * @return the Client
     */
    @Path( Constants.CLIENT_PATH + "/{" + Constants.PARAM_CLIENT_CODE + "}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get the active service contract associated to the given application client code", response = ClientSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getClient(
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_CLIENT_CODE ) String clientCode )
    {
        try
        {
            final String trustedCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
            final ClientGetRequest request = new ClientGetRequest( trustedCode );
            final ClientSearchResponse entity = (ClientSearchResponse) request.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
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
    public Response createClient( @ApiParam( name = "Request body", value = "An Identity Change Request" ) ClientApplicationDto clientDto )
    {
        try
        {
            final ClientCreateRequest identityStoreRequest = new ClientCreateRequest( clientDto, null );
            final ClientChangeResponse entity = (ClientChangeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
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
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_CLIENT_CODE ) String clientCode )
    {
        try
        {
            final ClientUpdateRequest identityStoreRequest = new ClientUpdateRequest( clientDto, clientCode );
            final ClientChangeResponse entity = (ClientChangeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * build error response from exception
     *
     * @param exception
     *            the exception
     * @return ResponseDto from exception
     */
    private Response getErrorResponse( Exception exception )
    {
        // For security purpose, send a generic message
        String strMessage;
        Response.StatusType status;

        AppLogService.error( "IdentityStoreRestService getErrorResponse : " + exception, exception );

        if ( exception instanceof ClientNotFoundException )
        {
            strMessage = ERROR_NO_SERVICE_CONTRACT_FOUND;
            status = Response.Status.NOT_FOUND;
        }
        else
        {
            strMessage = ERROR_DURING_TREATMENT + " : " + exception.getMessage( );
            status = Response.Status.BAD_REQUEST;
        }

        return buildResponse( strMessage, status );
    }

    /**
     * Builds a {@code Response} object from the specified message and status
     *
     * @param strMessage
     *            the message
     * @param status
     *            the status
     * @return the {@code Response} object
     */
    private Response buildResponse( String strMessage, Response.StatusType status )
    {
        final ResponseDto response = new ResponseDto( );
        response.setStatus( status.toString( ) );
        response.setMessage( strMessage );
        return Response.status( status ).type( MediaType.APPLICATION_JSON ).entity( response ).build( );
    }
}
