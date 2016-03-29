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
 * This class provides Data Access methods for AttributeCertifier objects
 */
public final class AttributeCertifierDAO implements IAttributeCertifierDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_attribute_certifier ) FROM identitystore_attribute_certifier";
    private static final String SQL_QUERY_SELECT = "SELECT id_attribute_certifier, name, description, logo_file, logo_mime_type FROM identitystore_attribute_certifier WHERE id_attribute_certifier = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_attribute_certifier ( id_attribute_certifier, name, description, logo_file, logo_mime_type ) VALUES ( ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_attribute_certifier WHERE id_attribute_certifier = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_attribute_certifier SET id_attribute_certifier = ?, name = ?, description = ?, logo_file = ?, logo_mime_type = ? WHERE id_attribute_certifier = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_attribute_certifier, name, description FROM identitystore_attribute_certifier";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_attribute_certifier FROM identitystore_attribute_certifier";

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
    public void insert( AttributeCertifier certifier, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        certifier.setId( newPrimaryKey( plugin ) );
        int nIndex = 1;
        
        daoUtil.setInt( nIndex++ , certifier.getId( ) );
        daoUtil.setString( nIndex++ , certifier.getName( ) );
        daoUtil.setString( nIndex++ , certifier.getDescription( ) );
        daoUtil.setBytes(nIndex++ , certifier.getLogo( ) );
        daoUtil.setString( nIndex++ , certifier.getLogoMimeType());

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeCertifier load( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1 , nKey );
        daoUtil.executeQuery( );
        AttributeCertifier certifier = null;

        if ( daoUtil.next( ) )
        {
            certifier = new AttributeCertifier();
            int nIndex = 1;
            
            certifier.setId( daoUtil.getInt( nIndex++ ) );
            certifier.setName( daoUtil.getString( nIndex++ ) );
            certifier.setDescription( daoUtil.getString( nIndex++ ) );
            certifier.setLogo( daoUtil.getBytes( nIndex++ ) );
            certifier.setLogoMimeType( daoUtil.getString( nIndex++ ));
        }

        daoUtil.free( );
        return certifier;
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
    public void store( AttributeCertifier certifier, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;
        
        daoUtil.setInt( nIndex++ , certifier.getId( ) );
        daoUtil.setString( nIndex++ , certifier.getName( ) );
        daoUtil.setString( nIndex++ , certifier.getDescription( ) );
        daoUtil.setBytes( nIndex++ , certifier.getLogo( ) );
        daoUtil.setString( nIndex++ , certifier.getLogoMimeType());
        daoUtil.setInt( nIndex , certifier.getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeCertifier> selectAttributeCertifiersList( Plugin plugin )
    {
        List<AttributeCertifier> certifierList = new ArrayList<>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            AttributeCertifier certifier = new AttributeCertifier(  );
            int nIndex = 1;
            
            certifier.setId( daoUtil.getInt( nIndex++ ) );
            certifier.setName( daoUtil.getString( nIndex++ ) );
            certifier.setDescription( daoUtil.getString( nIndex++ ) );

            certifierList.add( certifier );
        }

        daoUtil.free( );
        return certifierList;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdAttributeCertifiersList( Plugin plugin )
    {
        List<Integer> certifierList = new ArrayList<>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            certifierList.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );
        return certifierList;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectAttributeCertifiersReferenceList( Plugin plugin )
    {
        ReferenceList certifierList = new ReferenceList();
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            certifierList.addItem( daoUtil.getInt( 1 ) , daoUtil.getString( 2 ) );
        }

        daoUtil.free( );
        return certifierList;
    }
}