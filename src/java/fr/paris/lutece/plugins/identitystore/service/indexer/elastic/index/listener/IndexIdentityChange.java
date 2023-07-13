package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.listener;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;

public class IndexIdentityChange extends IdentityChange {
    protected Identity identity;

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }
}
