package fr.paris.lutece.plugins.identitystore.v1.service;

import static fr.paris.lutece.plugins.identitystore.v1.business.IdentityUtil.createIdentityInDatabase;

import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.v1.web.request.IdentityStoreDeleteRequest;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityStoreDeleteRequestTest extends LuteceTestCase
{
    private static final String _strQueryClientAppCode = "MyApplication";

    private static final String MESSAGE_DELETE_SUCCESSFUL = "Identity successfully deleted.";

    public void testDeletIdentity( )
    {
        Identity identityReference = createIdentityInDatabase( );
        IdentityDto identityDto = MockIdentityDto.createIdentityV1( identityReference );

        IdentityStoreDeleteRequest identityDelete = new IdentityStoreDeleteRequest( identityDto.getConnectionId( ), _strQueryClientAppCode );

        assertEquals( identityDelete.doRequest( ), MESSAGE_DELETE_SUCCESSFUL );
    }
}
