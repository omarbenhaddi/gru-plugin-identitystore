/*
 * Copyright (c) 2002-2016, Mairie de Paris
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
package fr.paris.lutece.plugins.identitystore.web.rs;

import com.sun.jersey.api.client.ClientResponse.Status;

import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifierHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.service.formatter.FormatterJsonFactory;
import fr.paris.lutece.plugins.identitystore.service.formatter.FormatterXmlFactory;
import fr.paris.lutece.plugins.identitystore.service.formatter.IFormatterFactory;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.JsonIdentityParser;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppException;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * REST service for channel resource
 *
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.IDENTITY_PATH )
public class IdentityStoreRestService
{
    protected static final Map<String, IFormatterFactory> _formatterFactories = new HashMap<String, IFormatterFactory>(  );

    static
    {
        _formatterFactories.put( MediaType.APPLICATION_JSON, new FormatterJsonFactory(  ) );
        _formatterFactories.put( MediaType.APPLICATION_XML, new FormatterXmlFactory(  ) );
    }

    /**
     * Gives Identity
     * @param accept the accepted format
     * @param format the format
     * @param strConnectionId connection ID
     * @param strClientCode client code
     * @return the identity
     */
    @GET
    @Path( "{" + Constants.PARAM_ID_CONNECTION + "}" )
    public Response getAttributesByConnectionId( @HeaderParam( HttpHeaders.ACCEPT )
    String accept, @PathParam( Constants.PARAM_ID_CONNECTION )
    String strConnectionId, @QueryParam( Constants.FORMAT_QUERY )
    String format, @QueryParam( Constants.PARAM_CLIENT_CODE )
    String strClientCode )
    {
        String strMediaType = getMediaType( accept, format );

        IFormatterFactory formatterFactory = _formatterFactories.get( strMediaType );

        Identity identity = IdentityStoreService.getIdentity( strConnectionId, strClientCode );

        String strResponse = formatterFactory.createFormatter( IdentityDto.class )
                                             .format( IdentityRestUtil.convertToDto( identity ) );

        return Response.ok( strResponse, strMediaType ).build(  );
    }

    /**
     * Update identity
     * @param strConnectionId connection ID
     * @param strClientAppCode client code of the application which update attributes
     * @param strUserId userId of the user which update attributes
     * @param strCertifierCode certfierCode  which certifies modification
     * @param strJsonContent attributes to update
     * @return http 200 if update is ok
     */
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Path( "{" + Constants.PARAM_ID_CONNECTION + "}" )
    public Response putAttributesByConnectionId( @PathParam( Constants.PARAM_ID_CONNECTION )
    String strConnectionId, @QueryParam( Constants.PARAM_CLIENT_CODE )
    String strClientAppCode, @QueryParam( Constants.PARAM_USER_ID )
    String strUserId, @QueryParam( Constants.PARAM_CERTIFIER_ID )
    String strCertifierCode, String strJsonContent )
    {
        IdentityDto identityDto;
        IFormatterFactory formatterFactory = _formatterFactories.get( MediaType.APPLICATION_JSON );

        try
        {
            identityDto = getIdentityFromRequest( strConnectionId, strClientAppCode, strUserId, strJsonContent );
        }
        catch ( AppException appException )
        {
            ResponseDto responseDto = new ResponseDto(  );
            responseDto.setStatus( String.valueOf( Status.BAD_REQUEST ) );
            responseDto.setMessage( appException.getMessage( ) );
            String strResponse = formatterFactory.createFormatter( ResponseDto.class ).format( responseDto );

            return Response.status( Status.BAD_REQUEST ).type( MediaType.APPLICATION_JSON ).entity( strResponse ).build(  );
        }

        //TODO
        ChangeAuthor author = null;
        author = new ChangeAuthor(  );

        AttributeCertifier certifier = null;

        if ( StringUtils.isNotBlank( strCertifierCode ) )
        {
            certifier = AttributeCertifierHome.findByCode( strCertifierCode );
        }

        ResponseDto responseDto = updateAttributes( identityDto, strConnectionId, author, certifier );

        String strResponse = formatterFactory.createFormatter( ResponseDto.class ).format( responseDto );

        return Response.ok( strResponse, MediaType.APPLICATION_JSON ).build(  );
    }

    /**
     * check input parameters
     * @param strConnectionId connection id of identity to update
     * @param strClientAppCode client application code asking for modif
     * @param strUserId user id which makes modification
     * @param strJsonContent json content of request
     * @return identityDto parsed identityDto
     * @throws AppException if request is not correct
     *
     */
    private IdentityDto getIdentityFromRequest( String strConnectionId, String strClientAppCode, String strUserId,
        String strJsonContent ) throws AppException
    {
        if ( StringUtils.isBlank( strConnectionId ) )
        {
            throw new AppException( Constants.PARAM_ID_CONNECTION + " is missing" );
        }

        if ( StringUtils.isBlank( strClientAppCode ) )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " is missing" );
        }

        if ( StringUtils.isBlank( strUserId ) )
        {
            throw new AppException( Constants.PARAM_USER_ID + " is missing" );
        }

        //TODO check previous identities
        //Identity identityPrevious = IdentityStoreService.getIdentity( strConnectionId, strClientAppCode );
        IdentityDto identityDto = JsonIdentityParser.parse( strJsonContent );

        if ( ( identityDto.getAttributes(  ) == null ) || ( identityDto.getAttributes(  ).size(  ) == 0 ) )
        {
            throw new AppException( "no attribute to update" );
        }

        for ( AttributeDto attributeDto : identityDto.getAttributes(  ) )
        {
            if ( !isWritable( attributeDto, strClientAppCode ) )
            {
                throw new AppException( attributeDto.getKey(  ) + " not writable" );
            }
        }

        return identityDto;
    }

    /**
     * check if new identity attributes have errors and returns them
     * @param identityDto new identity to update
     * @param strConnectionId connectionId of identity which will be updated
     * @param author author responsible for modification
     * @param certifier certfier responsible for modification
     * @return responseDto response containings updated fields
     *
     */
    private ResponseDto updateAttributes( IdentityDto identityDto, String strConnectionId, ChangeAuthor author,
        AttributeCertifier certifier )
    {
        StringBuilder sb = new StringBuilder( "Fields successfully updated : " );

        //Remove all attributes ?
        for ( AttributeDto attributeDto : identityDto.getAttributes(  ) )
        {
            IdentityStoreService.setAttribute( strConnectionId, attributeDto.getKey(  ), attributeDto.getValue(  ),
                author, certifier );
            sb.append( attributeDto.getKey(  ) + "," );
        }

        ResponseDto response = new ResponseDto(  );
        response.setStatus( Constants.RESPONSE_OK );
        response.setMessage( sb.substring( 0, sb.length(  ) - 1 ) );

        return response;
    }

    /**
     * check if attribute is writable for provided client
     * @param attributeDto attribute
     * @param strClientAppCode client application code
     * @return true if attribute is writable
     */
    private boolean isWritable( AttributeDto attributeDto, String strClientAppCode )
    {
        boolean isAuthorized = false;
        List<AttributeRight> lstAppRight = ClientApplicationHome.selectApplicationRights( ClientApplicationHome.findByCode( 
                    strClientAppCode ) );

        for ( AttributeRight attrRight : lstAppRight )
        {
            if ( attrRight.getAttributeKey(  ).getKeyName(  ).equals( attributeDto.getKey(  ) ) &&
                    attrRight.isWritable(  ) )
            {
                isAuthorized = true;

                break;
            }
        }

        return isAuthorized;
    }

    /**
     * Gives the media type depending on the specified parameters
     * @param accept the accepted format
     * @param format the format
     * @return the media type
     */
    protected String getMediaType( String accept, String format )
    {
        String strMediaType;

        if ( ( ( accept != null ) && accept.contains( MediaType.APPLICATION_JSON ) ) ||
                ( ( format != null ) && format.equals( Constants.MEDIA_TYPE_JSON ) ) )
        {
            strMediaType = MediaType.APPLICATION_JSON;
        }
        else
        {
            strMediaType = MediaType.APPLICATION_XML;
        }

        return strMediaType;
    }
}
