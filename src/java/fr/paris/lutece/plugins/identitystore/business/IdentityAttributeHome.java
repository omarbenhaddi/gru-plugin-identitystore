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

import fr.paris.lutece.plugins.identitystore.service.AttributeChange;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;
import java.util.Map;


/**
 * This class provides instances management methods (create, find, ...) for
 * IdentityAttribute objects
 */
public final class IdentityAttributeHome
{
    // Static variable pointed at the DAO instance
    private static IIdentityAttributeDAO _dao = SpringContextService.getBean( "identitystore.identityAttributeDAO" );
    private static Plugin _plugin = PluginService.getPlugin( "identitystore" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private IdentityAttributeHome(  )
    {
    }

    /**
     * Create an instance of the identityAttribute class
     *
     * @param identityAttribute
     *          The instance of the IdentityAttribute which contains the
     *          informations to store
     * @return The instance of identityAttribute which has been created with its
     *         primary key.
     */
    public static IdentityAttribute create( IdentityAttribute identityAttribute )
    {
        _dao.insert( identityAttribute, _plugin );

        return identityAttribute;
    }

    /**
     * Update of the identityAttribute which is specified in parameter
     *
     * @param identityAttribute
     *          The instance of the IdentityAttribute which contains the data to
     *          store
     * @return The instance of the identityAttribute which has been updated
     */
    public static IdentityAttribute update( IdentityAttribute identityAttribute )
    {
        _dao.store( identityAttribute, _plugin );

        return identityAttribute;
    }

    /**
     * Remove the identityAttribute whose identifier is specified in parameter
     *
     * @param nIdentityId
     *          The identity ID
     * @param nAttributeId
     *          The Attribute ID
     */
    public static void remove( int nIdentityId, int nAttributeId )
    {
        _dao.delete( nIdentityId, nAttributeId, _plugin );
    }

    /**
     * Remove all the identityAttribute of an identity
     *
     * @param nIdentityId
     *          The identity ID
     */
    public static void removeAllAttributes( int nIdentityId )
    {
        _dao.deleteAllAttributes( nIdentityId, _plugin );
    }

    /**
     * Returns an instance of a identityAttribute whose identifier is specified in
     * parameter
     *
     * @param nIdentityId
     *          The identity ID
     * @param nAttributeId
     *          The Attribute ID
     * @return an instance of IdentityAttribute
     */
    public static IdentityAttribute findByPrimaryKey( int nIdentityId, int nAttributeId )
    {
        return _dao.load( nIdentityId, nAttributeId, _plugin );
    }

    /**
     * Load the data of all attributes for a given identity ID which are allowed
     * for the client application provided
     *
     * @param nIdentityId
     *          The identity ID
     * @param strApplicationCode
     *          code of client application
     * @return the list which contains the data of all the identityAttribute
     *         objects
     */
    public static Map<String, IdentityAttribute> getAttributes( int nIdentityId, String strApplicationCode )
    {
        return _dao.selectAttributes( nIdentityId, strApplicationCode, _plugin );
    }

    /**
     * Load the data of all attributes for a given identity ID which are allowed
     * for the client application provided
     *
     * @param nIdentityId
     *          The identity ID
     * @return the map which contains the data of all the identityAttribute
     *         objects
     */
    public static Map<String, IdentityAttribute> getAttributes( int nIdentityId )
    {
        return _dao.selectAttributes( nIdentityId, _plugin );
    }

    /**
     * Load the data of an attribute for a given identity ID and attribute key
     * which is allowed for the client application provided
     *
     * @param nIdentityId
     *          The identity ID
     * @param strApplicationCode
     *          code of client application
     * @param strAttributeKey
     *          attribute key
     * @return the list which contains the data of all the identityAttribute
     *         objects
     */
    public static IdentityAttribute getAttribute( int nIdentityId, String strAttributeKey, String strApplicationCode )
    {
        return _dao.selectAttribute( nIdentityId, strAttributeKey, strApplicationCode, _plugin );
    }

    /**
     * Load the data of all the identityAttribute objects and returns them as a
     * referenceList
     *
     * @return the referenceList which contains the data of all the
     *         identityAttribute objects
     */
    public static ReferenceList getIdentityAttributesReferenceList(  )
    {
        return _dao.selectIdentityAttributesReferenceList( _plugin );
    }

    /**
     * add an attribute change event in history table
     *
     * @param attributeChange
     *          attribute change event
     */
    public static void addAttributeChangeHistory( AttributeChange attributeChange )
    {
        _dao.addAttributeChangeHistory( attributeChange, _plugin );
    }

    /**
     * get attribute change event in history table
     *
     * @param strAttributeKey attributekey
     * @param nIdentityId identityId
     * @return list of attribute changes
     */
    public static List<AttributeChange> getAttributeChangeHistory( int nIdentityId, String strAttributeKey )
    {
        return _dao.getAttributeChangeHistory( nIdentityId, strAttributeKey, _plugin );
    }
}
