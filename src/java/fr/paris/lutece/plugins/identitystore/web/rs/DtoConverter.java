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
package fr.paris.lutece.plugins.identitystore.web.rs;

import fr.paris.lutece.plugins.identitystore.business.Attribute;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifierHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.CertificateDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.service.AuthorType;
import fr.paris.lutece.portal.service.util.AppException;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * class to help managing rest feature
 *
 */
public final class DtoConverter
{
    /**
     * private constructor
     */
    private DtoConverter(  )
    {
    }

    /**
     * returns a identityDto initialized from provided identity
     *
     * @param identity
     *          business identity to convert
     * @param strClientAppCode
     *          client app code
     * @return identityDto initialized from provided identity
     */
    public static IdentityDto convertToDto( Identity identity, String strClientAppCode )
    {
        IdentityDto identityDto = new IdentityDto(  );
        identityDto.setConnectionId( identity.getConnectionId(  ) );
        identityDto.setCustomerId( identity.getCustomerId(  ) );

        if ( identity.getAttributes(  ) != null )
        {
            Map<String, AttributeDto> mapAttributeDto = new HashMap<String, AttributeDto>(  );
            List<AttributeRight> lstRights = ClientApplicationHome.selectApplicationRights( ClientApplicationHome.findByCode( 
                        strClientAppCode ) );

            for ( Attribute attribute : identity.getAttributes(  ).values(  ) )
            {
                AttributeDto attrDto = new AttributeDto(  );
                attrDto.setKey( attribute.getKey(  ) );
                attrDto.setValue( attribute.getValue(  ) );
                attrDto.setType( AttributeKeyHome.findByKey( attribute.getKey(  ) ).getKeyType(  ).getCode(  ) );

                for ( AttributeRight attRight : lstRights )
                {
                    if ( attRight.getAttributeKey(  ).getKeyName(  ).equals( attribute.getKey(  ) ) )
                    {
                        attrDto.setReadable( attRight.isReadable(  ) );
                        attrDto.setWritable( attRight.isWritable(  ) );
                        attrDto.setCertifiable( attRight.isCertifiable(  ) );
                        attrDto.setCertified( attribute.getCertificate(  ) != null );

                        break;
                    }
                }

                if ( attribute.getCertificate(  ) != null )
                {
                    AttributeCertifier certifier = AttributeCertifierHome.findByPrimaryKey( attribute.getCertificate(  )
                                                                                                     .getIdCertifier(  ) );
                    CertificateDto certifDto = new CertificateDto(  );
                    certifDto.setCertificateExpirationDate( attribute.getCertificate(  ).getExpirationDate(  ) );
                    certifDto.setCertifierCode( certifier.getCode(  ) );
                    certifDto.setCertifierName( certifier.getName(  ) );
                    certifDto.setCertifierLevel( attribute.getCertificate(  ).getCertificateLevel(  ) );
                    attrDto.setCertificate( certifDto );
                }

                mapAttributeDto.put( attrDto.getKey(  ), attrDto );
            }

            identityDto.setAttributes( mapAttributeDto );
        }

        return identityDto;
    }

    /**
     * returns ChangeAuthor read from authorDto
     *
     * @param authorDto
     *          authorDto (mandatory)
     * @return changeAuthor initialized from Dto datas
     * @throws AppException
     *           if provided dto is null
     */
    public static ChangeAuthor getAuthor( AuthorDto authorDto )
    {
        ChangeAuthor author = null;

        if ( authorDto != null )
        {
            author = new ChangeAuthor(  );
            author.setApplication( authorDto.getApplicationName(  ) );
            author.setUserName( authorDto.getUserName(  ) );

            if ( ( authorDto.getType(  ) != AuthorType.TYPE_APPLICATION.getTypeValue(  ) ) &&
                    ( authorDto.getType(  ) != AuthorType.TYPE_USER_ADMINISTRATOR.getTypeValue(  ) ) &&
                    ( authorDto.getType(  ) != AuthorType.TYPE_USER_OWNER.getTypeValue(  ) ) )
            {
                throw new AppException( "type provided is unknown type=" + authorDto.getType(  ) );
            }

            if ( ( authorDto.getType(  ) == AuthorType.TYPE_USER_ADMINISTRATOR.getTypeValue(  ) ) &&
                    StringUtils.isEmpty( authorDto.getEmail(  ) ) )
            {
                throw new AppException( "email field is missing" );
            }

            author.setEmail( authorDto.getEmail(  ) );
            author.setType( authorDto.getType(  ) );
        }
        else
        {
            throw new AppException( "no author provided" );
        }

        return author;
    }

    /**
     * returns certificate from Dto
     *
     * @param certificateDto
     *          certificate dto (can be null)
     * @return certificate initialized from Dto datas, null if provided dto is
     *         null
     * @throws AppException
     *           if provided certifier code is unknown or its expiration date is
     *           already expired
     *
     */
    public static AttributeCertificate getCertificate( CertificateDto certificateDto )
        throws AppException
    {
        AttributeCertificate attributeCertificate = null;

        if ( certificateDto != null )
        {
            attributeCertificate = new AttributeCertificate(  );
            attributeCertificate.setCertificateLevel( certificateDto.getCertifierLevel(  ) );
            attributeCertificate.setCertifier( certificateDto.getCertifierName(  ) );

            AttributeCertifier certifier = AttributeCertifierHome.findByCode( certificateDto.getCertifierCode(  ) );

            if ( certifier == null )
            {
                throw new AppException( "Unknown Certifier code, provided code=" + certificateDto.getCertifierCode(  ) );
            }

            attributeCertificate.setIdCertifier( certifier.getId(  ) );
            attributeCertificate.setCertificateDate( new Timestamp( ( new Date(  ) ).getTime(  ) ) );

            if ( ( certificateDto.getCertificateExpirationDate(  ) != null ) &&
                    certificateDto.getCertificateExpirationDate(  ).before( new Date(  ) ) )
            {
                throw new AppException( "Certificate expiration date is expired =" +
                    certificateDto.getCertificateExpirationDate(  ) );
            }

            attributeCertificate.setExpirationDate( new Timestamp( 
                    ( certificateDto.getCertificateExpirationDate(  ) ).getTime(  ) ) );
        }

        return attributeCertificate;
    }
}
