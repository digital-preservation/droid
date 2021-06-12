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
package uk.gov.nationalarchives.droid.command.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.context.annotation.Profile;
import uk.gov.nationalarchives.droid.command.context.GlobalContext;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceType;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionFieldEnum;
import uk.gov.nationalarchives.droid.core.interfaces.filter.CriterionOperator;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.filter.FilterCriterion;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.report.interfaces.ReportManager;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpecItem;

/**
 * @author rflitcroft
 *
 */
public class CommandFactoryTest {

    private CommandFactoryImpl factory;
    private GlobalContext context;
    private PrintWriter printWriter;

    private ExportCommand exportCommand;
    private ReportCommand reportCommand;
    private ProfileRunCommand profileRunCommand;
    private ListReportsCommand listReportsCommand;
    private ListAllSignatureFilesCommand listAllSignatureFiles;
    private String[] expectedProfiles = {
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid"
    };

    @Before
    public void setup() {
        exportCommand = new ExportCommand();
        reportCommand = new ReportCommand();
        profileRunCommand = new ProfileRunCommand();
        listReportsCommand = new ListReportsCommand();

        context = mock(GlobalContext.class);
        printWriter = mock(PrintWriter.class);
        factory = new CommandFactoryImpl(context, printWriter);
    }


    @Test
    public void testExportCommand() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-E",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFormatCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(expectedProfiles, e1.getProfiles());
        assertFalse(e1.isBom());
        assertEquals("out.csv", e1.getDestination());
    }

    @Test
    public void testExportCommandWithBom() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-B",
                "-E",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFormatCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertTrue(e1.isBom());
        assertEquals("out.csv", e1.getDestination());
    }


    @Test
    public void testExportCommandWithNoExportArgument() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-E",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFormatCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertNull(e1.getDestination());
    }

    @Test (expected = CommandLineSyntaxException.class)
    public void testExportCommandWithNoProfiles() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-E"
        };
        CommandLine cli = parse(args);

        factory.getExportFormatCommand(cli);
    }

    @Test
    public void testExportCommandFile() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE)).thenReturn(exportCommand);
        String[] args = new String[] {
            "-e",
            "out.csv",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFileCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertFalse(e1.isBom());
        assertEquals("out.csv", e1.getDestination());
    }

    @Test
    public void testExportCommandWithBomFile() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-B",
                "-e",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFileCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertTrue(e1.isBom());
        assertEquals("out.csv", e1.getDestination());
    }

    
    @Test
    public void testExportCommandWithNoExportArgumentFile() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE)).thenReturn(exportCommand);
        String[] args = new String[] {
            "-e",
            "-p",
            "tmp/profile 1.droid",
            "tmp/profile-2.droid",
            "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFileCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertNull(exportCommand.getDestination());
    }
    
    @Test (expected = CommandLineSyntaxException.class)
    public void testExportCommandWithNoProfilesFile() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE)).thenReturn(exportCommand);
        String[] args = new String[] {
            "-e",
        };
        CommandLine cli = parse(args);

        factory.getExportFileCommand(cli);
    }

    @Test
    public void testExportCommandWithColumns() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-E",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-co",
                "FILE_PATH NAME METHOD PUID"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFormatCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("out.csv", e1.getDestination());
        assertEquals("FILE_PATH NAME METHOD PUID", e1.getColumnsToWrite());
        assertTrue(e1.getQuoteAllFields());
    }

    @Test
    public void testExportCommandQuoteCommasOnly() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FORMAT)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-E",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-qc",
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFormatCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("out.csv", e1.getDestination());
        assertFalse(e1.getQuoteAllFields());
    }

    @Test
    public void testExportCommandFileWithColumns() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-e",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-co",
                "FILE_PATH NAME METHOD PUID"
        };
        CommandLine cli = parse(args);
        ExportCommand e1 = (ExportCommand) factory.getExportFileCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("out.csv", e1.getDestination());
        assertEquals("FILE_PATH NAME METHOD PUID", e1.getColumnsToWrite());
    }

    @Test
    public void testReportCommand() throws Exception {
        when(context.getReportCommand()).thenReturn(reportCommand);
        String[] args = new String[] {
                "-r",
                "tmp/resultreport.pdf",
                "-n",
                "File count and sizes",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        ReportCommand e1 = (ReportCommand) factory.getReportCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("tmp/resultreport.pdf", e1.getDestination());
        assertEquals("File count and sizes", e1.getReportType());
        assertEquals("pdf", e1.getReportOutputType());
    }

    @Test
    public void testAnyFilteredReportCommand() throws Exception {
        when(context.getReportCommand()).thenReturn(reportCommand);
        String[] args = new String[] {
                "-r",
                "tmp/resultreport.pdf",
                "-n",
                "File count and sizes",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-F",
                "file_size > 10000",
                "puid any fmt/101 fmt/102 fmt/103"
        };
        CommandLine cli = parse(args);

        assertNull(reportCommand.getFilter());
        ReportCommand e1 = (ReportCommand) factory.getReportCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("tmp/resultreport.pdf", e1.getDestination());
        assertEquals("File count and sizes", e1.getReportType());
        assertEquals("pdf", e1.getReportOutputType());
        Filter filter = e1.getFilter();
        assertEquals(false, filter.isNarrowed());

        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(2, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());

        FilterCriterion crit2 = criterionList.get(1);
        assertEquals(CriterionFieldEnum.PUID, crit2.getField());
        assertEquals(CriterionOperator.ANY_OF, crit2.getOperator());
        Object[] values = (Object[]) crit2.getValue();
        assertEquals(3, values.length);
        assertEquals("fmt/101", values[0]);
        assertEquals("fmt/102", values[1]);
        assertEquals("fmt/103", values[2]);
    }

    @Test
    public void testAllFilteredExportCommand() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-e",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-f",
                "file_size > 10000",
                "puid any fmt/101 fmt/102 fmt/103"
        };
        CommandLine cli = parse(args);

        assertNull(exportCommand.getFilter());
        ExportCommand e1 = (ExportCommand) factory.getExportFileCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("out.csv", e1.getDestination());

        Filter filter = e1.getFilter();
        assertNotNull(filter);
        assertEquals(true, filter.isNarrowed());
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(2, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());

        FilterCriterion crit2 = criterionList.get(1);
        assertEquals(CriterionFieldEnum.PUID, crit2.getField());
        assertEquals(CriterionOperator.ANY_OF, crit2.getOperator());
        Object[] values = (Object[]) crit2.getValue();
        assertEquals(3, values.length);
        assertEquals("fmt/101", values[0]);
        assertEquals("fmt/102", values[1]);
        assertEquals("fmt/103", values[2]);
    }

    @Test
    public void testAnyFilteredExportCommand() throws Exception {
        when(context.getExportCommand(ExportOptions.ONE_ROW_PER_FILE)).thenReturn(exportCommand);
        String[] args = new String[] {
                "-e",
                "out.csv",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-F",
                "file_size > 10000",
                "puid any fmt/101 fmt/102 fmt/103"
        };
        CommandLine cli = parse(args);

        assertNull(exportCommand.getFilter());
        ExportCommand e1 = (ExportCommand) factory.getExportFileCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("out.csv", e1.getDestination());

        Filter filter = e1.getFilter();
        assertEquals(false, filter.isNarrowed());

        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(2, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());

        FilterCriterion crit2 = criterionList.get(1);
        assertEquals(CriterionFieldEnum.PUID, crit2.getField());
        assertEquals(CriterionOperator.ANY_OF, crit2.getOperator());
        Object[] values = (Object[]) crit2.getValue();
        assertEquals(3, values.length);
        assertEquals("fmt/101", values[0]);
        assertEquals("fmt/102", values[1]);
        assertEquals("fmt/103", values[2]);
    }

    @Test
    public void testAllFilteredReportCommand() throws Exception {
        when(context.getReportCommand()).thenReturn(reportCommand);
        String[] args = new String[] {
                "-r",
                "tmp/resultreport.pdf",
                "-n",
                "File count and sizes",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-f",
                "file_size > 10000",
                "puid any fmt/101 fmt/102 fmt/103"
        };
        CommandLine cli = parse(args);

        assertNull(reportCommand.getFilter());
        ReportCommand e1 = (ReportCommand) factory.getReportCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("tmp/resultreport.pdf", e1.getDestination());
        assertEquals("File count and sizes", e1.getReportType());
        assertEquals("pdf", e1.getReportOutputType());
        Filter filter = e1.getFilter();
        assertNotNull(filter);
        assertEquals(true, filter.isNarrowed());
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(2, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());

        FilterCriterion crit2 = criterionList.get(1);
        assertEquals(CriterionFieldEnum.PUID, crit2.getField());
        assertEquals(CriterionOperator.ANY_OF, crit2.getOperator());
        Object[] values = (Object[]) crit2.getValue();
        assertEquals(3, values.length);
        assertEquals("fmt/101", values[0]);
        assertEquals("fmt/102", values[1]);
        assertEquals("fmt/103", values[2]);
    }


    @Test
    public void testReportSetReportTypeCommand() throws Exception {
        when(context.getReportCommand()).thenReturn(reportCommand);
        String[] args = new String[] {
                "-r",
                "tmp/resultreport.pdf",
                "-n",
                "File count and sizes",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid",
                "-t",
                "testReportType"
        };
        CommandLine cli = parse(args);
        ReportCommand e1 = (ReportCommand) factory.getReportCommand(cli);

        assertNotNull(e1);
        assertArrayEquals(e1.getProfiles(), expectedProfiles);
        assertEquals("tmp/resultreport.pdf", e1.getDestination());
        assertEquals("File count and sizes", e1.getReportType());
        assertEquals("testReportType", e1.getReportOutputType());
    }

    @Test(expected=CommandLineSyntaxException.class)
    public void testReportCommandWithNoProfiles() throws Exception {
        when(context.getReportCommand()).thenReturn(reportCommand);
        String[] args = new String[] {
                "-r",
                "tmp/resultreport.pdf",
                "-n",
                "File count and sizes"
        };
        CommandLine cli = parse(args);
        factory.getReportCommand(cli);
    }

    @Test(expected=CommandLineSyntaxException.class)
    public void testReportCommandWithNoReportType() throws Exception {
        when(context.getReportCommand()).thenReturn(reportCommand);
        String[] args = new String[] {
                "-r",
                "tmp/resultreport.pdf",
                "-p",
                "tmp/profile 1.droid",
                "tmp/profile-2.droid",
                "tmp/profile-3.droid"
        };
        CommandLine cli = parse(args);
        factory.getReportCommand(cli);
    }

    private CommandLine parse(String[] args) throws Exception {
        return new GnuParser().parse(CommandLineParam.options(), args);
    }

    @Test
    public void testAddProfileResourcesOnly() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
    }

    @Test
    public void testAddProfileRecursiveResources() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
    }

    @Test
    public void testAddProfileRecursiveResourcesQuoteCommas() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-qc"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(3, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertTrue(propertyNames.contains("profile.quoteAllFields"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
        assertEquals(Boolean.FALSE, profileRunCommand.getProperties().getProperty("profile.quoteAllFields"));
    }

    @Test
    public void testAddImplicitProfileRecursiveResources() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] { // if the -a option is omitted, the software will take any ubound arguments as the resources.
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
    }

    @Test
    public void testAddImplicitProfileRecursiveResourcesOneRowPerFormat() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] { // if the -a option is omitted, the software will take any ubound arguments as the resources.
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-ri"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FORMAT", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
    }

    @Test
    public void testAddProfileRecursiveResourcesSetCSV() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-o",
                "/home/user/Results/results.csv"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("/home/user/Results/results.csv", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("/home/user/Results/results.csv", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
    }

    @Test
    public void testAddProfileRecursiveResourcesSetCSVColumns() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-o",
                "/home/user/Results/results.csv",
                "-co",
                "ID", "PUID"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("/home/user/Results/results.csv", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(3, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertTrue(propertyNames.contains("profile.columnsToWrite"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("/home/user/Results/results.csv", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
        assertEquals("ID PUID", profileRunCommand.getProperties().getProperty("profile.columnsToWrite"));
    }

    @Test
    public void testAddProfileRecursiveResourcesSetProfile() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-p",
                "/home/user/Results/results.droid"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("/home/user/Results/results.droid", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(1, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
    }

    @Test
    public void testExtensionFilters() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-p",
                "/home/user/Results/results.droid",
                "-Nx",
                "bmp", "png", "jpg"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertEquals("/home/user/Results/results.droid", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(1, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertNull(profileRunCommand.getResultsFilter());
        Filter filter = profileRunCommand.getIdentificationFilter();
        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(1, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_EXTENSION, crit1.getField());
        assertEquals(CriterionOperator.ANY_OF, crit1.getOperator());
        Object[] values = (Object[]) crit1.getValue();
        assertEquals(3, values.length);
        assertEquals("BMP", values[0]);
        assertEquals("PNG", values[1]);
        assertEquals("JPG", values[2]);
    }

    @Test
    public void testMergeIdentificationExtensionFilter() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-FF",
                "file_size > 10000",
                "puid any fmt/101 fmt/102 fmt/103",
                "-Nx",
                "bmp", "png", "jpg"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));

        Filter filter = profileRunCommand.getIdentificationFilter();
        assertEquals(false, filter.isNarrowed());

        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(3, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());

        FilterCriterion crit2 = criterionList.get(1);
        assertEquals(CriterionFieldEnum.PUID, crit2.getField());
        assertEquals(CriterionOperator.ANY_OF, crit2.getOperator());

        Object[] values = (Object[]) crit2.getValue();
        assertEquals(3, values.length);
        assertEquals("fmt/101", values[0]);
        assertEquals("fmt/102", values[1]);
        assertEquals("fmt/103", values[2]);

        FilterCriterion crit3 = criterionList.get(2);
        assertEquals(CriterionFieldEnum.FILE_EXTENSION, crit3.getField());
        assertEquals(CriterionOperator.ANY_OF, crit3.getOperator());
        values = (Object[]) crit3.getValue();
        assertEquals(3, values.length);
        assertEquals("BMP", values[0]);
        assertEquals("PNG", values[1]);
        assertEquals("JPG", values[2]);
    }

    @Test
    public void testSetCommandLineProperties() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-Pr",
                "profile.maxBytesToScan=-1",
                "matchAllExtensions=false"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(4, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.maxBytesToScan"));
        assertTrue(propertyNames.contains("profile.matchAllExtensions"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
        //TODO: should these expected values be strings or typed values?
        assertEquals("-1", profileRunCommand.getProperties().getProperty("profile.maxBytesToScan"));
        assertEquals("false", profileRunCommand.getProperties().getProperty("profile.matchAllExtensions"));
    }

    @Test
    public void testSetCommandLinePropertyFromFile() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String testPropertyFile = getClass().getClassLoader().getResource("test.properties").getFile();
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-Pf",
                testPropertyFile
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(5, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertTrue(propertyNames.contains("profile.maxBytesToScan"));
        assertTrue(propertyNames.contains("profile.matchAllExtensions"));
        assertTrue(propertyNames.contains("profile.processZip"));

        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
        assertEquals("2000", profileRunCommand.getProperties().getProperty("profile.maxBytesToScan"));
        assertEquals("false", profileRunCommand.getProperties().getProperty("profile.matchAllExtensions"));
        assertEquals("true", profileRunCommand.getProperties().getProperty("profile.processZip"));
    }

    @Test
    public void testSetCommandLinePropertyFromFileAndCommandLine() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String testPropertyFile = getClass().getClassLoader().getResource("test.properties").getFile();
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-Pr",
                "processTar=false",
                "-Pf",
                testPropertyFile
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(6, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertTrue(propertyNames.contains("profile.maxBytesToScan"));
        assertTrue(propertyNames.contains("profile.matchAllExtensions"));
        assertTrue(propertyNames.contains("profile.processZip"));
        assertTrue(propertyNames.contains("profile.processTar"));

        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));
        assertEquals("2000", profileRunCommand.getProperties().getProperty("profile.maxBytesToScan"));
        assertEquals("false", profileRunCommand.getProperties().getProperty("profile.matchAllExtensions"));
        assertEquals("true", profileRunCommand.getProperties().getProperty("profile.processZip"));
        assertEquals("false", profileRunCommand.getProperties().getProperty("profile.processTar"));
    }

    @Test
    public void testSetProfileResultAnyFilter() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-F",
                "file_size > 10000",
                "puid any fmt/101 fmt/102 fmt/103"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));

        Filter filter = profileRunCommand.getResultsFilter();
        assertEquals(false, filter.isNarrowed());

        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(2, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());

        FilterCriterion crit2 = criterionList.get(1);
        assertEquals(CriterionFieldEnum.PUID, crit2.getField());
        assertEquals(CriterionOperator.ANY_OF, crit2.getOperator());

        Object[] values = (Object[]) crit2.getValue();
        assertEquals(3, values.length);
        assertEquals("fmt/101", values[0]);
        assertEquals("fmt/102", values[1]);
        assertEquals("fmt/103", values[2]);

    }

    @Test
    public void testSetProfileResultAllFilter() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-f",
                "file_size > 10000",
                "puid any fmt/101 fmt/102 fmt/103"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));

        Filter filter = profileRunCommand.getResultsFilter();
        assertEquals(true, filter.isNarrowed());

        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(2, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());

        FilterCriterion crit2 = criterionList.get(1);
        assertEquals(CriterionFieldEnum.PUID, crit2.getField());
        assertEquals(CriterionOperator.ANY_OF, crit2.getOperator());

        Object[] values = (Object[]) crit2.getValue();
        assertEquals(3, values.length);
        assertEquals("fmt/101", values[0]);
        assertEquals("fmt/102", values[1]);
        assertEquals("fmt/103", values[2]);

    }

    @Test
    public void testSetProfileIdentificationAnyFilter() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-FF",
                "file_size > 10000",
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));

        Filter filter = profileRunCommand.getIdentificationFilter();
        assertEquals(false, filter.isNarrowed());

        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(1, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());
    }

    @Test
    public void testSetProfileIdentificationAllFilter() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[]{
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-ff",
                "file_size > 10000",
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[]{
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertFalse(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertEquals("stdout", profileRunCommand.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(2, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", profileRunCommand.getProperties().getProperty("profile.outputFilePath"));

        Filter filter = profileRunCommand.getIdentificationFilter();
        assertEquals(true, filter.isNarrowed());

        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(1, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.FILE_SIZE, crit1.getField());
        assertEquals(CriterionOperator.GT, crit1.getOperator());
        assertEquals(10000L, crit1.getValue());
    }


    @Test
    public void testNoProfileMode() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-Nr",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"
        };
        CommandLine cli = parse(args);
        ProfileRunCommand e1 = (ProfileRunCommand) factory.getNoProfileCommand(cli);

        assertNotNull(e1);
        assertFalse(e1.getRecursive());

        assertEquals("stdout", e1.getDestination()); // output file not specified, so defaults to stdout.
        List<String> propertyNames = getPropertyNames(e1.getProperties());
        assertEquals(4, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertTrue(propertyNames.contains("profile.outputFilePath"));
        assertTrue(propertyNames.contains("profile.columnsToWrite"));
        assertTrue(propertyNames.contains("profile.quoteAllFields"));
        assertEquals("ONE_ROW_PER_FILE", e1.getProperties().getProperty("profile.exportOptions"));
        assertEquals("stdout", e1.getProperties().getProperty("profile.outputFilePath"));
        assertEquals(Boolean.FALSE, e1.getProperties().getProperty("profile.quoteAllFields"));
        assertEquals("NAME PUID", e1.getProperties().getProperty("profile.columnsToWrite"));

        assertNull( e1.getIdentificationFilter());
        Filter filter = e1.getResultsFilter();
        assertNotNull(filter);
        List<FilterCriterion> criterionList = filter.getCriteria();
        assertEquals(1, criterionList.size());
        FilterCriterion crit1 = criterionList.get(0);
        assertEquals(CriterionFieldEnum.RESOURCE_TYPE, crit1.getField());
        assertEquals(CriterionOperator.NONE_OF, crit1.getOperator());
        Object[] values = (Object[]) crit1.getValue();
        assertEquals(1, values.length);
        assertEquals(ResourceType.FOLDER, values[0]);
    }

    /**
    @Test
    public void testListReports() throws Exception {
        listReportsCommand.setPrintWriter(printWriter);
        when(context.getListReportsCommand()).thenReturn(listReportsCommand);
        ReportManager rm = mock(ReportManager.class);
        when(rm.listReportSpecs()).thenReturn(buildTestReportSpecs());
        listReportsCommand.setReportManager(rm);
        DroidCommand com = factory.getListReportCommand();
        com.execute();
        verify(printWriter).println("\nReport:\t'Report1'\n\tFormats:\t'Text'\t'Pdf'\t'DROID Report XML'");
    }

    private List<ReportSpec> buildTestReportSpecs() {
        List<ReportSpec> specList = new ArrayList<>();
        ReportSpec spec = new ReportSpec();
        spec.setName("Report1");
        List<Path> xslNames = new ArrayList<>();
        Path fakePath = new Path() {

            @Override
            public FileSystem getFileSystem() {
                return null;
            }

            @Override
            public boolean isAbsolute() {
                return false;
            }

            @Override
            public Path getRoot() {
                return null;
            }

            @Override
            public Path getFileName() {
                return this;
            }

            @Override
            public Path getParent() {
                return null;
            }

            @Override
            public int getNameCount() {
                return 0;
            }

            @Override
            public Path getName(int i) {
                return null;
            }

            @Override
            public Path subpath(int i, int i1) {
                return null;
            }

            @Override
            public boolean startsWith(Path path) {
                return false;
            }

            @Override
            public boolean startsWith(String other) {
                return Path.super.startsWith(other);
            }

            @Override
            public boolean endsWith(Path path) {
                return false;
            }

            @Override
            public boolean endsWith(String other) {
                return Path.super.endsWith(other);
            }

            @Override
            public Path normalize() {
                return null;
            }

            @Override
            public Path resolve(Path path) {
                return null;
            }

            @Override
            public Path resolve(String other) {
                return Path.super.resolve(other);
            }

            @Override
            public Path resolveSibling(Path other) {
                return Path.super.resolveSibling(other);
            }

            @Override
            public Path resolveSibling(String other) {
                return Path.super.resolveSibling(other);
            }

            @Override
            public Path relativize(Path path) {
                return null;
            }

            @Override
            public URI toUri() {
                return null;
            }

            @Override
            public Path toAbsolutePath() {
                return null;
            }

            @Override
            public Path toRealPath(LinkOption... linkOptions) throws IOException {
                return null;
            }

            @Override
            public File toFile() {
                return Path.super.toFile();
            }

            @Override
            public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
                return null;
            }

            @Override
            public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
                return Path.super.register(watcher, events);
            }

            @Override
            public Iterator<Path> iterator() {
                return Path.super.iterator();
            }

            @Override
            public int compareTo(Path path) {
                return 0;
            }


            @Override
            public String toString() {
                return "Text.txt.xsl";
            }
        };

        xslNames.add(fakePath);
        spec.setXslTransforms(xslNames);
        specList.add(spec);
        return specList;
    }
    */

    @Test
    public void testArchiveAll() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-p",
                "/home/user/Results/results.droid",
                "-A"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("/home/user/Results/results.droid", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(8, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));

        assertTrue(propertyNames.contains("profile.processZip"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processZip"));

        assertTrue(propertyNames.contains("profile.processTar"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processTar"));

        assertTrue(propertyNames.contains("profile.processRar"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processRar"));

        assertTrue(propertyNames.contains("profile.processGzip"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processGzip"));

        assertTrue(propertyNames.contains("profile.process7zip"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.process7zip"));

        assertTrue(propertyNames.contains("profile.processIso"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processIso"));

        assertTrue(propertyNames.contains("profile.processBzip2"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processBzip2"));
    }

    @Test
    public void testWebArchiveAll() throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] args = new String[] {
                "-a",
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls",
                "-R",
                "-p",
                "/home/user/Results/results.droid",
                "-W"
        };
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("/home/user/Results/results.droid", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());
        assertEquals(3, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));

        assertTrue(propertyNames.contains("profile.processArc"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processArc"));

        assertTrue(propertyNames.contains("profile.processWarc"));
        assertEquals(Boolean.TRUE, profileRunCommand.getProperties().getProperty("profile.processWarc"));
    }

    @Test
    public void testSetArchivesToProcess() throws Exception {
        String[] testCases = new String[] { "zip", "zip rar", "bzip2 gzip zip iso" };
        for (String test : testCases) {
            testSetArchives(test);
        }
    }

    private void testSetArchives(String test) throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] allArchives =new String[] {"zip", "gzip", "tar", "gzip", "bzip2", "rar", "iso"};
        String[] testArgs = test.split(" ");
        String[] args = new String[7 + testArgs.length];
        args[0] = "-a";
        args[1] = "/home/user/Documents/test.doc";
        args[2] = "/home/user/Documents/test.xls";
        args[3] = "-R";
        args[4] = "-p";
        args[5] = "/home/user/Results/results.droid";
        args[6] = "-At";
        for (int i = 0; i < testArgs.length; i++) {
            args[7+i] = testArgs[i];
        }
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("/home/user/Results/results.droid", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());

        assertEquals(8, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));

        for (String archive : allArchives) {
            String propertyname = "profile.process" + archive.substring(0, 1).toUpperCase(Locale.ROOT) + archive.substring(1).toLowerCase(Locale.ROOT);
            assertTrue(propertyNames.contains(propertyname));
            Boolean expected = test.contains(archive);
            assertEquals(expected, profileRunCommand.getProperties().getProperty(propertyname));
        }
    }

    @Test
    public void testSetWebArchivesToProcess() throws Exception {
        String[] testCases = new String[] { "arc", "arc warc", "warc" };
        for (String test : testCases) {
            testSetWebArchives(test);
        }
    }

    public void testSetWebArchives(String test) throws Exception {
        when(context.getProfileRunCommand()).thenReturn(profileRunCommand);
        String[] allArchives =new String[] {"arc", "warc"};
        String[] testArgs = test.split(" ");
        String[] args = new String[7 + testArgs.length];
        args[0] = "-a";
        args[1] = "/home/user/Documents/test.doc";
        args[2] = "/home/user/Documents/test.xls";
        args[3] = "-R";
        args[4] = "-p";
        args[5] = "/home/user/Results/results.droid";
        args[6] = "-Wt";
        for (int i = 0; i < testArgs.length; i++) {
            args[7+i] = testArgs[i];
        }
        CommandLine cli = parse(args);
        factory.getProfileCommand(cli);
        assertArrayEquals(new String[] {
                "/home/user/Documents/test.doc",
                "/home/user/Documents/test.xls"}, profileRunCommand.getResources());
        assertTrue(profileRunCommand.getRecursive());
        assertNull(profileRunCommand.getResultsFilter());
        assertNull(profileRunCommand.getIdentificationFilter());
        assertEquals("/home/user/Results/results.droid", profileRunCommand.getDestination());
        List<String> propertyNames = getPropertyNames(profileRunCommand.getProperties());

        assertEquals(3, propertyNames.size());
        assertTrue(propertyNames.contains("profile.exportOptions"));
        assertEquals("ONE_ROW_PER_FILE", profileRunCommand.getProperties().getProperty("profile.exportOptions"));

        for (String archive : allArchives) {
            String propertyname = "profile.process" + archive.substring(0, 1).toUpperCase(Locale.ROOT) + archive.substring(1).toLowerCase(Locale.ROOT);
            assertTrue(propertyNames.contains(propertyname));
            Boolean expected = Arrays.asList(testArgs).contains(archive);
            assertEquals(expected, profileRunCommand.getProperties().getProperty(propertyname));
        }

    }

    List<String> getPropertyNames(PropertiesConfiguration properties) {
        List<String> result = new ArrayList<>();
        Iterator<String> iterator = properties.getKeys();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
    
}
