<jsp:useBean id="adminidentitiesAttributeCertifier" scope="session" class="fr.paris.lutece.plugins.identitystore.web.AttributeCertifierJspBean" />
<% String strContent = adminidentitiesAttributeCertifier.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
