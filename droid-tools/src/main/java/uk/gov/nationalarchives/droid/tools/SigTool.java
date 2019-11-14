/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceAnchor;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceCompiler;
import uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType;

import net.byteseek.compiler.CompileException;

/**
 * A simple command line utility that can parse PRONOM / Container syntax signatures, and either output
 * XML for them, or another expression re-written to the options (e.g. binary syntax, container syntax),
 * with or without spaces.
 * <p>
 * Usage is: SigTool [options] {expressions|filename}
 *
 */
public final class SigTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(SigTool.class);

    private static final String PRONOM_OPTION = "p";
    private static final String BINARY_OPTION = "b";
    private static final String SPACE_OPTION = "s";
    private static final String FILE_OPTION = "f";
    private static final String NOTABS_OPTION = "n";
    private static final String ANCHOR_OPTION = "a";
    private static final String HELP_OPTION = "h";
    private static final String XML_OPTION = "x";
    private static final String EXPRESSION_OUTPUT = "e";
    private static final String EXPRESSION_OPTION = EXPRESSION_OUTPUT;
    private static final String CONTAINER_OPTION = "c";
    private static final String DROID_OPTION = "d";
    private static final String MATCH_OPTION = "m";
    private static final String OUTPUT_FILE = "o";

    private static final int SUCCESS = 0;
    private static final int FAILED_TO_PARSE_ARGUMENTS = 1;
    private static final int ANCHOR_VALUE_INCORRECT = 2;
    private static final int IO_EXCEPTION = 3;
    private static final int PARSE_ERROR = 4;
    private static final int COMPILE_ERROR = 5;


    private SigTool() {
    }

    /**
     * Runs the sigtool.
     *
     * @param args The command line arguments.
     */
    public static void main(String [] args) {
        final int returnCode = executeArguments(args);
        System.exit(returnCode);
    }

    private static int executeArguments(String[] args) {
        int exitCode;
        CommandLineParser parser = new DefaultParser();
        try {
            Options options = createOptions();
            if (args.length == 0) {
                printHelp(options);
                exitCode = SUCCESS;
            } else {
                CommandLine cli = parser.parse(options, args);
                exitCode = processCommands(cli, options);
            }
        } catch (ParseException e) {
            System.err.println("ERROR: " + e.getMessage());
            exitCode = FAILED_TO_PARSE_ARGUMENTS; // failed to parse.
        }
        return exitCode;
    }

    private static int processCommands(CommandLine cli, Options options) {
        int exitCode = SUCCESS;

        // General commands:
        if (cli.hasOption(HELP_OPTION)) {
            printHelp(options);
        }

        // Get option settings:
        ByteSequenceCompiler.CompileType compileType = cli.hasOption(PRONOM_OPTION)
                ? ByteSequenceCompiler.CompileType.PRONOM : ByteSequenceCompiler.CompileType.DROID;
        SignatureType sigType = cli.hasOption(BINARY_OPTION) ? SignatureType.BINARY : SignatureType.CONTAINER;
        boolean spaceElements = cli.hasOption(SPACE_OPTION); // only add spaces if requested.
        boolean processSigFiles = cli.hasOption(FILE_OPTION);
        boolean outputFile = cli.hasOption(OUTPUT_FILE);
        boolean noTabs = cli.hasOption(NOTABS_OPTION);
        ByteSequenceAnchor anchorType = cli.hasOption(ANCHOR_OPTION)
                ? getAnchor(cli.getOptionValue(ANCHOR_OPTION)) : ByteSequenceAnchor.BOFOffset;
        if (anchorType == null) {
            System.err.println("The value provided for the --anchor " + cli.getOptionValue(ANCHOR_OPTION)
                    + " is not recognised.  Must be bofoffset, eofoffset or variable.");
            exitCode = ANCHOR_VALUE_INCORRECT;
        } else {
            try {
                processCommands(outputFile, cli, processSigFiles, compileType, sigType, anchorType, spaceElements, noTabs);
            } catch (IOException e) {
                exitCode = IO_EXCEPTION;
                System.err.println("IO ERROR: " + e.getMessage());
            } catch (SignatureParseException e) {
                exitCode = PARSE_ERROR;
                System.err.println("PARSE ERROR: " + e.getMessage());
            } catch (CompileException e) {
                exitCode = COMPILE_ERROR;
                System.err.println("COMPILE ERROR: " + e.getMessage());
            }
        }
        return exitCode;
    }

    private static void processCommands(boolean outputFile, CommandLine cli, boolean processSigFiles,
                                        ByteSequenceCompiler.CompileType compileType, SignatureType sigType,
                                        ByteSequenceAnchor anchorType, boolean spaceElements, boolean noTabs)
            throws IOException, SignatureParseException, CompileException {
        final PrintStream output;
        if (outputFile) {
            output = createOutputFile(cli.getOptionValue(OUTPUT_FILE));
        } else {
            output = System.out;
        }
        try {
            // Process the commands:
            if (processSigFiles) { // using a file as an input:
                if (cli.hasOption(EXPRESSION_OUTPUT)) {
                    SigUtils.summariseSignatures(output, cli.getOptionValue(FILE_OPTION), sigType, spaceElements, noTabs);
                } else {
                    SigUtils.convertSignatureFileToNewFormat(output, cli.getOptionValue(FILE_OPTION), sigType, spaceElements);
                }
            } else { // using expressions on the command line as an input
                if (cli.hasOption(MATCH_OPTION)) {
                    SigUtils.matchExpressions(output, cli.getArgList(), anchorType, cli.getOptionValue(MATCH_OPTION));
                } else if (cli.hasOption(EXPRESSION_OUTPUT)) {
                    SigUtils.convertExpressionSyntax(output, cli.getArgList(), sigType, spaceElements, noTabs);
                } else {
                    SigUtils.convertExpressionsToXML(output, cli.getArgList(), compileType, sigType, anchorType, noTabs);
                }
            }
        } finally {
            output.close();
        }
    }

    private static PrintStream createOutputFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        FileOutputStream fos = new FileOutputStream(file);
        return new PrintStream(fos);
    }

    private static ByteSequenceAnchor getAnchor(String anchorText) {
        ByteSequenceAnchor anchor;
        switch (anchorText.toLowerCase()) {
            case "bofoffset" : anchor = ByteSequenceAnchor.BOFOffset; break;
            case "eofoffset" : anchor = ByteSequenceAnchor.EOFOffset; break;
            default: anchor = ByteSequenceAnchor.VariableOffset;
        }
        return anchor;
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(new Option(HELP_OPTION, "help", false,
                "Prints help on commands."));
        options.addOption(new Option(SPACE_OPTION, "spaces", false,
                "Signature elements have spaces between them."));
        options.addOption(new Option(NOTABS_OPTION, "notabs", false,
                "Don't include tab separated metadata - just output the expressions."));
        options.addOption(new Option(ANCHOR_OPTION, "anchor", true,
                "Where a signature is anchored - BOFoffset, EOFoffset or Variable."
                        + "Defaults to BOFoffset if not set."));
        options.addOption(new Option(OUTPUT_FILE, "output", true,
                "Specifies a file to output the results to.  If not specified, will output to console."));
        addOptionGroups(options, buildFileOptions(), buildOutputOptions(), buildSignatureOptions(), buildCompileOptions());
        return options;
    }

    private static OptionGroup buildCompileOptions() {
        OptionGroup compileTypeOptions = new OptionGroup();
        Option droidCompile = new Option(DROID_OPTION, "droid", false,
                "Signatures are compiled for DROID.");
        Option pronomCompile = new Option(PRONOM_OPTION, "pronom", false,
                "Signatures are compiled for PRONOM.");
        compileTypeOptions.addOption(droidCompile);
        compileTypeOptions.addOption(pronomCompile);
        return compileTypeOptions;
    }

    private static OptionGroup buildSignatureOptions() {
        // Binary or Container signatures
        OptionGroup sigTypeOptions = new OptionGroup();
        Option binarySignatures = new Option(BINARY_OPTION, "binary", false,
                "Signatures are in binary syntax.");
        Option containerSignatures = new Option(CONTAINER_OPTION, "container", false,
                "Signatures are in container syntax.");
        sigTypeOptions.addOption(binarySignatures);
        sigTypeOptions.addOption(containerSignatures);
        return sigTypeOptions;
    }

    private static OptionGroup buildOutputOptions() {
        OptionGroup outputOptions = new OptionGroup();
        Option xmlOutput = new Option(XML_OPTION, "xml", false,
                "Output is in XML format");
        Option expressionOutput = new Option(EXPRESSION_OPTION, "expression", false,
                "Output is a regular expression");
        outputOptions.addOption(xmlOutput);
        outputOptions.addOption(expressionOutput);
        return outputOptions;
    }

    private static OptionGroup buildFileOptions() {
        OptionGroup fileOptions = new OptionGroup();
        Option fileInput = new Option(FILE_OPTION, "file", true,
                "Filename of signature file to process.");
        Option matchFile = new Option(MATCH_OPTION, "match", true,
                "Filename of a file to match the signature against.");
        fileOptions.addOption(fileInput);
        fileOptions.addOption(matchFile);
        return fileOptions;
    }

    private static void addOptionGroups(Options options, OptionGroup... groups) {
        for (OptionGroup group : groups) {
            options.addOptionGroup(group);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("sigTool [Options] {expressions|filename}", options);
    }

}
