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

import fr.paris.lutece.plugins.identitystore.service.certifier.Certifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import java.util.Collection;

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

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS = "identitystore.manage_certifiers.pageTitle";

    // Markers
    private static final String MARK_ATTRIBUTECERTIFIER_LIST = "attributecertifier_list";

    // Views
    private static final String VIEW_MANAGE_ATTRIBUTECERTIFIERS = "manageAttributeCertifiers";


    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_ATTRIBUTECERTIFIERS, defaultView = true )
    public String getManageAttributeCertifiers( HttpServletRequest request )
    {

        Collection<Certifier> listAttributeCertifiers = CertifierRegistry.instance().getCertifiersList( );
        Map<String, Object> model = getModel();
        model.put( MARK_ATTRIBUTECERTIFIER_LIST, listAttributeCertifiers );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS, TEMPLATE_MANAGE_ATTRIBUTECERTIFIERS, model );
    }
}
