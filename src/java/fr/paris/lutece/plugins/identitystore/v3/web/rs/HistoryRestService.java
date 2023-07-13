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

import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.IdentityChange;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.swagger.SwaggerConstants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.HISTORY_PATH )
@Api( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.HISTORY_PATH )
public class HistoryRestService
{

    private static final String ERROR_NO_IDENTITY_FOUND = "No identity found";
    private static final String ERROR_NO_SERVICE_CONTRACT_FOUND = "No service contract found";
    private static final String ERROR_DURING_TREATMENT = "An error occurred during the treatment.";

    /**
     * Default constructor.
     */
    public HistoryRestService( )
    {
    }

    /**
     * Gives the identity history (identity+attributes) from a customerID
     *
     * @param strCustomerId
     *            customerID
     * @param strHeaderClientAppCode
     *            client code
     * @return the history
     */
    @GET
    @Path( "{" + Constants.PARAM_ID_CUSTOMER + "}" )
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get an identity history by its customer ID (CUID)", response = IdentityHistory.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity history Found" ),
            @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ), @ApiResponse( code = 403, message = "Failure" ),
            @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND ), @ApiResponse( code = 404, message = ERROR_NO_SERVICE_CONTRACT_FOUND )
    } )
    public Response getHistory(
            @ApiParam( name = Constants.PARAM_ID_CUSTOMER, value = "Customer ID of the requested identity" ) @PathParam( Constants.PARAM_ID_CUSTOMER ) String strCustomerId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.CLIENT_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode )
    {
        String strClientAppCode = IdentityStoreService.getTrustedClientCode( strHeaderClientAppCode, StringUtils.EMPTY );
        try
        {
            final Identity identity = IdentityHome.findByCustomerId( strCustomerId );
            if ( identity == null )
            {
                return buildResponse( ERROR_NO_IDENTITY_FOUND, Response.Status.NOT_FOUND );
            }
            final ServiceContract serviceContract = ServiceContractService.instance( ).getActiveServiceContract( strClientAppCode );
            if ( serviceContract == null )
            {
                return buildResponse( ERROR_NO_SERVICE_CONTRACT_FOUND, Response.Status.NOT_FOUND );
            }
            final Set<String> readableAttributeKeys = serviceContract.getAttributeRights( ).stream( ).filter( AttributeRight::isReadable )
                    .map( ar -> ar.getAttributeKey( ).getKeyName( ) ).collect( Collectors.toSet( ) );

            final List<IdentityChange> identityChangeList = IdentityHome.findHistoryByCustomerId( strCustomerId );
            final List<AttributeChange> attributeChangeList = IdentityAttributeHome.getAttributeChangeHistory( identity.getId( ) );

            final IdentityHistory history = new IdentityHistory( );
            history.setCustomerId( identity.getCustomerId( ) );
            identityChangeList.forEach( ic -> history.getIdentityChanges( ).add( IdentityHistoryMapper.toJsonIdentityChange( ic ) ) );
            attributeChangeList.stream( ).filter( ac -> readableAttributeKeys.contains( ac.getAttributeKey( ) ) )
                    .collect( Collectors.groupingBy( AttributeChange::getAttributeKey ) ).forEach( ( key, attributeChanges ) -> {
                        final AttributeHistory attributeHistory = new AttributeHistory( );
                        attributeHistory.setAttributeKey( key );
                        attributeHistory.setAttributeChanges( attributeChanges );
                        history.getAttributeHistories( ).add( attributeHistory );
                    } );

            return Response.status( Response.Status.OK ).entity( history ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( final Exception e )
        {
            AppLogService.error( "Error while fetching identity history.", e );
            return buildResponse( ERROR_DURING_TREATMENT + " : " + e.getMessage( ), Response.Status.BAD_REQUEST );
        }
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
    private Response buildResponse( final String strMessage, final Response.StatusType status )
    {
        final ResponseDto response = new ResponseDto( );
        response.setStatus( status.toString( ) );
        response.setMessage( strMessage );
        return Response.status( status ).type( MediaType.APPLICATION_JSON ).entity( response ).build( );
    }

}
