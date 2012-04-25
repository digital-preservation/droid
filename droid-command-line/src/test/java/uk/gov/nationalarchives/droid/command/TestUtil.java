/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
