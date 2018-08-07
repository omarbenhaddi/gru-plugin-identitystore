package fr.paris.lutece.plugins.identitystore.business;

public class MockIdentityAttribute
{
    public static IdentityAttribute create( Identity identity, AttributeKey attributeKey )
    {
        IdentityAttribute attribute = new IdentityAttribute( );
        attribute.setAttributeKey( attributeKey );
        attribute.setIdIdentity( identity.getId( ) );

        return attribute;
    }
}
