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
 * This class provides Data Access methods for AttributeCertificate objects
 */
public final class AttributeCertificateDAO implements IAttributeCertificateDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_attribute_certificate ) FROM identitystore_attibute_certificate";
    private static final String SQL_QUERY_SELECT = "SELECT id_attribute_certificate, id_certifier, certificate_date, certificate_level, expiration_date FROM identitystore_attibute_certificate WHERE id_attribute_certificate = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_attibute_certificate ( id_attribute_certificate, id_certifier, certificate_date, certificate_level, expiration_date ) VALUES ( ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_attibute_certificate WHERE id_attribute_certificate = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_attibute_certificate SET id_attribute_certificate = ?, id_certifier = ?, certificate_date = ?, certificate_level = ?, expiration_date = ? WHERE id_attribute_certificate = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_attribute_certificate, id_certifier, certificate_date, certificate_level, expiration_date FROM identitystore_attibute_certificate";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_attribute_certificate FROM identitystore_attibute_certificate";

    /**
     * Generates a new primary key
     * @param plugin The Plugin
     * @return The new primary key
     */
    public int newPrimaryKey( Plugin plugin)
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK , plugin  );
        daoUtil.executeQuery( );
        int nKey = 1;

        if( daoUtil.next( ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free();
        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( AttributeCertificate attributeCertificate, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        attributeCertificate.setId( newPrimaryKey( plugin ) );
        int nIndex = 1;
        
        daoUtil.setInt( nIndex++ , attributeCertificate.getId( ) );
        daoUtil.setInt( nIndex++ , attributeCertificate.getIdCertifier( ) );
        daoUtil.setDate( nIndex++ , attributeCertificate.getCertificateDate( ) );
        daoUtil.setInt( nIndex++ , attributeCertificate.getCertificateLevel( ) );
        daoUtil.setDate( nIndex++ , attributeCertificate.getExpirationDate( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeCertificate load( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1 , nKey );
        daoUtil.executeQuery( );
        AttributeCertificate attributeCertificate = null;

        if ( daoUtil.next( ) )
        {
            attributeCertificate = new AttributeCertificate();
            int nIndex = 1;
            
            attributeCertificate.setId( daoUtil.getInt( nIndex++ ) );
            attributeCertificate.setIdCertifier( daoUtil.getInt( nIndex++ ) );
            attributeCertificate.setCertificateDate( daoUtil.getDate( nIndex++ ) );
            attributeCertificate.setCertificateLevel( daoUtil.getInt( nIndex++ ) );
            attributeCertificate.setExpirationDate( daoUtil.getDate( nIndex++ ) );
        }

        daoUtil.free( );
        return attributeCertificate;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1 , nKey );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( AttributeCertificate attributeCertificate, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;
        
        daoUtil.setInt( nIndex++ , attributeCertificate.getId( ) );
        daoUtil.setInt( nIndex++ , attributeCertificate.getIdCertifier( ) );
        daoUtil.setDate( nIndex++ , attributeCertificate.getCertificateDate( ) );
        daoUtil.setInt( nIndex++ , attributeCertificate.getCertificateLevel( ) );
        daoUtil.setDate( nIndex++ , attributeCertificate.getExpirationDate( ) );
        daoUtil.setInt( nIndex , attributeCertificate.getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeCertificate> selectAttributeCertificatesList( Plugin plugin )
    {
        List<AttributeCertificate> attributeCertificateList = new ArrayList<>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AttributeCertificate attributeCertificate = new AttributeCertificate(  );
            int nIndex = 1;
            
            attributeCertificate.setId( daoUtil.getInt( nIndex++ ) );
            attributeCertificate.setIdCertifier( daoUtil.getInt( nIndex++ ) );
            attributeCertificate.setCertificateDate( daoUtil.getDate( nIndex++ ) );
            attributeCertificate.setCertificateLevel( daoUtil.getInt( nIndex++ ) );
            attributeCertificate.setExpirationDate( daoUtil.getDate( nIndex++ ) );

            attributeCertificateList.add( attributeCertificate );
        }

        daoUtil.free( );
        return attributeCertificateList;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdAttributeCertificatesList( Plugin plugin )
    {
        List<Integer> attributeCertificateList = new ArrayList<>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            attributeCertificateList.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );
        return attributeCertificateList;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectAttributeCertificatesReferenceList( Plugin plugin )
    {
        ReferenceList attributeCertificateList = new ReferenceList();
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            attributeCertificateList.addItem( daoUtil.getInt( 1 ) , daoUtil.getString( 2 ) );
        }

        daoUtil.free( );
        return attributeCertificateList;
    }
}