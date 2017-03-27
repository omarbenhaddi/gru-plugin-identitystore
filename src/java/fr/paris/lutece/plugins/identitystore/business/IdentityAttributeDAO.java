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
import fr.paris.lutece.plugins.identitystore.service.AttributeChangeType;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides Data Access methods for IdentityAttribute objects
 */
public final class IdentityAttributeDAO implements IIdentityAttributeDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_identity, id_attribute, attribute_value, id_certification, id_file, lastupdate_date FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity_attribute ( id_identity, id_attribute, attribute_value, id_certification, id_file ) VALUES ( ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ?";
    private static final String SQL_QUERY_DELETE_ALL_ATTR = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ?";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity_attribute SET id_identity = ?, id_attribute = ?, attribute_value = ?, id_certification = ?, id_file = ?, lastupdate_date = CURRENT_TIMESTAMP WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_SELECTALL = "SELECT a.id_attribute, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date "
            + " FROM identitystore_identity_attribute a" + " WHERE a.id_identity = ? ORDER BY a.id_attribute";
    private static final String SQL_QUERY_SELECT_BY_CLIENT_APP_CODE = "SELECT a.id_attribute, b.name, b.key_name, b.description, b.key_type, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date "
            + " FROM identitystore_identity_attribute a , identitystore_attribute b, identitystore_attribute_right c, identitystore_client_application d "
            + " WHERE a.id_identity = ? AND a.id_attribute = b.id_attribute AND c.id_attribute = a.id_attribute AND d.code = ? AND c.id_client_app = d.id_client_app and c.readable = 1";
    private static final String SQL_QUERY_SELECT_BY_KEY_AND_CLIENT_APP_CODE = "SELECT a.id_attribute, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date "
            + " FROM identitystore_identity_attribute a , identitystore_attribute b, identitystore_attribute_right c, identitystore_client_application d "
            + " WHERE a.id_identity = ? AND a.id_attribute = b.id_attribute AND c.id_attribute = a.id_attribute AND d.code = ? AND c.id_client_app = d.id_client_app and c.readable = 1 and b.key_name = ?";

    // Historical
    private static final String SQL_QUERY_NEW_HISTORY_PK = "SELECT max( id_history ) FROM identitystore_history_identity_attribute";
    private static final String SQL_QUERY_INSERT_HISTORY = "INSERT INTO identitystore_history_identity_attribute "
            + "( id_history, id_identity,change_type, identity_connection_id, attribute_key, attribute_new_value, attribute_old_value, author_id, author_email, author_type, author_service, certifier_name) "
            + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_SELECT_HISTORY = "SELECT modification_date, change_type, identity_connection_id, attribute_key, attribute_new_value, attribute_old_value, "
            + "author_id, author_email, author_type, author_service, certifier_name "
            + "FROM identitystore_history_identity_attribute "
            + " WHERE  attribute_key = ? AND id_identity = ? ORDER BY modification_date DESC";
    private static final String SQL_QUERY_DELETE_ALL_HISTORY = "DELETE FROM identitystore_history_identity_attribute WHERE id_identity = ?";

    /**
     * Generates a new primary key for identitystore_history_identity_attribute table
     *
     * @param plugin
     *            The Plugin
     * @return The new primary key
     */
    private synchronized int newHistoricPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_HISTORY_PK, plugin );
        daoUtil.executeQuery( );

        int nKey = 1;

        if ( daoUtil.next( ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free( );

        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( IdentityAttribute identityAttribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
        daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
        daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityAttribute load( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setInt( 2, nAttributeId );
        daoUtil.executeQuery( );

        IdentityAttribute identityAttribute = null;
        int nAttributeKey = -1;

        if ( daoUtil.next( ) )
        {
            identityAttribute = new IdentityAttribute( );

            int nIndex = 1;

            identityAttribute.setIdIdentity( daoUtil.getInt( nIndex++ ) );
            nAttributeKey = daoUtil.getInt( nIndex++ );
            identityAttribute.setValue( daoUtil.getString( nIndex++ ) );
            identityAttribute.setIdCertificate( daoUtil.getInt( nIndex++ ) );

            int nIdFile = daoUtil.getInt( nIndex++ );

            if ( nIdFile > 0 )
            {
                identityAttribute.setFile( FileHome.findByPrimaryKey( nIdFile ) );
            }
        }
        daoUtil.free( );

        if ( identityAttribute != null )
        {
            identityAttribute.setAttributeKey( AttributeKeyHome.findByPrimaryKey( nAttributeKey ) );
        }

        return identityAttribute;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        // FIXME Delete also the attribute history

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setInt( 2, nAttributeId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( IdentityAttribute identityAttribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
        daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
        daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );
        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
        daoUtil.setInt( nIndex, identityAttribute.getAttributeKey( ).getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, IdentityAttribute> selectAttributes( int nIdentityId, Plugin plugin )
    {
        Map<String, IdentityAttribute> attributesMap = new LinkedHashMap<String, IdentityAttribute>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            IdentityAttribute identityAttribute = new IdentityAttribute( );
            int nIndex = 1;

            AttributeKey attribute = AttributeKeyHome.findByPrimaryKey( daoUtil.getInt( nIndex++ ) );

            identityAttribute.setAttributeKey( attribute );
            identityAttribute.setIdIdentity( nIdentityId );
            identityAttribute.setValue( daoUtil.getString( nIndex++ ) );

            int nCertificateId = daoUtil.getInt( nIndex++ );
            AttributeCertificate certificate = null;
            if ( nCertificateId != 0 )
            {
                certificate = new AttributeCertificate( );
                certificate.setId( nCertificateId );
            }
            identityAttribute.setCertificate( certificate );

            int nIdFile = daoUtil.getInt( nIndex++ );
            File attrFile = null;
            if ( nIdFile > 0 )
            {
                attrFile = new File( );
                attrFile.setIdFile( nIdFile );
            }
            identityAttribute.setFile( attrFile );

            identityAttribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            attributesMap.put( attribute.getKeyName( ), identityAttribute );
        }
        daoUtil.free( );

        for ( String strAttrKey : attributesMap.keySet( ) )
        {
            if ( attributesMap.get( strAttrKey ).getCertificate( ) != null )
            {
                attributesMap.get( strAttrKey ).setCertificate(
                        AttributeCertificateHome.findByPrimaryKey( attributesMap.get( strAttrKey ).getCertificate( ).getId( ) ) );
            }
            if ( attributesMap.get( strAttrKey ).getFile( ) != null )
            {
                attributesMap.get( strAttrKey ).setFile( FileHome.findByPrimaryKey( attributesMap.get( strAttrKey ).getFile( ).getIdFile( ) ) );
            }
        }

        return attributesMap;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, IdentityAttribute> selectAttributes( int nIdentityId, String strApplicationCode, Plugin plugin )
    {
        Map<String, IdentityAttribute> attributesMap = new HashMap<String, IdentityAttribute>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CLIENT_APP_CODE, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setString( 2, strApplicationCode );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            IdentityAttribute attribute = new IdentityAttribute( );
            AttributeKey attributeKey = new AttributeKey( );

            int nIndex = 1;

            attributeKey.setId( daoUtil.getInt( nIndex++ ) );
            attributeKey.setName( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

            attribute.setAttributeKey( attributeKey );
            attribute.setIdIdentity( nIdentityId );
            attribute.setValue( daoUtil.getString( nIndex++ ) );

            int nCertificateId = daoUtil.getInt( nIndex++ );
            AttributeCertificate certificate = null;
            if ( nCertificateId != 0 )
            {
                certificate = new AttributeCertificate( );
                certificate.setId( nCertificateId );
            }
            attribute.setCertificate( certificate );

            int nIdFile = daoUtil.getInt( nIndex++ );
            File attrFile = null;
            if ( nIdFile > 0 )
            {
                attrFile = new File( );
                attrFile.setIdFile( nIdFile );
            }
            attribute.setFile( attrFile );

            attribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            attributesMap.put( attributeKey.getKeyName( ), attribute );
        }
        daoUtil.free( );

        for ( String strAttrKey : attributesMap.keySet( ) )
        {
            if ( attributesMap.get( strAttrKey ).getCertificate( ) != null )
            {
                attributesMap.get( strAttrKey ).setCertificate(
                        AttributeCertificateHome.findByPrimaryKey( attributesMap.get( strAttrKey ).getCertificate( ).getId( ) ) );
            }
            if ( attributesMap.get( strAttrKey ).getFile( ) != null )
            {
                attributesMap.get( strAttrKey ).setFile( FileHome.findByPrimaryKey( attributesMap.get( strAttrKey ).getFile( ).getIdFile( ) ) );
            }
        }

        return attributesMap;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectIdentityAttributesReferenceList( Plugin plugin )
    {
        ReferenceList identityAttributeList = new ReferenceList( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            identityAttributeList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free( );

        return identityAttributeList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityAttribute selectAttribute( int nIdentityId, String strAttributeKey, String strApplicationCode, Plugin plugin )
    {
        IdentityAttribute attribute = null;
        int nAttrKey = -1, nCertificateId = -1, nIdFile = -1;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_KEY_AND_CLIENT_APP_CODE, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setString( 2, strApplicationCode );
        daoUtil.setString( 3, strAttributeKey );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            attribute = new IdentityAttribute( );

            int nIndex = 1;
            nAttrKey = daoUtil.getInt( nIndex++ );
            attribute.setValue( daoUtil.getString( nIndex++ ) );
            nCertificateId = daoUtil.getInt( nIndex++ );
            nIdFile = daoUtil.getInt( nIndex++ );
            attribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            attribute.setIdIdentity( nIdentityId );
        }
        daoUtil.free( );

        if ( attribute != null )
        {
            attribute.setAttributeKey( AttributeKeyHome.findByPrimaryKey( nAttrKey ) );
            if ( nCertificateId > 0 )
            {
                attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( nCertificateId ) );
            }
            if ( nIdFile > 0 )
            {
                attribute.setFile( FileHome.findByPrimaryKey( nIdFile ) );
            }
        }

        return attribute;
    }

    @Override
    public void deleteAllAttributes( int nIdentityId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_HISTORY, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.executeUpdate( );
        daoUtil.free( );

        daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_ATTR, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    @Override
    public synchronized void addAttributeChangeHistory( AttributeChange attributeChange, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_HISTORY, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, newHistoricPrimaryKey( plugin ) );
        daoUtil.setInt( nIndex++, attributeChange.getIdentityId( ) );
        daoUtil.setInt( nIndex++, attributeChange.getChangeType( ).getValue( ) );
        daoUtil.setString( nIndex++, attributeChange.getIdentityConnectionId( ) );
        daoUtil.setString( nIndex++, attributeChange.getChangedKey( ) );
        daoUtil.setString( nIndex++, attributeChange.getNewValue( ) );
        daoUtil.setString( nIndex++, attributeChange.getOldValue( ) );
        daoUtil.setString( nIndex++, attributeChange.getAuthorId( ) );
        daoUtil.setString( nIndex++, attributeChange.getAuthorName( ) );
        daoUtil.setInt( nIndex++, attributeChange.getAuthorType( ) );
        daoUtil.setString( nIndex++, attributeChange.getAuthorService( ) );
        daoUtil.setString( nIndex++, attributeChange.getCertifier( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    @Override
    public List<AttributeChange> getAttributeChangeHistory( int nIdentityId, String strAttributeKey, Plugin plugin )
    {
        List<AttributeChange> listAttributeChange = new ArrayList<AttributeChange>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_HISTORY, plugin );
        daoUtil.setString( 1, strAttributeKey );
        daoUtil.setInt( 2, nIdentityId );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AttributeChange attributeChange = new AttributeChange( );
            int nIndex = 1;
            attributeChange.setDateChange( daoUtil.getTimestamp( nIndex++ ) );
            attributeChange.setChangeType( AttributeChangeType.valueOf( daoUtil.getInt( nIndex++ ) ) );
            attributeChange.setIdentityConnectionId( daoUtil.getString( nIndex++ ) );
            attributeChange.setChangedKey( daoUtil.getString( nIndex++ ) );
            attributeChange.setNewValue( daoUtil.getString( nIndex++ ) );
            attributeChange.setOldValue( daoUtil.getString( nIndex++ ) );
            attributeChange.setAuthorId( daoUtil.getString( nIndex++ ) );
            attributeChange.setAuthorName( daoUtil.getString( nIndex++ ) );
            attributeChange.setAuthorType( daoUtil.getInt( nIndex++ ) );
            attributeChange.setAuthorService( daoUtil.getString( nIndex++ ) );
            attributeChange.setCertifier( daoUtil.getString( nIndex++ ) );
            attributeChange.setIdentityId( nIdentityId );
            listAttributeChange.add( attributeChange );
        }

        daoUtil.free( );

        return listAttributeChange;
    }
}
