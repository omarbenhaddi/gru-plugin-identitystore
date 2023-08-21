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
package fr.paris.lutece.plugins.identitystore.service.history;

import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.*;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentityHistoryService
{
    private static IdentityHistoryService _instance;

    public static IdentityHistoryService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityHistoryService( );
        }
        return _instance;
    }

    public IdentityHistory get( final String customerId, final String clientCode ) throws IdentityStoreException
    {
        final Identity identity = IdentityHome.findByCustomerId( customerId );
        if ( identity == null )
        {
            throw new IdentityNotFoundException( "CustomerId = " + customerId );
        }
        final ServiceContract serviceContract = ServiceContractService.instance( ).getActiveServiceContract( clientCode );
        if ( serviceContract == null )
        {
            throw new ServiceContractNotFoundException( "Client App Code = " + clientCode );
        }
        final Set<String> readableAttributeKeys = serviceContract.getAttributeRights( ).stream( ).filter( AttributeRight::isReadable )
                .map( ar -> ar.getAttributeKey( ).getKeyName( ) ).collect( Collectors.toSet( ) );

        final List<IdentityChange> identityChangeList = IdentityHome.findHistoryByCustomerId( customerId );
        final List<AttributeChange> attributeChangeList = IdentityAttributeHome.getAttributeChangeHistory( identity.getId( ) );

        return toHistory( customerId, identityChangeList, attributeChangeList, readableAttributeKeys );
    }

    public IdentityHistorySearchResponse search( final IdentityHistorySearchRequest request, final String clientCode ) throws IdentityStoreException
    {

        final IdentityHistorySearchResponse response = new IdentityHistorySearchResponse( );
        final ServiceContract serviceContract = ServiceContractService.instance( ).getActiveServiceContract( clientCode );
        if ( serviceContract == null )
        {
            throw new ServiceContractNotFoundException( "Client App Code = " + clientCode );
        }
        final Set<String> readableAttributeKeys = serviceContract.getAttributeRights( ).stream( ).filter( AttributeRight::isReadable )
                .map( ar -> ar.getAttributeKey( ).getKeyName( ) ).collect( Collectors.toSet( ) );

        final List<IdentityChange> identityChangeList = IdentityHome.findHistoryBySearchParameters( request.getCustomerId( ), request.getClientCode( ),
                request.getAuthorName( ), request.getIdentityChangeType( ), request.getMetadata( ), request.getNbDaysFrom( ) );
        final Map<String, List<IdentityChange>> identityChangeMap = identityChangeList.stream( )
                .collect( Collectors.groupingBy( IdentityChange::getCustomerId ) );

        for ( final String customerId : identityChangeMap.keySet( ) )
        {
            final List<AttributeChange> attributeChangeList = IdentityAttributeHome.getAttributeChangeHistory( customerId );
            response.getHistories( ).add( this.toHistory( customerId, identityChangeMap.get( customerId ), attributeChangeList, readableAttributeKeys ) );
        }

        if ( response.getHistories( ).isEmpty( ) )
        {
            response.setStatus( HistorySearchStatusType.NOT_FOUND );
        }
        else
        {
            response.setStatus( HistorySearchStatusType.SUCCESS );
        }

        return response;
    }

    private IdentityHistory toHistory( final String customerId, final List<IdentityChange> identityChangeList, final List<AttributeChange> attributeChangeList,
            final Set<String> readableAttributeKeys )
    {
        final IdentityHistory history = new IdentityHistory( );
        history.setCustomerId( customerId );
        history.getIdentityChanges( ).addAll( identityChangeList );
        attributeChangeList.stream( ).filter( ac -> readableAttributeKeys.contains( ac.getAttributeKey( ) ) )
                .collect( Collectors.groupingBy( AttributeChange::getAttributeKey ) ).forEach( ( key, attributeChanges ) -> {
                    final AttributeHistory attributeHistory = new AttributeHistory( );
                    attributeHistory.setAttributeKey( key );
                    attributeHistory.setAttributeChanges( attributeChanges );
                    history.getAttributeHistories( ).add( attributeHistory );
                } );
        return history;
    }
}
