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
package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityQualityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * cache sur les requêtes d'identité (par CUID ou GUID), qui cache :<br/>
 * <ul>
 * <li>Les attributs autorisés en lecture défini dans le contrat de service</li>
 * <li>Le score de qualité</li>
 * <li>Le score de couverture</li>
 * <li>Les données de suspicions de doublons si l'identité est marquée comme suspecte</li>
 * <li>Les données d'exclusion si l'identité a été exclue d'une ou plusieurs suspicions de doublon avec d'autres identités</li>
 * </ul>
 * l'identité elle-même sera récupérée de la DB à chaque fois, afin de vérifier la date de dernière modification :<br/>
 * <ul>
 * <li>Si la date est la même dans l'objet caché et en DB, l'objet caché est retourné</li>
 * <li>Sinon, on supprime l'objet caché du cache, on récupère toutes les infos de la DB et on recache.</li>
 * </ul>
 * La clé de cache combine CUID + ID contrat de service
 */
public class IdentityDtoCache extends AbstractCacheableService
{
    public static final String SERVICE_NAME = "IdentityCache";

    public IdentityDtoCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        AppLogService.debug( "Init Identity cache" );
        this.resetCache( );
    }

    public void put( final IdentityDto identity, final int serviceContractId )
    {
        final String cacheKey = computeCacheKey( identity.getCustomerId( ), serviceContractId );
        if ( this.getKeys( ).contains( cacheKey ) )
        {
            this.removeKey( cacheKey );
        }
        this.putInCache( cacheKey, identity );
        AppLogService.debug( "Identity added to cache: " + cacheKey );
    }

    public void remove( final String cacheKey )
    {
        if ( this.getKeys( ).contains( cacheKey ) )
        {
            this.removeKey( cacheKey );
        }
        AppLogService.debug( "Identity removed from cache: " + cacheKey );
    }

    public IdentityDto getByConnectionId( final String connectionId, final ServiceContract serviceContract )
    {
        Identity identity = IdentityHome.findByConnectionId( connectionId );
        if ( identity != null && identity.isMerged( ) && identity.getMasterIdentityId( ) != null )
        {
            identity = IdentityHome.findByPrimaryKey( identity.getMasterIdentityId( ) );
        }
        if ( identity == null || StringUtils.isBlank( identity.getCustomerId( ) ) || identity.getLastUpdateDate( ) == null )
        {
            return null;
        }
        return get( identity.getCustomerId( ), identity.getLastUpdateDate( ), serviceContract );
    }

    public IdentityDto getByCustomerId( final String customerId, final ServiceContract serviceContract )
    {
        Identity identity = IdentityHome.findByCustomerIdNoAttributes( customerId );
        if ( identity != null && identity.isMerged( ) && identity.getMasterIdentityId( ) != null )
        {
            identity = IdentityHome.findByPrimaryKey( identity.getMasterIdentityId( ) );
        }
        if ( identity == null || StringUtils.isBlank( identity.getCustomerId( ) ) || identity.getLastUpdateDate( ) == null )
        {
            return null;
        }
        return get( identity.getCustomerId( ), identity.getLastUpdateDate( ), serviceContract );
    }

    private IdentityDto get( final String customerId, final Timestamp lastUpdateDateFromDb, final ServiceContract serviceContract )
    {
        final String cacheKey = computeCacheKey( customerId, serviceContract.getId( ) );
        final IdentityDto identityDtoFromCache = (IdentityDto) this.getFromCache( cacheKey );
        if ( identityDtoFromCache == null || !Objects.equals( identityDtoFromCache.getLastUpdateDate( ), lastUpdateDateFromDb ) )
        {
            this.remove( cacheKey );
            final IdentityDto enrichedIdentity = this.getFromDatabaseAndEnrich( customerId, serviceContract );
            if ( enrichedIdentity == null )
            {
                return null;
            }
            this.put( enrichedIdentity, serviceContract.getId( ) );
            return enrichedIdentity;
        }
        return identityDtoFromCache;
    }

    public IdentityDto getFromDatabaseAndEnrich( final String customerId, final ServiceContract serviceContract )
    {
        final Identity identity = IdentityHome.findMasterIdentityByCustomerId( customerId );
        if ( identity == null )
        {
            return null;
        }
        return convertAndEnrich( identity, serviceContract );
    }

    private IdentityDto convertAndEnrich( final Identity identity, final ServiceContract serviceContract )
    {
        final IdentityDto identityDto = DtoConverter.convertIdentityToDto( identity );
        IdentityQualityService.instance( ).enrich( null, identityDto, serviceContract, identity );
        return identityDto;
    }

    private String computeCacheKey( final String cuid, final int serviceContractId )
    {
        return cuid + "|" + serviceContractId;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
