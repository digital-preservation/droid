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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.byteseek.compiler.CompileException;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.parser.ParseException;
import net.byteseek.parser.tree.ParseTree;
import net.byteseek.parser.tree.ParseTreeType;
import net.byteseek.parser.tree.ParseTreeUtils;
import net.byteseek.parser.tree.node.BaseNode;
import net.byteseek.parser.tree.node.ChildrenNode;

import net.byteseek.utils.ByteUtils;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.SideFragment;
import uk.gov.nationalarchives.droid.core.signature.droid6.SubSequence;

import static uk.gov.nationalarchives.droid.core.signature.compiler.ByteSequenceAnchor.EOFOffset;

/**
 * class to compile a ByteSequence from a DROID syntax regular expression created by {@link ByteSequenceParser}.
 *
 * See main method {@link #compile(ByteSequence, String, ByteSequenceAnchor, CompileType)}.
 *
 * @author Matt Palmer
 */
public final class ByteSequenceCompiler {

    /**
     * Convenient static parser (there is no state, so we can just have a static compiler).
     */
    public static final ByteSequenceCompiler COMPILER = new ByteSequenceCompiler();

    /**
     * The maximum number of bytes which can match in a single position in an anchoring sequence.
     *
     * High values can cause performance problems for many search algorithms.
     * Low values impact performance a bit by forcing us to process simple matchers as fragments.
     * We want to allow some matching constructs in anchors which match more than one byte.
     * The biggest downside happens with large numbers of bytes, so we bias towards a lower number of bytes.
     */
    private static final int MAX_MATCHING_BYTES = 64;

    /**
     * Compilation type to build the objects from the expression.
     *
     * The length of the anchoring sequences can be different - PRONOM only allows straight bytes in anchors.
     */
    public enum CompileType {
        /**
         * Supports PRONOM syntax.
         */
        PRONOM,

        /**
         * Supports a super-set of the PRONOM syntax.
         */
        DROID
    }

    /**
     * A byteseek compiler used to compile the SequenceMatchers which are matched and searched for.
     */
    private static final SequenceMatcherCompiler MATCHER_COMPILER = new SequenceMatcherCompiler();

    /**
     * A re-usable node for all * syntax.  Reduces garbage collection to re-use it each time.
     */
    private static final ParseTree ZERO_TO_MANY = new ChildrenNode(ParseTreeType.ZERO_TO_MANY, BaseNode.ANY_NODE);

    /**
     * Compiles a ByteSequence from a DROID syntax regular expression, starting from a new byteSequence.
     * <p>
     * It is assumed that
     * <ul>
     *     <li>the compileType is DROID</li>
     *     <li>it is anchored to the BOF</li>
     * </ul>
     *
     * @param droidExpression The string containing a DROID syntax regular expression.
     * @throws CompileException If there is a problem compiling the DROID regular expression.
     * @return the compiled byteSequence
     */
    public ByteSequence compile(final String droidExpression) throws CompileException {
        return compile(droidExpression, ByteSequenceAnchor.BOFOffset, CompileType.DROID);
    }

    /**
     * Compiles a ByteSequence from a DROID syntax regular expression, compiling for DROID rather than PRONOM.
     *
     * @param droidExpression The string containing a DROID syntax regular expression.
     * @param anchor How the ByteSequence is to be anchored to the BOF, EOF or a variable search from BOF.
     * @return a ByteSequence from a DROID syntax regular expression, compiling for DROID rather than PRONOM.
     * @throws CompileException If there is a problem compiling the DROID regular expression.
     */
    public ByteSequence compile(final String droidExpression, final ByteSequenceAnchor anchor) throws CompileException {
        return compile(droidExpression, anchor, CompileType.DROID);
    }

    /**
     * Compiles a {@link ByteSequence} from a DROID syntax regular expression, starting from a new byte sequence.
     *
     * @param droidExpression The string containing a DROID syntax regular expression.
     * @param anchor How the ByteSequence is to be anchored to the BOF, EOF or a variable search from BOF.
     * @param compileType how to build the objects from the expression.
     * @throws CompileException If there is a problem compiling the DROID regular expression.
     * @return the compiled byteSequence
     */
    public ByteSequence compile(final String droidExpression,
                                final ByteSequenceAnchor anchor,
                                final CompileType compileType) throws CompileException {
        final ByteSequence newByteSequence = new ByteSequence();
        newByteSequence.setReference(anchor.getAnchorText());
        compile(newByteSequence, droidExpression, compileType);
        return newByteSequence;
    }

    /**
     * Compiles a {@link ByteSequence} from a DROID syntax regular expression.
     * <p>
     * It is assumed that
     * <ul>
     *     <li>the compileType is DROID</li>
     *     <li>it is anchored to the BOF</li>
     * </ul>
     *
     * @param sequence The ByteSequence which will be altered by compilation.
     * @param droidExpression The string containing a DROID syntax regular expression.
     * @throws CompileException If there is a problem compiling the DROID regular expression.
     */
    public void compile(final ByteSequence sequence, final String droidExpression) throws CompileException {
        compile(sequence, droidExpression, ByteSequenceAnchor.BOFOffset, CompileType.DROID);
    }

    /**
     * Compiles a ByteSequence from a DROID syntax regular expression, and how it is anchored to the BOF or EOF (or is a
     * variable type of anchor).
     *
     * @param sequence The ByteSequence which will be altered by compilation.
     * @param droidExpression The string containing a DROID syntax regular expression.
     * @param anchor How the ByteSequence is to be anchored to the BOF, EOF or a variable search from BOF.
     * @throws CompileException If there is a problem compiling the DROID regular expression.
     */
    public void compile(final ByteSequence sequence, final String droidExpression, ByteSequenceAnchor anchor) throws CompileException {
        compile(sequence, droidExpression, anchor, CompileType.DROID);
    }

    /**
     * Compiles a ByteSequence from a DROID syntax regular expression, and how it is anchored to the BOF or EOF (or is a
     * variable type of anchor).
     *
     * @param sequence The ByteSequence which will be altered by compilation.
     * @param droidExpression The string containing a DROID syntax regular expression.
     * @param anchor How the ByteSequence is to be anchored to the BOF, EOF or a variable search from BOF.
     * @param compileType how to build the objects from the expression.
     * @throws CompileException If there is a problem compiling the DROID regular expression.
     */
    public void compile(final ByteSequence sequence,
                        final String droidExpression,
                        final ByteSequenceAnchor anchor,
                        final CompileType compileType) throws CompileException {
        sequence.setReference(anchor.getAnchorText());
        compile(sequence, droidExpression, compileType);
    }

    /**
     * Compiles a ByteSequence from a DROID syntax regular expression.
     * <p>
     * It is assumed that the ByteSequence has already defined how it is anchored to the BOF or EOF (or is a variable
     * type of anchor).
     *
     * @param sequence The ByteSequence which will be altered by compilation.
     * @param droidExpression The string containing a DROID syntax regular expression.
     * @param compileType how to build the objects from the expression.
     * @throws CompileException If there is a problem compiling the DROID regular expression.
     */
    public void compile(final ByteSequence sequence,
                        final String droidExpression,
                        final CompileType compileType) throws CompileException {
        try {
            // Parse the expression into an abstract syntax tree (AST)
            final ParseTree sequenceNodes = ByteSequenceParser.PARSER.parse(droidExpression);

            // Compile the ByteSequence from the AST:
            compile(sequence, sequenceNodes, compileType);
        } catch (ParseException ex) {
            throw new CompileException(ex.getMessage(), ex);
        }
    }

    /**
     * Compiles a ByteSequence from an abstract syntax tree (AST).
     * <p>
     * 1.  Pre-processes the AST nodes to ensure wildcards are placed into the correct sub-sequence.
     * 2.  Compiles each subsequence in turn and adds it to the ByteSequence.
     *
     * @param sequence      The ByteSequence object to which SubSequences will be added.
     * @param sequenceNodes An Abstract Syntax Tree of a droid expression.
     * @param compileType how to build the objects from the expression.
     * @throws CompileException If there is a problem compiling the AST.
     */
    private void compile(final ByteSequence sequence,
                         final ParseTree sequenceNodes,
                         final CompileType compileType) throws CompileException {
        final boolean anchoredToEnd = EOFOffset.getAnchorText().equals(sequence.getReference());

        // Pre-process the parse tree, re-ordering some wildcards on subsequence boundaries.
        // Some wildcards at the stopValue or beginning of subsequences belong in the next/prior subsequence object,
        // depending on whether the overall byte sequence is anchored to the beginning or stopValue of a file.
        // It also replaces MIN_TO_MANY nodes with a REPEAT node (MIN) and a ZERO_TO_MANY node (MANY),
        // since these are logically equivalent, but easier to subsequently process once split.
        // The result of pre-processing is that each distinct subsequence is then trivially identified,
        // and contains all the information it needs to be built, without having to refer to information at the
        // stopValue or start of another subsequence.
        final List<ParseTree> sequenceList = preprocessSequence(sequenceNodes, anchoredToEnd, compileType);
        final int numNodes = sequenceList.size();

        // Scan through all the syntax tree nodes, building subsequences as we go (separated by * nodes):
        int subSequenceStart = 0;
        for (int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
            if (sequenceList.get(nodeIndex).getParseTreeType() == ParseTreeType.ZERO_TO_MANY) {
                sequence.addSubSequence(buildSubSequence(sequenceList, subSequenceStart, nodeIndex, anchoredToEnd, compileType));
                subSequenceStart = nodeIndex + 1;
            }
        }

        // Add final SubSequence, if any:
        if (subSequenceStart < numNodes) {
            sequence.addSubSequence(buildSubSequence(sequenceList, subSequenceStart, numNodes, anchoredToEnd, compileType));
        }

        // ByteSequence is now compiled.  Note that to use it for searching,
        // you still need to call prepareForUse() on it afterwards, just as when it's built from XML.
        // Don't call prepareForUse() here as you can get an infinite loop.
        // (prepareForUse() will invoke this compiler if a sequence attribute is set on a ByteSequence.)
    }

    //CHECKSTYLE:OFF - cyclomatic complexity too high.
    /**
     * This method processes the child nodes of a SEQUENCE ParseTree type into a List<ParseTree>.
     * We can't directly affect the children of SEQUENCE types, but we want to change some of the children and
     * optimise them.  So we build a new list, performing whatever optimisations are needed along the way.
     *
     * Along the way, it optimises nodes it can usefully optimise,
     * and re-orders wildcards around * wildcards to make subsequent SubSequence compilation easier.
     *
     * If there is nothing special to do for a particular type of node, it's just added to the list.
     * The switch statement does not need to look for all types of node, only the ones that something needs to
     * be done for (or which affect the processing of other nodes).
     *
     * @param sequenceNodes A ParseTree node with child nodes to process.
     * @param anchoredToEnd If the search sequence is anchored to the end .
     * @param compileType Whether we are compiling for PRONOM or DROID.
     * @return A list of ParseTrees ready for further compilation.
     * @throws CompileException If there was a problem processing the sequence.
     */
    private List<ParseTree> preprocessSequence(final ParseTree sequenceNodes, final boolean anchoredToEnd, final CompileType compileType) throws CompileException {
        // Iterate across the nodes in the SEQUENCE ParseTree type.
        // If the sequence is anchored to the end, we process the nodes in reverse order.
        final int numNodes = sequenceNodes.getNumChildren();
        final IntIterator index = anchoredToEnd ? new IntIterator(numNodes - 1, 0) : new IntIterator(0, numNodes - 1);
        final List<ParseTree> sequenceList = new ArrayList<>();
        int lastValuePosition = -1;
        while (index.hasNext()) {
            int currentIndex = index.next();
            final ParseTree node = sequenceNodes.getChild(currentIndex);
            sequenceList.add(node);
            switch (node.getParseTreeType()) {

                /*
                 * Process types that match byte values, tracking the last time we saw something that matches bytes:
                 */
                case BYTE: case RANGE: case STRING: case ALL_BITMASK: case SET: case ANY: {
                    lastValuePosition = sequenceList.size() - 1;
                    break;
                }

                case ALTERNATIVES: {
                    lastValuePosition = sequenceList.size() - 1;
                    if (compileType == CompileType.DROID) {
                        sequenceList.set(sequenceList.size() - 1, optimiseSingleByteAlternatives(node));
                    }
                    break;
                }

                /*
                 * Process * wildcard types that form a sub-sequence boundary.
                 * At this point, we want to ensure that the * wildcard is right next to the last value type we saw.
                 *
                 * This is because it makes subsequent compilation into DROID objects easier if we ensure that any
                 * gaps, e.g. {10}, exist at the start of a subsequence rather than the end of the previous one.
                 *
                 * For example, the sequence:
                 *
                 *    01 03 04 {10} * 05 06 07
                 *
                 * has a {10} gap at the end of the first subsequence.  But it's easier to compile if it instead reads:
                 *
                 *    01 03 04 * {10} 05 06 07
                 *
                 * This is because to model this in DROID objects, the {10} gap is a property of the following
                 * subsequence, not the one it was actually defined in.  It's equivalent whether a fixed gap happens at
                 * end of one sequence, or the beginning of the next - but the objects expect inter-subsequence gaps
                 * to be recorded in the following subsequence.
                 *
                 */
                case ZERO_TO_MANY: { // subsequence boundary.
                    // If the last value is not immediately before the * wildcard, we have some wildcard gaps between
                    // it and the next subsequence.  Move the wildcard after the last value position.
                    if (lastValuePosition + 1 < sequenceList.size() - 1) {
                        // insert zero to many node after last value position.
                        sequenceList.add(lastValuePosition + 1, node);
                        // remove the current zero to many node.
                        sequenceList.remove(sequenceList.size() - 1);
                    }
                    break;
                }
                case REPEAT_MIN_TO_MANY: { // subsequence boundary {n,*} - Change the {n-*} into a * followed by an {n}.
                    // Replace the current {n-*} node with a repeat {n} node.
                    sequenceList.set(sequenceList.size() - 1,
                            new ChildrenNode(ParseTreeType.REPEAT, node.getChild(0), BaseNode.ANY_NODE));
                    // Insert the * wildcard just after the last value position.
                    sequenceList.add(lastValuePosition + 1, ZERO_TO_MANY);
                    break;
                }

                default: {
                    // Do nothing.  It is not an error to encounter a type we don't need to pre-process in some way.
                }
            }
        }

        // If we processed the nodes in reverse order, reverse the final list to get the nodes back in normal order.
        if (anchoredToEnd) {
            Collections.reverse(sequenceList);
        }
        return sequenceList;
    }

    /**
     * Looks for alternatives which match several different single bytes.
     * These can be more efficiently represented in DROID using a Set matcher from byteseek,
     * rather than a list of SideFragments in DROID itself.
     *
     * Instead of (01|02|03) we would like [01 02 03].
     * Also, instead of (&01|[10:40]|03) we can have [&01 10:40 03]
     * If we have alternatives with some sequences in them, we can still optimise the single byte alternatives, e.g.
     * Instead of (01|02|03|'something else') we get ([01 02 03] | 'something else')
     * <p>
     * @param node The node to optimise
     * @return An optimised alternative node, or the original node passed in if no optimisations can be done.
     */
    private ParseTree optimiseSingleByteAlternatives(final ParseTree node) throws CompileException {
        if (node.getParseTreeType() == ParseTreeType.ALTERNATIVES) {

            // Locate any single byte alternatives:
            final Set<Integer> singleByteIndexes = new HashSet<>();
            for (int i = 0; i < node.getNumChildren(); i++) {
                ParseTree child = node.getChild(i);
                switch (child.getParseTreeType()) {
                    case BYTE: case RANGE: case ALL_BITMASK: case SET: {
                        singleByteIndexes.add(i);
                        break;
                    }
                    case STRING: { // A single char (<256) string can be modelled as an ISO-8859-1 byte.
                        final String value;
                        try {
                            value = child.getTextValue();
                        } catch (ParseException e) {
                            throw new CompileException(e.getMessage(), e);
                        }
                        if (value.length() == 1 && value.charAt(0) < 256) {
                            singleByteIndexes.add(i);
                        }
                        break;
                    }
                }
            }

            // If there is more than one single byte value, we can optimise them into just one SET:
            if (singleByteIndexes.size() > 1) {
                final List<ParseTree> newAlternativeChildren = new ArrayList<>();
                final List<ParseTree> setChildren = new ArrayList<>();
                for (int i = 0; i < node.getNumChildren(); i++) {
                    final ParseTree child = node.getChild(i);
                    if (singleByteIndexes.contains(i)) {
                        setChildren.add(child);
                    } else {
                        newAlternativeChildren.add(child);
                    }
                }
                final ParseTree setNode = new ChildrenNode(ParseTreeType.SET, setChildren);

                // If there are no further alternatives as they were all single byte values, just return the set:
                if (newAlternativeChildren.isEmpty()) {
                    return setNode;
                }

                // Otherwise we have some single bytes optimised, but part of a bigger set of non single byte alternatives.
                newAlternativeChildren.add(setNode);
                return new ChildrenNode(ParseTreeType.ALTERNATIVES, newAlternativeChildren);
            }
        }
        // No change to original node - just return it.
        return node;
    }

    private SubSequence buildSubSequence(final List<ParseTree> sequenceList,
                                         final int subSequenceStart, final int subSequenceEnd,
                                         final boolean anchoredToEOF,
                                         final CompileType compileType) throws CompileException {
        /// Find the anchoring search sequence:
        final IntPair anchorRange = locateSearchSequence(sequenceList, subSequenceStart, subSequenceEnd, compileType);
        if (anchorRange == NO_RESULT) {
            throw new CompileException("No anchoring sequence could be found in a subsequence.");
        }

        final ParseTree anchorSequence  = createSubSequenceTree(sequenceList, anchorRange.firstInt, anchorRange.secondInt);
        final SequenceMatcher anchorMatcher = MATCHER_COMPILER.compile(anchorSequence);

        final List<List<SideFragment>> leftFragments = new ArrayList<>();
        final IntPair leftOffsets =
                createLeftFragments(sequenceList, leftFragments,
                        anchorRange.firstInt - 1, subSequenceStart);

        final List<List<SideFragment>> rightFragments = new ArrayList<>();
        final IntPair rightOffsets =
                createRightFragments(sequenceList, rightFragments,
                        anchorRange.secondInt + 1, subSequenceEnd - 1);

        // Choose which remaining fragment offsets are used for the subsequence offsets as a whole:
        final IntPair subSequenceOffsets = anchoredToEOF ? rightOffsets : leftOffsets;

        return new SubSequence(anchorMatcher, leftFragments, rightFragments, subSequenceOffsets.firstInt, subSequenceOffsets.secondInt);
    }

    //CHECKSTYLE:OFF - cyclomatic complexity too high.
    private IntPair createLeftFragments(final List<ParseTree> sequenceList, final List<List<SideFragment>> leftFragments,
                                        final int fragmentStart, final int fragmentEnd) throws CompileException {
        int position = 1;
        int minGap   = 0;
        int maxGap   = 0;
        int startValueIndex = Integer.MAX_VALUE;
        int endValueIndex = Integer.MAX_VALUE;
        for (int fragmentIndex = fragmentStart; fragmentIndex >= fragmentEnd; fragmentIndex--) {
            final ParseTree node = sequenceList.get(fragmentIndex);
            switch (node.getParseTreeType()) {
                case BYTE:
                case ANY:
                case RANGE:
                case ALL_BITMASK:
                case SET:
                case STRING: { // nodes carrying a match for a byte or bytes:
                    // Track when we first encounter the stopValue of a fragment sequence (we hit this first as we go backwards for left fragments):
                    if (endValueIndex == Integer.MAX_VALUE) {
                        endValueIndex = fragmentIndex;
                    }
                    // Continuously track the start of any fragment.
                    startValueIndex = fragmentIndex;
                    break;
                }

                case REPEAT:
                case REPEAT_MIN_TO_MAX: { // wildcard gaps
                    if (startValueIndex == fragmentIndex + 1) { // Add any previous value not yet processed.
                        leftFragments.add(buildFragment(sequenceList, startValueIndex, endValueIndex, minGap, maxGap, position));
                        position++;
                        minGap = 0;
                        maxGap = 0;
                        startValueIndex = Integer.MAX_VALUE;
                        endValueIndex   = Integer.MAX_VALUE;
                    }
                    int nodeMin = getMinGap(node);
                    int nodeMax = getMaxGap(node); // will be zero if no max set.
                    if (nodeMax == 0) {
                        nodeMax = nodeMin; // should be minimum of min.
                    }
                    minGap += nodeMin;
                    maxGap += nodeMax;
                    break;
                }

                case ALTERNATIVES: { // a set of alternatives - these always form fragments of their own.
                    if (startValueIndex == fragmentIndex + 1) { // Add any previous value not yet processed:
                        leftFragments.add(buildFragment(sequenceList, startValueIndex, endValueIndex, minGap, maxGap, position));
                        position++;
                        minGap = 0;
                        maxGap = 0;
                    }
                    // Add alternatives
                    leftFragments.add(compileAlternatives(node, minGap, maxGap, position));
                    position++;
                    minGap = 0;
                    maxGap = 0;
                    startValueIndex = Integer.MAX_VALUE;
                    endValueIndex   = Integer.MAX_VALUE;
                    break;
                }

                default: throw new CompileException("Unknown node type: " + node + " found at node index: " + fragmentIndex);
            }
        }

        // Add any final unprocessed value fragment at start:
        if (startValueIndex == fragmentEnd) {
            leftFragments.add(buildFragment(sequenceList, startValueIndex, endValueIndex, minGap, maxGap, position));
            minGap = 0;
            maxGap = 0;
        }

        // If we never got a min-max range (just min ranges), the max may still be set to zero. Must be at least min.
        if (maxGap < minGap) {
            maxGap = minGap;
        }

        // Return any final min / max gap left over at the start of the left fragments:
        return new IntPair(minGap, maxGap);
    }

    private IntPair createRightFragments(final List<ParseTree> sequenceList, final List<List<SideFragment>> rightFragments,
                                         final int fragmentStart, final int fragmentEnd) throws CompileException {
        int position = 1;
        int minGap = 0;
        int maxGap = 0;
        int startValueIndex = Integer.MAX_VALUE;
        int endValueIndex = Integer.MAX_VALUE;
        for (int fragmentIndex = fragmentStart; fragmentIndex <= fragmentEnd; fragmentIndex++) {
            final ParseTree node = sequenceList.get(fragmentIndex);
            switch (node.getParseTreeType()) {

                case BYTE:
                case ANY:
                case RANGE:
                case ALL_BITMASK:
                case SET:
                case STRING: { // nodes carrying a match for a byte or bytes:
                    // Track when we first encounter the start of a fragment sequence
                    if (startValueIndex == Integer.MAX_VALUE) {
                        startValueIndex = fragmentIndex;
                    }
                    // Continuously track the stopValue of any fragment.
                    endValueIndex = fragmentIndex;
                    break;
                }

                case REPEAT:
                case REPEAT_MIN_TO_MAX: { // wildcard gaps
                    if (endValueIndex == fragmentIndex - 1) { // Add any previous value not yet processed.
                        rightFragments.add(buildFragment(sequenceList, startValueIndex, endValueIndex, minGap, maxGap, position));
                        position++;
                        minGap = 0;
                        maxGap = 0;
                        startValueIndex = Integer.MAX_VALUE;
                        endValueIndex = Integer.MAX_VALUE;
                    }
                    int nodeMin = getMinGap(node);
                    int nodeMax = getMaxGap(node); // will be zero if no max set.
                    if (nodeMax == 0) {
                        nodeMax = nodeMin; // should be minimum of min.
                    }
                    minGap += nodeMin;
                    maxGap += nodeMax;
                    break;
                }

                case ALTERNATIVES: { // a set of alternatives - these always form fragments of their own.
                    if (endValueIndex == fragmentIndex - 1) { // Add any previous value not yet processed:
                        rightFragments.add(buildFragment(sequenceList, startValueIndex, endValueIndex, minGap, maxGap, position));
                        position++;
                        minGap = 0;
                        maxGap = 0;
                    }
                    // Add alternatives
                    rightFragments.add(compileAlternatives(node, minGap, maxGap, position));
                    position++;
                    minGap = 0;
                    maxGap = 0;
                    startValueIndex = Integer.MAX_VALUE;
                    endValueIndex = Integer.MAX_VALUE;
                    break;
                }

                default:
                    throw new CompileException("Unknown node type: " + node + " found at node index: " + fragmentIndex);
            }
        }

        // Add any final unprocessed value fragment at stopValue:
        if (endValueIndex == fragmentEnd) {
            rightFragments.add(buildFragment(sequenceList, startValueIndex, endValueIndex, minGap, maxGap, position));
            minGap = 0;
            maxGap = 0;
        }

        // If we never got a min-max range (just min ranges), the max may still be set to zero. Must be at least min.
        if (maxGap < minGap) {
            maxGap = minGap;
        }

        // Return any final min / max gap left over at the start of the left fragments:
        return new IntPair(minGap, maxGap);
    }

    private List<SideFragment> buildFragment(List<ParseTree> sequenceList, int startValueIndex, int endValueIndex,
                                             int minGap, int maxGap, int position) throws CompileException {
        final List<SideFragment> fragments = new ArrayList<>();
        final ParseTree fragmentTree = createSubSequenceTree(sequenceList, startValueIndex, endValueIndex);
        final SequenceMatcher matcher = MATCHER_COMPILER.compile(fragmentTree);
        final int minMax = maxGap < minGap ? minGap : maxGap;
        final SideFragment fragment = new SideFragment(matcher, minGap, minMax, position);
        fragments.add(fragment);
        return fragments;
    }

    private ParseTree createSubSequenceTree(List<ParseTree> sequenceList, int subSequenceStart, int subSequenceEnd) {
        // Sublist uses an exclusive end index, so we have to add one to the end position we have to get the right list.
        return new ChildrenNode(ParseTreeType.SEQUENCE, sequenceList.subList(subSequenceStart, subSequenceEnd + 1));
    }

    private List<SideFragment> compileAlternatives(ParseTree node, int minGap, int maxGap, int position) throws CompileException {
        final int numChildren = node.getNumChildren();
        final List<SideFragment> alternatives = new ArrayList<>();
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            final ParseTree alternative = node.getChild(childIndex);
            final SequenceMatcher fragmentMatcher = MATCHER_COMPILER.compile(alternative);
            final int minMax = maxGap < minGap ? minGap : maxGap;
            final SideFragment fragment = new SideFragment(fragmentMatcher, minGap, minMax, position);
            alternatives.add(fragment);
        }
        return alternatives;
    }

    private int getMinGap(final ParseTree node) throws CompileException {
        if (node.getParseTreeType() == ParseTreeType.REPEAT
                || node.getParseTreeType() == ParseTreeType.REPEAT_MIN_TO_MAX) {
            try {
                return node.getNumChildren() > 0 ? node.getChild(0).getIntValue() : 0;
            } catch (ParseException ex) {
                throw new CompileException(ex.getMessage(), ex);
            }
        }
        return 0;
    }

    private int getMaxGap(final ParseTree node) throws CompileException {
        if (node.getParseTreeType() == ParseTreeType.REPEAT_MIN_TO_MAX) {
            try {
                return node.getNumChildren() > 1 ? node.getChild(1).getIntValue() : 0;
            } catch (ParseException ex) {
                throw new CompileException(ex.getMessage(), ex);
            }
        }
        return 0;
    }

    /**
     * Locates the longest possible "anchoring" sequence to use for searching.
     * All other parts of the subsequence to the left and right of the anchor become fragments.
     * In general, longer anchors can be searched for faster than short anchors.
     *
     * @param sequence A list of ParseTree nodes which are the sequence we're scanning.
     * @param startIndex The index of the node to search from.
     * @param endIndex   The end index (exclusive) of the last node to search for.
     * @param compileType whether to compile for PRONOM (only bytes in anchor) or DROID (more complex matchers in anchor)
     * @return The start and end indexes of the search sequence as an IntPair.
     * @throws CompileException If a suitable anchoring sequence can't be found.
     */
    private IntPair locateSearchSequence(final List<ParseTree> sequence,
                                         final int startIndex, final int endIndex,
                                         final CompileType compileType) throws CompileException {
        // PRONOM anchors can only contain bytes.
        if (compileType == CompileType.PRONOM) {
            return locateSearchSequence(sequence, startIndex, endIndex, PRONOMStrategy);
        }

        // DROID anchors can contain sets, bitmasks and ranges, as long as they aren't too big.
        IntPair result = locateSearchSequence(sequence, startIndex, endIndex, DROIDStrategy);
        if (result == NO_RESULT) {

            // If we couldn't find an anchor with limited sets, bitmasks or ranges, try again allowing anything:
            result = locateSearchSequence(sequence, startIndex, endIndex, AllowAllStrategy);
        }
        return result;
    }

    /**
     * Locates the longest possible "anchoring" sequence to use for searching.
     * All other parts of the subsequence to the left and right of the anchor become fragments.
     * In general, longer anchors can be searched for faster than short anchors.
     *
     * @param sequence The sequence of nodes to search in for an anchoring sequence.
     * @param startIndex The index of the node to start searching from.
     * @param endIndex   The end index (exclusive) of the last node to search for.
     * @param anchorStrategy  Which elements can appear in anchors (bytes: PRONOM, some sets: DROID, anything: emergency)
     * @return The start and end indexes of the search sequence as an IntPair.
     * @throws CompileException If a suitable anchoring sequence can't be found.
     */
    private IntPair locateSearchSequence(final List<ParseTree> sequence,
                                         final int startIndex, final int endIndex,
                                         final AnchorStrategy anchorStrategy) throws CompileException {
        //int length = 0;
        int startPos = startIndex;
        int bestLength = 0;
        int bestStart  = 0;
        int bestEnd    = 0;
        for (int childIndex = startIndex; childIndex < endIndex; childIndex++) {
            ParseTree child = sequence.get(childIndex);

            switch (child.getParseTreeType()) {

                /* -----------------------------------------------------------------------------------------------------
                 * Types which can sometimes be part of an anchoring sequence:
                 */
                case RANGE: case SET: case ALL_BITMASK: case ANY: {
                    if (anchorStrategy.canBePartOfAnchor(child)) {
                        break;
                    }
                    // If not part of anchor, FALL THROUGH to final section for things which can't be part of an anchor.
                    // Intentionally no break statement here - it goes to the ALTERNATIVES, ANY, REPEAT and REPEAT_MIN_TO_MAX section.
                }

                /* -----------------------------------------------------------------------------------------------------
                 * Types which can't ever be part of an anchoring sequence:
                 */
                case ALTERNATIVES: case REPEAT: case REPEAT_MIN_TO_MAX: {
                    // If we found a longer sequence than we had so far, use that:
                    int totalLength = calculateLength(sequence, startPos, childIndex - 1);
                    if (totalLength > bestLength) {
                        bestLength = totalLength;
                        bestStart  = startPos;
                        bestEnd    = childIndex - 1;
                    }

                    // Start looking for a longer suitable sequence:
                    startPos = childIndex + 1; // next subsequence to look for.
                    break;
                }

                default: {
                    // do nothing - we're only looking for types which aren't part of an anchoring sequence.
                }
            }
        }

        // Do a final check to see if the last nodes processed are the longest:
        int totalLength = calculateLength(sequence, startPos, endIndex - 1);
        if (totalLength > bestLength) {
            bestLength = totalLength;
            bestStart  = startPos;
            bestEnd    = endIndex - 1;
        }

        // If we have no best length, then we have no anchoring subsequence - DROID can't process it.
        if (bestLength == 0) {
            return NO_RESULT;
        }

        return new IntPair(bestStart, bestEnd);
    }

    private int calculateLength(List<ParseTree> sequence, int startPos, int endPos) throws CompileException {
        int totalLength = 0;
        try {
            for (int index = startPos; index <= endPos; index++) {
                ParseTree node = sequence.get(index);
                switch (node.getParseTreeType()) {
                    case BYTE:
                    case RANGE:
                    case ANY:
                    case ALL_BITMASK:
                    case SET: {
                        totalLength++;
                        break;
                    }
                    case STRING: {
                        totalLength += node.getTextValue().length();
                        break;
                    }
                    case REPEAT: { // a value repeated a number of times - length = num repeats.
                        totalLength += node.getChild(0).getIntValue();
                        break;
                    }

                    default:
                        throw new CompileException("Could not calculate length of node " + node);
                }
            }
        } catch (ParseException e) {
            throw new CompileException(e.getMessage(), e);
        }

        return totalLength;
    }

    private static int countMatchingBitmask(ParseTree node) throws ParseException {
        return ByteUtils.countBytesMatchingAllBits(node.getByteValue());
    }

    private static int countMatchingSet(ParseTree node) throws ParseException {
        return ParseTreeUtils.calculateSetValues(node).size();
    }

    // Only include ranges as potential anchor members if they are not too big.
    // Large ranges are poor members for a search anchor, as they can massively impede many search algorithms if they're present.
    private static int countMatchingRange(final ParseTree rangeNode) throws CompileException {
        if (rangeNode.getParseTreeType() == ParseTreeType.RANGE) {
            final int range1, range2;
            try {
                range1 = rangeNode.getChild(0).getIntValue();
                range2 = rangeNode.getChild(1).getIntValue();
            } catch (ParseException e) {
                throw new CompileException(e.getMessage(), e);
            }
            return range2 > range1 ? range2 - range1 : range1 - range2; // range values are not necessarily smaller to larger...
        }
        throw new IllegalArgumentException("Parse tree node is not a RANGE type: " + rangeNode);
    }

    private static IntPair NO_RESULT = new IntPair(-1, -1);

    private static class IntPair {
        final int firstInt;
        final int secondInt;
        public IntPair(final int firstInt, final int secondInt) {
            this.firstInt = firstInt;
            this.secondInt = secondInt;
        }
    }

    private static class IntIterator {
        private final int stopValue;
        private final int increment;
        private int position;

        public IntIterator(final int start, final int end) {
            // Won't iterate negative numbers - this is to iterate index positions in a sequence.
            if (start < 0 || end < 0) {
                this.position = 0;
                this.increment = 0;
                this.stopValue = 0;
            } else {
                this.position = start;
                this.increment = start < end ? 1 : -1;
                this.stopValue = end + increment;
            }
        }

        public boolean hasNext() {
            return position != stopValue;
        }

        public int next() {
            if (hasNext()) {
                final int currentPosition = position;
                position += increment;
                return currentPosition;
            }
            throw new NoSuchElementException("Iterator has position: " + position + " stopValue: " + stopValue + " and increment " + increment);
        }
    }

    private static AnchorStrategy PRONOMStrategy   = new PRONOMAnchorStrategy();
    private static AnchorStrategy DROIDStrategy    = new DROIDAnchorStrategy();
    private static AnchorStrategy AllowAllStrategy = new AllowAllAnchorStrategy();

    private interface AnchorStrategy {
        boolean canBePartOfAnchor(ParseTree node) throws CompileException;
    }

    private static class PRONOMAnchorStrategy implements AnchorStrategy {
        @Override
        public boolean canBePartOfAnchor(final ParseTree node) throws CompileException {
            return false;
        }
    }

    private static class DROIDAnchorStrategy implements AnchorStrategy {
        @Override
        public boolean canBePartOfAnchor(final ParseTree node) throws CompileException {
            final ParseTreeType type = node.getParseTreeType();
            try {
                return (type == ParseTreeType.RANGE && countMatchingRange(node) <= MAX_MATCHING_BYTES) ||
                        (type == ParseTreeType.SET && countMatchingSet(node) <= MAX_MATCHING_BYTES) ||
                        (type == ParseTreeType.ALL_BITMASK && countMatchingBitmask(node) <= MAX_MATCHING_BYTES);
            } catch (ParseException e) {
                throw new CompileException(e.getMessage(), e);
            }
        }
    }

    private static class AllowAllAnchorStrategy implements AnchorStrategy {
        @Override
        public boolean canBePartOfAnchor(final ParseTree node) throws CompileException {
            return true;
        }
    }

}
