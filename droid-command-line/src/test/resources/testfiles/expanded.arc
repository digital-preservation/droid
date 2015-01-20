filedesc://IAH-20141126102606-00000-argos-8080.arc 0.0.0.0 20141126102606 text/plain 1259
1 1 InternetArchive
URL IP-address Archive-date Content-type Archive-length
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<arcmetadata xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:arc="http://archive.org/arc/1.0/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://archive.org/arc/1.0/" xsi:schemaLocation="http://archive.org/arc/1.0/ http://www.archive.org/arc/1.0/arc.xsd">
<arc:software>Heritrix 1.14.4 http://crawler.archive.org</arc:software>
<arc:hostname>argos</arc:hostname>
<arc:ip>127.0.0.1</arc:ip>
<dcterms:isPartOf>test</dcterms:isPartOf>
<dc:description>Default Profile</dc:description>
<arc:operator>Admin</arc:operator>
<ns0:date xmlns:ns0="http://purl.org/dc/elements/1.1/" xsi:type="dcterms:W3CDTF">2014-11-26T10:25:55+00:00</ns0:date>
<arc:http-header-user-agent>Mozilla/5.0 (compatible; heritrix/1.14.4 +http://www.nationalarchive.gov.uk/test.html)</arc:http-header-user-agent>
<arc:http-header-from>admin@nationalarchives.gov.uk</arc:http-header-from>
<arc:robots>classic</arc:robots>
<dc:format>ARC file version 1.1</dc:format>
<dcterms:conformsTo xsi:type="dcterms:URI">http://www.archive.org/web/researcher/ArcFileFormat.php</dcterms:conformsTo>
</arcmetadata>

http://127.0.0.1/robots.txt 127.0.0.1 20141126102605 text/html 431
HTTP/1.1 404 Not Found
Date: Wed, 26 Nov 2014 10:26:05 GMT
Server: Apache
Content-Length: 267
Connection: close
Content-Type: text/html; charset=iso-8859-1

<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
<html><head>
<title>404 Not Found</title>
</head><body>
<h1>Not Found</h1>
<p>The requested URL /robots.txt was not found on this server.</p>
<hr>
<address>Apache Server at 127.0.0.1 Port 80</address>
</body></html>

http://127.0.0.1/ 127.0.0.1 20141126102608 text/html 458
HTTP/1.1 200 OK
Date: Wed, 26 Nov 2014 10:26:08 GMT
Server: Apache
Last-Modified: Tue, 25 Nov 2014 11:53:02 GMT
ETag: "5626c3-dc-508ad8da02380"
Accept-Ranges: bytes
Content-Length: 220
Connection: close
Content-Type: text/html

<html>
	<head>
		<script language="javascript" type="text/javascript" src="js/test.js"></script>
	</head>
	<body>
		<h1>Test</h1>
		<ul>
			<li><a href="content/test.html">Content page</a></li>
		</ul>
	</body>
</html>


http://127.0.0.1/js/test.js 127.0.0.1 20141126102610 application/x-javascript 279
HTTP/1.1 200 OK
Date: Wed, 26 Nov 2014 10:26:10 GMT
Server: Apache
Last-Modified: Tue, 25 Nov 2014 11:50:49 GMT
ETag: "562680-1b-508ad85b2b840"
Accept-Ranges: bytes
Content-Length: 27
Connection: close
Content-Type: application/x-javascript

         alert("js Test");

http://127.0.0.1/content/test.html 127.0.0.1 20141126102612 text/html 357
HTTP/1.1 200 OK
Date: Wed, 26 Nov 2014 10:26:12 GMT
Server: Apache
Last-Modified: Tue, 25 Nov 2014 11:52:03 GMT
ETag: "5626c6-77-508ad8a1bdec0"
Accept-Ranges: bytes
Content-Length: 119
Connection: close
Content-Type: text/html

<html>
	<head>
		<title>Testing</title>
	</head>
	<body>
		<h1>Testing</h1>
		<p>This is my test</p>
	</body>
</html>


