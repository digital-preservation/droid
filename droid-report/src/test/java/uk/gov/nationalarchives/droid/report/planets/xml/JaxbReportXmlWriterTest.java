/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.report.planets.xml;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.report.JaxbReportXmlWriter;


/**
 * @author rflitcroft
 *
 */
public class JaxbReportXmlWriterTest {

    private JaxbReportXmlWriter writer;
    
    @Before
    public void setup() throws JAXBException {
        writer = new JaxbReportXmlWriter();
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @Test
    public void testWriteReportWithOneItemAndOneProfile() throws Exception {
        /* intend to get rid of planets report... modify test to use xsl transformed result?
         * 
        Report report = new Report();
        
        ReportSpec reportSpec = new ReportSpec();
        ReportSpecItem item = new ReportSpecItem();
        item.setDescription("File sizes by PUID");
        item.setField(ReportFieldEnum.FILE_SIZE);
        item.setGroupByField(ReportFieldEnum.PUID);
        reportSpec.getItems().add(item);
        
        ReportItem reportItem = new ReportItem();
        reportItem.setReportSpecItem(item);
        
        ProfileReportData profileData = new ProfileReportData();
        profileData.setProfileName("profile 1");
        profileData.setAverage(10D);
        profileData.setCount(5L);
        profileData.setMax(20L);
        profileData.setMin(1L);
        profileData.setSum(50L);

        GroupedFieldItem groupedFieldItem = new GroupedFieldItem();
        groupedFieldItem.setValue("fmt/101");
        groupedFieldItem.addProfileData(profileData);
        
        reportItem.addGroupedFieldItem(groupedFieldItem);
        
        StringWriter out = new StringWriter();

        report.addItem(reportItem);
        
        writer.writeReport(report, out);
        
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n"
            +   "<Report>\r\n"
            +  "    <Profiles/>"
            +  "    <ReportItems>\r\n"
            +  "        <ReportItem>\r\n"
            +  "            <Specification>\r\n"
            +  "                <Description>File sizes by PUID</Description>"
            +  "                <Field>FILE_SIZE</Field>"
            +  "                <GroupByField>PUID</GroupByField>"
            +  "            </Specification>\r\n"
            +  "            <Groups>\r\n"
            +  "                <Group>\r\n"
            +  "                    <Value>fmt/101</Value>\r\n"
            +  "                    <ProfileSummaries>\r\n"
            +  "                        <ProfileSummary>\r\n"
            +  "                            <Name>profile 1</Name>\r\n"
            +  "                            <Count>5</Count>\r\n"
            +  "                            <Sum>50</Sum>\r\n"
            +  "                            <Min>1</Min>\r\n" 
            +  "                            <Max>20</Max>\r\n"
            +  "                            <Average>10.0</Average>\r\n"
            +  "                        </ProfileSummary>\r\n"
            +  "                    </ProfileSummaries>\r\n"
            +  "                    <GroupAggregateSummary>\r\n"
            +  "                        <Count>5</Count>\r\n"
            +  "                        <Sum>50</Sum>\r\n"
            +  "                        <Min>1</Min>\r\n" 
            +  "                        <Max>20</Max>\r\n"
            +  "                        <Average>10.0</Average>\r\n"
            +  "                    </GroupAggregateSummary>\r\n"
            +  "                </Group>\r\n"
            +  "            </Groups>\r\n"
            +  "            <ReportItemAggregateSummary>\r\n"
            +  "                <Count>5</Count>\r\n"
            +  "                <Sum>50</Sum>\r\n"
            +  "                <Min>1</Min>\r\n" 
            +  "                <Max>20</Max>\r\n"
            +  "                <Average>10.0</Average>\r\n"
            +  "            </ReportItemAggregateSummary>\r\n"
            +  "        </ReportItem>\r\n"
            +  "    </ReportItems>\r\n"
            +  "</Report>\r\n";
        
        System.out.println(out.getBuffer().toString());
        
        XMLAssert.assertXMLEqual(control, out.getBuffer().toString());
        */
    }

    @Test
    public void testWriteReportWithOneItemAndTwoProfiles() throws Exception {
        /*
        Report report = new Report();
        
        ReportSpec reportSpec = new ReportSpec();
        ReportSpecItem item = new ReportSpecItem();
        item.setField(ReportFieldEnum.FILE_SIZE);
        item.setGroupByField(ReportFieldEnum.PUID);
        reportSpec.getItems().add(item);
        
        ReportItem reportItem = new ReportItem();
        reportItem.setReportSpecItem(item);
        
        ProfileReportData profileData1 = new ProfileReportData();
        profileData1.setProfileName("profile 1");
        profileData1.setAverage(10D);
        profileData1.setCount(10L);
        profileData1.setMax(20L);
        profileData1.setMin(1L);
        profileData1.setSum(50L);

        ProfileReportData profileData2 = new ProfileReportData();
        profileData2.setProfileName("profile 2");
        profileData2.setAverage(20D);
        profileData2.setCount(7L);
        profileData2.setMax(19L);
        profileData2.setMin(0L);
        profileData2.setSum(60L);

        GroupedFieldItem groupedFieldItem = new GroupedFieldItem();
        groupedFieldItem.setValue("fmt/101");
        groupedFieldItem.addProfileData(profileData1);
        groupedFieldItem.addProfileData(profileData2);
        
        reportItem.addGroupedFieldItem(groupedFieldItem);
        
        StringWriter out = new StringWriter();

        report.addItem(reportItem);
        
        writer.writeReport(report, out);
        
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n"
            +   "<Report>\r\n"
            +  "    <Profiles/>"
            +  "    <ReportItems>\r\n"
            +  "        <ReportItem>\r\n"
            +  "            <Specification>\r\n"
            +  "                <Field>FILE_SIZE</Field>\r\n"
            +  "                <GroupByField>PUID</GroupByField>\r\n"
            +  "            </Specification>\r\n"
            +  "            <Groups>\r\n"
            +  "                <Group>\r\n"
            +  "                    <Value>fmt/101</Value>\r\n"
            +  "                    <ProfileSummaries>\r\n"
            +  "                        <ProfileSummary>\r\n"
            +  "                            <Count>10</Count>\r\n"
            +  "                            <Sum>50</Sum>\r\n"
            +  "                            <Min>1</Min>\r\n" 
            +  "                            <Max>20</Max>\r\n"
            +  "                            <Average>10.0</Average>\r\n"
            +  "                            <Name>profile 1</Name>\r\n"
            +  "                        </ProfileSummary>\r\n"
            +  "                        <ProfileSummary>\r\n"
            +  "                            <Count>7</Count>\r\n"
            +  "                            <Sum>60</Sum>\r\n"
            +  "                            <Min>0</Min>\r\n" 
            +  "                            <Max>19</Max>\r\n"
            +  "                            <Average>20.0</Average>\r\n"
            +  "                            <Name>profile 2</Name>\r\n"
            +  "                        </ProfileSummary>\r\n"
            +  "                    </ProfileSummaries>\r\n"
            +  "                    <GroupAggregateSummary>\r\n"
            +  "                        <Count>17</Count>\r\n"
            +  "                        <Sum>110</Sum>\r\n"
            +  "                        <Min>0</Min>\r\n" 
            +  "                        <Max>20</Max>\r\n"
            +  "                        <Average>14.117647058823529</Average>\r\n"
            +  "                    </GroupAggregateSummary>\r\n"
            +  "                </Group>\r\n"
            +  "            </Groups>\r\n"
            +  "            <ReportItemAggregateSummary>\r\n"
            +  "                <Count>17</Count>\r\n"
            +  "                <Sum>110</Sum>\r\n"
            +  "                <Min>0</Min>\r\n" 
            +  "                <Max>20</Max>\r\n"
            +  "                <Average>14.117647058823529</Average>\r\n"
            +  "            </ReportItemAggregateSummary>\r\n"
            +  "        </ReportItem>\r\n"
            +  "    </ReportItems>\r\n"
            +  "</Report>\r\n";
        
        System.out.println(out.getBuffer().toString());
        
        XMLAssert.assertXMLEqual(control, out.getBuffer().toString());
        */
    }
    
    @Test
    public void testWriteReportWithTwoGroupsAndTwoProfiles() throws Exception {
        /*
        Report report = new Report();
        
        ReportSpec reportSpec = new ReportSpec();
        ReportSpecItem item = new ReportSpecItem();
        item.setField(ReportFieldEnum.FILE_SIZE);
        item.setGroupByField(ReportFieldEnum.PUID);
        reportSpec.getItems().add(item);
        
        ReportItem reportItem = new ReportItem();
        reportItem.setReportSpecItem(item);
        
        ProfileReportData profileData1 = new ProfileReportData();
        profileData1.setProfileName("profile 1");
        profileData1.setAverage(10D);
        profileData1.setCount(10L);
        profileData1.setMax(20L);
        profileData1.setMin(1L);
        profileData1.setSum(50L);

        ProfileReportData profileData2 = new ProfileReportData();
        profileData2.setProfileName("profile 2");
        profileData2.setAverage(20D);
        profileData2.setCount(7L);
        profileData2.setMax(19L);
        profileData2.setMin(0L);
        profileData2.setSum(60L);

        ProfileReportData profileData3 = new ProfileReportData();
        profileData3.setProfileName("profile 1");
        profileData3.setAverage(10D);
        profileData3.setCount(10L);
        profileData3.setMax(20L);
        profileData3.setMin(1L);
        profileData3.setSum(50L);

        ProfileReportData profileData4 = new ProfileReportData();
        profileData4.setProfileName("profile 2");
        profileData4.setAverage(20D);
        profileData4.setCount(7L);
        profileData4.setMax(19L);
        profileData4.setMin(0L);
        profileData4.setSum(60L);

        GroupedFieldItem groupedFieldItem1 = new GroupedFieldItem();
        groupedFieldItem1.setValue("fmt/101");
        groupedFieldItem1.addProfileData(profileData1);
        groupedFieldItem1.addProfileData(profileData2);
        
        GroupedFieldItem groupedFieldItem2 = new GroupedFieldItem();
        groupedFieldItem2.setValue("fmt/102");
        groupedFieldItem2.addProfileData(profileData3);
        groupedFieldItem2.addProfileData(profileData4);

        reportItem.addGroupedFieldItem(groupedFieldItem1);
        reportItem.addGroupedFieldItem(groupedFieldItem2);
        
        StringWriter out = new StringWriter();

        report.addItem(reportItem);
        
        writer.writeReport(report, out);
        
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n"
            +   "<Report>\r\n"
            +  "    <Profiles/>"
            +  "    <ReportItems>\r\n"
            +  "        <ReportItem>\r\n"
            +  "            <Specification>\r\n"
            +  "                <Field>FILE_SIZE</Field>\r\n"
            +  "                <GroupByField>PUID</GroupByField>\r\n"
            +  "            </Specification>\r\n"
            +  "            <Groups>\r\n"
            +  "                <Group>\r\n"
            +  "                    <Value>fmt/101</Value>\r\n"
            +  "                    <ProfileSummaries>\r\n"
            +  "                        <ProfileSummary>\r\n"
            +  "                            <Count>10</Count>\r\n"
            +  "                            <Sum>50</Sum>\r\n"
            +  "                            <Min>1</Min>\r\n" 
            +  "                            <Max>20</Max>\r\n"
            +  "                            <Average>10.0</Average>\r\n"
            +  "                            <Name>profile 1</Name>\r\n"
            +  "                        </ProfileSummary>\r\n"
            +  "                        <ProfileSummary>\r\n"
            +  "                            <Count>7</Count>\r\n"
            +  "                            <Sum>60</Sum>\r\n"
            +  "                            <Min>0</Min>\r\n" 
            +  "                            <Max>19</Max>\r\n"
            +  "                            <Average>20.0</Average>\r\n"
            +  "                            <Name>profile 2</Name>\r\n"
            +  "                        </ProfileSummary>\r\n"
            +  "                    </ProfileSummaries>\r\n"
            +  "                    <GroupAggregateSummary>\r\n"
            +  "                        <Count>17</Count>\r\n"
            +  "                        <Sum>110</Sum>\r\n"
            +  "                        <Min>0</Min>\r\n" 
            +  "                        <Max>20</Max>\r\n"
            +  "                        <Average>14.117647058823529</Average>\r\n"
            +  "                    </GroupAggregateSummary>\r\n"
            +  "                </Group>\r\n"
            +  "                <Group>\r\n"
            +  "                    <Value>fmt/102</Value>\r\n"
            +  "                    <ProfileSummaries>\r\n"
            +  "                        <ProfileSummary>\r\n"
            +  "                            <Count>10</Count>\r\n"
            +  "                            <Sum>50</Sum>\r\n"
            +  "                            <Min>1</Min>\r\n" 
            +  "                            <Max>20</Max>\r\n"
            +  "                            <Average>10.0</Average>\r\n"
            +  "                            <Name>profile 1</Name>\r\n"
            +  "                        </ProfileSummary>\r\n"
            +  "                        <ProfileSummary>\r\n"
            +  "                            <Count>7</Count>\r\n"
            +  "                            <Sum>60</Sum>\r\n"
            +  "                            <Min>0</Min>\r\n" 
            +  "                            <Max>19</Max>\r\n"
            +  "                            <Average>20.0</Average>\r\n"
            +  "                            <Name>profile 2</Name>\r\n"
            +  "                        </ProfileSummary>\r\n"
            +  "                    </ProfileSummaries>\r\n"
            +  "                    <GroupAggregateSummary>\r\n"
            +  "                        <Count>17</Count>\r\n"
            +  "                        <Sum>110</Sum>\r\n"
            +  "                        <Min>0</Min>\r\n" 
            +  "                        <Max>20</Max>\r\n"
            +  "                        <Average>14.117647058823529</Average>\r\n"
            +  "                    </GroupAggregateSummary>\r\n"
            +  "                </Group>\r\n"
            +  "            </Groups>\r\n"
            +  "            <ReportItemAggregateSummary>\r\n"
            +  "                <Count>34</Count>\r\n"
            +  "                <Sum>220</Sum>\r\n"
            +  "                <Min>0</Min>\r\n"
            +  "                <Max>20</Max>\r\n"
            +  "                <Average>14.117647058823529</Average>\r\n"
            +  "            </ReportItemAggregateSummary>"
            +  "        </ReportItem>\r\n"
            +  "    </ReportItems>\r\n"
            +  "</Report>\r\n";
        
        
        XMLAssert.assertXMLEqual(control, out.getBuffer().toString());
        */
    }
}
