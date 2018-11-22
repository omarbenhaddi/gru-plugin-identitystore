package fr.paris.lutece.plugins.identitystore.v2.service;

import static fr.paris.lutece.plugins.identitystore.v2.business.IdentityUtil.createIdentityInDatabase;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreDeleteRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityStoreDeleteRequestTest extends LuteceTestCase
{
    private final String _strQueryClientAppCode = "MyApplication";

    private static final String MESSAGE_DELETE_SUCCESSFUL = "Identity successfully deleted.";

    public void testDeletIdentity( )
    {
        Identity identityReference = createIdentityInDatabase( );
        IdentityDto identityDto = MockIdentityDto.create( identityReference );

        IdentityStoreDeleteRequest identityDelete = new IdentityStoreDeleteRequest( identityDto.getConnectionId( ), _strQueryClientAppCode );

        assertEquals( identityDelete.doRequest( ), MESSAGE_DELETE_SUCCESSFUL );
    }
}
