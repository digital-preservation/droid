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
