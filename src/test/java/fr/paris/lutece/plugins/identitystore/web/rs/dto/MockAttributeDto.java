package fr.paris.lutece.plugins.identitystore.web.rs.dto;

import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;

public class MockAttributeDto
{
    public static AttributeDto create( IdentityDto identity, String strKey, String strValue )
    {
        AttributeDto attribute = create( strKey, strValue );
        identity.getAttributes( ).put( attribute.getKey( ), attribute );

        return attribute;
    }

    public static AttributeDto create( String strKey, String strValue )
    {
        AttributeDto attribute = new AttributeDto( );
        attribute.setKey( strKey );
        attribute.setValue( strValue );

        return attribute;
    }

    public static AttributeDto create( IdentityDto identity, IdentityAttribute identityAttribute )
    {
        AttributeDto attribute = create( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute.getValue( ) );
        identity.getAttributes( ).put( attribute.getKey( ), attribute );

        return attribute;
    }
}
