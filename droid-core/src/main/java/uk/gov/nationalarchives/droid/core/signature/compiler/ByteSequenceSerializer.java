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
package uk.gov.nationalarchives.droid.core.signature.compiler;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.byteseek.compiler.CompileException;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.parser.ParseException;
import net.byteseek.parser.regex.RegexParser;
import net.byteseek.parser.tree.ParseTree;
import net.byteseek.parser.tree.ParseTreeType;
import net.byteseek.parser.tree.node.ByteNode;
import net.byteseek.parser.tree.node.ChildrenNode;

import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.SideFragment;
import uk.gov.nationalarchives.droid.core.signature.droid6.SubSequence;
import uk.gov.nationalarchives.droid.core.signature.xml.XmlUtils;

import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.BINARY;
import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.CONTAINER;

/**
 * A class which provides serialization methods for ByteSequences, toXML and to a PRONOM or byteseek expression.
 * This can be used to obtain XML from a signature, or to rewrite a signature into a different signature type,
 * e.g. you could put a binary signature into the serializer with container as the output target, and the
 * signature will be rewritten using the more advanced syntax.
 */
public final class ByteSequenceSerializer {

    /**
     * Convenient static serializer instance (it's all stateless, so you don't need more than one).
     */
    public static final ByteSequenceSerializer SERIALIZER = new ByteSequenceSerializer();

    /**
     * The name of the Sequence attribute in XML.
     */
    private static final String SEQUENCE = "Sequence";

    /**
     * An underlying byteseek parser used to transform byteseek expressions into an Abstract Syntax Tree.
     */
    private static final RegexParser PARSER = new RegexParser();

    /**
     * The name of the Reference attribute in XML.
     */
    private static final String REFERENCE = "Reference";

    /**
     * The name of the ByteSequence element in XML.
     */
    private static final String BYTE_SEQUENCE = "ByteSequence";

    /**
     * The name of the LeftFragment element in XML.
     */
    private static final String LEFT_FRAGMENT = "LeftFragment";

    /**
     * The name of the RightFragment element in XML.
     */
    private static final String RIGHT_FRAGMENT = "RightFragment";

    /**
     * The name of the Position attribute in XML.
     */
    private static final String POSITION = "Position";

    /**
     * The name of the MinOffset attribute in XML.
     */
    private static final String MIN_OFFSET = "MinOffset";

    /**
     * The name of the MaxOffset attribute in XML.
     */
    private static final String MAX_OFFSET = "MaxOffset";

    /**
     * The name of the SubSequence element in XML.
     */
    private static final String SUB_SEQUENCE = "SubSequence";

    /**
     * The name of the SubSeqMinOffset attribute in XML.
     */
    private static final String SUB_SEQ_MIN_OFFSET = "SubSeqMinOffset";

    /**
     * The name of the SubSeqMaxOffset attribute in XML.
     */
    private static final String SUB_SEQ_MAX_OFFSET = "SubSeqMaxOffset";

    /**
     * A format string to output two hex digits.
     */
    private static final String HEXDIGITS = "%02x";

    /**
     * Returns the XML for a PRONOM expression.
     *
     * @param sequence The PRONOM expression in either binary or container syntax.
     * @param anchor   Whether the expression is anchored to BOF, EOF or is variable.
     * @param compileType Whether to compile the XML with a PRONOM target, or a DROID target.  DROID supports longer anchor sequences.
     * @param sigType  Whether the XML is intended to be binary compatible, or container compatible.
     * @return An XML string containing the output of compiling the PRONOM expression.
     * @throws CompileException If anything goes wrong during the compilation.
     */
    public String toXML(String sequence, ByteSequenceAnchor anchor, ByteSequenceCompiler.CompileType compileType,
                        SignatureType sigType) throws CompileException {
        return toXML(ByteSequenceCompiler.COMPILER.compile(sequence, anchor, compileType), sigType);
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
        try {
            return XmlUtils.toXmlString(doc, false);
        } catch (TransformerException e) {
            throw new CompileException(e.getMessage(), e);
        }
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
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return dBuilder.newDocument();
    }



    private Element createByteSequenceElement(Document doc, ByteSequence sequence) {
        Element byteSequence = doc.createElement(BYTE_SEQUENCE);
        byteSequence.setAttribute(REFERENCE, sequence.getReference());
        if (!sequence.getSequence().isEmpty()) {
            byteSequence.setAttribute(SEQUENCE, sequence.getSequence());
        }
        doc.appendChild(byteSequence);

        return byteSequence;
    }

    private Element createSubSequenceElement(Document doc, SubSequence sub, SignatureType sigType, int position) throws ParseException {
        Element subSequence = createBasicSubSequenceElement(doc, sub, sigType, position);
        appendFragments(doc, subSequence, sub.getLeftFragments(), LEFT_FRAGMENT, sigType);
        appendFragments(doc, subSequence, sub.getRightFragments(), RIGHT_FRAGMENT, sigType);
        return subSequence;
    }

    private void appendFragments(Document doc, Element subsequence, List<List<SideFragment>> fragments, String elementName,
                                 SignatureType sigType) throws ParseException {
        int fragPos = 0;
        for (List<SideFragment> fragsAtPos : fragments) {
            fragPos++;
            for (SideFragment frag : fragsAtPos) {
                Element fragment = doc.createElement(elementName);
                fragment.setAttribute(POSITION, Integer.toString(fragPos));
                fragment.setAttribute(MIN_OFFSET, Integer.toString(frag.getMinOffset()));
                fragment.setAttribute(MAX_OFFSET, Integer.toString(frag.getMaxOffset()));
                fragment.setTextContent(getSequenceMatcherExpression(frag.getMatcher(), sigType));
                subsequence.appendChild(fragment);
            }
        }
    }

    private Element createBasicSubSequenceElement(Document doc, SubSequence sub, SignatureType sigType, int position) throws ParseException {
        Element subSequence = doc.createElement(SUB_SEQUENCE);
        subSequence.setAttribute(POSITION, Integer.toString(position));
        subSequence.setAttribute(SUB_SEQ_MIN_OFFSET, Integer.toString(sub.getMinSeqOffset()));
        subSequence.setAttribute(SUB_SEQ_MAX_OFFSET, Integer.toString(sub.getMaxSeqOffset()));

        // Add sequence element containing the signature to search for:
        Element seq = doc.createElement(SEQUENCE);
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
        toPRONOMExpression(tree, builder, sigType, spaceElements, false, false);
        return builder.toString();
    }

    //CHECKSTYLE:OFF - cyclomatic complexity too high.
    private void toPRONOMExpression(final ParseTree tree, final StringBuilder builder, SignatureType sigType, boolean spaceElements, final boolean inSet, final boolean inAlternatives) throws ParseException {
        switch (tree.getParseTreeType()) {
            case BYTE: {
                builder.append(String.format(HEXDIGITS, tree.getByteValue() & 0xFF).toUpperCase());
                break;
            }
            case STRING: { // If processing a string in a set ['abc'] as alternatives, have to process as ('a'|'b'|'c')
                String value = tree.getTextValue();
                if (sigType == CONTAINER) { //CONTAINER SIG FORMAT: output strings as strings.
                    builder.append('\'').append(value).append('\'');
                } else {                    //BINARY SIG FORMAT: output strings as byte sequences.
                    for (int i = 0; i < value.length(); i++) {
                        int theChar = value.charAt(i);
                        builder.append(String.format(HEXDIGITS, theChar).toUpperCase());
                    }
                }
                break;
            }
            case ALL_BITMASK: {
                if (!inSet) {
                    builder.append('[');
                }
                builder.append('&').append(String.format(HEXDIGITS, tree.getByteValue() & 0xFF));
                if (!inSet) {
                    builder.append(']');
                }
                break;
            }
            case RANGE: {
                if (!inSet) {
                    builder.append('[');
                }
                appendByteValue(tree.getChild(0).getIntValue(), sigType == CONTAINER, builder);
                builder.append(':');
                appendByteValue(tree.getChild(1).getIntValue(), sigType == CONTAINER, builder);
                if (!inSet) {
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
            case SET: { // represent as multi-byte sets [abc]?  Or use (a|b|c) syntax?
                ParseTree alternativeSet = detectAlternativeSetRanges(tree, sigType);
                if (alternativeSet != null) {
                    toPRONOMExpression(alternativeSet, builder, sigType, spaceElements, false, inAlternatives);
                } else if (sigType == BINARY && allChildrenAreSingleBytes(tree)) {
                    appendAlternatives(tree, builder, sigType, spaceElements, true, inAlternatives);
                } else {
                    if (!inSet) {
                        builder.append('[');
                        if (tree.isValueInverted()) {
                            builder.append('!');
                        }
                    }
                    for (int i = 0; i < tree.getNumChildren(); i++) {
                        if (spaceElements && i > 0) builder.append(' ');
                        toPRONOMExpression(tree.getChild(i), builder, sigType, spaceElements, true, inAlternatives);
                    }
                    if (!inSet) {
                        builder.append(']');
                    }
                }
                break;
            }
            case ALTERNATIVES: {
                appendAlternatives(tree, builder, sigType, spaceElements, false, inAlternatives);
                break;
            }
            case SEQUENCE: {
                for (int i = 0; i < tree.getNumChildren(); i++) {
                    if (spaceElements && i > 0) builder.append(' ');
                    toPRONOMExpression(tree.getChild(i), builder, sigType, spaceElements, inSet, inAlternatives);
                }
                break;
            }
            case ZERO_TO_MANY: {
                builder.append('*');
                break;
            }
            default : throw new ParseException("Encountered an unknown node type: " + tree);
        }
    }

    private ParseTree detectAlternativeSetRanges(ParseTree node, SignatureType sigType) throws ParseException {
        final int numChildren = node.getNumChildren();
        // Only try this for sets large enough for two alternative ranges to matter syntactically
        if (numChildren > 16) {
            // Build up a bitset of all byte values in the set:
            BitSet bitset = new BitSet(256);
            int minPos = 256;
            int maxPos = -1;
            for (int i = 0; i < numChildren; i++) {
                ParseTree child = node.getChild(i);
                if (child.getParseTreeType() == ParseTreeType.BYTE) {
                    final int byteValue = child.getIntValue();
                    minPos = Math.min(byteValue, minPos);
                    maxPos = Math.max(byteValue, maxPos);
                    bitset.set(byteValue);
                } else {
                    return null; // If the set has more than just bytes in it, it's not going to be two or more ranges.
                }
            }

            // Scan the bitset to find contiguous ranges of set bits:
            List<Integer> startRangePos = new ArrayList<>();
            List<Integer> endRangePos = new ArrayList<>();

            boolean lastBitSet = false;
            int startPos = -1;
            for (int bitPos = minPos; bitPos < maxPos; bitPos++) {
                if (bitset.get(bitPos)) {
                    if (!lastBitSet) { // first bit set in a possible range:
                        startPos = bitPos;
                    }
                    lastBitSet = true;
                } else {
                    if (lastBitSet) { // the last bit was set, this isn't, so is the end of a possible range.
                        int length = bitPos - startPos;
                        if (length < 4) {
                            return null; // small ranges aren't going to be usefully represented with alternative range syntax.
                        }
                        if (startRangePos.size() > 4) {
                            return null; // lots of ranges aren't going to be usefully represented with alternative range syntax.
                        }
                        startRangePos.add(startPos);
                        endRangePos.add(bitPos - 1);
                        startPos = -1;
                    }
                    lastBitSet = false;
                }
            }
            if (lastBitSet) {
                startRangePos.add(startPos);
                endRangePos.add(maxPos);
            }

            // Build nodes representing the ranges detected:
            List<ParseTree> rangeChildren = new ArrayList<>();
            for (int i = 0; i < startRangePos.size(); i++) {
                ParseTree rangeStart = new ByteNode((byte) startRangePos.get(i).intValue());
                ParseTree rangeEnd   = new ByteNode((byte) endRangePos.get(i).intValue());
                ParseTree rangeNode = new ChildrenNode(ParseTreeType.RANGE, rangeStart, rangeEnd);
                rangeChildren.add(rangeNode);
            }

            // Return the set of ranges, using () syntax for binary, and [] syntax for container signatures:
            final ParseTreeType nodeType = sigType == BINARY ? ParseTreeType.ALTERNATIVES : ParseTreeType.SET;
            return new ChildrenNode(nodeType, rangeChildren);
        }
        return null;
    }

    private void appendAlternatives(ParseTree alternatives, StringBuilder builder,
                                    SignatureType sigType, boolean spaceElements, boolean inSet, boolean inAlternatives)  throws ParseException {
        if (!inAlternatives) {
            builder.append('(');
        }
        for (int i = 0; i < alternatives.getNumChildren(); i++) {
            if (i > 0) {
                appendAlternativePipe(builder, spaceElements);
            }
            ParseTree alternative = alternatives.getChild(i);

            // If we're processing a set as a list of alternatives, we need to handle strings differently.
            // Strings in sets need to be broken into distinct bytes separated by |.
            if (inSet && alternative.getParseTreeType() == ParseTreeType.STRING) {
                appendStringAsAlternativeBytes(builder, alternative.getTextValue(), spaceElements);
            } else {
                toPRONOMExpression(alternatives.getChild(i), builder, sigType, spaceElements, false, true);
            }
        }
        if (!inAlternatives) {
            builder.append(')');
        }
    }

    private void appendStringAsAlternativeBytes(StringBuilder builder, String value, boolean spaceElements) throws ParseException {
        for (int charIndex = 0; charIndex < value.length(); charIndex++) {
            if (charIndex > 0) {
                appendAlternativePipe(builder, spaceElements);
            }
            char theChar = value.charAt(charIndex);
            if (theChar > 255) {
                throw new ParseException("Could not process a char in a string with a value higher than 255: " + theChar);
            }
            builder.append(String.format(HEXDIGITS, (int) theChar));
        }
    }

    private void appendAlternativePipe(StringBuilder builder, boolean spaceElements) {
        if (spaceElements) builder.append(' ');
        builder.append('|');
        if (spaceElements) builder.append(' ');
    }

    private boolean allChildrenAreSingleBytes(ParseTree node) throws ParseException {
        for (int i = 0; i < node.getNumChildren(); i++) {
            ParseTree child = node.getChild(i);
            switch (child.getParseTreeType()) {
                case BYTE: case STRING: {
                    break; // fine.
                }
                //TODO: are there other things we can put in alternative syntax like ranges?  e.g. ([&01] | 02 | 03).
                default : return false;
            }
        }
        return true;
    }

    private void appendByteValue(int value, boolean prettyPrint, StringBuilder builder) {
        if (prettyPrint && value >= ' ' && value <= '~') {
            builder.append('\'').append((char) value).append('\'');
        } else {
            builder.append(String.format(HEXDIGITS, value).toUpperCase());
        }
    }

}
