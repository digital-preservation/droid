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
package uk.gov.nationalarchives.droid.core;

import org.junit.Before;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by Toke Eskildsen, te@kb.dk on 2017-09-15.
 *
 * Tests fast termination of signature matching when only 1 match is needed.
 * This test borrows a lot from {@link SkeletonSuiteTest}.
 */
public class MatchLimitTest {

    private static final String TEST_FILES_DIR = "test-skeletons/";
    private File[] allPaths;

    private static final String[] KNOWN_MULTI_MATCH_FILES = new String[]{
            "test-skeletons/fmt/fmt-708-signature-id-831.wav", // 3 matches
            "test-skeletons/fmt/fmt-295-signature-id-314.ods"
    };
    private static final String SIGFILE = "test_sig_files/DROID_SignatureFile_V88.xml";

    @Before
    public void setup() throws FileNotFoundException, IOException {
        File fmtDirectory = new File(TEST_FILES_DIR + "fmt");
        File xfmtDirectory = new File(TEST_FILES_DIR + "x-fmt");

        File[] fmtPaths = fmtDirectory.listFiles();
        File[] xfmtPaths = xfmtDirectory.listFiles();
        this.allPaths = new File[fmtPaths.length + xfmtPaths.length];
        System.arraycopy(fmtPaths, 0, allPaths, 0, fmtPaths.length);
        System.arraycopy(xfmtPaths, 0, allPaths, fmtPaths.length, xfmtPaths.length);
    }

    /*
    Matches a file with known multiple signature matches and verifies that fast termination works.
     */
    @Test
    public void testMatchLimit() throws IOException {
        BinarySignatureIdentifier droid = createIdentifier();
        for (String knownMulti : KNOWN_MULTI_MATCH_FILES) {
            assertLimit(droid, knownMulti);
        }
    }

    /* There seems to be a problem with file handles not being properly freed */
    @Test
    public void testFileHandlesDepletion() throws IOException {
        final int RUNS = 1000;
        BinarySignatureIdentifier droid = createIdentifier();
        for (File sample: allPaths) {
            for (int i = 0; i < RUNS; i++) {
                getMatches(droid, sample, -11); // Might trigger "Too many open files"
            }
        }
    }

    /*
    Simple performance test that get matches for all known sample files with and without limit
    Disabled per default as it is slow.
     */
    @Test
    public void testLimitSpeed() throws IOException {
        final int RUNS = 50; // Raise this to raise measurement precision
        BinarySignatureIdentifier droid = createIdentifier();
        long totalNoLimitNS = 0;
        long totalLimitNS = 0;

        for (File sample: allPaths) {
            long noLimitNS = 0;
            long limitNS = 0;
            for (int i = 0; i < RUNS; i++) {
                {
                    noLimitNS -= System.nanoTime();
                    assertTrue(getMatches(droid, sample, -1).getResults().size() != -1);
                    noLimitNS += System.nanoTime();
                }
                {
                    limitNS -= System.nanoTime();
                    assertTrue(getMatches(droid, sample, 1).getResults().size() != -1);
                    limitNS += System.nanoTime();
                }
            }
            totalNoLimitNS += noLimitNS;
            totalLimitNS += limitNS;
            System.out.println(String.format(
                    "Average ms/detection for %d tests: no_limit=%.2f, limit_2==%.2f for " + sample,
                    RUNS, noLimitNS / 1000000.0 / RUNS, limitNS / 1000000.0 / RUNS));
        }

        System.out.println(String.format(
                "\nFinal result: Average ms/detection for %d tests: no_limit=%.2f, limit_1==%.2f for all %d samples",
                RUNS,
                totalNoLimitNS / 1000000.0 / RUNS / allPaths.length,
                totalLimitNS / 1000000.0 / RUNS / allPaths.length,
                allPaths.length));
    }

    private BinarySignatureIdentifier createIdentifier() {
        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(SIGFILE);
        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }
        return droid;
    }

    private void assertLimit(BinarySignatureIdentifier droid, String knownMulti) throws IOException {
        {
            int matchCount = getMatches(droid, knownMulti, -1).getResults().size();
            assertTrue(
                    "Plain matchBinarySignatures should return > 1 match for " + knownMulti +
                    ", but matched " + matchCount,
                    matchCount > 1);
        }
        {
            int matchCount = getMatches(droid, knownMulti, 1).getResults().size();
            assertEquals(
                    "matchBinarySignatures with maxMatches==1 should return 1 match for " + knownMulti,
                    1, matchCount);
        }
    }

    private IdentificationResultCollection getMatches(
            BinarySignatureIdentifier droid, String file, int maxMatches) throws IOException {
        return getMatches(droid, new File(file), maxMatches);
    }

    private IdentificationResultCollection getMatches(
            BinarySignatureIdentifier droid, File file, int maxMatches) throws IOException {
        URI resourceUri = file.toURI();
        String filename = file.getName();
        RequestMetaData metaData = new RequestMetaData(file.length(), file.lastModified(), filename);
        RequestIdentifier identifier = new RequestIdentifier(resourceUri);
        identifier.setParentId(1L);
        IdentificationRequest<File> request = new FileSystemIdentificationRequest(metaData, identifier);
        try {
            request.open(file);
            return droid.matchBinarySignatures(request, maxMatches);
        } finally {
            request.close();
        }
    }
}
