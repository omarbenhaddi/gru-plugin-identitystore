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
package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.IdentityManagementResourceIdService;
import fr.paris.lutece.plugins.identitystore.service.contract.RefAttributeCertificationDefinitionNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides the user interface to manage Identity features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageIdentities.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class IdentityJspBean extends ManageIdentitiesJspBean
{
    /**
     * 
     */
    private static final long serialVersionUID = 6053504380426222888L;
    // Templates
    private static final String TEMPLATE_SEARCH_IDENTITIES = "/admin/plugins/identitystore/search_identities.html";
    private static final String TEMPLATE_VIEW_IDENTITY = "/admin/plugins/identitystore/view_identity.html";
    private static final String TEMPLATE_VIEW_ATTRIBUTE_HISTORY = "/admin/plugins/identitystore/view_attribute_change_history.html";

    // Parameters
    private static final String PARAMETER_ID_IDENTITY = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES = "identitystore.manage_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_VIEW_IDENTITY = "identitystore.create_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY = "identitystore.view_change_history.pageTitle";

    // Markers
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_ATTRIBUTES_CHANGE_MAP = "attributes_change_map";
    private static final String MARK_ATTRIBUTES_CURRENT_MAP = "attributes_current_map";
    private static final String MARK_CERTIFIERS_MAP = "certifiers_map";
    private static final String MARK_QUERY = "query";
    private static final String MARK_QUERY_FILTER = "query_filter";
    private static final String MARK_QUERY_FILTER_REFLIST = "query_filter_list";
    private static final String MARK_HAS_CREATE_ROLE = "createIdentityRole";
    private static final String MARK_HAS_MODIFY_ROLE = "modifyIdentityRole";
    private static final String MARK_HAS_DELETE_ROLE = "deleteIdentityRole";
    private static final String MARK_HAS_VIEW_ROLE = "viewIdentityRole";
    private static final String MARK_HAS_ATTRIBUTS_HISTO_ROLE = "histoAttributsRole";
    private static final String JSP_MANAGE_IDENTITIES = "jsp/admin/plugins/identitystore/ManageIdentities.jsp";

    // Views
    private static final String VIEW_MANAGE_IDENTITIES = "manageIdentitys";
    private static final String VIEW_IDENTITY = "viewIdentity";
    private static final String VIEW_ATTRIBUTE_HISTORY = "viewAttributeHistory";

    // Session variable to store working values
    private Identity _identity;

    private ISearchIdentityService _searchIdentityService = SpringContextService.getBean( "identitystore.db.searchIdentityService" );

    @View( value = VIEW_MANAGE_IDENTITIES, defaultView = true )
    public String getManageIdentitys( HttpServletRequest request ) throws RefAttributeCertificationDefinitionNotFoundException
    {
        _identity = null;
        final Map<String, String> queryParameters = this.getQueryParameters( request );

        final List<SearchAttributeDto> atttributes = new ArrayList<>( );
        final List<QualifiedIdentity> qualifiedIdentities = new ArrayList<>( );
        final String cuid = queryParameters.get( QUERY_PARAM_CUID );
        final String guid = queryParameters.get( QUERY_PARAM_GUID );
        final String email = queryParameters.get( QUERY_PARAM_EMAIL );
        final String gender = queryParameters.get( QUERY_PARAM_GENDER );
        final String family_name = queryParameters.get( QUERY_PARAM_FAMILY_NAME );
        final String preferred_username = queryParameters.get( QUERY_PARAM_PREFERRED_USERNAME );
        final String first_name = queryParameters.get( QUERY_PARAM_FIRST_NAME );
        final String birthdate = queryParameters.get( QUERY_PARAM_BIRTHDATE );
        final String birthplace = queryParameters.get( QUERY_PARAM_INSEE_BIRTHPLACE_LABEL );
        final String birthcountry = queryParameters.get( QUERY_PARAM_INSEE_BIRTHCOUNTRY_LABEL );
        final String phone = queryParameters.get( QUERY_PARAM_PHONE );

        if ( StringUtils.isNotEmpty( cuid ) )
        {
            final Identity identity = IdentityHome.findMasterIdentityByCustomerId( cuid );
            if ( identity != null )
            {
                final QualifiedIdentity qualifiedIdentity = DtoConverter.convertIdentityToDto( identity );
                qualifiedIdentities.add( qualifiedIdentity );
            }
        }
        else
        {
            if ( StringUtils.isNotEmpty( guid ) )
            {
                final Identity identity = IdentityHome.findMasterIdentityByConnectionId( guid );
                if ( identity != null )
                {
                    final QualifiedIdentity qualifiedIdentity = DtoConverter.convertIdentityToDto( identity );
                    qualifiedIdentities.add( qualifiedIdentity );
                }
            }
            else
            {
                if ( StringUtils.isNotEmpty( email ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_LOGIN, email, true ) );
                }
                if ( StringUtils.isNotEmpty( gender ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_GENDER, gender, true ) );
                }
                if ( StringUtils.isNotEmpty( family_name ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_FAMILY_NAME, family_name, false ) );
                }
                if ( StringUtils.isNotEmpty( preferred_username ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_PREFERRED_USERNAME, preferred_username, false ) );
                }
                if ( StringUtils.isNotEmpty( first_name ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_FIRST_NAME, first_name, false ) );
                }
                if ( StringUtils.isNotEmpty( birthdate ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_BIRTH_DATE, birthdate, true ) );
                }
                if ( StringUtils.isNotEmpty( birthplace ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_BIRTH_PLACE, birthplace, true ) );
                }
                if ( StringUtils.isNotEmpty( birthcountry ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_BIRTH_COUNTRY, birthcountry, true ) );
                }
                if ( StringUtils.isNotEmpty( phone ) )
                {
                    atttributes.add( new SearchAttributeDto( Constants.PARAM_MOBILE_PHONE, phone, true ) );
                }
                if ( CollectionUtils.isNotEmpty( atttributes ) )
                {
                    qualifiedIdentities.addAll( _searchIdentityService.getQualifiedIdentities( atttributes, 0, false ) );
                }
            }
        }

        final Map<String, Object> model = getPaginatedListModel( request, MARK_IDENTITY_LIST, qualifiedIdentities, JSP_MANAGE_IDENTITIES );
        model.put( MARK_HAS_CREATE_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_CREATE_IDENTITY, getUser( ) ) );
        model.put( MARK_HAS_MODIFY_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_MODIFY_IDENTITY, getUser( ) ) );
        model.put( MARK_HAS_DELETE_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_DELETE_IDENTITY, getUser( ) ) );
        model.put( MARK_HAS_VIEW_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_VIEW_IDENTITY, getUser( ) ) );

        model.put( QUERY_PARAM_CUID, cuid );
        model.put( QUERY_PARAM_GUID, guid );
        model.put( QUERY_PARAM_FAMILY_NAME, family_name );
        model.put( QUERY_PARAM_PREFERRED_USERNAME, preferred_username );
        model.put( QUERY_PARAM_FIRST_NAME, first_name );
        model.put( QUERY_PARAM_EMAIL, email );
        model.put( QUERY_PARAM_INSEE_BIRTHPLACE_LABEL, birthplace );
        model.put( QUERY_PARAM_INSEE_BIRTHCOUNTRY_LABEL, birthcountry );
        model.put( QUERY_PARAM_PHONE, phone );
        model.put( QUERY_PARAM_BIRTHDATE, birthdate );
        model.put( QUERY_PARAM_GENDER, gender );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_SEARCH_IDENTITIES, model );
    }

    /**
     * view identity
     *
     * @param request
     *            http request
     * @return The HTML form to view info
     */
    @View( VIEW_IDENTITY )
    public String getViewIdentity( HttpServletRequest request )
    {
        // int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );
        final String nId = request.getParameter( PARAMETER_ID_IDENTITY );

        _identity = IdentityHome.findByCustomerId( nId );

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, _identity );
        model.put( MARK_HAS_ATTRIBUTS_HISTO_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_ATTRIBUTS_HISTO, getUser( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_VIEW_IDENTITY, TEMPLATE_VIEW_IDENTITY, model );
    }

    /**
     * Build the attribute history View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_ATTRIBUTE_HISTORY )
    public String getAttributeHistoryView( HttpServletRequest request )
    {
        // here we use a LinkedHashMap to have same attributs order as in viewIdentity
        final Map<String, List<AttributeChange>> mapAttributesChange = new LinkedHashMap<>( );

        if ( _identity != null && MapUtils.isNotEmpty( _identity.getAttributes( ) ) )
        {
            for ( String strAttributeKey : _identity.getAttributes( ).keySet( ) )
            {
                mapAttributesChange.put( strAttributeKey, IdentityAttributeHome.getAttributeChangeHistory( _identity.getId( ), strAttributeKey ) );
            }
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTES_CHANGE_MAP, mapAttributesChange );

        return getPage( PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY, TEMPLATE_VIEW_ATTRIBUTE_HISTORY, model );
    }
}
