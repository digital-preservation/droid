/*
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
package uk.gov.nationalarchives.droid.internal.api;

import com.sun.net.httpserver.HttpServer;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.nationalarchives.droid.core.SignatureParseException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.gov.nationalarchives.droid.internal.api.DroidAPITestUtils.createHttpServer;
import static uk.gov.nationalarchives.droid.internal.api.DroidAPITestUtils.createS3Server;

/**
 * Test internal API against skeleton sample. Unfortunately skeleton sample is in different project,
 * but it works if we use relative path.
 * The test looks at puid mentioned in the filename and expects that as a result from the API
 *
 */
public class DroidAPISkeletonTest {

    private static HttpServer s3Server;

    private static HttpServer httpServer;

    private static DroidAPI api;

    @BeforeAll
    static void setup() throws IOException, SignatureParseException {
        s3Server = createS3Server();
        httpServer = createHttpServer();
        api = DroidAPITestUtils.createApi(URI.create("http://localhost:" + s3Server.getAddress().getPort()));
    }

    public record SkeletonTest(String puid, URI uri) {}

    public static Stream<SkeletonTest> data() throws IOException {
        Pattern FILENAME = Pattern.compile("((?:x-)?fmt)-(\\d+)-signature-id-(\\d+).*");
        Set<String> ignorePuid = getIgnoredPuids();
        return Stream.concat(
                        Files.list(Paths.get("../droid-core/test-skeletons/fmt")),
                        Files.list(Paths.get("../droid-core/test-skeletons/x-fmt"))
                ).flatMap(path -> {
                    Matcher matcher = FILENAME.matcher(path.getFileName().toString());
                    if (!matcher.matches()) {
                        return null;
                    } else {
                        String puid = matcher.group(1) + "/" + matcher.group(2);
                        if (ignorePuid.contains(puid)) {
                            return null;
                        }
                        String uriPath = path.toUri().getPath()
                                .replaceAll(" ", "%20")
                                .replaceAll("\\\\", "/");
                        return Stream.of(
                            new SkeletonTest(puid, path.toUri()),
                            new SkeletonTest(puid, URI.create("s3://localhost:" + s3Server.getAddress().getPort() + uriPath)),
                            new SkeletonTest(puid, URI.create("s3://localhost:" + httpServer.getAddress().getPort() + uriPath))
                        );
                    }
                }).filter(Objects::nonNull);
    }

    /**
     * This method returns the list of PUIDs to be ignored for the purpose of testing the results
     * from the API for the test skeleton suite.
     * Following table describes the current puids that are ignored because the identification using current
     * signature file does not match the indicated puid in filename
     * ---------------- Current signature file V118 ----------------
     * fmt-1062-signature-id-1435.3fr			fmt/353		Signature
     * fmt-1157-signature-id-1539.nfo
     * fmt-1739-signature-id-2077.toast		    fmt/468		Signature
     * fmt-1757-signature-id-2094.iso			fmt/1740	Signature
     * fmt-1757-signature-id-2095.iso			fmt/1740	Signature
     *
     * @return Puids that are ignored for the current signature file
     */
    private static Set<String> getIgnoredPuids() {
        return Stream.of("fmt/1062", "fmt/1157", "fmt/1757", "fmt/1739")
                .collect(Collectors.toCollection(HashSet::new));
    }


    @Execution(ExecutionMode.CONCURRENT)
    @ParameterizedTest
    @MethodSource("data")
    public void skeletonTest(SkeletonTest skeletonTest) throws Exception {
        List<DroidAPI.APIIdentificationResult> results = api.submit(skeletonTest.uri)
                .stream()
                .flatMap(l -> l.identificationResults().stream()).collect(Collectors.toList());
        assertThat(results, hasItem(ResultMatcher.resultWithPuid(skeletonTest.puid)));
    }

    private static class ResultMatcher extends TypeSafeMatcher<DroidAPI.APIIdentificationResult> {

        private final String expectedPuid;

        private ResultMatcher(String expectedPuid) {
            this.expectedPuid = expectedPuid;
        }

        @Override
        protected boolean matchesSafely(DroidAPI.APIIdentificationResult item) {
            return expectedPuid.equals(item.puid());
        }

        @Override
        protected void describeMismatchSafely(DroidAPI.APIIdentificationResult item, Description mismatchDescription) {
            mismatchDescription.appendText("expected puid " + expectedPuid + " but got: " + item.puid());
        }

        @Override
        public void describeTo(Description description) {

        }

        public static org.hamcrest.Matcher<DroidAPI.APIIdentificationResult> resultWithPuid(String puid) {
            return new ResultMatcher(puid);
        }
    }
}
