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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRuleHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.SearchRuleType;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.html.AbstractPaginator;
import fr.paris.lutece.util.url.UrlItem;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage IdentitySearchRule features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageIdentitySearchRules.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_IDENTITY_SEARCH_RULES_MANAGEMENT" )
public class IdentitySearchRuleJspBean extends ManageIdentitiesJspBean
{

    // Templates
    private static final String TEMPLATE_MANAGE_IDENTITYSEARCHRULES = "/admin/plugins/identitystore/manage_identitysearchrules.html";
    private static final String TEMPLATE_CREATE_IDENTITYSEARCHRULE = "/admin/plugins/identitystore/create_identitysearchrule.html";
    private static final String TEMPLATE_MODIFY_IDENTITYSEARCHRULE = "/admin/plugins/identitystore/create_identitysearchrule.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITYSEARCHRULES = "identitystore.manage_identitysearchrules.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_IDENTITYSEARCHRULE = "identitystore.modify_identitysearchrule.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_IDENTITYSEARCHRULE = "identitystore.create_identitysearchrule.pageTitle";

    // Markers
    private static final String MARK_IDENTITYSEARCHRULE_LIST = "identitysearchrule_list";
    private static final String MARK_IDENTITYSEARCHRULE = "identitysearchrule";
    private static final String MARK_ACTION = "action";
    private static final String MARK_AVAILABLE_ATTRIBUTES = "available_attributes";
    private static final String MARK_AVAILABLE_RULE_TYPES = "available_rule_types";
    private static final String MARK_SELECTED_ATTRIBUTES = "rule_attributes";

    private static final String JSP_MANAGE_IDENTITYSEARCHRULES = "jsp/admin/plugins/identitystore/ManageIdentitySearchRules.jsp";

    // Views
    private static final String VIEW_MANAGE_IDENTITYSEARCHRULES = "manageIdentitySearchRules";
    private static final String VIEW_CREATE_IDENTITYSEARCHRULE = "createIdentitySearchRule";
    private static final String VIEW_MODIFY_IDENTITYSEARCHRULE = "modifyIdentitySearchRule";

    // Actions
    private static final String ACTION_CREATE_IDENTITYSEARCHRULE = "createIdentitySearchRule";
    private static final String ACTION_MODIFY_IDENTITYSEARCHRULE = "modifyIdentitySearchRule";
    private static final String ACTION_REMOVE_IDENTITYSEARCHRULE = "removeIdentitySearchRule";
    private static final String ACTION_CONFIRM_REMOVE_IDENTITYSEARCHRULE = "confirmRemoveIdentitySearchRule";

    // Parameters
    private static final String PARAMETER_ID_IDENTITYSEARCHRULE = "id";
    private static final String PARAMETER_SELECTED_TYPE = "selected_type";
    private static final String PARAMETER_SELECTED_ATTRIBUTES = "selected_attributes";

    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_IDENTITYSEARCHRULE = "identitystore.message.confirmRemoveIdentitySearchRule";

    // Infos
    private static final String INFO_IDENTITYSEARCHRULE_CREATED = "identitystore.info.identitysearchrule.created";
    private static final String INFO_IDENTITYSEARCHRULE_UPDATED = "identitystore.info.identitysearchrule.updated";
    private static final String INFO_IDENTITYSEARCHRULE_REMOVED = "identitystore.info.identitysearchrule.removed";

    // Session variable to store working values
    private IdentitySearchRule _identitySearchRule;
    private final List<IdentitySearchRule> _listIdentitySearchRules = new ArrayList<>( );

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_IDENTITYSEARCHRULES, defaultView = true )
    public String getManageIdentitySearchRules( final HttpServletRequest request )
    {
        _identitySearchRule = null;
        _listIdentitySearchRules.clear( );

        if ( request.getParameter( AbstractPaginator.PARAMETER_PAGE_INDEX ) == null || _listIdentitySearchRules.isEmpty( ) )
        {
            _listIdentitySearchRules.addAll( IdentitySearchRuleHome.findAll( ) );
        }

        final Map<String, Object> model = getPaginatedListModel( request, MARK_IDENTITYSEARCHRULE_LIST, _listIdentitySearchRules,
                JSP_MANAGE_IDENTITYSEARCHRULES );
        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITYSEARCHRULES, TEMPLATE_MANAGE_IDENTITYSEARCHRULES, model );
    }

    /**
     * Returns the form to create an identity search rule
     *
     * @param request
     *            The Http request
     * @return the html code of the identity search rule form
     */
    @View( VIEW_CREATE_IDENTITYSEARCHRULE )
    public String getCreateDuplicateRule( HttpServletRequest request )
    {
        final Map<String, Object> model = getModel( );

        if ( _identitySearchRule == null )
        {
            _identitySearchRule = new IdentitySearchRule( );
        }

        model.put( MARK_IDENTITYSEARCHRULE, _identitySearchRule );
        model.put( MARK_SELECTED_ATTRIBUTES, new ArrayList<>( ) );
        model.put( MARK_AVAILABLE_RULE_TYPES, SearchRuleType.values( ) );
        model.put( MARK_ACTION, "action_createIdentitySearchRule" );
        model.put( MARK_AVAILABLE_ATTRIBUTES, AttributeKeyHome.getAttributeKeysList( ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_IDENTITYSEARCHRULE ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_IDENTITYSEARCHRULE, TEMPLATE_CREATE_IDENTITYSEARCHRULE, model );
    }

    /**
     * Process the data capture form of a new identity search rule
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_IDENTITYSEARCHRULE )
    public String doCreateIdentitySearchRule( final HttpServletRequest request ) throws AccessDeniedException
    {
        _identitySearchRule.setType( SearchRuleType.valueOf( request.getParameter( PARAMETER_SELECTED_TYPE ) ) );
        _identitySearchRule.getAttributes( ).addAll( this.extractAttributeKeys( request ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_IDENTITYSEARCHRULE ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( _identitySearchRule.getType( ) == null || _identitySearchRule.getAttributes( ).isEmpty( ) )
        {
            return redirect( request, VIEW_CREATE_IDENTITYSEARCHRULE );
        }

        try
        {
            IdentitySearchRuleHome.create( _identitySearchRule );
        }
        catch( final Exception e )
        {
            addError( e.getMessage( ) );
            return redirectView( request, VIEW_CREATE_IDENTITYSEARCHRULE );
        }

        addInfo( INFO_IDENTITYSEARCHRULE_CREATED, getLocale( ) );
        _listIdentitySearchRules.clear( );
        _identitySearchRule = null;

        return redirectView( request, VIEW_MANAGE_IDENTITYSEARCHRULES );
    }

    private List<AttributeKey> extractAttributeKeys( final HttpServletRequest request )
    {
        final String [ ] selectedAttributes = request.getParameterValues( PARAMETER_SELECTED_ATTRIBUTES );
        if ( selectedAttributes != null )
        {
            return Arrays.stream( selectedAttributes ).map( AttributeKeyHome::findByKey ).collect( Collectors.toList( ) );
        }
        return new ArrayList<>( );
    }

    /**
     * Manages the removal form of an identity search rule whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_IDENTITYSEARCHRULE )
    public String getConfirmRemoveIdentitySearchRule( final HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITYSEARCHRULE ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_IDENTITYSEARCHRULE ) );
        url.addParameter( PARAMETER_ID_IDENTITYSEARCHRULE, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_IDENTITYSEARCHRULE, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of an identity search rule
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage identity search rules
     */
    @Action( ACTION_REMOVE_IDENTITYSEARCHRULE )
    public String doRemoveIdentitySearchRule( final HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITYSEARCHRULE ) );
        IdentitySearchRuleHome.delete( nId );
        addInfo( INFO_IDENTITYSEARCHRULE_REMOVED, getLocale( ) );
        _listIdentitySearchRules.clear( );

        return redirectView( request, VIEW_MANAGE_IDENTITYSEARCHRULES );
    }

    /**
     * Returns the form to update info about an identity search rule
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_IDENTITYSEARCHRULE )
    public String getModifyIdentitySearchRule( final HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITYSEARCHRULE ) );

        if ( _identitySearchRule == null || ( _identitySearchRule.getId( ) != nId ) )
        {
            _identitySearchRule = IdentitySearchRuleHome.find( nId );
            if ( _identitySearchRule == null )
            {
                throw new AppException( ERROR_RESOURCE_NOT_FOUND );
            }
        }

        final Map<String, Object> model = getModel( );

        model.put( MARK_IDENTITYSEARCHRULE, _identitySearchRule );
        model.put( MARK_SELECTED_ATTRIBUTES, _identitySearchRule.getAttributes( ).stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toList( ) ) );
        model.put( MARK_AVAILABLE_RULE_TYPES, SearchRuleType.values( ) );
        model.put( MARK_ACTION, "action_modifyIdentitySearchRule" );
        model.put( MARK_AVAILABLE_ATTRIBUTES, AttributeKeyHome.getAttributeKeysList( ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_IDENTITYSEARCHRULE ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_IDENTITYSEARCHRULE, TEMPLATE_MODIFY_IDENTITYSEARCHRULE, model );
    }

    /**
     * Process the change form of an identity search rule
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_IDENTITYSEARCHRULE )
    public String doModifyIdentitySearchRule( final HttpServletRequest request ) throws AccessDeniedException
    {
        _identitySearchRule.setType( SearchRuleType.valueOf( request.getParameter( PARAMETER_SELECTED_TYPE ) ) );
        _identitySearchRule.getAttributes( ).clear( );
        _identitySearchRule.getAttributes( ).addAll( this.extractAttributeKeys( request ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_IDENTITYSEARCHRULE ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( _identitySearchRule.getType( ) == null || _identitySearchRule.getAttributes( ).isEmpty( ) )
        {
            return redirect( request, VIEW_MODIFY_IDENTITYSEARCHRULE, PARAMETER_ID_IDENTITYSEARCHRULE, _identitySearchRule.getId( ) );
        }

        try
        {
            IdentitySearchRuleHome.update( _identitySearchRule );
        }
        catch( final Exception e )
        {
            addError( e.getMessage( ) );
            return redirect( request, VIEW_MODIFY_IDENTITYSEARCHRULE, PARAMETER_ID_IDENTITYSEARCHRULE, _identitySearchRule.getId( ) );
        }

        addInfo( INFO_IDENTITYSEARCHRULE_UPDATED, getLocale( ) );
        _listIdentitySearchRules.clear( );
        _identitySearchRule = null;

        return redirectView( request, VIEW_MANAGE_IDENTITYSEARCHRULES );
    }

}
