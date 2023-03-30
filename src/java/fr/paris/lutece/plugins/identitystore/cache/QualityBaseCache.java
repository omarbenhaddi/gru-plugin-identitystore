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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import org.apache.log4j.Logger;

public class QualityBaseCache extends AbstractCacheableService
{

    private static Logger _logger = Logger.getLogger( QualityBaseCache.class );

    public static final String SERVICE_NAME = "QualityBaseCache";
    public static final String KEY = "QualityBase";

    public QualityBaseCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        _logger.info( "Init service quality base cache" );
        this.resetCache( );
        this.put( KEY, AttributeKeyHome.getQualityBase( ) );
    }

    public void put( final String key, final Integer qualityBase )
    {
        if ( this.getKeys( ).contains( key ) )
        {
            this.removeKey( key );
        }
        this.putInCache( KEY, qualityBase );
        _logger.info( "Quality base has been updated to : " + qualityBase );
    }

    public Integer get( )
    {
        Integer qualityBase = (Integer) this.getFromCache( KEY );
        if ( qualityBase == null )
        {
            qualityBase = AttributeKeyHome.getQualityBase( );
            this.put( KEY, qualityBase );
        }
        return qualityBase;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
