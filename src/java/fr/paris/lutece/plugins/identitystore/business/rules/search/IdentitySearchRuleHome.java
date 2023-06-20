package fr.paris.lutece.plugins.identitystore.business.rules.search;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

public final class IdentitySearchRuleHome {

    private static IdentitySearchRuleDAO _dao = SpringContextService.getBean(IdentitySearchRuleDAO.BEAN_NAME);
    private static Plugin _plugin = PluginService.getPlugin(IdentityStorePlugin.PLUGIN_NAME);

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
    public static List<IdentitySearchRule> findAll()
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
