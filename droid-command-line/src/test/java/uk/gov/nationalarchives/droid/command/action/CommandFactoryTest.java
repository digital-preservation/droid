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
