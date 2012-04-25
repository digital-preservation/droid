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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.nationalarchives.droid.command.TestUtil;

/**
 * @author rflitcroft
 *
 */
@Ignore
public class NonsenseCommandTest {

    private PrintWriter printWriter;
    
    @Before
    public void setup() {
        printWriter = mock(PrintWriter.class);
    }

    @Test
    public void testCommandLineErrorCommand() {
        CommandLineErrorCommand versionCommand = new CommandLineErrorCommand(printWriter, CommandLineParam.options());
        versionCommand.execute();
        
        verify(printWriter).println("Invalid usage: use droid -h to print the options.");
        verify(printWriter).println("usage: droid [options]");
        TestUtil.verifyHelpOptions(printWriter);
    }

}
