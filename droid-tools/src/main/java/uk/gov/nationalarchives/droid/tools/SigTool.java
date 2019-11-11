/*
 * Copyright (c) 2019, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import net.byteseek.compiler.CompileException;
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

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.BINARY;
import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.CONTAINER;

/**
 * A simple command line utility that can parse PRONOM / Container syntax signatures, and either output
 * XML for them, or another expression re-written to the options (e.g. binary syntax, container syntax),
 * with or without spaces.
 * <p>
 * Usage is: SigTool [options] {expressions|filename}
 *
 */
public class SigTool {


    public static void main(String [] args) {
        final int returnCode = executeArguments(args);
        System.exit(returnCode);
    }



    private static int executeArguments(String[] args) {
        int exitCode = 0;
        CommandLineParser parser = new DefaultParser();
        try {
            Options options = createOptions();
            CommandLine cli = parser.parse(options, args);
            exitCode = processCommands(cli, options);
        } catch (ParseException e) {
            System.err.println("ERROR: " + e.getMessage());
            return 1; // failed to parse.
        }
        return exitCode;
    }

    private static int processCommands(CommandLine cli, Options options) {
        // General commands:
        if (cli.hasOption("h")) {
            printHelp(options);
        }

        // Get option settings:
        ByteSequenceCompiler.CompileType compileType = cli.hasOption("p") ? ByteSequenceCompiler.CompileType.PRONOM : ByteSequenceCompiler.CompileType.DROID;
        SignatureType sigType = cli.hasOption("b") ? SignatureType.BINARY : SignatureType.CONTAINER;
        boolean spaceElements = cli.hasOption("s"); // only add spaces if requested.
        boolean processSigFiles = cli.hasOption("f");

        // Process the commands:
        if (processSigFiles) { // using a file as an input:
            if (cli.hasOption("e")) {
                return processExpressionSigFile(cli.getOptionValue("f"), sigType, spaceElements);
            } else {
                return processXMLSigFile(cli.getOptionValue("f"), compileType, sigType, spaceElements);
            }
        } else { // using expressions on the command line as an input
            if (cli.hasOption("e")) {
                return processExpressionCommands(cli.getArgList(), compileType, sigType, spaceElements);
            } else {
                //TODO: why no spacing control over XML commands?
                return processXMLCommands(cli.getArgList(), compileType, sigType);
            }
        }
    }

    private static Options createOptions() {
        // General options
        OptionGroup miscGroup = new OptionGroup();
        Option help = new Option("h", "help", false, "Prints help on commands.");
        miscGroup.addOption(help);

        // Input options
        OptionGroup inputOptions = new OptionGroup();
        Option fileInput = new Option("f", "file", true, "Filename of signature file to process.");
        inputOptions.addOption(fileInput);

        // Output options
        OptionGroup outputOptions = new OptionGroup();
        Option xmlOutput = new Option ("x", "xml", false, "Output is in XML format");
        Option expressionOutput = new Option("e", "expression", false, "Output is a regular expression");
        outputOptions.addOption(xmlOutput);
        outputOptions.addOption(expressionOutput);

        // Binary or Container signatures
        OptionGroup sigTypeOptions = new OptionGroup();
        Option binarySignatures = new Option("b", "binary", false, "Signatures are in binary syntax.");
        Option containerSignatures = new Option("c", "container", false, "Signatures are in container syntax.");
        sigTypeOptions.addOption(binarySignatures);
        sigTypeOptions.addOption(containerSignatures);

        // Compile type options
        OptionGroup compileTypeOptions = new OptionGroup();
        Option droidCompile = new Option("d", "droid", false, "Signatures are compiled for DROID.");
        Option pronomCompile = new Option("p", "pronom", false, "Signatures are compiled for PRONOM.");
        compileTypeOptions.addOption(droidCompile);
        compileTypeOptions.addOption(pronomCompile);

        // Formatting options
        OptionGroup formattingOptions = new OptionGroup();
        Option spaceElements = new Option("s", "spaces", false, "Signature elements have spaces between them.");
        formattingOptions.addOption(spaceElements);

        Options options = new Options();
        options.addOptionGroup(miscGroup);
        options.addOptionGroup(inputOptions);
        options.addOptionGroup(outputOptions);
        options.addOptionGroup(sigTypeOptions);
        options.addOptionGroup(compileTypeOptions);
        options.addOptionGroup(formattingOptions);
        return options;
    }

    private static int processXMLSigFile(String filename, ByteSequenceCompiler.CompileType compileType, SignatureType sigType, boolean spaceElements) {
        int exitCode = 0;
        try {
            Document doc = XmlUtils.readXMLFile(filename);
            processSigFileToXMLWithExpressions(doc, compileType, sigType, spaceElements);
        } catch (IOException e) {
            System.err.println("IO problem reading file: " + filename + "\n" + e.getMessage());
            exitCode = 19;
        }
        return exitCode;
    }

    private static int processExpressionSigFile(String filename, SignatureType sigType, boolean spaceElements) {
        int exitCode = 0;
        Document doc = null;
        try {
            doc = XmlUtils.readXMLFile(filename);
        } catch (IOException e) {
            System.err.println("IO problem reading file: " + filename + "\n" + e.getMessage());
            return 19;
        }
        SignatureType fileType = SigUtils.getSigFileType(doc);
        if (fileType == null) {
            exitCode = 10; // couldn't parse sig filetype.
        } else if (fileType == CONTAINER) {
            processContainerSigFileToExpressions(filename, sigType, spaceElements);
        } else if (fileType == BINARY) {
            processBinarySigFileToExpressions(filename, sigType, spaceElements);
        } else {
            System.err.println("Unknown type of signature file: " + fileType);
            exitCode = 20;
        }
        return exitCode;
    }

    /**
     * Go through all container signatures in a container sig file, and output some metadata along with
     * a compiled PRONOM expression for each.
     *
     * @param filename The container signature file to process.
     * @param sigType
     * @param spaceElements
     * @return
     */
    private static int processContainerSigFileToExpressions(String filename, SignatureType sigType, boolean spaceElements) {
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
                    processInternalSignatures(header, sigcol.getInternalSignatures(), sigType, spaceElements);
                }
            }
        }
        return exitCode;
    }

    private static int processBinarySigFileToExpressions(String filename, SignatureType sigType, boolean spaceElements) {
        int exitCode = 0;
        FFSignatureFile sigFile = SigUtils.readBinarySignatures(filename);
        System.out.println("Version\tSig ID\tReference\tSequence");
        processInternalSignatures(sigFile.getVersion(), sigFile.getSignatures(), sigType, spaceElements);
        return exitCode;
    }

    private static void processInternalSignatures(String header, List<InternalSignature> sigcol,
                                                  SignatureType sigType, boolean spaceElements) {
        for (InternalSignature isig : sigcol) {
            for (ByteSequence seq : isig.getByteSequences()) {
                try {
                    seq.prepareForUse(); //TODO: check NPE if not prepared for use?  If so, why?
                    String sequence = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, sigType, spaceElements);
                    System.out.println( header + '\t' + isig.getID() + '\t' + seq.getReference() + '\t' + sequence);
                } catch (CompileException e) {
                    System.err.println("ERROR compiling sequence: " + seq);
                }
            }
        }
    }

    private static int processSigFileToXMLWithExpressions(Document doc, ByteSequenceCompiler.CompileType compileType, SignatureType sigType, boolean spaceElements) {

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
                    return 17;
                }
            } catch (SignatureParseException e) {
                System.err.println("ERROR parsing XML of byte sequence: " + e.getMessage());
                return 18;
            }
        }

        // Output the XML
        try {
            System.out.println(XmlUtils.toXmlString(doc, true));
        } catch (TransformerException e) {
            System.err.println("ERROR transforming XML document to string.");
            return 16;
        }
        return 0;
    }

    private static int processXMLCommands(List<String> expressions, ByteSequenceCompiler.CompileType compileType, SignatureType sigType) {
        // anything not an option are the expressions to process.
        for (String expression : expressions) {
            try {
                //TODO: specify Offset types for signature.
                String xml = ByteSequenceSerializer.SERIALIZER.toXML(expression, ByteSequenceAnchor.BOFOffset, compileType, sigType);
                System.out.println(expression + "\t" + xml);
            } catch (CompileException e) {
                System.err.println("ERROR: could not compile expression: " + expression + "\n" + e.getMessage());
                return 3; // compilation error processing expression.
            }
        }
        return 0;
    }

    private static int processExpressionCommands(List<String> expressions, ByteSequenceCompiler.CompileType compileType,
                                                 SignatureType sigType, boolean spaceElements) {
        // anything not an option are the expressions to process.
        for (String expression : expressions) {
            try {
                //TODO: specify element spacing.
                String xml = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(expression, sigType, spaceElements) ;
                System.out.println(expression + "\t" + xml);
            } catch (CompileException e) {
                System.err.println("ERROR: could not compile expression: " + expression + "\n" + e.getMessage());
                return 3; // compilation error processing expression.
            }
        }
        return 0;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "SigTool [Options] {expressions|filename}", options);
    }



}
