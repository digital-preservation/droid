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
package uk.gov.nationalarchives.droid.command;

import java.io.PrintWriter;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.mockito.ArgumentCaptor;

/**
 * @author rflitcroft
 *
 */
@Ignore
public final class TestUtil {

    private static final String NEW_LINE = System.getProperty("line.separator");
    
    private TestUtil() { }
    
    /**
     * Verifies help options are written correctly to the print writer.
     * @param writer a print writer
     */
    public static void verifyHelpOptions(PrintWriter writer) {
        ArgumentCaptor<String> helpCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(writer, atLeastOnce()).println(helpCaptor.capture());
        String help = helpCaptor.getValue();
        
        int i = 0;
        String[] helps = help.split(NEW_LINE);
        assertEquals("  -a,--profile-resources <resources...>    runs a profile with the resources", helps[i++]);
        assertEquals("                                           specified", helps[i++]);
        assertEquals("  -c,--check-signature-update              Check for signature updates", helps[i++]);
        assertEquals("  -d,--download-signature-update           Download latest signature updates", helps[i++]);
        assertEquals("  -e,--export <filename>                   export the specified profiles", helps[i++]);
        assertEquals("  -f,--filter-all <filter ...>             filters which must match all the", helps[i++]);
        assertEquals("                                           filter conditions (logical AND)", helps[i++]);
        assertEquals("  -F,--filter-any <filter ...>             filters which may match any filter", helps[i++]);
        assertEquals("                                           conditions (logical OR)", helps[i++]);
        assertEquals("  -h,--help                                print this message", helps[i++]); 
        assertEquals("  -k,--filter-fields                       lists the "
                + "available fields to use in", helps[i++]); 
        assertEquals("                                           filters", helps[i++]); 
        assertEquals("  -n,--report-name <report name>           name of the report", helps[i++]);
        assertEquals("  -p,--profile <filename ...>              a list of profiles to export or", helps[i++]);
        assertEquals("                                           report", helps[i++]); 
        assertEquals("  -q,--quiet                               only print errors to the console", helps[i++]); 
        assertEquals("  -r,--report <filename>                   generate report", helps[i++]);
        assertEquals("  -R,--recurse                             recurse into subdirectories", helps[i++]);
        assertEquals("  -s,--configure-signature-file <version>  sets the current default signature", helps[i++]);
        assertEquals("                                           file version", helps[i++]);
        assertEquals("  -v,--version                             the software version", helps[i++]);
        assertEquals("  -X,--list-signature-files                Lists all available signature files", helps[i++]);
        assertEquals("  -x,--display-signature-file              the current default signature file", helps[i++]);
        assertEquals("                                           version", helps[i++]);
        
        assertEquals(helps.length, i);
        
    }
    
    /**
     * Verifies help options are written correctly to the print writer.
     * @param writer a print writer
     */
    public static void verifyFilterFieldHelp(PrintWriter writer) {
        
    }
}
