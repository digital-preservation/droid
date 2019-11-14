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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.byteseek.compiler.CompileException;

import uk.gov.nationalarchives.droid.container.BinarySignatureXMLParser;
import uk.gov.nationalarchives.droid.container.ContainerFile;
import uk.gov.nationalarchives.droid.container.ContainerSignature;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
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

    private static final BinarySignatureXMLParser<ByteSequence> XML_PARSER = new BinarySignatureXMLParser<>();
    private static final char TAB_CHAR = '\t';
    private static final char NEW_LINE_CHAR = '\n';

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
        return XML_PARSER.fromXmlElement(byteSequenceElement);
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
     * @param spaceElements Whether to space syntactic elements for greater readability.
     * @param noTabs If notabs is set, just the compiled expressions are output, without the original expression or tabs.
     * @throws CompileException If there is a problem compiling the expression.
     */
    public static void convertExpressionSyntax(PrintStream output, List<String> expressions, SignatureType sigType,
                                               boolean spaceElements, boolean noTabs) throws CompileException {
        // anything not an option are the expressions to process.
        for (String expression : expressions) {
            String xml = ByteSequenceSerializer.SERIALIZER.toPRONOMExpression(expression, sigType, spaceElements) ;
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
        matchSignatures(output, compileExpressions(expressions, anchor), anchor, pathToScan);
    }

    /**
     * Matches an InternalSignatureCollection against a file or files in a folder and outputs a tab-delimited summary
     * of the matches.
     *
     * @param output The PrintStream to write the output to.
     * @param sigs The InternalSignatureCollection to match against the files.
     * @param anchor Whether the expressions are anchored to the BOFoffset, EOFoffset or VariableOffset.
     * @param pathToScan The path of a file or a folder to match.
     * @throws IOException If a problem occurs during IO.
     */
    public static void matchSignatures(PrintStream output, InternalSignatureCollection sigs, ByteSequenceAnchor anchor,
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
                        exceptionMessages.append("IO exception processing: ").append(filename).append(':')
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
        IdentificationRequest fileRequest = new FileSystemIdentificationRequest(metaData, identifier);
        fileRequest.open(file);
        return new IdentificationRequestByteReaderAdapter(fileRequest);
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
