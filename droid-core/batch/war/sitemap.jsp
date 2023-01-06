<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@page import="com.agilej.viewer.server.SitemapHelper"%>
<%@page import="javax.xml.parsers.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*"%>
<%@page import="java.text.*"%>
<%@page import="org.w3c.dom.*"%>

<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:image="http://www.google.com/schemas/sitemap-image/1.1"> 
<%
	final String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
	final String lastmod = request.getParameter("lastmod") != null ? request.getParameter("lastmod") : today;
	final String priority = request.getParameter("priority") != null ? request.getParameter("priority") : "0.8";
	final String changefreq = request.getParameter("changefreq") != null ? request.getParameter("changefreq") : "weekly";
	final Set<String> results = SitemapHelper.getSiteMap(request.getServletContext().getRealPath("/data"));
	final String baseURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	for (String url : results)
	{
%> 
	<url>
		<loc><%=baseURL%>/<%=url%></loc>
		<lastmod><%=lastmod%></lastmod>
		<priority><%=priority%></priority>
		<changefreq><%=changefreq%></changefreq>
	</url>
<%
	}
%> 
</urlset>
