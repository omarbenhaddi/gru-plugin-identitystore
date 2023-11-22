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
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ActiveServiceContractGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractListGetAllRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractListGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractPutEndDateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractsSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.swagger.SwaggerConstants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static fr.paris.lutece.plugins.identitystore.v3.web.rs.error.UncaughtServiceContractNotFoundExceptionMapper.ERROR_NO_SERVICE_CONTRACT_FOUND;
import static fr.paris.lutece.plugins.rest.service.mapper.GenericUncaughtExceptionMapper.ERROR_DURING_TREATMENT;

/**
 * ServiceContractRest
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
@Api( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
public class ServiceContractRestService
{

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
            @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response getAllServiceContractList(
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        // TODO paginer l'appel
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ServiceContractListGetAllRequest request = new ServiceContractListGetAllRequest( trustedClientCode, authorName, authorType );
        final ServiceContractsSearchResponse entity = (ServiceContractsSearchResponse) request.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Get ServiceContract List
     *
     * @param headerClientCode
     *            client code
     * @return the ServiceContract
     */
    @Path( Constants.SERVICECONTRACTS_PATH + "/{" + Constants.PARAM_TARGET_CLIENT_CODE + "}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get all service contract associated to the given client code", response = ServiceContractsSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Service contract Found" ),
            @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ), @ApiResponse( code = 403, message = "Failure" ),
            @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response getServiceContractList(
            @ApiParam( name = Constants.PARAM_TARGET_CLIENT_CODE, value = SwaggerConstants.PARAM_TARGET_CLIENT_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_TARGET_CLIENT_CODE ) String strTargetClientCode,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String headerClientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedHeaderClientCode = IdentityStoreService.getTrustedClientCode( headerClientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ServiceContractListGetRequest request = new ServiceContractListGetRequest( strTargetClientCode, trustedHeaderClientCode, authorName, authorType );
        final ServiceContractsSearchResponse entity = (ServiceContractsSearchResponse) request.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Get ServiceContract
     *
     * @param headerClientCode
     *            client code
     * @return the ServiceContract
     */
    @Path( Constants.ACTIVE_SERVICE_CONTRACT_PATH + "/{" + Constants.PARAM_TARGET_CLIENT_CODE + "}" )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get the active service contract associated to the given client code", response = ServiceContractSearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Service contract Found" ),
            @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ), @ApiResponse( code = 403, message = "Failure" ),
            @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response getActiveServiceContract(
            @ApiParam( name = Constants.PARAM_TARGET_CLIENT_CODE, value = SwaggerConstants.PARAM_TARGET_CLIENT_CODE_DESCRIPTION ) @PathParam( Constants.PARAM_TARGET_CLIENT_CODE ) String strTargetClientCode,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String headerClientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedHeaderClientCode = IdentityStoreService.getTrustedClientCode( headerClientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ActiveServiceContractGetRequest request = new ActiveServiceContractGetRequest( strTargetClientCode, trustedHeaderClientCode, authorName,
                authorType );
        final ServiceContractSearchResponse entity = (ServiceContractSearchResponse) request.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
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
            @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response getServiceContract(
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_ID_SERVICE_CONTRACT, value = "ID of the searched contract" ) @PathParam( Constants.PARAM_ID_SERVICE_CONTRACT ) Integer serviceContractId,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ServiceContractGetRequest request = new ServiceContractGetRequest( trustedClientCode, serviceContractId, authorName, authorType );
        final ServiceContractSearchResponse entity = (ServiceContractSearchResponse) request.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
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
    public Response createServiceContract( @ApiParam( name = "Request body", value = "A service contract creation Request" ) ServiceContractDto serviceContract,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ServiceContractCreateRequest identityStoreRequest = new ServiceContractCreateRequest( serviceContract, trustedClientCode, authorName,
                authorType );
        final ServiceContractChangeResponse entity = (ServiceContractChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
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
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 409, message = "Conflict" ),
            @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response updateServiceContract( @ApiParam( name = "Request body", value = "A service contract change Request" ) ServiceContractDto serviceContract,
            @ApiParam( name = Constants.PARAM_ID_SERVICE_CONTRACT, value = "ID of the updated contract" ) @PathParam( Constants.PARAM_ID_SERVICE_CONTRACT ) Integer serviceContractId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ServiceContractUpdateRequest identityStoreRequest = new ServiceContractUpdateRequest( serviceContract, trustedClientCode, serviceContractId,
                authorName, authorType );
        final ServiceContractChangeResponse entity = (ServiceContractChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
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
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 409, message = "Conflict" ),
            @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response closeServiceContract( @ApiParam( name = "Request body", value = "An Identity Change Request" ) ServiceContractDto serviceContract,
            @ApiParam( name = Constants.PARAM_ID_SERVICE_CONTRACT, value = "ID of the updated contract" ) @PathParam( Constants.PARAM_ID_SERVICE_CONTRACT ) Integer serviceContractId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType,
            @ApiParam( name = Constants.PARAM_APPLICATION_CODE, value = SwaggerConstants.PARAM_APPLICATION_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_APPLICATION_CODE ) @DefaultValue( "" ) String strHeaderAppCode )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY, strHeaderAppCode );
        final ServiceContractPutEndDateRequest identityStoreRequest = new ServiceContractPutEndDateRequest( serviceContract, trustedClientCode,
                serviceContractId, authorName, authorType );
        final ServiceContractChangeResponse entity = (ServiceContractChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

}
