/*
 * Copyright (c) 2002-2024, City of Paris
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
package fr.paris.lutece.plugins.identitystore.business.rules.search;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class IdentitySearchRuleDAO implements IIdentitySearchRuleDAO
{

    private static final String SQL_QUERY_SELECT_RULE = "SELECT id_rule, type FROM identitystore_identity_search_rule WHERE id_rule = ? ";
    private static final String SQL_QUERY_SELECTALL_RULE = "SELECT id_rule, type FROM identitystore_identity_search_rule";

    private static final String SQL_QUERY_INSERT_RULE = "INSERT INTO identitystore_identity_search_rule ( type ) VALUES ( ? ) ";
    private static final String SQL_QUERY_INSERT_RULE_ATTRIBUTE = "INSERT INTO identitystore_identity_search_rule_attribute ( id_rule, id_attribute ) VALUES ( ?, ? ) ";

    private static final String SQL_QUERY_UPDATE_RULE = "UPDATE identitystore_identity_search_rule SET type = ? WHERE id_rule = ? ";

    private static final String SQL_QUERY_DELETE_RULE = "DELETE FROM identitystore_identity_search_rule WHERE id_rule = ? ";
    private static final String SQL_QUERY_DELETE_RULE_ATTRIBUTES = "DELETE FROM identitystore_identity_search_rule_attribute WHERE id_rule = ? ";

    private static final String SQL_QUERY_SELECTALL_ATTRIBUTES_BY_RULE_ID = "SELECT ia.id_attribute, ia.name, ia.description, ia.key_name, ia.key_type, ia.key_weight, ia.certifiable, ia.pivot, ia.common_search_key, ia.mandatory_for_creation FROM identitystore_identity_search_rule_attribute r JOIN identitystore_ref_attribute ia on r.id_attribute = ia.id_attribute  WHERE r.id_rule = ? ";

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentitySearchRule select( final int nRuleId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_RULE, plugin ) )
        {
            daoUtil.setInt( 1, nRuleId );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return this.getRule( daoUtil, 0, plugin );
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<IdentitySearchRule> selectAll( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_RULE, plugin ) )
        {
            final List<IdentitySearchRule> rules = new ArrayList<>( );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                rules.add( this.getRule( daoUtil, 0, plugin ) );
            }
            return rules;
        }
    }

    private IdentitySearchRule getRule( final DAOUtil daoUtil, final int offset, final Plugin plugin )
    {
        int nIndex = offset;
        final IdentitySearchRule rule = new IdentitySearchRule( );
        rule.setId( daoUtil.getInt( ++nIndex ) );
        rule.setType( SearchRuleType.valueOf( daoUtil.getString( ++nIndex ) ) );
        rule.getAttributes( ).addAll( this.getAttributes( rule.getId( ), plugin ) );
        return rule;
    }

    private List<AttributeKey> getAttributes( final int ruleId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ATTRIBUTES_BY_RULE_ID, plugin ) )
        {
            final List<AttributeKey> checkedAttributes = new ArrayList<>( );
            daoUtil.setInt( 1, ruleId );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                checkedAttributes.add( this.getAttributeKey( daoUtil, 0 ) );
            }
            if ( !checkedAttributes.isEmpty( ) )
            {
                checkedAttributes.sort( Comparator.comparing( AttributeKey::getName ) );
            }
            return checkedAttributes;
        }
    }

    private AttributeKey getAttributeKey( final DAOUtil daoUtil, final int offset )
    {
        final AttributeKey attributeKey = new AttributeKey( );
        int nIndex = offset;
        attributeKey.setId( daoUtil.getInt( ++nIndex ) );
        attributeKey.setName( daoUtil.getString( ++nIndex ) );
        attributeKey.setDescription( daoUtil.getString( ++nIndex ) );
        attributeKey.setKeyName( daoUtil.getString( ++nIndex ) );
        attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( ++nIndex ) ) );
        attributeKey.setKeyWeight( daoUtil.getInt( ++nIndex ) );
        attributeKey.setCertifiable( daoUtil.getBoolean( ++nIndex ) );
        attributeKey.setPivot( daoUtil.getBoolean( ++nIndex ) );
        attributeKey.setCommonSearchKeyName( daoUtil.getString( ++nIndex ) );
        attributeKey.setMandatoryForCreation( daoUtil.getBoolean( ++nIndex ) );
        return attributeKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( final IdentitySearchRule identitySearchRule, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_RULE, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setString( ++nIndex, identitySearchRule.getType( ).name( ) );
            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                identitySearchRule.setId( daoUtil.getGeneratedKeyInt( 1 ) );
                for ( final AttributeKey attribute : identitySearchRule.getAttributes( ) )
                {
                    this.insertRuleAttribute( identitySearchRule.getId( ), attribute.getId( ), plugin );
                }
            }
        }
    }

    private void insertRuleAttribute( final int ruleId, final int attributeId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_RULE_ATTRIBUTE, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setInt( ++nIndex, ruleId );
            daoUtil.setInt( ++nIndex, attributeId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void update( final IdentitySearchRule identitySearchRule, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_RULE, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setString( ++nIndex, identitySearchRule.getType( ).name( ) );
            daoUtil.setInt( ++nIndex, identitySearchRule.getId( ) );
            daoUtil.executeUpdate( );
            this.deleteRuleAttributes( identitySearchRule.getId( ), plugin );
            for ( final AttributeKey attribute : identitySearchRule.getAttributes( ) )
            {
                this.insertRuleAttribute( identitySearchRule.getId( ), attribute.getId( ), plugin );
            }
        }
    }

    private void deleteRuleAttributes( final int ruleId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_RULE_ATTRIBUTES, plugin ) )
        {
            daoUtil.setInt( 1, ruleId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( final int nRuleId, final Plugin plugin )
    {
        this.deleteRuleAttributes( nRuleId, plugin );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_RULE, plugin ) )
        {
            daoUtil.setInt( 1, nRuleId );
            daoUtil.executeUpdate( );
        }
    }
}
