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

import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test internal API against skeleton sample. Unfortunately skeleton sample is in different project,
 * but it works if we use relative path.
 */
@RunWith(Parameterized.class)
public class DroidApiSkeletonTest {

    private final String puid;
    private final Path path;
    private final DroidAPI api;

    public DroidApiSkeletonTest(String puid, Path path, DroidAPI api) {
        this.puid = puid;
        this.path = path;
        this.api = api;
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException, SignatureParseException {
        Pattern FILENAME = Pattern.compile("((?:x-)?fmt)-(\\d+)-signature-id-(\\d+).*");
        Set<String> ignorePuid = Sets.newHashSet("fmt/651", "fmt/652", "fmt/685");


        DroidAPI api = DroidAPI.getInstance(
                Paths.get("custom_home/signature_files/DROID_SignatureFile_V96.xml"),
                Paths.get("custom_home/container_sigs/container-signature-20200121.xml")
        );  //Create only once instance of Droid.

        return Stream.concat(
                        Files.list(Paths.get("../droid-core/test-skeletons/fmt")),
                        Files.list(Paths.get("../droid-core/test-skeletons/x-fmt"))
                ).map(x -> {
                    Matcher z = FILENAME.matcher(x.getFileName().toString());
                    if (!z.matches()) {
                        return null;
                    } else {
                        return new Object[]{z.group(1) + "/" + z.group(2), x, api};
                    }
                }).filter(x -> x != null && !ignorePuid.contains(x[0]))
                .collect(Collectors.toList());
    }

    @Test
    public void skeletonTest() throws Exception {
        IdentificationResultCollection result = api.submit(path);
        List<IdentificationResult> results = result.getResults();
        assertThat(results, hasItem(ResultMatcher.resultWithPuid(puid)));
    }

    private static class ResultMatcher extends TypeSafeMatcher<IdentificationResult> {

        private final String expectedPuid;

        private ResultMatcher(String expectedPuid) {
            this.expectedPuid = expectedPuid;
        }

        @Override
        protected boolean matchesSafely(IdentificationResult item) {
            return expectedPuid.equals(item.getPuid());
        }

        @Override
        protected void describeMismatchSafely(IdentificationResult item, Description mismatchDescription) {
            mismatchDescription.appendText("expected puid " + expectedPuid + " but got: " + item.getPuid());
        }

        @Override
        public void describeTo(Description description) {

        }

        public static org.hamcrest.Matcher<IdentificationResult> resultWithPuid(String puid) {
            return new ResultMatcher(puid);
        }
    }


}
