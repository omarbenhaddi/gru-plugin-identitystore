package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.alias;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude( JsonInclude.Include.NON_NULL )
public class AliasAction {
    @JsonIgnore
    protected String name;
    protected String index;
    protected String alias;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
