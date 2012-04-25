/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.profile.DirectoryProfileResource;
import uk.gov.nationalarchives.droid.submitter.FileWalker.ProgressEntry;

/**
 * @author rflitcroft
 *
 */
public class FileWalkerPersistenceTest {

    private ProfileWalkerDao profileWalkerDao;
    private File testDir;
    
    @Before
    public void setup() throws JAXBException {
        XMLUnit.setIgnoreWhitespace(true);
        
        profileWalkerDao = new ProfileWalkerDao();
        testDir = new File("tmp/" + getClass().getSimpleName());
        testDir.mkdirs();
        profileWalkerDao.setProfileHomeDir(testDir.getPath());
    }
    
    @Test
    public void testSaveProfileWithSerializedPofileSpecWalker() throws Exception {
        
        final File dirResource1 = new File("root/dir");
        final File dirResource2 = new File("root/dir/subDir");
        final File dirResource3 = new File("root/dir/subdir1/subDir2");
        
        Deque<ProgressEntry> progress = new ArrayDeque<ProgressEntry>();
        
        final File root = new File("root");
        FileWalker filewalker = new FileWalker(root.toURI(), true);
        progress.push(new ProgressEntry(dirResource1.toURI(), 1, "X", null));
        progress.push(new ProgressEntry(dirResource2.toURI(), 2, "Y", null));
        progress.push(new ProgressEntry(dirResource3.toURI(), 3, "Z", null));
        
        filewalker.setProgress(progress);
        
        ProfileWalkState state = new ProfileWalkState();
        state.setCurrentFileWalker(filewalker);
        state.setCurrentResource(new DirectoryProfileResource(root, true));
        
        profileWalkerDao.save(state);
        
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" 
            + "<ProfileWalk Status=\"NOT_STARTED\">" 
            + "    <Dir Recursive=\"true\">" 
            + "        <Size>0</Size>" 
            + "        <LastModifiedDate>1970-01-01T01:00:00+01:00</LastModifiedDate>" 
            + "        <Extension></Extension>" 
            + "        <Name>root</Name>" 
            + "        <Uri>" + root.toURI() + "</Uri>" 
            + "        <Path>" + getPath(root) + "</Path>"
            + "    </Dir>" 
            + "    <FileWalker Recursive=\"true\">" 
            + "        <RootUri>" + root.toURI() + "</RootUri>" 
            + "        <Progress>"
            + "            <ProgressEntry Id=\"3\" Prefix=\"Z\">" 
            + "                <Uri>" + dirResource3.toURI() + "</Uri>" 
            + "            </ProgressEntry>"
            + "            <ProgressEntry Id=\"2\" Prefix=\"Y\">" 
            + "                <Uri>" + dirResource2.toURI() + "</Uri>" 
            + "            </ProgressEntry>"
            + "            <ProgressEntry Id=\"1\" Prefix=\"X\">" 
            + "                <Uri>" + dirResource1.toURI() + "</Uri>" 
            + "            </ProgressEntry>"
            + "        </Progress>"
            + "    </FileWalker>" 
            + "</ProfileWalk>"; 

        XMLAssert.assertXMLEqual(new StringReader(control), 
                new FileReader(new File(testDir, "profile_progress.xml")));

    }

    public String getPath(File file) {
        String location = file.toURI().toString();
        String decodedLocation = java.net.URLDecoder.decode(location);
        int uriPrefix = decodedLocation.indexOf(":/");
        return decodedLocation.substring(uriPrefix + 2);
    }    
    
}
