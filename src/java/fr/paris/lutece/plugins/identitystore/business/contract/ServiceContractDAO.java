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
package fr.paris.lutece.plugins.identitystore.business.contract;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.sql.Date;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class provides Data Access methods for ServiceContract objects
 */
public final class ServiceContractDAO implements IServiceContractDAO
{
    private static final String COLUMNS = "name, moa_entity_name, moe_responsible_name, moa_contact_name, moe_entity_name, data_retention_period_in_months, service_type, starting_date, ending_date, authorized_creation, authorized_update, authorized_search, authorized_merge, authorized_account_update, authorized_deletion, authorized_import, authorized_export";
    private static final String JOINED_COLUMNS = "a.id_service_contract, a.name, a.moa_entity_name, a.moe_responsible_name, a.moa_contact_name, a.moe_entity_name, a.data_retention_period_in_months, a.service_type, a.starting_date, a.ending_date, a.authorized_creation, a.authorized_update, a.authorized_search, a.authorized_merge, a.authorized_account_update, a.authorized_deletion, a.authorized_import, a.authorized_export";
    private static final String SQL_QUERY_SELECT = "SELECT id_service_contract, " + COLUMNS
            + " FROM identitystore_service_contract WHERE id_service_contract = ?";
    private static final String SQL_QUERY_SELECT_WITH_CLIENT_APP_ID = "SELECT id_service_contract, " + COLUMNS
            + " FROM identitystore_service_contract WHERE id_client_app = ?";
    private static final String SQL_QUERY_SELECT_ACTIVE_WITH_CLIENT_APP_CODE = "SELECT " + JOINED_COLUMNS
            + " FROM identitystore_service_contract a JOIN identitystore_client_application b on a.id_client_app = b.id_client_app WHERE b.client_code = ? AND CASE WHEN a.ending_date IS NULL THEN NOW() >= a.starting_date ELSE NOW() BETWEEN a.starting_date AND a.ending_date END";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_service_contract (id_client_app, " + COLUMNS
            + " ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_service_contract WHERE id_service_contract = ?";
    private static final String SQL_QUERY_DELETE_WITH_CLIENT_APP_ID = "DELETE FROM identitystore_service_contract WHERE id_client_app = ?";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_service_contract SET name = ?, id_client_app = ?, moa_entity_name = ?, moe_responsible_name = ?, moa_contact_name = ?, moe_entity_name = ?, data_retention_period_in_months = ?, service_type = ?, starting_date = ?, ending_date = ?, authorized_creation = ?, authorized_update = ?, authorized_search = ?, authorized_merge = ?, authorized_account_update = ?, authorized_deletion = ?, authorized_import = ?, authorized_export = ? WHERE id_service_contract = ?";
    private static final String SQL_QUERY_UPDATE_DATE = "UPDATE identitystore_service_contract SET ending_date = ? WHERE id_service_contract = ?";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_service_contract FROM identitystore_service_contract";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT b.client_code, " + JOINED_COLUMNS
            + " FROM identitystore_service_contract a JOIN identitystore_client_application b on a.id_client_app = b.id_client_app WHERE id_service_contract IN (  ";
    private static final String SQL_QUERY_SELECT_BETWEEN_ACTIVE_DATES = "SELECT id_service_contract, " + COLUMNS
            + " FROM identitystore_service_contract WHERE starting_date BETWEEN ? AND ? OR ending_date BETWEEN ? AND ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( ServiceContract serviceContract, int clientApplicationId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, clientApplicationId );
            daoUtil.setString( nIndex++, serviceContract.getName( ) );
            daoUtil.setString( nIndex++, serviceContract.getMoaEntityName( ) );
            daoUtil.setString( nIndex++, serviceContract.getMoeResponsibleName( ) );
            daoUtil.setString( nIndex++, serviceContract.getMoaContactName( ) );
            daoUtil.setString( nIndex++, serviceContract.getMoeEntityName( ) );
            daoUtil.setInt( nIndex++, serviceContract.getDataRetentionPeriodInMonths( ) );
            daoUtil.setString( nIndex++, serviceContract.getServiceType( ) );
            daoUtil.setDate( nIndex++, serviceContract.getStartingDate( ) );
            daoUtil.setDate( nIndex++, serviceContract.getEndingDate( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedCreation( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedUpdate( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedSearch( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedMerge( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedAccountUpdate( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedDeletion( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedImport( ) );
            daoUtil.setBoolean( nIndex, serviceContract.getAuthorizedExport( ) );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                serviceContract.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Optional<ServiceContract> load( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            ServiceContract serviceContract = null;

            if ( daoUtil.next( ) )
            {
                serviceContract = this.extractServiceContract( daoUtil, 1 );
            }

            return Optional.ofNullable( serviceContract );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ServiceContract> loadFromClientApplication( int nKey, Plugin plugin )
    {
        List<ServiceContract> serviceContractList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_WITH_CLIENT_APP_ID, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                serviceContractList.add( this.extractServiceContract( daoUtil, 1 ) );
            }

            return serviceContractList;
        }
    }

    @Override
    public List<ServiceContract> selectActiveServiceContract( String clientCode, Plugin plugin )
    {
        List<ServiceContract> serviceContractList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ACTIVE_WITH_CLIENT_APP_CODE, plugin ) )
        {
            daoUtil.setString( 1, clientCode );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                serviceContractList.add( this.extractServiceContract( daoUtil, 1 ) );
            }

            return serviceContractList;
        }
    }

    @Override
    public void close( ServiceContract serviceContract, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_DATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setDate( nIndex++, serviceContract.getEndingDate( ) );
            daoUtil.setInt( nIndex, serviceContract.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
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
    public void deleteFromClientApp( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_WITH_CLIENT_APP_ID, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( ServiceContract serviceContract, int clientApplicationId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setString( nIndex++, serviceContract.getName( ) );
            daoUtil.setInt( nIndex++, clientApplicationId );
            daoUtil.setString( nIndex++, serviceContract.getMoaEntityName( ) );
            daoUtil.setString( nIndex++, serviceContract.getMoeResponsibleName( ) );
            daoUtil.setString( nIndex++, serviceContract.getMoaContactName( ) );
            daoUtil.setString( nIndex++, serviceContract.getMoeEntityName( ) );
            daoUtil.setInt( nIndex++, serviceContract.getDataRetentionPeriodInMonths( ) );
            daoUtil.setString( nIndex++, serviceContract.getServiceType( ) );
            daoUtil.setDate( nIndex++, serviceContract.getStartingDate( ) );
            daoUtil.setDate( nIndex++, serviceContract.getEndingDate( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedCreation( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedUpdate( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedSearch( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedMerge( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedAccountUpdate( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedDeletion( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedImport( ) );
            daoUtil.setBoolean( nIndex++, serviceContract.getAuthorizedExport( ) );
            daoUtil.setInt( nIndex, serviceContract.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdServiceContractsList( Plugin plugin )
    {
        List<Integer> serviceContractList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                serviceContractList.add( daoUtil.getInt( 1 ) );
            }

            return serviceContractList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ImmutablePair<ServiceContract, String>> selectServiceContractsListByIds( Plugin plugin, List<Integer> listIds )
    {
        List<ImmutablePair<ServiceContract, String>> serviceContractList = new ArrayList<>( );

        StringBuilder builder = new StringBuilder( );

        if ( !listIds.isEmpty( ) )
        {
            for ( int i = 0; i < listIds.size( ); i++ )
            {
                builder.append( "?," );
            }

            String placeHolders = builder.deleteCharAt( builder.length( ) - 1 ).toString( );
            String stmt = SQL_QUERY_SELECTALL_BY_IDS + placeHolders + ")";

            try ( final DAOUtil daoUtil = new DAOUtil( stmt, plugin ) )
            {
                int index = 1;
                for ( Integer n : listIds )
                {
                    daoUtil.setInt( index++, n );
                }

                daoUtil.executeQuery( );
                while ( daoUtil.next( ) )
                {
                    int nIndex = 1;
                    final String clientCode = daoUtil.getString( nIndex++ );
                    serviceContractList.add( new ImmutablePair<>( this.extractServiceContract( daoUtil, nIndex ), clientCode ) );
                }
            }
        }
        return serviceContractList;

    }

    @Override
    public List<ServiceContract> selectServiceContractBetweenDate( Plugin plugin, Date startingDate, Date endingDate )
    {
        List<ServiceContract> serviceContractList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BETWEEN_ACTIVE_DATES, plugin ) )
        {
            daoUtil.setDate( 1, startingDate );
            daoUtil.setDate( 2, endingDate );
            daoUtil.setDate( 3, startingDate );
            daoUtil.setDate( 4, endingDate );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                serviceContractList.add( this.extractServiceContract( daoUtil, 1 ) );
            }

            return serviceContractList;
        }
    }

    private ServiceContract extractServiceContract( final DAOUtil daoUtil, final int offset )
    {
        final ServiceContract serviceContract = new ServiceContract( );
        int nIndex = offset;
        serviceContract.setId( daoUtil.getInt( nIndex++ ) );
        serviceContract.setName( daoUtil.getString( nIndex++ ) );
        serviceContract.setMoaEntityName( daoUtil.getString( nIndex++ ) );
        serviceContract.setMoeResponsibleName( daoUtil.getString( nIndex++ ) );
        serviceContract.setMoaContactName( daoUtil.getString( nIndex++ ) );
        serviceContract.setMoeEntityName( daoUtil.getString( nIndex++ ) );
        serviceContract.setDataRetentionPeriodInMonths( daoUtil.getInt( nIndex++ ) );
        serviceContract.setServiceType( daoUtil.getString( nIndex++ ) );
        serviceContract.setStartingDate( daoUtil.getDate( nIndex++ ) );
        serviceContract.setEndingDate( daoUtil.getDate( nIndex++ ) );
        serviceContract.setAuthorizedCreation( daoUtil.getBoolean( nIndex++ ) );
        serviceContract.setAuthorizedUpdate( daoUtil.getBoolean( nIndex++ ) );
        serviceContract.setAuthorizedSearch( daoUtil.getBoolean( nIndex++ ) );
        serviceContract.setAuthorizedMerge( daoUtil.getBoolean( nIndex++ ) );
        serviceContract.setAuthorizedAccountUpdate( daoUtil.getBoolean( nIndex++ ) );
        serviceContract.setAuthorizedDeletion( daoUtil.getBoolean( nIndex++ ) );
        serviceContract.setAuthorizedImport( daoUtil.getBoolean( nIndex++ ) );
        serviceContract.setAuthorizedExport( daoUtil.getBoolean( nIndex ) );
        return serviceContract;
    }
}
