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
package uk.gov.nationalarchives.droid.gui.help;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;

public class AboutDialogDataTest {

    @Test
    public void should_build_the_data_for_about_box() {
        AboutDialogData.AboutDialogDataBuilder builder = new AboutDialogData.AboutDialogDataBuilder();
        AboutDialogData data = builder.build();
        assertThat(data, notNullValue());
    }

    @Test
    public void should_initialise_all_fields_with_empty_strings_when_not_set_using_builder() {
        AboutDialogData.AboutDialogDataBuilder builder = new AboutDialogData.AboutDialogDataBuilder();
        AboutDialogData data = builder.build();
        assertThat(data.getDroidFolder(), notNullValue());
        assertThat(data.getBuildTimeStamp(), notNullValue());
        assertThat(data.getDroidVersion(), notNullValue());
        assertThat(data.getOperatingSystem(), notNullValue());
        assertThat(data.getJavaLocation(), notNullValue());
        assertThat(data.getJavaVersion(), notNullValue());
        assertThat(data.getLogFolder(), notNullValue());

        assertThat(data.getDroidFolder(), isEmptyString());
        assertThat(data.getBuildTimeStamp(), isEmptyString());
        assertThat(data.getDroidVersion(), isEmptyString());
        assertThat(data.getOperatingSystem(), isEmptyString());
        assertThat(data.getJavaLocation(), isEmptyString());
        assertThat(data.getJavaVersion(), isEmptyString());
        assertThat(data.getLogFolder(), isEmptyString());
    }

    @Test
    public void should_initialise_fields_used_with_builder_to_the_correct_values() {
        AboutDialogData.AboutDialogDataBuilder builder = new AboutDialogData.AboutDialogDataBuilder();
        AboutDialogData data = builder.withDroidFolder("some_folder").withOsName("rocky linux").withJavaVersion("1.1.8").build();

        assertThat(data.getDroidFolder(), is("some_folder"));
        assertThat(data.getBuildTimeStamp(), isEmptyString());
        assertThat(data.getDroidVersion(), isEmptyString());
        assertThat(data.getOperatingSystem(), is("rocky linux"));
        assertThat(data.getJavaLocation(), isEmptyString());
        assertThat(data.getJavaVersion(), is("1.1.8"));
        assertThat(data.getLogFolder(), isEmptyString());
    }

}