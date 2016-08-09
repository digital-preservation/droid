package uk.gov.nationalarchives.droid.core.fragments;

/**
 * Created by boreilly on 09/08/2016.
 *
 */

import java.io.File;
import java.net.URI;
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
        File file = new File(TESTAREA + fileToScan);
        assertTrue(file.exists());
        URI resourceUri = file.toURI();

        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<File> request = new FileSystemIdentificationRequest(metaData, identifier);
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
        File file = new File(TESTAREA + fileToScan);
        assertTrue(file.exists());
        URI resourceUri = file.toURI();

        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<File> request = new FileSystemIdentificationRequest(metaData, identifier);
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
        File file = new File(TESTAREA + fileToScan);
        assertTrue(file.exists());
        URI resourceUri = file.toURI();

        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<File> request = new FileSystemIdentificationRequest(metaData, identifier);
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
    * The second fragment occurs once, at an valid in relation to the first fragment.  However, it lies beyond the
    * maximum offset from EOF for the sequence as a whole.  Therefore, no match shoudl be identified - unlike in
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
        File file = new File(TESTAREA + fileToScan);
        assertTrue(file.exists());
        URI resourceUri = file.toURI();

        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), fileToScan);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);

        IdentificationRequest<File> request = new FileSystemIdentificationRequest(metaData, identifier);
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
}
