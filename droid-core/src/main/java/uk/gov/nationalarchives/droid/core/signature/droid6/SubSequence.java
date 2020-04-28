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
/*
 * The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 *
 * Developed by:
 * 
 * DROID 5:
 * ------------
 * Matt Palmer, The National Archives 2009-2010.
 * Multiple bug fixes and performance optimisations:
 *  - pre-calculate properties rather than leaving them as strings read from the XML file.
 *  - refactoring to avoid continually recalculating the same values in various functions.
 *  - calculate the bounds of the search window correctly, avoiding numerous IndexOutOfBoundsExceptions
 *  - search direction logic corrected for backwards wildcard searching
 *  - array resized correctly in search routine, preventing match failure through IndexOutOfBoundsException.
 *  - more performant search choice where signatures have a bounded gap at the start of the signature.
 *
 * TODO:
 *  - fix issue where starting sequence should match at a known positionInFile (related to more
 *    preformance search choice 'fix').
 *
 * DROID 4 and earlier:
 * ------------------------
 * Tessella Support Services plc
 * 3 Vineyard Chambers
 * Abingdon, OX14 3PX
 * United Kingdom
 * http://www.tessella.com
 *
 * Tessella/NPD/4305
 * PRONOM 4
 *
 * $Id: SubSequence.java,v 1.8 2006/03/13 15:15:29 linb Exp $
 *
 * $Log: SubSequence.java,v $
 * Revision 1.8  2006/03/13 15:15:29  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.7  2006/02/13 10:29:40  gaur
 * Fixed bug in searching a short file for a byte sequence at a large offset from BOF
 *
 * Revision 1.6  2006/02/13 09:26:16  gaur
 * Fixed bug in searching files from EOF, after first STS round
 *
 * Revision 1.5  2006/02/09 15:04:37  gaur
 * Corrected formatting
 *
 * Revision 1.4  2006/02/07 17:16:23  linb
 * - Change fileReader to IdentificationResults in formal parameters of methods
 * - use new static constructors
 * - Add detection of if a filePath is a URL or not
 *
 * Revision 1.3  2006/02/07 11:30:04  gaur
 * Added support for endianness of signature
 *
 *
 * $History: SubSequence.java $            // subSequence.setBigEndian(byteSequence.isBigEndian());
 *
 * *****************  Version 6  *****************
 * User: Walm         Date: 29/09/05   Time: 9:16
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * Bug fix in response to JIRA issue PRON-29.
 * changed startPosInFile to an array + some changes to the way start
 * positionInFile options are dealt with.
 *
 * *****************  Version 5  *****************
 * User: Walm         Date: 17/05/05   Time: 12:47
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * added more error trapping
 *
 * *****************  Version 4  *****************
 * User: Walm         Date: 5/04/05    Time: 18:08
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid6;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.ArrayUtils;

import net.byteseek.compiler.CompileException;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.searcher.bytes.ByteMatcherSearcher;
import net.byteseek.searcher.Searcher;
import net.byteseek.searcher.SearchResult;
import net.byteseek.searcher.sequence.horspool.HorspoolFinalFlagSearcher;
import net.byteseek.matcher.sequence.SequenceMatcher;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * A SubSequence is an extended byte-string to match.
 *
 * It must include at least one unambiguous sequence of
 * bytes or sets of bytes, which can be searched for using
 * the BoyerMooreHorpsool (BMH) algorithm.  This is known as the
 * "anchor" sequence.  
 *
 * If necessary, it can include Left and
 * Right Fragments, which are parts of the extended string of
 * bytes which cannot be searched for using BMH.  These fragments
 * include features like alternative (A|B|C) and gaps in the 
 * string, e.g. {5} or {5-10}.   
 *
 *
 * @author Martin Waller
 * @author Matt Palmer
 * @version 6.0.0
 *
 * @author  Brian O'Reilly
 * @version 6.2.2
 */
public class SubSequence extends SimpleElement {

    private static final String SEQUENCE_PARSE_ERROR = "The signature sub-sequence [%s] could not be parsed. "
            + "The error returned was [%s]";

    private static final SequenceMatcherCompiler SEQUENCE_COMPILER = new SequenceMatcherCompiler();

    private static final boolean EXPRESSION_BEFORE_GAPS = true;
    private static final boolean GAPS_BEFORE_EXPRESSION = false;

    private int minSeqOffset;
    private int maxSeqOffset;
    private int minLeftFragmentLength;
    private int maxLeftFragmentLength;
    private int minRightFragmentLength;
    private int maxRightFragmentLength;
    private int numLeftFragmentPositions;
    private int numRightFragmentPositions;
    private boolean fullFileScan;
    private List<LeftFragment> leftFragments = new ArrayList<LeftFragment>();
    private List<RightFragment> rightFragments = new ArrayList<RightFragment>();
    private SequenceMatcher matcher;
    private Searcher searcher;
    private final List<List<SideFragment>> orderedLeftFragments = new ArrayList<List<SideFragment>>();
    private final List<List<SideFragment>> orderedRightFragments = new ArrayList<List<SideFragment>>();
    private boolean backwardsSearch;
    private boolean isInvalidSubSequence;
    private String subsequenceText;
    private boolean[] orderedLeftFragsHaveVariableOffset;
    private boolean[] orderedRightFragsHaveVariableOffset;
    private boolean useLeftFragmentBackTrack;
    private boolean useRightFragmentBackTrack;
    private boolean preparedForUse;

    /**
     * Default constructor.
     */
    public SubSequence() {
    }

    /**
     * Constructs a SubSequence object directly from pre-built components.
     *
     * @param anchor The SequenceMatcher which is used to search for the subsequence.
     * @param leftFragments  fragments to add to the left of the subsequence.
     * @param rightFragments fragments to add to the right of the subsequence.
     * @param minSeqOffset The minimum offset to begin looking for this subsequence.
     * @param maxSeqOffset The maximum offset to find this subsequence.
     */
    public SubSequence(final SequenceMatcher anchor,
                       final List<List<SideFragment>> leftFragments,
                       final List<List<SideFragment>> rightFragments,
                       final int minSeqOffset, final int maxSeqOffset) {
        this.matcher = anchor;
        this.orderedLeftFragments.addAll(leftFragments);
        this.orderedRightFragments.addAll(rightFragments);
        this.minSeqOffset = minSeqOffset;
        this.maxSeqOffset = maxSeqOffset;
        calculateFragmentProperties();
        buildSearcher();
        preparedForUse = true;
    }

    /**
     *
     * @param leftFrag A fragment to add to the left of the subsequence.
     */
    public final void addLeftFragment(final LeftFragment leftFrag) {
        leftFragments.add(leftFrag);
    }

    /**
     *
     * @param rightFrag A fragment to add to the right of the subsequence.
     */
    public final void addRightFragment(final RightFragment rightFrag) {
        rightFragments.add(rightFrag);
    }


    /**
     * @deprecated Shifts are calculated by the net.domesdaybook searchers.
     * @param theShift Not used - preserved for backwards compatibility.
     */
    @Deprecated
    public final void setShift(final Shift theShift) {
        // Only required to preserve compatibility with the DROID 4 XML parser.
    }

    /**
     * @deprecated Shifts are calculated by the net.domesdaybook searchers.
     * @param theValue Not used - preserved for backwards compatibility.
     */
    @Deprecated
    public final void setDefaultShift(final String theValue) {
        // Only required to preserve compatibility with the DROID 4 XML parser.
    }

    /**
     *
     * @param seq A regular expression defining the anchor sequence for the subsequence.
     */
    public final void setSequence(final String seq) {
        subsequenceText = FragmentRewriter.rewriteFragment(seq);
    }

    /**
     *
     * @param theOffset The minimum offset to begin looking for this subsequence.
     */
    public final void setMinSeqOffset(final int theOffset) {
        this.minSeqOffset = theOffset;
        if (this.maxSeqOffset < this.minSeqOffset) {
            this.maxSeqOffset = this.minSeqOffset;
        }
    }

    /**
     *
     * @param theOffset The maximum offset to find this subsequence.
     */
    public final void setMaxSeqOffset(final int theOffset) {
        this.maxSeqOffset = theOffset;
        if (this.maxSeqOffset < this.minSeqOffset) {
            this.maxSeqOffset = this.minSeqOffset;
        }
    }

    /**
     * Needed so the XML parser has a method to call
     * when it encounters this information in the XML file,
     * but the information is no longer required.
     *
     * Note: unclear whether this is used anymore.
     *
     * @deprecated min frag length not used anymore
     * @param theLength The minimum length of a fragment.
     */
    @Deprecated
    public void setMinFragLength(int theLength) {
        //this.minFragLength = theLength;
    }

    /**
     * @param name Name of attribute to set
     * @param value Value of attribute to set (may be ignored)
     */
    @Override
    public final void setAttributeValue(final String name, final String value) {
        if ("SubSeqMinOffset".equals(name)) {
            setMinSeqOffset(Integer.parseInt(value));
        } else if ("SubSeqMaxOffset".equals(name)) {
            setMaxSeqOffset(Integer.parseInt(value));
        } else if ("MinFragLength".equals(name)) {
            //setMinFragLength(Integer.parseInt(value));
            setMinFragLength(-1);
        } else {
            if (!"Position".equals(name)) {
                unknownAttributeWarning(name, this.getElementName());
            }
        }
    }

    /* getters */

    /**
     * @param leftFrag true to return information about the left fragments, false to return
     * information about the right fragments.
     * @return the number of fragment offsetPositions for either the left or right fragments.
     */
    public final int getNumFragmentPositions(final boolean leftFrag) {
        return leftFrag ? this.numLeftFragmentPositions
                : this.numRightFragmentPositions;
    }

    /**
     *
     * @param leftFrag true to return information about the left fragments, false to return
     * information about the right fragments.
     * @param thePosition The fragment positionInFile to retrieve number of fragments for.
     * @return the number of alternative fragments for the given left or right positionInFile.
     */
    public final int getNumAlternativeFragments(final boolean leftFrag, final int thePosition) {
        return leftFrag ? this.orderedLeftFragments.get(thePosition - 1).size()
                : this.orderedRightFragments.get(thePosition - 1).size();
    }

    /**
     * Get the number of alternative fragment options at a given fragmemt posoition.
     * @param thePosition The (1-based) fragment position index.
     * @param orderedFragments List of List of Side Fragments (inner list is to allow for multiple options at positions.
     * @return The number of alternative fragments at the position supplied in thePosition.
     */
    public final int getNumAlternativeFragments(final int thePosition, List<List<SideFragment>> orderedFragments) {
        return orderedFragments.get(thePosition - 1).size();
    }

    /**
     *
     * @param leftFrag true to return information about the left fragments, false to return
     * information about the right fragments.     * 
     * @param thePosition The fragment positionInFile to retrieve number of fragments for.
     * @param alternateIndex The index of the fragment alternative at the given left or right positionInFile.
     * @return The fragment alternative at the given left or right positionInFile.
     */
    public final SideFragment getFragment(final boolean leftFrag, final int thePosition, final int alternateIndex) {
        return leftFrag ? (SideFragment) (this.orderedLeftFragments.get(thePosition - 1)).get(alternateIndex)
                : (SideFragment) (this.orderedRightFragments.get(thePosition - 1)).get(alternateIndex);
    }

    /**
     * @return the number of bytes matched by the anchoring sequence.
     */
    public final int getNumBytes() {
        return matcher == null ? 0 : matcher.length();
    }

    /**
     *
     * @return The minimum offset to skip when looking for this subsequence.
     */
    public final int getMinSeqOffset() {
        return minSeqOffset;
    }

    /**
     *
     * @return the maximum offset to search up to when looking for this subsequence.
     */
    public final int getMaxSeqOffset() {
        return maxSeqOffset;
    }

    /**
     * Note: unclear whether this is used anymore.
     *
     * @return The minimum fragment length.
     */
    /*
    public final int getMinFragLength() {
        return minFragLength;
    }
    */

    /**
     * This method must be called after the signature file 
     * has been parsed and before running any file identifications.
     *
     * @param reverseOrder Whether this subsequence is scanned forwards in the file
     * or backwards from the end of the file.
     * @param fullScan Whether this subsequence follows a wildcard .* sequence.
     */
    public final void prepareForUse(final boolean reverseOrder, final boolean fullScan) {
        this.backwardsSearch = reverseOrder;
        this.fullFileScan = fullScan;
        processSequenceFragments();
    }

    /**
     * Accessor method for internal objects to allow testing.
     * @return matcher
     */
    public SequenceMatcher getAnchorMatcher() {
        return matcher; // matchers are immutable, no harm in just returning it.
    }

    /**
     * @return fragments to add to the left of the subsequence
     */
    public List<List<SideFragment>> getLeftFragments() {
        return defensiveCopy(orderedLeftFragments);
    }

    /**
     * @return fragments to add to the right of the subsequence
     */
    public List<List<SideFragment>> getRightFragments() {
        return defensiveCopy(orderedRightFragments);
    }

    private List<List<SideFragment>> defensiveCopy(List<List<SideFragment>> toCopy) {
        final List<List<SideFragment>> defensiveCopy = new ArrayList<>();
        for (List<SideFragment> fragmentsAtPosition : toCopy) {
            final List<SideFragment> newFragmentList = new ArrayList<>();
            for (SideFragment fragment : fragmentsAtPosition) {
                newFragmentList.add(new SideFragment(fragment));
            }
            defensiveCopy.add(newFragmentList);
        }
        return defensiveCopy;
    }

    /*
     * Re-orders the left and right sequence fragments in increasing positionInFile order.
     * Also calculates the minimum and maximum lengths a fragment can have.
     */
    private void processSequenceFragments() {
        if (!preparedForUse) {
            buildFragmentsFromXMLObjects();
            calculateFragmentProperties();
        }
        preparedForUse = true;
    }

    /**
     * If a SubSequence was built by parsing XML, the fragments are stored in the leftFragments and rightFragments
     * lists, which aren't ordered for searching.  To search, DROID needs to re-build those lists with the fragments
     * in the right order, and collapsed into lists of lists (e.g. for each fragment position we have a list of
     * possible alternative matches (alternatives fragments, e.g. (01 02 03 | 02 04 05) matches two fragments at the same position.
     *
     * It also performs some optimisation on the fragments:
     *
     * 1.  Turning alternatives of single bytes into sets, e.g. (01|02|03) is more efficiently matched as [01 02 03].
     * 2.  Capturing some fragments back into the main anchor sequence, as DROID can search for some fragments directly.
     *
     * Once it has built and optimised the fragments, it builds the matcher and search required for actual searching.
     */
    private void buildFragmentsFromXMLObjects() {
        if (leftFragments != null && rightFragments != null) {
            buildOrderedLeftFragments();
            buildOrderedRightFragments();

            optimiseSingleByteAlternatives(orderedLeftFragments);
            optimiseSingleByteAlternatives(orderedRightFragments);
            captureLeftFragments();
            captureRightFragments();

            buildMatcherAndSearcher();
            this.leftFragments = null;
            this.rightFragments = null;
        }
    }

    /**
     * Calculates various properties of the fragments needed to search effectively with them.
     */
    private void calculateFragmentProperties() {
        this.numLeftFragmentPositions = orderedLeftFragments.size();
        this.numRightFragmentPositions = orderedRightFragments.size();
        isInvalidSubSequence = isInvalidSubSequence ? true : checkForInvalidFragments();
        if (this.numLeftFragmentPositions > 0) {
            this.orderedLeftFragsHaveVariableOffset =
                    determineFragmentPositionVariableOffsetStatus(this.orderedLeftFragments);
            this.useLeftFragmentBackTrack = ArrayUtils.contains(this.orderedLeftFragsHaveVariableOffset, true);
        }
        if (this.numRightFragmentPositions > 0) {
            this.orderedRightFragsHaveVariableOffset =
                    determineFragmentPositionVariableOffsetStatus(this.orderedRightFragments);
            this.useRightFragmentBackTrack = ArrayUtils.contains(this.orderedRightFragsHaveVariableOffset, true);
        }
        calculateMinMaxLeftFragmentLength();
        calculateMinMaxRightFragmentLength();
    }

    /**
     * Processes the left fragment XML list into a List<List<SideFragment>>
     */
    private void buildOrderedLeftFragments() {
        int numPositions = 0;
        for (int i = 0; i < leftFragments.size(); i++) {
            final int currentPosition = this.getRawLeftFragment(i).getPosition();
            if (currentPosition > numPositions) {
                numPositions = currentPosition;
            }
        }

        //initialise all necessary fragment lists (one for each positionInFile)
        for (int i = 0; i < numPositions; i++) { //loop through fragment offsetPositions
            final List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
            orderedLeftFragments.add(alternativeFragments);
        }

        //Add fragments to new structure
        for (int i = 0; i < leftFragments.size(); i++) {  //loop through all fragments
            final SideFragment fragment = this.getRawLeftFragment(i);
            final int currentPosition = fragment.getPosition();
            orderedLeftFragments.get(currentPosition - 1).add(fragment);
        }
    }

    /**
     * Processes the right fragment XML list into a List<List<SideFragment>>
     */
    private void buildOrderedRightFragments() {
    /* Right fragments */
        //Determine the number of fragment subsequences there are
        int numPositions = 0;
        for (int i = 0; i < rightFragments.size(); i++) {
            final int currentPosition = this.getRawRightFragment(i).getPosition();
            if (currentPosition > numPositions) {
                numPositions = currentPosition;
            }
        }

        //initialise all necessary fragment lists (one for each positionInFile)
        for (int i = 0; i < numPositions; i++) { //loop through fragment offsetPositions
            final List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
            orderedRightFragments.add(alternativeFragments);
        }

        //Add fragments to new structure
        for (int i = 0; i < rightFragments.size(); i++) {  //loop through all fragments
            final SideFragment fragment = this.getRawRightFragment(i);
            final int currentPosition = fragment.getPosition();
            orderedRightFragments.get(currentPosition - 1).add(fragment);
        }
    }

    /**
     * Optimise alternative sequences of single bytes into a byte-class,
     * instead of being a set of alternatives.  This is more efficient to match
     * using byteseek.
     */
    private void optimiseSingleByteAlternatives(List<List<SideFragment>> fragments) {
        for (int fragPos = 0; fragPos < fragments.size(); fragPos++) { // loop through all offsetPositions:
            final List<SideFragment> fragmentsToMatch = fragments.get(fragPos);
            final int noOfFragments = fragmentsToMatch.size();
            if (noOfFragments > 1) {
                boolean allFragmentsLengthOne = true;
                SideFragment frag = null;
                StringBuilder expression = new StringBuilder();
                expression.append('[');
                for (int fragmentIndex = 0; fragmentIndex < noOfFragments; fragmentIndex++) {
                    frag = fragmentsToMatch.get(fragmentIndex);
                    if (frag.getNumBytes() > 1) {
                        allFragmentsLengthOne = false;
                        break;
                    }
                    expression.append(frag.toRegularExpression(false));
                }
                if (allFragmentsLengthOne && frag != null) {
                    SideFragment newFrag = new LeftFragment();
                    newFrag.setPosition(frag.getPosition());
                    newFrag.setMinOffset(frag.getMinOffset());
                    newFrag.setMaxOffset(frag.getMaxOffset());
                    expression.append(']');
                    newFrag.setFragment(expression.toString());
                    List<SideFragment> newList = new ArrayList<SideFragment>();
                    newList.add(newFrag);
                    fragments.set(fragPos, newList);
                }
            }
        }
    }

    /**
     * Find fragments on the left which could be captured back into the main anchor sequence:
     */
    private void captureLeftFragments() {
        int captureFragPos = -1;
        int numLeftFragmentPos = orderedLeftFragments.size();
    FRAGS:
        for (int position = 0; position < numLeftFragmentPos; position++) {
            List<SideFragment> fragsAtPos = orderedLeftFragments.get(position);
            if (fragsAtPos.size() == 1) { // no alternatives at this positionInFile.
                for (SideFragment frag : fragsAtPos) {
                    if (frag.getMinOffset() == 0 && frag.getMaxOffset() == 0) { // bangs right up to the main sequence
                        subsequenceText = frag.toRegularExpression(true) + ' ' + subsequenceText;
                        captureFragPos = position;
                    } else if (frag.getMinOffset() == frag.getMaxOffset()) { // a fixed offset from the main sequence
                        if (backwardsSearch) {
                            //TODO: work out if adding would make shift worse - it depends on this fragment
                            //      but also potentially fragments after it.
                            break FRAGS;
                        } else {
                            // CHECKSTYLE:OFF " .{" and "} " defined locally in separate methods for optimal performance
                            subsequenceText = frag.toRegularExpression(true)
                                    + " .{" + frag.getMinOffset() + "} " + subsequenceText;
                            // CHECKSTYLE:ON
                            captureFragPos = position;
                        }
                    } else {
                        break FRAGS;
                    }
                }
            } else {
                break FRAGS;
            }
        }
        rewriteRemainingFragments(orderedLeftFragments, captureFragPos);
    }

    /**
     * Find fragments on the right which could be captured back into the main anchor sequence:
     */
    private void captureRightFragments() {
        int captureFragPos = -1;
        int numRightPos = orderedRightFragments.size();

    FRAGS:
        for (int position = 0; position < numRightPos; position++) {
            List<SideFragment> fragsAtPos = orderedRightFragments.get(position);
            if (fragsAtPos.size() == 1) { // no alternatives at this positionInFile.
                for (SideFragment frag : fragsAtPos) {
                    if (frag.getMinOffset() == 0 && frag.getMaxOffset() == 0) { // bangs right up to the main sequence
                        subsequenceText = subsequenceText + ' ' + frag.toRegularExpression(true);
                        captureFragPos = position;
                        // CHECKSTYLE:OFF     Quite legitimate to have more than 120 chars per line just now...
                    } else if (frag.getMinOffset() == frag.getMaxOffset()) { // a fixed offset from the main sequence
                        if (backwardsSearch) { // things on the right can't make our average shift worse, so just add them.
                            subsequenceText = subsequenceText + " .{" + frag.getMinOffset() + "} " + frag.toRegularExpression(true);
                            captureFragPos = position;
                            // CHECKSTYLE:ON
                        } else { // work out if adding this would make our average shift worse.
                            //TODO: work out if adding would make shift worse - it depends on this fragment
                            //      but also potentially fragments after it.
                            break FRAGS;
                        }
                    } else {
                        break FRAGS;
                    }
                }
            } else {
                break FRAGS;
            }
        }
        rewriteRemainingFragments(orderedRightFragments, captureFragPos);
    }

    private void rewriteRemainingFragments(List<List<SideFragment>> orderedList, int captureFragPos) {
        // Have we have captured fragments up to this positionInFile into the main subsequence?
        if (captureFragPos > -1) {
            // Remove all the fragments we have captured.
            for (int deleteCount = 0; deleteCount <= captureFragPos; deleteCount++) {
                orderedList.remove(0);
            }
            // Rewrite the offsetPositions of the remaining fragments.
            for (int changePos = 0; changePos < orderedList.size(); changePos++) {
                List<SideFragment> fragments = orderedList.get(changePos);
                for (final SideFragment fragment : fragments) {
                    fragment.setPosition(changePos);
                }
            }
        }
    }

    /**
     * Calculate the overall min and max offsets for all left fragments.
     */
    private void calculateMinMaxLeftFragmentLength() {
        // Calculate minimum and maximum size of left fragments:
        minLeftFragmentLength = 0;
        maxLeftFragmentLength = 0;
        for (int position = 0; position < orderedLeftFragments.size(); position++) {
            final List<SideFragment> fragmentList = orderedLeftFragments.get(position);
            int minFragSize = Integer.MAX_VALUE;
            int maxFragSize = 0;
            for (int fragmentIndex = 0; fragmentIndex < fragmentList.size(); fragmentIndex++) {
                final SideFragment frag = fragmentList.get(fragmentIndex);
                final int fragMinSpace = frag.getNumBytes() + frag.getMinOffset();
                final int fragMaxSpace = frag.getNumBytes() + frag.getMaxOffset();
                if (fragMinSpace < minFragSize) {
                    minFragSize = fragMinSpace;
                }
                if (fragMaxSpace > maxFragSize) {
                    maxFragSize = fragMaxSpace;
                }
            }
            minLeftFragmentLength += minFragSize;
            maxLeftFragmentLength += maxFragSize;
        }
    }

    /**
     * Calculate the overall min and max offsets for all right fragments.
     */
    private void calculateMinMaxRightFragmentLength() {
        // Calculate minimum and maximum size of right fragments:
        minRightFragmentLength = 0;
        maxRightFragmentLength = 0;
        for (int position = 0; position < orderedRightFragments.size(); position++) {
            final List<SideFragment> fragmentList = orderedRightFragments.get(position);
            int minFragSize = Integer.MAX_VALUE;
            int maxFragSize = 0;
            for (int fragmentIndex = 0; fragmentIndex < fragmentList.size(); fragmentIndex++) {
                final SideFragment frag = fragmentList.get(fragmentIndex);
                final int fragMinSpace = frag.getNumBytes() + frag.getMinOffset();
                final int fragMaxSpace = frag.getNumBytes() + frag.getMaxOffset();
                if (fragMinSpace < minFragSize) {
                    minFragSize = fragMinSpace;
                }
                if (fragMaxSpace > maxFragSize) {
                    maxFragSize = fragMaxSpace;
                }
            }
            minRightFragmentLength += minFragSize;
            maxRightFragmentLength += maxFragSize;
        }
    }

    /**
     * Compiles the matcher and searcher objects to search for the main anchor sequence with.
     */
    private void buildMatcherAndSearcher() {
        try {
            // CHECKSTYLE:OFF     Quite legitimate to have more than 120 chars per line just now...
            matcher = SEQUENCE_COMPILER.compile(subsequenceText);
            buildSearcher();
            // CHECKSTYLE:ON
        } catch (CompileException ex) {
            final String warning = String.format(SEQUENCE_PARSE_ERROR, subsequenceText, ex.getMessage());
            getLog().warn(warning);
            isInvalidSubSequence = true;
        }
    }

    /**
     * Creates the right searcher for the matcher.
     */
    private void buildSearcher() {
        if (matcher.length() == 1) {
            searcher = new ByteMatcherSearcher(matcher.getMatcherForPosition(0)); // use simplest byte matcher searcher if the matcher is length 1.
        } else {
            searcher = new HorspoolFinalFlagSearcher(matcher); // use shifting searcher if shifts can be bigger than one.
        }
    }

    private int getNumberOfFragmentPositions(final List<SideFragment> fragments) {
        int numPositions = 0;
        for (int i = 0; i < leftFragments.size(); i++) {
            final int currentPosition = fragments.get(i).getPosition();
            if (currentPosition > numPositions) {
                numPositions = currentPosition;
            }
        }
        return numPositions;
    }

    /**
     *
     * @return Whether the subsequence is invalid.
     */
    public boolean isInvalidSubSequence() {
        return isInvalidSubSequence;
    }


    private boolean checkForInvalidFragments() {
        return checkFragmentList(orderedLeftFragments)
                || checkFragmentList(orderedRightFragments);
    }


    private boolean checkFragmentList(List<List<SideFragment>> orderedFragmentList) {
        for (List<SideFragment> fragmentList : orderedFragmentList) {
            for (SideFragment fragment : fragmentList) {
                if (fragment.isInvalidFragment()) {
                    return true;
                }
            }
        }
        return false;
    }


    // Build a regular expression representation of a list of alternatives
    private String getFragmentAlternativesAsRegularExpression(
            final boolean prettyPrint,
            final int positionIndex,
            final List<SideFragment> fragments) {
        final StringBuffer regularExpression = new StringBuffer();
        regularExpression.append(prettyPrint ? " (" : "(");
        final int lastAlternate = fragments.size();
        for (int alternateIndex = 0; alternateIndex < lastAlternate; alternateIndex++) {
            if (alternateIndex > 0) {
                regularExpression.append("|"); // | already a good separator - no need for spaces in pretty printing.
            }
            final SideFragment fragment = fragments.get(alternateIndex);
            regularExpression.append(fragment.toRegularExpression(prettyPrint));
        }
        regularExpression.append(prettyPrint ? ") " : ")");
        return regularExpression.toString();
    }


    private void appendFragmentstoRegularExpression(
            final boolean prettyPrint,
            final StringBuffer regularExpression,
            final boolean expressionFirst,
            final int positionIndex,
            final List<SideFragment> fragments) {
        final SideFragment fragment = fragments.get(0);
        final int minFragmentOffset = fragment.getMinOffset();
        final int maxFragmentOffset = fragment.getMaxOffset();

        // If we have more than one fragment at a positionInFile, it's a list of alternatives:
        String fragmentExpression;
        if (fragments.size() > 1) { // Write out the fragments as a list of alternatives:
            fragmentExpression = getFragmentAlternativesAsRegularExpression(
                    prettyPrint, positionIndex, fragments);
        } else { // otherwise just get the fragment:
            fragmentExpression = fragment.toRegularExpression(prettyPrint);
        }
        ByteSequence.appendBoundedGapExpression(prettyPrint, expressionFirst,
                regularExpression, fragmentExpression, minFragmentOffset, maxFragmentOffset);
    }

    private boolean[] determineFragmentPositionVariableOffsetStatus(List<List<SideFragment>> orderedFragmentsList) {

        //For each fragment position, record whether or not the constituent fragments include at least
        // one fragment where the minimum and maximum offsets differ.
        boolean[] orderedFragsHaveVariableOffset = new boolean[orderedFragmentsList.size()];
        for (int i = 0; i < orderedFragmentsList.size(); i++) {
            orderedFragsHaveVariableOffset[i] = fragmentsContainVariableOffset(orderedFragmentsList.get(i));
        }

        //return orderedFragmentsList.size() > 1 &&  ArrayUtils.contains(orderedFragsHaveVariableOffset, true);
        return orderedFragsHaveVariableOffset;
    }

    private boolean fragmentsContainVariableOffset(List<SideFragment> fragmentList) {

        boolean hasAtLeastOneVariableOffset = false;

        for (SideFragment fragment: fragmentList) {
            if (fragment.getMinOffset() != fragment.getMaxOffset()) {
                hasAtLeastOneVariableOffset = true;
                break;
            }
        }

        return hasAtLeastOneVariableOffset;
    }

    /**
     *
     * @param prettyPrint Whether to pretty print the regular expression or not.
     * @return A regular expression representing the subsequence.
     */
    public final String toRegularExpression(final boolean prettyPrint) {

        StringBuffer regularExpression = new StringBuffer();

        // Write out the left fragments:
        for (int positionIndex = numLeftFragmentPositions; positionIndex > 0; positionIndex--) {
            final List<SideFragment> fragments = orderedLeftFragments.get(positionIndex - 1);
            appendFragmentstoRegularExpression(prettyPrint, regularExpression,
                    EXPRESSION_BEFORE_GAPS,
                    positionIndex, fragments);
        }

        // Write out the anchor sequence:
        regularExpression.append(matcher.toRegularExpression(prettyPrint));

        // Write out the right fragments:
        for (int positionIndex = 1; positionIndex <= numRightFragmentPositions; positionIndex++) {
            final List<SideFragment> fragments = orderedRightFragments.get(positionIndex - 1);
            appendFragmentstoRegularExpression(prettyPrint, regularExpression,
                    GAPS_BEFORE_EXPRESSION,
                    positionIndex, fragments);
        }

        return regularExpression.toString();
    }

    /***
     * toString override.
     * @return Formatted simple name of the class.
     */
    public String toString() {
        return getClass().getSimpleName() + '[' + toRegularExpression(true) + ']';
    }

    /** Uses the Boyer-Moore-Horspool search algorithm to find a sequence within a window
     * on a file.
     *
     * The search proceeds by trying to find an "anchor" sequence of bytes
     * in the file, using the Boyer-Moore-Horspool algorithm, which permits it
     * to skip over bytes if they can't possibly match the anchor sequence.
     * It scans from the opposite end of the sequence to the search direction.
     * This means it doesn't have to check every single byte in the search window.
     * In general, the longer the anchor sequence, the more bytes we can skip.
     * When it finds an anchor sequence, it checks any left or right
     * fragments that may surround it, to verify the match.
     *
     * @param position The positionInFile to begin searching from.
     * @param targetFile The file to search in.
     * @param maxBytesToScan The maximum amount of bytes to read from
     * the beginning or end of the file.  If negative, scanning is unlimited.
     * @param bofSubsequence Indicates when subsequence is anchored to BOF
     * @param eofSubsequence Indicates when subsequence is anchored to EOF
     * @return boolean True on success
     */
    //CHECKSTYLE:OFF - far too complex method.
    public final boolean findSequenceFromPosition(final long position,
                                                  final ByteReader targetFile, final long maxBytesToScan,
                                                  final boolean bofSubsequence, final boolean eofSubsequence) {
        boolean entireSequenceFound = false;
        try {
            // Local variables to speed up commonly used arrays and decisions:
            final boolean hasLeftFragments = !orderedLeftFragments.isEmpty();
            final boolean hasRightFragments = !orderedRightFragments.isEmpty();

            // Define the length of the file and the pattern, minus one to get an offset from a zero index positionInFile.
            final long lastBytePositionInFile = targetFile.getNumBytes() - 1;

            //final int lastBytePositionInAnchor = sequence.length -1;
            final int matchLength = matcher.length();
            final int lastBytePositionInAnchor = matchLength - 1;

            // Define the smallest and greatest possible byte positionInFile in the file we could match at:
            // the first possible byte positionInFile is the start of the file plus the minimum amount of
            // left fragments to check before this sequence.
            final long firstPossibleBytePosition = minLeftFragmentLength;
            // the last possible byte positionInFile is the end of the file, minus the minimum
            // right fragments to check after this sequence.
            final long lastPossibleBytePosition = lastBytePositionInFile - minRightFragmentLength;

            // Provide two implementations of the same algorithm -
            // one for forward searching, the other for backwards searching.
            // Although the differences between them are very small, DROID spends the majority of its time here,
            // so even small performance improvements add up quickly.
            final WindowReader windowReader = targetFile.getWindowReader();

            if (this.backwardsSearch) {

                // Define the search window relative to our starting positionInFile:
                final long maximumPossibleStartingPosition = position - minRightFragmentLength - lastBytePositionInAnchor;
                final long startSearchWindow = maximumPossibleStartingPosition - this.getMinSeqOffset();
                final int rightFragmentWindow = maxRightFragmentLength - minRightFragmentLength;
                long endSearchWindow = fullFileScan
                        ? 0
                        : maximumPossibleStartingPosition - this.getMaxSeqOffset() - rightFragmentWindow;

                // Limit the maximum bytes to scan.
                if (maxBytesToScan > 0 && endSearchWindow < lastBytePositionInFile - maxBytesToScan) {
                    endSearchWindow  = lastBytePositionInFile - maxBytesToScan;
                }

                // If we're starting outside a possible match positionInFile,
                // don't continue:
                if (startSearchWindow > lastPossibleBytePosition) {
                    return false;
                }

                // Ensure we don't run over the start of the file,
                // if it's shorter than the sequence we're trying to check.
                if (endSearchWindow < firstPossibleBytePosition) {
                    endSearchWindow = firstPossibleBytePosition;
                }

                long matchPosition = startSearchWindow;
                while (matchPosition >= endSearchWindow) {

                    if (matchPosition == endSearchWindow) {
                        matchPosition = matcher.matches(windowReader, matchPosition)?
                                matchPosition : -1;
                    } else {
                        final List<SearchResult<SequenceMatcher>> matches =
                                searcher.searchBackwards(windowReader, matchPosition, endSearchWindow);
                        matchPosition = matches.size() > 0?
                                matches.get(0).getMatchPosition() : -1;
                    }

                    if (matchPosition != -1) {
                        boolean matchFound = true;
                        // Check that any right fragments, behind our sequence, match.
                        if (hasRightFragments) {

                            // Get the fragment option furthest to the right of the main sequence (and nearest the
                            // end of the file).
                            List<List<SideFragment>> furthestRightFragmentOption =
                                    this.orderedRightFragments.subList(orderedRightFragments.size() -1,orderedRightFragments.size());

                            // We record information about the file positions and offsets at which any fragments in the
                            // rightmost fragment position are found.  This is in case we need to do a recheck for
                            // further occurrences, if the match is beyond the maximum offset for the sequence as a
                            // whole.  TODO: This code can benefit from further refactoring, along with the
                            // bytePosForRightFragments method - e.g. the file positions in
                            // finalOptionOffSetFoundPositions currently duplicate the data in the returned array.
                            OffsetAndFilePositions finalOptionOffSetFoundPositions = new OffsetAndFilePositions(furthestRightFragmentOption.get(0));

                            final long[] rightFragmentPositions =
                                    bytePosForRightFragments(windowReader, matchPosition + matchLength,
                                            targetFile.getFileMarker(), 1, 0, orderedRightFragments, finalOptionOffSetFoundPositions);
                            matchFound = rightFragmentPositions.length > 0;

                            // Assume for now that furthest right fragment has been found at a position which is invalid
                            // with respect to the offsets for the sequence as a whole from EOF.
                            boolean rightMostFragmentPositionInvalid = true;

                            if(matchFound) {

                                //Get the positionInFile of the fragment furthest to the right of the main sequence (and nearest the
                                // end of the file).
                                long currentNearestEOFRightmostFragmentPosition = rightFragmentPositions[rightFragmentPositions.length -1];
                                long currentFurthestEOFRightmostFragmentPosition = rightFragmentPositions.length > 1 ? rightFragmentPositions[0] :
                                        currentNearestEOFRightmostFragmentPosition;

                                // BNO: If the fragment found is beyond the minimum offset, we already know its offset
                                // is invalid.  If it is greater than the maximum offset however, we need to check for
                                // any further occurrences that may be positioned at or before the maximum offset, but
                                // not before the minimum offset.
                                //If the current subsequence is not the first or only one in the sequence, we need
                                //to allow for any earlier subsequences, since offsets are relative to the previous
                                // subsequence.  If it's the first or only seqeunce, the offsets will be relative to BOF.

                                if((currentFurthestEOFRightmostFragmentPosition) <= (position - this.minSeqOffset)) {
                                    rightMostFragmentPositionInvalid =
                                            currentNearestEOFRightmostFragmentPosition < (position - this.maxSeqOffset)
                                            &&
                                            checkRightFragmentForInvalidOffset(
                                            windowReader, currentNearestEOFRightmostFragmentPosition + 1,
                                            position, this.maxSeqOffset, this.minSeqOffset,
                                            furthestRightFragmentOption, finalOptionOffSetFoundPositions);
                                }
                            }

                            matchFound = !rightMostFragmentPositionInvalid;
                        }
                        if (matchFound) {
                            // Check that any left fragments, before our sequence, match.
                            if (hasLeftFragments) {
                                final long[] leftFragmentPositions =
                                        bytePosForLeftFragments(windowReader, 0, matchPosition - 1, -1, 0,
                                                orderedLeftFragments, null);
                                matchFound = leftFragmentPositions.length > 0;
                                matchPosition = matchFound ? leftFragmentPositions[0] : matchPosition;
                            }
                            if (matchFound) {
                                // Record that a match has been found for the entire sequence:
                                targetFile.setFileMarker(matchPosition - 1L);
                                entireSequenceFound = true;
                                break;
                            }
                        }
                        matchPosition -= 1;
                    } else {
                        break;
                    }
                }
            } else { // Searching forwards - the same algorithm optimised for forwards searching:
                // Define the search window relative to our starting positionInFile:
                final long minimumPossibleStartingPosition =
                        position + minLeftFragmentLength + lastBytePositionInAnchor;
                final long startSearchWindow = minimumPossibleStartingPosition + this.getMinSeqOffset();
                final int leftFragmentWindow = maxLeftFragmentLength - minLeftFragmentLength;
                long endSearchWindow = fullFileScan
                        ? lastPossibleBytePosition
                        : minimumPossibleStartingPosition + this.getMaxSeqOffset() + leftFragmentWindow;

                // Limit the maximum bytes to scan.
                if (maxBytesToScan > 0 && endSearchWindow > maxBytesToScan) {
                    endSearchWindow  = maxBytesToScan;
                }

                // If we're starting outside a possible match positionInFile, don't continue:
                if (startSearchWindow < firstPossibleBytePosition) {
                    return false;
                }

                // Ensure the end positionInFile doesn't run over the end of the file,
                // if it's shorter than the sequence we're trying to check.
                if (endSearchWindow > lastPossibleBytePosition) {
                    endSearchWindow = lastPossibleBytePosition;
                }

                //long matchPosition = startSearchWindow;
                long matchPosition = startSearchWindow;

                while (matchPosition <= endSearchWindow) {

                    final long matchStarterPosition = matchPosition - matchLength + 1;
                    final long matchEndingPosition  = endSearchWindow - matchLength + 1;
                    if (matchStarterPosition == matchEndingPosition) {
                        matchPosition = matcher.matches(windowReader, matchStarterPosition)?
                                matchStarterPosition + matchLength - 1 : -1;
                    } else {
                        final List<SearchResult<SequenceMatcher>> matches =
                                searcher.searchForwards(windowReader, matchStarterPosition, matchEndingPosition);
                        matchPosition = matches.size() > 0?
                                matches.get(0).getMatchPosition() + matchLength - 1 : -1;
                    }

                    if (matchPosition != -1) {
                        boolean matchFound = true;
                        if (hasLeftFragments) { // Check that any left fragments, behind our sequence match:

                            // Get the fragment option furthest to the left of the main sequence (and nearest the
                            // start of the file).
                            List<List<SideFragment>> furthestLeftFragmentOption =
                                    this.orderedLeftFragments.subList(orderedLeftFragments.size() -1,orderedLeftFragments.size());

                            // We record information about the file positions and offsets at which any fragments in the
                            // leftmost fragment position are found.  This is in case we need to do a recheck for
                            // further occurrences, if the match is beyond the maximum offset for the sequence as a
                            // whole.  TODO: This code can benefit from further refactoring, along with the
                            // bytePosForLeftFragments method - e.g. the file positions in
                            // finalOptionOffSetFoundPositions currently duplicate the data in the returned array.
                            OffsetAndFilePositions finalOptionOffSetFoundPositions = new OffsetAndFilePositions(furthestLeftFragmentOption.get(0));

                            final long[] leftFragmentPositions =
                                    bytePosForLeftFragments(windowReader, targetFile.getFileMarker(),
                                            matchPosition - matchLength, -1, 0, orderedLeftFragments, finalOptionOffSetFoundPositions);
                            matchFound = leftFragmentPositions.length > 0;

                            // Assume for now that furthest left fragment has bee found at a position which is invalid
                            // with respect to the offsets for the sequence as a whole from BOF.
                            boolean leftMostFragmentPositionInvalid = true;

                            if (matchFound) {
                                long currentNearestBOFLeftmostFragmentPosition = leftFragmentPositions[leftFragmentPositions.length -1];
                                long currentFurthestBOFLeftmostFragmentPosition = leftFragmentPositions.length > 1 ? leftFragmentPositions[0] :currentNearestBOFLeftmostFragmentPosition;

                                // BNO: If the fragment found is beyond the minimum offset, we already know its offset
                                // is invalid.  If it is greater than the maximum offset however, we need to check for
                                // any further occurrences that may be positioned at or before the maximum offset, but
                                // not before the minimum offset.

                                //If the current subsequence is not the first or only one in the sequence, we need
                                //to allow for any earlier subsequences, since offsets are relative to the previous
                                // subsequence.  If it's the first or only sequence, the offsets will be relative to BOF.
                                long minOffsetFromBOF = this.minSeqOffset + position;
                                long maxOffsetFromBOF = this.maxSeqOffset + position;

                                if (currentNearestBOFLeftmostFragmentPosition >= minOffsetFromBOF) {
                                    leftMostFragmentPositionInvalid = ((currentFurthestBOFLeftmostFragmentPosition > maxOffsetFromBOF
                                    )
                                            && checkLeftFragmentForInvalidOffset(windowReader,
                                            0,
                                            currentNearestBOFLeftmostFragmentPosition,
                                            maxOffsetFromBOF,
                                            minOffsetFromBOF, furthestLeftFragmentOption, finalOptionOffSetFoundPositions));
                                }
                            }
//                            // check BOF max seq offset (bugfix)
                            if (matchFound
                                    && bofSubsequence
                                    && leftMostFragmentPositionInvalid) {
                                matchFound = false;
                            }
                        }
                        if (matchFound) {
                            if (hasRightFragments) { // Check that any right fragments after our sequence match:
                                final long[] rightFragmentPositions =
                                        bytePosForRightFragments(windowReader, matchPosition + 1,
                                                lastBytePositionInFile, 1, 0, orderedRightFragments, null);
                                matchFound = rightFragmentPositions.length > 0;
                                // check EOF max seq offset (bugfix)
                                // TODO: rightFragmentPositions[0] here = last byte of rightmost fragment offset from BOF
                                // So  rightFragmentPositions[0] > position - this.maxSeqOffset would be more correct
                                // here than rightFragmentPositions[0]> this.maxSeqOffset.
                                // But this would still not be entirely right as we would also need to subtract
                                // the number of bytes in the fragment - and how would we know which fragment was found
                                // if there was more than one option of different sizes?
                                // But not sure if this  matters anyway possibly the statement never evaluates to true:
                                // - would eofSubsequence = true ever occur if searching forwards?
                                if (matchFound
                                        && eofSubsequence
                                        && rightFragmentPositions[0] > this.maxSeqOffset) {
                                    matchFound = false;
                                }

                                matchPosition = matchFound ? rightFragmentPositions[0] : matchPosition;
                            }
                            if (matchFound) {
                                targetFile.setFileMarker(matchPosition + 1L);
                                entireSequenceFound = true;
                                break;
                            }
                        }
                        matchPosition += 1;
                    } else {
                        break;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            getLog().debug(e.getMessage());
        } catch (IOException e) {
            getLog().error(e.getMessage());
        } catch (Exception e) {
            getLog().debug(e.getMessage());
        }
        //CHECKSTYLE:ON
        return entireSequenceFound;
    }

    /**
     * Searches for the right fragments of this subsequence between the given byte
     * offsetPositions in the file.  Either returns the last byte taken up by the
     * identified sequences or returns -2 if no match was found
     *
     * @param bytes           the binary file to be identified
     * @param leftBytePos     left-most byte positionInFile of allowed search window on file
     * @param rightBytePos    right-most byte positionInFile of allowed search window on file
     * @param searchDirection 1 for a left to right search, -1 for right to left
     * @param offsetRange     range of possible start offsetPositions in the direction of searchDirection
     * @param fragments       list of fragments at positions for which to check
     * @param finalOffsetFoundPositions  Class to hold information about the file positions and offsets at which the
     *                                   fragment(s) in the final fragment option was (or were) previously found in the
     *                                   file.  Will be null unless we're rechecking the final position fragment(s) for
     *                                   compliance with the overall sequence offset after all the fragments were found
     *                                   with valid offsets in relation to each other in the first pass check.
     * @return
     */
    //CHECKSTYLE:OFF - way, way, way too complex.
    private long[] bytePosForRightFragments(final WindowReader bytes, final long leftBytePos, final long rightBytePos,
                                            final int searchDirection, final int offsetRange,
                                            List<List<SideFragment>> fragments,
                                            OffsetAndFilePositions finalOffsetFoundPositions) {
        //CHECKSTYLE:ON
        final boolean leftFrag = false;
        long startPos = leftBytePos;
        int posLoopStart = 1;

        final int numFragPos = fragments.size();
        if (searchDirection == -1) {
            startPos = rightBytePos;
            posLoopStart = numFragPos;
        }

        //now set up the array so that it can potentially hold all possibilities
        int totalNumOptions = offsetRange + 1;

        for (int iFragPos = 1; iFragPos <= numFragPos; iFragPos++) {
            //totalNumOptions = totalNumOptions * this.getNumAlternativeFragments(leftFrag, iFragPos);
            totalNumOptions = totalNumOptions * this.getNumAlternativeFragments(iFragPos, fragments);
        }

        long[] markerPos = new long[totalNumOptions];
        for (int iOffset = 0; iOffset <= offsetRange; iOffset++) {
            markerPos[iOffset] = startPos + iOffset * searchDirection;
        }
        int numOptions = 1 + offsetRange;

        boolean seqNotFound = false;

        //Stack to store intermediate fragment hits. Avoid the overhead of the stack though if there's only one
        // fragment option and/or none of the fragment positions have variable offsets.
        Stack<FragmentHit> fragmentHits = null;
        if (this.useRightFragmentBackTrack) {
            fragmentHits = new Stack<FragmentHit>();
        }

        boolean recheckingFinalFragmentOption = finalOffsetFoundPositions != null
                && finalOffsetFoundPositions.getFirstPositionInFile() != -1;

        for (int iFragPos = posLoopStart;
             (!seqNotFound) && (iFragPos <= numFragPos) && (iFragPos >= 1);
             iFragPos += searchDirection) {
            final List<SideFragment> fragmentsAtPosition = fragments.get(iFragPos - 1);
            final int numAltFrags = fragmentsAtPosition.size();
            //array to store possible end offsetPositions after this fragment positionInFile has been examined
            long[] tempEndPos = new long[numAltFrags * numOptions];

            int numEndPos = 0;

            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end offsetPositions
            FRAGS_AT_POSITION:
                for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
                    SideFragment fragment = fragmentsAtPosition.get(iAlt);

                    // If checking for a further occurrence of a fragment found previously,  then adjust the offsets to
                    // take account of the previous occurrence in relation to the previous fragment position (or main
                    // sequence).  We'll need to clone the fragment in such cases as the adjustment applies only for
                    // the current file!
                    long previousInstanceOffsetFoundPosition = finalOffsetFoundPositions
                            == null  || !recheckingFinalFragmentOption ? -1
                            : finalOffsetFoundPositions.getOffsetPosition(iAlt);

                    long previousInstanceFilePosition = -1;

                    if (finalOffsetFoundPositions != null && recheckingFinalFragmentOption
                            && previousInstanceOffsetFoundPosition != -1) {
                        fragment = fragment.copy();
                        //This will be the offset of the last byte in in the fragment form BOF
                        previousInstanceFilePosition = finalOffsetFoundPositions.getFilePosition(iAlt);

                        long newMaxOffset = fragment.getMaxOffset() - previousInstanceOffsetFoundPosition
                                - (leftBytePos - (previousInstanceFilePosition - fragment.getNumBytes() + 1));
                        long newMinOffset = fragment.getMinOffset() - previousInstanceOffsetFoundPosition
                                - (leftBytePos - (previousInstanceFilePosition - fragment.getNumBytes() + 1));

                        fragment.setMinOffset((int) Math.max(newMinOffset, 0));
                        fragment.setMaxOffset((int) newMaxOffset);
                        if (fragment.getMaxOffset() < 0) {
                            continue;
                        }
                    }

                    long tempFragEnd;
                    if (searchDirection == -1) {
                        tempFragEnd =
                                this.endBytePosForSeqFrag(bytes, leftBytePos,
                                        markerPos[iOption], false, searchDirection, iFragPos, fragment);
                    } else {
                        tempFragEnd =
                                this.endBytePosForSeqFrag(bytes, markerPos[iOption],
                                        rightBytePos, false, searchDirection, iFragPos, fragment);
                    }

                    if (tempFragEnd > -1) { // a match has been found
                        tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                        numEndPos += 1;

                        //Get the offset at which the fragment was actually found,
                        long offSetFound = tempFragEnd - markerPos[iOption] - fragment.getNumBytes() + 1;

                        // ...and, if the current or following fragment position has a variable offset,  add to the
                        // stack as the last successful fragment match and current backtrack target.
                        if (fragmentHits != null && iFragPos < numFragPos
                                &&
                                (this.orderedRightFragsHaveVariableOffset[iFragPos]
                                        || this.orderedRightFragsHaveVariableOffset[iFragPos - 1])
                        ) {
                            FragmentHit fragmentHit =
                                new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos - 1 ], offSetFound);
                            fragmentHits.push(fragmentHit);
                        }

                        // Record the fragment position to pass back, if we're on the final option
                        if (iFragPos == numFragPos) {
                            //TODO: Long or INT.  ?? Suggest longs throughout...
                            if (finalOffsetFoundPositions != null) {
                                finalOffsetFoundPositions.setPosition(iAlt, (int) offSetFound, tempFragEnd);
                            }
                        }
                    } else {
                        //Assuming we're on the final possible fragment for this position and none so far have matched..
                        if (iAlt == numAltFrags - 1 && numEndPos == 0 && iOption == numOptions - 1) {
                            //No match was found for the current fragment.  Check back through any earlier fragment
                            // matches to see if there are any further occurences of a fragment within its offset range,
                            // and if so, revert to that point and resume checking from there.

                            while (fragmentHits != null &&  !fragmentHits.empty()) {
                                FragmentHit lastGoodFragRef = fragmentHits.pop();
                                //Retrieve the fragment that corresponds to the last successful match.  Create a copy
                                // of this fragment which can then be used to test for a further match based on a new
                                // offset defined from the previous position.  We need to use a clone because this
                                // class instance is used to check multiple files on different threads and the revised
                                // check only applies to this specific file. Alternatively we could copy the original
                                // list at the outset but this would potentially create additional objects on the heap
                                // unnecessarily and impact performance.
                                fragment = fragments.get(lastGoodFragRef.getFragmentSignaturePosition() - 1)
                                    .get(lastGoodFragRef.getAlternativeFragmentNumber()).copy();

                                //Adjust the offsets so that we now look for a further occurrence of the fragment
                                // to the left or right of the earlier match.
                                fragment.setMinOffset(Math.max(fragment.getMinOffset()
                                    - (int) lastGoodFragRef.getOffsetFound()  - fragment.getNumBytes(), 0));
                                fragment.setMaxOffset(fragment.getMaxOffset()
                                    - (int) lastGoodFragRef.getOffsetFound()  - fragment.getNumBytes());
                                if (fragment.getMaxOffset() < 0) {
                                    break;
                                }

                                //Check for a further occurrence of the fragment beyond the last match position
                                if (searchDirection == 1) {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, lastGoodFragRef.getPositionInFile(),
                                                    rightBytePos, false, searchDirection,
                                                    lastGoodFragRef.getFragmentSignaturePosition(), fragment);
                                } else {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, leftBytePos,
                                                    lastGoodFragRef.getPositionInFile(), false, searchDirection,
                                                    lastGoodFragRef.getFragmentSignaturePosition(), fragment);
                                }

                                if (tempFragEnd > -1L) {
                                    //Add the newly found fragment instance to the top of the stack and reset the loop
                                    // position to resume checking for further fragments from that point.
                                    tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                                    //CHECKSTYLE:OFF : Quite legitimate to modify control variables here, as we're
                                    // reverting to an earlier fragment!
                                    iFragPos = lastGoodFragRef.getFragmentSignaturePosition();
                                    iAlt = lastGoodFragRef.getAlternativeFragmentNumber();
                                    //CHECKSTYLE:ON
                                    //Get the offset of this new instance of the current fragment from the previous
                                    //fragment, or main sequence if this is the first fragment.
                                    long newOffSetFoundFromPreviousMatch = tempEndPos[numEndPos]
                                        - lastGoodFragRef.getPositionInFile() + lastGoodFragRef.getOffsetFound();
                                    FragmentHit fragmentHit =
                                        new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos],
                                            newOffSetFoundFromPreviousMatch);
                                    fragmentHits.push(fragmentHit);
                                    numEndPos += 1;

                                    break FRAGS_AT_POSITION;
                                }
                            }
                        }
                    }
                }
            }

            if (numEndPos == 0) {
                seqNotFound = true;
            } else {
                numOptions = 0;
                for (int iOption = 0; iOption < numEndPos; iOption++) {
                    //eliminate any repeated end offsetPositions
                    boolean addEndPos = true;
                    for (int iMarker = 0; iMarker < numOptions; iMarker++) {
                        if (markerPos[iMarker] == tempEndPos[iOption]) {
                            addEndPos = false;
                            break;
                        }
                    }
                    if (addEndPos) {
                        markerPos[numOptions] = tempEndPos[iOption];
                        numOptions++;
                    }
                }
            }
        }

        if (fragmentHits != null) {
            fragmentHits.clear();
        }

        //prepare array to be returned
        if (seqNotFound) {
            // no possible offsetPositions found, return 0 length array
            return new long[0];
        }
        // return ordered array of possibilities
        long[] outArray = new long[numOptions];

        // convert values to negative temporarily so that reverse
        // sort order can be obtained for a right to left search direction
        if (searchDirection < 0) {
            for (int iOption = 0; iOption < numOptions; iOption++) {
                markerPos[iOption] = -markerPos[iOption];
            }
        }

        //sort the values in the array
        Arrays.sort(markerPos, 0, numOptions);

        //convert values back to positive now that a reverse sort order has been obtained
        if (searchDirection < 0) {
            for (int iOption = 0; iOption < numOptions; iOption++) {
                markerPos[iOption] = -markerPos[iOption];
            }
        }

        //copy to a new array which has precisely the correct length
        System.arraycopy(markerPos, 0, outArray, 0, numOptions);

        //correct the value
        for (int iOption = 0; iOption < numOptions; iOption++) {
            outArray[iOption] -= searchDirection;
        }

        return outArray;
    }


    /**
     * searches for the specified fragment sequence
     * between the leftmost and rightmost byte offsetPositions that are given.
     * returns the end positionInFile of the found sequence or -1 if it is not found
     *
     * @param bytes           The bytes being reviewed for identification
     * @param leftEndBytePos  leftmost positionInFile in file at which to search
     * @param rightEndBytePos rightmost postion in file at which to search-
     * @param leftFrag        flag to indicate whether looking at left or right fragments
     * @param searchDirection direction in which search is carried out (1 for left to right, -1 for right to left)
     * @param fragPos         positionInFile of left/right sequence fragment to use
     * @param fragment        The fragment to search for.
     * @return                If subSeqFound = true, an array containing the file positions of each of the options
     *                           found for the final fragment position.  These values represent the offset of
     *                           the first byte of the fragment from BOF.  E.g. say the furthest right fragment position
     *                           has only one option - the 2-byte fragment BB AA, and the the  file consists of the
     *                           following bytes:  00 00 11 22 33 44 55 00 00 FF EE 00  CC DD BB AA 00 00 00
     *                           Then - Cp - the return value will be a single element array with the value 14.
     *                        If subSeqFound = true, new long[0].
     */
    //CHECKSTYLE:OFF too long and complex.
    private long endBytePosForSeqFrag(final WindowReader bytes,
                                      final long leftEndBytePos, final long rightEndBytePos,
                                      final boolean leftFrag, final int searchDirection, final int fragPos,
                                      final SideFragment fragment) {
        //CHECKSTYLE:ON
        long startPosInFile;
        long lastStartPosInFile;
        long endPosInFile = -1L;
        final long searchDirectionL = searchDirection;
        int minOffset;
        int maxOffset;
        final int numBytes = fragment.getNumBytes();
        final int byteOffset = (searchDirection == 1) ? 0 : numBytes - 1;

        if (leftFrag && (searchDirection == -1)) {
            minOffset = fragment.getMinOffset();
            maxOffset = fragment.getMaxOffset();
        } else if (!leftFrag && (searchDirection == 1)) {
            minOffset = fragment.getMinOffset();
            maxOffset = fragment.getMaxOffset();
        } else if (fragPos < this.getNumFragmentPositions(leftFrag)) {
            final SideFragment nextFragment = this.getFragment(leftFrag, fragPos + 1, 0);
            minOffset = nextFragment.getMinOffset();
            maxOffset = nextFragment.getMaxOffset();
        } else {
            minOffset = 0;
            maxOffset = 0;
        }

        // set up start and end offsetPositions for searches taking into account min and max offsets
        if (searchDirection == -1) {
            startPosInFile = rightEndBytePos - minOffset;
            final long lastStartPosInFile1 = leftEndBytePos + numBytes - 1L;
            final long lastStartPosInFile2 = rightEndBytePos - maxOffset;
            lastStartPosInFile = (lastStartPosInFile1 < lastStartPosInFile2)
                    ? lastStartPosInFile2 : lastStartPosInFile1;
        } else {
            startPosInFile = leftEndBytePos + minOffset;
            final long lastStartPosInFile1 = rightEndBytePos - numBytes + 1L;
            final long lastStartPosInFile2 = leftEndBytePos + maxOffset;
            lastStartPosInFile = (lastStartPosInFile1 < lastStartPosInFile2)
                    ? lastStartPosInFile1 : lastStartPosInFile2;
        }

        //keep searching until either the sequence fragment is found or until the end of the search area has been
        // reached. Compare sequence with file contents directly at fileMarker positionInFile
        while (searchDirectionL * (lastStartPosInFile - startPosInFile) >= 0L) {
            try {
                if (fragment.matchesBytes(bytes, startPosInFile - byteOffset)) {
                    endPosInFile = startPosInFile + (numBytes * searchDirectionL) - searchDirectionL;
                    break;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            startPosInFile += searchDirectionL;
        }
        return endPosInFile;  //this is -1 unless subSeqFound = true
    }

    /*
    * Searches for the left fragments of this subsequence between the given byte
    * offsetPositions in the file.  Either returns the last byte taken up by the
    * identified sequences or returns -2 if no match was found
    *
    * @param bytes           the binary file to be identified
    * @param leftBytePos     left-most byte positionInFile of allowed search window on file
    * @param rightBytePos    right-most byte positionInFile of allowed search window on file
    * @param searchDirection 1 for a left to right search, -1 for right to left
    * @param offsetRange     range of possible start offsetPositions in the direction of searchDirection
    * @param fragments       list of fragments at positions for which to check
    * @param finalOffsetFoundPositions  Class to hold information about the file positions and offsets at which the
    *                                   fragment(s) in the final fragment option was (or were) previously found in the
    *                                   file.  Will be null unless we're rechecking the final position fragment(s) for
    *                                   compliance with the overall sequence offset after all the fragments were found
    *                                   with valid offsets in relation to each other in the first pass check.
    * @return               If subSeqFound = true, an array containing the file positions of each of the options
    *                           found for the final fragment position.  These values represent the offset of
    *                           the first byte of the fragment from BOF.  E.g. say the furthest left fragment position
    *                           has only one option - the 2-byte fragment AA BB, and the the start of the file reads
    *                           00 00 AA BB CC DD .... Then - Cp - the return value will be a single element array
    *                           with the value 2.
    *                        If subSeqFound = true, new long[0].
    */
    //CHECKSTYLE:OFF - way, way, way too complex.
    private long[] bytePosForLeftFragments(final WindowReader bytes, final long leftBytePos, final long rightBytePos,
                                           final int searchDirection, final int offsetRange,
                                           List<List<SideFragment>> fragments,
                                           OffsetAndFilePositions finalOffsetFoundPositions) {
    //CHECKSTYLE:ON
        final boolean leftFrag = true;

        // set up loop start and end depending on search order:
        final int numFragPos = fragments.size(); // getNumFragmentPositions(leftFrag);
        long startPos;
        int posLoopStart;
        if (searchDirection == 1) {
            startPos = leftBytePos;
            posLoopStart = numFragPos;
        } else {
            startPos = rightBytePos;
            posLoopStart = 1;
        }

        // Calculate the total possible number of options in all the fragments:
        //TODO: can most of this calculation be done up front?
        int totalNumOptions = offsetRange + 1;
        for (int iFragPos = 1; iFragPos <= numFragPos; iFragPos++) {
            totalNumOptions = totalNumOptions * this.getNumAlternativeFragments(iFragPos, fragments);
        }

        //now set up the array so that it can potentially hold all possibilities
        long[] markerPos = new long[totalNumOptions];
        for (int iOffset = 0; iOffset <= offsetRange; iOffset++) {
            markerPos[iOffset] = startPos + iOffset * searchDirection;
        }
        int numOptions = 1 + offsetRange;

        // Search for the fragments:
        boolean seqNotFound = false;

        //Stack to store intermediate fragment hits. Avoid the overhead of the stack though if there's
        // only one fragment option  and/or none of the fragment positions have variable offsets.
        Stack<FragmentHit> fragmentHits = null;
        if (this.useLeftFragmentBackTrack) {
            fragmentHits = new Stack<FragmentHit>();
        }

        boolean recheckingFinalFragmentOption = finalOffsetFoundPositions != null
                && finalOffsetFoundPositions.getFirstPositionInFile() != -1;

        for (int iFragPos = posLoopStart; (!seqNotFound) && (iFragPos <= numFragPos) && (iFragPos >= 1);
             // so for r-l left search i.e. -1, this is effectively iFragPos++...
             iFragPos -= searchDirection) {
            final List<SideFragment> fragmentsAtPosition = fragments.get(iFragPos - 1);
            final int numAltFrags = fragmentsAtPosition.size();
            //array to store possible end offsetPositions after this fragment positionInFile has been examined
            long[] tempEndPos = new long[numAltFrags * numOptions];

            int numEndPos = 0;
            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end offsetPositions

            FRAGS_AT_POSITION:
                for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
                    SideFragment fragment = fragmentsAtPosition.get(iAlt);

                    // If checking for a further occurrence of a fragment found previously, then adjust the offsets
                    // to take account of the previous occurrence in relation to the previous fragment position (or main
                    // sequence).  We'll need to clone the fragment in such cases as the adjustment applies only for
                    // the current file!
                    // If it's more than 1 we're not on a recheck of the final fragment...
                    long previousInstanceOffsetFoundPosition = finalOffsetFoundPositions == null
                        || !recheckingFinalFragmentOption ? -1 : finalOffsetFoundPositions.getOffsetPosition(iAlt);

                    long previousInstanceFilePosition = -1;

                    if (finalOffsetFoundPositions != null && recheckingFinalFragmentOption
                        && previousInstanceOffsetFoundPosition != -1) {

                        fragment = fragment.copy();

                        previousInstanceFilePosition = finalOffsetFoundPositions.getFilePosition(iAlt);

                        fragment.setMinOffset((int) Math.max(fragment.getMinOffset()
                            - previousInstanceOffsetFoundPosition - fragment.getNumBytes()
                                - (previousInstanceFilePosition - rightBytePos) + 1, 0));
                        fragment.setMaxOffset((int) (fragment.getMaxOffset()
                            - previousInstanceOffsetFoundPosition
                                - fragment.getNumBytes() - (previousInstanceFilePosition - rightBytePos) + 1));

                        if (fragment.getMaxOffset() < 0) {
                            continue;
                        }
                    }

                    long tempFragEnd;
                    if (searchDirection == 1) {
                        tempFragEnd =
                                this.endBytePosForSeqFrag(bytes, markerPos[iOption],
                                        rightBytePos, true, searchDirection,
                                        iFragPos, fragment);
                    } else {
                        //i.e. the offset of the fragment form BOF
                        tempFragEnd =
                                this.endBytePosForSeqFrag(bytes, leftBytePos,
                                        markerPos[iOption], true, searchDirection,
                                        iFragPos, fragment);
                    }
                    if (tempFragEnd > -1L) { // a match has been found
                        tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                        numEndPos += 1;

                        //Get the offset at which the fragment was actually found,
                        long    offSetFound = markerPos[iOption] - tempFragEnd - fragment.getNumBytes() + 1;

                        // ...and, if the current or following fragment position has a variable offset,  add to the
                        // stack as the last successful fragment match and current backtrack target.
                        if (fragmentHits != null && iFragPos < numFragPos
                            && (this.orderedLeftFragsHaveVariableOffset[iFragPos]
                                    || this.orderedLeftFragsHaveVariableOffset[iFragPos - 1])
                        ) {
                            FragmentHit fragmentHit =
                                new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos - 1 ], offSetFound);
                            fragmentHits.push(fragmentHit);
                        }

                        // Record the fragment position to pass back, if we're on the final fragment position
                        if (iFragPos == numFragPos) {
                            //TODO: Long or INT.  ?? Suggest longs throughout but this involves refactoring elsewhere...
                            if (finalOffsetFoundPositions != null) {
                                finalOffsetFoundPositions.setPosition(iAlt, (int) offSetFound, tempFragEnd);
                            }
                        }
                    } else {
                        //Assuming we're on the final possible fragment for this position and none so far have matched..
                        if (iAlt == numAltFrags - 1  && numEndPos == 0 && iOption == numOptions - 1) {
                            //No match was found for the current fragment.  Check back through any earlier fragment
                            // matches to see if there are any further occurrences of a fragment within its
                            // offset range, and if so, revert to that point and resume checking from there.

                            while (fragmentHits != null && !fragmentHits.empty()) {

                                FragmentHit lastGoodFragRef = fragmentHits.pop();
                                //Retrieve the fragment that corresponds to the last successful match.  Create a copy
                                // of this fragment which can then be used to test for a further match based on a new
                                // offset defined from the previous position.  We need to use a clone because this
                                // class instance is used to check multiple files on different threads and the revised
                                // check only applies to this specific file. Alternatively we could copy the original
                                // list at the outset but this would potentially create additional objects on the heap
                                // unnecessarily and impact performance.
                                fragment = fragments.get(lastGoodFragRef.getFragmentSignaturePosition() - 1)
                                    .get(lastGoodFragRef.getAlternativeFragmentNumber()).copy();

                                //Adjust the offsets so that we now look for a further occurrence of the fragment
                                // to the left of the earlier match.
                                fragment.setMinOffset(Math.max(fragment.getMinOffset()
                                    - (int) lastGoodFragRef.getOffsetFound() - fragment.getNumBytes(), 0));
                                fragment.setMaxOffset(fragment.getMaxOffset()
                                    - (int) lastGoodFragRef.getOffsetFound() - fragment.getNumBytes());
                                if (fragment.getMaxOffset() < 0) {
                                    break;
                                }

                                //Check for a further occurrence of the fragment beyond the last match position
                                if (searchDirection == 1) {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, lastGoodFragRef.getPositionInFile(),
                                                    rightBytePos, true, searchDirection,
                                                    lastGoodFragRef.getFragmentSignaturePosition(), fragment);
                                } else {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, leftBytePos,
                                                    lastGoodFragRef.getPositionInFile(), true, searchDirection,
                                                    lastGoodFragRef.getFragmentSignaturePosition(), fragment);
                                }

                                if (tempFragEnd > -1L) {
                                    tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                                    //Add the newly found fragment instance to the top of the stack and reset the loop
                                    // position to resume checking for further fragments from that point.
                                    //CHECKSTYLE:OFF : Quite legitimate to modify control variables here, as we're
                                    // reverting to an earlier fragment!
                                    iFragPos = lastGoodFragRef.getFragmentSignaturePosition();
                                    iAlt = lastGoodFragRef.getAlternativeFragmentNumber();
                                    //CHECKSTYLE:ON
                                    //Get the offset of this new instance of the current fragment from the previous
                                    //fragment, or main sequence if this is the first fragment.
                                    long newOffSetFoundFromPreviousMatch = (lastGoodFragRef.getPositionInFile()
                                            - tempFragEnd) + lastGoodFragRef.getOffsetFound() + 1;
                                    FragmentHit fragmentHit = new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos],
                                        newOffSetFoundFromPreviousMatch);
                                    fragmentHits.push(fragmentHit);
                                    numEndPos += 1;

                                    break FRAGS_AT_POSITION;
                                }
                            }
                        }
                    }
                }
            }
            if (numEndPos == 0) {
                seqNotFound = true;
            } else {
                numOptions = 0;
                for (int iOption = 0; iOption < numEndPos; iOption++) {
                    //eliminate any repeated end offsetPositions
                    boolean addEndPos = true;
                    for (int iMarker = 0; iMarker < numOptions; iMarker++) {
                        if (markerPos[iMarker] == tempEndPos[iOption]) {
                            addEndPos = false;
                            break;
                        }
                    }
                    if (addEndPos) {
                        markerPos[numOptions] = tempEndPos[iOption];
                        numOptions++;
                    }
                }
            }
        }

        if (fragmentHits != null) {
            fragmentHits.clear();
        }

        //prepare array to be returned
        if (seqNotFound) {
            // no possible offsetPositions found, return 0 length array
            return new long[0];
        }
        // return ordered array of possibilities
        long[] outArray = new long[numOptions];

        // convert values to negative temporarily so that reverse sort order
        // can be obtained for a right to left search direction
        if (searchDirection < 0) {
            for (int iOption = 0; iOption < numOptions; iOption++) {
                markerPos[iOption] = -markerPos[iOption];
            }
        }

        //sort the values in the array
        Arrays.sort(markerPos, 0, numOptions);

        //convert values back to positive now that a reverse sort order has been obtained
        if (searchDirection < 0) {
            for (int iOption = 0; iOption < numOptions; iOption++) {
                markerPos[iOption] = -markerPos[iOption];
            }
        }

        //copy to a new array which has precisely the correct length
        System.arraycopy(markerPos, 0, outArray, 0, numOptions);

        //correct the value
        for (int iOption = 0; iOption < numOptions; iOption++) {
            outArray[iOption] -= searchDirection;
        }

        return outArray;
    }

    // The following two  methods exist to cater for situations where the first (or only) left or right fragment in a
    // subsequence occurs more than once to the left or right of the main byte sequence.  In these cases, the initial
    // check will find the instance closest to the main byte sequence that meets the fragment's own offset requirements.
    // However, if this instance is beyond the maximum offset for the sequence as a whole, the whole sequence would
    // previously be set to not match - even though there may be further instances of the  fragment at less than or
    // equal to the maximum sequence offset. So we need to check for any further occurrences to see if there is one that
    // occurs in the byte stream at a position at or before the allowed maximum offset, but not less than the minimum
    // offset.
    /**
     * This method is called when the furthest left fragment has been found at an offset which is beyond
     * the maximum offset from BOF for the sequence as a whole.  It checks for any further occurrences that
     * may be positioned between the minimum and maximum sequence offsets.
     *
     * @param windowReader Byteseek reader within which to search for the fragment(s)
     * @param leftBytePos  Furthest left byte position to search
     * @param rightBytePos  Furthest right byte position to search
     * @param maxOffset     Maximum offset from BOF for the sequence as a whole
     * @param minOffset     Minimum offset from BOF for the sequence as a whole
     * @param leftFragOpt  List of one or more left fragment options, representing the highest numbered left
     *                      fragment position in the signature file.
     * @param lastOffsetFoundPositions An OffsetAndFilePositions instance containing details of the offsets and file
     *                                 positions at which the fragment options in leftFragOpt were previously found.
     * @return True if no fragment occurrence found within the valid offset range for the sequence.
     *         False if one or more occurrences are found within the valid offset range.
     */
    private boolean checkLeftFragmentForInvalidOffset(WindowReader windowReader, final long leftBytePos,
                                                      final long rightBytePos, final long maxOffset,
                                                      final long minOffset, List<List<SideFragment>>  leftFragOpt,
                                                      OffsetAndFilePositions lastOffsetFoundPositions) {

        long[] fragmentPositions;
        long currentRightBytePos = rightBytePos;


        do {
            fragmentPositions =
                    bytePosForLeftFragments(windowReader, leftBytePos,
                            currentRightBytePos, -1, 0, leftFragOpt, lastOffsetFoundPositions);
            if (fragmentPositions.length  > 0) {
                currentRightBytePos = fragmentPositions[0] - 1;
            }
        } while (fragmentPositions.length > 0 && fragmentPositions[fragmentPositions.length - 1] > maxOffset);

        //CHECKSTYLE:OFF  Doesn't like parentheses but they make the code more readable IMHO.
        return (fragmentPositions.length == 0 || fragmentPositions[0] > maxOffset || fragmentPositions[0] < minOffset);
        //CHECKSTYLE:ON
    }

    /**
     * This method is called when the furthest right fragment has been found at an offset which is beyond
     * the maximum offset from EOF for the sequence as a whole.  It checks for any further occurrences that
     * may be positioned between the minimum and maximum sequence offsets.
     *
     * @param windowReader Byteseek reader within which to search for the fragment(s)
     * @param leftBytePos  Furthest left byte position to search
     * @param rightBytePos  Furthest right byte position to search
     * @param maxOffset     Maximum offset from EOF for the sequence as a whole
     * @param minOffset     Minimum offset from EOF for the sequence as a whole
     * @param rightFragOpt  List of one or more right frsagment options, representing the highest numbered right
     *                      fragment position in the signature file.
     * @param lastOffsetFoundPositions An OffsetAndFilePositions instance containing details of the offsets and file
     *                                 positions at which the fragment options in rightFragOpt were previously found.
     * @return True if no fragment occurrence found within the valid offset range for the sequence.
     *         False if one or more occurrences are found within the valid offset range.
     */
    private boolean checkRightFragmentForInvalidOffset(WindowReader windowReader, final long leftBytePos,
                                                       final long rightBytePos, final long maxOffset,
                                                       final long minOffset, List<List<SideFragment>>  rightFragOpt,
                                                            OffsetAndFilePositions lastOffsetFoundPositions) {
        long[] fragmentPositions;
        long currentLeftBytePos = leftBytePos;

        do {
            fragmentPositions =
                    bytePosForRightFragments(windowReader, currentLeftBytePos,
                            rightBytePos, 1, 0, rightFragOpt, lastOffsetFoundPositions);
            if (fragmentPositions.length  > 0) {
                currentLeftBytePos = fragmentPositions[0] + 1;
            }
        } while (fragmentPositions.length > 0 && fragmentPositions[0] < (rightBytePos - maxOffset));

        //CHECKSTYLE:OFF  Doesn't like parentheses but they make the code more readable IMHO.
        return (fragmentPositions.length == 0 || fragmentPositions[0] < (rightBytePos - maxOffset)
            || fragmentPositions[0] > (rightBytePos - minOffset));
        //CHECKSTYLE:ON
    }

    private LeftFragment getRawLeftFragment(final int theIndex) {
        return leftFragments.get(theIndex);
    }

    private RightFragment getRawRightFragment(final int theIndex) {
        return rightFragments.get(theIndex);
    }

    /**
     * Helper class to hold history details of fragment matches found whilst checking a file
     * for matches against the left and/or right fragments in a binary signature.
     */
    private class FragmentHit implements Comparable<FragmentHit> {

        private int fragmentSignaturePosition;
        private int alternativeFragmentNumber;
        private long positionInFile;
        private long offsetFound;

        /**
         *
         * @param fragmentSignaturePosition  The index of the fragment position in the signature to which the fragment
         *                                   hit relates.
         *                       This is 1 based and  correlates with the "Position" attribute in the RightFragment or
         *                       LeftFragment element in the signature file.
         * @param alternativeFragmentNumber The zero-base index of the fragment within the List<SideFragment> in which
         *                       it resides, in other words the index for the fragment option for a given fragment
         *                       position. Usually 0 since mostly there is only one alternative fragment at any given
         *                       position.
         * @param positionInFile The position within the byte stream at which the fragment was found . i.e for a left
         *                       fragment the 1-based offset of the first byte in the fragment from BOF, and for a right
         *                       fragment, the 1-based offset of the   last byte in the fragment from BOF.  Note the use
         *                       of 1 based offsets here rather than the normal 0 based offsets - this corresponds to
         *                       the return value assigned to tempEndFrag from calls to endBytePosForSeqFrag
         * @param offsetFound The offset at which the fragment was found, relative to the previous fragment
         *                    (or main sequence if fragmentSignaturePosition is zero)
         */
        public FragmentHit(int fragmentSignaturePosition, int alternativeFragmentNumber, long positionInFile,
           long offsetFound) {
            this.fragmentSignaturePosition = fragmentSignaturePosition;
            this.alternativeFragmentNumber = alternativeFragmentNumber;
            this.positionInFile = positionInFile;
            this.offsetFound = offsetFound;
        }

        public int getAlternativeFragmentNumber() {
            return alternativeFragmentNumber;
        }


        public int getFragmentSignaturePosition() {
            return fragmentSignaturePosition;
        }

        public long getPositionInFile() {
            return positionInFile;
        }

        public long getOffsetFound() {
            return offsetFound;
        }

        @Override
        public int compareTo(FragmentHit other) {

            int result;

            if (this.getPositionInFile() > other.getPositionInFile()) {
                result = 1;
            } else if (this.getPositionInFile() < other.getPositionInFile()) {
                result = -1;
            } else {
                result = 0;
            }
            return result;
        }
    }

    /**
     * Helper class to store information about fragments found in a byte stream. The class is used to store
     * information for a given List<SideFragment> - which may contain one or more SideFragments for a
     * given fragment position in a binary signature.  The data stored comprises the offsets and file positions
     * at which SideFragments within the list have been found in the byte stream.
     *
     */
    private class OffsetAndFilePositions {

        private static final int NO_OFFSET_POSITION_FOUND = -1;
        //The indices of theses arrays correspond to the indices in
        // a List<SideFragment>.  E.g.
        // - if there is only one option for a given fragment position, the arrays
        //  will each have only one element.
        // - if there is more than one option for the given fragment position, the arrays
        //  will be initialised with a number of elements to equal the number of options.
        private int[] offsetPositions;
        private long[] filePositions;

        public OffsetAndFilePositions(List<SideFragment> fragments) {
            //Initialise the arrays to contain a number of elements equal to the options for the SideFragment
            // at a given position
            this.offsetPositions = new int[fragments.size()];
            this.filePositions = new long[fragments.size()];
            for (int i = 0; i < fragments.size(); i++) {
                offsetPositions[i] = NO_OFFSET_POSITION_FOUND;
                filePositions[i] = NO_OFFSET_POSITION_FOUND;
            }
        }

        /**
         * Records data about a fragment hit
         * @param altPos The index for the fragment within the originating List<SideFragment> passed to the constructor
         *               A List<SideFragment> is a list of possible alternative fragments that may be found at a given
         *               fragment position (in most cases, there will be only one option so this will be zero)
         * @param offsetPos The offset at which the fragment was found, relative to the previous fragment
         *                  (or main sequence)
         * @param filePos  The position within the byte stream at which the fragment was found . i.e for a left fragment
         * the offset of the first byte in the fragment from BOF, and for a right fragment, the offset of the
         * last byte in the fragment from BOF.
         */
        public void setPosition(int altPos, int offsetPos, long filePos) {
            //TODO: Do we want to check for bounds here and raise a customised exception?
            this.offsetPositions[altPos] = offsetPos;
            this.filePositions[altPos] = filePos;
        }

        /**
         * Returns the offset at which a given fragment has been found
         * @param altPos The index of the fragment in the List<SideFragment> passed to the constructor
         * @return The offset at which the fragment was found relative to the previous fragment (or main sequence)
         */
        public long getOffsetPosition(int altPos) {
            return this.offsetPositions[altPos];
        }

        /**
         * Returns the position within the byte stream at which a given fragment was found
         * @param altPos   The index of the fragment in the List<SideFragment> passed to the constructor
         * @return  The position within the byte stream where the fragment was found. i.e for a left fragment
         * the offset of the first byte in the fragment from BOF, and for a right fragment, the offset of the
         * last byte in the fragment from BOF.
         */
        public long getFilePosition(int altPos) {
            return this.filePositions[altPos];
        }

        /**
         * Finds the first file position (from BOF) at which any of the fragment options have been found
         * @return The lowest numbered file position at which a fragment has been found, or -1
         * if none of the fragments have been found.
         */
        public long getFirstPositionInFile()  {
            if (offsetPositions == null) {
                return -1;
            }

            long temp = Long.MAX_VALUE;
            for (int i = 0; i < offsetPositions.length; i++) {
                if (offsetPositions[i] < temp && offsetPositions[i] != NO_OFFSET_POSITION_FOUND) {
                    temp = offsetPositions[i];
                }
            }
            return temp == Long.MAX_VALUE ? NO_OFFSET_POSITION_FOUND : temp;
        }

    }
}
