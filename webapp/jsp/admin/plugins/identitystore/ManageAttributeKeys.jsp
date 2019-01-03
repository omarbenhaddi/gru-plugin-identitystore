<jsp:useBean id="adminidentitiesAttributeKey" scope="session" class="fr.paris.lutece.plugins.identitystore.web.AttributeKeyJspBean" />
<% String strContent = adminidentitiesAttributeKey.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
