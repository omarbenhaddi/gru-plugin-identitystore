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
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * This class provides Data Access methods for Identity objects
 */
public final class IdentityDAO implements IIdentityDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_identity ) FROM identitystore_identity";
    private static final String SQL_QUERY_NEW_CUSTOMER_ID_KEY = "SELECT max( customer_id ) FROM identitystore_identity";
    private static final String SQL_QUERY_SELECT = "SELECT id_identity, connection_id, customer_id FROM identitystore_identity WHERE id_identity = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity ( id_identity, connection_id, customer_id ) VALUES ( ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity WHERE id_identity = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity SET id_identity = ?, connection_id = ?, customer_id = ? WHERE id_identity = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_identity, connection_id, customer_id FROM identitystore_identity";
    private static final String SQL_QUERY_SELECTALL_CUSTOMER_IDS = "SELECT customer_id FROM identitystore_identity";
    private static final String SQL_QUERY_SELECT_BY_CONNECTION_ID = "SELECT id_identity, connection_id, customer_id FROM identitystore_identity WHERE connection_id = ?";
    private static final String SQL_QUERY_SELECT_BY_CUSTOMER_ID = "SELECT id_identity, connection_id, customer_id FROM identitystore_identity WHERE customer_id = ?";

    /**
     * Generates a new primary key
     *
     * @param plugin
     *          The Plugin
     * @return The new primary key
     */
    public int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery(  );

        int nKey = 1;

        if ( daoUtil.next(  ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free(  );

        return nKey;
    }

    /**
     * Generates a new customerId key
     *
     * @param plugin
     *          The Plugin
     * @return The new primary key
     */
    public int newCustomerIdKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_CUSTOMER_ID_KEY, plugin );
        daoUtil.executeQuery(  );

        int nKey = 1;

        if ( daoUtil.next(  ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free(  );

        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( Identity identity, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        identity.setId( newPrimaryKey( plugin ) );

        int nIndex = 1;
        identity.setCustomerId( newCustomerIdKey( plugin ) );
        daoUtil.setInt( nIndex++, identity.getId(  ) );
        daoUtil.setString( nIndex++, identity.getConnectionId(  ) );
        daoUtil.setInt( nIndex++, identity.getCustomerId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Identity load( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeQuery(  );

        Identity identity = null;

        if ( daoUtil.next(  ) )
        {
            identity = new Identity(  );

            int nIndex = 1;

            identity.setId( daoUtil.getInt( nIndex++ ) );
            identity.setConnectionId( daoUtil.getString( nIndex++ ) );
            identity.setCustomerId( daoUtil.getInt( nIndex++ ) );
        }

        daoUtil.free(  );

        if ( identity != null )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId(  ) ) );
        }

        return identity;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( Identity identity, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, identity.getId(  ) );
        daoUtil.setString( nIndex++, identity.getConnectionId(  ) );
        daoUtil.setInt( nIndex++, identity.getCustomerId(  ) );
        daoUtil.setInt( nIndex, identity.getId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectIdentitysList( Plugin plugin )
    {
        List<Identity> identityList = new ArrayList<Identity>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            Identity identity = new Identity(  );
            identity = getIdentityFromQuery( daoUtil );
            identityList.add( identity );
        }

        daoUtil.free(  );

        for ( Identity identity : identityList )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId(  ) ) );
        }

        return identityList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectCustomerIdsList( Plugin plugin )
    {
        List<Integer> listIds = new ArrayList<Integer>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_CUSTOMER_IDS, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            Integer identity = Integer.valueOf( daoUtil.getInt( 1 ) );
            listIds.add( identity );
        }

        daoUtil.free(  );

        return listIds;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectIdentitysReferenceList( Plugin plugin )
    {
        ReferenceList identityList = new ReferenceList(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            identityList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free(  );

        return identityList;
    }

    @Override
    public Identity selectByConnectionId( String strConnectionId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CONNECTION_ID, plugin );
        daoUtil.setString( 1, strConnectionId );
        daoUtil.executeQuery(  );

        Identity identity = null;

        if ( daoUtil.next(  ) )
        {
            identity = getIdentityFromQuery( daoUtil );
        }

        daoUtil.free(  );

        return identity;
    }

    @Override
    public Identity selectByCustomerId( int nCustomerId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CUSTOMER_ID, plugin );
        daoUtil.setInt( 1, nCustomerId );
        daoUtil.executeQuery(  );

        Identity identity = null;

        if ( daoUtil.next(  ) )
        {
            identity = getIdentityFromQuery( daoUtil );
        }

        daoUtil.free(  );

        return identity;
    }

    /**
     * return Identity object from select query
     *
     * @param daoUtil
     *          daoUtil initialized with select query
     * @return Identity load from result
     */
    private Identity getIdentityFromQuery( DAOUtil daoUtil )
    {
        Identity identity = new Identity(  );

        int nIndex = 1;

        identity.setId( daoUtil.getInt( nIndex++ ) );
        identity.setConnectionId( daoUtil.getString( nIndex++ ) );
        identity.setCustomerId( daoUtil.getInt( nIndex++ ) );

        return identity;
    }
}
