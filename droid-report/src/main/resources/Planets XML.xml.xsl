<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following
    conditions are met:

     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

     * Neither the name of the The National Archives nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

-->

<xsl:transform 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:datetime="http://exslt.org/dates-and-times" 
    exclude-result-prefixes="datetime">

	<xsl:output method="xml" indent="yes" encoding="UTF-8" /> 
	
	<xsl:template match="/">
		<xsl:apply-templates/>      
	</xsl:template>
	  
	<xsl:template match="Report">
	    <xsl:if test="count(Profiles/Profile) > 1">
	       <xsl:text disable-output-escaping="yes">&lt;FileProfiles&gt;</xsl:text>
	    </xsl:if>
		<xsl:for-each select="Profiles/Profile">
			<xsl:variable name="ProfileId" select="@Id"></xsl:variable>
			<FileProfile>
				<profilingStartDate><xsl:value-of select="StartDate"/></profilingStartDate>
				<profilingEndDate><xsl:value-of select="EndDate"/></profilingEndDate>
				<profilingSaveDate><xsl:value-of select="CreatedDate"/></profilingSaveDate>
				<totalReadableFiles><xsl:value-of select="../../ReportItems/ReportItem[Specification/Description='File count and sizes']/Groups/Group/ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Count"/></totalReadableFiles>
				<totalUnreadableFiles><xsl:value-of select="../../ReportItems/ReportItem[Specification/Description='Unreadable files']/Groups/Group/ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Count"/></totalUnreadableFiles>
				<totalUnreadableFolders><xsl:value-of select="../../ReportItems/ReportItem[Specification/Description='Unreadable folders']/Groups/Group/ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Count"/></totalUnreadableFolders>
				<totalSize><xsl:value-of select="../../ReportItems/ReportItem[Specification/Description='File count and sizes']/Groups/Group/ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Sum"/></totalSize>
				<smallestSize><xsl:value-of select="../../ReportItems/ReportItem[Specification/Description='File count and sizes']/Groups/Group/ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Min"/></smallestSize>
				<largestSize><xsl:value-of select="../../ReportItems/ReportItem[Specification/Description='File count and sizes']/Groups/Group/ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Max"/></largestSize>
				<meanSize><xsl:value-of select="../../ReportItems/ReportItem[Specification/Description='File count and sizes']/Groups/Group/ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Average"/></meanSize>
				<pathsProcessed>
					<xsl:for-each select="//Path">
						<pathItem><xsl:value-of select="."/></pathItem><xsl:text>&#10;</xsl:text>
					</xsl:for-each>
				</pathsProcessed>
				<byYear>
					<xsl:for-each select="../../ReportItems/ReportItem[Specification/Description='File count and sizes per year last modified']/Groups/Group[ProfileSummaries/ProfileSummary[@Id=$ProfileId]]">
						<yearItem>
							<year><xsl:value-of select="Values/Value"/></year>
							<numFiles><xsl:value-of select="ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Count"/></numFiles>
							<totalFileSize><xsl:value-of select="ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Sum"/></totalFileSize>
						</yearItem>
					</xsl:for-each>
				</byYear>
				<byFormat>
					<xsl:for-each select="../../ReportItems/ReportItem[Specification/Description='File sizes per PUID']/Groups/Group[ProfileSummaries/ProfileSummary[@Id=$ProfileId]]">
						<formatItem>
							<PUID><xsl:value-of select="Values/Value[position()=1]"/></PUID>
							<MIME><xsl:value-of select="Values/Value[position()=4]"/></MIME>
							<FormatName><xsl:value-of select="Values/Value[position()=2]"/></FormatName>
							<FormatVersion><xsl:value-of select="Values/Value[position()=3]"/></FormatVersion>
							<numFiles><xsl:value-of select="ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Count"/></numFiles>
							<totalFileSize><xsl:value-of select="ProfileSummaries/ProfileSummary[@Id=$ProfileId]/Sum"/></totalFileSize>
						</formatItem>
					</xsl:for-each>
				</byFormat>
			</FileProfile>
		</xsl:for-each>
        <xsl:if test="count(Profiles/Profile) > 1">
           <xsl:text disable-output-escaping="yes">&lt;/FileProfiles&gt;</xsl:text>
        </xsl:if>		
	</xsl:template>

</xsl:transform>
