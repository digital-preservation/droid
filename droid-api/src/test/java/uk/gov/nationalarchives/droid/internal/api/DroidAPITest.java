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

import org.junit.Before;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DroidAPITest {

    private DroidAPI api;

    @Before
    public void setup() throws SignatureParseException {
        api = DroidAPITestUtils.createApi();
    }

    @Test
    public void should_create_non_null_instance_using_test_utility_class() {
        assertThat(api, is(notNullValue()));
    }

    @Test
    public void should_identify_given_file_with_binary_signature() throws IOException {
        List<ApiResult> results = api.submit(
                Paths.get("src/test/resources/persistence.zip"));
        assertThat(results, is(notNullValue()));

        assertThat(results.size(), is(1));

        ApiResult identificationResult = results.get(0);

        assertThat(identificationResult.getPuid(), is("x-fmt/263"));
        assertThat(identificationResult.getName(), is("ZIP Format"));
        assertThat(identificationResult.getMethod(), is(IdentificationMethod.BINARY_SIGNATURE));
    }

    @Test
    public void should_identify_given_file_using_container_signature() throws IOException {
        List<ApiResult> results = api.submit(
                Paths.get("../droid-container/src/test/resources/odf_text.odt"));
        assertThat(results, is(notNullValue()));

        assertThat(results.size(), is(1));

        ApiResult identificationResult = results.get(0);

        assertThat(identificationResult.getPuid(), is("fmt/291"));
        assertThat(identificationResult.getName(), is("Open Document Text 1.2"));
        assertThat(identificationResult.getMethod(), is(IdentificationMethod.CONTAINER));
    }

    @Test
    public void should_identify_given_file_using_file_extension() throws IOException {
        List<ApiResult> results = api.submit(Paths.get("src/test/resources/test.txt"));
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(1));

        ApiResult singleResult = results.get(0);

        assertThat(singleResult.getPuid(), is("x-fmt/111"));
        assertThat(singleResult.getMethod(), is(IdentificationMethod.EXTENSION));
    }

    @Test
    public void should_report_extension_of_the_file_under_identification_test() throws IOException {
        List<ApiResult> resultsWithExtension = api.submit(Paths.get("src/test/resources/test.txt"));
        List<ApiResult> resultsWithoutExtension = api.submit(Paths.get("src/test/resources/word97"));

        assertThat(resultsWithExtension.get(0).getExtension(), is("txt"));
        assertThat(resultsWithoutExtension.get(0).getExtension(), is(""));
    }

    @Test
    public void should_report_all_puids_when_there_are_more_than_one_identification_hits() throws IOException {
        List<ApiResult> results = api.submit(Paths.get("src/test/resources/double-identification.jpg"));
        assertThat(results.size(), is(2));
        assertThat(results.stream().map(ApiResult::getPuid).collect(Collectors.toList()),
                containsInAnyOrder("fmt/96", "fmt/41"));
        assertThat(results.stream().map(ApiResult::getName).collect(Collectors.toList()),
                containsInAnyOrder("Raw JPEG Stream", "Hypertext Markup Language"));
    }

    @Test
    public void should_report_when_there_is_an_extension_mismatch() throws IOException {
        List<ApiResult> results = api.submit(Paths.get("src/test/resources/docx-file-as-xls.xlsx"));
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getPuid(), is("fmt/412"));
        assertThat(results.get(0).isFileExtensionMismatch(), is(true));
    }

    @Test
    public void should_report_correct_version_for_the_binary_and_container_signature() {
        assertThat(api.getContainerSignatureVersion(), is("20230822"));
        assertThat(api.getDroidVersion(), is(ResourceBundle.getBundle("options").getString("version_no")));
        assertThat(api.getBinarySignatureVersion(), is("114"));
    }

    @Test
    public void should_produce_zero_results_for_an_empty_file() throws IOException {
        List<ApiResult> results = api.submit(Paths.get("src/test/resources/test"));
        assertThat(results, hasSize(0));
    }

    @Test
    public void should_produce_results_for_every_time_a_file_is_submitted_for_identification() throws IOException {
        final int MAX_ITER = 5000;
        int acc = 0;
        for (int i = 0; i < MAX_ITER; i++) {
            List<ApiResult> results = api.submit(
                    Paths.get("../droid-container/src/test/resources/odf_text.odt"));
            acc += results.size();
        }
        assertThat(acc, is(MAX_ITER));
    }
}
