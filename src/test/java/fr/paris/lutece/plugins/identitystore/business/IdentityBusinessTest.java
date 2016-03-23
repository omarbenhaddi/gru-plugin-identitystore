/*
 * Copyright (c) 2002-2016, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */

package fr.paris.lutece.plugins.identitystore.business;

import fr.paris.lutece.test.LuteceTestCase;

import java.sql.Date;

public class IdentityBusinessTest extends LuteceTestCase
{
    private final static String CONNECTIONID1 = "ConnectionId1";
    private final static String CONNECTIONID2 = "ConnectionId2";
    private final static String CUSTOMERID1 = "CustomerId1";
    private final static String CUSTOMERID2 = "CustomerId2";
    private final static String GIVENNAME1 = "GivenName1";
    private final static String GIVENNAME2 = "GivenName2";
    private final static String FAMILYNAME1 = "FamilyName1";
    private final static String FAMILYNAME2 = "FamilyName2";
    private final static int GENDER1 = 1;
    private final static int GENDER2 = 2;
	private final static Date BIRTHDATE1 = new Date( 1000000l );
    private final static Date BIRTHDATE2 = new Date( 2000000l );
    private final static String BIRTHPLACE1 = "Birthplace1";
    private final static String BIRTHPLACE2 = "Birthplace2";
    private final static String EMAIL1 = "Email1";
    private final static String EMAIL2 = "Email2";
    private final static String PREFERREDUSERNAME1 = "PreferredUsername1";
    private final static String PREFERREDUSERNAME2 = "PreferredUsername2";
    private final static String ADDRESS1 = "Address1";
    private final static String ADDRESS2 = "Address2";
    private final static String PHONE1 = "Phone1";
    private final static String PHONE2 = "Phone2";

    public void testBusiness(  )
    {
        // Initialize an object
        Identity identity = new Identity();
        identity.setConnectionId( CONNECTIONID1 );
        identity.setCustomerId( CUSTOMERID1 );
        identity.setGivenName( GIVENNAME1 );
        identity.setFamilyName( FAMILYNAME1 );
        identity.setGender( GENDER1 );
        identity.setBirthdate( BIRTHDATE1 );
        identity.setBirthplace( BIRTHPLACE1 );
        identity.setEmail( EMAIL1 );
        identity.setPreferredUsername( PREFERREDUSERNAME1 );
        identity.setAddress( ADDRESS1 );
        identity.setPhone( PHONE1 );

        // Create test
        IdentityHome.create( identity );
        Identity identityStored = IdentityHome.findByPrimaryKey( identity.getId( ) );
        assertEquals( identityStored.getConnectionId() , identity.getConnectionId( ) );
        assertEquals( identityStored.getCustomerId() , identity.getCustomerId( ) );
        assertEquals( identityStored.getGivenName() , identity.getGivenName( ) );
        assertEquals( identityStored.getFamilyName() , identity.getFamilyName( ) );
        assertEquals( identityStored.getGender() , identity.getGender( ) );
        assertEquals( identityStored.getBirthdate() , identity.getBirthdate( ) );
        assertEquals( identityStored.getBirthplace() , identity.getBirthplace( ) );
        assertEquals( identityStored.getEmail() , identity.getEmail( ) );
        assertEquals( identityStored.getPreferredUsername() , identity.getPreferredUsername( ) );
        assertEquals( identityStored.getAddress() , identity.getAddress( ) );
        assertEquals( identityStored.getPhone() , identity.getPhone( ) );

        // Update test
        identity.setConnectionId( CONNECTIONID2 );
        identity.setCustomerId( CUSTOMERID2 );
        identity.setGivenName( GIVENNAME2 );
        identity.setFamilyName( FAMILYNAME2 );
        identity.setGender( GENDER2 );
        identity.setBirthdate( BIRTHDATE2 );
        identity.setBirthplace( BIRTHPLACE2 );
        identity.setEmail( EMAIL2 );
        identity.setPreferredUsername( PREFERREDUSERNAME2 );
        identity.setAddress( ADDRESS2 );
        identity.setPhone( PHONE2 );
        IdentityHome.update( identity );
        identityStored = IdentityHome.findByPrimaryKey( identity.getId( ) );
        assertEquals( identityStored.getConnectionId() , identity.getConnectionId( ) );
        assertEquals( identityStored.getCustomerId() , identity.getCustomerId( ) );
        assertEquals( identityStored.getGivenName() , identity.getGivenName( ) );
        assertEquals( identityStored.getFamilyName() , identity.getFamilyName( ) );
        assertEquals( identityStored.getGender() , identity.getGender( ) );
        assertEquals( identityStored.getBirthdate() , identity.getBirthdate( ) );
        assertEquals( identityStored.getBirthplace() , identity.getBirthplace( ) );
        assertEquals( identityStored.getEmail() , identity.getEmail( ) );
        assertEquals( identityStored.getPreferredUsername() , identity.getPreferredUsername( ) );
        assertEquals( identityStored.getAddress() , identity.getAddress( ) );
        assertEquals( identityStored.getPhone() , identity.getPhone( ) );

        // List test
        IdentityHome.getIdentitysList();

        // Delete test
        IdentityHome.remove( identity.getId( ) );
        identityStored = IdentityHome.findByPrimaryKey( identity.getId( ) );
        assertNull( identityStored );
        
    }

}