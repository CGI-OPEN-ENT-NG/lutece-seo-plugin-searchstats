<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="searchstats" scope="session" class="fr.paris.lutece.plugins.searchstats.web.SearchStatsJspBean" />

<% searchstats.init( request , searchstats.RIGHT_MANAGE_SEARCH_STATS ); %>
<%= searchstats.manageSearchStats( request )%>

<%@include file="../../AdminFooter.jsp" %>
