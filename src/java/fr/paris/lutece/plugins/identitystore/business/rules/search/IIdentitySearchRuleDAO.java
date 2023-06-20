package fr.paris.lutece.plugins.identitystore.business.rules.search;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;

public interface IIdentitySearchRuleDAO {

    String BEAN_NAME = "identitystore.identitySearchRuleDao";

    /**
     * Insert a new record in the table.
     *
     * @param identitySearchRule
     *            instance of the IdentitySearchRule object to insert
     * @param plugin
     *            the Plugin
     */
    void insert(IdentitySearchRule identitySearchRule, Plugin plugin);

    /**
     * Update the record in the table
     *
     * @param identitySearchRule
     *            the reference of the IdentitySearchRule
     * @param plugin
     *            the Plugin
     */
    void update( IdentitySearchRule identitySearchRule, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nRuleId
     *            The rule ID
     * @param plugin
     *            the Plugin
     */
    void delete( int nRuleId, Plugin plugin );

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the data from the table
     *
     * @param nRuleId
     *            The rule ID
     * @param plugin
     *            the Plugin
     * @return The instance of the IdentitySearchRule
     */
    IdentitySearchRule select( int nRuleId, Plugin plugin );

    /**
     * Load the data from the table
     *
     * @param plugin
     *            the Plugin
     * @return The instances of the IdentitySearchRule
     */
    List<IdentitySearchRule> selectAll(Plugin plugin);
}
