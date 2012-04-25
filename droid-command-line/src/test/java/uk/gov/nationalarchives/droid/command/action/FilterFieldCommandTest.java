/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
