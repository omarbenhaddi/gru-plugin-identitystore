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
package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleHome;
import fr.paris.lutece.plugins.identitystore.service.duplicate.DuplicateRuleNotFoundException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.log4j.Logger;

import java.util.Objects;

public class DuplicateRulesCache extends AbstractCacheableService
{

    private static Logger _logger = Logger.getLogger( DuplicateRulesCache.class );

    public static final String SERVICE_NAME = "DuplicateRulesCache";

    public DuplicateRulesCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        AppLogService.info( "Init AttributeKey cache" );
        this.resetCache( );
        DuplicateRuleHome.findAll( ).forEach( duplicateRule -> this.put( duplicateRule ) );
    }

    public void put( final DuplicateRule rule )
    {
        if ( this.getKeys( ).contains( rule.getName( ) ) )
        {
            this.removeKey( rule.getName( ) );
        }
        this.putInCache( rule.getName( ), rule );
        AppLogService.info( "Duplicate rule added to cache: " + rule.getName( ) );
    }

    public void remove( final String ruleName )
    {
        if ( this.getKeys( ).contains( ruleName ) )
        {
            this.removeKey( ruleName );
        }

        AppLogService.info( "Duplicate rule removed from cache: " + ruleName );
    }

    /**
     * Deletes a {@link ServiceContract} by its id in the database
     *
     * @param id
     */
    public void remove( final Integer id )
    {
        this.getKeys( ).forEach( key -> {
            try
            {
                DuplicateRule duplicateRule = this.get( key );
                if ( Objects.equals( duplicateRule.getId( ), id ) )
                {
                    this.removeKey( key );
                }
            }
            catch( DuplicateRuleNotFoundException e )
            {
                AppLogService.error( "Cannot delete service contract with id" + id + " : {}", e );
            }
        } );
    }

    public DuplicateRule get( final String duplicateRuleName ) throws DuplicateRuleNotFoundException
    {
        DuplicateRule duplicateRule = (DuplicateRule) this.getFromCache( duplicateRuleName );
        if ( duplicateRule == null )
        {
            duplicateRule = this.getFromDatabase( duplicateRuleName );
            this.put( duplicateRule );
        }
        return duplicateRule;
    }

    public DuplicateRule getFromDatabase( final String duplicateRuleName ) throws DuplicateRuleNotFoundException
    {
        final DuplicateRule duplicateRule = DuplicateRuleHome.find( duplicateRuleName );
        if ( duplicateRule == null )
        {
            throw new DuplicateRuleNotFoundException( "No attribute key could be found with key " + duplicateRuleName );
        }
        return duplicateRule;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
