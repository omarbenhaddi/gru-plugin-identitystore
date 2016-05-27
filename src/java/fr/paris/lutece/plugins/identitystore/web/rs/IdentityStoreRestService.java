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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;

import fr.paris.lutece.plugins.identitystore.business.Attribute;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.KeyType;
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.util.AppException;

import net.sf.json.util.JSONUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;


/**
 * REST service for channel resource
 *
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.IDENTITY_PATH )
public final class IdentityStoreRestService
{
    private static final String DOWNLOAD_FILE_PATH = "/file/";
    private ObjectMapper _objectMapper;

    /**
     * private constructor
     */
    private IdentityStoreRestService(  )
    {
        _objectMapper = new ObjectMapper(  );
        _objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
        _objectMapper.enable( SerializationFeature.WRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.UNWRAP_ROOT_VALUE );
    }

    /**
     * Gives Identity
     * @param strConnectionId connection ID
     * @param strClientAppCode client code
     * @return the identity
     */
    @GET
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getAttributesByConnectionId( @QueryParam( Constants.PARAM_ID_CONNECTION )
    String strConnectionId, @QueryParam( Constants.PARAM_CLIENT_CODE )
    String strClientAppCode )
    {
        String strJsonResponse;

        try
        {
            checkClientApplicationCode( strClientAppCode );

            Identity identity = IdentityStoreService.getIdentity( strConnectionId, strClientAppCode );

            //TODO check if rule is correct
            if ( identity == null )
            {
                ResponseDto response = new ResponseDto(  );
                response.setMessage( "no identity found for connectionId=" + strConnectionId );
                response.setStatus( String.valueOf( Status.NOT_FOUND ) );

                String strResponse;
                strResponse = _objectMapper.writeValueAsString( response );

                return Response.status( Status.NOT_FOUND ).type( MediaType.APPLICATION_JSON ).entity( strResponse )
                               .build(  );
            }

            strJsonResponse = _objectMapper.writeValueAsString( IdentityDtoUtil.convertToDto( identity, strClientAppCode ) );

            return Response.ok( strJsonResponse ).build(  );
        }
        catch ( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     *
     * @param formParams form params, bodypars used for files upload
     * @param strConnectionId connectionId
     * @param strClientAppCode clientAppCode
     * @param strUserId user Id
     * @param strCertifierCode certifier code
     * @return http 200 if update is ok with ResponseDto
     */
    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response setAttributesByConnectionId( FormDataMultiPart formParams,
        @FormDataParam( Constants.PARAM_ID_CONNECTION )
    String strConnectionId, @FormDataParam( Constants.PARAM_CLIENT_CODE )
    String strClientAppCode, @FormDataParam( Constants.PARAM_USER_ID )
    String strUserId, @FormDataParam( Constants.PARAM_CERTIFIER_ID )
    String strCertifierCode )
    {
        IdentityDto identityDto = null;
        String strBody = StringUtils.EMPTY;

        //TODO
        ChangeAuthor author = null;
        author = new ChangeAuthor(  );

        Map<String, File> mapAttachedFiles = new HashMap<String, File>(  );
        AttributeCertifier certifier = null;

        try
        {
            checkSetAttributeParams( strConnectionId, strClientAppCode, strUserId );

            for ( BodyPart part : formParams.getBodyParts(  ) )
            {
                InputStream inputStream = part.getEntityAs( InputStream.class );
                ContentDisposition contentDispo = part.getContentDisposition(  );

                if ( contentDispo == null )
                {
                    //content-body of request
                    strBody = IOUtils.toString( inputStream );

                    if ( JSONUtils.mayBeJSON( strBody ) )
                    {
                        identityDto = getIdentityFromJson( strBody );
                    }
                    else
                    {
                        throw new AppException( "Error parsing json request " + strBody );
                    }
                }
                else if ( StringUtils.isNotBlank( contentDispo.getFileName(  ) ) )
                {
                    //attachment file
                    PhysicalFile physicalFile = new PhysicalFile(  );
                    physicalFile.setValue( IOUtils.toByteArray( inputStream ) );

                    fr.paris.lutece.portal.business.file.File file = new fr.paris.lutece.portal.business.file.File(  );
                    file.setPhysicalFile( physicalFile );
                    file.setMimeType( part.getMediaType(  ).getType(  ) + "/" + part.getMediaType(  ).getSubtype(  ) );
                    file.setSize( physicalFile.getValue(  ).length );
                    file.setTitle( contentDispo.getFileName(  ) );
                    mapAttachedFiles.put( contentDispo.getFileName(  ), file );
                }
            }

            checkAttributes( identityDto, strClientAppCode, mapAttachedFiles );

            ResponseDto responseDto = updateAttributes( identityDto, strConnectionId, author, certifier,
                    mapAttachedFiles );

            String strResponse = _objectMapper.writeValueAsString( responseDto );

            return Response.ok( strResponse, MediaType.APPLICATION_JSON ).build(  );
        }
        catch ( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * returns requested file matching attributeKey / connectionId if application is authorized
     * @param strConnectionId connectionId (must not be empty)
     * @param strClientAppCode client application code (must not be empty)
     * @param strAttributeKey attribute key containing file (must not be empty)
     * @return http 200 Response containing requested file, http 400 otherwise
     */
    @GET
    @Path( DOWNLOAD_FILE_PATH )
    public Response downloadFileAttribute( @QueryParam( Constants.PARAM_ID_CONNECTION )
    String strConnectionId, @QueryParam( Constants.PARAM_CLIENT_CODE )
    String strClientAppCode, @QueryParam( Constants.PARAM_ATTRIBUTE_KEY )
    String strAttributeKey )
    {
        File file = null;

        try
        {
            checkDownloadFileAttributeParams( strConnectionId, strClientAppCode, strAttributeKey );
            file = getFileAttribute( strConnectionId, strClientAppCode, strAttributeKey );
        }
        catch ( Exception exception )
        {
            return getErrorResponse( exception );
        }

        ResponseBuilder response = Response.ok( (Object) PhysicalFileHome.findByPrimaryKey( 
                    file.getPhysicalFile(  ).getIdPhysicalFile(  ) ).getValue(  ) );
        response.header( "Content-Disposition", "attachment; filename=" + file.getTitle(  ) );
        response.header( "Content-Type", file.getMimeType(  ) );

        return response.build(  );
    }

    /**
     * check that client application code exists in identitystore
     * @param strClientCode client application code
     * @throws AppException thrown if null
     */
    private void checkClientApplicationCode( String strClientCode )
        throws AppException
    {
        ClientApplication clientApp = ClientApplicationHome.findByCode( strClientCode );

        if ( clientApp == null )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " : " + strClientCode + " is unknown " );
        }
    }

    /**
     * check input parameters
     * @param strConnectionId connection id of identity to update
     * @param strClientAppCode client application code asking for modif
     * @param strUserId user id which makes modification
     * @throws AppException if request is not correct
     */
    private void checkSetAttributeParams( String strConnectionId, String strClientAppCode, String strUserId )
        throws AppException
    {
        if ( StringUtils.isBlank( strConnectionId ) )
        {
            throw new AppException( Constants.PARAM_ID_CONNECTION + " is missing" );
        }

        if ( StringUtils.isBlank( strClientAppCode ) )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " is missing" );
        }

        checkClientApplicationCode( strClientAppCode );

        if ( StringUtils.isBlank( strUserId ) )
        {
            throw new AppException( Constants.PARAM_USER_ID + " is missing" );
        }
    }

    /**
     * build error response from exception
     * @param e exception
     * @return ResponseDto from exception
     */
    private Response getErrorResponse( Exception e )
    {
        ResponseDto response = new ResponseDto(  );
        response.setMessage( e.getMessage(  ) );
        response.setStatus( String.valueOf( Status.BAD_REQUEST ) );

        String strResponse;

        try
        {
            strResponse = _objectMapper.writeValueAsString( response );

            return Response.status( Status.BAD_REQUEST ).type( MediaType.APPLICATION_JSON ).entity( strResponse ).build(  );
        }
        catch ( JsonProcessingException jpe )
        {
            return Response.status( Status.BAD_REQUEST ).type( MediaType.TEXT_PLAIN ).entity( e.getMessage(  ) ).build(  );
        }
    }

    /**
     * @param strConnectionId connectionId (must not be empty)
     * @param strClientCode client application code (must not be empty)
     * @param strAttributeKey attribute key containing file (must not be empty)
     * @throws AppException thrown if input parameters are invalid or no file is found
     */
    private void checkDownloadFileAttributeParams( String strConnectionId, String strClientCode, String strAttributeKey )
        throws AppException
    {
        checkClientApplicationCode( strClientCode );

        if ( StringUtils.isBlank( strConnectionId ) )
        {
            throw new AppException( Constants.PARAM_ID_CONNECTION + " is null or empty" );
        }

        if ( StringUtils.isBlank( strClientCode ) )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " is null or empty" );
        }

        if ( StringUtils.isBlank( strAttributeKey ) )
        {
            throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " is null or empty" );
        }
    }

    /**
     * @param strConnectionId connectionId (must not be empty)
     * @param strClientCode client application code (must not be empty)
     * @param strAttributeKey attribute key containing file (must not be empty)
     * @return File matching connectionId/attribute key if readable for client application code
     * @throws AppException thrown if input parameters are invalid or no file is found
     */
    private File getFileAttribute( String strConnectionId, String strClientCode, String strAttributeKey )
        throws AppException
    {
        Attribute attribute = IdentityStoreService.getAttribute( strConnectionId, strAttributeKey, strClientCode );

        if ( ( attribute == null ) || ( attribute.getFile(  ) == null ) )
        {
            throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + strAttributeKey +
                " attribute not found for " + Constants.PARAM_ID_CONNECTION + "=" + strConnectionId + "  " +
                Constants.PARAM_CLIENT_CODE + "=" + strClientCode );
        }

        return attribute.getFile(  );
    }

    /**
     * check attached files are present in identity Dto and that attributes to update
     * exist and are writable (or not writable AND unchanged)
     * @param identityDto identityDto with list of attributes
     * @param strClientAppCode application code to check right
     * @param mapAttachedFiles map of attached files
     * @throws AppException thrown if provided attributes are not valid
     */
    private void checkAttributes( IdentityDto identityDto, String strClientAppCode, Map<String, File> mapAttachedFiles )
        throws AppException
    {
        if ( ( mapAttachedFiles != null ) && !mapAttachedFiles.isEmpty(  ) )
        {
            //check if input files is present in identity DTO attributes
            for ( Map.Entry<String, File> entry : mapAttachedFiles.entrySet(  ) )
            {
                boolean bFound = false;

                for ( AttributeDto attributeDto : identityDto.getAttributes(  ).values(  ) )
                {
                    AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey(  ) );
                    if ( attributeKey == null )
                    {
                        throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeDto.getKey(  ) +
                            " is provided but does not exist" );
                    }
                    //check that attribute is file type and that its name is matching 
                    if ( attributeKey.getKeyType(  ).equals( KeyType.FILE ) &&
                            StringUtils.isNotBlank( attributeDto.getValue(  ) ) &&
                            attributeDto.getValue(  ).equals( entry.getKey(  ) ) )
                    {
                        bFound = true;

                        break;
                    }
                }

                if ( !bFound )
                {
                    throw new AppException( Constants.PARAM_FILE + " " + entry.getKey(  ) +
                        " is provided but its attribute is missing" );
                }
            }
        }

        if ( identityDto.getAttributes(  ) != null )
        {
            List<AttributeRight> lstRights = ClientApplicationHome.selectApplicationRights( ClientApplicationHome.findByCode( 
                        strClientAppCode ) );

            //check that all file attribute type provided with filename in dto have matching attachements  
            for ( AttributeDto attributeDto : identityDto.getAttributes(  ).values(  ) )
            {
                AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey(  ) );
                if ( attributeKey == null )
                {
                    throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeDto.getKey(  ) +
                        " is provided but does not exist" );
                }
                for ( AttributeRight attRight : lstRights )
                {
                    Attribute attribute = IdentityStoreService.getAttribute( identityDto.getConnectionId(  ),
                            attRight.getAttributeKey(  ).getKeyName(  ), strClientAppCode );

                    if ( attRight.getAttributeKey(  ).getId(  ) == attributeKey.getId(  ) )
                    {
                        //if provided attribute is writable, or if no change => ok
                        if ( attRight.isWritable(  ) || attributeDto.getValue(  ).equals( attribute.getValue(  ) ) )
                        {
                            break;
                        }
                        else
                        {
                            throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeKey.getKeyName(  ) +
                                " is provided but is not writable" );
                        }
                    }
                }

                if ( attributeKey.getKeyType(  ).equals( KeyType.FILE ) &&
                        StringUtils.isNotBlank( attributeDto.getValue(  ) ) &&
                        ( ( mapAttachedFiles == null ) || ( mapAttachedFiles.get( attributeDto.getValue(  ) ) == null ) ) )
                {
                    throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeKey.getKeyName(  ) +
                        " is provided with filename=" + attributeDto.getValue(  ) + " but no file is attached" );
                }
            }
        }
    }

    /**
     * check input parameters
     * @param strJsonContent json content of request
     * @return identityDto parsed identityDto
     * @throws AppException if request is not correct
     * @throws IOException if error occurs while parsing json
     * @throws JsonMappingException if error occurs while parsing json
     * @throws JsonParseException if error occurs while parsing json
     *
     */
    private IdentityDto getIdentityFromJson( String strJsonContent )
        throws AppException, JsonParseException, JsonMappingException, IOException
    {
        IdentityDto identityDto = _objectMapper.readValue( strJsonContent, IdentityDto.class );

        if ( ( identityDto.getAttributes(  ) == null ) || ( identityDto.getAttributes(  ).size(  ) == 0 ) )
        {
            throw new AppException( "no attribute to update" );
        }

        return identityDto;
    }

    /**
     * check if new identity attributes have errors and returns them
     * @param identityDto new identity to update
     * @param strConnectionId connectionId of identity which will be updated
     * @param author author responsible for modification
     * @param certifier certfier responsible for modification
     * @param mapAttachedFiles map containing File matching key attribute name
     * @return responseDto response containings updated fields
     *
     */
    private ResponseDto updateAttributes( IdentityDto identityDto, String strConnectionId, ChangeAuthor author,
        AttributeCertifier certifier, Map<String, File> mapAttachedFiles )
    {
        StringBuilder sb = new StringBuilder( "Fields successfully updated : " );

        Identity identity = IdentityHome.findByConnectionId( strConnectionId );

        if ( identity == null )
        {
            identity = new Identity(  );
            identity.setConnectionId( identityDto.getConnectionId(  ) );
            identity.setCustomerId( identityDto.getCustomerId(  ) );
            IdentityHome.create( identity );
        }

        for ( AttributeDto attributeDto : identityDto.getAttributes(  ).values(  ) )
        {
            File file = null;
            AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey(  ) );

            if ( attributeKey.getKeyType(  ).equals( KeyType.FILE ) &&
                    StringUtils.isNotBlank( attributeDto.getValue(  ) ) )
            {
                file = mapAttachedFiles.get( attributeDto.getValue(  ) );
            }

            IdentityStoreService.setAttribute( strConnectionId, attributeDto.getKey(  ), attributeDto.getValue(  ),
                file, author, certifier );
            sb.append( attributeDto.getKey(  ) + "," );
        }

        ResponseDto response = new ResponseDto(  );
        response.setStatus( Constants.RESPONSE_OK );
        response.setMessage( sb.substring( 0, sb.length(  ) - 1 ) );

        return response;
    }
}
