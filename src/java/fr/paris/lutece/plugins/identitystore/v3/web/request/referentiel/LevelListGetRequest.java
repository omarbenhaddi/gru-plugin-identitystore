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
package fr.paris.lutece.plugins.identitystore.v3.web.request.referentiel;

import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevelHome;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.LevelSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.ReferentielSearchStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class LevelListGetRequest extends AbstractIdentityStoreRequest
{

    /**
     * Constructor of IdentityStoreGetRequest
     *
     * @param strClientCode
     *            the client application Code
     */
    public LevelListGetRequest( String strClientCode )
    {
        super( strClientCode );
    }

    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        // TODO ?
    }

    /**
     * get the identity
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    public LevelSearchResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final LevelSearchResponse response = new LevelSearchResponse( );

        final List<RefCertificationLevel> refCertificationLevelsList = RefCertificationLevelHome.getRefCertificationLevelsList( );

        if ( refCertificationLevelsList == null || CollectionUtils.isEmpty( refCertificationLevelsList ) )
        {
            response.setStatus( ReferentielSearchStatusType.NOT_FOUND );
            response.setI18nMessageKey( Constants.PROPERTY_REST_ERROR_NO_CERTIFICATION_LEVEL_FOUND );
        }
        else
        {
            response.setLevels( DtoConverter.convertRefLevelsToListDto( refCertificationLevelsList ) );
            response.setStatus( ReferentielSearchStatusType.SUCCESS );
            response.setI18nMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION );
        }

        return response;
    }

}
