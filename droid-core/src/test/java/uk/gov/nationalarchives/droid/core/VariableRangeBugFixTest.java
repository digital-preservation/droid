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
 * Test variable range bug has been fixed
 * 
 * Hitherto, a signature containing fragments involving a variable length range  
 * (e.g. frag1{10-20}frag2 ... frag3) could sometimes fail to identify a matching file.  
 * This would happen when frag2 occurs in the file more than once within the range interval  
 * with earlier, but not all, occurrences being found too far from frag3 to satisfy the signature.
 * The signature search algorithm has now been amended to continue a hitherto unsuccessful search
 * until all variable length ranges have been completely examined.
 *
 * @author rbrennan
 */
public class VariableRangeBugFixTest {
    
    private final String TESTAREA = "test_sig_files/";
    
    @Test
    public void testLeftVariableRange() throws Exception {

        final String SIGFILE = "VariableRange_SignatureFile.xml";
        final String SCANFILE = "VariableRange.enl";
        final int EXPECTED_HITS = 1;
        final String EXPECTED_PUID = "x-fmt/757";

    
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
    
    @Test
    public void testRightVariableRange() throws Exception {
        
        final String SIGFILE = "VariableRangeSwap_SignatureFile.xml";
        final String SCANFILE = "VariableRangeSwap.enl";
        final int EXPECTED_HITS = 1;
        final String EXPECTED_PUID = "x-fmt/758";
    
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
