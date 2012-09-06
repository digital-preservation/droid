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
package uk.gov.nationalarchives.droid.profile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterValue;

/**
 * @author rflitcroft
 * 
 */
public class ProfileSpecToXmlPersistenceTest {
	
	File tmpDir;
	File profileFile;

    private JaxbProfileSpecDao profileSpecJaxbDao;

    @BeforeClass
    public static void init() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Before
    public void setup() throws JAXBException {
        profileSpecJaxbDao = new JaxbProfileSpecDao();
        new File("profiles/untitled-1").mkdirs();
        tmpDir = new File("tmp");
        tmpDir.mkdir();
        profileFile = new File(tmpDir, "profile.xml");
    }

    @After
    public void tearDown() throws IOException {
       FileUtils.deleteDirectory(new File("profiles/"));
       if(profileFile.exists()) {
    	   profileFile.deleteOnExit();
       }
       if(tmpDir.exists()) {
    	   tmpDir.deleteOnExit();
       }
    }
    
    @Test
    public void testSaveEmptyProfileSpecAsXml() throws Exception {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        File file = new File("profiles/untitled-1/profile.xml");
        FileUtils.deleteQuietly(file);

        ProfileSpec profileSpec = new ProfileSpec();

        ProfileInstance profile = new ProfileInstance(ProfileState.INITIALISING);
        profile.changeState(ProfileState.STOPPED);
        profile.setUuid("untitled-1");
        profile.setProfileSpec(profileSpec);
        profile.setDateCreated(df.parse("2009-01-01 12:00:00"));
        profile.setSignatureFileVersion(26);

        profileSpecJaxbDao.saveProfile(profile, new File("profiles/untitled-1"));

        String control = "<Profile Id=\"untitled-1\">"
                + "  <CreatedDate>2009-01-01T00:00:00Z</CreatedDate>"
                + "  <State>STOPPED</State>"
                + "  <Throttle>0</Throttle>"
                + "  <SignatureFileVersion>26</SignatureFileVersion>"
                + "  <ProfileSpec>"
                + "    <Resources/>" 
                + "  </ProfileSpec>" 
                + "</Profile>";

        Reader test = new FileReader(file);
        XMLAssert.assertXMLEqual(new StringReader(control), test);

    }

    @Test
    public void testSaveProfileSpecWithSomeResources() throws Exception {

        File file = new File("profiles/untitled-1/profile.xml");
        FileUtils.deleteQuietly(file);

        File resource1 = new File("file/1");
        File resource2 = new File("file/2");
        File resource3 = new File("dir/1");
        File resource4 = new File("dir/2");

        ProfileSpec profileSpec = new ProfileSpec();
        profileSpec.addResource(new FileProfileResource(resource1));
        profileSpec.addResource(new FileProfileResource(resource2));
        profileSpec.addResource(new DirectoryProfileResource(resource3, false));
        profileSpec.addResource(new DirectoryProfileResource(resource4, true));

        ProfileInstance profile = new ProfileInstance(ProfileState.INITIALISING);
        profile.changeState(ProfileState.STOPPED);
        profile.setSignatureFileVersion(26);
        profile.setUuid("untitled-1");
        profile.setProfileSpec(profileSpec);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        profile.setDateCreated(df.parse("2009-01-01 12:00:00"));

        profileSpecJaxbDao.saveProfile(profile, new File("profiles/untitled-1"));

        DateTime testDateTime = new DateTime(0L);
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
        String control = "<Profile Id=\"untitled-1\">"
                + "  <CreatedDate>2009-01-01T00:00:00Z</CreatedDate>"
                + "  <ProfileSpec>"
                + "    <Resources>"
                + "     <File>"
                + "      <Size>0</Size>"
                + "      <LastModifiedDate>" + formatter.print(testDateTime) + "</LastModifiedDate>"
                + "      <Extension></Extension>"
                + "      <Name>1</Name>"
                + "      <Uri>"
                + resource1.toURI()
                + "</Uri>"
                + "      <Path>"
                + getPath(resource1)
                + "     </Path>"
                + "     </File>"
                + "     <File>"
                + "      <Size>0</Size>"
                + "      <LastModifiedDate>" + formatter.print(testDateTime) + "</LastModifiedDate>"
                + "      <Extension></Extension>"
                + "      <Name>2</Name>"
                + "      <Uri>"
                + resource2.toURI()
                + "</Uri>"
                + "      <Path>"
                + getPath(resource2)
                + "     </Path>"
                + "     </File>"
                + "     <Dir Recursive=\"false\">"
                + "      <Size>0</Size>"
                + "      <LastModifiedDate>" + formatter.print(testDateTime) + "</LastModifiedDate>"
                + "      <Extension></Extension>"
                + "      <Name>1</Name>"
                + "      <Uri>"
                + resource3.toURI()
                + "</Uri>"
                + "      <Path>"
                + getPath(resource3)
                + "     </Path>"
                + "     </Dir>"
                + "     <Dir Recursive=\"true\">"
                + "      <Size>0</Size>"
                + "      <LastModifiedDate>" + formatter.print(testDateTime) + "</LastModifiedDate>"
                + "      <Extension></Extension>"
                + "      <Name>2</Name>"
                + "      <Uri>"
                + resource4.toURI()
                + "</Uri>"
                + "      <Path>"
                + getPath(resource4)
                + "     </Path>"
                + "     </Dir>"
                + "    </Resources>"
                + "  </ProfileSpec>"
                + "  <State>STOPPED</State>"
                + "  <SignatureFileVersion>26</SignatureFileVersion>"
                + "  <Throttle>0</Throttle>"
                + "</Profile>";

        Reader test = new FileReader(file);
        Diff diff = new Diff(new StringReader(control), test);
        assertTrue(diff.similar());
    }

    public String getPath(File file) {
        String location = file.toURI().toString();
        String decodedLocation = java.net.URLDecoder.decode(location);
        int uriPrefix = decodedLocation.indexOf(":/");
        return decodedLocation.substring(uriPrefix + 2);
    }
    
    @Test
    public void testXmlToProfileSpec() throws Exception {

        File resource1 = new File("file/1");
        File resource2 = new File("file/2");
        File resource3 = new File("dir/1");
        File resource4 = new File("dir/2");

        BufferedWriter writer = new BufferedWriter(new FileWriter(
                "profiles/untitled-1/profile.xml"));
        writer.append("<Profile>");
        writer.newLine();
        writer.append("  <CreatedDate>2009-01-01T00:00:00Z</CreatedDate>");
        writer.newLine();
        writer.append("  <Location>untitled-1</Location>");
        writer.newLine();
        writer.append("  <State>STOPPED</State>");
        writer.newLine();
        writer.append("  <Throttle>120</Throttle>");
        writer.newLine();
        writer.append("  <SignatureFileVersion>26</SignatureFileVersion>");
        writer.newLine();
        writer.append("  <ProfileSpec>");
        writer.newLine();
        writer.append("    <Name>untitled-1</Name>");
        writer.newLine();
        writer.append("    <Resources>");
        writer.newLine();
        writer.append("     <File>");
        writer.newLine();
        writer.append("      <Uri>" + resource1.toURI() + "</Uri>");
        writer.newLine();
        writer.append("      <Size></Size>");
        writer.newLine();
        writer.append("      <LastModifiedDate>1979-01-01-T01:00:00:00+01:00</LastModifiedDate>");
        writer.newLine();
        writer.append("      <Extension></Extension>");
        writer.newLine();
        writer.append("      <Name></Name>");
        writer.newLine();
        writer.append("     </File>");
        writer.newLine();
        writer.append("     <File>");
        writer.newLine();
        writer.append("      <Uri>" + resource2.toURI() + "</Uri>");
        writer.newLine();
        writer.append("      <Size></Size>");
        writer.newLine();
        writer.append("      <LastModifiedDate>1979-01-01-T01:00:00:00+01:00</LastModifiedDate>");
        writer.newLine();
        writer.append("      <Extension></Extension>");
        writer.newLine();
        writer.append("      <Name></Name>");
        writer.newLine();
        writer.append("     </File>");
        writer.newLine();
        writer.append("     <Dir Recursive=\"false\">");
        writer.newLine();
        writer.append("      <Uri>" + resource3.toURI() + "</Uri>");
        writer.newLine();
        writer.append("      <Size></Size>");
        writer.newLine();
        writer.append("      <LastModifiedDate>1979-01-01-T01:00:00:00+01:00</LastModifiedDate>");
        writer.newLine();
        writer.append("      <Extension></Extension>");
        writer.newLine();
        writer.append("      <Name></Name>");
        writer.newLine();
        writer.append("     </Dir>");
        writer.newLine();
        writer.append("     <Dir Recursive=\"true\">");
        writer.newLine();
        writer.append("      <Uri>" + resource4.toURI() + "</Uri>");
        writer.newLine();
        writer.append("      <Size></Size>");
        writer.newLine();
        writer.append("      <LastModifiedDate>1979-01-01-T01:00:00:00+01:00</LastModifiedDate>");
        writer.newLine();
        writer.append("      <Extension></Extension>");
        writer.newLine();
        writer.append("      <Name></Name>");
        writer.newLine();
        writer.append("     </Dir>");
        writer.newLine();
        writer.append("    </Resources>");
        writer.newLine();
        writer.append("  </ProfileSpec>");
        writer.append("</Profile>");
        writer.newLine();
        writer.close();

        ProfileInstance profile = profileSpecJaxbDao
                .loadProfile(new FileInputStream(
                        "profiles/untitled-1/profile.xml"));

        assertEquals("STOPPED", profile.getState().name());
        assertEquals(120, profile.getThrottle());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        assertEquals(df.parse("2009-01-01 00:00:00"), profile.getDateCreated());

        ProfileSpec profileSpec = profile.getProfileSpec();

        assertEquals(26, profile.getSignatureFileVersion().intValue());

        List<AbstractProfileResource> resources = profileSpec.getResources();
        Iterator<AbstractProfileResource> resourceIterator = resources
                .iterator();
        assertEquals(4, resources.size());
        assertEquals(new FileProfileResource(resource1), resourceIterator
                .next());
        assertEquals(new FileProfileResource(resource2), resourceIterator
                .next());
        assertEquals(new DirectoryProfileResource(resource3, false),
                resourceIterator.next());
        assertEquals(new DirectoryProfileResource(resource4, true),
                resourceIterator.next());

    }
    
    @Test
    public void testSaveProfileSpecWithFilter() throws Exception {
        
        FilterCriterionImpl criterion = new FilterCriterionImpl();
        criterion.setField(CriterionFieldEnum.PUID);
        criterion.setOperator(CriterionOperator.ANY_OF);
        criterion.setSelectedValues(new ArrayList<FilterValue>());
        criterion.addSelectedValue(new FilterValue(1, "puid", "fmt/101"));
        
        FilterImpl filter = new FilterImpl();
        filter.setEnabled(true);
        filter.addFilterCiterion(criterion, 0);
        
        ProfileInstance profile = new ProfileInstance(ProfileState.INITIALISING);
        profile.changeState(ProfileState.STOPPED);
        profile.setFilter(filter);
        
        profileSpecJaxbDao.saveProfile(profile, tmpDir);
        
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" 
            + "<Profile>" 
            + "    <CreatedDate>" 
            +          ISODateTimeFormat.dateTime().print(profile.getDateCreated().getTime()) 
            + "    </CreatedDate>" 
            + "    <State>STOPPED</State>"
            + "    <Filter>"
            + "        <Enabled>true</Enabled>" 
            + "        <Narrowed>false</Narrowed>"
            + "        <Criteria>"
            + "            <FieldName>PUID</FieldName>"
            + "            <Operator>ANY_OF</Operator>"
            + "            <Parameter>"
            + "                <Id>1</Id>"
            + "                <Description>puid</Description>"
            + "                <Value>fmt/101</Value>"
            + "            </Parameter>"
            + "            <RowNumber>0</RowNumber>"
            + "         </Criteria>"
            + "    </Filter>"
            + "    <Throttle>0</Throttle>"
            + "</Profile>";
        
        XMLAssert.assertXMLEqual(new StringReader(control), new FileReader("tmp/profile.xml"));
    }
    
}
