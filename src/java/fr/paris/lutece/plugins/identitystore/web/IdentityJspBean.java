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
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.service.IdentityChange;
import fr.paris.lutece.plugins.identitystore.service.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.service.IdentityManagementResourceIdService;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.web.service.AuthorType;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private static final String TEMPLATE_MANAGE_IDENTITIES = "/admin/plugins/identitystore/manage_identities.html";
    private static final String TEMPLATE_CREATE_IDENTITY = "/admin/plugins/identitystore/create_identity.html";
    private static final String TEMPLATE_MODIFY_IDENTITY = "/admin/plugins/identitystore/modify_identity.html";
    private static final String TEMPLATE_VIEW_IDENTITY = "/admin/plugins/identitystore/view_identity.html";
    private static final String TEMPLATE_VIEW_ATTRIBUTE_HISTORY = "/admin/plugins/identitystore/view_attribute_change_history.html";

    // Parameters
    private static final String PARAMETER_ID_IDENTITY = "id";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_FAMILY_NAME = "family_name";
    private static final String PARAMETER_ATTRIBUTE_KEY = "attribute_key";
    private static final String PARAMETER_QUERY = "query";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES = "identitystore.manage_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_IDENTITY = "identitystore.modify_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_IDENTITY = "identitystore.create_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY = "identitystore.view_change_history.pageTitle";

    // Markers
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_ATTRIBUTES_CHANGE_MAP = "attributes_change_map";
    private static final String MARK_ATTRIBUTES_CURRENT_MAP = "attributes_current_map";
    private static final String MARK_QUERY = "query";
    private static final String MARK_HAS_CREATE_ROLE = "createIdentityRole";
    private static final String MARK_HAS_MODIFY_ROLE = "modifyIdentityRole";
    private static final String MARK_HAS_DELETE_ROLE = "deleteIdentityRole";
    private static final String MARK_HAS_VIEW_ROLE = "viewIdentityRole";
    private static final String MARK_HAS_ATTRIBUTS_HISTO_ROLE = "histoAttributsRole";
    private static final String JSP_MANAGE_IDENTITIES = "jsp/admin/plugins/identitystore/ManageIdentities.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_IDENTITY = "identitystore.message.confirmRemoveIdentity";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.identity.attribute.";

    // Views
    private static final String VIEW_MANAGE_IDENTITIES = "manageIdentitys";
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
    private String _strQuery;
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
    @View( value = VIEW_MANAGE_IDENTITIES, defaultView = true )
    public String getManageIdentitys( HttpServletRequest request )
    {
        _identity = null;
        String strQuery = request.getParameter( PARAMETER_QUERY );

        if ( strQuery != null )
        {
            _strQuery = strQuery;
        }

        List<Identity> listIdentities;
        if ( _strQuery != null )
        {
            listIdentities = IdentityHome.findByAttributeValue( _strQuery );
        }
        else
        {
            listIdentities = new ArrayList<Identity>( );
        }

        Map<String, Object> model = getPaginatedListModel( request, MARK_IDENTITY_LIST, listIdentities, JSP_MANAGE_IDENTITIES );
        model.put( MARK_QUERY, _strQuery );
        model.put( MARK_HAS_CREATE_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_CREATE_IDENTITY, getUser( ) ) );
        model.put( MARK_HAS_MODIFY_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_MODIFY_IDENTITY, getUser( ) ) );
        model.put( MARK_HAS_DELETE_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_DELETE_IDENTITY, getUser( ) ) );
        model.put( MARK_HAS_VIEW_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_VIEW_IDENTITY, getUser( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_MANAGE_IDENTITIES, model );
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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_CREATE_IDENTITY, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_CREATE_IDENTITY, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
        populate( _identity, request );

        // Check constraints
        if ( !validateBean( _identity, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_IDENTITY );
        }

        IdentityHome.create( _identity );
        IdentityAttribute idAttrFirstName = saveFirstNameAttribute( request.getParameter( PARAMETER_FIRST_NAME ) );
        IdentityAttribute idAttrLastName = saveLastNameAttribute( request.getParameter( PARAMETER_FAMILY_NAME ) );
        addInfo( INFO_IDENTITY_CREATED, getLocale( ) );

        // notify listeners
        IdentityChange identityChange = new IdentityChange( );
        identityChange.setIdentity( _identity );
        identityChange.setChangeType( IdentityChangeType.CREATE );
        IdentityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );

        AttributeChange changeFirstName = IdentityStoreNotifyListenerService.buildAttributeChange( _identity, idAttrFirstName.getAttributeKey( ).getKeyName( ),
                idAttrFirstName.getValue( ), StringUtils.EMPTY, getAuthor( ), idAttrFirstName.getCertificate( ), true );
        IdentityStoreNotifyListenerService.notifyListenersAttributeChange( changeFirstName );
        AttributeChange changeLastName = IdentityStoreNotifyListenerService.buildAttributeChange( _identity, idAttrLastName.getAttributeKey( ).getKeyName( ),
                idAttrLastName.getValue( ), StringUtils.EMPTY, getAuthor( ), idAttrLastName.getCertificate( ), true );
        IdentityStoreNotifyListenerService.notifyListenersAttributeChange( changeLastName );

        return redirectView( request, VIEW_MANAGE_IDENTITIES );
    }

    /**
     * save firstname in attribute s table
     *
     * @param strFirstName
     *            firstname to save
     */
    private IdentityAttribute saveFirstNameAttribute( String strFirstName )
    {
        IdentityAttribute idAttrFirstName = new IdentityAttribute( );
        idAttrFirstName.setValue( strFirstName );
        idAttrFirstName.setAttributeKey( _attrKeyFirstName );
        idAttrFirstName.setIdIdentity( _identity.getId( ) );
        IdentityAttributeHome.create( idAttrFirstName );
        return idAttrFirstName;
    }

    /**
     * save lastname in attribute s table
     *
     * @param strLastName
     *            lastname to save
     */
    private IdentityAttribute saveLastNameAttribute( String strLastName )
    {
        IdentityAttribute idAttrLastName = new IdentityAttribute( );
        idAttrLastName.setValue( strLastName );
        idAttrLastName.setAttributeKey( _attrKeyLastName );
        idAttrLastName.setIdIdentity( _identity.getId( ) );
        IdentityAttributeHome.create( idAttrLastName );
        return idAttrLastName;
    }

    private ChangeAuthor getAuthor( )
    {
        ChangeAuthor author = new ChangeAuthor( );
        author.setApplication( AppPropertiesService.getProperty( IdentityConstants.PROPERTY_APPLICATION_CODE ) );
        author.setEmail( getUser( ).getEmail( ) );
        author.setType( AuthorType.TYPE_USER_ADMINISTRATOR.getTypeValue( ) );
        author.setUserName( getUser( ).getFirstName( ) );
        return author;
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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_DELETE_IDENTITY, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_DELETE_IDENTITY, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );
        Identity identity = IdentityHome.findByPrimaryKey( nId );
        IdentityHome.remove( nId );
        addInfo( INFO_IDENTITY_REMOVED, getLocale( ) );

        // notify listeners
        IdentityChange identityChange = new IdentityChange( );
        identityChange.setIdentity( identity );
        identityChange.setChangeType( IdentityChangeType.DELETE );
        IdentityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );

        return redirectView( request, VIEW_MANAGE_IDENTITIES );
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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_MODIFY_IDENTITY, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_MODIFY_IDENTITY, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
        populate( _identity, request );

        // Check constraints
        if ( !validateBean( _identity, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_IDENTITY, PARAMETER_ID_IDENTITY, _identity.getId( ) );
        }

        IdentityHome.update( _identity );
        IdentityAttribute idAttrFirstName = _identity.getAttributes( ).get( _attrKeyFirstName.getKeyName( ) );
        String strCurrentFirstName = idAttrFirstName.getValue( );
        String strRequestFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        boolean bUpdateFirstName = !StringUtils.equals( strCurrentFirstName, strRequestFirstName );
        if ( bUpdateFirstName )
        {
            idAttrFirstName.setValue( strRequestFirstName );
            IdentityAttributeHome.update( idAttrFirstName );
            _identity.getAttributes( ).put( _attrKeyFirstName.getKeyName( ), idAttrFirstName );
        }
        IdentityAttribute idAttrLastName = _identity.getAttributes( ).get( _attrKeyLastName.getKeyName( ) );
        String strCurrentLastName = idAttrLastName.getValue( );
        String strRequestLastName = request.getParameter( PARAMETER_FAMILY_NAME );
        boolean bUpdateLastName = !StringUtils.equals( strCurrentLastName, strRequestLastName );
        if ( bUpdateLastName )
        {
            idAttrLastName.setValue( strRequestLastName );
            IdentityAttributeHome.update( idAttrLastName );
            _identity.getAttributes( ).put( _attrKeyLastName.getKeyName( ), idAttrLastName );
        }
        addInfo( INFO_IDENTITY_UPDATED, getLocale( ) );

        // notify listeners
        IdentityChange identityChange = new IdentityChange( );
        identityChange.setIdentity( _identity );
        identityChange.setChangeType( IdentityChangeType.UPDATE );
        IdentityStoreNotifyListenerService.notifyListenersIdentityChange( identityChange );

        if ( bUpdateFirstName )
        {
            AttributeChange changeFirstName = IdentityStoreNotifyListenerService.buildAttributeChange( _identity, idAttrFirstName.getAttributeKey( )
                    .getKeyName( ), idAttrFirstName.getValue( ), strCurrentFirstName, getAuthor( ), idAttrFirstName.getCertificate( ), false );
            IdentityStoreNotifyListenerService.notifyListenersAttributeChange( changeFirstName );
        }
        if ( bUpdateLastName )
        {
            AttributeChange changeLastName = IdentityStoreNotifyListenerService.buildAttributeChange( _identity,
                    idAttrLastName.getAttributeKey( ).getKeyName( ), idAttrLastName.getValue( ), strCurrentLastName, getAuthor( ),
                    idAttrLastName.getCertificate( ), false );
            IdentityStoreNotifyListenerService.notifyListenersAttributeChange( changeLastName );
        }

        return redirectView( request, VIEW_MANAGE_IDENTITIES );
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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_VIEW_IDENTITY, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_IDENTITY ) );

        _identity = IdentityHome.findByPrimaryKey( nId );

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, _identity );
        model.put( MARK_HAS_ATTRIBUTS_HISTO_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_ATTRIBUTS_HISTO, getUser( ) ) );

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
        if ( !IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_ATTRIBUTS_HISTO, getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }
        // here we use a LinkedHashMap to have same attributs order as in viewIdentity
        Map<String, List<AttributeChange>> mapAttributesChange = new LinkedHashMap<String, List<AttributeChange>>( );
        Map<String, IdentityAttribute> mapCurrentAttributes = new HashMap<String, IdentityAttribute>( );

        if ( _identity != null && MapUtils.isNotEmpty( _identity.getAttributes( ) ) )
        {
            for ( String strAttributeKey : _identity.getAttributes( ).keySet( ) )
            {
                mapCurrentAttributes.put( strAttributeKey, _identity.getAttributes( ).get( strAttributeKey ) );
                List<AttributeChange> lstAttributeChange = new ArrayList<AttributeChange>( );
                lstAttributeChange = IdentityAttributeHome.getAttributeChangeHistory( _identity.getId( ), strAttributeKey );
                mapAttributesChange.put( strAttributeKey, lstAttributeChange );
            }
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTES_CHANGE_MAP, mapAttributesChange );
        model.put( MARK_ATTRIBUTES_CURRENT_MAP, mapCurrentAttributes );

        return getPage( PROPERTY_PAGE_TITLE_VIEW_CHANGE_HISTORY, TEMPLATE_VIEW_ATTRIBUTE_HISTORY, model );
    }
}
