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
package uk.gov.nationalarchives.droid.command.action;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListReportsCommandTest {

    @Test
    public void shouldSendCorrectReportToThePrintWriter_WhenThereAreReportsDefined() throws CommandExecutionException {
        ListReportsCommand command = new ListReportsCommand();
        ReportManager mockManager = mock(ReportManager.class);
        PrintWriter mockWriter = mock(PrintWriter.class);

        ReportSpec spec1 = mock(ReportSpec.class);
        ReportSpec spec2 = mock(ReportSpec.class);

        when(spec1.getName()).thenReturn("Report name for spec 1");
        when(spec2.getName()).thenReturn("Report name for spec 2");

        when(mockManager.listReportSpecs()).thenReturn(Arrays.asList(spec1, spec2));

        command.setReportManager(mockManager);
        command.setPrintWriter(mockWriter);

        command.execute();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockWriter, times(1)).println(captor.capture());

        String expected = "\nReport:\t'Report name for spec 1'\n\tFormats:\t'Pdf'\t'DROID Report XML'" +
                          "\nReport:\t'Report name for spec 2'\n\tFormats:\t'Pdf'\t'DROID Report XML'";
        String actual = captor.getValue();

        assertEquals(expected, actual);
    }
}