package fr.paris.lutece.plugins.identitystore.business.rules.search;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IdentitySearchRule implements Serializable {

    private int _nId;
    private SearchRuleType _type;
    private List<AttributeKey> _listAttributes = new ArrayList<>();

    public int getId() {
        return _nId;
    }

    public void setId(final int _nId) {
        this._nId = _nId;
    }

    public SearchRuleType getType() {
        return _type;
    }

    public void setType(final SearchRuleType _type) {
        this._type = _type;
    }

    public List<AttributeKey> getAttributes() {
        return _listAttributes;
    }

    public void setAttributes(final List<AttributeKey> _listAttributes) {
        this._listAttributes = _listAttributes;
    }
}
