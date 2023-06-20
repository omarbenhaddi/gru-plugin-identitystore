<jsp:useBean id="manageIdentitySearchRules" scope="session" class="fr.paris.lutece.plugins.identitystore.web.IdentitySearchRuleJspBean" />
<% String strContent = manageIdentitySearchRules.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
