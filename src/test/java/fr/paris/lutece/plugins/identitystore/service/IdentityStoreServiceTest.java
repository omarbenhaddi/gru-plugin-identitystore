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

import java.util.HashMap;
import java.util.Map;

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AppRightDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.ApplicationRightsDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeStatusDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.CertificateDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.service.AuthorType;
import fr.paris.lutece.test.LuteceTestCase;

/**
 *
 * @author levy
 */
public class IdentityStoreServiceTest extends LuteceTestCase
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

    // test according to initContext
    public void testService( )
    {
        // getApplicationRights
        ApplicationRightsDto apprights = IdentityStoreService.getApplicationRights( IdentityStoreTestContext.SAMPLE_APPCODE );
        assertEquals( IdentityStoreTestContext.SAMPLE_APPCODE, apprights.getApplicationCode( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, apprights.getAppRights( ).size( ) ); // cf init_db_identitystore_sample.sql
        for ( AppRightDto appright : apprights.getAppRights( ) )
        {
            int nSizeCertifier = 0;
            if ( IdentityStoreTestContext.ATTRKEY_1.equals( appright.getAttributeKey( ) ) )
            {
                nSizeCertifier = 3;
            }
            if ( IdentityStoreTestContext.ATTRKEY_3.equals( appright.getAttributeKey( ) ) )
            {
                nSizeCertifier = 2;
            }
            assertEquals( appright.getAttributeKey( ) + " READABLE", true, appright.isReadable( ) );
            assertEquals( appright.getAttributeKey( ) + " WRITABLE", true, appright.isWritable( ) );
            if ( nSizeCertifier == 0 )
            {
                assertTrue( appright.getCertifiers( ) == null || appright.getCertifiers( ).size( ) == 0 );
            }
            else
            {
                assertEquals( appright.getAttributeKey( ) + " CERTIFIER", nSizeCertifier, appright.getCertifiers( ).size( ) );
            }
        }

        // getIdentityByConnectionId
        Identity identityDB = IdentityStoreService.getIdentityByConnectionId( IdentityStoreTestContext.SAMPLE_CONNECTIONID,
                IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNotNull( identityDB );
        assertEquals( IdentityStoreTestContext.SAMPLE_CUSTOMERID, identityDB.getCustomerId( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityDB.getAttributes( ).size( ) );
        identityDB = IdentityStoreService.getIdentityByConnectionId( TEST_CONNECTIONID1, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNull( identityDB );

        // getIdentityByCustomerId
        identityDB = IdentityStoreService.getIdentityByCustomerId( IdentityStoreTestContext.SAMPLE_CUSTOMERID, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNotNull( identityDB );
        assertEquals( IdentityStoreTestContext.SAMPLE_CONNECTIONID, identityDB.getConnectionId( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityDB.getAttributes( ).size( ) );
        identityDB = IdentityStoreService.getIdentityByCustomerId( TEST_KOID, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNull( identityDB );

        // getAttribute
        IdentityAttribute attributeDB = IdentityStoreService.getAttribute( IdentityStoreTestContext.SAMPLE_CONNECTIONID, IdentityStoreTestContext.ATTRKEY_1,
                IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNotNull( attributeDB );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1, attributeDB.getValue( ) );
        attributeDB = IdentityStoreService.getAttribute( IdentityStoreTestContext.SAMPLE_CONNECTIONID, TEST_KOID, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNull( attributeDB );

        // getIdentityChangeForCreation
        // TEST_CONNECTIONID1 has already been tested to give null identity in previous lines
        IdentityChangeDto identityChangeDto = IdentityStoreService.buildIdentityChange( IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNotNull( identityChangeDto );
        assertNotNull( identityChangeDto.getIdentity( ) );
        assertNotNull( identityChangeDto.getAuthor( ) );
        assertNull( identityChangeDto.getIdentity( ).getConnectionId( ) );
        assertNull( identityChangeDto.getIdentity( ).getCustomerId( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_APPCODE, identityChangeDto.getAuthor( ).getApplicationCode( ) );
        assertEquals( AuthorType.TYPE_APPLICATION.getTypeValue( ), identityChangeDto.getAuthor( ).getType( ) );

        // getOrCreateIdentity untestable due to lack of attributes in MockIdentityInfoExternalProvider
        // removeIdentity untestable due to empty identitystore.application.code.delete.authorized.list

        // updateIdentity
        IdentityDto identityDto = new IdentityDto( );
        identityDto.setConnectionId( IdentityStoreTestContext.SAMPLE_CONNECTIONID );
        Map<String, AttributeDto> mapAttributes = new HashMap<>( );
        AttributeDto attr1 = new AttributeDto( );
        attr1.setKey( IdentityStoreTestContext.ATTRKEY_1 );
        attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "_test" );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_1, attr1 );
        identityDto.setAttributes( mapAttributes );
        identityChangeDto.setIdentity( identityDto );
        // simple update
        IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "_test", attr1After.getValue( ) );
        assertNull( attr1After.getCertificate( ) );
        assertFalse( attr1After.isCertified( ) );
        AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "_test", attr1StatusAfter.getNewValue( ) );
        assertNull( attr1StatusAfter.getNewCertifier( ) );
        assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );

        // unchange without certificate
        identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "_test", attr1After.getValue( ) );
        assertNull( attr1After.getCertificate( ) );
        assertFalse( attr1After.isCertified( ) );
        attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.INFO_NO_CHANGE_REQUEST_CODE, attr1StatusAfter.getStatusCode( ) );
        assertNull( attr1StatusAfter.getNewValue( ) );
        assertNull( attr1StatusAfter.getNewCertifier( ) );
        assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );

        // try remove not certified
        attr1.setValue( null );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_1, attr1 );
        identityDto.setAttributes( mapAttributes );
        identityChangeDto.setIdentity( identityDto );
        identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "_test", attr1After.getValue( ) );
        attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.INFO_DELETE_NOT_ALLOW_CODE, attr1StatusAfter.getStatusCode( ) );
        assertNull( attr1StatusAfter.getNewValue( ) );
        assertNull( attr1StatusAfter.getNewCertifier( ) );
        assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );

        // certify low certifier
        CertificateDto certificateDto = new CertificateDto( );
        certificateDto.setCertifierCode( IdentityStoreTestContext.CERTIFIER1_CODE );
        attr1.setValue( IdentityStoreTestContext.ATTRVAL_1 + "_certif1" );
        attr1.setCertificate( certificateDto );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_1, attr1 );
        identityDto.setAttributes( mapAttributes );
        identityChangeDto.setIdentity( identityDto );
        identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1 + "_certif1", attr1After.getValue( ) );
        assertNotNull( attr1After.getCertificate( ) );
        assertTrue( attr1After.isCertified( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
        attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1 + "_certif1", attr1StatusAfter.getNewValue( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1StatusAfter.getNewCertifier( ) );
        assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );

        // certify high certifier
        certificateDto = new CertificateDto( );
        certificateDto.setCertifierCode( IdentityStoreTestContext.CERTIFIER2_CODE );
        attr1.setValue( IdentityStoreTestContext.ATTRVAL_1 + "_certif2" );
        attr1.setCertificate( certificateDto );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_1, attr1 );
        identityDto.setAttributes( mapAttributes );
        identityChangeDto.setIdentity( identityDto );
        identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1 + "_certif2", attr1After.getValue( ) );
        assertNotNull( attr1After.getCertificate( ) );
        assertTrue( attr1After.isCertified( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
        attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1 + "_certif2", attr1StatusAfter.getNewValue( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1StatusAfter.getNewCertifier( ) );
        assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );

        // try update without certificat
        attr1.setValue( IdentityStoreTestContext.ATTRVAL_1 + "_test" );
        attr1.setCertificate( null );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_1, attr1 );
        identityDto.setAttributes( mapAttributes );
        identityChangeDto.setIdentity( identityDto );
        identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1 + "_certif2", attr1After.getValue( ) );
        assertNotNull( attr1After.getCertificate( ) );
        assertTrue( attr1After.isCertified( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
        attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
        assertNull( attr1StatusAfter.getNewValue( ) );
        assertNull( attr1StatusAfter.getNewCertifier( ) );
        assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );

        // try certify lower certifier
        certificateDto = new CertificateDto( );
        certificateDto.setCertifierCode( IdentityStoreTestContext.CERTIFIER1_CODE );
        attr1.setValue( IdentityStoreTestContext.ATTRVAL_1 + "_certif1" );
        attr1.setCertificate( certificateDto );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_1, attr1 );
        identityDto.setAttributes( mapAttributes );
        identityChangeDto.setIdentity( identityDto );
        identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1 + "_certif2", attr1After.getValue( ) );
        assertNotNull( attr1After.getCertificate( ) );
        assertTrue( attr1After.isCertified( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
        attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
        assertNull( attr1StatusAfter.getNewValue( ) );
        assertNull( attr1StatusAfter.getNewCertifier( ) );
        assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );

        // try certify same level but finish sooner
        certificateDto = new CertificateDto( );
        certificateDto.setCertifierCode( IdentityStoreTestContext.CERTIFIER3_CODE );
        attr1.setValue( IdentityStoreTestContext.ATTRVAL_1 + "_certif3" );
        attr1.setCertificate( certificateDto );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_1, attr1 );
        identityDto.setAttributes( mapAttributes );
        identityChangeDto.setIdentity( identityDto );
        identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
        assertNotNull( identityAfterDto );
        assertNotNull( identityAfterDto.getAttributes( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityAfterDto.getAttributes( ).size( ) );
        attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1After );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1 + "_certif2", attr1After.getValue( ) );
        assertNotNull( attr1After.getCertificate( ) );
        assertTrue( attr1After.isCertified( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
        attr1StatusAfter = attr1After.getStatus( );
        assertNotNull( attr1StatusAfter );
        assertEquals( AttributeStatusDto.INFO_LONGER_CERTIFIER_CODE, attr1StatusAfter.getStatusCode( ) );
        assertNull( attr1StatusAfter.getNewValue( ) );
        assertNull( attr1StatusAfter.getNewCertifier( ) );
        assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
    }
}
