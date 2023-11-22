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
package fr.paris.lutece.plugins.identitystore.business.rules.search;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

public final class IdentitySearchRuleHome
{

    private static final IdentitySearchRuleDAO _dao = SpringContextService.getBean( IdentitySearchRuleDAO.BEAN_NAME );
    private static final Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private IdentitySearchRuleHome( )
    {
    }

    /**
     * Create an instance of {@link IdentitySearchRule}
     *
     * @param duplicateRule
     *            The instance of the {@link IdentitySearchRule} which contains the information to store
     * @return The instance of {@link IdentitySearchRule} which has been created with its primary key.
     */
    public static IdentitySearchRule create( IdentitySearchRule duplicateRule )
    {
        _dao.insert( duplicateRule, _plugin );
        return duplicateRule;
    }

    /**
     * Update an instance of {@link IdentitySearchRule}
     *
     * @param duplicateRule
     *            The instance of the {@link IdentitySearchRule} which contains the information to store
     * @return The instance of {@link IdentitySearchRule} which has been updated with its primary key.
     */
    public static IdentitySearchRule update( IdentitySearchRule duplicateRule )
    {
        _dao.update( duplicateRule, _plugin );
        return duplicateRule;
    }

    /**
     * Delete an instance of {@link IdentitySearchRule}
     *
     * @param duplicateRule
     *            The instance of the {@link IdentitySearchRule} to delete
     */
    public static void delete( IdentitySearchRule duplicateRule )
    {
        _dao.delete( duplicateRule.getId( ), _plugin );
    }

    /**
     * Delete an instance of {@link IdentitySearchRule}
     *
     * @param id
     *            The id of the {@link IdentitySearchRule} to delete
     */
    public static void delete( int id )
    {
        _dao.delete( id, _plugin );
    }

    /**
     * Get all existing instances of {@link IdentitySearchRule}
     *
     * @return The existing instances of {@link IdentitySearchRule}.
     */
    public static List<IdentitySearchRule> findAll( )
    {
        return _dao.selectAll( _plugin );
    }

    /**
     * Get existing instances of {@link IdentitySearchRule} by its ID
     *
     * @return The existing instances of {@link IdentitySearchRule} identified by param ruleId.
     */
    public static IdentitySearchRule find( int ruleId )
    {
        return _dao.select( ruleId, _plugin );
    }

}
