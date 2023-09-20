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
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.util.Optional;

/**
 * This class represents a put end date request for ServiceContractRestService
 */
public class ServiceContractPutEndDateRequest extends AbstractIdentityStoreRequest
{
    private final ServiceContractDto _serviceContractDto;
    private final Integer _serviceContractId;

    /**
     * Constructor of ServiceContractPutEndDateRequest
     *
     * @param serviceContractDto
     *            the dto of identity's change
     * @param strClientAppCode
     *            the app client code
     * @param serviceContractId
     *            the id of the service contract
     */
    public ServiceContractPutEndDateRequest( ServiceContractDto serviceContractDto, String strClientAppCode, Integer serviceContractId, String authorName,
            String authorType ) throws IdentityStoreException
    {
        super( strClientAppCode, authorName, authorType );
        this._serviceContractDto = serviceContractDto;
        this._serviceContractId = serviceContractId;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkClosingServiceContract( _serviceContractDto );
        IdentityRequestValidator.instance( ).checkContractId( _serviceContractId );
    }

    @Override
    public ServiceContractChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final ServiceContractChangeResponse response = new ServiceContractChangeResponse( );
        final ClientApplication clientApplication = ClientApplicationHome.findByCode( _serviceContractDto.getClientCode( ) );
        if ( clientApplication == null )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "No application could be found with code " + _serviceContractDto.getClientCode( ) )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_APPLICATION_NOT_FOUND ) );
        }
        else
        {
            final Optional<ServiceContract> serviceContractToClose = ServiceContractHome.findByPrimaryKey( _serviceContractId );
            if ( !serviceContractToClose.isPresent( ) )
            {
                response.setStatus( ResponseStatusFactory.failure( ).setMessage( "No service contract could be found with code " + _serviceContractId )
                        .setMessageKey( Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_NOT_FOUND ) );
            }
            else
            {
                _serviceContractDto.setId( _serviceContractId );
                ServiceContractService.instance( ).close( DtoConverter.convertDtoToContract( _serviceContractDto ) );
                response.setStatus( ResponseStatusFactory.success( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
            }
        }

        return response;
    }

}
