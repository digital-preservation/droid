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
package uk.gov.nationalarchives.droid.core;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.junit.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by boreilly on 09/11/2016.
 *
 * This test is based around the skeleton file suite provide by Ross Spencer at:
 * https://github.com/exponential-decay/pronom-archive-and-skeleton-test-suite/releases/tag/
 * skeleton-test-suite-2019-05-09-sig-file-v95
 * See also: https://github.com/digital-preservation/droid/issues/87
 *
 * It can be adapted and expanded as required.  The  default expectation is that
 * the  PUID should match the start of the filename after replacing the "-" after fmt with a "/"
 * E.g. fmt-1-signature-id-1032.wav should be identified as fmt/1.
 * There are though two "exceptions" lists under this directory where a different result is expected:
 *  - current-differently-identified-files.txt, where DROID is expected to identify the file, but not with the PUID
 *    derived from the filename.
 *  - current-unidentified-files, where DROID is not expected to identify the file at all.
 *
 */
public class SkeletonSuiteTest {

    private final static String NO_PUID = "{None}";

    private static final String TEST_FILES_DIR = "test-skeletons/";
    //TODO: Read latest signature file by default where required in this and other tests.
    private static final String SIGFILE = "test_sig_files/DROID_SignatureFile_V95.xml";
    private static final Pattern PuidInFilenamePattern = Pattern.compile("^(x-)?fmt-\\d{1,4}");
    private static final Pattern PuidPattern = Pattern.compile("^(x-)?fmt/\\d{1,4}");

    private HashMap<String, String> filesWithPuids;
    private HashMap<String, String> currentMisidentifiedFiles = new HashMap<>();
    private String[] currentKnownUnidentifiedFiles;
    private final List<Path> allPaths = new ArrayList<>();

    @Before
    public void setup() throws IOException{

        //Hashmap to store filenames and the PUID with which DROId is expected to identify the file.
        this.filesWithPuids = new HashMap<>();

        final Path fmtDirectory = Paths.get(TEST_FILES_DIR + "fmt");
        final Path xfmtDirectory = Paths.get(TEST_FILES_DIR + "x-fmt");
        for(final File file : fmtDirectory.toFile().listFiles()) {
            allPaths.add(file.toPath());
        }
        for(final File file : xfmtDirectory.toFile().listFiles()) {
            allPaths.add(file.toPath());
        }

        //Populate the hashmap using filenames as keys and PUIDs.  We need to do it this way round as some
        //PUIDs are expected by more than one file.
        for(final Path skeletonPath : allPaths) {

            String filename = null;
            if (skeletonPath != null && Files.isRegularFile(skeletonPath)) {
                try {
                     filename = skeletonPath.getFileName().toString();
                } catch (NullPointerException e) {
                    System.out.println("Could not get file name for " + skeletonPath);
                }

                assertNotEquals(filename, null);

                String expectedPuid = NO_PUID;

                //The files are named so that the expected PUID should match the start of the filename after replacing
                // the - after fmt with /  e.g. fmt-1-signature-id-1032.wav should be identified as fmt/1, and
                // x-fmt-1-signature-id-485.mcw as x-fmt/1
                Matcher puidMatcher = PuidInFilenamePattern.matcher(filename);
                while(puidMatcher.find()) {
                    expectedPuid = puidMatcher.group().replace("fmt-", "fmt/").toLowerCase();
                    assertNotEquals(expectedPuid, null);
                }
                // If we haven't got a PUID from the filename in the expected format for any file, don't go any further.
                assertNotEquals(expectedPuid, NO_PUID);
                assertTrue(expectedPuid.matches(this.PuidPattern.pattern()));
                this.filesWithPuids.put(filename, expectedPuid);
            }
        }

        //Look up the list of files currently known not to be identified, and store the filenames in an array.
        populateNonAndDifferentlyIdentifiedFiles();

        //Check that no file has been added to both the "no identifications" and "other identifications" lists!
        String inBothLists =
                "One or more skeleton files is listed for both \"no identifications\" and \"other\" identifications!\n";
        inBothLists += "Please review your skeleton file test configuration.";
        for(String s: currentMisidentifiedFiles.keySet()) {
            assertTrue(inBothLists, !ArrayUtils.contains(currentKnownUnidentifiedFiles,s));
        }
    }

    @Test
    public void testBinarySkeletonMatch() throws Exception {

        BinarySignatureIdentifier droid = new BinarySignatureIdentifier();
        droid.setSignatureFile(SIGFILE);

        try {
            droid.init();
        } catch (SignatureParseException x) {
            assertEquals("Can't parse signature file", x.getMessage());
        }

        int errorCount = 0;

        //Go through all the skeleton files.  Check if the PUID that DROId identifies for the file matches the beginning
        // of the file name. Or if not, that it is expected to return a different PUID, or none at all.
        for(final Path skeletonPath : this.allPaths) {

            URI resourceUri = skeletonPath.toUri();
            String filename = skeletonPath.getFileName().toString();

            RequestMetaData metaData = new RequestMetaData(
                    Files.size(skeletonPath), Files.getLastModifiedTime(skeletonPath).toMillis(), filename);
            RequestIdentifier identifier = new RequestIdentifier(resourceUri);
            identifier.setParentId(1L);

            IdentificationRequest<Path> request = new FileSystemIdentificationRequest(metaData, identifier);
            request.open(skeletonPath);

            IdentificationResultCollection resultsCollection = droid.matchBinarySignatures(request);
            List<IdentificationResult> results = resultsCollection.getResults();
            String expectedPuid = filesWithPuids.get(filename);

            assertNotEquals(expectedPuid, null);

            if (!ArrayUtils.contains(this.currentKnownUnidentifiedFiles, filename)) {

                // Check if we have any results from DROID - we should have as the file is not in the list of known
                // unidentified files.
                try {
                    // Catch assertion failure so we can print an error and continue, checking the total
                    // error count after all files are processed.
                    assertTrue(results.size() >= 1);
                } catch (AssertionError e) {
                    System.out.println("No results found for file: " + filename + ". Expected: " + expectedPuid);
                    errorCount++;
                    continue;
                }

                //If we reach here, we have at least one result from DROID for the current file.
                //Allow for more than one identification to be returned by DROID.  This is fine as long as one of them
                //matches the expected PUID.
                String[] puidsIdentified = getPuidsFromIdentification(results);

                try {
                    //Catch assertion failure so we can print error and continue, checking the total error count after
                    // all files are processed.
                    Assert.assertTrue(ArrayUtils.contains(puidsIdentified,expectedPuid));
                } catch( AssertionError e) {
                    //Is this a file where we're expecting a different PUID to the one the filename starts with?
                    if(this.currentMisidentifiedFiles.containsKey(filename)) {
                        String expectedWrongPuid = currentMisidentifiedFiles.get(filename);
                        if(ArrayUtils.contains(puidsIdentified, expectedWrongPuid)) {
                            System.out.println(String.format("INFO: Skeleton file %s identified by expected \"wrong\"" +
                                    " PUID %s instead of %s.", filename, expectedWrongPuid, expectedPuid));
                        } else {
                            System.out.println(printError(filename, expectedWrongPuid, puidsIdentified));
                            errorCount++;
                        }
                    } else {
                        // We expected DROID to identify this file with a PUID matching the start of the filename - so
                        // this is an unexpected result.
                        System.out.println(printError(filename, expectedPuid, puidsIdentified));
                        errorCount++;
                    }

                }
            } else {
                //We expect this file not be identified by its name derived PUID, or by any alternative PUID
                try {
                    //Catch assertion failure so we can print error and continue, checking the total error count after
                    // all files are processed.
                    assertTrue(results.size() == 0);
                }  catch ( AssertionError e) {

                    String[] puidsIdentifiedForFile = getPuidsFromIdentification(results);

                    System.out.println(printError(filename, NO_PUID, puidsIdentifiedForFile));
                    errorCount++;
                }

            }
        }

        assertEquals(String.format("%1$d error(s) occurred in skeleton file identifications, see earlier messages.",
                errorCount), 0, errorCount);
    }

    private void populateNonAndDifferentlyIdentifiedFiles() throws IOException {

        final List<String> unidentifiedFiles = new ArrayList<>();

        try(final BufferedReader reader = Files.newBufferedReader(Paths.get("test-skeletons/current-unidentified-files.txt"), UTF_8)) {

            String strLine;
            while ((strLine = reader.readLine()) != null) {
                // Split the line by whitespace to allow for comments after filename.
                // N.B. Assumes no spaces in the filename!
                //Allow for comment lines and commented out entries...
                if (!strLine.startsWith("//")) {
                    String filename = strLine.split("\\s+")[0];
                    unidentifiedFiles.add(filename);
                }
            }

        }

        this.currentKnownUnidentifiedFiles = unidentifiedFiles.toArray(new String[0]);

        //Populate files whihc ar expected to be identified by DROId but the PUID identified is not currently
        //expected to match the beginning of the file name.

        try(final BufferedReader reader = Files.newBufferedReader(Paths.get("test-skeletons/current-differently-identified-files.txt"), UTF_8)) {

            String strLine;
            while ((strLine = reader.readLine()) != null) {
                // Split the line by whitespace to allow for comments after filename and PUID.
                // N.B. Assumes no spaces in the filename!
                //Allow for comment lines and commented out entries.
                if (!strLine.startsWith("//")) {
                    String filename = strLine.split("\\s+")[0];
                    String puid = strLine.split("\\s+")[1];
                    this.currentMisidentifiedFiles.put(filename, puid);
                }
            }
        }
    }

    private static String printError(String filename, String expectedPuid, String[] puidsIdentified) {

        StringBuilder sb = new StringBuilder("ERROR: ");

        if (expectedPuid == NO_PUID) {
            sb.append("Skeleton file " + filename + " expected to have no identifications, however:");
        } else {
            sb.append("Couldn't find expected PUID: " + expectedPuid);
            sb.append(" for skeleton file " + filename);
        }

        for (String puid:puidsIdentified) {
            sb.append("\n - DROID Identified " + puid);
        }
        sb.append("\n");
        return sb.toString();
    }

    private static String[] getPuidsFromIdentification(List<IdentificationResult> results) {

        String[] puidsIdentified = new String[results.size()];

        for(int i = 0; i< results.size() ; i++) {
            IdentificationResult result = results.get(i);
            puidsIdentified[i] = result.getPuid();
        }
        return  puidsIdentified;
    }
}