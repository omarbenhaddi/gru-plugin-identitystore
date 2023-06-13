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
package fr.paris.lutece.plugins.identitystore.business.identity;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class provides Data Access methods for Identity objects
 */
public final class IdentityDAO implements IIdentityDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_identity ) FROM identitystore_identity";
    private static final String SQL_QUERY_SELECT = "SELECT id_identity, connection_id, customer_id, is_mon_paris_active FROM identitystore_identity WHERE id_identity = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity (  connection_id, customer_id, date_create, is_mon_paris_active ) VALUES ( ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity WHERE id_identity = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity SET id_identity = ?, connection_id = ?, customer_id = ?, last_update_date = ?, is_mon_paris_active = ? WHERE id_identity = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, is_mon_paris_active FROM identitystore_identity";
    private static final String SQL_QUERY_SELECTALL_FULL = "SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active FROM identitystore_identity";
    private static final String SQL_QUERY_SELECTALL_CUSTOMER_IDS = "SELECT customer_id FROM identitystore_identity";
    private static final String SQL_QUERY_SELECTALL_CUSTOMER_IDS_WITH_LIMIT = "SELECT customer_id FROM identitystore_identity ORDER BY id_identity ASC LIMIT ?, ?";
    private static final String SQL_QUERY_SELECT_BY_CONNECTION_ID = "SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, date_create, last_update_date, date_merge, is_mon_paris_active FROM identitystore_identity WHERE connection_id = ?";
    private static final String SQL_QUERY_SELECT_BY_CUSTOMER_ID = "SELECT id_identity, connection_id,  customer_id, is_deleted, is_merged, date_create, last_update_date, date_merge, is_mon_paris_active FROM identitystore_identity WHERE customer_id = ?";
    private static final String SQL_QUERY_SELECT_NOT_MERGED_BY_CUSTOMER_ID = "WITH RECURSIVE identity_tree AS ("
            + "    SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active"
            + "    FROM identitystore_identity" + "    WHERE customer_id = ?" + "    UNION ALL"
            + "    SELECT id.id_identity, id.connection_id, id.customer_id, id.is_deleted, id.is_merged, id.id_master_identity, id.date_create, id.last_update_date, id.date_merge, id.is_mon_paris_active"
            + "    FROM identitystore_identity id" + "        INNER JOIN identity_tree mtree ON mtree.id_master_identity = id.id_identity" + " )"
            + " select id_identity, connection_id, customer_id, is_deleted, is_merged, date_create, last_update_date, date_merge, is_mon_paris_active from identity_tree where is_merged = 0;";

    private static final String SQL_QUERY_SELECT_NOT_MERGED_BY_CONNECTION_ID = "WITH RECURSIVE identity_tree AS ("
            + "    SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active"
            + "    FROM identitystore_identity" + "    WHERE connection_id = ?" + "    UNION ALL"
            + "    SELECT id.id_identity, id.connection_id, id.customer_id, id.is_deleted, id.is_merged, id.id_master_identity, id.date_create, id.last_update_date, id.date_merge, id.is_mon_paris_active"
            + "    FROM identitystore_identity id" + "        INNER JOIN identity_tree mtree ON mtree.id_master_identity = id.id_identity" + " )"
            + " select id_identity, connection_id, customer_id, is_deleted, is_merged, date_create, last_update_date, date_merge, is_mon_paris_active from identity_tree where is_merged = 0;";

    private static final String SQL_QUERY_SELECT_NOT_MERGED_BY_BOTH_CONNECTION_AND_CUSTOMER_ID = "WITH RECURSIVE identity_tree AS ("
            + "    SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active"
            + "    FROM identitystore_identity" + "    WHERE customer_id = ? AND connection_id = ?" + "    UNION ALL"
            + "    SELECT id.id_identity, id.connection_id, id.customer_id, id.is_deleted, id.is_merged, id.id_master_identity, id.date_create, id.last_update_date, id.date_merge, id.is_mon_paris_active"
            + "    FROM identitystore_identity id" + "        INNER JOIN identity_tree mtree ON mtree.id_master_identity = id.id_identity" + " )"
            + " select id_identity, connection_id, customer_id, is_deleted, is_merged, date_create, last_update_date, date_merge, is_mon_paris_active from identity_tree where is_merged = 0;";
    private static final String SQL_QUERY_SELECT_ID_BY_CONNECTION_ID = "SELECT id_identity, is_deleted, is_merged FROM identitystore_identity WHERE connection_id = ?";
    private static final String SQL_QUERY_SELECT_BY_ATTRIBUTE = "SELECT DISTINCT a.id_identity, a.connection_id, a.customer_id, a.is_deleted, a.is_merged, a.date_create, a.last_update_date, a.date_merge, a.is_mon_paris_active "
            + " FROM identitystore_identity a,  identitystore_identity_attribute b " + " WHERE a.id_identity = b.id_identity AND b.attribute_value ";
    private static final String SQL_QUERY_FILTER_ATTRIBUTE = " AND b.id_attribute = ? ";
    private static final String SQL_QUERY_SELECT_BY_ATTRIBUTES_FOR_API_SEARCH = "SELECT DISTINCT a.id_identity, a.connection_id, a.customer_id, a.is_deleted, a.is_merged, a.date_create, a.last_update_date, a.date_merge, a.is_mon_paris_active"
            + " FROM identitystore_identity a, identitystore_identity_attribute b, identitystore_ref_attribute c"
            + " WHERE a.id_identity = b.id_identity AND b.id_attribute = c.id_attribute AND (${filter})"
            + " GROUP BY a.id_identity HAVING COUNT(DISTINCT b.id_attribute) >= ? LIMIT ${limit}";
    private static final String SQL_QUERY_FILTER_ATTRIBUTE_FOR_API_SEARCH = "(c.key_name = ? AND b.attribute_value IN (${list}))";
    private static final String SQL_QUERY_SELECT_ALL_BY_CONNECTION_ID = "SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, date_create, last_update_date, date_merge, is_mon_paris_active FROM identitystore_identity WHERE connection_id ";
    private static final String SQL_QUERY_SELECT_ALL_BY_CUSTOMER_ID = "SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, date_create, last_update_date, date_merge, is_mon_paris_active  FROM identitystore_identity WHERE customer_id ";
    private static final String SQL_QUERY_SELECT_BY_ALL_ATTRIBUTES_CID_GUID_LIKE = "SELECT DISTINCT a.id_identity, a.connection_id, a.customer_id, a.is_deleted, a.is_merged, a.date_create, a.last_update_date, a.date_merge, a.is_mon_paris_active FROM identitystore_identity a,  identitystore_identity_attribute b "
            + " WHERE (a.id_identity = b.id_identity AND b.attribute_value LIKE ? )" + " OR a.customer_id LIKE ? OR a.connection_id LIKE ?";
    private static final String SQL_QUERY_SELECT_BY_ALL_ATTRIBUTES_CID_GUID = "SELECT DISTINCT a.id_identity, a.connection_id, a.customer_id, a.is_deleted, a.is_merged, a.date_create, a.last_update_date, a.date_merge, a.is_mon_paris_active FROM identitystore_identity a,  identitystore_identity_attribute b "
            + " WHERE (a.id_identity = b.id_identity AND b.attribute_value = ? )" + " OR a.customer_id = ? OR a.connection_id = ?";
    private static final String SQL_QUERY_SOFT_DELETE = "UPDATE identitystore_identity SET is_deleted = 1, date_delete = now(), connection_id = null WHERE id_identity = ?";
    private static final String SQL_QUERY_MERGE = "UPDATE identitystore_identity SET is_merged = 1, date_merge = now(), id_master_identity = ? WHERE id_identity = ?";
    private static final String SQL_QUERY_SELECT_BY_ATTRIBUTE_EXISTING = "SELECT DISTINCT a.id_identity, a.connection_id, a.customer_id, a.is_deleted, a.is_merged, a.date_create, a.last_update_date, a.date_merge, a.is_mon_paris_active"
            + " FROM identitystore_identity a" + " JOIN identitystore_identity_attribute b ON a.id_identity = b.id_identity"
            + " WHERE b.id_attribute IN (${id_attribute_list}) AND (${not_merged}) AND (${not_suspicious})"
            + " GROUP BY a.id_identity HAVING COUNT (DISTINCT b.id_attribute) = ${count} LIMIT ${limit}";
    private static final String SQL_QUERY_FILTER_NOT_MERGED = "a.is_merged = 0 AND a.date_merge IS NULL";
    private static final String SQL_QUERY_FILTER_NOT_SUSPICIOUS = "NOT EXISTS (SELECT c.id_suspicious_identity FROM identitystore_quality_suspicious_identity c WHERE c.customer_id = a.customer_id)";

    /**
     * Generates a new customerId key using Java UUID
     *
     * @return The new customerID
     */
    public String newCustomerIdKey( )
    {
        return UUID.randomUUID( ).toString( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( Identity identity, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            identity.setCreationDate( new Timestamp( new Date( ).getTime( ) ) );

            int nIndex = 1;
            identity.setCustomerId( newCustomerIdKey( ) );
            daoUtil.setString( nIndex++, identity.getConnectionId( ) );
            daoUtil.setString( nIndex++, identity.getCustomerId( ) );
            daoUtil.setTimestamp( nIndex++, identity.getCreationDate( ) );
            daoUtil.setBoolean( nIndex, identity.isMonParisActive( ) );

            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                identity.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Identity load( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = new Identity( );

                int nIndex = 1;

                identity.setId( daoUtil.getInt( nIndex++ ) );
                identity.setConnectionId( daoUtil.getString( nIndex++ ) );
                identity.setCustomerId( daoUtil.getString( nIndex++ ) );
                identity.setMonParisActive( daoUtil.getBoolean( nIndex ) );

            }

            if ( identity != null )
            {
                identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ) ) );
            }

            return identity;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void hardDelete( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void softDelete( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SOFT_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void merge( Identity identity, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_MERGE, plugin ) )
        {
            daoUtil.setInt( 1, identity.getMasterIdentityId( ) );
            daoUtil.setInt( 2, identity.getId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( Identity identity, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            identity.setLastUpdateDate( new Timestamp( new Date( ).getTime( ) ) );

            daoUtil.setInt( nIndex++, identity.getId( ) );
            daoUtil.setString( nIndex++, identity.getConnectionId( ) );
            daoUtil.setString( nIndex++, identity.getCustomerId( ) );
            daoUtil.setTimestamp( nIndex++, identity.getLastUpdateDate( ) );
            daoUtil.setBoolean( nIndex++, identity.isMonParisActive( ) );
            daoUtil.setInt( nIndex, identity.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<String> selectCustomerIdsList( Plugin plugin )
    {
        final List<String> listIds = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_CUSTOMER_IDS, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                String identity = daoUtil.getString( 1 );
                listIds.add( identity );
            }

            return listIds;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> selectCustomerIdsList( int nStart, int nLimit, Plugin plugin )
    {
        final List<String> listIds = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_CUSTOMER_IDS_WITH_LIMIT, plugin ) )
        {
            daoUtil.setInt( 1, nStart );
            daoUtil.setInt( 2, nLimit );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                String identity = daoUtil.getString( 1 );
                listIds.add( identity );
            }

            return listIds;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectIdentitysReferenceList( Plugin plugin )
    {
        final ReferenceList identityList = new ReferenceList( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                identityList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return identityList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectAll( Plugin plugin )
    {
        final List<Identity> identityList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_FULL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                int nIndex = 1;
                final Identity identity = new Identity( );
                identityList.add( identity );
                identity.setId( daoUtil.getInt( nIndex++ ) );
                identity.setConnectionId( daoUtil.getString( nIndex++ ) );
                identity.setCustomerId( daoUtil.getString( nIndex++ ) );
                identity.setDeleted( daoUtil.getBoolean( nIndex++ ) );
                identity.setMerged( daoUtil.getBoolean( nIndex++ ) );
                identity.setMasterIdentityId( daoUtil.getInt( nIndex++ ) );
                identity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setMergeDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setMonParisActive( daoUtil.getBoolean( nIndex ) );
            }

            return identityList;
        }
    }

    @Override
    public Identity selectByConnectionId( String strConnectionId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CONNECTION_ID, plugin ) )
        {
            daoUtil.setString( 1, strConnectionId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public Identity selectByCustomerId( String strCustomerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public Identity selectNotMergedByCustomerId( String strCustomerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NOT_MERGED_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public Identity selectNotMergedByConnectionId( String strCustomerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NOT_MERGED_BY_CONNECTION_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public Identity selectNotMergedByCustomerIdAndConnectionID( String strCustomerId, String strConnectionId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NOT_MERGED_BY_BOTH_CONNECTION_AND_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.setString( 2, strConnectionId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public int selectIdByConnectionId( String strConnectionId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ID_BY_CONNECTION_ID, plugin ) )
        {
            daoUtil.setString( 1, strConnectionId );
            daoUtil.executeQuery( );

            int nIdentityId = -1;

            if ( daoUtil.next( ) )
            {
                nIdentityId = daoUtil.getInt( 1 );
            }

            return nIdentityId;
        }
    }

    /**
     * return Identity object from select query
     *
     * @param daoUtil
     *            daoUtil initialized with select query
     * @return Identity load from result
     */
    private Identity getIdentityFromQuery( DAOUtil daoUtil )
    {
        Identity identity = new Identity( );

        int nIndex = 1;

        identity.setId( daoUtil.getInt( nIndex++ ) );
        identity.setConnectionId( daoUtil.getString( nIndex++ ) );
        identity.setCustomerId( daoUtil.getString( nIndex++ ) );
        identity.setDeleted( daoUtil.getBoolean( nIndex++ ) );
        identity.setMerged( daoUtil.getBoolean( nIndex++ ) );
        identity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
        identity.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
        identity.setMergeDate( daoUtil.getTimestamp( nIndex++ ) );
        identity.setMonParisActive( daoUtil.getBoolean( nIndex ) );
        return identity;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectByAttributeValue( String strAttributeId, String strAttributeValue, Plugin plugin )
    {
        List<Identity> listIdentities = new ArrayList<>( );
        String strSQL = SQL_QUERY_SELECT_BY_ATTRIBUTE;
        String strValue = strAttributeValue;
        if ( strAttributeValue.contains( "*" ) )
        {
            strValue = strValue.replace( '*', '%' );
            strSQL += " LIKE ?";
        }
        else
        {
            strSQL += " = ?";
        }

        if ( StringUtils.isNotEmpty( strAttributeId ) )
        {
            strSQL += SQL_QUERY_FILTER_ATTRIBUTE;
        }

        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            daoUtil.setString( 1, strValue );

            if ( StringUtils.isNotEmpty( strAttributeId ) )
            {
                daoUtil.setInt( 2, Integer.parseInt( strAttributeId ) );
            }
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIdentities.add( this.getIdentityFromQuery( daoUtil ) );
            }

            return listIdentities;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectByAttributesValueForApiSearch( Map<String, List<String>> mapAttributes, int nMaxNbIdentityReturned, Plugin plugin )
    {
        List<Identity> listIdentities = new ArrayList<>( );

        Queue<String> queueAttributeId = new ArrayDeque<>( );
        List<String> listAttributeFilter = new ArrayList<>( );

        if ( mapAttributes == null || mapAttributes.isEmpty( ) )
        {
            return listIdentities;
        }

        for ( Map.Entry<String, List<String>> entryAttribute : mapAttributes.entrySet( ) )
        {
            String strAttributeId = entryAttribute.getKey( );
            List<String> listAttributeValues = entryAttribute.getValue( );
            if ( listAttributeValues == null || listAttributeValues.isEmpty( ) )
            {
                continue;
            }

            queueAttributeId.add( strAttributeId );

            List<String> listIn = new ArrayList<>( );

            for ( int i = 0; i < listAttributeValues.size( ); i++ )
            {
                listIn.add( "?" );
            }

            listAttributeFilter.add( SQL_QUERY_FILTER_ATTRIBUTE_FOR_API_SEARCH.replace( "${list}", String.join( ", ", listIn ) ) );
        }

        if ( listAttributeFilter.isEmpty( ) )
        {
            return listIdentities;
        }

        String strSQL = SQL_QUERY_SELECT_BY_ATTRIBUTES_FOR_API_SEARCH.replace( "${filter}", String.join( " OR ", listAttributeFilter ) );
        strSQL = strSQL.replace( "${limit}", String.valueOf( nMaxNbIdentityReturned ) );

        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            int nIndex = 1;

            for ( String strAttributeId : queueAttributeId )
            {
                daoUtil.setString( nIndex++, strAttributeId );

                for ( String strAttributeValue : mapAttributes.get( strAttributeId ) )
                {
                    daoUtil.setString( nIndex++, strAttributeValue );
                }
            }

            daoUtil.setInt( nIndex++, queueAttributeId.size( ) );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Identity identity = getIdentityFromQuery( daoUtil );
                listIdentities.add( identity );
            }

            return listIdentities;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectByAllAttributesValue( String strAttributeValue, Plugin plugin )
    {
        List<Identity> listIdentities = new ArrayList<Identity>( );
        String strSQL = SQL_QUERY_SELECT_BY_ALL_ATTRIBUTES_CID_GUID;

        String strFinalAttributeValue = strAttributeValue;
        if ( strAttributeValue.contains( "*" ) )
        {
            strFinalAttributeValue = strAttributeValue.replace( '*', '%' );
            strSQL = SQL_QUERY_SELECT_BY_ALL_ATTRIBUTES_CID_GUID_LIKE;
        }

        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            daoUtil.setString( 1, strFinalAttributeValue );
            daoUtil.setString( 2, strFinalAttributeValue );
            daoUtil.setString( 3, strFinalAttributeValue );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Identity identity = getIdentityFromQuery( daoUtil );
                listIdentities.add( identity );
            }

            return listIdentities;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectAllByCustomerId( String strCustomerId, Plugin plugin )
    {
        List<Identity> listIdentities = new ArrayList<>( );
        String strSQL = SQL_QUERY_SELECT_ALL_BY_CUSTOMER_ID;

        String strFinalCustomerId = strCustomerId;
        if ( strCustomerId.contains( "*" ) )
        {
            strFinalCustomerId = strCustomerId.replace( '*', '%' );
            strSQL += "LIKE ?";
        }
        else
        {
            strSQL += "= ?";
        }

        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            daoUtil.setString( 1, strFinalCustomerId );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Identity identity = getIdentityFromQuery( daoUtil );
                listIdentities.add( identity );
            }

            return listIdentities;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectAllByConnectionId( String strConnectionId, Plugin plugin )
    {
        List<Identity> listIdentities = new ArrayList<Identity>( );
        String strSQL = SQL_QUERY_SELECT_ALL_BY_CONNECTION_ID;

        String strFinalConnectionId = strConnectionId;
        if ( strConnectionId.contains( "*" ) )
        {
            strFinalConnectionId = strConnectionId.replace( '*', '%' );
            strSQL += "LIKE ?";
        }
        else
        {
            strSQL += "= ?";
        }

        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            daoUtil.setString( 1, strFinalConnectionId );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Identity identity = getIdentityFromQuery( daoUtil );
                listIdentities.add( identity );
            }

            return listIdentities;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectByAttributeExisting( final List<Integer> idAttributeList, final boolean notMerged, final boolean notSuspicious, final int limit,
            final Plugin plugin )
    {
        final List<Identity> listIdentities = new ArrayList<>( );
        if ( idAttributeList == null || idAttributeList.isEmpty( ) )
        {
            return listIdentities;
        }
        String sql = SQL_QUERY_SELECT_BY_ATTRIBUTE_EXISTING
                .replace( "${id_attribute_list}", idAttributeList.stream( ).map( Object::toString ).collect( Collectors.joining( ", " ) ) )
                .replace( "${not_merged}", ( notMerged ? SQL_QUERY_FILTER_NOT_MERGED : "1=1" ) )
                .replace( "${not_suspicious}", ( notSuspicious ? SQL_QUERY_FILTER_NOT_SUSPICIOUS : "1=1" ) )
                .replace( "${count}", String.valueOf( idAttributeList.size( ) ) ).replace( "${limit}", String.valueOf( limit ) );
        try ( final DAOUtil daoUtil = new DAOUtil( sql, plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                final Identity identity = getIdentityFromQuery( daoUtil );
                listIdentities.add( identity );
            }
        }
        return listIdentities;
    }

}
