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
 * This class provides Data Access methods for IdentityAttribute objects
 */
public final class IdentityAttributeDAO implements IIdentityAttributeDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_identity, id_attribute, attribute_value, id_certification FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity_attribute ( id_identity, id_attribute, attribute_value, id_certification ) VALUES ( ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ?";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity_attribute SET id_identity = ?, id_attribute = ?, attribute_value = ?, id_certification = ? WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_SELECTALL = "SELECT b.key_name, b.name, a.attribute_value, a.id_certification " +
        " FROM identitystore_identity_attribute a , identitystore_attribute b" +
        " WHERE a.id_identity = ? AND a.id_attribute = b.id_attribute";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( IdentityAttribute identityAttribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity(  ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdAttribute(  ) );
        daoUtil.setString( nIndex++, identityAttribute.getAttributeValue(  ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
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
        daoUtil.executeQuery(  );

        IdentityAttribute identityAttribute = null;

        if ( daoUtil.next(  ) )
        {
            identityAttribute = new IdentityAttribute(  );

            int nIndex = 1;

            identityAttribute.setIdIdentity( daoUtil.getInt( nIndex++ ) );
            identityAttribute.setIdAttribute( daoUtil.getInt( nIndex++ ) );
            identityAttribute.setAttributeValue( daoUtil.getString( nIndex++ ) );
            identityAttribute.setIdCertificate( daoUtil.getInt( nIndex++ ) );
        }

        daoUtil.free(  );

        return identityAttribute;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setInt( 2, nAttributeId );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( IdentityAttribute identityAttribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity(  ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdAttribute(  ) );
        daoUtil.setString( nIndex++, identityAttribute.getAttributeValue(  ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate(  ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity(  ) );
        daoUtil.setInt( nIndex, identityAttribute.getIdAttribute(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Attribute> selectAttributesList( int nIdentityId, Plugin plugin )
    {
        List<Attribute> attributesList = new ArrayList<Attribute>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            Attribute attribute = new Attribute(  );
            int nIndex = 1;

            attribute.setKey( daoUtil.getString( nIndex++ ) );
            attribute.setName( daoUtil.getString( nIndex++ ) );
            attribute.setValue( daoUtil.getString( nIndex++ ) );

            int nCertificateId = daoUtil.getInt( nIndex++ );

            if ( nCertificateId != 0 )
            {
                AttributeCertificate certificate = AttributeCertificateHome.findByPrimaryKey( nCertificateId );
                attribute.setCertificate( certificate );
                attribute.setLevel( certificate.getCertificateLevel(  ) );
            }

            attributesList.add( attribute );
        }

        daoUtil.free(  );

        return attributesList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectIdentityAttributesReferenceList( Plugin plugin )
    {
        ReferenceList identityAttributeList = new ReferenceList(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            identityAttributeList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free(  );

        return identityAttributeList;
    }
}
