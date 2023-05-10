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

import fr.paris.lutece.plugins.identitystore.service.application.ClientNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.request.referentiel.LevelListGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.referentiel.ProcessusListGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.LevelSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.ProcessusSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * ReferentialRestService
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.REFERENTIAL_PATH )
@Api( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.REFERENTIAL_PATH )
public class ReferentielRestService
{
    private static final String ERROR_NO_SERVICE_CONTRACT_FOUND = "No service contract found";
    private static final String ERROR_DURING_TREATMENT = "An error occurred during the treatment.";

    /**
     * Get Processus
     *
     * @return the Client
     */
    @Path( Constants.REFERENTIAL_PROCESSUS_PATH )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get all processus existing in the identity store referential", response = ProcessusSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getAllProcessus( )
    {
        try
        {
            final ProcessusListGetRequest request = new ProcessusListGetRequest( null );
            final ProcessusSearchResponse entity = (ProcessusSearchResponse) request.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Get Levels
     *
     * @return the Client
     */
    @Path( Constants.REFERENTIAL_LEVEL_PATH )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get the active service contract associated to the given application client code", response = LevelSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getAllLevels( )
    {
        try
        {
            final LevelListGetRequest request = new LevelListGetRequest( null );
            final LevelSearchResponse entity = (LevelSearchResponse) request.doRequest( );
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
