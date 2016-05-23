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


public class IdentityAttributeBusinessTest extends LuteceTestCase
{
    private final static int IDIDENTITY1 = 1;
    private final static int IDIDENTITY2 = 2;
    private final static int IDATTRIBUTE1 = 1;
    private final static int IDATTRIBUTE2 = 2;
    private final static String ATTRIBUTEVALUE1 = "AttributeValue1";
    private final static String ATTRIBUTEVALUE2 = "AttributeValue2";
    private final static int IDCERTIFICATION1 = 1;
    private final static int IDCERTIFICATION2 = 2;

    public void testBusiness(  )
    {
        // Initialize an object
        IdentityAttribute identityAttribute = new IdentityAttribute(  );
        identityAttribute.setIdIdentity( IDIDENTITY1 );
        identityAttribute.setIdAttribute( IDATTRIBUTE1 );
        identityAttribute.setAttributeValue( ATTRIBUTEVALUE1 );
        identityAttribute.setIdCertificate( IDCERTIFICATION1 );

        // Create test
        IdentityAttributeHome.create( identityAttribute );

        IdentityAttribute identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity(  ),
                identityAttribute.getIdAttribute(  ) );
        assertEquals( identityAttributeStored.getIdIdentity(  ), identityAttribute.getIdIdentity(  ) );
        assertEquals( identityAttributeStored.getIdAttribute(  ), identityAttribute.getIdAttribute(  ) );
        assertEquals( identityAttributeStored.getAttributeValue(  ), identityAttribute.getAttributeValue(  ) );
        assertEquals( identityAttributeStored.getIdCertificate(  ), identityAttribute.getIdCertificate(  ) );

        // Update test
        identityAttribute.setIdIdentity( IDIDENTITY2 );
        identityAttribute.setIdAttribute( IDATTRIBUTE2 );
        identityAttribute.setAttributeValue( ATTRIBUTEVALUE2 );
        identityAttribute.setIdCertificate( IDCERTIFICATION2 );
        IdentityAttributeHome.update( identityAttribute );
        identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity(  ),
                identityAttribute.getIdAttribute(  ) );
        assertEquals( identityAttributeStored.getIdIdentity(  ), identityAttribute.getIdIdentity(  ) );
        assertEquals( identityAttributeStored.getIdAttribute(  ), identityAttribute.getIdAttribute(  ) );
        assertEquals( identityAttributeStored.getAttributeValue(  ), identityAttribute.getAttributeValue(  ) );
        assertEquals( identityAttributeStored.getIdCertificate(  ), identityAttribute.getIdCertificate(  ) );

        // List test
        IdentityAttributeHome.getAttributes( 1 );

        // Delete test
        IdentityAttributeHome.remove( identityAttribute.getIdIdentity(  ), identityAttribute.getIdAttribute(  ) );
        identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity(  ),
                identityAttribute.getIdAttribute(  ) );
        assertNull( identityAttributeStored );
    }
}
