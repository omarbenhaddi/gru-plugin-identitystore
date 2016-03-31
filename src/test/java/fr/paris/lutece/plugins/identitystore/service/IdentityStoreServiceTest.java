/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.paris.lutece.plugins.identitystore.service;

import fr.paris.lutece.plugins.identitystore.business.Attribute;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.test.LuteceTestCase;

import org.junit.Test;

import java.util.List;


/**
 *
 * @author levy
 */
public class IdentityStoreServiceTest extends LuteceTestCase
{
    /**
     * Test of getAttributesByConnectionId method, of class IdentityStoreService.
     */
    @Test
    public void testGetAttributesByConnectionId(  )
    {
        System.out.println( "getAttributesByConnectionId" );

        Identity identity = getIdentity(  );
        IdentityStoreService.setAttribute( identity.getConnectionId(  ), "email", "john.doe@nowhere.com", null );

        List<Attribute> list = IdentityStoreService.getAttributesByConnectionId( identity.getConnectionId(  ) );
        assertTrue( list.size(  ) == 1 );
    }

    private Identity getIdentity(  )
    {
        Identity identity = new Identity(  );
        identity.setGivenName( "John" );
        identity.setFamilyName( "Doe" );
        identity.setConnectionId( "A45654EF" );
        IdentityHome.create( identity );

        return identity;
    }
}
