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
package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.IdentityConstants;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.AttributeChange;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage Identity features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageIdentities.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class IdentityJspBean extends ManageIdentitiesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_IDENTITYS = "/admin/plugins/identitystore/manage_identities.html";
    private static final String TEMPLATE_CREATE_IDENTITY = "/admin/plugins/identitystore/create_identity.html";
    private static final String TEMPLATE_MODIFY_IDENTITY = "/admin/plugins/identitystore/modify_identity.html";
    private static final String TEMPLATE_VIEW_IDENTITY = "/admin/plugins/identitystore/view_identity.html";
    private static final String TEMPLATE_VIEW_ATTRIBUTE_HISTORY = "/admin/plugins/identitystore/view_attribute_change_history.html";

    // Parameters
    private static final String PARAMETER_ID_IDENTITY = "id";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_FAMILY_NAME = "family_name";
    private static final String PARAMETER_ATTRIBUTE_KEY = "attribute_key";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITYS = "identitystore.manage_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_IDENTITY = "identitystore.modify_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_IDENTITY = "identitystore.create_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY = "identitystore.view_change_history.pageTitle";

    // Markers
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_ATTRIBUTE_CHANGE_LIST = "attributes_change_list";
    private static final String JSP_MANAGE_IDENTITYS = "jsp/admin/plugins/identitystore/ManageIdentities.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_IDENTITY = "identitystore.message.confirmRemoveIdentity";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.identity.attribute.";

    // Views
    private static final String VIEW_MANAGE_IDENTITYS = "manageIdentitys";
    private static final String VIEW_CREATE_IDENTITY = "createIdentity";
    private static final String VIEW_MODIFY_IDENTITY = "modifyIdentity";
    private static final String VIEW_IDENTITY = "viewIdentity";
    private static final String VIEW_ATTRIBUTE_HISTORY = "viewAttributeHistory";

    // Actions
    private static final String ACTION_CREATE_IDENTITY = "createIdentity";
    private static final String ACTION_MODIFY_IDENTITY = "modifyIdentity";
    private static final String ACTION_REMOVE_IDENTITY = "removeIdentity";
    private static final String ACTION_CONFIRM_REMOVE_IDENTITY = "confirmRemoveIdentity";

    // Infos
    private static final String INFO_IDENTITY_CREATED = "identitystore.info.identity.created";
    private static final String INFO_IDENTITY_UPDATED = "identitystore.info.identity.updated";
    private static final String INFO_IDENTITY_REMOVED = "identitystore.info.identity.removed";

    // Session variable to store working values
    private Identity _identity;
    private AttributeKey _attrKeyFirstName = AttributeKeyHome.findByKey( AppPropertiesService
            .getProperty( IdentityConstants.PROPERTY_ATTRIBUTE_USER_NAME_GIVEN ) );
    private AttributeKey _attrKeyLastName = AttributeKeyHome.findByKey( AppPropertiesService
            .getProperty( IdentityConstants.PROPERTY_ATTRIBUTE_USER_PREFERRED_NAME ) );

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_IDENTITYS, defaultView = true )
    public String getManageIdentitys( HttpServletRequest request )
    {
        _identity = null;

        List<Identity> listIdentitys = IdentityHome.getIdentitysList( );
        Map<String, Object> model = getPaginatedListModel( request, MARK_IDENTITY_LIST, listIdentitys, JSP_MANAGE_IDENTITYS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITYS, TEMPLATE_MANAGE_IDENTITYS, model );
    }

    /**
     * Returns the form to create a identity
     *
     * @param request
     *            The Http request
     * @return the html code of the identity form
     */
    @View( VIEW_CREATE_IDENTITY )
    public String getCreateIdentity( HttpServletRequest request )
    {
        _identity = ( _identity != null ) ? _identity : new Identity( );

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, _identity );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_IDENTITY, TEMPLATE_CREATE_IDENTITY, model );
    }

    /**
     * Process the data capture form of a new identity
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_IDENTITY )
    public String doCreateIdentity( HttpServletRequest request )
    {
        populate( _identity, request );

        // Check constraints
        if ( !validateBean( _identity, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_IDENTITY );
        }

        IdentityHome.create( _identity );
        saveFirstNameAttribute( request.getParameter( PARAMETER_FIRST_NAME ) );
        saveLastNameAttribute( request.getParameter( PARAMETER_FAMILY_NAME ) );
        addInfo( INFO_IDENTITY_CREATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_IDENTITYS );
    }

    /**
     * save firstname in attribute s table
     *
     * @param strFirstName
     *            firstname to save
     */
    private void saveFirstNameAttribute( String strFirstName )
    {
        IdentityAttribute idAttrFirstName = new IdentityAttribute( );
        idAttrFirstName.setValue( strFirstName );
        idAttrFirstName.setAttributeKey( _attrKeyFirstName );
        idAttrFirstName.setIdIdentity( _identity.getId( ) );
        IdentityAttributeHome.create( idAttrFirstName );
    }

    /**
     * save lastname in attribute s table
     *
     * @param strLastName
     *            lastname to save
     */
    private void saveLastNameAttribute( String strLastName )
    {
        IdentityAttribute idAttrFirstName = new IdentityAttribute( );
        idAttrFirstName.setValue( strLastName );
        idAttrFirstName.setAttributeKey( _attrKeyLastName );
        idAttrFirstName.setIdIdentity( _identity.getId( ) );
        IdentityAttributeHome.create( idAttrFirstName );
    }

    /**
     * Manages the removal form of a identity whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_IDENTITY )
    public String getConfirmRemoveIdentity( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_IDENTITY ) );
        url.addParameter( PARAMETER_ID_IDENTITY, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_IDENTITY, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a identity
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage identitys
     */
    @Action( ACTION_REMOVE_IDENTITY )
    public String doRemoveIdentity( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );
        IdentityHome.remove( nId );
        addInfo( INFO_IDENTITY_REMOVED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_IDENTITYS );
    }

    /**
     * Returns the form to update info about a identity
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_IDENTITY )
    public String getModifyIdentity( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );

        if ( ( _identity == null ) || ( _identity.getId( ) != nId ) )
        {
            _identity = IdentityHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, _identity );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_IDENTITY, TEMPLATE_MODIFY_IDENTITY, model );
    }

    /**
     * Process the change form of a identity
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_IDENTITY )
    public String doModifyIdentity( HttpServletRequest request )
    {
        populate( _identity, request );

        // Check constraints
        if ( !validateBean( _identity, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_IDENTITY, PARAMETER_ID_IDENTITY, _identity.getId( ) );
        }

        IdentityHome.update( _identity );
        saveFirstNameAttribute( request.getParameter( PARAMETER_FIRST_NAME ) );
        saveLastNameAttribute( request.getParameter( PARAMETER_FAMILY_NAME ) );
        addInfo( INFO_IDENTITY_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_IDENTITYS );
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
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );

        _identity = IdentityHome.findByPrimaryKey( nId );

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, _identity );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_IDENTITY, TEMPLATE_VIEW_IDENTITY, model );
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
        String strAttributeKey = request.getParameter( PARAMETER_ATTRIBUTE_KEY );
        List<AttributeChange> lstAttributeChange = new ArrayList<AttributeChange>( );

        if ( ( _identity != null ) && StringUtils.isNotBlank( strAttributeKey ) )
        {
            lstAttributeChange = IdentityAttributeHome.getAttributeChangeHistory( _identity.getId( ), strAttributeKey );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTE_CHANGE_LIST, lstAttributeChange );

        return getPage( PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY, TEMPLATE_VIEW_ATTRIBUTE_HISTORY, model );
    }
}
