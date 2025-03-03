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
package fr.paris.lutece.plugins.identitystore.v3.web.request.contract;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeCertification;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

/**
 * This class represents a create request for ServiceContractRestService
 */
public class ServiceContractCreateRequest extends AbstractIdentityStoreAppCodeRequest
{
    private final ServiceContractDto _serviceContractDto;

    private ClientApplication clientApplication;
    private ServiceContract serviceContractToCreate;

    /**
     * Constructor of ServiceContractCreateRequest
     *
     * @param serviceContractDto
     *            the dto of identity's change
     * @param strClientCode
     *            the app client code
     */
    public ServiceContractCreateRequest( final ServiceContractDto serviceContractDto, final String strClientCode, final String strAppCode,
            final String authorName, final String authorType ) throws IdentityStoreException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this._serviceContractDto = serviceContractDto;
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException
    {
        if (_serviceContractDto != null && _serviceContractDto.getClientCode() != null) {
            clientApplication = ClientApplicationHome.findByCode(_serviceContractDto.getClientCode());
            if (clientApplication == null) {
                throw new ResourceNotFoundException("No application could be found with code " + _serviceContractDto.getClientCode(),
                                                    Constants.PROPERTY_REST_ERROR_APPLICATION_NOT_FOUND);
            }
            serviceContractToCreate = DtoConverter.convertDtoToContract(_serviceContractDto);
        }
    }

    @Override
    protected void validateRequestFormat( ) throws RequestFormatException
    {
        IdentityRequestValidator.instance( ).checkServiceContract( _serviceContractDto );
        ServiceContractService.instance( ).validateContractDefinition( serviceContractToCreate, clientApplication.getId( ) );
    }

    @Override
    protected void validateClientAuthorization( ) throws ClientAuthorizationException
    {
        // do nothing
    }

    @Override
    protected void validateResourcesConsistency( ) throws ResourceConsistencyException
    {
        // do nothing
    }

    @Override
    protected void formatRequestContent( ) throws RequestContentFormattingException
    {
        // do nothing
    }

    @Override
    protected void checkDuplicatesConsistency( ) throws DuplicatesConsistencyException
    {
        // do nothing
    }

    @Override
    protected ServiceContractChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final ServiceContractChangeResponse response = new ServiceContractChangeResponse( );
        final ServiceContract createdServiceContract = ServiceContractService.instance( ).create( serviceContractToCreate, clientApplication );
        // TODO amélioration générale à mener sur ce point
        for ( final AttributeCertification certification : createdServiceContract.getAttributeCertifications( ) )
        {
            for ( final RefAttributeCertificationProcessus processus : certification.getRefAttributeCertificationProcessus( ) )
            {
                processus.setLevel(
                        AttributeCertificationDefinitionService.instance( ).get( processus.getCode( ), certification.getAttributeKey( ).getKeyName( ) ) );
            }
        }
        response.setServiceContract( DtoConverter.convertContractToDto( createdServiceContract ) );
        response.setStatus( ResponseStatusFactory.success( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );

        return response;
    }

}
