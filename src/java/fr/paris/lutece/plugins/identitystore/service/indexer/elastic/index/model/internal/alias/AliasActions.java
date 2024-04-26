package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.alias;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.CustomSerializer;

import java.util.ArrayList;
import java.util.List;

public class AliasActions {
    @JsonSerialize( using = CustomSerializer.class )
    protected List<AliasAction> actions = new ArrayList<>( );

    public void addAction( AliasAction action ) {
        actions.add( action );
    }

    public List<AliasAction> getActions() {
        return actions;
    }

    public void setActions(List<AliasAction> actions) {
        this.actions = actions;
    }
}
