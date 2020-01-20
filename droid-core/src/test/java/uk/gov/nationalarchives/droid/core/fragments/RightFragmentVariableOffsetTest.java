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
 *
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


public class RightFragmentVariableOffsetTest {

    private final String TESTAREA = "test_sig_files/";

    /*
    ** In this test, there is a sub sequence with 2 right fragments.  The first fragment
    ** is at a variable offset to the main subsequence, and the second frgament is then at
    ** a fixed offset to the first fragment.  In the test file, the first fragment occurs
    ** twice within its permitted offset range.  The second fragment will be a valid match
    ** only when assessed using offsets relative to the second occurrence of the first fragment.
    ** Sequence is defined relative to EOF (End of file).
    **
     */
    @Test
    public void testRecurringIntermediateRightFragmentWithVariableOffsetEOFSeq() throws Exception {

        final String sigFile1 = "right-frag-1680.xml";
        final String fileToScan = "file1680-1a.ext";
        final String expectedPuid = "dev/1680";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile1);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        Path file = Paths.get(TESTAREA + fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
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
    ** Similar to the above test, except that the sequence containing the fragments is defined from BOF
    ** (beginning of file)
     */
    @Test
    public void testRecurringIntermediateRightFragmentWithVariableOffsetBOFSeq() throws Exception {

        final String sigFile1 = "right-frag-1681.xml";
        final String fileToScan = "file1680-1a.ext";
        final String expectedPuid = "dev/1681";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile1);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        Path file = Paths.get(TESTAREA + fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
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
    ** In this test, the sequence is defined with a variable offset range from EOF, and there are 2 right fragments.
    * The second fragment occurs twice - the first occurrence is valid relative to the offset from the first fragment,
    * but not in terms of the offset for the sequence as a whole.  The second occurrence, however is valid in terms
    * of both the offset from the first fragment and for the sequence as a whole.
    * N.B. This test does also pass in DROID 6.2.1
     */
    @Test
    public void testFinalRecurringRightFragmentWithVariableSeqOffset() throws Exception {

        final String sigFile1 = "right-frag-1682.xml";
        final String fileToScan = "file1680-2.ext";
        final String expectedPuid = "dev/1682";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile1);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        Path file = Paths.get(TESTAREA + fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
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
    * In this test, the sequence is defined with a variable offset range from EOF, and there are 2 right fragments.
    * The second fragment occurs once, at an offset valid in relation to the first fragment.  However, it lies beyond
    * the maximum offset from EOF for the sequence as a whole.  Therefore, no match shoudl be identified - unlike in
    * previous versions of DROID. (This test will fail in DROID 6.2.1).
    */
    @Test
    public void testFinalRightFragmentBeyondSeqMaxOffsetFix() throws Exception {

        final String sigFile1 = "right-frag-1682.xml";
        final String fileToScan = "file1680-2a.ext";
        final String expectedPuid = "dev/1682";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile1);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        Path file = Paths.get(TESTAREA + fileToScan);
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
    * Similar to above but more subtle.  A real world example though raised in Github - see
    * https://github.com/digital-preservation/droid/issues/104.
    * There's only one right fragment position but four options with differing sizes.
    * Two of the options consist of a single byte, the other two are two bytes - but use only the bytes in the single
    * byte options.
    * The right fragment must be at an offset of 0 from the main sequence 2525454F46, and also at an offset of 0
    * from EOF. On the first pass, the match fails because the rightmost fragment found does not meet the
    * requirement to be at an offset of 0 from EOF.  So we need to go and check for  further occurrence. The test
    * signature file has two signatures that are identical except that the second one has an additional right
    * right fragment option. The 1st signatures has the options 0D, 0A and 0D0A.  This means that the final bytes
    * of the file must match one of the following:
    * - 2525454F460D
    * - 2525454F460A
    * - 2525454F460D0A
    * The second signature has the additional fragment option 0A0A.  So the final bytes of the file can now also match:
    * - 2525454F460A0A
    * The test file ends with these bytes so should match the 2nd signature but not the first.
    * These test signatures are based on signature ID 190 in the current signature file (v.88).  The current live
    * version does not have the additional option so would fail with this DROID build. However, it would pass with
    * earlier versions of DROID up to 6.2.1 because they do not correctly enforce the EOF offset for the sequence
    * as a whole - and ths match against 2525454F460A even though the EOF offset for this pattern  is 1, not zero.
    */
    @Test
    public void testEdsFile() throws Exception {

        final String sigFile1 = "DROID_SignatureFile_x-fmt-91-only.xml";
        final String fileToScan = "819913.eps";
        final String expectedPuid = "x-fmt/91a";

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + sigFile1);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        Path file = Paths.get(TESTAREA + fileToScan);
        assertTrue(Files.exists(file));
        URI resourceUri = file.toUri();

        RequestMetaData metaData = new RequestMetaData(Files.size(file), Files.getLastModifiedTime(file).toMillis(), fileToScan);
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
