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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.command.action.CheckSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.CommandFactory;
import uk.gov.nationalarchives.droid.command.action.CommandFactoryImpl;
import uk.gov.nationalarchives.droid.command.action.CommandLineException;
import uk.gov.nationalarchives.droid.command.action.CommandLineSyntaxException;
import uk.gov.nationalarchives.droid.command.action.ConfigureDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DisplayDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DownloadSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ExportCommand;
import uk.gov.nationalarchives.droid.command.action.ListAllSignatureFilesCommand;
import uk.gov.nationalarchives.droid.command.action.ProfileRunCommand;
import uk.gov.nationalarchives.droid.command.action.ReportCommand;
import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter;
import uk.gov.nationalarchives.droid.command.filter.CommandLineFilter.FilterType;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;

/**
 * @author rflitcroft
 *
 */
public class DroidCommandLineTest {

    private CommandFactory commandFactory;
    private PrintWriter printWriter;
    private GlobalContext context;
    
    @Before
    public void setup() {
        printWriter = mock(PrintWriter.class);
        context = mock(GlobalContext.class);
        commandFactory = new CommandFactoryImpl(context, printWriter);
    }
    
    @Test(expected = CommandLineSyntaxException.class)
    public void testParseNonsense() throws Exception {
        String[] args = new String[] {
            "--zzzzzz",
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
    }
    
    @Ignore
    @Test
    public void testVersionShort() throws CommandLineException {
        String[] args = new String[] {
            "-v",
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(printWriter).println("5.0.3-beta");
    }

    @Ignore
    @Test
    public void testVersionLong() throws CommandLineException {
        String[] args = new String[] {
            "-version",
        };
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(printWriter).println("5.0.3-beta");
    }
    
    @Test
    @Ignore
    public void testHelpShort() throws CommandLineException {
        String[] args = new String[] {
            "-h",
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(printWriter).println("usage: droid [options]");
        verify(printWriter).println("OPTIONS:");
        TestUtil.verifyHelpOptions(printWriter);
    }

    @Test
    @Ignore
    public void testHelpLong() throws CommandLineException {
        String[] args = new String[] {
            "-help",
        };
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(printWriter).println("usage: droid [options]");
        verify(printWriter).println("OPTIONS:");
        TestUtil.verifyHelpOptions(printWriter);
    }
    
    @Test
    public void testExportWith3Profiles() throws Exception {
        String[] args = new String[] {
            "-E",
            "test.csv",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(command).execute();
        
    }

    @Test
    public void testExportWith3ProfilesOutputToStdOut() throws Exception {
        String[] args = new String[] {
            "-E",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(command).execute();
        
    }

    @Test
    public void testExportWith3ProfilesWithNarrowingFilter() throws Exception {
        String[] args = new String[] {
            "-E",
            "out.csv",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
            "-f",
            "file_size = 720",
            "puid = 'fmt/101'",
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(command).setDestination("out.csv");
        verify(command).setProfiles(new String[] {
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        });
        
        ArgumentCaptor<CommandLineFilter> filterCaptor = ArgumentCaptor.forClass(CommandLineFilter.class);
        verify(command).setFilter(filterCaptor.capture());
        verify(command).execute();
        
        CommandLineFilter filter = filterCaptor.getValue();
        assertArrayEquals(new String[] {
            "file_size = 720",
            "puid = 'fmt/101'",
        }, filter.getFilters());
        
        assertEquals(FilterType.ALL, filter.getFilterType());
        
    }

    @Test
    public void testExportWith3ProfilesWithWideFilter() throws Exception {
        String[] args = new String[] {
            "-E",
            "out.csv",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
            "-F",
            "file_size = 720",
            "puid = 'fmt/101'",
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(command).setDestination("out.csv");
        verify(command).setProfiles(new String[] {
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid",
        });
        
        ArgumentCaptor<CommandLineFilter> filterCaptor = ArgumentCaptor.forClass(CommandLineFilter.class);
        verify(command).setFilter(filterCaptor.capture());
        verify(command).execute();
        
        CommandLineFilter filter = filterCaptor.getValue();
        assertArrayEquals(new String[] {
            "file_size = 720",
            "puid = 'fmt/101'",
        }, filter.getFilters());
        
        assertEquals(FilterType.ANY, filter.getFilterType());
        
    }

    @Test(expected = CommandLineException.class)
    public void testHelpAndVersionIsNotValidCombination() throws Exception {
        
        String[] args = new String[] {
            "-h",
            "-v",
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
    }
    
    @Test
    public void testReport() throws Exception {
        String[] args = new String[] {
            "-r",
            "out.xml",
            "-p",
            "profile1.droid",
            "-n",
            "planets",
        };
        
        ReportCommand command = mock(ReportCommand.class);
        when(context.getReportCommand()).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, commandFactory);
        droidCommandLine.run();
        
        verify(command).execute();
    }
    
    @Test
    public void testFilterFieldNames() throws Exception {
        String[] args = new String[] {
            "-k",
        };
        
        FilterFieldCommand command = mock(FilterFieldCommand.class);
        CommandFactory factory = mock(CommandFactory.class);
        when(factory.getFilterFieldCommand()).thenReturn(command);
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, factory);
        droidCommandLine.run();
        
        verify(command).execute();
        
    }
    
    @Test
    public void testRunAndSaveProfileToFile() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "file1.txt",
            "file/number/2.txt",
            "-p",
            "test",
        };
        
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        when(context.getProfileRunCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
        
        verify(command).setDestination("test");
        verify(command).setResources(new String[] {
            "file1.txt",
            "file/number/2.txt",
        });
    }
    
    @Test(expected = CommandLineException.class)
    public void testRunAndSaveProfileToMultipleFiles() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "file1.txt",
            "file/number/2.txt",
            "-p",
            "test1",
            "test2",
        };
        
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        when(context.getProfileRunCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
        
    }

    @Test
    public void testRunAndSaveProfileToFileWithRecursion() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "file1.txt",
            "file/number/2.txt",
            "-p",
            "test",
            "-R",
        };
        
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        when(context.getProfileRunCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
        
        verify(command).setDestination("test");
        verify(command).setResources(new String[] {
            "file1.txt",
            "file/number/2.txt",
        });
        verify(command).setRecursive(true);
    }

    @Test(expected = CommandLineException.class)
    public void testRunAndSaveProfileWithNoProfileName() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "file1.txt",
            "file/number/2.txt",
        };
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();

    }

    @Test(expected = CommandLineException.class)
    public void testRunAndSaveProfileWithNoResources() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "-p",
            "test.droid",
        };
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();

    }

    @Test
    public void testMain() {
        String[] args = new String[] {
            "-zzz",
        };
        DroidCommandLine.main(args);
    }
    
    @Test
    public void testFilterHelp() {
        String[] args = new String[] {
            "-k",
        };
        DroidCommandLine.main(args);
    }

    @Test
    public void testQuietMode() {
        String[] args = new String[] {
            "-q",
        };
        DroidCommandLine.main(args);
    }
    
    @Test
    public void testCheckForNewSignatureFile() throws CommandLineException {
        
        String[] args = new String[] {
            "-c",
        };
        
        CheckSignatureUpdateCommand command = mock(CheckSignatureUpdateCommand.class);
        when(context.getCheckSignatureUpdateCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
    }

    @Test
    public void testDownloadLatestSignatureFile() throws CommandLineException {
        
        String[] args = new String[] {
            "-d",
        };
        
        DownloadSignatureUpdateCommand command = mock(DownloadSignatureUpdateCommand.class);
        when(context.getDownloadSignatureUpdateCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
    }

    @Test
    public void testDisplayDefaultSignatureFileVersion() throws CommandLineException {
        
        String[] args = new String[] {
            "-x",
        };
        
        DisplayDefaultSignatureFileVersionCommand command = mock(DisplayDefaultSignatureFileVersionCommand.class);
        when(context.getDisplayDefaultSignatureFileVersionCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
    }

    @Test
    public void testDisplayAllSignatureFiles() throws CommandLineException {
        
        String[] args = new String[] {
            "-X",
        };
        
        ListAllSignatureFilesCommand command = mock(ListAllSignatureFilesCommand.class);
        when(context.getListAllSignatureFilesCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
    }

    @Test
    public void testConfigureDefaultSignatureFileVersion() throws CommandLineException {
        
        String[] args = new String[] {
            "-s 99",
        };
        
        ConfigureDefaultSignatureFileVersionCommand command = mock(ConfigureDefaultSignatureFileVersionCommand.class);
        when(context.getConfigureDefaultSignatureFileVersionCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
        
        verify(command).setSignatureFileVersion(99);
    }

    @Test(expected = CommandLineSyntaxException.class)
    public void testConfigureDefaultSignatureFileVersionWithMissingValue() throws CommandLineException {
        
        String[] args = new String[] {
            "-s",
        };
        
        ConfigureDefaultSignatureFileVersionCommand command = mock(ConfigureDefaultSignatureFileVersionCommand.class);
        when(context.getConfigureDefaultSignatureFileVersionCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, commandFactory);
        commandLine.run();
    }
}
