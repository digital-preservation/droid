package uk.gov.nationalarchives.droid.core.fragments;

/**
 * Created by boreilly on 09/08/2016.
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

}
