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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityObjectHome;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FullIndexTask extends AbstractIndexTask
{
    private static final String TASK_REINDEX_BATCH_SIZE_PROPERTY = "task.reindex.batch.size";
    private static final String TASK_REINDEX_ACTIVE_PROPERTY = "task.reindex.active";
    private final boolean active = AppPropertiesService.getPropertyBoolean( TASK_REINDEX_ACTIVE_PROPERTY, false );
    private final static Logger _logger = Logger.getLogger(FullIndexTask.class);
    private final IIdentityIndexer _identityIndexer;

    public FullIndexTask( IIdentityIndexer _identityIndexer )
    {
        this._identityIndexer = _identityIndexer;
    }

    public void run( )
    {
        if(active) {
            this.doJob();
        } else {
            _logger.info("Full index task is not active.");
        }
    }

    private void doJob() {
        final StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        this.init( );
        this.getStatus( ).log( "Starting identities full reindex at " + DateFormatUtils.format( stopWatch.getStartTime( ), "dd-MM-yyyy'T'HH:mm:ss" ) );
        final int batchSize = AppPropertiesService.getPropertyInt( TASK_REINDEX_BATCH_SIZE_PROPERTY, 1000 );
        final List<String> customerIdsList = new ArrayList<>( );
        if ( _identityIndexer.isAlive( ) )
        {
            final String newIndex = "identities-" + UUID.randomUUID( );
            this.getStatus( ).log( "ES available :: indexing" );
            try
            {
                this.getStatus( ).log( "Creating new index : " + newIndex );
                this._identityIndexer.initIndex( newIndex );

                if ( this._identityIndexer.indexExists( IIdentityIndexer.CURRENT_INDEX_ALIAS ) )
                {
                    this.getStatus( ).log( "Set current index READ-ONLY" );
                    this._identityIndexer.makeIndexReadOnly( IIdentityIndexer.CURRENT_INDEX_ALIAS );
                }
                else
                {
                    this._identityIndexer.createOrUpdateAlias( "", newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                }

                final List<String> eligibleCustomerIdsListForIndex = IdentityObjectHome.getEligibleCustomerIdsListForIndex( );
                customerIdsList.addAll( eligibleCustomerIdsListForIndex );
                this.getStatus( ).setNbTotalIdentities( customerIdsList.size( ) );
                this.getStatus( ).log( "NB identities to be indexed : " + this.getStatus( ).getNbTotalIdentities( ) );
                this.getStatus( ).log( "Size of indexing batches : " + batchSize );
                final Batch<String> batch = Batch.ofSize( customerIdsList, batchSize );
                this.getStatus( ).log( "NB of indexing batches : " + batch.size( ) );
                batch.stream( ).parallel( ).forEach( customerIds -> {
                    final List<BulkAction> actions = customerIds.stream( ).map( IdentityObjectHome::findByCustomerId )
                            .map( identityObject -> new BulkAction( identityObject.getCustomerId( ), identityObject, BulkActionType.INDEX ) )
                            .collect( Collectors.toList( ) );
                    _identityIndexer.bulk( actions, newIndex );
                    this.getStatus( ).incrementCurrentNbIndexedIdentities( customerIds.size( ) );
                } );
                this.getStatus( ).log( "All batches processed, now switch alias to publish new index.." );
                final String oldIndex = this._identityIndexer.getIndexBehindAlias( IIdentityIndexer.CURRENT_INDEX_ALIAS );
                if ( !StringUtils.equals( oldIndex, newIndex ) )
                {
                    this.getStatus( ).log( "Old index id: " + oldIndex );
                    this._identityIndexer.createOrUpdateAlias( oldIndex, newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                    if ( oldIndex != null )
                    {
                        this.getStatus( ).log( "Delete old index : " + oldIndex );
                        this._identityIndexer.deleteIndex( oldIndex );
                    }
                }
            }
            catch( final ElasticClientException e )
            {
                this.getStatus( ).log( "Failed to reindex " + e.getMessage( ) );
            }
            // Index pending identities
            new MissingIndexTask( ).run( );
        }
        else
        {
            this.getStatus( ).log( "[ERROR] ES not available" );
        }
        stopWatch.stop( );
        final String duration = DurationFormatUtils.formatDurationWords( stopWatch.getTime( ), true, true );

        if ( CollectionUtils.isNotEmpty( customerIdsList ) )
        {
            this.getStatus( ).log( "Re-indexed  " + customerIdsList.size( ) + " identities in " + duration );
        }
        else
        {
            this.getStatus( ).log( "Re-indexed failed" );
        }
        this.close( );
    }

}
