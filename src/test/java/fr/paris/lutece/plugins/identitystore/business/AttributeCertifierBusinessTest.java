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


public class AttributeCertifierBusinessTest extends LuteceTestCase
{
    private final static String NAME1 = "Name1";
    private final static String NAME2 = "Name2";
    private final static String DESCRIPTION1 = "Description1";
    private final static String DESCRIPTION2 = "Description2";
    private final static String LOGO1 = "Logo1";
    private final static String LOGO2 = "Logo2";

    public void testBusiness(  )
    {
        // Initialize an object
        AttributeCertifier attributeCertifier = new AttributeCertifier();
        attributeCertifier.setName( NAME1 );
        attributeCertifier.setDescription( DESCRIPTION1 );

        // Create test
        AttributeCertifierHome.create( attributeCertifier );
        AttributeCertifier attributeCertifierStored = AttributeCertifierHome.findByPrimaryKey( attributeCertifier.getId( ) );
        assertEquals( attributeCertifierStored.getName() , attributeCertifier.getName( ) );
        assertEquals( attributeCertifierStored.getDescription() , attributeCertifier.getDescription( ) );

        // Update test
        attributeCertifier.setName( NAME2 );
        attributeCertifier.setDescription( DESCRIPTION2 );
        AttributeCertifierHome.update( attributeCertifier );
        attributeCertifierStored = AttributeCertifierHome.findByPrimaryKey( attributeCertifier.getId( ) );
        assertEquals( attributeCertifierStored.getName() , attributeCertifier.getName( ) );
        assertEquals( attributeCertifierStored.getDescription() , attributeCertifier.getDescription( ) );

        // List test
        AttributeCertifierHome.getAttributeCertifiersList();

        // Delete test
        AttributeCertifierHome.remove( attributeCertifier.getId( ) );
        attributeCertifierStored = AttributeCertifierHome.findByPrimaryKey( attributeCertifier.getId( ) );
        assertNull( attributeCertifierStored );
        
    }

}