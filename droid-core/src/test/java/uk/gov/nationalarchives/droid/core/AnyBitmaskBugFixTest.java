/*
 * Test byteseek handles senior bit of ~bitmask correctly
 */
package uk.gov.nationalarchives.droid.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
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
    
    public AnyBitmaskBugFixTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testAnyBitmask() throws Exception {
    
        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile("test_sig_files/AnyBitmask_SignatureFile.xml");
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        File file = new File("test_sig_files/test80.bit");
        assertTrue(file.exists());
        URI resourceUri = file.toURI();
  
        InputStream in = new FileInputStream(file);
        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), "test80.bit");
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);
        
        IdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);
        request.open(in);

        IdentificationResultCollection resultsCollection = droid.matchBinarySignatures(request);
        List<IdentificationResult> results = resultsCollection.getResults();
        
        //byteseek 1.1
        assertEquals(0, results.size());
        
        //byteseek 1.2
//        assertEquals(1, results.size());
        
//        IdentificationResult result = results.getResults().iterator().next();
//        assertEquals("any/1", result.getPuid());
      }
}
