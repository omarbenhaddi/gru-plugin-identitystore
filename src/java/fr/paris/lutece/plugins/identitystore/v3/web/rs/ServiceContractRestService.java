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
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractsSearchResponse;
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
 * ServiceContractRest
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
@Api( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
public class ServiceContractRestService
{
    private static final String ERROR_NO_SERVICE_CONTRACT_FOUND = "No service contract found";
    private static final String ERROR_DURING_TREATMENT = "An error occurred during the treatment.";

    /**
     * Get ServiceContract List
     *
     * @param clientCode
     *            client code
     * @return the ServiceContract
     */
    @Path( Constants.SERVICECONTRACTS_PATH )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get all service contract associated to the given client code", response = ServiceContractsSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Service contract Found" ),
            @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ), @ApiResponse( code = 403, message = "Failure" ),
            @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getServiceContractList(
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode )
    {
        try
        {
            final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
            final ServiceContractListGetRequest request = new ServiceContractListGetRequest( trustedClientCode );
            final ServiceContractsSearchResponse entity = (ServiceContractsSearchResponse) request.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Get ServiceContract
     *
     * @param clientCode
     *            client code
     * @return the ServiceContract
     */
    @Path( Constants.SERVICECONTRACT_PATH )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get the active service contract associated to the given client code", response = ServiceContractSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Service contract Found" ),
            @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ), @ApiResponse( code = 403, message = "Failure" ),
            @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getActiveServiceContract(
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode )
    {
        try
        {
            final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
            final ActiveServiceContractGetRequest request = new ActiveServiceContractGetRequest( trustedClientCode );
            final ServiceContractSearchResponse entity = (ServiceContractSearchResponse) request.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Get ServiceContract
     *
     * @param clientCode
     *            client code
     * @param serviceContractId
     *            service contract ID
     * @return the ServiceContract
     */
    @Path( Constants.SERVICECONTRACT_PATH + "/{" + Constants.PARAM_ID_SERVICE_CONTRACT + "}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get the service contract associated to the given ID and application client code", response = ServiceContractSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Service contract Found" ),
            @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ), @ApiResponse( code = 403, message = "Failure" ),
            @ApiResponse( code = 404, message = "No service contract found" )
    } )
    public Response getServiceContract(
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_ID_SERVICE_CONTRACT, value = "ID of the searched contract" ) @PathParam( Constants.PARAM_ID_SERVICE_CONTRACT ) Integer serviceContractId )
    {
        try
        {
            final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
            final ServiceContractGetRequest request = new ServiceContractGetRequest( trustedClientCode, serviceContractId );
            final ServiceContractSearchResponse entity = (ServiceContractSearchResponse) request.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Creates a service contract.<br/>
     * The service contract is created from the provided {@link ServiceContractDto}.
     *
     * @param serviceContract
     *            the service contract to create
     * @param clientCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link ServiceContractChangeResponse}
     */
    @Path( Constants.SERVICECONTRACT_PATH )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Create a new Service Contract associated with the given client code", response = ServiceContractChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response createServiceContract( @ApiParam( name = "Request body", value = "An Identity Change Request" ) ServiceContractDto serviceContract,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode )
    {
        try
        {
            final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
            final ServiceContractCreateRequest identityStoreRequest = new ServiceContractCreateRequest( serviceContract, trustedClientCode );
            final ServiceContractChangeResponse entity = (ServiceContractChangeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Updates a service contract.<br/>
     * The service contract is updated from the provided {@link ServiceContractDto}.
     *
     * @param serviceContract
     *            the service contract to update
     * @param serviceContractId
     *            the service contract ID
     * @param clientCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link ServiceContractChangeResponse}
     */
    @PUT
    @Path( Constants.SERVICECONTRACT_PATH + "/{" + Constants.PARAM_ID_SERVICE_CONTRACT + "}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Update an existing Service Contract", response = ServiceContractChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response updateServiceContract( @ApiParam( name = "Request body", value = "An Identity Change Request" ) ServiceContractDto serviceContract,
            @ApiParam( name = Constants.PARAM_ID_SERVICE_CONTRACT, value = "ID of the updated contract" ) @PathParam( Constants.PARAM_ID_SERVICE_CONTRACT ) Integer serviceContractId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode )
    {
        try
        {
            final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
            final ServiceContractUpdateRequest identityStoreRequest = new ServiceContractUpdateRequest( serviceContract, trustedClientCode, serviceContractId );
            final ServiceContractChangeResponse entity = (ServiceContractChangeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Closes a service contract by specifying an end date.<br/>
     * The service contract is updated from the provided {@link ServiceContractDto}.
     *
     * @param serviceContract
     *            the service contract to update
     * @param serviceContractId
     *            the service contract ID
     * @param clientCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link ServiceContractChangeResponse}
     */
    @PUT
    @Path( Constants.SERVICECONTRACT_PATH + "/{" + Constants.PARAM_ID_SERVICE_CONTRACT + "}" + Constants.SERVICECONTRACT_END_DATE_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Patches the end date of the service contract", response = ServiceContractChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response closeServiceContract( @ApiParam( name = "Request body", value = "An Identity Change Request" ) ServiceContractDto serviceContract,
            @ApiParam( name = Constants.PARAM_ID_SERVICE_CONTRACT, value = "ID of the updated contract" ) @PathParam( Constants.PARAM_ID_SERVICE_CONTRACT ) Integer serviceContractId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode )
    {
        try
        {
            final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
            final ServiceContractPutEndDateRequest identityStoreRequest = new ServiceContractPutEndDateRequest( serviceContract, trustedClientCode,
                    serviceContractId );
            final ServiceContractChangeResponse entity = (ServiceContractChangeResponse) identityStoreRequest.doRequest( );
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

        if ( exception instanceof ServiceContractNotFoundException )
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
