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
package fr.paris.lutece.plugins.identitystore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.SimpleCertifier;

/**
 * Init test context, such has certifiers
 */
public class IdentityStoreTestContext
{
    public final static String SAMPLE_APPCODE = "MyApplication";
    public final static String ATTRKEY_1 = "first_name";
    public final static String ATTRVAL_1 = "John";
    public final static String ATTRKEY_2 = "family_name";
    public final static String ATTRVAL_2 = "Doe";
    public final static String ATTRKEY_3 = "preferred_username";
    public final static String ATTRVAL_3 = "Joe";
    public final static int CERTIFIERS_NB = 3;
    public final static String CERTIFIER1_CODE = "certifiercode1";
    public final static String CERTIFIER1_NAME = CERTIFIER1_CODE + "_name";
    public final static int CERTIFIER1_LEVEL = 1;
    public final static int CERTIFIER1_EXPIRATIONDELAY = 10;
    public final static String CERTIFIER2_CODE = "certifiercode2";
    public final static String CERTIFIER2_NAME = CERTIFIER2_CODE + "_name";
    public final static int CERTIFIER2_LEVEL = 2;
    public final static int CERTIFIER2_EXPIRATIONDELAY = 20;
    public final static String CERTIFIER3_CODE = "certifiercode3";
    public final static String CERTIFIER3_NAME = CERTIFIER3_CODE + "_name";
    public final static int CERTIFIER3_LEVEL = 2;
    public final static int CERTIFIER3_EXPIRATIONDELAY = 13;

    public final static String SAMPLE_CONNECTIONID = "azerty";
    public final static String SAMPLE_CUSTOMERID = "3F2504E0-4F89-11D3-9A0C-0305E82C3301";
    public final static int SAMPLE_NB_ATTR = 18;

    private static boolean _bInit = false;

    /**
     * singleton
     */
    private IdentityStoreTestContext( )
    {
        super( );
        // TODO Auto-generated constructor stub
    }

    public static void initContext( ) throws CertifierNotFoundException
    {
        if ( !_bInit )
        {
            _bInit = true;
            init( );
        }
    }

    private static void init( )
    {
        // register certifiers
        AbstractCertifier certif1 = new SimpleCertifier( CERTIFIER1_CODE );
        certif1.setCertificateLevel( CERTIFIER1_LEVEL );
        certif1.setExpirationDelay( CERTIFIER1_EXPIRATIONDELAY );
        certif1.setName( CERTIFIER1_NAME );
        certif1.setCertifiableAttributesList( Arrays.asList( new String [ ] {
                ATTRKEY_1, ATTRKEY_3
        } ) );

        AbstractCertifier certif2 = new SimpleCertifier( CERTIFIER2_CODE );
        certif2.setCertificateLevel( CERTIFIER2_LEVEL );
        certif2.setExpirationDelay( CERTIFIER2_EXPIRATIONDELAY );
        certif2.setName( CERTIFIER2_NAME );
        certif2.setCertifiableAttributesList( Arrays.asList( new String [ ] {
                ATTRKEY_1, ATTRKEY_2
        } ) );

        AbstractCertifier certif3 = new SimpleCertifier( CERTIFIER3_CODE );
        certif3.setCertificateLevel( CERTIFIER3_LEVEL );
        certif3.setExpirationDelay( CERTIFIER3_EXPIRATIONDELAY );
        certif3.setName( CERTIFIER3_NAME );
        certif3.setCertifiableAttributesList( Arrays.asList( new String [ ] {
                ATTRKEY_1, ATTRKEY_2, ATTRKEY_3
        } ) );

        // update rights from init_db_identitystore_sample.sql
        ClientApplication clientApp = ClientApplicationHome.findByCode( SAMPLE_APPCODE );
        List<AttributeRight> listAttrRight = new ArrayList<>( );
        for ( AttributeRight attributeRight : ClientApplicationHome.selectApplicationRights( clientApp ) )
        {
            if ( ATTRKEY_1.equals( attributeRight.getAttributeKey( ).getKeyName( ) ) || ATTRKEY_3.equals( attributeRight.getAttributeKey( ).getKeyName( ) ) )
            {
                attributeRight.setCertifiable( true );
            }
            listAttrRight.add( attributeRight );
        }
        ClientApplicationHome.removeApplicationRights( clientApp );
        ClientApplicationHome.addAttributeRights( listAttrRight );

        // add certifier to application
        ClientApplicationHome.addCertifier( clientApp, certif1 );
        ClientApplicationHome.addCertifier( clientApp, certif2 );
        ClientApplicationHome.addCertifier( clientApp, certif3 );
    }
}
