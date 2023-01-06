<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@page import="com.agilej.viewer.server.*"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*" %>

<%! static Logger LOG = Logger.getLogger("diagram.jsp"); %>

<%
final String hashFragment = request.getParameter("_escaped_fragment_");
%>

<!doctype html>
<html id="main">
<head>
<link rel="shortcut icon" href="icon.png">
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta name="keywords" content="uml, class diagram, reverse engineered, java, diagram generator">
<meta name="fragment" content="!">
<link type="text/css" rel="stylesheet" href="viewer.css">
<title>AgileJ UML class diagram</title>

<%
		if (hashFragment == null)
		{
%>
<script type="text/javascript" language="javascript" src="viewer/viewer.nocache.js"></script>

<script src="viewer/ace/ace.js" type="text/javascript" charset="utf-8"></script>
<script src="viewer/ace/theme-eclipse.js" type="text/javascript" charset="utf-8"></script>
<script src="viewer/ace/mode-javascript.js" type="text/javascript" charset="utf-8"></script>
<script src="viewer/ace/theme-tomorrow.js" type="text/javascript" charset="utf-8"></script>
<script src="viewer/ace/tooltip.js" type="text/javascript" charset="utf-8"></script>
<script src="viewer/ace/jquery-2.1.1.min.js" type="text/javascript" charset="utf-8"></script>

<!--  Needed for autocompletion support. -->
<script src="viewer/ace/ext-language_tools.js" type="text/javascript" charset="utf-8"></script>

<%
		}
%> 
</head>
<body>
	<%
	try
	{
		if (hashFragment == null)
		{
	%>

	<!-- history support -->
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position: absolute; width: 0; height: 0; border: 0"></iframe>
	<noscript>
		<div style="width: 22em; position: center; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">Your web browser must have JavaScript enabled in order for this application to display correctly.</div>
	</noscript>
	<table width="100%" height="90%">
		<tr>
			<td id="bodyContainer"></td>
		</tr>
	</table>
	<table width="90%">
		<tr>
			<td id="displayeditor"></td>
		</tr>
		<tr>
			<td id="label"> </td>
		</tr>
		
	</table>
	
	<% 		
		}
		else
		{
			final List<String> typeNames = SitemapHelper.getTypeNames(hashFragment);
	%>

	<h2>
		This auto-generated Java class diagram is reverse engineered from <%=typeNames.size()%> Java classes and interfaces.
	</h2>
	
	<%
			for (String typeName : typeNames)
			{
	%>
	<%=typeName%><br />
	<% 
			}
	%>
	<p>
		Please see full details about this class diagram generator at <a href="http://www.agilej.com">www.agilej.com</a>.
		Reverse engineered Java UML class diagrams for the Eclipse IDE.
	</p>
	<% 
		}
	}
	catch (Exception exception)
	{
		LOG.error("exception in viewer.jsp page", exception);
	}
	%>
	
</body>
</html>

