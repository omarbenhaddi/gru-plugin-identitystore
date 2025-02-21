package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentity;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage identity lock (search, resolve)
 */
@Controller( controllerJsp = "IdentityLocks.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_LOCKS_MANAGEMENT" )
public class IdentityLocksJspBean extends MVCAdminJspBean
{
    // Messages


    // Beans

    // Properties
    protected static final String PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES = "identitymediation.search_duplicates.pageTitle";

    // Parameters
    protected static final String PARAM_SUSPICIOUS_IDENTITIES_LIST = "suspicious_identities_list";
    protected static final String PARAM_CUSTOMER_ID = "customer_id";

    //Views
    protected static final String VIEW_SEARCH_LOCKS = "searchLocks";

    //Actions
    protected static final String ACTION_REMOVE_LOCK = "confirmRemoveLockedIdentity";

    // Templates
    protected static final String TEMPLATE_SEARCH_LOCKS = "/admin/plugins/identitystore/search_locks.html";

    // Session variable to store working values
    protected RequestAuthor _agentAuthor;


    /**
     * Process the data to send the search request and returns the duplicates search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the duplicate form
     */
    @View( value = VIEW_SEARCH_LOCKS, defaultView = true )
    public String getSearchLocks( final HttpServletRequest request ) throws AccessDeniedException
    {
        List<SuspiciousIdentity> suspiciousIdentityList = SuspiciousIdentityHome.getAllLocks();
        suspiciousIdentityList.sort(new Comparator<SuspiciousIdentity>()
        {
            public int compare(SuspiciousIdentity o1, SuspiciousIdentity o2)
            {
                return o1.getLock().getLockEndDate().compareTo(o2.getLock().getLockEndDate());
            }
        });
        Collections.reverse(suspiciousIdentityList);
        final Map<String, Object> model = getModel( );

        model.put( PARAM_SUSPICIOUS_IDENTITIES_LIST, suspiciousIdentityList);

        return this.getPage( PROPERTY_PAGE_TITLE_SEARCH_DUPLICATES, TEMPLATE_SEARCH_LOCKS, model );
    }

    @Action( value = ACTION_REMOVE_LOCK)
    public String doRemoveLock( final HttpServletRequest request )
    {
        String customerId = request.getParameter( PARAM_CUSTOMER_ID );
        SuspiciousIdentityHome.removeLock( customerId );

        return redirectView( request, VIEW_SEARCH_LOCKS );
    }

    protected RequestAuthor buildAgentAuthor( )
    {
        if ( _agentAuthor == null )
        {
            _agentAuthor = new RequestAuthor( );
            _agentAuthor.setName( getUser( ).getEmail( ) );
            _agentAuthor.setType( AuthorType.agent );
        }
        return _agentAuthor;
    }

}
