/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;


import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 *
 */
public class ProfileDiskActionTest {
	
	Path profilesDir;
    Path profileToSaveDir;
    Path profileToLoadDir;
    Path profileXml;
    Path dbDir;
    Path serviceProperties;
    Path file2;
    Path file3;
    Path file4;
    Path tmpDir;
    Path destination;
		
	@Before
	public void setUp() throws Exception {
		profilesDir = Paths.get("target/profiles");
        FileUtil.mkdirsQuietly(profilesDir);

		profileToSaveDir = profilesDir.resolve("profileToSave");
        FileUtil.mkdirsQuietly(profileToSaveDir);

		profileToLoadDir = profilesDir.resolve("myProfile");

		profileXml = profileToSaveDir.resolve("profile.xml");
        Files.createFile(profileXml);
		writeXmlData();

		dbDir = profileToSaveDir.resolve("db");
        FileUtil.mkdirsQuietly(dbDir);

        serviceProperties = dbDir.resolve("service.properties");
        Files.createFile(serviceProperties);

        file2 = dbDir.resolve("file2");
        Files.createFile(file2);

        file3 = dbDir.resolve("file3");
        Files.createFile(file3);

        file4 = dbDir.resolve("file4");
        Files.createFile(file4);

        tmpDir = Paths.get("tmp");
        FileUtil.mkdirsQuietly(tmpDir);

        destination = tmpDir.resolve("saved.drd");
	}

	@Test
    public void testSaveProfileToFile() throws Exception {
        ProgressObserver callback = mock(ProgressObserver.class);
        ProfileDiskAction action = new ProfileDiskAction();
        action.saveProfile(Paths.get("target/profiles", "profileToSave"), destination, callback);
        //assertTrue(destination.exists());

        /* check the destination is a zip file with the following entries:
         * profile.xml file
         * db - directory (not empty)
         */
        try(final ZipFile savedFile = new ZipFile(destination.toFile())) {

            final Enumeration<? extends ZipEntry> entries = savedFile.entries();
            final List<String> entryNames = new ArrayList<>();
            while (entries.hasMoreElements()) {
                entryNames.add(FilenameUtils.separatorsToUnix(entries.nextElement().getName()));
            }
            assertEquals(true, entryNames.contains("db/file2"));
            assertEquals(true, entryNames.contains("db/file3"));
            assertEquals(true, entryNames.contains("db/file4"));
            assertEquals(true, entryNames.contains("db/service.properties"));
            assertEquals(true, entryNames.contains("profile.xml"));

            // uncompress the profiles.xml entry to make sure it was zipped OK.
            try(final ZipFile saved = new ZipFile(destination.toFile());
                    final InputStream in = saved.getInputStream(new ZipEntry("profile.xml"))) {
                final StringBuilder sb = new StringBuilder();
                int bytesIn = 0;
                byte[] readBuffer = new byte[20];
                while ((bytesIn = in.read(readBuffer)) != -1) {
                    sb.append(new String(readBuffer, 0, bytesIn));
                }

                assertEquals(getXmlString(), sb.toString());
                verify(callback, atLeastOnce()).onProgress(AdditionalMatchers.leq(100));
            }
        }
    }

    @Test
    public void testLoadProfileFromFile() throws Exception {
        final Path source = Paths.get("test-profiles/saved.drd");
        assertTrue(Files.exists(source));

        //File destination = new File(profilesDir, "myProfile");
        //FileUtils.deleteQuietly(destination);

        ProgressObserver observer = mock(ProgressObserver.class);

        ProfileDiskAction profileDiskAction = new ProfileDiskAction();
        profileDiskAction.load(source, profileToLoadDir, observer);

        assertTrue(Files.isDirectory(profileToLoadDir));
        assertTrue(Files.isRegularFile(profileToLoadDir.resolve("profile.xml")));
        assertTrue(Files.isDirectory(profileToLoadDir.resolve("db")));
        assertTrue(Files.isRegularFile(profileToLoadDir.resolve("db/file2")));
        assertTrue(Files.isRegularFile(profileToLoadDir.resolve("db/file3")));
        assertTrue(Files.isRegularFile(profileToLoadDir.resolve("db/file4")));

        // check that profiles.xml was unzipped OK
        InputStream in = new FileInputStream("target/profiles/myProfile/profile.xml");

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

        verify(observer, atLeastOnce()).onProgress(AdditionalMatchers.leq(100));
        verify(observer, atLeastOnce()).onProgress(100);
    }
    
    private void writeXmlData() throws Exception {
        try(final Writer out = Files.newBufferedWriter(profileXml, UTF_8)) {
            out.write(getXmlString());
        }
    }
    
    private String getXmlString() {
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
                + "<profiles/>";
    }
    
    @After
    public void tearDown() throws IOException {
    	if(Files.exists(destination)) {
    		destination.toFile().deleteOnExit();
    	}
    	if(Files.exists(tmpDir)) {
    		tmpDir.toFile().deleteOnExit();
    	}
    	if(Files.exists(file2)){
            Files.delete(file2);
    	}
    	if(Files.exists(file3)) {
            Files.delete(file3);
    	}
    	if(Files.exists(file4)) {
            Files.delete(file4);
    	}
    	if(Files.exists(serviceProperties)) {
            Files.delete(serviceProperties);
    	}
    	if(Files.exists(dbDir)) {
    		dbDir.toFile().deleteOnExit();
    	}
    	if(Files.exists(profileXml)) {
    		Files.delete(profileXml);
    	}
    	if(Files.exists(profileToSaveDir)) {
    		profileToSaveDir.toFile().deleteOnExit();
    	}
    	if(Files.exists(profileToLoadDir)) {
    		profileToLoadDir.toFile().deleteOnExit();
    	}
    	if(Files.exists(profilesDir)) {
    		profilesDir.toFile().deleteOnExit();
    	}
    }

}