/**
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
package uk.gov.nationalarchives.droid.command;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.nationalarchives.droid.command.action.CheckSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.CommandFactory;
import uk.gov.nationalarchives.droid.command.action.CommandLineException;
import uk.gov.nationalarchives.droid.command.action.ConfigureDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DisplayDefaultSignatureFileVersionCommand;
import uk.gov.nationalarchives.droid.command.action.DownloadSignatureUpdateCommand;
import uk.gov.nationalarchives.droid.command.action.ExportCommand;
import uk.gov.nationalarchives.droid.command.action.FilterFieldCommand;
import uk.gov.nationalarchives.droid.command.action.ListAllSignatureFilesCommand;
import uk.gov.nationalarchives.droid.command.action.ProfileRunCommand;
import uk.gov.nationalarchives.droid.command.action.ReportCommand;
import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;
import uk.gov.nationalarchives.droid.core.interfaces.filter.BasicFilter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rflitcroft
 *
 */
public class DroidCommandLineTest {

    private PrintWriter printWriter;
    private GlobalContext context;
    
    @Before
    public void setup() {
        printWriter = mock(PrintWriter.class);
        context = mock(GlobalContext.class);
        RuntimeConfig.configureRuntimeEnvironment();
    }
    
    @Test
    public void testParseNonsense() throws CommandLineException{
        String[] args = new String[] {
            "--zzzzzz"
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, printWriter);
        
        //a return code of 1 denotes a failure
        Assert.assertEquals(1, droidCommandLine.processExecution());
        verify(printWriter).println("Incorrect command line syntax: Unrecognized option: --zzzzzz");
    }
    
    @Ignore
    @Test
    public void testVersionShort() throws CommandLineException {
        String[] args = new String[] {
            "-v"
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.processExecution();
        
        verify(printWriter).println("5.0.3-beta");
    }

    @Ignore
    @Test
    public void testVersionLong() throws CommandLineException {
        String[] args = new String[] {
            "-version",
        };
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.processExecution();
        
        verify(printWriter).println("5.0.3-beta");
    }
    
    @Test
    @Ignore
    public void testHelpShort() throws CommandLineException {
        String[] args = new String[] {
            "-h"
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.processExecution();
        
        verify(printWriter).println("usage: droid [options]");
        verify(printWriter).println("OPTIONS:");
        TestUtil.verifyHelpOptions(printWriter);
    }

    @Test
    @Ignore
    public void testHelpLong() throws CommandLineException {
        String[] args = new String[] {
            "-help"
        };
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.processExecution();
        
        verify(printWriter).println("usage: droid [options]");
        verify(printWriter).println("OPTIONS:");
        TestUtil.verifyHelpOptions(printWriter);
    }
    
    @Test
    //@Ignore  //BNO
    public void testExportWith3Profiles() throws Exception {
        String[] args = new String[] {
            "-E",
            "test.csv",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid"
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.setContext(context);
        
        
        droidCommandLine.processExecution();
        
        verify(command).execute();
        
    }

    @Test
    public void testExportWith3ProfilesOutputToStdOut() throws Exception {
        String[] args = new String[] {
            "-E",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid"
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        
        droidCommandLine.setContext(context);
        
        droidCommandLine.processExecution();
        
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
            "puid = 'fmt/101'"
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        
        droidCommandLine.setContext(context);
        
        droidCommandLine.processExecution();
        
        verify(command).setDestination("out.csv");
        verify(command).setProfiles(new String[] {
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid"
        });
        
        ArgumentCaptor<Filter> filterCaptor = ArgumentCaptor.forClass(BasicFilter.class);
        verify(command).setFilter(filterCaptor.capture());
        verify(command).execute();
        
        Filter filter = filterCaptor.getValue();
        assertTrue(filter.isNarrowed());

        List<FilterCriterion> list = filter.getCriteria();
        assertEquals(2, list.size());

        FilterCriterion first = list.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, first.getField());
        assertEquals(CriterionOperator.EQ, first.getOperator());
        assertEquals(720L, first.getValue());

        FilterCriterion second = list.get(1);
        assertEquals(CriterionFieldEnum.PUID, second.getField());
        assertEquals(CriterionOperator.EQ, second.getOperator());
        assertEquals("fmt/101", second.getValue());
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
            "puid = 'fmt/101'"
        };
        
        ExportCommand command = mock(ExportCommand.class);
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        
        droidCommandLine.setContext(context);
        
        droidCommandLine.processExecution();
        
        verify(command).setDestination("out.csv");
        verify(command).setProfiles(new String[] {
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid"
        });
        
        ArgumentCaptor<Filter> filterCaptor = ArgumentCaptor.forClass(BasicFilter.class);
        verify(command).setFilter(filterCaptor.capture());
        verify(command).execute();
        
        Filter filter = filterCaptor.getValue();
        assertFalse(filter.isNarrowed());

        List<FilterCriterion> list = filter.getCriteria();
        assertEquals(2, list.size());

        FilterCriterion first = list.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, first.getField());
        assertEquals(CriterionOperator.EQ, first.getOperator());
        assertEquals(720L, first.getValue());

        FilterCriterion second = list.get(1);
        assertEquals(CriterionFieldEnum.PUID, second.getField());
        assertEquals(CriterionOperator.EQ, second.getOperator());
        assertEquals("fmt/101", second.getValue());

    }

    @Test
    public void testHelpAndVersionIsNotValidCombination() throws Exception {
        
        String[] args = new String[] {
            "-h",
            "-v"
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, printWriter);
        
        droidCommandLine.setContext(context);

        droidCommandLine.processExecution();
        verify(printWriter).println("Incorrect command line syntax: The option 'v' was specified but an " +
                "option from this group has already been selected: 'h'");
    }
    
    @Test
    public void testReport() throws Exception {
        String[] args = new String[] {
            "-r",
            "out.xml",
            "-p",
            "profile1.droid",
            "-n",
            "planets"
        };
        
        ReportCommand command = mock(ReportCommand.class);
        when(context.getReportCommand()).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        
        droidCommandLine.setContext(context);
        
        droidCommandLine.processExecution();
        
        verify(command).execute();
    }
    
    @Test
    //@Ignore //BNO
    public void testFilterFieldNames() throws Exception {
        String[] args = new String[] {
            "-k"
        };
        
        FilterFieldCommand command = mock(FilterFieldCommand.class);
        CommandFactory factory = mock(CommandFactory.class);
        when(factory.getFilterFieldCommand()).thenReturn(command);
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        
        droidCommandLine.setContext(context);
        
        droidCommandLine.processExecution();
        
        //verify(command).execute();
        
    }
    
    @Test
    public void testRunAndSaveProfileToFile() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "file1.txt",
            "file/number/2.txt",
            "-p",
            "test"
        };
        
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        when(context.getProfileRunCommand()).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        
        droidCommandLine.setContext(context);
        
        droidCommandLine.processExecution();
        
        verify(command).setDestination("test");
        verify(command).setResources(new String[] {
            "file1.txt",
            "file/number/2.txt"
        });
    }
    
    @Test
    public void testRunAndSaveProfileToMultipleFiles() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "file1.txt",
            "file/number/2.txt",
            "-p",
            "test1",
            "test2"
        };
        
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        when(context.getProfileRunCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, printWriter);
        commandLine.processExecution();

        verify(printWriter).println("Incorrect command line syntax: Must specify exactly one profile.");
    }

    @Test
    public void testRunAndSaveProfileToFileWithRecursion() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "file1.txt",
            "file/number/2.txt",
            "-p",
            "test",
            "-R"
        };
        
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        
        when(context.getProfileRunCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.setContext(context);
        
        commandLine.processExecution();
        
        verify(command).setDestination("test");
        verify(command).setResources(new String[] {
            "file1.txt",
            "file/number/2.txt"
        });
        verify(command).setRecursive(true);
    }

    @Test
    @Ignore("New code allows not supplying a profile name now (goes to console as CSV instead).  Write test for that?")
    public void testRunAndSaveProfileWithNoProfileName() throws CommandLineException {
        String[] args = new String[]{
                "-a",
                "file1.txt",
                "file/number/2.txt"
        };

        DroidCommandLine commandLine = new DroidCommandLine(args, printWriter);
        commandLine.processExecution();
        verify(printWriter).println("Incorrect command line syntax: Must specify exactly one profile.");
    }

    @Test
    public void testRunAndSaveProfileWithNoResources() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "-p",
            "test.droid"
        };
        
        DroidCommandLine commandLine = new DroidCommandLine(args, printWriter);
        commandLine.processExecution();
        verify(printWriter).println("Incorrect command line syntax: Missing argument for option: a");
    }

    
    @Test
    public void testFilterHelp() throws CommandLineException {
        String[] args = new String[] {
            "-k"
        };

        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.processExecution();
    }

    @Test
    public void testQuietMode() throws CommandLineException {
        String[] args = new String[] {
        	"-q",
            "-a",
            "file1.txt",
            "-p",
            "test.droid"
        };
        
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        
        when(context.getProfileRunCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.setContext(context);

        commandLine.processExecution();
    }
    
    @Test
    public void testCheckForNewSignatureFile() throws CommandLineException {
        
        String[] args = new String[] {
            "-c",
        };
        
        CheckSignatureUpdateCommand command = mock(CheckSignatureUpdateCommand.class);
        when(context.getCheckSignatureUpdateCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.setContext(context);
        
        commandLine.processExecution();
    }

    @Ignore
    @Test
    public void testDownloadLatestSignatureFile() throws CommandLineException {
        
        String[] args = new String[] {
            "-r"
        };
        
        DownloadSignatureUpdateCommand command = mock(DownloadSignatureUpdateCommand.class);
        when(context.getDownloadSignatureUpdateCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.setContext(context);
        
        commandLine.processExecution();
    }

    @Test
    public void testDisplayDefaultSignatureFileVersion() throws CommandLineException {
        
        String[] args = new String[] {
            "-x"
        };
        
        DisplayDefaultSignatureFileVersionCommand command = mock(DisplayDefaultSignatureFileVersionCommand.class);
        when(context.getDisplayDefaultSignatureFileVersionCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.setContext(context);
        
        commandLine.processExecution();
    }

    @Test
    public void testDisplayAllSignatureFiles() throws CommandLineException {
        
        String[] args = new String[] {
            "-X"
        };
        
        ListAllSignatureFilesCommand command = mock(ListAllSignatureFilesCommand.class);
        when(context.getListAllSignatureFilesCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.processExecution();
    }

    @Test
    public void testConfigureDefaultSignatureFileVersion() throws CommandLineException {
        
        String[] args = new String[] {
            "-s 99"
        };
        
        ConfigureDefaultSignatureFileVersionCommand command = mock(ConfigureDefaultSignatureFileVersionCommand.class);
        when(context.getConfigureDefaultSignatureFileVersionCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.setContext(context);
        
        commandLine.processExecution();
        
        verify(command).setSignatureFileVersion(99);
    }

    @Test
    public void testConfigureDefaultSignatureFileVersionWithMissingValue() throws CommandLineException {
        
        String[] args = new String[] {
            "-s"
        };
        
        ConfigureDefaultSignatureFileVersionCommand command = mock(ConfigureDefaultSignatureFileVersionCommand.class);
        when(context.getConfigureDefaultSignatureFileVersionCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args, printWriter);
        commandLine.processExecution();
        verify(printWriter).println("Incorrect command line syntax: Missing argument for option: s");
    }

}
