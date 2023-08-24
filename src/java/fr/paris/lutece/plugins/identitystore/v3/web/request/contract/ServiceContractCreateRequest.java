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

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeCertification;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;

/**
 * This class represents a create request for ServiceContractRestService
 */
public class ServiceContractCreateRequest extends AbstractIdentityStoreRequest
{
    private final ServiceContractDto _serviceContractDto;

    /**
     * Constructor of ServiceContractCreateRequest
     *
     * @param serviceContractDto
     *            the dto of identity's change
     * @param strClientAppCode
     *            the app client code
     */
    public ServiceContractCreateRequest( ServiceContractDto serviceContractDto, String strClientAppCode )
    {
        super( strClientAppCode );
        this._serviceContractDto = serviceContractDto;
    }

    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkServiceContract( _serviceContractDto );
        IdentityRequestValidator.instance( ).checkClientApplication( _strClientCode );
    }

    @Override
    public ServiceContractChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final ServiceContractChangeResponse response = new ServiceContractChangeResponse( );
        final ClientApplication clientApplication = ClientApplicationHome.findByCode( _strClientCode );
        if ( clientApplication == null )
        {
            response.setStatus( ServiceContractChangeStatusType.FAILURE );
            response.setMessage( "No application could be found with code " + _strClientCode );
            response.setI18nMessageKey( Constants.PROPERTY_REST_ERROR_APPLICATION_NOT_FOUND );
        }
        else
        {
            final ServiceContract serviceContract = ServiceContractService.instance( ).create( DtoConverter.convertDtoToContract( _serviceContractDto ),
                    clientApplication.getId( ) );
            // TODO amélioration générale à mener sur ce point
            for ( final AttributeCertification certification : serviceContract.getAttributeCertifications( ) )
            {
                for ( final RefAttributeCertificationProcessus processus : certification.getRefAttributeCertificationProcessus( ) )
                {
                    processus.setLevel(
                            AttributeCertificationDefinitionService.instance( ).get( processus.getCode( ), certification.getAttributeKey( ).getKeyName( ) ) );
                }
            }
            response.setServiceContract( DtoConverter.convertContractToDto( serviceContract ) );
            response.setStatus( ServiceContractChangeStatusType.SUCCESS );
            response.setI18nMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION );
        }

        return response;
    }

}
