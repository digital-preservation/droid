<?xml version="1.0" encoding="UTF-8"?>

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
