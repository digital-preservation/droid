/**
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
package uk.gov.nationalarchives.droid.core.signature.compiler;

import net.byteseek.compiler.CompileException;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.parser.ParseException;
import net.byteseek.parser.regex.RegexParser;
import net.byteseek.parser.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.SideFragment;
import uk.gov.nationalarchives.droid.core.signature.droid6.SubSequence;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.util.List;

import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceSerializer.SignatureType.BINARY;
import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceSerializer.SignatureType.CONTAINER;

/**
 * A class which provides serialization methods for ByteSequences, toXML and to a PRONOM or byteseek expression.
 * This can be used to obtain XML from a signature, or to rewrite a signature into a different signature type,
 * e.g. you could put a binary signature into the serializer with container as the output target, and the
 * signature will be rewritten using the more advanced syntax.
 */
public class ByteSequenceSerializer {

    /**
     * Convenient static serializer instance (it's all stateless, so you don't need more than one)
     */
    public final static ByteSequenceSerializer SERIALIZER = new ByteSequenceSerializer();

    /**
     * An underlying byteseek parser used to transform byteseek expressions into an Abstract Syntax Tree.
     */
    private final static RegexParser PARSER = new RegexParser();

    /**
     * The type of signature we are targeting for output.
     */
    public enum SignatureType {
        /**
         * The signature should be as close to a standard binary signature as possible.
         * This means it will use the more standard PRONOM syntax (e.g. alternatives instead of sets) and have no strings.
         */
        BINARY,

        /**
         * The signature can use the full potential of container syntax.  This includes strings and multi-byte sets.
         */
        CONTAINER
    }

    /**
     * Returns the XML for a PRONOM expression.
     *
     * @param sequence The PRONOM expression in either binary or container syntax.
     * @param anchor   Whether the expression is anchored to BOF, EOF or is variable.
     * @param type     Whether to compile the XML with a PRONOM target, or a DROID target.  DROID supports longer anchor sequences.
     * @param sigType  Whether the XML is intended to be binary compatible, or container compatible.
     * @return An XML string containing the output of compiling the PRONOM expression.
     * @throws CompileException If anything goes wrong during the compilation.
     */
    public String toXML(String sequence, ByteSequenceAnchor anchor, ByteSequenceCompiler.CompileType type, SignatureType sigType) throws CompileException {
        return toXML(ByteSequenceCompiler.COMPILER.compile(sequence, anchor, type), sigType);
    }

    /**
     * Returns the XML for a ByteSequence object.
     *
     * @param sequence The PRONOM expression in either binary or container syntax.
     * @param sigType  Whether the XML is intended to be binary compatible, or container compatible.
     * @return An XML string containing a representation of the ByteSequence object.
     * @throws CompileException If anything goes wrong during the compilation.
     */
    public String toXML(ByteSequence sequence, SignatureType sigType) throws CompileException {
        Document doc = getXMLDocument();
        Element byteSequence = createByteSequenceElement(doc, sequence);
        int position = 1;
        for (SubSequence sub  : sequence.getSubSequences()) {
            try {
                byteSequence.appendChild(createSubSequenceElement(doc, sub, sigType, position++));
            } catch (ParseException e) {
                throw new CompileException(e.getMessage(), e);
            }
        }
        return toXmlString(doc);
    }

    /**
     * Returns a byteseek-compatible regular expression for a ByteSequence.
     *
     * @param sequence The ByteSequence.
     * @return A string containing a byteseek-compatible regular expression.
     */
    public String toByteseekExpression(ByteSequence sequence) {
        return sequence.toRegularExpression(true);
    }

    /**
     * Rewrites a PRONOM expression with the syntax selected.
     *
     * @param expression The PRONOM expression to rewrite.
     * @param sigType Whether the syntax should target binary or container signature syntax.
     * @param spaceElements Whether elements in the expression should have whitespace between them.
     * @return A PRONOM expression with the syntax selected
     * @throws CompileException If anything goes wrong during the compilation.
     */
    public String toPRONOMExpression(String expression, SignatureType sigType, boolean spaceElements) throws CompileException {
        return toPRONOMExpression(ByteSequenceCompiler.COMPILER.compile(expression), sigType, spaceElements);
    }

    /**
     * Returns a PRONOM expression given a ByteSequence, and what kind of signature to produce.
     *
     * @param sequence The ByteSequence to get the PRONOM expression from.
     * @param sigType Whether to return a binary PRONOM compatible expression, or container compatible.
     * @param spaceElements Whether to add spaces between signature elements (for readability).
     * @return a PRONOM expression given a ByteSequence, and what kind of signature to produce.
     * @throws CompileException If anything goes wrong during the compilation.
     */
    public String toPRONOMExpression(ByteSequence sequence, SignatureType sigType, boolean spaceElements) throws CompileException {
        try {
            String byteseekRegex = sequence.toRegularExpression(true);
            ParseTree parsed = PARSER.parse(byteseekRegex);
            return toPRONOMExpression(parsed, sigType, spaceElements);
        } catch (ParseException e) {
            throw new CompileException(e.getMessage(), e);
        }
    }

    /* ******************************************************************************************************************
     * Private methods.
     */

    private Document getXMLDocument() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return dBuilder.newDocument();
    }

    private static String toXmlString(Document document) throws CompileException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            throw new CompileException(e.getMessage(), e);
        }
    }

    private Element createByteSequenceElement(Document doc, ByteSequence sequence) {
        Element byteSequence = doc.createElement("ByteSequence");
        byteSequence.setAttribute("Reference", sequence.getReference());
        if (!sequence.getSequence().isEmpty()) {
            byteSequence.setAttribute("Sequence", sequence.getSequence());
        }
        doc.appendChild(byteSequence);
        return byteSequence;
    }

    private Element createSubSequenceElement(Document doc, SubSequence sub, SignatureType sigType, int position) throws ParseException {
        Element subSequence = createBasicSubSequenceElement(doc, sub, sigType, position);
        appendFragments(doc, subSequence, sub.getLeftFragments(), "LeftFragment", sigType);
        appendFragments(doc, subSequence, sub.getRightFragments(), "RightFragment", sigType);
        return subSequence;
    }

    private void appendFragments(Document doc, Element subsequence, List<List<SideFragment>> fragments, String elementName,
                                 SignatureType sigType) throws ParseException {
        int fragPos = 0;
        for (List<SideFragment> fragsAtPos : fragments) {
            fragPos++;
            for (SideFragment frag : fragsAtPos) {
                Element fragment = doc.createElement(elementName);
                fragment.setAttribute("Position", Integer.toString(fragPos));
                fragment.setAttribute("MinOffset",Integer.toString(frag.getMinOffset()));
                fragment.setAttribute("MaxOffset",Integer.toString(frag.getMaxOffset()));
                fragment.setTextContent(getSequenceMatcherExpression(frag.getMatcher(), sigType));
                subsequence.appendChild(fragment);
            }
        }
    }

    private Element createBasicSubSequenceElement(Document doc, SubSequence sub, SignatureType sigType, int position) throws ParseException {
        Element subSequence = doc.createElement("SubSequence");
        subSequence.setAttribute("Position", Integer.toString(position));
        subSequence.setAttribute("SubSeqMinOffset", Integer.toString(sub.getMinSeqOffset()));
        subSequence.setAttribute("SubSeqMaxOffset", Integer.toString(sub.getMaxSeqOffset()));

        // Add sequence element containing the signature to search for:
        Element seq = doc.createElement("Sequence");
        seq.setTextContent(getSequenceMatcherExpression(sub.getAnchorMatcher(), sigType));
        subSequence.appendChild(seq);

        return subSequence;
    }

    private String getSequenceMatcherExpression(SequenceMatcher matcher, SignatureType sigType) throws ParseException {
        String byteseekExpression = matcher.toRegularExpression(true);
        ParseTree parsed = PARSER.parse(byteseekExpression);
        return toPRONOMExpression(parsed, sigType, false);
    }

    private String toPRONOMExpression(final ParseTree tree, SignatureType sigType, boolean spaceElements) throws ParseException {
        final StringBuilder builder = new StringBuilder();
        toPRONOMExpression(tree, builder, sigType, spaceElements, false);
        return builder.toString();
    }

    private void toPRONOMExpression(final ParseTree tree, final StringBuilder builder, SignatureType sigType, boolean spaceElements, final boolean inBrackets) throws ParseException {
        switch (tree.getParseTreeType()) {
            case BYTE: {
                builder.append(String.format("%02x", tree.getByteValue() & 0xFF).toUpperCase());
                break;
            }
            case STRING: {
                String value = tree.getTextValue();
                if (sigType == CONTAINER) { // output strings as strings.
                    builder.append('\'').append(value).append('\'');
                } else { // output strings as byte sequences.
                    for (int i = 0; i < value.length(); i++) {
                        int theChar = value.charAt(i);
                        builder.append(String.format("%02x", theChar).toUpperCase());
                    }
                }
                break;
            }
            case ALL_BITMASK: {
                if (!inBrackets) {
                    builder.append('[');
                }
                builder.append('&').append(String.format("%02x", tree.getByteValue() & 0xFF));
                if (!inBrackets) {
                    builder.append(']');
                }
                break;
            }
            case RANGE: {
                if (!inBrackets) {
                    builder.append('[');
                }
                appendByteValue(tree.getChild(0).getIntValue(), sigType == CONTAINER, builder);
                builder.append(':');
                appendByteValue(tree.getChild(1).getIntValue(), sigType == CONTAINER, builder);
                if (!inBrackets) {
                    builder.append(']');
                }
                break;
            }
            case ANY: {
                builder.append("??");
                break;
            }
            case REPEAT: { // repeats only get used for fixed gaps in droid expressions, e.g. {5}
                builder.append('{').append(Integer.toString(tree.getChild(0).getIntValue())).append('}');
                break;
            }
            case REPEAT_MIN_TO_MANY: {
                builder.append('{').
                        append(Integer.toString(tree.getChild(0).getIntValue())).
                        append("-*}");
                break;
            }
            case REPEAT_MIN_TO_MAX: { // repeat min to max is only used for variable gaps.
                builder.append('{').
                        append(Integer.toString(tree.getChild(0).getIntValue())).
                        append('-').
                        append(Integer.toString(tree.getChild(1).getIntValue())).
                        append('}');
                break;
            }
            case SET: { // represent as multi-byte sets?  Or use (||) syntax?
                if (sigType == BINARY && allChildrenAreSingleBytes(tree)) {
                    appendAlternatives(tree, builder, sigType, spaceElements, inBrackets);
                } else {
                    if (!inBrackets) {
                        builder.append('[');
                        if (tree.isValueInverted()) {
                            builder.append('!');
                        }
                    }
                    for (int i = 0; i < tree.getNumChildren(); i++) {
                        toPRONOMExpression(tree.getChild(i), builder, sigType, spaceElements, true);
                    }
                    if (!inBrackets) {
                        builder.append(']');
                    }
                }
                break;
            }
            case ALTERNATIVES: {
                appendAlternatives(tree, builder, sigType, spaceElements, inBrackets);
                break;
            }
            case SEQUENCE: {
                for (int i = 0; i < tree.getNumChildren(); i++) {
                    toPRONOMExpression(tree.getChild(i), builder, sigType, spaceElements, inBrackets);
                }
                break;
            }
            case ZERO_TO_MANY: {
                builder.append('*');
                break;
            }
            default : throw new ParseException("Encountered an unknown node type: " + tree);
        }
        if (spaceElements) builder.append(' ');
    }

    private void appendAlternatives(ParseTree alternatives, StringBuilder builder,
                                           SignatureType sigType, boolean spaceElements, boolean inBrackets)  throws ParseException {
        if (!inBrackets) {
            builder.append('(');
        }
        boolean first = true;
        for (int i = 0; i < alternatives.getNumChildren(); i++) {
            if (!first) {
                builder.append('|');
            }
            toPRONOMExpression(alternatives.getChild(i), builder, sigType, spaceElements, true);
            first = false;
        }
        if (!inBrackets) {
            builder.append(')');
        }
    }

    private boolean allChildrenAreSingleBytes(ParseTree node) throws ParseException {
        for (int i = 0; i < node.getNumChildren(); i++) {
            ParseTree child = node.getChild(i);
            switch (child.getParseTreeType()) {
                case BYTE: {
                    break; // fine.
                }
                case STRING: {
                    String value = child.getTextValue();
                    if (!(value.length() == 1 && value.charAt(0) < 256)) {
                        return false;
                    }
                    break;
                }
                default : return false;
            }
        }
        return true;
    }

    private void appendByteValue(int value, boolean prettyPrint, StringBuilder builder) {
        if (prettyPrint && value >= ' ' && value <= '~') {
            builder.append('\'').append((char) value).append('\'');
        } else {
            builder.append(String.format("%02x", value).toUpperCase());
        }
    }

}
