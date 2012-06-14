/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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

/*
 * Test BOF/EOF maximum offset bug has been fixed
 * 
 * With BOF signatures of the form SSS{x-z}TTTT
 * where:   BOF max offset is 0,
 *          SSS and TTTT are fragments with TTTT being the longer and therefore the anchor,
 *          x and z define an interval range,
 * DROID6 finds the anchor, TTTT, then scans back towards the BOF looking for SSS within 
 * the range, x-z, from TTTT. If SSS is found at offset y before the end of the range is
 * reached, a positive id is incorrectly returned even if SSS is offset up to (z-y) from BOF.
 * Ditto for EOF signatures of the form TTTT{x-z}SSS.
 *
 * @author rbrennan
 */
public class BofEofOffsetBugFixTest {
    
    private final String TESTAREA = "test_sig_files/";
    private final String SIGFILE = "BofEofOffset_SignatureFile.xml";
    private final String SCANFILE = "BofEofOffset.bof";

    private final int EXPECTED_HITS = 0;
    private final String EXPECTED_PUID = "x-fmt/752";

    @Test
    public void testBofOffset() throws Exception {
    
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
