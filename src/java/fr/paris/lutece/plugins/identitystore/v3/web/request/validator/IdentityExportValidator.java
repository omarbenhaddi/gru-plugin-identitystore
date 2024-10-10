/*
 * Copyright (c) 2002-2024, City of Paris
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
package fr.paris.lutece.plugins.identitystore.v3.web.request.validator;

import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class IdentityExportValidator
{
    private final int exportLimit = AppPropertiesService.getPropertyInt( "identitystore.identity.export.size.limit", 500 );

    private static IdentityExportValidator instance;

    public static IdentityExportValidator instance( )
    {
        if ( instance == null )
        {
            instance = new IdentityExportValidator( );
        }
        return instance;
    }

    private IdentityExportValidator( )
    {
    }

    public void validateExportRequest( final IdentityExportRequest request, final ServiceContract serviceContract ) throws RequestFormatException {
        if ( request.getCuidList( ).size( ) > exportLimit )
        {
            throw new RequestFormatException( "Provided CUID list exceeds the allowed export limit of " + exportLimit,
                    Constants.PROPERTY_REST_ERROR_EXPORT_LIMIT_EXCEEDED );
        }

        if (request.getAttributeKeyList() != null && !request.getAttributeKeyList().isEmpty()) {

            for (final String searchAttributeKey : request.getAttributeKeyList()) {
                final Optional<AttributeRight> attributeRight = serviceContract.getAttributeRights().stream()
                        .filter(a -> StringUtils.equals(a.getAttributeKey().getKeyName(), searchAttributeKey)).findFirst();
                if (attributeRight.isPresent()) {
                    boolean canReadAttribute = attributeRight.get().isReadable();

                    if (!canReadAttribute) {
                        throw new RequestFormatException(searchAttributeKey + " key is not readable in service contract definition.", Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION);
                    }
                } else {
                    throw new RequestFormatException(searchAttributeKey + " key does not exist in service contract definition.", Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION);
                }
            }
        }
    }

}
