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

import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifierHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.fileupload.FileItem;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * This class provides the user interface to manage AttributeCertifier features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageAttributeCertifiers.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class AttributeCertifierJspBean extends AdminIdentitiesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_ATTRIBUTECERTIFIERS = "/admin/plugins/identitystore/manage_certifiers.html";
    private static final String TEMPLATE_CREATE_ATTRIBUTECERTIFIER = "/admin/plugins/identitystore/create_certifier.html";
    private static final String TEMPLATE_MODIFY_ATTRIBUTECERTIFIER = "/admin/plugins/identitystore/modify_certifier.html";

    // Parameters
    private static final String PARAMETER_ID_ATTRIBUTECERTIFIER = "id";
    private static final String PARAMETER_LOGO_FILE = "logo_file";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS = "identitystore.manage_certifiers.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_ATTRIBUTECERTIFIER = "identitystore.modify_certifier.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_ATTRIBUTECERTIFIER = "identitystore.create_certifier.pageTitle";

    // Markers
    private static final String MARK_ATTRIBUTECERTIFIER_LIST = "attributecertifier_list";
    private static final String MARK_ATTRIBUTECERTIFIER = "attributecertifier";
    private static final String JSP_MANAGE_ATTRIBUTECERTIFIERS = "jsp/admin/plugins/identitystore/ManageAttributeCertifiers.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_ATTRIBUTECERTIFIER = "identitystore.message.confirmRemoveAttributeCertifier";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.attributecertifier.attribute.";

    // Views
    private static final String VIEW_MANAGE_ATTRIBUTECERTIFIERS = "manageAttributeCertifiers";
    private static final String VIEW_CREATE_ATTRIBUTECERTIFIER = "createAttributeCertifier";
    private static final String VIEW_MODIFY_ATTRIBUTECERTIFIER = "modifyAttributeCertifier";

    // Actions
    private static final String ACTION_CREATE_ATTRIBUTECERTIFIER = "createAttributeCertifier";
    private static final String ACTION_MODIFY_ATTRIBUTECERTIFIER = "modifyAttributeCertifier";
    private static final String ACTION_REMOVE_ATTRIBUTECERTIFIER = "removeAttributeCertifier";
    private static final String ACTION_CONFIRM_REMOVE_ATTRIBUTECERTIFIER = "confirmRemoveAttributeCertifier";

    // Infos
    private static final String INFO_ATTRIBUTECERTIFIER_CREATED = "identitystore.info.attributecertifier.created";
    private static final String INFO_ATTRIBUTECERTIFIER_UPDATED = "identitystore.info.attributecertifier.updated";
    private static final String INFO_ATTRIBUTECERTIFIER_REMOVED = "identitystore.info.attributecertifier.removed";

    // Session variable to store working values
    private AttributeCertifier _certifier;

    /**
     * Build the Manage View
     * @param request The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_ATTRIBUTECERTIFIERS, defaultView = true )
    public String getManageAttributeCertifiers( HttpServletRequest request )
    {
        _certifier = null;

        List<AttributeCertifier> listAttributeCertifiers = AttributeCertifierHome.getAttributeCertifiersList(  );
        Map<String, Object> model = getPaginatedListModel( request, MARK_ATTRIBUTECERTIFIER_LIST,
                listAttributeCertifiers, JSP_MANAGE_ATTRIBUTECERTIFIERS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS, TEMPLATE_MANAGE_ATTRIBUTECERTIFIERS, model );
    }

    /**
     * Returns the form to create a attributecertifier
     *
     * @param request The Http request
     * @return the html code of the attributecertifier form
     */
    @View( VIEW_CREATE_ATTRIBUTECERTIFIER )
    public String getCreateAttributeCertifier( HttpServletRequest request )
    {
        _certifier = ( _certifier != null ) ? _certifier : new AttributeCertifier(  );

        Map<String, Object> model = getModel(  );
        model.put( MARK_ATTRIBUTECERTIFIER, _certifier );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_ATTRIBUTECERTIFIER, TEMPLATE_CREATE_ATTRIBUTECERTIFIER, model );
    }

    /**
     * Process the data capture form of a new attributecertifier
     *
     * @param request The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_ATTRIBUTECERTIFIER )
    public String doCreateAttributeCertifier( HttpServletRequest request )
    {
        populate( _certifier, request );

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        FileItem fileItem = multipartRequest.getFile( PARAMETER_LOGO_FILE );

        if ( ( fileItem != null ) && ( fileItem.getName(  ) != null ) && !"".equals( fileItem.getName(  ) ) )
        {
            _certifier.setLogo( fileItem.get(  ) );
            _certifier.setLogoMimeType( fileItem.getContentType(  ) );
        }
        else
        {
            _certifier.setLogo( null );
        }

        // Check constraints
        if ( !validateBean( _certifier, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_ATTRIBUTECERTIFIER );
        }

        AttributeCertifierHome.create( _certifier );
        addInfo( INFO_ATTRIBUTECERTIFIER_CREATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTECERTIFIERS );
    }

    /**
     * Manages the removal form of a attributecertifier whose identifier is in the http
     * request
     *
     * @param request The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_ATTRIBUTECERTIFIER )
    public String getConfirmRemoveAttributeCertifier( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTECERTIFIER ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_ATTRIBUTECERTIFIER ) );
        url.addParameter( PARAMETER_ID_ATTRIBUTECERTIFIER, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_ATTRIBUTECERTIFIER,
                url.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a attributecertifier
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage attributecertifiers
     */
    @Action( ACTION_REMOVE_ATTRIBUTECERTIFIER )
    public String doRemoveAttributeCertifier( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTECERTIFIER ) );
        AttributeCertifierHome.remove( nId );
        addInfo( INFO_ATTRIBUTECERTIFIER_REMOVED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTECERTIFIERS );
    }

    /**
     * Returns the form to update info about a attributecertifier
     *
     * @param request The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_ATTRIBUTECERTIFIER )
    public String getModifyAttributeCertifier( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTECERTIFIER ) );

        if ( ( _certifier == null ) || ( _certifier.getId(  ) != nId ) )
        {
            _certifier = AttributeCertifierHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel(  );
        model.put( MARK_ATTRIBUTECERTIFIER, _certifier );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_ATTRIBUTECERTIFIER, TEMPLATE_MODIFY_ATTRIBUTECERTIFIER, model );
    }

    /**
     * Process the change form of a attributecertifier
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_ATTRIBUTECERTIFIER )
    public String doModifyAttributeCertifier( HttpServletRequest request )
    {
        populate( _certifier, request );

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        FileItem fileItem = multipartRequest.getFile( PARAMETER_LOGO_FILE );

        if ( ( fileItem != null ) && ( fileItem.getName(  ) != null ) && !"".equals( fileItem.getName(  ) ) )
        {
            _certifier.setLogo( fileItem.get(  ) );
            _certifier.setLogoMimeType( fileItem.getContentType(  ) );
        }
        else
        {
            _certifier.setLogo( null );
        }

        // Check constraints
        if ( !validateBean( _certifier, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_ATTRIBUTECERTIFIER, PARAMETER_ID_ATTRIBUTECERTIFIER,
                _certifier.getId(  ) );
        }

        AttributeCertifierHome.update( _certifier );
        addInfo( INFO_ATTRIBUTECERTIFIER_UPDATED, getLocale(  ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTECERTIFIERS );
    }
}
