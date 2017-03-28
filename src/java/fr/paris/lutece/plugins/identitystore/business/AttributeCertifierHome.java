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
package fr.paris.lutece.plugins.identitystore.business;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for AttributeCertifier objects
 */
public final class AttributeCertifierHome
{
    // Static variable pointed at the DAO instance
    private static IAttributeCertifierDAO _dao = SpringContextService.getBean( IAttributeCertifierDAO.BEAN_NAME );
    private static Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private AttributeCertifierHome( )
    {
    }

    /**
     * Create an instance of the attributeCertifier class
     *
     * @param attributeCertifier
     *            The instance of the AttributeCertifier which contains the informations to store
     * @return The instance of attributeCertifier which has been created with its primary key.
     */
    public static AttributeCertifier create( AttributeCertifier attributeCertifier )
    {
        _dao.insert( attributeCertifier, _plugin );

        return attributeCertifier;
    }

    /**
     * Update of the attributeCertifier which is specified in parameter
     *
     * @param attributeCertifier
     *            The instance of the AttributeCertifier which contains the data to store
     * @return The instance of the attributeCertifier which has been updated
     */
    public static AttributeCertifier update( AttributeCertifier attributeCertifier )
    {
        _dao.store( attributeCertifier, _plugin );

        return attributeCertifier;
    }

    /**
     * Remove the attributeCertifier whose identifier is specified in parameter
     *
     * @param nKey
     *            The attributeCertifier Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a attributeCertifier whose identifier is specified in parameter
     *
     * @param nKey
     *            The attributeCertifier primary key
     * @return an instance of AttributeCertifier
     */
    public static AttributeCertifier findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Returns an instance of a attributeCertifier whose identifier is specified in parameter
     *
     * @param strCode
     *            The attributeCertifier code
     * @return an instance of AttributeCertifier
     */
    public static AttributeCertifier findByCode( String strCode )
    {
        return _dao.selectByCode( strCode, _plugin );
    }

    /**
     * Load the data of all the attributeCertifier objects and returns them as a list
     *
     * @return the list which contains the data of all the attributeCertifier objects
     */
    public static List<AttributeCertifier> getAttributeCertifiersList( )
    {
        return _dao.selectAttributeCertifiersList( _plugin );
    }

    /**
     * Load the id of all the attributeCertifier objects and returns them as a list
     *
     * @return the list which contains the id of all the attributeCertifier objects
     */
    public static List<Integer> getIdAttributeCertifiersList( )
    {
        return _dao.selectIdAttributeCertifiersList( _plugin );
    }

    /**
     * Load the data of all the attributeCertifier objects and returns them as a referenceList
     *
     * @return the referenceList which contains the data of all the attributeCertifier objects
     */
    public static ReferenceList getAttributeCertifiersReferenceList( )
    {
        return _dao.selectAttributeCertifiersReferenceList( _plugin );
    }
}