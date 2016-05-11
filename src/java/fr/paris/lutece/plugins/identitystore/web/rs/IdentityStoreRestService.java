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
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import fr.paris.lutece.plugins.identitystore.business.Attribute;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.KeyType;
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
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.util.AppException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

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
import javax.ws.rs.core.Response.ResponseBuilder;


/**
 * REST service for channel resource
 *
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.IDENTITY_PATH )
public class IdentityStoreRestService
{
    protected static final Map<String, IFormatterFactory> _formatterFactories = new HashMap<String, IFormatterFactory>(  );
    private static final String PARAM_FILE_KEY_NAME = "name";
    private static final String DOWNLOAD_FILE_PATH = "/file/";

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
    @Path( "{" + Constants.PARAM_ID_CONNECTION + "}" )
    public Response setAttributesByConnectionId( FormDataMultiPart formParams,
        @PathParam( Constants.PARAM_ID_CONNECTION )
    String strConnectionId, @QueryParam( Constants.PARAM_CLIENT_CODE )
    String strClientAppCode, @QueryParam( Constants.PARAM_USER_ID )
    String strUserId, @QueryParam( Constants.PARAM_CERTIFIER_ID )
    String strCertifierCode )
    {
        IdentityDto identityDto = null;
        IFormatterFactory formatterFactory = _formatterFactories.get( MediaType.APPLICATION_JSON );
        String strBody = StringUtils.EMPTY;

        //TODO
        ChangeAuthor author = null;
        author = new ChangeAuthor(  );

        Map<String, File> mapAttachedFiles = new HashMap<String, File>(  );
        AttributeCertifier certifier = null;

        try
        {
            for ( BodyPart part : formParams.getBodyParts(  ) )
            {
                InputStream inputStream = part.getEntityAs( InputStream.class );
                ContentDisposition contentDispo = part.getContentDisposition(  );

                if ( contentDispo == null )
                {
                    //content-body of request
                    strBody = IOUtils.toString( inputStream );
                    identityDto = getIdentityFromRequest( strConnectionId, strClientAppCode, strUserId, strBody );
                }
                else
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

            checkAttachedFiles( identityDto, mapAttachedFiles );

            ResponseDto responseDto = updateAttributes( identityDto, strConnectionId, author, certifier,
                    mapAttachedFiles );
            String strResponse = formatterFactory.createFormatter( ResponseDto.class ).format( responseDto );

            return Response.ok( strResponse, MediaType.APPLICATION_JSON ).build(  );
        }
        catch ( Exception e )
        {
            ResponseDto responseDto = new ResponseDto(  );
            responseDto.setStatus( String.valueOf( Status.BAD_REQUEST ) );
            responseDto.setMessage( e.getMessage(  ) );

            String strResponse = formatterFactory.createFormatter( ResponseDto.class ).format( responseDto );

            return Response.status( Status.BAD_REQUEST ).type( MediaType.APPLICATION_JSON ).entity( strResponse ).build(  );
        }
    }

    /**
     * returns requested file matching attributeKey / connectionId if application is authorized
     * @param strConnectionId connectionId (must not be empty)
     * @param strClientCode client application code (must not be empty)
     * @param strAttributeKey attribute key containing file (must not be empty)
     * @return http 200 Response containing requested file, http 400 otherwise
     */
    @GET
    @Path( DOWNLOAD_FILE_PATH + "{" + Constants.PARAM_ID_CONNECTION + "}" )
    public Response downloadFileAttribute( @PathParam( Constants.PARAM_ID_CONNECTION )
    String strConnectionId, @QueryParam( Constants.PARAM_CLIENT_CODE )
    String strClientCode, @QueryParam( Constants.PARAM_ATTRIBUTE_KEY )
    String strAttributeKey )
    {
        File file = null;

        try
        {
            file = getFileAttribute( strConnectionId, strClientCode, strAttributeKey );
        }
        catch ( AppException appEx )
        {
            IFormatterFactory formatterFactory = _formatterFactories.get( MediaType.APPLICATION_JSON );
            ResponseDto responseDto = new ResponseDto(  );
            responseDto.setStatus( String.valueOf( Status.BAD_REQUEST ) );
            responseDto.setMessage( appEx.getMessage(  ) );

            String strResponse = formatterFactory.createFormatter( ResponseDto.class ).format( responseDto );

            return Response.status( Status.BAD_REQUEST ).type( MediaType.APPLICATION_JSON ).entity( strResponse ).build(  );
        }

        ResponseBuilder response = Response.ok( (Object) PhysicalFileHome.findByPrimaryKey( 
                    file.getPhysicalFile(  ).getIdPhysicalFile(  ) ).getValue(  ) );
        response.header( "Content-Disposition", "attachment; filename=" + file.getTitle(  ) );
        response.header( "Content-Type", file.getMimeType(  ) );

        return response.build(  );
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
     * check attached files are present in identity Dto
     * @param identityDto identityDto with list of attributes
     * @param mapAttachedFiles map of attached files
     * @throws AppException thrown if a file is attached without its attribute
     */
    private void checkAttachedFiles( IdentityDto identityDto, Map<String, File> mapAttachedFiles )
        throws AppException
    {
        if ( ( mapAttachedFiles != null ) && !mapAttachedFiles.isEmpty(  ) )
        {
            //check if input files is present in identity DTO attributes
            for ( Map.Entry<String, File> entry : mapAttachedFiles.entrySet(  ) )
            {
                boolean bFound = false;

                for ( AttributeDto attribute : identityDto.getAttributes(  ) )
                {
                    AttributeKey attributeKey = AttributeKeyHome.findByKey( attribute.getKey(  ) );

                    //check that attribute is file type and that its name is matching 
                    if ( attributeKey.getKeyType(  ).equals( KeyType.FILE ) &&
                            StringUtils.isNotBlank( attribute.getValue(  ) ) &&
                            attribute.getValue(  ).equals( entry.getKey(  ) ) )
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
            //check that all file attribute type provided with filename in dto have matching attachements  
            for ( AttributeDto attribute : identityDto.getAttributes(  ) )
            {
                AttributeKey attributeKey = AttributeKeyHome.findByKey( attribute.getKey(  ) );

                if ( attributeKey.getKeyType(  ).equals( KeyType.FILE ) &&
                        StringUtils.isNotBlank( attribute.getValue(  ) ) &&
                        ( ( mapAttachedFiles == null ) || ( mapAttachedFiles.get( attribute.getValue(  ) ) == null ) ) )
                {
                    throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeKey.getKeyName(  ) +
                        " is provided with filename=" + attribute.getValue(  ) + " but no file is attached" );
                }
            }
        }
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

        //TODO check previous identities ?
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
     * @param mapAttachedFiles map containing File matching key attribute name
     * @return responseDto response containings updated fields
     *
     */
    private ResponseDto updateAttributes( IdentityDto identityDto, String strConnectionId, ChangeAuthor author,
        AttributeCertifier certifier, Map<String, File> mapAttachedFiles )
    {
        StringBuilder sb = new StringBuilder( "Fields successfully updated : " );

        for ( AttributeDto attributeDto : identityDto.getAttributes(  ) )
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
