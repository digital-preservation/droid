/*
 * Test byteseek handles senior bit of ~bitmask correctly
 */
package uk.gov.nationalarchives.droid.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 *
 * @author rbrennan
 */
public class AnyBitmaskBugFixTest {
    private final String TESTAREA = "test_sig_files/";
    private final String SIGFILE = "AnyBitmask_SignatureFile.xml";
    private final String SCANFILE = "AnyBitmask.bit";
    
    private final int EXPECTED_HITS = 1;
    private final String EXPECTED_PUID = "dev/1";

    @Test
    public void testAnyBitmask() throws Exception {
        System.out.println("Running our very own AnyBitmask test");
        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(TESTAREA + SIGFILE);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        File file = new File(TESTAREA + SCANFILE);
        assertTrue(file.exists());
        URI resourceUri = file.toURI();
  
        InputStream in = new FileInputStream(file);
        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), SCANFILE);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);
        
        IdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(in);

        IdentificationResultCollection resultsCollection = droid.matchBinarySignatures(request);
        List<IdentificationResult> results = resultsCollection.getResults();
        
        assertEquals(EXPECTED_HITS, results.size());
        Iterator<IdentificationResult> iter = results.iterator();
        while (iter.hasNext()) {
            IdentificationResult result = iter.next();
            assertEquals(EXPECTED_PUID, result.getPuid());
        }
      }
}
