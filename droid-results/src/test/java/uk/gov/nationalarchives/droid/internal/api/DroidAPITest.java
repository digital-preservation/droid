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

import org.junit.Ignore;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DroidAPITest {

    @Test
    public void testCreateInstance() {
        DroidAPI api = aApi();
        assertThat(api, is(notNullValue()));
    }

    @Test
    public void testIdentification() throws IOException {
        DroidAPI api = aApi();

        IdentificationResultCollection result = api.submit(
                Paths.get("./test_sig_files/persistence.zip"));
        assertThat(result, is(notNullValue()));

        assertThat(result.getResults().size(), is(1));

        IdentificationResult identificationResult = result.getResults().get(0);

        assertThat(identificationResult.getPuid(), is("x-fmt/263"));
        assertThat(identificationResult.getName(), is("ZIP Format"));
    }

    @Test
    public void testContainerIdentification() throws IOException {
        DroidAPI api = aApi();

        IdentificationResultCollection result = api.submit(
                Paths.get("../droid-container/src/test/resources/odf_text.odt"));
        assertThat(result, is(notNullValue()));

        assertThat(result.getResults().size(), is(1));

        IdentificationResult identificationResult = result.getResults().get(0);

        assertThat(identificationResult.getPuid(), is("fmt/291"));
        assertThat(identificationResult.getName(), is("Open Document Text 1.2"));

    }

    @Ignore
    public void testRunInLoop() throws IOException {
        long start = System.currentTimeMillis();
        DroidAPI api = aApi();
        final int MAX_ITER = 5000;
        int acc = 0;
        long init = System.currentTimeMillis();
        for (int i = 0; i < MAX_ITER; i++) {
            IdentificationResultCollection result = api.submit(
                    Paths.get("../droid-container/src/test/resources/odf_text.odt"));
            acc += result.getResults().size();
        }

        long end = System.currentTimeMillis();

        System.out.println(acc);

        System.out.println(init - start);
        System.out.println((double) (end - init) / (double) MAX_ITER);


    }


    private DroidAPI aApi() {
        try {
            return DroidAPI.getInstance(Paths.get("custom_home/signature_files/DROID_SignatureFile_V96.xml"),
                    Paths.get("custom_home/container_sigs/container-signature-20200121.xml"));
        } catch (SignatureParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
