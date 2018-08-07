/*
 * Copyright (c) 2002-2017, Mairie de Paris
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
package fr.paris.lutece.plugins.identitystore.service;

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.MockIdentity;
import fr.paris.lutece.plugins.identitystore.business.MockIdentityAttribute;
import fr.paris.lutece.test.LuteceTestCase;

/**
 *
 * @author levy
 */
public class IdentityStoreServiceGetIdentityTest extends LuteceTestCase
{
    private final String TEST_CONNECTIONID1 = "12345";
    private final String TEST_KOID = "KO_ID";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );

        IdentityStoreTestContext.initContext( );
    }

    public void testGetIdentityByConnectionId( )
    {
        Identity identityReference = createIdentityInDatabase( );
        AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
        AttributeKey attributeKey2 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_2 );
        createIdentityAttributeInDatabase( identityReference, attributeKey1 );
        createIdentityAttributeInDatabase( identityReference, attributeKey2 );

        Identity identityDB = IdentityStoreService.getIdentityByConnectionId( identityReference.getConnectionId( ), IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNotNull( identityDB );
        assertEquals( identityReference.getCustomerId( ), identityDB.getCustomerId( ) );
        assertEquals( 2, identityDB.getAttributes( ).size( ) );
    }

    private AttributeKey findAttributeKey( String strAttributeKeyName )
    {
        return AttributeKeyHome.findByKey( strAttributeKeyName );
    }

    private Identity createIdentityInDatabase( )
    {
        Identity identity = MockIdentity.create( );
        identity = IdentityHome.create( identity );

        return identity;
    }

    private IdentityAttribute createIdentityAttributeInDatabase( Identity identity, AttributeKey attributeKey )
    {
        IdentityAttribute identityAttribute = MockIdentityAttribute.create( identity, attributeKey );
        identityAttribute = IdentityAttributeHome.create( identityAttribute );

        identity.getAttributes( ).put( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute );

        return identityAttribute;
    }

    public void testGetIdentityByConnectionIdWithUnknownConnectionId( )
    {
        Identity identityDB = IdentityStoreService.getIdentityByConnectionId( TEST_CONNECTIONID1, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNull( identityDB );
    }

    public void testGetIdentityByCustomerId( )
    {
        Identity identityDB = IdentityStoreService
                .getIdentityByCustomerId( IdentityStoreTestContext.SAMPLE_CUSTOMERID, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNotNull( identityDB );
        assertEquals( IdentityStoreTestContext.SAMPLE_CONNECTIONID, identityDB.getConnectionId( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityDB.getAttributes( ).size( ) );
        identityDB = IdentityStoreService.getIdentityByCustomerId( TEST_KOID, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNull( identityDB );
    }

    // getOrCreateIdentity untestable due to lack of attributes in MockIdentityInfoExternalProvider
}
