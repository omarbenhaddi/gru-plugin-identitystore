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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryService
{
    protected final int MAX_RETRY = AppPropertiesService.getPropertyInt( "identitystore.task.reindex.retry.max", 500 );
    protected final int TEMPO_RETRY = AppPropertiesService.getPropertyInt( "identitystore.task.reindex.retry.wait", 100 );
    private final String ELASTIC_URL = AppPropertiesService.getProperty( "elasticsearch.url" );
    private final String ELASTIC_USER = AppPropertiesService.getProperty( "elasticsearch.user", "" );
    private final String ELASTIC_PWD = AppPropertiesService.getProperty( "elasticsearch.pwd", "" );

    public boolean callBulkWithRetry( final List<BulkAction> bulkActions, final String index )
    {
        final IIdentityIndexer identityIndexer = this.createIdentityIndexer( );
        final AtomicInteger failedCalls = new AtomicInteger( 0 );
        boolean processed = identityIndexer.bulk( bulkActions, index );
        while ( !processed )
        {
            final int nbRetry = failedCalls.getAndIncrement( );
            AppLogService.error( "Retry nb " + nbRetry );
            if ( nbRetry > MAX_RETRY )
            {
                AppLogService.error( "The number of retries exceeds the configured value of " + MAX_RETRY + ", interrupting.." );
                return false;
            }
            try
            {
                Thread.sleep( TEMPO_RETRY );
            }
            catch( InterruptedException e )
            {
                AppLogService.error( "Could thread sleep.. + " + e.getMessage( ) );
            }
            processed = identityIndexer.bulk( bulkActions, index );
        }
        return true;
    }

    private IIdentityIndexer createIdentityIndexer( )
    {
        if ( StringUtils.isAnyBlank( ELASTIC_USER, ELASTIC_PWD ) )
        {
            AppLogService.debug( "Creating elastic connection without authentification" );
            return new IdentityIndexer( ELASTIC_URL );
        }
        else
        {
            AppLogService.debug( "Creating elastic connection with authentification" );
            return new IdentityIndexer( ELASTIC_URL, ELASTIC_USER, ELASTIC_PWD );
        }
    }
}
