<jsp:useBean id="manageDuplicateRules" scope="session" class="fr.paris.lutece.plugins.identitystore.web.DuplicateRulesJspBean" />
<% String strContent = manageDuplicateRules.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
