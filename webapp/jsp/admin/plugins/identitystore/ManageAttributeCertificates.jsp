<jsp:useBean id="adminidentitiesAttributeCertificate" scope="session" class="fr.paris.lutece.plugins.identitystore.web.AttributeCertificateJspBean" />
<% String strContent = adminidentitiesAttributeCertificate.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
