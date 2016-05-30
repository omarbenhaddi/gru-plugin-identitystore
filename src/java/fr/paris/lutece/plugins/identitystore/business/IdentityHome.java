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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;


/**
 * This class provides instances management methods (create, find, ...) for Identity objects
 */
public final class IdentityHome
{
    // Static variable pointed at the DAO instance
    private static IIdentityDAO _dao = SpringContextService.getBean( "identitystore.identityDAO" );
    private static Plugin _plugin = PluginService.getPlugin( "identitystore" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private IdentityHome(  )
    {
    }

    /**
     * Create an instance of the identity class
     * @param identity The instance of the Identity which contains the informations to store
     * @return The  instance of identity which has been created with its primary key.
     */
    public static Identity create( Identity identity )
    {
        _dao.insert( identity, _plugin );

        return identity;
    }

    /**
     * Update of the identity which is specified in parameter
     * @param identity The instance of the Identity which contains the data to store
     * @return The instance of the  identity which has been updated
     */
    public static Identity update( Identity identity )
    {
        _dao.store( identity, _plugin );

        return identity;
    }

    /**
     * Remove the identity whose identifier is specified in parameter
     * @param nIdentityId The identity Id
     */
    public static void remove( int nIdentityId )
    {
        IdentityAttributeHome.removeAllAttributes( nIdentityId );
        _dao.delete( nIdentityId, _plugin );
    }

    /**
     * Returns an instance of a identity whose identifier is specified in parameter
     * @param nKey The identity primary key
     * @return an instance of Identity
     */
    public static Identity findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Find by connection ID
     * @param strConnectionId The connection ID
     * @return The Identity
     */
    public static Identity findByConnectionId( String strConnectionId )
    {
        return _dao.selectByConnectionId( strConnectionId, _plugin );
    }

    /**
     * Find by connection ID
     * @param strConnectionId The connection ID
     * @param strClientAppCode code of application client which requires infos
     * @return The Identity
     */
    public static Identity findByConnectionId( String strConnectionId, String strClientAppCode )
    {
        Identity identity = _dao.selectByConnectionId( strConnectionId, _plugin );

        if ( identity != null )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId(  ), strClientAppCode ) );
        }

        return identity;
    }

    /**
     * Load the data of all the identity objects and returns them as a list
     * @return the list which contains the data of all the identity objects
     */
    public static List<Identity> getIdentitysList(  )
    {
        return _dao.selectIdentitysList( _plugin );
    }

    /**
     * Load the data of all the identity objects and returns them as a referenceList
     * @return the referenceList which contains the data of all the identity objects
     */
    public static ReferenceList getIdentitysReferenceList(  )
    {
        return _dao.selectIdentitysReferenceList( _plugin );
    }
}
