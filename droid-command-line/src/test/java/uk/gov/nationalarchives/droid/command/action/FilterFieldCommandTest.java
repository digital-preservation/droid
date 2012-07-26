/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.command.FilterFieldCommand;

/**
 * @author rflitcroft
 *
 */
@Ignore
public class FilterFieldCommandTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Test
    public void testFilterFieldOutput() {
        
        PrintWriter printWriter = mock(PrintWriter.class);
        
        FilterFieldCommand command = new FilterFieldCommand(printWriter);
        command.execute();
        
        ArgumentCaptor<String> helpCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(printWriter, times(11)).println(helpCaptor.capture());
        verify(printWriter, atLeastOnce()).flush();
        final List<String> allValues = helpCaptor.getAllValues();
        Iterator<String> helps = allValues.iterator();
        
        assertPrinted("file_ext", "The file extension (e.g. 'exe')", helps.next());
        assertPrinted("file_name", "The name of the resource (e.g. 'system.dll')", helps.next());
        assertPrinted("file_size", "The file size in bytes (e.g. 150000)", helps.next());
        assertPrinted("format_count", "The number of format identifications made", helps.next());
        assertPrinted("format_name", "The file format description (text)", helps.next());
        assertPrinted("last_modified", "The last modifed date of the file ( yyyy-MM-dd )", helps.next());
        assertPrinted("method", "How the file was identified ( ext | bin )", helps.next());
        assertPrinted("mime_type", "The mime-type of the identification", helps.next());
        assertPrinted("puid", "The PUID identified (e.g. x-fmt/101)", helps.next());
        //assertPrinted("severity", "The severity of a message associated with a resource "
        //    + "( info | warn | error )", helps.next());
        assertPrinted("status", "The identification job status ( not_done | done | problem )", helps.next());
        assertPrinted("type", "The type of resource ( File | Folder | Container )", helps.next());
        
        assertFalse(helps.hasNext());
        
    }
    
    private static void assertPrinted(String line1, String line2, String actual) {
        assertEquals(line1 + NEW_LINE + "    " + line2, actual);
    }
}
