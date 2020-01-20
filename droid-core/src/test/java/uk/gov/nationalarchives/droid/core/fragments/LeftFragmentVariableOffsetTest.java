/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.core.fragments;

/**
 * Created by boreilly on 09/08/2016.
 */
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


public class LeftFragmentVariableOffsetTest {

    private final String TESTAREA = "test_sig_files/";

    /*
    ** In this test, the sequence is defined with a variable offset range from BOF, and there are 2 left fragments.
    * The final fragment occurs twice - the first occurrence is valid relative to the offset from the previous fragment,
    * but is beyond the maximum offset for the sequence as a whole.  The second occurrence, however is valid in terms
    * of both the offset from the previous fragment and within the permitted range for the sequence as a whole.
    * N.B. This test fails in previous versions of DROID
 */
    @Test
    public void testFinalRecurringLeftFragmentWithVariableSeqOffsetFix() throws Exception {

        final String sigFile = "left-frag-1200.xml";
        final String fileToScan = "left1200.ext";
        final String expectedPuid = "dev/1200";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        final Path file = Paths.get(TESTAREA, fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(
                Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<Path> request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(file);

        IdentificationResultCollection resultsCollection = droid.matchBinarySignatures(request);
        List<IdentificationResult> results = resultsCollection.getResults();

        assertEquals(1, results.size());

        Iterator<IdentificationResult> iter = results.iterator();
        while (iter.hasNext()) {
            IdentificationResult result = iter.next();
            assertEquals(expectedPuid, result.getPuid());
        }
    }

    /*
    ** Similar to above - except that there is no second occurrence of the final left fragment - and
    ** the sole occurrence of this fragment is beyond the maximum offset for the sequence as a whole
    ** from BOF. N.B. This test passes in the previous version DROID 6.2.1
     */
    @Test
    public void testFinalLeftFragmentBeyondSeqMaxOffset() throws Exception {

        final String sigFile = "left-frag-1200.xml";
        final String fileToScan = "left1200a.ext";
        final String expectedPuid = "dev/1200";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        final Path file = Paths.get(TESTAREA, fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<Path> request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(file);

        IdentificationResultCollection resultsCollection = droid.matchBinarySignatures(request);
        List<IdentificationResult> results = resultsCollection.getResults();

        assertEquals(0, results.size());

        Iterator<IdentificationResult> iter = results.iterator();
        while (iter.hasNext()) {
            IdentificationResult result = iter.next();
            assertEquals(expectedPuid, result.getPuid());
        }
    }

    /*
    ** In this test, there is a sub sequence with 2 left fragments.  The first fragment
    ** is at a variable offset to the main subsequence, and the second fragment is then at
    ** a fixed offset to the first fragment.  In the test file, the first fragment occurs
    ** twice within its permitted offset range.  The second fragment will be a valid match
    ** only when assessed using offsets relative to the second occurrence of the first fragment.
    ** Sequence is defined relative to BOF (End of file).
    *** N.B. This test fails in previous versions of DROID
     */
    @Test
    public void testRecurringIntermediateLeftFragmentWithVariableBofOffsetFix() throws Exception {

        final String sigFile = "left-frag-1201.xml";
        final String fileToScan = "left1201.ext";
        final String expectedPuid = "dev/1201";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        final Path file = Paths.get(TESTAREA, fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(
                Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<Path> request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(file);

        IdentificationResultCollection resultsCollection = droid.matchBinarySignatures(request);
        List<IdentificationResult> results = resultsCollection.getResults();

        assertEquals(1, results.size());

        Iterator<IdentificationResult> iter = results.iterator();
        while (iter.hasNext()) {
            IdentificationResult result = iter.next();
            assertEquals(expectedPuid, result.getPuid());
        }
    }

    /*
    ** As above, except the sequence is defined from EOF instead of BOF
    * N.B. This test fails in previous versions of DROID
     */
    @Test
    public void testRecurringIntermediateLeftFragmentWithVariableEofOffsetFix() throws Exception {

        final String sigFile = "left-frag-1201-EOF.xml";
        final String fileToScan = "left1201.ext";
        final String expectedPuid = "dev/1201";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        final Path file = Paths.get(TESTAREA, fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(
                Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<Path> request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(file);

        IdentificationResultCollection resultsCollection = droid.matchBinarySignatures(request);
        List<IdentificationResult> results = resultsCollection.getResults();

        assertEquals(1, results.size());

        Iterator<IdentificationResult> iter = results.iterator();
        while (iter.hasNext()) {
            IdentificationResult result = iter.next();
            assertEquals(expectedPuid, result.getPuid());
        }
    }

}
