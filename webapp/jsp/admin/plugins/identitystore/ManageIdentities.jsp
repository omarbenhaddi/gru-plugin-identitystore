<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="manageidentities" scope="session" class="fr.paris.lutece.plugins.identitystore.web.ManageIdentitiesJspBean" />

<% manageidentities.init( request, manageidentities.RIGHT_MANAGEIDENTITIES ); %>
<%= manageidentities.getManageIdentitiesHome ( request ) %>

<%@ include file="../../AdminFooter.jsp" %>
