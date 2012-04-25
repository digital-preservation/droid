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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.junit.Before;
import org.junit.Test;

import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;

/**
 * @author rflitcroft
 *
 */
public class CommandFactoryTest {

    private CommandFactoryImpl factory;
    private GlobalContext context;
    private PrintWriter printWriter;
    
    @Before
    public void setup() {
        context = mock(GlobalContext.class);
        printWriter = mock(PrintWriter.class);
        
        factory = new CommandFactoryImpl(context, printWriter);
    }
    
    
    @Test
    public void testExportCommand() throws Exception {
        
        ExportCommand exportCommand = new ExportCommand();
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        
        String[] args = new String[] {
            "-E",
            "out.csv",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        };
            
        String[] expectedProfiles = {
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        };

        CommandLineParser parser = new GnuParser();
        CommandLine cli = parser.parse(CommandLineParam.options(), args);
        
        ExportCommand e1 = (ExportCommand) factory.getExportFormatCommand(cli);
        
        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("out.csv", exportCommand.getDestination());
    }
    
    @Test
    public void testExportCommandWithNoExportArgument() throws Exception {
        
        ExportCommand exportCommand = new ExportCommand();
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        
        String[] args = new String[] {
            "-e",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        };
            
        String[] expectedProfiles = {
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        };

        CommandLineParser parser = new GnuParser();
        CommandLine cli = parser.parse(CommandLineParam.options(), args);
        
        ExportCommand e1 = (ExportCommand) factory.getExportFormatCommand(cli);
        
        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertNull(exportCommand.getDestination());
    }
    
    @Test (expected = CommandLineSyntaxException.class)
    public void testExportCommandWithNoProfiles() throws Exception {
        
        ExportCommand exportCommand = new ExportCommand();
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        
        String[] args = new String[] {
            "-e",
        };
        
        CommandLineParser parser = new GnuParser();
        CommandLine cli = parser.parse(CommandLineParam.options(), args);

        factory.getExportFormatCommand(cli);
        
    }
    
}
