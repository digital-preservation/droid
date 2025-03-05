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
package uk.gov.nationalarchives.droid.command;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.matchers.Equals;
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
import uk.gov.nationalarchives.droid.export.interfaces.ExportOutputOptions;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        ArgumentCaptor<StringBuilder> stringBuilderArgumentCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter).println(stringBuilderArgumentCaptor.capture());

        Assert.assertEquals(stringBuilderArgumentCaptor.getValue().toString(), "Incorrect command line syntax: Unrecognized option: --zzzzzz");
    }

    @Test
    public void testParseInvalidString() throws CommandLineException{
        String[] args = new String[] {
                "NotACommand"
        };

        DroidCommandLine droidCommandLine = new DroidCommandLine(args, printWriter);

        //a return code of 1 denotes a failure
        Assert.assertEquals(1, droidCommandLine.processExecution());

        ArgumentCaptor<StringBuilder> stringBuilderCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter).println(stringBuilderCaptor.capture());

        Assert.assertEquals(stringBuilderCaptor.getValue().toString(), "An unknown error occurred: Unknown location [NotACommand]");
    }
    
    @Test
    public void testVersionShort() throws CommandLineException {
        String[] args = new String[] {
            "-v"
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.setPrintWriter(printWriter);
        droidCommandLine.processExecution();
        
        verify(printWriter).println(any(StringBuilder.class));
    }

    @Test
    public void testVersionLong() throws CommandLineException {
        String[] args = new String[] {
            "-version",
        };
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.setPrintWriter(printWriter);
        droidCommandLine.processExecution();
        
        verify(printWriter).println(any(StringBuilder.class));
    }
    
    @Test
    public void testHelpShort() throws CommandLineException {
        String[] args = new String[] {
            "-h"
        };
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.setPrintWriter(printWriter);
        droidCommandLine.processExecution();

        ArgumentCaptor<StringBuilder> stringBuilderCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter, times(13)).println(stringBuilderCaptor.capture());

        List<StringBuilder> allValues = stringBuilderCaptor.getAllValues();
        Assert.assertEquals(allValues.getFirst().toString(), "usage: droid [options]");
        Assert.assertEquals(allValues.get(1).toString(), "OPTIONS:");
    }

    @Test
    public void testHelpLong() throws CommandLineException {
        String[] args = new String[] {
            "-help"
        };
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.setPrintWriter(printWriter);
        droidCommandLine.processExecution();

        ArgumentCaptor<StringBuilder> stringBuilderCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter, times(13)).println(stringBuilderCaptor.capture());

        List<StringBuilder> allValues = stringBuilderCaptor.getAllValues();
        Assert.assertEquals(allValues.getFirst().toString(), "usage: droid [options]");
        Assert.assertEquals(allValues.get(1).toString(), "OPTIONS:");
    }
    
    @Test
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
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT, ExportOutputOptions.CSV_OUTPUT)).thenReturn(command);

        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.setContext(context);
        droidCommandLine.processExecution();

        // Validate that the profiles were set correctly.
        ArgumentCaptor<String[]> profileCaptor = ArgumentCaptor.forClass(String[].class);
        verify(command).setProfiles(profileCaptor.capture());
        String[] setProfiles = profileCaptor.getValue();
        assertEquals("tmp/profile 1.droid", setProfiles[0]);
        assertEquals("tmp/profile-2.droid", setProfiles[1]);
        assertEquals("tmp/profile-3.droid", setProfiles[2]);

        // Validate that the export command was executed.
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
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT, ExportOutputOptions.CSV_OUTPUT)).thenReturn(command);
        
        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        
        droidCommandLine.setContext(context);
        
        droidCommandLine.processExecution();

        // Validate that the profiles were set correctly.
        ArgumentCaptor<String[]> profileCaptor = ArgumentCaptor.forClass(String[].class);
        verify(command).setProfiles(profileCaptor.capture());
        String[] setProfiles = profileCaptor.getValue();
        assertEquals("tmp/profile 1.droid", setProfiles[0]);
        assertEquals("tmp/profile-2.droid", setProfiles[1]);
        assertEquals("tmp/profile-3.droid", setProfiles[2]);

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
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT, ExportOutputOptions.CSV_OUTPUT)).thenReturn(command);
        
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
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT, ExportOutputOptions.CSV_OUTPUT)).thenReturn(command);
        
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

        ArgumentCaptor<StringBuilder> stringBuilderCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter).println(stringBuilderCaptor.capture());

        Assert.assertEquals(stringBuilderCaptor.getValue().toString(), "Incorrect command line syntax: The option 'v' was specified but an " +
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

        verify(command).setProfiles(new String[] {"profile1.droid"});
        verify(command).setReportType("planets");
        verify(command).setDestination("out.xml");
        verify(command).execute();
    }

    @Test
    public void testFilterFieldNames() throws Exception {
        String[] args = new String[] {
            "-k"
        };
        
        FilterFieldCommand command = mock(FilterFieldCommand.class);
        CommandFactory factory = mock(CommandFactory.class);
        when(factory.getFilterFieldCommand()).thenReturn(command);

        DroidCommandLine droidCommandLine = new DroidCommandLine(args);
        droidCommandLine.setCommandFactory(factory);
        droidCommandLine.setContext(context);
        droidCommandLine.processExecution();
        
        verify(command).execute();
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
    public void testRunAndSaveProfileToFileNoAOption() throws CommandLineException {
        String[] args = new String[] {
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
    public void testRunAndSaveProfileNoArgs() throws CommandLineException {
        String[] args = new String[] {
                "-p",
                "test"
        };
        ProfileRunCommand command = mock(ProfileRunCommand.class);
        when(context.getProfileRunCommand()).thenReturn(command);
        DroidCommandLine droidCommandLine = new DroidCommandLine(args, printWriter);
        droidCommandLine.setContext(context);
        droidCommandLine.processExecution();
        ArgumentCaptor<StringBuilder> argCaptor = ArgumentCaptor.forClass(StringBuilder.class);
        verify(printWriter).println(argCaptor.capture());
        StringBuilder value = argCaptor.getValue();
        assertTrue(value.toString().startsWith("Incorrect command line syntax: No actionable command line options specified (use -h to see all available options): -p"));
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

        ArgumentCaptor<StringBuilder> stringBuilderCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter).println(stringBuilderCaptor.capture());
        Assert.assertEquals(stringBuilderCaptor.getValue().toString(), "Incorrect command line syntax: Must specify exactly one profile.");
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
    public void testRunAndSaveProfileWithNoResources() throws CommandLineException {
        String[] args = new String[] {
            "-a",
            "-p",
            "test.droid"
        };
        
        DroidCommandLine commandLine = new DroidCommandLine(args, printWriter);
        commandLine.processExecution();

        ArgumentCaptor<StringBuilder> stringBuilderCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter).println(stringBuilderCaptor.capture());
        Assert.assertEquals(stringBuilderCaptor.getValue().toString(), "Incorrect command line syntax: Missing argument for option: a");
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

    @Test
    public void testDownloadLatestSignatureFile() throws CommandLineException {
        
        String[] args = new String[] {
            "-d"
        };
        
        DownloadSignatureUpdateCommand command = mock(DownloadSignatureUpdateCommand.class);
        when(context.getDownloadSignatureUpdateCommand()).thenReturn(command);
        
        DroidCommandLine commandLine = new DroidCommandLine(args);
        commandLine.setContext(context);
        
        commandLine.processExecution();
        verify(command).execute();
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

        ArgumentCaptor<StringBuilder> stringBuilderCaptor = ArgumentCaptor.forClass(StringBuilder.class);

        verify(printWriter).println(stringBuilderCaptor.capture());

        Assert.assertEquals(stringBuilderCaptor.getValue().toString(), "Incorrect command line syntax: Missing argument for option: s");
    }

    @Test
    public void testGetSetCommandFactory() {
        DroidCommandLine commandLine = new DroidCommandLine(new String[0]);
        assertNull(commandLine.getCommandFactory());
        CommandFactory factory = mock(CommandFactory.class);
        commandLine.setCommandFactory(factory);
        assertEquals(factory, commandLine.getCommandFactory());
    }

    @Test
    public void testGetSetPrintWriter() {
        DroidCommandLine commandLine = new DroidCommandLine(new String[0]);
        assertNull(commandLine.getPrintWriter());
        PrintWriter writer = mock(PrintWriter.class);
        commandLine.setPrintWriter(writer);
        assertEquals(writer, commandLine.getPrintWriter());
    }

    @Test
    public void testGetSetContext() {
        DroidCommandLine commandLine = new DroidCommandLine(new String[0]);
        assertNull(commandLine.getContext());
        GlobalContext context = mock(GlobalContext.class);
        commandLine.setContext(context);
        assertEquals(context, commandLine.getContext());
    }


}
