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

    public static Stream<SkeletonTest> data() throws IOException, SignatureParseException {
        Pattern FILENAME = Pattern.compile("((?:x-)?fmt)-(\\d+)-signature-id-(\\d+).*");
        Set<String> ignorePuid = getIgnoredPuids();
        return Stream.concat(
                        Files.list(Paths.get("../droid-core/test-skeletons/fmt")),
                        Files.list(Paths.get("../droid-core/test-skeletons/x-fmt"))
                ).flatMap(x -> {
                    Matcher z = FILENAME.matcher(x.getFileName().toString());
                    if (!z.matches()) {
                        return null;
                    } else {
                        String puid = z.group(1) + "/" + z.group(2);
                        if (ignorePuid.contains(puid)) {
                            return null;
                        }
                        String uriPath = x.toUri().getPath()
                                .replaceAll(" ", "%20")
                                .replaceAll("\\\\", "/");
                        return Stream.of(
                            new SkeletonTest(puid, x.toUri()),
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
        List<ApiResult> results = api.submit(skeletonTest.uri);
        assertThat(results, hasItem(ResultMatcher.resultWithPuid(skeletonTest.puid)));
    }

    private static class ResultMatcher extends TypeSafeMatcher<ApiResult> {

        private final String expectedPuid;

        private ResultMatcher(String expectedPuid) {
            this.expectedPuid = expectedPuid;
        }

        @Override
        protected boolean matchesSafely(ApiResult item) {
            return expectedPuid.equals(item.getPuid());
        }

        @Override
        protected void describeMismatchSafely(ApiResult item, Description mismatchDescription) {
            mismatchDescription.appendText("expected puid " + expectedPuid + " but got: " + item.getPuid());
        }

        @Override
        public void describeTo(Description description) {

        }

        public static org.hamcrest.Matcher<ApiResult> resultWithPuid(String puid) {
            return new ResultMatcher(puid);
        }
    }
}
