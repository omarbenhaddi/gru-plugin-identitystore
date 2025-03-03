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
package fr.paris.lutece.plugins.identitystore.service.identity;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.cache.IdentityDtoCache;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IdentityExportService
{

    // CACHE
    private final IdentityDtoCache _identityDtoCache = SpringContextService.getBean( "identitystore.identityDtoCache" );

    // PROPERTIES
    private final boolean includeDeletedIdentitiesDefault = AppPropertiesService.getPropertyBoolean( "identitystore.identity.export.include.deleted.identities",
            false );

    // INSTANCE
    private static IdentityExportService instance;

    private IdentityExportService( )
    {

    }

    public static IdentityExportService instance( )
    {
        if ( instance == null )
        {
            instance = new IdentityExportService( );
        }
        return instance;
    }

    /**
     * Exports a list of identities according to the provided request and client code.
     * 
     * @param request
     *            the export request
     * @param serviceContract
     *            the service contract
     * @return
     */
    public List<IdentityDto> export( final IdentityExportRequest request, final ServiceContract serviceContract ) throws IdentityStoreException
    {
        final List<IdentityDto> identities = new ArrayList<>( );
        final boolean includeDeletedIdentities = request.getIncludeDeletedIdentities( ) != null ? request.getIncludeDeletedIdentities( )
                : includeDeletedIdentitiesDefault;
        for ( final String cuid : request.getCuidList( ) )
        {
            final IdentityDto identity = _identityDtoCache.getByCustomerId( cuid, serviceContract );
            if ( identity == null || ( !includeDeletedIdentities && identity.getExpiration( ) != null && identity.getExpiration( ).isDeleted( ) ) )
            {
                continue;
            }
            if ( !request.getAttributeKeyList( ).isEmpty( ) )
            {
                final List<AttributeDto> attrToKeep = identity.getAttributes( ).stream( ).filter( a -> request.getAttributeKeyList( ).contains( a.getKey( ) ) )
                        .collect( Collectors.toList( ) );
                identity.setAttributes( attrToKeep );
            }
            identities.add( identity );
        }
        if ( identities.isEmpty( ) )
        {
            throw new ResourceNotFoundException( "No identities were found for provided CUIDs", Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY );
        }

        return identities;
    }
}
