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
package fr.paris.lutece.plugins.identitystore.v3.web.request.contract;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeCertification;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractsSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * This class represents a get request for ServiceContractRestService
 *
 */
public class ServiceContractListGetRequest extends AbstractIdentityStoreRequest
{
    private final String _strTargetClientCode;

    /**
     * Constructor of ServiceContractListGetRequest
     *
     * @param strClientCode
     *            the client application Code
     */
    public ServiceContractListGetRequest( String strTargetClientCode, String strClientCode, String authorName, String authorType ) throws IdentityStoreException
    {
        super( strClientCode, authorName, authorType );
        this._strTargetClientCode = strTargetClientCode;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkTargetClientCode( this._strTargetClientCode );
    }

    /**
     * get the service contract list
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    public ServiceContractsSearchResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final ServiceContractsSearchResponse response = new ServiceContractsSearchResponse( );

        final List<ServiceContract> serviceContracts = ClientApplicationHome.selectServiceContracts( ClientApplicationHome.findByCode( _strTargetClientCode ) );
        if ( CollectionUtils.isEmpty( serviceContracts ) )
        {
            response.setStatus( ResponseStatusFactory.noResult( ).setMessageKey( Constants.PROPERTY_REST_ERROR_NO_SERVICE_CONTRACT_FOUND ) );
        }
        else
        {
            for ( final ServiceContract serviceContract : serviceContracts )
            {
                serviceContract.setAttributeRights( ServiceContractHome.selectApplicationRights( serviceContract ) );
                serviceContract.setAttributeCertifications( ServiceContractHome.selectAttributeCertifications( serviceContract ) );
                serviceContract.setAttributeRequirements( ServiceContractHome.selectAttributeRequirements( serviceContract ) );
                // TODO amélioration générale à mener sur ce point
                for ( final AttributeCertification certification : serviceContract.getAttributeCertifications( ) )
                {
                    for ( final RefAttributeCertificationProcessus processus : certification.getRefAttributeCertificationProcessus( ) )
                    {
                        processus.setLevel( AttributeCertificationDefinitionService.instance( ).get( processus.getCode( ),
                                certification.getAttributeKey( ).getKeyName( ) ) );
                    }
                }

                response.getServiceContracts( ).add( DtoConverter.convertContractToDto( serviceContract ) );
            }

            response.setStatus( ResponseStatusFactory.ok( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
        }

        return response;
    }

}
