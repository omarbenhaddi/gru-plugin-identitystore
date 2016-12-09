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
package fr.paris.lutece.plugins.identitystore.web.request;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.service.Constants;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.util.AppException;

/**
 * This class represents a create request for IdentityStoreRestServive
 */
public class IdentityStoreCreateRequest extends IdentityStoreRequest
{

    private IdentityChangeDto _identityChangeDto;
    private Map<String, File> _mapAttachedFiles;
    private ObjectMapper _objectMapper;

    /**
     * Constructor of IdentityStoreCreateRequest
     * 
     * @param identityChangeDto
     *            the dto of identity's change
     * @param objectMapper
     *            for json transformation
     */
    public IdentityStoreCreateRequest( IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles, ObjectMapper objectMapper )
    {
        super( );
        this._identityChangeDto = identityChangeDto;
        this._mapAttachedFiles = mapAttachedFiles;
        this._objectMapper = objectMapper;
    }

    /**
     * Valid the create request
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    protected void validRequest( ) throws AppException
    {
        IdentityRequestValidator.instance( ).checkIdentityChange( _identityChangeDto );

        if ( StringUtils.isNotEmpty( _identityChangeDto.getIdentity( ).getCustomerId( ) ) )
        {
            IdentityRequestValidator.instance( ).checkIdentity( null, _identityChangeDto.getIdentity( ).getCustomerId( ) );
        }
        else
            if ( StringUtils.isNotEmpty( _identityChangeDto.getIdentity( ).getConnectionId( ) ) )
            {
                IdentityRequestValidator.instance( ).checkIdentity( _identityChangeDto.getIdentity( ).getConnectionId( ), Constants.NO_CUSTOMER_ID );
            }

        IdentityRequestValidator.instance( ).checkClientApplication( _identityChangeDto.getAuthor( ).getApplicationCode( ) );
        IdentityRequestValidator.instance( ).checkAttributes( _identityChangeDto.getIdentity( ), _identityChangeDto.getAuthor( ).getApplicationCode( ),
                _mapAttachedFiles );
    }

    /**
     * create the identity
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    protected String doSpecificRequest( ) throws AppException
    {
        String strCustomerId = _identityChangeDto.getIdentity( ).getCustomerId( );
        String strConnectionId = _identityChangeDto.getIdentity( ).getConnectionId( );
        String strClientAppCode = _identityChangeDto.getAuthor( ).getApplicationCode( );
        Identity identity = null;

        if ( StringUtils.isNotEmpty( strCustomerId ) )
        {
            identity = IdentityStoreService.getIdentityByCustomerId( strCustomerId, strClientAppCode );

            if ( identity == null )
            {
                throw new IdentityNotFoundException( "No identity found for " + Constants.PARAM_ID_CUSTOMER + "(" + strCustomerId + ")" );
            }
        }
        else
        {
            if ( !StringUtils.isEmpty( strConnectionId ) )
            {
                identity = IdentityStoreService.getIdentityByConnectionId( strConnectionId, strClientAppCode );

                if ( identity == null )
                {
                    identity = createIdentity( strConnectionId );
                    identity = updateIdentity( identity, _identityChangeDto, _mapAttachedFiles );
                }
            }
            else
            {
                identity = createIdentity( _identityChangeDto, _mapAttachedFiles );
            }
        }

        try
        {
            return _objectMapper.writeValueAsString( DtoConverter.convertToDto( identity, strClientAppCode ) );
        }
        catch( JsonProcessingException e )
        {
            throw new AppException( ERROR_JSON_MAPPING, e );
        }
    }

}
