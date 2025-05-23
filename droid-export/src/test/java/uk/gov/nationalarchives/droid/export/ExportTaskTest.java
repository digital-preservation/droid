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
package uk.gov.nationalarchives.droid.export;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.nationalarchives.droid.export.interfaces.ExportDetails;
import uk.gov.nationalarchives.droid.export.interfaces.ItemWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @author Adam Retter
 *
 */
public class ExportTaskTest {

    ItemWriter itemWriter;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        itemWriter = mock(ItemWriter.class);
    }

    @Test
    public void testEncodingDefault() throws IOException {
        File tempFile = temporaryFolder.newFile("export-task-test-default-encoding");
        final String destination = tempFile.getAbsolutePath();
        ExportDetails exportDetails = new ExportDetails.ExportDetailsBuilder().build();

        final ExportTask pmExportTask = spy(new ExportTask(destination, new ArrayList<String>(), null, exportDetails, itemWriter, null));
        pmExportTask.run();

        verify(pmExportTask, never()).newOutputFileWriterEncoded(null, tempFile.toPath());
    }

    @Test
    public void testEncodingSpecific() throws IOException {
        File tempFile = temporaryFolder.newFile("export-task-test-specific-encoding");
        final String destination = tempFile.getAbsolutePath();
        final String encoding = "UTF-8";
        ExportDetails.ExportDetailsBuilder builder = new ExportDetails.ExportDetailsBuilder();
        ExportDetails exportDetails = builder.withOutputEncoding("UTF-8").build();
        final ExportTask pmExportTask = spy(new ExportTask(destination, new ArrayList<String>(), null, exportDetails, itemWriter, null));
        pmExportTask.run();

        verify(pmExportTask, times(1)).newOutputFileWriterEncoded(encoding, tempFile.toPath());
    }
}
