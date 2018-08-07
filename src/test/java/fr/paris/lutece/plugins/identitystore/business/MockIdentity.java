package fr.paris.lutece.plugins.identitystore.business;

import java.util.HashMap;
import java.util.Map;

import fr.paris.lutece.plugins.identitystore.util.IdGenerator;

public class MockIdentity
{
    public static Identity create( )
    {
        Identity identity = new Identity( );
        Map<String, IdentityAttribute> mapAttributes = new HashMap<>( );

        identity.setCustomerId( Integer.toString( IdGenerator.generateId( ) ) );
        identity.setConnectionId( Integer.toString( IdGenerator.generateId( ) ) );
        identity.setAttributes( mapAttributes );

        return identity;
    }
}
