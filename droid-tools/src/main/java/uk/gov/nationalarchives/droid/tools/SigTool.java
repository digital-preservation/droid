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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.gov.nationalarchives.droid.container.ContainerFile;
import uk.gov.nationalarchives.droid.container.ContainerSignature;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceAnchor;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceCompiler;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceSerializer;
import uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignature;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;
import uk.gov.nationalarchives.droid.core.signature.xml.XmlUtils;

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
    private static final String IO_PROBLEM_READING_FILE = "IO problem reading file: ";

    private static final int FAILED_TO_PARSE_ARGUMENTS = 1;
    private static final int COULDNT_READ_SIGFILE = 10;
    private static final int ANCHOR_VALUE_INCORRECT = 13;
    private static final int IO_PROBLEM = 19;
    private static final int UNKNOWN_SIG_FILE_TYPE = 20;
    private static final int ERROR_COMPILING = 15;
    private static final int ERROR_CONVERTING_SEQUENCE = 17;
    private static final int ERROR_PARSING_BYTE_SEQUENCE_XML = 18;
    private static final int ERROR_TRANSFORMING_XML_TO_STRING = 16;
    private static final int ERROR_COMPILING_EXPRESSION = 3;

    private static final String ERROR_COMPILING_EXPRESSION_MESSAGE = "ERROR: could not compile expression: ";
    private static final String TAB_CHAR = "\t";
    private static final String NEW_LINE_CHAR = "\n";

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
            CommandLine cli = parser.parse(options, args);
            exitCode = processCommands(cli, options);
        } catch (ParseException e) {
            System.err.println("ERROR: " + e.getMessage());
            exitCode = FAILED_TO_PARSE_ARGUMENTS; // failed to parse.
        }
        return exitCode;
    }

    //CHECKSTYLE:OFF - cyclomatic complexity too high.
    private static int processCommands(CommandLine cli, Options options) {
        int exitCode = 0;

        // General commands:
        if (cli.hasOption(HELP_OPTION) || cli.getArgs().length == 0) {
            printHelp(options);
        }

        // Get option settings:
        ByteSequenceCompiler.CompileType compileType = cli.hasOption(PRONOM_OPTION)
                ? ByteSequenceCompiler.CompileType.PRONOM : ByteSequenceCompiler.CompileType.DROID;
        SignatureType sigType = cli.hasOption(BINARY_OPTION) ? SignatureType.BINARY : SignatureType.CONTAINER;
        boolean spaceElements = cli.hasOption(SPACE_OPTION); // only add spaces if requested.
        boolean processSigFiles = cli.hasOption(FILE_OPTION);
        boolean noTabs = cli.hasOption(NOTABS_OPTION);
        ByteSequenceAnchor anchorType = cli.hasOption(ANCHOR_OPTION)
                ? getAnchor(cli.getOptionValue(ANCHOR_OPTION)) : ByteSequenceAnchor.BOFOffset;
        if (anchorType == null) {
            System.err.println("The value provided for the --anchor " + cli.getOptionValue(ANCHOR_OPTION)
                    + " is not recognised.  Must be bofoffset, eofoffset or variable.");
            exitCode = ANCHOR_VALUE_INCORRECT;
        }

        // Process the commands:
        if (exitCode == 0) {
            if (processSigFiles) { // using a file as an input:
                if (cli.hasOption(EXPRESSION_OUTPUT)) {
                    exitCode = processExpressionSigFile(cli.getOptionValue(FILE_OPTION), sigType, spaceElements, noTabs);
                } else {
                    exitCode = processXMLSigFile(cli.getOptionValue(FILE_OPTION), sigType, spaceElements);
                }
            } else { // using expressions on the command line as an input
                if (cli.hasOption(EXPRESSION_OUTPUT)) {
                    exitCode = processExpressionCommands(cli.getArgList(), sigType, spaceElements, noTabs);
                } else {
                    exitCode = processXMLCommands(cli.getArgList(), compileType, sigType, anchorType, noTabs);
                }
            }
        }
        return exitCode;
    }
    //CHECKSTYLE:ON

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
        // General options
        Option help = new Option(HELP_OPTION, "help", false,
                "Prints help on commands.");

        // Input options
        OptionGroup inputOptions = new OptionGroup();
        Option fileInput = new Option(FILE_OPTION, "file", true,
                "Filename of signature file to process.");
        inputOptions.addOption(fileInput);

        // Output options
        OptionGroup outputOptions = new OptionGroup();
        Option xmlOutput = new Option(XML_OPTION, "xml", false,
                "Output is in XML format");
        Option expressionOutput = new Option(EXPRESSION_OPTION, "expression", false,
                "Output is a regular expression");
        outputOptions.addOption(xmlOutput);
        outputOptions.addOption(expressionOutput);

        // Binary or Container signatures
        OptionGroup sigTypeOptions = new OptionGroup();
        Option binarySignatures = new Option(BINARY_OPTION, "binary", false,
                "Signatures are in binary syntax.");
        Option containerSignatures = new Option(CONTAINER_OPTION, "container", false,
                "Signatures are in container syntax.");
        sigTypeOptions.addOption(binarySignatures);
        sigTypeOptions.addOption(containerSignatures);

        // Compile type options
        OptionGroup compileTypeOptions = new OptionGroup();
        Option droidCompile = new Option(DROID_OPTION, "droid", false,
                "Signatures are compiled for DROID.");
        Option pronomCompile = new Option(PRONOM_OPTION, "pronom", false,
                "Signatures are compiled for PRONOM.");
        compileTypeOptions.addOption(droidCompile);
        compileTypeOptions.addOption(pronomCompile);

        // Formatting options
        Option spaceElements = new Option(SPACE_OPTION, "spaces", false,
                "Signature elements have spaces between them.");
        Option noTabs        = new Option(NOTABS_OPTION, "notabs", false,
                "Don't include tab separated metadata - just output the expressions.");
        Option sigAnchor     = new Option(ANCHOR_OPTION, "anchor", true,
                "Where a signature is anchored - BOFoffset, EOFoffset or Variable."
                        + "Defaults to BOFoffset if not set.");

        Options options = new Options();

        // options which can be used at any time:
        options.addOption(help);
        options.addOption(spaceElements);
        options.addOption(noTabs);
        options.addOption(sigAnchor);

        // options which have mutually exclusive options (in groups):
        addOptionGroups(options, inputOptions, outputOptions, sigTypeOptions, compileTypeOptions);

        return options;
    }

    private static void addOptionGroups(Options options, OptionGroup... groups) {
        for (OptionGroup group : groups) {
            options.addOptionGroup(group);
        }
    }

    private static int processXMLSigFile(String filename, SignatureType sigType, boolean spaceElements) {
        int exitCode;
        try {
            Document doc = XmlUtils.readXMLFile(filename);
            exitCode = processSigFileToXMLWithExpressions(doc, sigType, spaceElements);
        } catch (IOException e) {
            System.err.println(IO_PROBLEM_READING_FILE + filename + NEW_LINE_CHAR + e.getMessage());
            exitCode = IO_PROBLEM;
        }
        return exitCode;
    }

    private static int processExpressionSigFile(String filename, SignatureType sigType, boolean spaceElements, boolean noTabs) {
        int exitCode;
        Document doc;
        try {
            doc = XmlUtils.readXMLFile(filename);
        } catch (IOException e) {
            System.err.println(IO_PROBLEM_READING_FILE + filename + NEW_LINE_CHAR + e.getMessage());
            return IO_PROBLEM;
        }
        SignatureType fileType = SigUtils.getSigFileType(doc);
        if (fileType == null) {
            exitCode = COULDNT_READ_SIGFILE; // couldn't parse sig filetype.
        } else if (fileType == SignatureType.CONTAINER) {
            exitCode = processContainerSigFileToExpressions(filename, sigType, spaceElements, noTabs);
        } else if (fileType == SignatureType.BINARY) {
            exitCode = processBinarySigFileToExpressions(filename, sigType, spaceElements, noTabs);
        } else {
            System.err.println("Unknown type of signature file: " + fileType);
            exitCode = UNKNOWN_SIG_FILE_TYPE;
        }
        return exitCode;
    }

    private static int processContainerSigFileToExpressions(String filename, SignatureType sigType,
                                                            boolean spaceElements,  boolean noTabs) {
        int exitCode = 0;
        ContainerSignatureDefinitions sigDefs = SigUtils.readContainerSignatures(filename);
        System.out.println("Description\tContainer Sig ID\tContainer File\tInternal Sig ID\tReference\tSequence");
        for (ContainerSignature sig : sigDefs.getContainerSignatures()) {
            Map<String, ContainerFile> map = sig.getFiles();
            for (String cfilename : map.keySet()) {
                ContainerFile cFile = map.get(cfilename);
                InternalSignatureCollection sigcol = cFile.getCompiledBinarySignatures();
                if (sigcol != null) { // container files don't have to have binary signatures
                    String header = sig.getDescription() + '\t' + sig.getId() + '\t' + cfilename;
                    exitCode = processInternalSignatures(header, sigcol.getInternalSignatures(), sigType, spaceElements, noTabs);
                }
            }
        }
        return exitCode;
    }

    private static int processBinarySigFileToExpressions(String filename, SignatureType sigType,
                                                         boolean spaceElements, boolean noTabs) {
        int exitCode = 0;
        FFSignatureFile sigFile = SigUtils.readBinarySignatures(filename);
        if (!noTabs) {
            System.out.println("Version\tSig ID\tReference\tSequence");
        }

        processInternalSignatures(sigFile.getVersion(), sigFile.getSignatures(), sigType, spaceElements, noTabs);
        return exitCode;
    }

    private static int processInternalSignatures(String header, List<InternalSignature> sigcol,
                                                  SignatureType sigType, boolean spaceElements, boolean noTabs) {
        int exitCode = 0;
        for (InternalSignature isig : sigcol) {
            for (ByteSequence seq : isig.getByteSequences()) {
                try {
                    seq.prepareForUse();
                    String sequence = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, sigType, spaceElements);
                    if (noTabs) {
                        System.out.println(sequence);
                    } else {
                        System.out.println(header + '\t' + isig.getID() + '\t' + seq.getReference() + '\t' + sequence);
                    }
                } catch (CompileException e) {
                    System.err.println("ERROR compiling sequence: " + seq);
                    exitCode = ERROR_COMPILING;
                }
            }
        }
        return exitCode;
    }

    private static int processSigFileToXMLWithExpressions(Document doc, SignatureType sigType, boolean spaceElements) {
        int exitCode = 0;
        // Convert all ByteSequence elements into a simpler version with just a PRONOM expression in the Reference attribute:
        NodeList byteSequenceElements = doc.getElementsByTagName("ByteSequence");
        for (int i = 0; i < byteSequenceElements.getLength(); i++) {
            Element byteSequence = (Element) byteSequenceElements.item(i);
            try {
                ByteSequence seq = SigUtils.parseByteSequenceXML(byteSequence);
                seq.prepareForUse();
                try {
                    // Set a PRONOM expression as the Sequence attribute of the ByteSequence element:
                    String expression = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, sigType, spaceElements);
                    byteSequence.setAttribute("Sequence", expression);
                    // Remove all child subsequence and other nodes from the byte sequence.
                    while (byteSequence.hasChildNodes()) {
                        byteSequence.removeChild(byteSequence.getFirstChild());
                    }
                } catch (CompileException e) {
                    System.err.println("ERROR converting byte sequence into a PRONOM expression: " + e.getMessage());
                    exitCode = ERROR_CONVERTING_SEQUENCE;
                }
            } catch (SignatureParseException e) {
                System.err.println("ERROR parsing XML of byte sequence: " + e.getMessage());
                exitCode = ERROR_PARSING_BYTE_SEQUENCE_XML;
            }
        }

        // Output the XML
        if (exitCode == 0) {
            try {
                System.out.println(XmlUtils.toXmlString(doc, true));
            } catch (TransformerException e) {
                System.err.println("ERROR transforming XML document to string.");
                return ERROR_TRANSFORMING_XML_TO_STRING;
            }
        }
        return exitCode;
    }

    private static int processXMLCommands(List<String> expressions, ByteSequenceCompiler.CompileType compileType,
                                          SignatureType sigType, ByteSequenceAnchor offset, boolean noTabs) {
        // anything not an option are the expressions to process.
        for (String expression : expressions) {
            try {
                String xml = ByteSequenceSerializer.SERIALIZER.toXML(expression, offset, compileType, sigType);
                if (noTabs) {
                    System.out.println(xml);
                } else {
                    System.out.println(expression + TAB_CHAR + xml);
                }
            } catch (CompileException e) {
                System.err.println(ERROR_COMPILING_EXPRESSION_MESSAGE + expression + NEW_LINE_CHAR + e.getMessage());
                return ERROR_COMPILING_EXPRESSION; // compilation error processing expression.
            }
        }
        return 0;
    }

    private static int processExpressionCommands(List<String> expressions, SignatureType sigType,
                                                 boolean spaceElements, boolean noTabs) {
        // anything not an option are the expressions to process.
        for (String expression : expressions) {
            try {
                String xml = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(expression, sigType, spaceElements) ;
                if (noTabs) {
                    System.out.println(xml);
                } else {
                    System.out.println(expression + TAB_CHAR + xml);
                }
            } catch (CompileException e) {
                System.err.println(ERROR_COMPILING_EXPRESSION_MESSAGE + expression + NEW_LINE_CHAR + e.getMessage());
                return ERROR_COMPILING_EXPRESSION; // compilation error processing expression.
            }
        }
        return 0;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("sigTool [Options] {expressions|filename}", options);
    }



}
