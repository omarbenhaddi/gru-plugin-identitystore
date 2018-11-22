<jsp:useBean id="manageidentitiesIdentity" scope="session" class="fr.paris.lutece.plugins.identitystore.v2.web.IdentityJspBean" />
<% String strContent = manageidentitiesIdentity.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
