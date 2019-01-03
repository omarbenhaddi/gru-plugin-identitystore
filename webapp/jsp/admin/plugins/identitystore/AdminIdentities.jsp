<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="adminidentities" scope="session" class="fr.paris.lutece.plugins.identitystore.web.AdminIdentitiesJspBean" />

<% adminidentities.init( request, adminidentities.RIGHT_ADMINIDENTITIES ); %>
<%= adminidentities.getAdminIdentitiesHome ( request ) %>

<%@ include file="../../AdminFooter.jsp" %>
