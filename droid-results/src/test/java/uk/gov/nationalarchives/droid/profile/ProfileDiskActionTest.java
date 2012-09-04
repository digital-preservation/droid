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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.io.FilenameUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rflitcroft
 *
 */
public class ProfileDiskActionTest {
	
	File profilesDir;
	File profileToSaveDir;
	File profileToLoadDir;
	File profileXml;
	File dbDir;
	File serviceProperties;
	File file2;
	File file3;
	File file4;
	File tmpDir;
	File destination;
		
	@Before
	public void setUp() throws Exception {
		profilesDir = new File("profiles");
		profilesDir.mkdir();
		profileToSaveDir = new File(profilesDir, "profileToSave");
		profileToSaveDir.mkdir();
		profileToLoadDir = new File(profilesDir, "myProfile");
		profileXml = new File(profileToSaveDir, "profile.xml");
		profileXml.createNewFile();
		writeXmlData();
		dbDir = new File(profileToSaveDir, "db");
        dbDir.mkdir();
        serviceProperties = new File(dbDir, "service.properties");
        serviceProperties.createNewFile();
        file2 = new File(dbDir,"file2");
        file2.createNewFile();
        file3 = new File(dbDir, "file3");
        file3.createNewFile();
        file4 = new File(dbDir, "file4");
        file4.createNewFile();
        tmpDir = new File("tmp");
        tmpDir.mkdir();
        destination = new File(tmpDir, "saved.drd");
	}

	@Test
    public void testSaveProfileToFile() throws Exception {
        ProgressObserver callback = mock(ProgressObserver.class);
        ProfileDiskAction action = new ProfileDiskAction();
        action.saveProfile("profiles/" + "profileToSave", destination, callback);
        //assertTrue(destination.exists());

        /* check the destination is a zip file with the following entries:
         * profile.xml file
         * db - directory (not empty)
         */
        ZipFile savedFile = new ZipFile(destination);

        Enumeration<? extends ZipEntry> entries = savedFile.entries();
        List<String> entryNames = new ArrayList<String>();
        while (entries.hasMoreElements()) {
            entryNames.add(FilenameUtils.separatorsToUnix(entries.nextElement().getName()));
        }
        assertEquals(true, entryNames.contains("db/file2"));
        assertEquals(true, entryNames.contains("db/file3"));
        assertEquals(true, entryNames.contains("db/file4"));
        assertEquals(true, entryNames.contains("db/service.properties"));
        assertEquals(true, entryNames.contains("profile.xml"));

        // uncompress the profiles.xml entry to make sure it was zipped OK.
        ZipFile saved = new ZipFile(destination);
        InputStream in = saved.getInputStream(new ZipEntry("profile.xml"));

        StringBuilder sb = new StringBuilder();
        int bytesIn = 0;
        byte[] readBuffer = new byte[20];
        while ((bytesIn = in.read(readBuffer)) != -1) {
            sb.append(new String(readBuffer, 0, bytesIn));
        }
        in.close();

        assertEquals(getXmlString(), sb.toString());
        
        verify(callback, atLeastOnce()).onProgress(argThat(Matchers.lessThanOrEqualTo(100)));
    }

    @Test
    public void testLoadProfileFromFile() throws Exception {

        File source = new File("test-profiles/saved.drd");
        assertTrue(source.exists());

        //File destination = new File(profilesDir, "myProfile");
        //FileUtils.deleteQuietly(destination);

        ProgressObserver observer = mock(ProgressObserver.class);

        ProfileDiskAction profileDiskAction = new ProfileDiskAction();
        profileDiskAction.load(source, profileToLoadDir, observer);

        assertTrue(profileToLoadDir.isDirectory());
        assertTrue(new File(profileToLoadDir + "/profile.xml").isFile());
        assertTrue(new File(profileToLoadDir + "/db").isDirectory());
        assertTrue(new File(profileToLoadDir + "/db/file2").isFile());
        assertTrue(new File(profileToLoadDir + "/db/file3").isFile());
        assertTrue(new File(profileToLoadDir + "/db/file4").isFile());

        // check that profiles.xml was unzipped OK
        InputStream in = new FileInputStream("profiles/myProfile/profile.xml");

        StringBuilder sb = new StringBuilder();
        int bytesIn = 0;
        byte[] readBuffer = new byte[20];
        while ((bytesIn = in.read(readBuffer)) != -1) {
            sb.append(new String(readBuffer, 0, bytesIn));
        }
        in.close();

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            + "<profiles/>";
        assertEquals(xml, sb.toString());

        verify(observer, atLeastOnce()).onProgress(argThat(Matchers.lessThanOrEqualTo(100)));
        //verify(observer).onProgress(100);
    }
    
    private void writeXmlData() throws Exception {
        FileWriter out = new FileWriter(profileXml);
        out.write(getXmlString());
        out.close();
    }
    
    private String getXmlString() {
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                + "<profiles/>";
    }
    
    @After
    public void tearDown() {
    	if(destination.exists()) {
    		destination.deleteOnExit();
    	}
    	if(tmpDir.exists()) {
    		tmpDir.deleteOnExit();
    	}
    	if(file2.exists()){
    		file2.delete();
    	}
    	if(file3.exists()) {
    		file3.delete();
    	}
    	if(file4.exists()) {
    		file4.delete();
    	}
    	if(serviceProperties.exists()) {
    		serviceProperties.delete();
    	}
    	if(dbDir.exists()) {
    		dbDir.delete();
    	}
    	if(profileXml.exists()) {
    		profileXml.delete();
    	}
    	if(profileToSaveDir.exists()) {
    		profileToSaveDir.delete();
    	}
    	if(profileToLoadDir.exists()) {
    		profileToLoadDir.delete();
    	}
    	if(profilesDir.exists()) {
    		profilesDir.delete();
    	}
    }

}