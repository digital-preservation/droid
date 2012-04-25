/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rflitcroft
 *
 */
public class ProfileDiskActionTest {

    @Test
    public void testSaveProfileToFile() throws Exception {

        FileUtils.deleteQuietly(new File("profiles/profileToSave"));
        // create a file system looking like the one we want to save.
        new File("profiles/profileToSave").mkdirs();
        File profilesXml = new File("profiles/profileToSave/profile.xml");
        profilesXml.createNewFile();

        // write some xml to the profiles file...
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            + "<profiles/>";

        FileWriter out = new FileWriter(profilesXml);
        out.write(xml);
        out.close();

        new File("profiles/profileToSave/db").mkdir();
        new File("profiles/profileToSave/db/service.properties").createNewFile();
        new File("profiles/profileToSave/db/file2").createNewFile();
        new File("profiles/profileToSave/db/file3").createNewFile();
        new File("profiles/profileToSave/db/file4").createNewFile();

        new File("tmp").mkdir();
        String destinationFilename = "tmp/saved.drd";

        File destination = new File(destinationFilename);
        destination.delete();
        
        ProgressObserver callback = mock(ProgressObserver.class);

        ProfileDiskAction action = new ProfileDiskAction();
        action.saveProfile("profiles/" + "profileToSave", destination, callback);
        assertTrue(destination.exists());

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

        assertEquals(xml, sb.toString());
        
        verify(callback, atLeastOnce()).onProgress(argThat(Matchers.lessThanOrEqualTo(100)));
        //verify(callback, atLeastOnce()).onProgress(100);

    }

    @Test
    public void testLoadProfileFromFile() throws Exception {

        File source = new File("test-profiles/saved.drd");
        assertTrue(source.exists());

        File destination = new File("profiles/myProfile");
        FileUtils.deleteQuietly(destination);

        ProgressObserver observer = mock(ProgressObserver.class);

        ProfileDiskAction profileDiskAction = new ProfileDiskAction();
        profileDiskAction.load(source, destination, observer);

        assertTrue(destination.isDirectory());
        assertTrue(new File(destination + "/profile.xml").isFile());
        assertTrue(new File(destination + "/db").isDirectory());
        assertTrue(new File(destination + "/db/file2").isFile());
        assertTrue(new File(destination + "/db/file3").isFile());
        assertTrue(new File(destination + "/db/file4").isFile());

        // check that profiles.xml was unzipped OK
        InputStream in = new FileInputStream("profiles/myProfile/profile.xml");

        StringBuilder sb = new StringBuilder();
        int bytesIn = 0;
        byte[] readBuffer = new byte[20];
        while ((bytesIn = in.read(readBuffer)) != -1) {
            sb.append(new String(readBuffer, 0, bytesIn));
        }

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            + "<profiles/>";
        assertEquals(xml, sb.toString());

        verify(observer, atLeastOnce()).onProgress(argThat(Matchers.lessThanOrEqualTo(100)));
        //verify(observer).onProgress(100);
    }
}
