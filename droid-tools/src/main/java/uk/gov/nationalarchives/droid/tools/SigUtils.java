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
package uk.gov.nationalarchives.droid.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.byteseek.compiler.CompileException;
import uk.gov.nationalarchives.droid.container.BinarySignatureXMLParser;
import uk.gov.nationalarchives.droid.container.ContainerFile;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequest;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignature;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatch;
import uk.gov.nationalarchives.droid.container.ContainerSignatureMatchCollection;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.container.IdentifierEngine;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.IdentificationRequestByteReaderAdapter;
import uk.gov.nationalarchives.droid.core.SignatureFileParser;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceAnchor;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceCompiler;
import uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceSerializer;
import uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignature;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;
import uk.gov.nationalarchives.droid.core.signature.xml.XmlUtils;

/**
 * A static utility class with some useful methods for processing signature files.
 */
public final class SigUtils {

    private static final BinarySignatureXMLParser<ByteSequence> SEQ_PARSER = new BinarySignatureXMLParser<>();
    private static final BinarySignatureXMLParser<InternalSignature> SIG_PARSER = new BinarySignatureXMLParser<>();

    private static final String ZIP_SIG_XML =
            "<InternalSignature ID=\"200\" Specificity=\"Specific\">"
                    + "<ByteSequence Reference=\"BOFoffset\" Sequence=\"{0-4}'PK'0304\"/>"
                    + "<ByteSequence Endianness=\"Little-endian\" Reference=\"EOFoffset\" "
                    + "Sequence=\"'PK'01{43-65531}'PK'0506{18-65531}\"/></InternalSignature>";

    private static final String OLE2_SIG_XML = "<InternalSignature ID=\"170\" Specificity=\"Specific\">"
            + "<ByteSequence Reference=\"BOFoffset\" Sequence=\"D0CF11E0A1B11AE1{20}FEFF\"/></InternalSignature>";

    private static final char NEW_LINE_CHAR = '\n';
    private static final char TAB_CHAR = '\t';
    private static final String TAB_ONE = "\t1";
    private static final String TAB_ZERO = "\t0";

    private static final InternalSignatureCollection CONTAINER_SIGNATURES;
    private static final String IO_EXCEPTION_PROCESSING = "IO exception processing: ";

    private static final int ZIP_SIG_ID = 200;
    private static final int OLE2_SIG_ID = 170;

    static {
        InternalSignature zipSig = createInternalSignatureFromXML(ZIP_SIG_XML);
        InternalSignature ole2Sig = createInternalSignatureFromXML(OLE2_SIG_XML);
        CONTAINER_SIGNATURES = new InternalSignatureCollection();
        if (zipSig != null) {
            zipSig.prepareForUse();
            CONTAINER_SIGNATURES.addInternalSignature(zipSig);
        }
        if (ole2Sig != null) {
            ole2Sig.prepareForUse();
            CONTAINER_SIGNATURES.addInternalSignature(ole2Sig);
        }
    }

    private static final IdentifierEngine ZIP_IDENTIFIER_ENGINE = new ZipIdentifierEngine();
    private static final IdentifierEngine OLE2_IDENTIFIER_ENGINE = new Ole2IdentifierEngine();

    /**
     * Private constructor for static utility class.
     */
    private SigUtils() {
    }

    /**
     * Returns whether a signature file is a binary or container signature file.
     * @param doc The XML document.
     * @return The signature type of a signature file, or null if it isn't one of those.
     */
    public static SignatureType getSigFileType(Document doc) {
        SignatureType returnValue = null;
        if (doc != null) {
            Element root = doc.getDocumentElement();
            switch (root.getNodeName()) {
                case "ContainerSignatureMapping":   returnValue = SignatureType.CONTAINER; break;
                case "FFSignatureFile":             returnValue = SignatureType.BINARY; break;
                default: returnValue = null;
            }
        }
        return returnValue;
    }

    /**
     * Returns ContainerSignatureDefinitions from a container signature file.
     * @param filename The name of the container signature file.
     * @return ContainerSignatureDefinitions from a container signature file.
     * @throws SignatureParseException if there was a problem parsing the signatures.
     */
    public static ContainerSignatureDefinitions readContainerSignatures(String filename) throws SignatureParseException {
        ContainerSignatureDefinitions containerSignatureDefinitions = null;
        if (filename != null) {
            final Path containerSignaturesFile = Paths.get(filename);
            if (!Files.exists(containerSignaturesFile)) {
                throw new IllegalArgumentException("Container signature file not found");
            }
            try (final InputStream in = new BufferedInputStream(Files.newInputStream(containerSignaturesFile))) {
                final ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
                return parser.parse(in);
            } catch (final IOException | JAXBException ioe) {
                throw new IllegalArgumentException(ioe);
            }
        }
        return null;
    }

    /**
     * Returns FFSignatureFile object from a binary signature file.
     * @param filename The name of the binary signature file.
     * @return FFSignatureFile object from a binary signature file.
     * @throws SignatureParseException if there was a problem parsing the signatures.
     */
    public static FFSignatureFile readBinarySignatures(String filename) throws SignatureParseException {
        File theFile = new File(filename);
        SignatureFileParser parser = new SignatureFileParser();
        return parser.parseSigFile(theFile.toPath());
    }

    /**
     * Parses a ByteSequence XML fragment into a ByteSequence object.
     * @param byteSequenceElement A ByteSequence XML element.
     * @return A ByteSequence object parsed from an XML element.
     * @throws SignatureParseException If there is a problem parsing the XML element.
     */
    public static ByteSequence parseByteSequenceXML(Element byteSequenceElement) throws SignatureParseException {
        return SEQ_PARSER.fromXmlElement(byteSequenceElement);
    }

    /**
     * Parses an InternalSignature XML fragment into an InternalSignature object.
     * @param internalSigElement An InternalSignature XML element.
     * @return An InternalSignature object parsed from an XML element.
     * @throws SignatureParseException If there is a problem parsing the XML element.
     */
    public static InternalSignature parseInternalSignatureXML(Element internalSigElement) throws SignatureParseException {
        return SIG_PARSER.fromXmlElement(internalSigElement);
    }

    /**
     * Parses an InternalSignature from an XML fragment string.  If it could not be parsed, or any other error
     * occurs, it return null.
     *
     * @param xml The XML string containing an internal signature to parse.
     * @return An InternalSignature object created from the XML fragment string.
     */
    public static InternalSignature createInternalSignatureFromXML(String xml)  {
        try {
            return parseInternalSignatureXML(XmlUtils.readXMLFragment(xml));
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (SignatureParseException e) {
        }
        return null;
    }

    /**
     * Converts either a binary or container signature file to use compiled PRONOM expressions, rather than the
     * older complex XML under each ByteSequence element.
     *
     * @param output The PrintStream to write the converted signature file to.
     * @param filename The name of the signature file to convert.
     * @param sigType The type of signature syntax to use when converting (BINARY or CONTAINER)
     * @param spaceElements Whether to put spaces between syntactic elements for greater readability.
     * @throws IOException If a problem occurs during IO.
     * @throws SignatureParseException If a problem occurs parsing the signature file.
     */
    public static void convertSignatureFileToNewFormat(PrintStream output, String filename, SignatureType sigType,
                                                       boolean spaceElements) throws IOException, SignatureParseException {
        convertSignatureFileToNewFormat(output, XmlUtils.readXMLFile(filename), sigType, spaceElements);
    }

    /**
     * Converts either a binary or container signature file to use compiled PRONOM expressions, rather than the
     * older complex XML under each ByteSequence element.
     * @param output The PrintStream to write the converted signature file to.
     * @param doc An XML Document containing a parsed signature file.
     * @param sigType The type of signature syntax to use when converting (BINARY or CONTAINER)
     * @param spaceElements Whether to put spaces between syntactic elements for greater readability.
     * @throws SignatureParseException If a problem occurs parsing the signature file.
     */
    public static void convertSignatureFileToNewFormat(PrintStream output, Document doc, SignatureType sigType,
                                                      boolean spaceElements) throws SignatureParseException {
        // Convert all ByteSequence elements into a simpler version with just a PRONOM expression in the Reference attribute:
        NodeList byteSequenceElements = doc.getElementsByTagName("ByteSequence");
        for (int i = 0; i < byteSequenceElements.getLength(); i++) {
            Element byteSequence = (Element) byteSequenceElements.item(i);
            ByteSequence seq = parseByteSequenceXML(byteSequence);
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
                throw new SignatureParseException(e.getMessage(), e);
            }
        }

        // Output the XML
        try {
            output.println(XmlUtils.toXmlString(doc, true));
        } catch (TransformerException e) {
            throw new SignatureParseException(e.getMessage(), e);
        }
    }

    /**
     * Produces a summary of all the signatures in either a binary or container signature file in a tab delimited format,
     * converting the signatures into either binary or container syntax.
     *
     * @param output The PrintStream to write the tab-delimited summary to.
     * @param signatureFileName The name of the signature file to summarise.
     * @param sigType Whether to render expressions as BINARY or CONTAINER syntax.
     * @param spaceElements Whether to put spaces between syntactic elements for greater readability.
     * @param noTabs If no tabs are specified, then only the compiled expressions will be output, not tab-delimited metadata.
     * @throws IOException If a problem occurs during IO.
     * @throws SignatureParseException If a problem occurs parsing the signature file.
     */
    public static void summariseSignatures(PrintStream output, String signatureFileName, SignatureType sigType,
                                           boolean spaceElements, boolean noTabs) throws IOException, SignatureParseException {
        Document doc = XmlUtils.readXMLFile(signatureFileName);
        switch (getSigFileType(doc)) {
            case BINARY: {
                summariseBinarySignatureFile(output, signatureFileName, sigType, spaceElements, noTabs);
                break;
            }
            case CONTAINER: {
                summariseContainerSignatureFile(output, signatureFileName, sigType, spaceElements, noTabs);
                break;
            }
            default : throw new SignatureParseException("Not a binary or container signature file: " + signatureFileName);
        }
    }

    /**
     * Produces a summary of all the signatures in a binary signature in a tab delimited format,
     * converting the signatures into either binary or container syntax.
     *
     * @param output The PrintStream to write the tab-delimited summary to.
     * @param signatureFileName The name of the signature file to summarise.
     * @param sigType Whether to render expressions as BINARY or CONTAINER syntax.
     * @param spaceElements Whether to put spaces between syntactic elements for greater readability.
     * @param noTabs If no tabs are specified, then only the compiled expressions will be output, not tab-delimited metadata.
     * @throws SignatureParseException If a problem occurs parsing the signature file.
     */
    public static void summariseBinarySignatureFile(PrintStream output, String signatureFileName, SignatureType sigType,
                                                    boolean spaceElements, boolean noTabs) throws SignatureParseException {
        FFSignatureFile sigFile = readBinarySignatures(signatureFileName);
        if (!noTabs) {
            output.println("Version\tSig ID\tReference\tSequence");
        }
        summariseInternalSignatures(output, sigFile.getVersion(), sigFile.getSignatures(), sigType, spaceElements, noTabs);
    }

    /**
     * Produces a summary of all the signatures in a container signature in a tab delimited format,
     * converting the signatures into either binary or container syntax.
     *
     * @param output The PrintStream to write the tab-delimited summary to.
     * @param signatureFileName The name of the signature file to summarise.
     * @param sigType Whether to render expressions as BINARY or CONTAINER syntax.
     * @param spaceElements Whether to put spaces between syntactic elements for greater readability.
     * @param noTabs If no tabs are specified, then only the compiled expressions will be output, not tab-delimited metadata.
     * @throws SignatureParseException If a problem occurs parsing the signature file.
     */
    public static void summariseContainerSignatureFile(PrintStream output, String signatureFileName, SignatureType sigType,
                                                       boolean spaceElements, boolean noTabs) throws SignatureParseException {
        ContainerSignatureDefinitions sigDefs = readContainerSignatures(signatureFileName);
        output.println("Description\tContainer Sig ID\tContainer File\tInternal Sig ID\tReference\tSequence");
        for (ContainerSignature sig : sigDefs.getContainerSignatures()) {
            Map<String, ContainerFile> map = sig.getFiles();
            for (String cfilename : map.keySet()) {
                ContainerFile cFile = map.get(cfilename);
                InternalSignatureCollection sigcol = cFile.getCompiledBinarySignatures();
                if (sigcol != null) { // container files don't have to have binary signatures
                    String header = sig.getDescription() + TAB_CHAR + sig.getId() + TAB_CHAR + cfilename;
                    summariseInternalSignatures(output, header, sigcol.getInternalSignatures(), sigType, spaceElements, noTabs);
                }
            }
        }
    }

    /**
     * Produces a summary of a list of InternalSignature objects in a tab delimited format.
     *
     * @param output The PrintStream to write the tab-delimited summary to.
     * @param header A descriptive header to render as the first column of each summary.
     * @param sigcol A list of InternalSignature objects to summarise.
     * @param sigType Whether to render expressions as BINARY or CONTAINER syntax.
     * @param spaceElements Whether to put spaces between syntactic elements for greater readability.
     * @param noTabs If no tabs are specified, then only the compiled expressions will be output, not tab-delimited metadata.
     * @throws SignatureParseException If a problem occurs converting the InternalSignature to a PRONOM expression.
     */
    public static void summariseInternalSignatures(PrintStream output, String header, List<InternalSignature> sigcol,
                                                  SignatureType sigType, boolean spaceElements, boolean noTabs) throws SignatureParseException {
        for (InternalSignature isig : sigcol) {
            for (ByteSequence seq : isig.getByteSequences()) {
                try {
                    seq.prepareForUse();
                    String sequence = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(seq, sigType, spaceElements);
                    if (noTabs) {
                        output.println(sequence);
                    } else {
                        output.println(header + TAB_CHAR + isig.getID() + TAB_CHAR + seq.getReference() + TAB_CHAR + sequence);
                    }
                } catch (CompileException e) {
                    throw new SignatureParseException(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Converts a list of PRONOM expressions to XML, with each expression on a separate line in a tab-delimited format.
     *
     * @param output The PrintStream to write the XML to.
     * @param expressions A list of expressions to convert.
     * @param compileType Whether to compile signatures for DROID or PRONOM (PRONOM sigs often have more fragments).
     * @param sigType Whether to render the expressions in BINARY or CONTAINER syntax.
     * @param offset Whether the expressions are anchored to BOFOffset, EOFOffset or VariableOffset.
     * @param noTabs If notabs is set, then only the XML is output without the tab-delimited metadata.
     * @throws CompileException If there was a problem compiling the expressions.
     */
    public static void convertExpressionsToXML(PrintStream output, List<String> expressions, ByteSequenceCompiler.CompileType compileType,
                                              SignatureType sigType, ByteSequenceAnchor offset, boolean noTabs) throws CompileException {
        for (String expression : expressions) {
            String xml = ByteSequenceSerializer.SERIALIZER.toXML(expression, offset, compileType, sigType);
            if (noTabs) {
                output.println(xml);
            } else {
                output.println(expression + TAB_CHAR + xml);
            }
        }
    }

    /**
     * Converts a PRONOM expression into either binary or container format, in a tab delimited format with the
     * original expression, a tab, then the new compiled expression.
     *
     * @param output The PrintStream to write the new expression to.
     * @param expressions A list of expressions to convert.
     * @param sigType Whether the expressions should be in BINARY or CONTAINER syntax.
     * @param anchorType whether a signature is anchored to the BOF or EOF of a file.
     * @param spaceElements Whether to space syntactic elements for greater readability.
     * @param noTabs If notabs is set, just the compiled expressions are output, without the original expression or tabs.
     * @throws CompileException If there is a problem compiling the expression.
     */
    public static void convertExpressionSyntax(PrintStream output, List<String> expressions, SignatureType sigType,
                                               ByteSequenceAnchor anchorType, boolean spaceElements, boolean noTabs) throws CompileException {
        // anything not an option are the expressions to process.
        for (String expression : expressions) {
            String xml = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(expression, sigType, anchorType, spaceElements) ;
            if (noTabs) {
                output.println(xml);
            } else {
                output.println(expression + TAB_CHAR + xml);
            }
        }
    }

    /**
     * Matches a list of PRONOM expressions against a file or files in a folder and outputs a tab delimited summary of
     * the matches.
     *
     * @param output The PrintStream to write the output to.
     * @param expressions A list of expressions to find.
     * @param anchor Whether the expressions are anchored to the BOFoffset, EOFoffset or VariableOffset.
     * @param pathToScan The path of a file or a folder to match.
     * @throws CompileException If there is a problem compiling the expressions to match.
     * @throws IOException If a problem occurs during IO.
     */
    public static void matchExpressions(PrintStream output, List<String> expressions, ByteSequenceAnchor anchor,
                                        String pathToScan) throws CompileException, IOException {
        StringBuilder expressionHeader = new StringBuilder("Expressions:");
        for (String expression : expressions) {
            expressionHeader.append(TAB_CHAR).append(expression);
        }
        output.println(expressionHeader.toString());
        matchSignatures(output, compileExpressions(expressions, anchor), pathToScan);
    }

    /**
     * Matches an InternalSignatureCollection against a file or files in a folder and outputs a tab-delimited summary
     * of the matches.
     *
     * @param output The PrintStream to write the output to.
     * @param sigs The InternalSignatureCollection to match against the files.
     * @param pathToScan The path of a file or a folder to match.
     * @throws IOException If a problem occurs during IO.
     */
    public static void matchSignatures(PrintStream output, InternalSignatureCollection sigs,
                                       String pathToScan) throws IOException {
        String pathToUse = getPathWithoutEndingSeparator(pathToScan);
        File scanFile = new File(pathToUse);
        if (scanFile.exists()) {
            // Output header:
            StringBuilder header = new StringBuilder("File");
            for (int i = 0; i < sigs.getInternalSignatures().size(); i++) {
                header.append("\tHits");
            }
            output.println(header);

            // Scan file or directory:
            if (scanFile.isDirectory()) {
                StringBuilder exceptionMessages = new StringBuilder();
                String[] files = scanFile.list();
                for (String filename : files) {
                    try {
                        String childPath = pathToUse + File.separator + filename;
                        File childFile = new File(childPath);
                        if (childFile.isFile()) {
                            List<InternalSignature> matchingSigs = matchFile(childPath, sigs);
                            printSignatureMatches(output, childPath, sigs, matchingSigs);
                        }
                    } catch (IOException e) {
                        exceptionMessages.append(IO_EXCEPTION_PROCESSING).append(filename).append(':')
                        .append(e.getMessage()).append(NEW_LINE_CHAR);
                    }
                }
                String failureMessages = exceptionMessages.toString();
                if (!failureMessages.isEmpty()) {
                    throw new IOException(failureMessages);
                }
            } else {
                List<InternalSignature> matchingSigs = matchFile(pathToUse, sigs);
                printSignatureMatches(output, pathToUse, sigs, matchingSigs);
            }
        }
    }

    /**
     * Matches an InternalSignatureCollection against a given file.
     *
     * @param filename The filename to scan.
     * @param sigs The InternalSignatureCollection to match.
     * @return A list of InternalSignatures which matched the file.
     * @throws IOException If a problem occurs during IO.
     */
    public static List<InternalSignature> matchFile(String filename, InternalSignatureCollection sigs) throws IOException {
        ByteReader reader =  null;
        try {
            Path file = Paths.get(filename);
            reader = getByteReaderForFile(file);
            return sigs.getMatchingSignatures(reader, -1);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Matches ContainerSignature against a file or files in a folder and outputs a tab-delimited summary
     * of the matches.
     *
     * @param output The PrintStream to write the output to.
     * @param signature The container signature
     * @param internalPath The path of the file inside the container to match the signature against.
     * @param anchor Whether the signture is anchored to BOF, EOF or Variable.
     * @param pathToScan The path of a file or a folder to match.
     * @throws IOException If a problem occurs during IO.
     * @throws CompileException if there is a problem compiling the signature.
     */
    public static void matchContainerFile(PrintStream output, String signature, String internalPath,
                                                ByteSequenceAnchor anchor, String pathToScan) throws IOException, CompileException {
        String pathToUse = getPathWithoutEndingSeparator(pathToScan);
        File scanFile = new File(pathToUse);
        if (scanFile.exists()) {
            // Output header:
            output.println("File\tHit");

            // Scan file or directory:
            if (scanFile.isDirectory()) {
                StringBuilder exceptionMessages = new StringBuilder();
                String[] files = scanFile.list();
                for (String filename : files) {
                    try {
                        String childPath = pathToUse + File.separator + filename;
                        File childFile = new File(childPath);
                        if (childFile.isFile()) {
                            if (matchContainerFile(filename, signature, internalPath, anchor)) {
                                output.println(filename + TAB_ONE);
                            } else {
                                output.println(filename + TAB_ZERO);
                            }
                        }
                    } catch (IOException e) {
                        exceptionMessages.append(IO_EXCEPTION_PROCESSING).append(filename).append(':')
                                .append(e.getMessage()).append(NEW_LINE_CHAR);
                    }
                }
                String failureMessages = exceptionMessages.toString();
                if (!failureMessages.isEmpty()) {
                    throw new IOException(failureMessages);
                }
            } else {
                if (matchContainerFile(pathToUse, signature, internalPath, anchor)) {
                    output.println(pathToUse + TAB_ONE);
                } else {
                    output.println(pathToUse + TAB_ZERO);
                }
            }
        }
    }

    /**
     * Returns true if a container signature matches a file.
     *
     * @param filename The filename of the file to test.
     * @param signature The signature to try.
     * @param internalPath The internal path of the file inside the container to match against.
     * @param signatureAnchor Whether the signature is anchored to BOF, EOF or Variable.
     * @return true if a container signature matches a file.
     * @throws IOException If an IO problem happens reading the file.
     * @throws CompileException If the signature can't be compiled.
     */
    public static boolean matchContainerFile(String filename, String signature, String internalPath,
                                             ByteSequenceAnchor signatureAnchor) throws IOException, CompileException {
        ContainerSignature sig = createContainerSignature(1, signature, signature, internalPath, signatureAnchor);
        return matchContainerFile(filename, sig);
    }

    /**
     * Returns true if a container signature matches a file.
     *
     * @param filename The filename of the file to test.
     * @param sig The container signature to test against.
     * @return  true if a container signature matches a file.
     * @throws IOException If an IO problem happens reading the file.
     */
    public static boolean matchContainerFile(String filename, ContainerSignature sig) throws IOException {
        ContainerFileIdentificationRequest fileRequest = null;
        try {
            Path file = Paths.get(filename);
            fileRequest = new ContainerFileIdentificationRequest(null);
            InputStream stream = new FileInputStream(new File(filename));
            fileRequest.open(stream);
            return matchContainerFile(filename, fileRequest, sig);
        } finally {
            if (fileRequest != null) {
                fileRequest.close();
            }
        }
    }

    /**
     * Returns true if a container signature matches a file.
     *
     * @param filename The file to identify.
     * @param request An identification request for the container file.
     * @param sig The container signature to test against.
     * @return  true if a container signature matches a file.
     * @throws IOException If an IO problem happens reading the file.
     */
    public static boolean matchContainerFile(String filename, IdentificationRequest request, ContainerSignature sig) throws IOException {
        boolean result = false;
        IdentifierEngine engine = getContainerIdentifierEngine(filename);
        if (engine != null) {
            ContainerSignatureMatchCollection collection = getContainerMatchCollection(sig);
            engine.process(request, collection);
            for (ContainerSignatureMatch match : collection.getContainerSignatureMatches()) {
                if (!match.isMatch()) {
                    return false;
                }
            }
            result = true;
        }
        return result;
    }

    /**
     * Builds a ContainerSignatureMatchCollection object from a ContainerSignature.
     *
     * @param sig The ContainerSignature to build the collection from.
     * @return a ContainerSignatureMatchCollection object from a ContainerSignature.
     */
    public static ContainerSignatureMatchCollection getContainerMatchCollection(ContainerSignature sig) {
        ArrayList<ContainerSignature> sigs = new ArrayList<>();
        sigs.add(sig);
        ArrayList<String> filesInSig = new ArrayList<>(sig.getFiles().keySet());
        return new ContainerSignatureMatchCollection(sigs, filesInSig, -1);
    }

    /**
     * Returns an identification engine (zip or ole2) for a file, or null if it can' be recognised.
     * @param filename The filename to get an identifier engine for.
     * @return an identification engine (zip or ole2) for a file, or null if it can' be recognised.
     * @throws IOException If an IO problem happens reading the file.
     */
    public static IdentifierEngine getContainerIdentifierEngine(String filename) throws IOException {
        IdentifierEngine engine = null;
        List<InternalSignature> matches = matchFile(filename, CONTAINER_SIGNATURES);
        if (matches.size() == 1) {
            InternalSignature sig = matches.get(0);
            switch (sig.getID()) {
                case ZIP_SIG_ID: { // zip sig identifier.
                    engine = ZIP_IDENTIFIER_ENGINE;
                    break;
                }
                case OLE2_SIG_ID: { // ole2 sig identifier.
                    engine = OLE2_IDENTIFIER_ENGINE;
                    break;
                }
                default : engine = null;
            }
        }
        if (engine != null) {
            engine.setRequestFactory(new ContainerFileIdentificationRequestFactory());
        }
        return engine;
    }

    /**
     * Creates a container signature.
     *
     * @param id The id of the signature.
     * @param description A description of the signature.
     * @param signature The signature to compile.
     * @param internalPath The internal path of the file the signature will be matched against.
     * @param signatureAnchor Whether the signature is anchored to BOF, EOF or Variable.
     * @return a container signature.
     * @throws CompileException if there was a problem compiling the signature.
     */
    public static ContainerSignature createContainerSignature(int id, String description, String signature,
                                                               String internalPath, ByteSequenceAnchor signatureAnchor)
            throws CompileException {
        InternalSignatureCollection sigs = compileExpression(signature, signatureAnchor);
        ContainerFile file = createContainerFile(sigs, internalPath);
        List<ContainerFile> files = new ArrayList<>();
        files.add(file);
        return createContainerSignature(id, description, files);
    }

    /**
     * Creates a container signature from a list of ContainerFile objects.
     * @param id The id of the signature.
     * @param description The description of the signature.
     * @param containerFiles A list of ContainerFile objects which define what signatures match against which files.
     * @return a container signature from a list of ContainerFile objects.
     */
    public static ContainerSignature createContainerSignature(int id, String description, List<ContainerFile> containerFiles) {
        ContainerSignature containerSig = new ContainerSignature();
        containerSig.setId(id);
        containerSig.setDescription(description);
        containerSig.setFiles(containerFiles);
        return containerSig;
    }

    /**
     * Creates a ContainerFile from an InternalSignatureCollection and the path to match against.
     * @param binarySigs The binary sigs which will be run on the internal container file path.
     * @param containerFilePath The file path inside the container to run the binary signatures against.
     * @return a ContainerFile from an InternalSignatureCollection and the path to match against.
     */
    public static ContainerFile createContainerFile(InternalSignatureCollection binarySigs, String containerFilePath) {
        ContainerFile containerFile = new ContainerFile();
        containerFile.setPath(containerFilePath);
        containerFile.setBinarySignatures(binarySigs);
        return containerFile;
    }

    /**
     * Returns a ByteReader (required for matching signatures) for a given file.
     *
     * @param file The file to obtain a ByteReader for.
     * @return a ByteReader (required for matching signatures) for a given file.
     * @throws IOException if there is a problem opening the file.
     */
    public static ByteReader getByteReaderForFile(Path file) throws IOException {
        long size = Files.size(file);
        long lastmodified = Files.getLastModifiedTime(file).toMillis();
        RequestMetaData metaData = new RequestMetaData(size, lastmodified, file.toString());
        RequestIdentifier identifier = new RequestIdentifier(file.toUri());
        try (IdentificationRequest fileRequest = new FileSystemIdentificationRequest(metaData, identifier)) {
            fileRequest.open(file);
            return new IdentificationRequestByteReaderAdapter(fileRequest);
        }
    }

    /**
     * Compiles an expression into an InternalSignatureCollection.
     * A fake file format will be appended using PUID format: "tst/1"
     * @param expression The list of expression to compile.
     * @param anchor Whether the expressions are anchored to the BOFoffset, EOFoffset or VariableOffset.
     * @return An InternalSignatureCollection containing the compiled expressions.
     * @throws CompileException If a problem occurs during compilation.
     */
    public static InternalSignatureCollection compileExpression(String expression, ByteSequenceAnchor anchor) throws CompileException {
        ArrayList<String> expressions = new ArrayList<>();
        expressions.add(expression);
        return compileExpressions(expressions, anchor);
    }

    /**
     * Compiles a list of expressions into an InternalSignatureCollection.
     * A fake file format will be appended for each expression using PUID format: "tst/{signum}"
     * @param expressions The list of expression to compile.
     * @param anchor Whether the expressions are anchored to the BOFoffset, EOFoffset or VariableOffset.
     * @return An InternalSignatureCollection containing the compiled expressions.
     * @throws CompileException If a problem occurs during compilation.
     */
    public static InternalSignatureCollection compileExpressions(List<String> expressions, ByteSequenceAnchor anchor) throws CompileException {
        int sigID = 0;
        InternalSignatureCollection sigs = new InternalSignatureCollection();
        for (String expression : expressions) {
            ByteSequence sequence = ByteSequenceCompiler.COMPILER.compile(expression, anchor);
            InternalSignature sig = new InternalSignature();
            String sigIDString = Integer.toString(sigID++);
            sig.setID(sigIDString);
            sig.addByteSequence(sequence);
            sig.addFileFormat(getFakeFileFormat(sigIDString));
            sig.prepareForUse();
            sigs.addInternalSignature(sig);
        }
        return sigs;
    }

    private static void printSignatureMatches(PrintStream output, String header, InternalSignatureCollection sigs, List<InternalSignature> matchingSignatures) {
        int[] hitNums = new int[sigs.getInternalSignatures().size()];
        for (InternalSignature hit : matchingSignatures) {
            hitNums[hit.getID()] = 1;
        }
        StringBuilder builder = new StringBuilder(header);
        for (int i = 0; i < hitNums.length; i++) {
            builder.append(TAB_CHAR).append(hitNums[i]);
        }
        output.println(builder.toString());
    }

    private static FileFormat getFakeFileFormat(String sigID) {
        FileFormat fakeFormat = new FileFormat();
        fakeFormat.setAttributeValue("Name", "Test format: " + sigID);
        fakeFormat.setAttributeValue("PUID", "tst/" + sigID);
        fakeFormat.setInternalSignatureID(sigID);
        return fakeFormat;
    }

    private static String getPathWithoutEndingSeparator(String path) {
        if (path.endsWith(File.separator)) {
            return path.substring(0, path.length() - File.separator.length());
        }
        return path;
    }
}
