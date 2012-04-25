<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:datetime="http://exslt.org/dates-and-times" 
    exclude-result-prefixes="datetime">

  <xsl:output method="xml" indent="yes" encoding="UTF-8" /> 
  <xsl:param name="reportDir" select="'report'"/>
  <xsl:template match="Report">
    <html>
	   <head>
  		 <style type="text/css">
			table.logo { width: 100%; padding: 0px; border: none; border: 1px solid #789DB3;}
			table.logo td { border: none; background-color: #F4F4F4; vertical-align: middle; text-align: left;padding: 4px; }
			table.simple { width: 100%; padding: 0px; border: none; border: 1px solid #789DB3;}
			table.simple td { border: none; background-color: #F4F4F4; vertical-align: middle; text-align: right;padding: 7px; }
			table.simple td.metadata { border: none; background-color: #F4F4F4; vertical-align: middle; text-align: left;padding: 7px; }
			table.simple th.group { background-color: #666; color: #fff; padding: 0px; text-align: center; border-bottom: 0px #fff solid; font-weight: bold;}
			table.simple th.metadata { background-color: #A0A0A0; color: #fff; padding: 0px; text-align: center; border-bottom: 0px #fff solid; font-weight: bold;}
			table.simple th.head { background-color: #666; color: #fff; padding: 4px; text-align: center; border-bottom: 2px #fff solid; font-weight: bold;} 
			table.simple td.foot { background-color: #ddd; color: #111; padding: 4px; text-align: right; border-bottom: 2px #fff solid; font-weight: bold;}
			table.embedded { width: 100%; padding: 0px; border: none; border: 0px solid #F4F4F4;}
			table.embedded th.head { background-color: #A0A0A0; color: #fff; padding: 0px; text-align: center; border-bottom: 0px #fff solid; font-weight: bold;}
			table.embedded td.groupvalues { background-color: #666; color: #fff; padding: 0px; text-align: center; border-bottom: 0px #fff solid; font-weight: bold;}
			table.embedded td { border: none; background-color: #F4F4F4; vertical-align: middle; text-align: right;padding: 3px; }
			table.embedded td.metadata { border: none; background-color: #F4F4F4; color: #000000; vertical-align: middle; text-align: left;padding: 3px; font-weight:normal}
  		 </style>
 	  </head>
      <body>
      	<h1><xsl:value-of select="Title"/></h1>
      	<xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="Title">
  </xsl:template>

<!-- Profile metadata -->
  <xsl:template name="profilesTemplate" match="Profiles">
    <h2>Profile Summary</h2>
    <table class="simple">
      <tr>
      	<th class="head">Name</th>
       	<th class="head">Signature version</th>
       	<th class="head">Container version</th>
   	    <th class="head">Started</th>
        <th class="head">Finished</th>
        <th class="head">Filters</th>
      </tr>
      <xsl:for-each select="Profile">
        <tr>
          <td><xsl:value-of select="Name"/></td>
	      <td><xsl:value-of select="SignatureFileVersion"/></td>
	      <td><xsl:value-of select="ContainerSignatureFileVersion"/></td>
	      <td><xsl:value-of select="datetime:format-date(StartDate,'dd MMM yyyy')"/></td>
	      <td><xsl:value-of select="datetime:format-date(EndDate,'dd MMM yyyy')"/></td>
	      <td><xsl:if test="Filter/Enabled = 'true'">
	      	<table class="embedded">
				<xsl:if test="count(Filter/Criteria) > 1">
					<tr>
						<td colspan="3" class="metadata">
							<xsl:if test="Filter/Narrowed = 'true'">
								<xsl:text>(all filter criteria below must be true)</xsl:text>
							</xsl:if>
							<xsl:if test="Filter/Narrowed != 'true'">
								<xsl:text>(any filter criteria below must be true)</xsl:text>
							</xsl:if>
						</td>
					</tr>
				</xsl:if>
				<xsl:for-each select="Filter/Criteria">
					<tr>
						<td class="metadata"><xsl:value-of select="FieldName"/><xsl:text> </xsl:text></td>
						<td class="metadata"><xsl:value-of select="Operator"/><xsl:text> </xsl:text></td>
						<td class="metadata"><xsl:value-of select="Value"/></td>
					</tr>
				</xsl:for-each>				
	      	</table>
	      </xsl:if></td>
	    </tr>
      </xsl:for-each>
   	</table>
  </xsl:template> 

<!-- Report items -->
  <xsl:template name="reportItemTemplate" match="ReportItems/ReportItem">
    <h2><xsl:value-of select="Specification/Description"/></h2>

	<!--  Report item descriptive metadata -->
    <table class="simple">
    	<tr>
    		<th class="metadata">Report field</th>
    		<th class="metadata">Grouping fields</th>
    	</tr>
    	<tr>
    		<td class="metadata"><xsl:value-of select="Specification/Field"/></td>
		    <td>
		    	<xsl:if test="Specification/GroupByFields != ''">
			    	<table class="embedded">
			    		<tr>
			    			<xsl:for-each select="Specification/GroupByFields/GroupByField">
				    			<td class="metadata">
					    			<xsl:if test="Function != ''">
			    						<xsl:value-of select="Function"/><xsl:text>(</xsl:text>
		  		  					</xsl:if>
			    					<xsl:value-of select="Field"/>
		    						<xsl:if test="Function != ''">
			    						<xsl:text>)</xsl:text>
		    						</xsl:if>
								</td>
		    				</xsl:for-each>
		    			</tr>
		    		</table>
		    	</xsl:if>
		    </td>
    	</tr>
		<xsl:if test="Specification/Filter != ''">
			<tr><th class="metadata" colspan="2"><xsl:text>Filter fields: </xsl:text> 
				<xsl:if test="count(Specification/Filter/Criteria) > 1">
					<xsl:if test="Specification/Filter/Narrowed = 'true'">
						<xsl:text>(all filter criteria below must be true)</xsl:text>
					</xsl:if>
					<xsl:if test="Specification/Filter/Narrowed != 'true'">
						<xsl:text>(any filter criteria below must be true)</xsl:text>
					</xsl:if>
				</xsl:if>
			</th></tr>
			<tr><th colspan="2"><table class="embedded">
				<tr>
					<th class="head">Field</th>
					<th class="head">Operator</th>
					<th class="head">Values</th>
				</tr>			
				<xsl:for-each select="Specification/Filter/Criteria">
					<tr>
						<td class="metadata"><xsl:value-of select="FieldName"/><xsl:text> </xsl:text></td>
						<td class="metadata"><xsl:value-of select="Operator"/><xsl:text> </xsl:text></td>
						<td class="metadata"><xsl:value-of select="Value"/></td>
					</tr>
				</xsl:for-each>
			</table></th></tr>
		</xsl:if>
    </table><p/>
    
    <!-- Report values -->
    <xsl:for-each select="Groups/Group">
      <table class="simple">
		<xsl:if test="../../Specification/GroupByFields != ''">
			<tr>
				<th colspan="6" class="group">
					<table class="embedded"><tr>
						<xsl:for-each select="Values/Value">
		  					<td class="groupvalues"><xsl:value-of select="."/></td>
		  				</xsl:for-each>
	  				</tr></table>
				</th>
			</tr>
		</xsl:if>
        <tr>
          <th class="head">Profile</th>
          <th class="head">Count</th>  
          <th class="head">Sum</th>  
          <th class="head">Min</th>  
          <th class="head">Max</th>  
          <th class="head">Average</th>  
        </tr>
        <xsl:for-each select="ProfileSummaries/ProfileSummary">
			<tr>
		      <td><xsl:value-of select="Name"/></td>
		      <td><xsl:value-of select="Count"/></td>
		      <td><xsl:value-of select="Sum"/></td>
		      <td><xsl:value-of select="Min"/></td>
		      <td><xsl:value-of select="Max"/></td>
		      <td><xsl:value-of select="Average"/></td>
		    </tr>        
        </xsl:for-each>
        <tr>
	       	<td class="foot">Profile totals</td>
	       	<td class="foot"><xsl:value-of select="GroupAggregateSummary/Count"/></td>
	       	<td class="foot"><xsl:value-of select="GroupAggregateSummary/Sum"/></td>
	       	<td class="foot"><xsl:value-of select="GroupAggregateSummary/Min"/></td>
	       	<td class="foot"><xsl:value-of select="GroupAggregateSummary/Max"/></td>
	       	<td class="foot"><xsl:value-of select="GroupAggregateSummary/Average"/></td>
        </tr>
      </table><p/>
    </xsl:for-each>
    <xsl:if test="Specification/GroupByFields != ''">
	    <h3>Group totals</h3>
    	<table class="simple">
	          <th class="head">Count</th>  
	          <th class="head">Sum</th>  
	          <th class="head">Min</th>  
	          <th class="head">Max</th>  
	          <th class="head">Average</th>
	          <tr>
		          <td><xsl:value-of select="ReportItemAggregateSummary/Count"/></td>
		          <td><xsl:value-of select="ReportItemAggregateSummary/Sum"/></td>
		          <td><xsl:value-of select="ReportItemAggregateSummary/Min"/></td>
		          <td><xsl:value-of select="ReportItemAggregateSummary/Max"/></td>
		          <td><xsl:value-of select="ReportItemAggregateSummary/Average"/></td>
	          </tr>
	    </table><p/>
	</xsl:if>
    <table>
    </table>
  </xsl:template>
	
</xsl:transform>
