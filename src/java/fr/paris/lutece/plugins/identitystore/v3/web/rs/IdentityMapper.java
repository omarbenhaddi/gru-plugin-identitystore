/*
 * Copyright (c) 2002-2023, City of Paris
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
package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentityMapper
{

    public static IdentityDto toDto( final Identity identity )
    {
        final IdentityDto dto = new IdentityDto( );
        dto.setConnectionId( identity.getConnectionId( ) );
        dto.setCustomerId( identity.getCustomerId( ) );
        dto.setMonParisActive( identity.isMonParisActive( ) );

        final List<AttributeDto> attributeDtos = identity.getAttributes( ).entrySet( ).stream( ).map( attr -> {
            AttributeDto attribute = new AttributeDto( );
            attribute.setKey( attr.getKey( ) );
            attribute.setValue( attr.getValue( ).getValue( ) );
            if ( attr.getValue( ).getCertificate( ) != null )
            {
                attribute.setCertifier( attr.getValue( ).getCertificate( ).getCertifierCode( ) );
                attribute.setCertificationDate( attr.getValue( ).getCertificate( ).getExpirationDate( ) );
            }

            return attribute;
        } ).collect( Collectors.toList( ) );

        dto.setAttributes( attributeDtos );

        return dto;
    }

    public static Identity toBean( final IdentityDto identityDto ){
        final Identity bean = new Identity();
        bean.setCustomerId(identityDto.getCustomerId());
        bean.setConnectionId(identityDto.getConnectionId());
        bean.setMerged(identityDto.getMerge() != null && identityDto.getMerge().isMerged());
        bean.setExpirationDate(identityDto.getExpiration() != null ? identityDto.getExpiration().getExpirationDate() : null);
        bean.setDeleted(identityDto.getExpiration() != null && identityDto.getExpiration().isDeleted());
        bean.setMonParisActive(identityDto.isMonParisActive());
        bean.setLastUpdateDate(identityDto.getLastUpdateDate());
        final Map<String, IdentityAttribute> attributes = identityDto.getAttributes( ).stream( ).map(attributeDto -> {
            IdentityAttribute attribute = new IdentityAttribute( );
            final AttributeKey attributeKey = new AttributeKey();
            attributeKey.setKeyName(attributeDto.getKey( ));
            attributeKey.setKeyType(KeyType.valueOf(attributeDto.getType()));
            attribute.setAttributeKey(attributeKey);
            attribute.setValue( attributeDto.getValue( ) );
            if ( attributeDto.getCertifier( ) != null )
            {
                final AttributeCertificate certificate = new AttributeCertificate();
                attribute.setCertificate(certificate);
                certificate.setCertifierCode( attributeDto.getCertifier() );
                certificate.setCertifierName( attributeDto.getCertifier() );
                certificate.setCertificateDate(Timestamp.from(attributeDto.getCertificationDate().toInstant()));
            }

            return attribute;
        } ).collect( Collectors.toMap( identityAttribute -> identityAttribute.getAttributeKey().getKeyName(), identityAttribute -> identityAttribute ) );
        bean.getAttributes().putAll(attributes);
        return bean;
    }
}
