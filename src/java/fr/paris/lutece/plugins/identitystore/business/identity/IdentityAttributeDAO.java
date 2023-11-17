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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
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
    private static final String SQL_QUERY_SELECT = "SELECT id_identity, id_attribute, attribute_value, id_certification, id_file, lastupdate_date, lastupdate_client FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity_attribute ( id_identity, id_attribute, attribute_value, id_certification, id_file, lastupdate_client ) VALUES ( ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ?";
    private static final String SQL_QUERY_DELETE_ALL_ATTR = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ?";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity_attribute SET id_identity = ?, id_attribute = ?, attribute_value = ?, id_certification = ?, id_file = ?, lastupdate_date = CURRENT_TIMESTAMP, lastupdate_client = ? WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_SELECTALL = "SELECT a.id_attribute, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_client "
            + " FROM identitystore_identity_attribute a" + " WHERE a.id_identity = ? ORDER BY a.id_attribute";
    private static final String SQL_QUERY_SELECT_BY_CLIENT_APP_CODE = "SELECT a.id_attribute, b.name, b.key_name, b.description, b.key_type, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_client "
            + " FROM identitystore_identity_attribute a , identitystore_ref_attribute b, identitystore_service_contract_attribute_right c, identitystore_client_application d "
            + " WHERE a.id_identity = ? AND a.id_attribute = b.id_attribute AND c.id_attribute = a.id_attribute AND d.code = ? AND c.id_client_app = d.id_client_app and c.readable = 1";

    private static final String SQL_QUERY_SELECT_BY_LIST_IDENTITY = "SELECT a.id_attribute, b.name, b.key_name, b.description, b.key_type, a.id_identity, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_client"
            + " FROM identitystore_identity_attribute a, identitystore_ref_attribute b"
            + " WHERE a.id_attribute = b.id_attribute AND a.id_identity IN (${list_identity})";

    // Historical
    private static final String SQL_QUERY_INSERT_HISTORY = "INSERT INTO identitystore_identity_attribute_history  "
            + "   (change_type, change_satus, change_message, author_type, author_name, client_code, id_identity, attribute_key, attribute_value, certification_process, certification_date, modification_date, metadata) "
            + "   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_json(?::json))";
    private static final String SQL_QUERY_SELECT_ATTRIBUTE_HISTORY = "SELECT id_history, change_type, change_satus, change_message, author_type, author_name, client_code, id_identity, attribute_key, attribute_value, certification_process, certification_date, modification_date, metadata::text FROM identitystore_identity_attribute_history WHERE id_identity = ? ORDER BY modification_date DESC";
    private static final String SQL_QUERY_SELECT_ATTRIBUTE_HISTORY_BY_CUSTOMER_ID = "SELECT a.id_history, a.change_type, a.change_satus, a.change_message, a.author_type, a.author_name, a.client_code, a.id_identity, a.attribute_key, a.attribute_value, a.certification_process, a.certification_date, a.modification_date, a.metadata::text FROM identitystore_identity_attribute_history a JOIN identitystore_identity i ON a.id_identity = i.id_identity WHERE i.customer_id = ? ORDER BY a.modification_date DESC";
    private static final String SQL_QUERY_DELETE_ALL_HISTORY = "DELETE FROM identitystore_identity_attribute_history  WHERE id_identity = ?";

    private final ObjectMapper objectMapper = new ObjectMapper( );

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( IdentityAttribute identityAttribute, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
            daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
            daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );
            daoUtil.setString( nIndex++, identityAttribute.getLastUpdateClientCode( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityAttribute load( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
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

                identityAttribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
                identityAttribute.setLastUpdateClientCode( daoUtil.getString( nIndex++ ) );

            }

            if ( identityAttribute != null )
            {
                identityAttribute.setAttributeKey( AttributeKeyHome.findByPrimaryKey( nAttributeKey ) );
            }

            return identityAttribute;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        // FIXME Delete also the attribute history

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.setInt( 2, nAttributeId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( IdentityAttribute identityAttribute, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
            daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
            daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );
            daoUtil.setString( nIndex++, identityAttribute.getLastUpdateClientCode( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
            daoUtil.setInt( nIndex, identityAttribute.getAttributeKey( ).getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, IdentityAttribute> selectAttributes( int nIdentityId, Plugin plugin )
    {
        final Map<String, IdentityAttribute> attributesMap = new LinkedHashMap<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
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
                identityAttribute.setLastUpdateClientCode( daoUtil.getString( nIndex ) );

                attributesMap.put( attribute.getKeyName( ), identityAttribute );
            }

            for ( IdentityAttribute attribute : attributesMap.values( ) )
            {
                if ( attribute.getCertificate( ) != null )
                {
                    attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( attribute.getCertificate( ).getId( ) ) );

                    if ( isCertificateExpired( attribute ) )
                    {
                        attribute.setCertificate( null );
                    }
                }
                if ( attribute.getFile( ) != null )
                {
                    attribute.setFile( FileHome.findByPrimaryKey( attribute.getFile( ).getIdFile( ) ) );
                }
            }

            return attributesMap;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<IdentityAttribute> selectAllAttributesByIdentityList( List<Identity> listIdentity, Plugin plugin )
    {
        List<IdentityAttribute> listIdentityAttributes = new ArrayList<>( );

        if ( listIdentity == null || listIdentity.isEmpty( ) )
        {
            return listIdentityAttributes;
        }

        List<String> listIn = new ArrayList<>( );

        for ( int i = 0; i < listIdentity.size( ); i++ )
        {
            listIn.add( "?" );
        }

        String strSQL = SQL_QUERY_SELECT_BY_LIST_IDENTITY.replace( "${list_identity}", String.join( ", ", listIn ) );

        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            int nIndex = 1;

            for ( Identity identity : listIdentity )
            {
                daoUtil.setInt( nIndex++, identity.getId( ) );
            }

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                IdentityAttribute attribute = new IdentityAttribute( );
                AttributeKey attributeKey = new AttributeKey( );

                nIndex = 1;

                attributeKey.setId( daoUtil.getInt( nIndex++ ) );
                attributeKey.setName( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

                attribute.setAttributeKey( attributeKey );
                attribute.setIdIdentity( daoUtil.getInt( nIndex++ ) );
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
                attribute.setLastUpdateClientCode( daoUtil.getString( nIndex++ ) );
                listIdentityAttributes.add( attribute );
            }

            for ( IdentityAttribute attribute : listIdentityAttributes )
            {
                if ( attribute.getCertificate( ) != null )
                {
                    attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( attribute.getCertificate( ).getId( ) ) );

                    if ( isCertificateExpired( attribute ) )
                    {
                        attribute.setCertificate( null );
                    }
                }
                if ( attribute.getFile( ) != null )
                {
                    attribute.setFile( FileHome.findByPrimaryKey( attribute.getFile( ).getIdFile( ) ) );
                }
            }

            return listIdentityAttributes;
        }
    }

    /**
     * Return true if the attribute certificate is expired or false if not
     * 
     * @param attribute
     * @return true if the identity attribute is not null and expired
     */
    private boolean isCertificateExpired( IdentityAttribute attribute )
    {
        return attribute.getCertificate( ) != null && attribute.getCertificate( ).getExpirationDate( ) != null
                && attribute.getCertificate( ).getExpirationDate( ).before( new Date( ) );
    }

    @Override
    public void deleteAllAttributes( int nIdentityId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.executeUpdate( );
        }

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_ATTR, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public synchronized void addAttributeChangeHistory( AttributeChange attributeChange, Plugin plugin ) throws IdentityStoreException
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_HISTORY, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, attributeChange.getChangeType( ).getValue( ) );
            daoUtil.setString( nIndex++, attributeChange.getChangeSatus( ) );
            daoUtil.setString( nIndex++, attributeChange.getChangeMessage( ) );
            daoUtil.setString( nIndex++, attributeChange.getAuthorType( ).name( ) );
            daoUtil.setString( nIndex++, attributeChange.getAuthorName( ) );
            daoUtil.setString( nIndex++, attributeChange.getClientCode( ) );
            daoUtil.setInt( nIndex++, attributeChange.getIdIdentity( ) );
            daoUtil.setString( nIndex++, attributeChange.getAttributeKey( ) );
            daoUtil.setString( nIndex++, attributeChange.getAttributeValue( ) );
            daoUtil.setString( nIndex++, attributeChange.getCertificationProcessus( ) );
            daoUtil.setTimestamp( nIndex++, attributeChange.getCertificationDate( ) );
            daoUtil.setTimestamp( nIndex++, attributeChange.getModificationDate( ) );
            daoUtil.setString( nIndex, objectMapper.writeValueAsString( attributeChange.getMetadata( ) ) );

            daoUtil.executeUpdate( );
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    @Override
    public List<AttributeChange> getAttributeChangeHistory( int nIdentityId, Plugin plugin ) throws IdentityStoreException
    {
        final List<AttributeChange> listAttributeChange = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ATTRIBUTE_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listAttributeChange.add( this.getAttributeChange( daoUtil ) );
            }

            return listAttributeChange;
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    @Override
    public List<AttributeChange> getAttributeChangeHistory( String customerId, Plugin plugin ) throws IdentityStoreException
    {
        final List<AttributeChange> listAttributeChange = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ATTRIBUTE_HISTORY_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listAttributeChange.add( this.getAttributeChange( daoUtil ) );
            }

            return listAttributeChange;
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    public AttributeChange getAttributeChange( final DAOUtil daoUtil ) throws JsonProcessingException
    {
        final AttributeChange attributeChange = new AttributeChange( );
        int nIndex = 1;
        attributeChange.setId( daoUtil.getInt( nIndex++ ) );
        attributeChange.setChangeType( AttributeChangeType.valueOf( daoUtil.getInt( nIndex++ ) ) );
        attributeChange.setChangeSatus( daoUtil.getString( nIndex++ ) );
        attributeChange.setChangeMessage( daoUtil.getString( nIndex++ ) );
        attributeChange.setAuthorType( AuthorType.valueOf( daoUtil.getString( nIndex++ ) ) );
        attributeChange.setAuthorName( daoUtil.getString( nIndex++ ) );
        attributeChange.setClientCode( daoUtil.getString( nIndex++ ) );
        attributeChange.setIdIdentity( daoUtil.getInt( nIndex++ ) );
        attributeChange.setAttributeKey( daoUtil.getString( nIndex++ ) );
        attributeChange.setAttributeValue( daoUtil.getString( nIndex++ ) );
        attributeChange.setCertificationProcessus( daoUtil.getString( nIndex++ ) );
        attributeChange.setCertificationDate( daoUtil.getTimestamp( nIndex++ ) );
        attributeChange.setModificationDate( daoUtil.getTimestamp( nIndex++ ) );
        final String jsonMap = daoUtil.getString( nIndex );
        if ( StringUtils.isNotEmpty( jsonMap ) )
        {
            final Map<String, String> mapMetaData = objectMapper.readValue( jsonMap, new TypeReference<Map<String, String>>( )
            {
            } );
            attributeChange.getMetadata( ).clear( );
            if ( mapMetaData != null && !mapMetaData.isEmpty( ) )
            {
                attributeChange.getMetadata( ).putAll( mapMetaData );
            }
        }
        return attributeChange;
    }
}
