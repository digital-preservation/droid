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
package uk.gov.nationalarchives.droid.submitter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.profile.DirectoryProfileResource;
import uk.gov.nationalarchives.droid.submitter.FileWalker.ProgressEntry;

/**
 * @author rflitcroft, boreilly
 * BNO: String control amended as follows after
 * JAXB changes to reduce littering
 * - Added </children> empty elements in <ProgressEntry>
 * - Moved <RootUri> element to after <Progress>
 */
public class FileWalkerPersistenceTest {

    private ProfileWalkerDao profileWalkerDao;
    private Path testDir;
    
    @Before
    public void setup() throws JAXBException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        
        profileWalkerDao = new ProfileWalkerDao();
        testDir = Paths.get("target/tmp/" + getClass().getSimpleName());
        Files.createDirectories(testDir);
        profileWalkerDao.setProfileHomeDir(testDir);
    }
    
    @Test
    public void testSaveProfileWithSerializedPofileSpecWalker() throws Exception {
        
        final Path dirResource1 = Paths.get("root/dir");
        final Path dirResource2 = Paths.get("root/dir/subDir");
        final Path dirResource3 = Paths.get("root/dir/subdir1/subDir2");
        
        final Deque<ProgressEntry> progress = new ArrayDeque<ProgressEntry>();
        
        final Path root = Paths.get("root");
        FileWalker filewalker = new FileWalker(root.toUri(), true);
        progress.push(new ProgressEntry(dirResource1, 1, "X", null));
        progress.push(new ProgressEntry(dirResource2, 2, "Y", null));
        progress.push(new ProgressEntry(dirResource3, 3, "Z", null));
        
        filewalker.setProgress(progress);
        
        ProfileWalkState state = new ProfileWalkState();
        state.setCurrentFileWalker(filewalker);
        state.setCurrentResource(new DirectoryProfileResource(root, true));
        
        profileWalkerDao.save(state);
        
        DateTime testDateTime = new DateTime(0L);
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
        String control = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<ProfileWalk Status=\"NOT_STARTED\">\n"
            + "    <Dir Recursive=\"true\">\n"
            + "        <Size>-1</Size>\n"
            + "        <LastModifiedDate>" + formatter.print(testDateTime) + "</LastModifiedDate>\n"
            + "        <Extension></Extension>\n"
            + "        <Name>root</Name>\n"
            + "        <Uri>" + root.toUri() + "</Uri>\n"
            + "        <Path>" + getPath(root) + "</Path>\n"
            + "    </Dir>\n"
            + "    <FileWalker Recursive=\"true\">\n"
            + "        <Progress>\n"
            + "            <ProgressEntry Id=\"3\" Prefix=\"Z\">\n"
            + "                <Children/>\n"
            + "                <Uri>" + dirResource3.toUri() + "</Uri>\n"
            + "            </ProgressEntry>\n"
            + "            <ProgressEntry Id=\"2\" Prefix=\"Y\">\n"
            + "                <Children/>\n"
            + "                <Uri>" + dirResource2.toUri() + "</Uri>\n"
            + "            </ProgressEntry>\n"
            + "            <ProgressEntry Id=\"1\" Prefix=\"X\">\n"
            + "                <Children/>\n"
            + "                <Uri>" + dirResource1.toUri() + "</Uri>\n"
            + "            </ProgressEntry>\n"
            + "        </Progress>\n"
            + "        <RootUri>" + root.toUri() + "</RootUri>\n"
            + "    </FileWalker>\n"
            + "</ProfileWalk>"; 

        System.out.println("Outputting profile_progress.xml");
        for(final String line : Files.readAllLines(testDir.resolve("profile_progress.xml"), UTF_8)) {
            System.out.println(line);
        }

        System.out.println(control);

        try(final Reader reader = Files.newBufferedReader(testDir.resolve("profile_progress.xml"), UTF_8)) {
            final Diff diff = new Diff(new StringReader(control), reader);
            assertTrue(diff.similar());
        }
    }

    public String getPath(final Path file) {
        final String location = file.toUri().toString();
        final String decodedLocation = java.net.URLDecoder.decode(location);
        final int uriPrefix = decodedLocation.indexOf(":/");
        return decodedLocation.substring(uriPrefix + 2);
    }    
    
}
