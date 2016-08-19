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

import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.searcher.Searcher;
import net.byteseek.searcher.bytes.ByteMatcherSearcher;
import net.byteseek.searcher.sequence.horspool.HorspoolFinalFlagSearcher;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;
import net.byteseek.compiler.CompileException;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.searcher.SearchResult;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;


/**
 * A SubSequence is an extended byte-string to match.
 *
 * It must include at least one unambiguous sequence of
 * bytes or sets of bytes, which can be searched for using
 * the BoyerMooreHorpsool (BMH) algorithm.  This is known as the
 * "anchor" sequence.  
 *
 * <p/>If necessary, it can include Left and 
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
    private boolean hasLeftFragments;
    private boolean hasRightFragments;

    private String subsequenceText;

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
        hasLeftFragments  = !orderedLeftFragments.isEmpty();
        hasRightFragments = !orderedRightFragments.isEmpty();
    }


    /*
     * Re-orders the left and right sequence fragments in increasing positionInFile order.
     * Also calculates the minimum and maximum lengths a fragment can have.
     */
    private void processSequenceFragments() {
        buildOrderedLeftFragments();
        buildOrderedRightFragments();

        optimiseSingleByteAlternatives(orderedLeftFragments);
        optimiseSingleByteAlternatives(orderedRightFragments);

        captureLeftFragments();
        captureRightFragments();

        calculateMinMaxLeftFragmentLength();
        calculateMinMaxRightFragmentLength();

        buildMatcherAndSearcher();

        this.leftFragments = null;
        this.rightFragments = null;
        this.numLeftFragmentPositions = orderedLeftFragments.size();
        this.numRightFragmentPositions = orderedRightFragments.size();
        isInvalidSubSequence = isInvalidSubSequence ? true : checkForInvalidFragments();
    }

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

    private void captureRightFragments() {
        int captureFragPos = -1;
        int numRightPos = orderedRightFragments.size();
        //FRAGS: for (int positionInFile = numRightPos -1; positionInFile >= 0; positionInFile--) {
    FRAGS:
        for (int position = 0; position < numRightPos; position++) {
            List<SideFragment> fragsAtPos = orderedRightFragments.get(position);
            if (fragsAtPos.size() == 1) { // no alternatives at this positionInFile.
                for (SideFragment frag : fragsAtPos) {
                    if (frag.getMinOffset() == 0 && frag.getMaxOffset() == 0) { // bangs right up to the main sequence
                        subsequenceText = subsequenceText + ' ' + frag.toRegularExpression(true);
                        captureFragPos = position;
                        // CHECKSTYLE:OFF     Quite legitmiate to have more than 120 chars per line just now...
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


    private void buildMatcherAndSearcher() {
        try {
            // CHECKSTYLE:OFF     Quite legitimate to have more than 120 chars per line just now...
            matcher = SEQUENCE_COMPILER.compile(subsequenceText);
            if (matcher.length() == 1) {
                searcher = new ByteMatcherSearcher(matcher.getMatcherForPosition(0)); // use simplest byte matcher searcher if the matcher is length 1.
            } else {
                searcher = new HorspoolFinalFlagSearcher(matcher); // use shifting searcher if shifts can be bigger than one.
            }
            // CHECKSTYLE:ON
        } catch (CompileException ex) {
            final String warning = String.format(SEQUENCE_PARSE_ERROR, subsequenceText, ex.getMessage());
            getLog().warn(warning);
            isInvalidSubSequence = true;
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
        //regularExpression.append(ByteSequence.bytesToString(prettyPrint, byteSequence));
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

                            OffsetAndFilePositions fragmentFoundPos = new OffsetAndFilePositions();
                            final long[] rightFragmentPositions =
                                    bytePosForRightFragments(windowReader, matchPosition + matchLength,
                                            targetFile.getFileMarker(), 1, 0, orderedRightFragments, fragmentFoundPos);
                            matchFound = rightFragmentPositions.length > 0;

                            boolean rightMostFragmentPositionInvalid = true;


                            if(matchFound) {
                                //Get the positionInFile of the fragment furthest to the right of the main sequence (and nearest the
                                // end of the file).
                                long currentRightmostFragmentPosition =
                                        rightFragmentPositions[rightFragmentPositions.length -1];

                                // Get the fragment option furthest to the right of the main sequence (and nearest the
                                // end of the file).
                                List<List<SideFragment>> furthestRightFragmentOption =
                                       this.orderedRightFragments.subList(orderedRightFragments.size() -1,orderedRightFragments.size());

                                // Get the number of bytes in the rightmost fragment.  Whilst there may be more than one
                                // option for the fragment value at this positionInFile, the length for each option will always
                                // be the same, so we only need to check the first one.
                                //SideFragment rightmostFragment = furthestRightFragmentOption.get(0).get(0);
                                //long fragmentLength = rightmostFragment.getNumBytes();

                                // BNO: If the fragment found is beyond the minimum offset, we already know its offset
                                // is invalid.  If it is greater than the maximum offset however, we need to check for
                                // any further occurrences that may be positioned before the maximum and minimum offsets.
                                if((currentRightmostFragmentPosition) <= (lastBytePositionInFile - this.minSeqOffset)) {
                                    rightMostFragmentPositionInvalid =
                                            currentRightmostFragmentPosition < (lastBytePositionInFile - this.maxSeqOffset)
                                            &&
                                            checkRightFragmentForInvalidOffset(
                                            windowReader, currentRightmostFragmentPosition + 1,
                                            lastBytePositionInFile, this.maxSeqOffset, this.minSeqOffset,
                                            furthestRightFragmentOption, fragmentFoundPos);
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

                // If we're starting outside a possible match positionInFile,
                // don't continue:
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

                            OffsetAndFilePositions finalOptionOffSetFoundPositions = new OffsetAndFilePositions(furthestLeftFragmentOption.get(0));


                            final long[] leftFragmentPositions =
                                    bytePosForLeftFragments(windowReader, targetFile.getFileMarker(),
                                            matchPosition - matchLength, -1, 0, orderedLeftFragments, finalOptionOffSetFoundPositions);
                            matchFound = leftFragmentPositions.length > 0;

                            boolean leftMostFragmentPositionInvalid = true;


                            if (matchFound) {
                                long currentNearestBOFLeftmostFragmentPosition = leftFragmentPositions[leftFragmentPositions.length -1];
                                long currentFurthestBOFLeftmostFragmentPosition = leftFragmentPositions.length > 1 ? leftFragmentPositions[0] :currentNearestBOFLeftmostFragmentPosition;



                                // BNO: If the fragment found is beyond the minimum offset, we already know its offset
                                // is invalid.  If it is greater than the maximum offset however, we need to check for
                                // any further occurrences that may be positioned before the maximum offset.
                                if (currentNearestBOFLeftmostFragmentPosition >= this.minSeqOffset) {
                                    leftMostFragmentPositionInvalid = ((currentFurthestBOFLeftmostFragmentPosition > this.maxSeqOffset
                                    )
                                            && checkLeftFragmentForInvalidOffset(windowReader,
                                            0,
                                            currentNearestBOFLeftmostFragmentPosition,
                                            this.maxSeqOffset,
                                            this.minSeqOffset, furthestLeftFragmentOption, finalOptionOffSetFoundPositions));
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
     * @return
     */
    //CHECKSTYLE:OFF - way, way, way too complex.
/*    private long[] bytePosForRightFragments(final WindowReader bytes, final long leftBytePos, final long rightBytePos,
                                            final int searchDirection, final int offsetRange) {
        //CHECKSTYLE:ON
        final boolean leftFrag = false;
        long startPos = leftBytePos;
        int posLoopStart = 1;
        final int numFragPos = numRightFragmentPositions;
        if (searchDirection == -1) {
            startPos = rightBytePos;
            posLoopStart = numFragPos;
        }

        //now set up the array so that it can potentially hold all possibilities
        int totalNumOptions = offsetRange + 1;
        for (int iFragPos = 1; iFragPos <= numFragPos; iFragPos++) {
            totalNumOptions = totalNumOptions * this.getNumAlternativeFragments(leftFrag, iFragPos);
        }
        long[] markerPos = new long[totalNumOptions];
        for (int iOffset = 0; iOffset <= offsetRange; iOffset++) {
            markerPos[iOffset] = startPos + iOffset * searchDirection;
        }
        int numOptions = 1 + offsetRange;

        boolean seqNotFound = false;
        for (int iFragPos = posLoopStart;
             (!seqNotFound) && (iFragPos <= numFragPos) && (iFragPos >= 1);
             iFragPos += searchDirection) {
            final List<SideFragment> fragmentsAtPosition = orderedRightFragments.get(iFragPos - 1);
            final int numAltFrags = fragmentsAtPosition.size();
            //array to store possible end offsetPositions after this fragment positionInFile has been examined
            long[] tempEndPos = new long[numAltFrags * numOptions];
            int numEndPos = 0;


            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end offsetPositions
                for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
                    final SideFragment fragment = fragmentsAtPosition.get(iAlt);
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
    }*/

    //Similar to above, but user can pass in the list of fragments.
    private long[] bytePosForRightFragments(final WindowReader bytes, final long leftBytePos, final long rightBytePos,
                                            final int searchDirection, final int offsetRange,
                                            List<List<SideFragment>> fragments, OffsetAndFilePositions offsetFoundPositions) {
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
            totalNumOptions = totalNumOptions * this.getNumAlternativeFragments(leftFrag, iFragPos);
        }

        long[] markerPos = new long[totalNumOptions];
        for (int iOffset = 0; iOffset <= offsetRange; iOffset++) {
            markerPos[iOffset] = startPos + iOffset * searchDirection;
        }
        int numOptions = 1 + offsetRange;

        boolean seqNotFound = false;

        //TODO: Possibly avoid the overhead of the stack if there's only one fragment position and option?
        Stack<FragmentHit> fragmentHits = new Stack<FragmentHit>();

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

                    // If we're checking for a further occurrence of a fragment found previously, then adjust the offsets
                    // to take account of the previous occurrence in relation to the previous fragment position (or main
                    // sequence).  We'll need to clone the fragment in such cases as the adjustment applies only for
                    // the current file!
                    if(offsetFoundPositions != null && offsetFoundPositions.getOffsetPosition(iAlt) != -1) {
                        fragment = fragment.copy();
                        long previousInstanceOffsetFoundPosition = offsetFoundPositions.getOffsetPosition(iAlt);
                        long previousInstanceFilePosition = offsetFoundPositions.getFilePosition(iAlt);
                        fragment.setMinOffset(Math.max(fragment.getMinOffset() - (int) previousInstanceOffsetFoundPosition  - fragment.getNumBytes(), 0));
                        fragment.setMaxOffset(fragment.getMaxOffset() - (int) previousInstanceOffsetFoundPosition  - fragment.getNumBytes());
                        if(fragment.getMaxOffset() < 0) {
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

                        //Get the offset at which the fragment was actually found, and add to the stack as the last
                        // successful fragment match.
                        if (fragmentHits != null) {
                            long offSetFound = tempFragEnd - markerPos[iOption] - fragment.getNumBytes() + 1;
                            FragmentHit fragmentHit = new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos -1 ], offSetFound);
                            fragmentHits.push(fragmentHit);
                        }
/*                        else {
                            //TODO: Do we need to allow for multiple final fragment options with potentially different sizes?
                            finalFragmentOffsetFoundPosition = tempFragEnd - markerPos[iOption] - fragment.getNumBytes() + 1;
                        }*/
                    } else {
                        //Assuming we're on the final possible fragment for this position and none so far have matched...
                        if(iAlt == numAltFrags - 1 && numEndPos == 0) {
                            //No match was found for the current fragment.  Check back through any earlier fragment matches
                            //to see if there are any further occurences of a fragment within its offset range, and if so,
                            //revert to that point and resume checking from there.

                            while (fragmentHits != null &&  !fragmentHits.empty()) {
                                FragmentHit lastGoodFragRef = fragmentHits.pop();
                                //Retrieve the fragment that corresponds to the last successful match.  Create a copy of this fragment which can then be used
                                // to test for a further match based on a new offset defined from the previous position.  We need to use a clone
                                // because this class instance is used to check multiple files and the revised check only applies to this specific file.
                                // alternatively we could copy the original list at the outset but this would potentially cr
                                // eate additional objects
                                // on the heap unnecessarily and impact performance.
                                fragment = fragments.get(lastGoodFragRef.getFragmentPosition() - 1).get(lastGoodFragRef.getAlternativeFragmentNumber()).copy();

                                //Adjust the offsets so that we now look for a further occurrence of the fragment to the left
                                //or right of the earlier match.
                                fragment.setMinOffset(Math.max(fragment.getMinOffset() - (int) lastGoodFragRef.getOffsetFound()  - fragment.getNumBytes(), 0));
                                fragment.setMaxOffset(fragment.getMaxOffset() - (int) lastGoodFragRef.getOffsetFound()  - fragment.getNumBytes());
                                if(fragment.getMaxOffset() < 0) {
                                    break;
                                }

                                //Check for a further occurrence of the fragment beyond the last match position
                                if (searchDirection == 1) {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, lastGoodFragRef.getPositionInFile(),
                                                    rightBytePos, false, searchDirection,
                                                    lastGoodFragRef.getFragmentPosition(), fragment);
                                } else {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, leftBytePos,
                                                    lastGoodFragRef.getPositionInFile(), false, searchDirection,
                                                    lastGoodFragRef.getFragmentPosition(), fragment);
                                }

                                if (tempFragEnd > -1L) {
                                    //Add the newly found fragment instance to the top of the stack and reset the loop
                                    // position to resume checking for further fragments from that point.
                                    tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                                    iFragPos = lastGoodFragRef.getFragmentPosition();
                                    iAlt = lastGoodFragRef.getAlternativeFragmentNumber();
                                    //Get the offset of this new instance of the current fragment from the previous
                                    //fragment, or main sequence if this is the first fragment.
                                    long newOffSetFoundFromPreviousMatch = tempEndPos[numEndPos] - lastGoodFragRef.getPositionInFile() + lastGoodFragRef.getOffsetFound();
                                    FragmentHit fragmentHit = new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos], newOffSetFoundFromPreviousMatch);
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

        if (offsetFoundPositions != null) {
            if(fragmentHits !=null  && fragmentHits.size() > 0) {
                //TODO: set offsetPositions as required
                //offsetFoundPositions.setPosition(fragmentHits.get(fragmentHits.size() -1).getOffsetFound());
            } else  {
                //offsetFoundPositions.setPosition(finalFragmentOffsetFoundPosition);
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
     * @return
     */
    //CHECKSTYLE:OFF too long and complex.
    private long endBytePosForSeqFrag(final WindowReader bytes,
                                      final long leftEndBytePos, final long rightEndBytePos,
                                      final boolean leftFrag, final int searchDirection, final int fragPos, final SideFragment fragment) {
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

        //keep searching until either the sequence fragment is found
        // or until the end of the search area has been reached.
        //compare sequence with file contents directly at fileMarker positionInFile
        //boolean subSeqFound = false;
        //while ((!subSeqFound) && ((searchDirectionL) * (lastStartPosInFile - startPosInFile) >= 0L)) {
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
    //CHECKSTYLE:OFF Too complex method...
/*
    private long[] bytePosForLeftFragments(final WindowReader bytes, final long leftBytePos, final long rightBytePos,
                                           final int searchDirection, final int offsetRange) {

        final boolean leftFrag = true;

        // set up loop start and end depending on search order:
        final int numFragPos = this.numLeftFragmentPositions; // getNumFragmentPositions(leftFrag);
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
            totalNumOptions = totalNumOptions * this.getNumAlternativeFragments(leftFrag, iFragPos);
        }

        //now set up the array so that it can potentially hold all possibilities
        long[] markerPos = new long[totalNumOptions];
        for (int iOffset = 0; iOffset <= offsetRange; iOffset++) {
            markerPos[iOffset] = startPos + iOffset * searchDirection;
        }
        int numOptions = 1 + offsetRange;

        // Search for the fragments:
        boolean seqNotFound = false;
        for (int iFragPos = posLoopStart; (!seqNotFound) && (iFragPos <= numFragPos) && (iFragPos >= 1);
             iFragPos -= searchDirection) {
            final List<SideFragment> fragmentsAtPosition = orderedLeftFragments.get(iFragPos - 1);
            final int numAltFrags = fragmentsAtPosition.size();
            //array to store possible end offsetPositions after this fragment positionInFile has been examined
            long[] tempEndPos = new long[numAltFrags * numOptions];

            int numEndPos = 0;
            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end offsetPositions
                for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
                    final SideFragment fragment = fragmentsAtPosition.get(iAlt);
                    long tempFragEnd;
                    if (searchDirection == 1) {
                        tempFragEnd =
                                this.endBytePosForSeqFrag(bytes, markerPos[iOption],
                                        rightBytePos, true, searchDirection,
                                        iFragPos, fragment);
                    } else {
                        tempFragEnd =
                                this.endBytePosForSeqFrag(bytes, leftBytePos,
                                        markerPos[iOption], true, searchDirection,
                                        iFragPos, fragment);
                    }
                    if (tempFragEnd > -1L) { // a match has been found
                        tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                        numEndPos += 1;
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
*/

    // Similar to above, but allows the caller to pass in his own List<List<SideFragment>> instead of using
    // the class member orderedLeftFragments
    private long[] bytePosForLeftFragments(final WindowReader bytes, final long leftBytePos, final long rightBytePos,
                                           final int searchDirection, final int offsetRange,
                                           List<List<SideFragment>> fragments,  OffsetAndFilePositions finalOffsetFoundPositions) {

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
            //totalNumOptions = totalNumOptions * this.getNumAlternativeFragments(leftFrag, iFragPos);
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

        //Stack to store intermediate fragment hits.
        //Avoid the overhead of the stack though if there's only one fragment option
        Stack<FragmentHit> fragmentHits = null;
        if(fragments.size() > 1) {
            fragmentHits= new Stack<FragmentHit>();
        }

        boolean recheckingFinalFragmentOption = finalOffsetFoundPositions != null && finalOffsetFoundPositions.getFirstPositionInFile() != -1;

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

                    // If we're checking for a further occurrence of a fragment found previously, then adjust the offsets
                    // to take account of the previous occurrence in relation to the previous fragment position (or main
                    // sequence).  We'll need to clone the fragment in such cases as the adjustment applies only for
                    // the current file!
                    // If it's more than 1 we're not on a recheck of the final fragment...
                    long previousInstanceOffsetFoundPosition = finalOffsetFoundPositions == null ? -1 : finalOffsetFoundPositions.getOffsetPosition(iAlt);
                    long previousInstanceFilePosition = -1;
                    if(finalOffsetFoundPositions != null && recheckingFinalFragmentOption && previousInstanceOffsetFoundPosition != -1) {
                        fragment = fragment.copy();
                            previousInstanceFilePosition = finalOffsetFoundPositions.getFilePosition(iAlt);
                            fragment.setMinOffset((int)Math.max(fragment.getMinOffset() - previousInstanceOffsetFoundPosition - fragment.getNumBytes()  - (previousInstanceFilePosition - rightBytePos) + 1, 0));
                            fragment.setMaxOffset((int)(fragment.getMaxOffset() - previousInstanceOffsetFoundPosition - fragment.getNumBytes() - (previousInstanceFilePosition - rightBytePos) + 1)) ;
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
                        tempFragEnd =
                                this.endBytePosForSeqFrag(bytes, leftBytePos,
                                        markerPos[iOption], true, searchDirection,
                                        iFragPos, fragment);
                    }
                    if (tempFragEnd > -1L) { // a match has been found
                        tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                        numEndPos += 1;

                        //long offSetFound;
                        //if(!recheckingFinalFragmentOption) {
                        long    offSetFound = markerPos[iOption] - tempFragEnd - fragment.getNumBytes() + 1;
                        //} else {
                        //    offSetFound = previousInstanceOffsetFoundPosition + fragment.getNumBytes();
                       // }

                        //Get the offset at which the fragment was actually found, and add to the stack as the last
                        // successful fragment match.
                        //long offSetFound = markerPos[iOption]- tempFragEnd;
                        if (fragmentHits != null && iFragPos < numFragPos) {
                            FragmentHit fragmentHit = new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos -1 ], offSetFound);
                            fragmentHits.push(fragmentHit);
                        }

                        /*else {
                            //We have only one fragment position and are not usinng the stack
                            //TODO: Do we need to allow for multiple final fragment options with potentially different sizes?
                            long currentPos = markerPos[iOption] - tempFragEnd - fragment.getNumBytes() + 1;

                            if()
                            finalFragmentOffsetFoundPosition =
                        }*/

                        // Record the fragment positin to pass back, if we're on the final option
                        if(iFragPos == numFragPos) {
                            //TODO: Long or INT.  ?? Suggest longs throughout...
                            if(finalOffsetFoundPositions != null) {
                                finalOffsetFoundPositions.setPosition(iAlt, (int)offSetFound, tempFragEnd);
                            }
                        }
                    } else {
                        //Assuming we're on the final possible fragment for this position and none so far have matched...
                        if(iAlt == numAltFrags - 1  && numEndPos == 0) {
                            //No match was found for the current fragment.  Check back through any earlier fragment matches
                            //to see if there are any further occurences of a fragment within its offset range, and if so,
                            //revert to that point and resume checking from there.

                            while (fragmentHits != null && !fragmentHits.empty()) {

                                FragmentHit lastGoodFragRef = fragmentHits.pop();
                                //Retrieve the fragment that corresponds to the last successful match.  Create a copy of this fragment which can then be used
                                // to test for a further match based on a new offset defined from the previous position.  We need to use a clone
                                // because this class instance is used to check multiple files and the revised check only applies to this specific file.
                                // alternatively we could copy the original list at the outset but this would potentially create additional objects
                                // on the heap unnecessarily and impact performance.
                                fragment = fragments.get(lastGoodFragRef.getFragmentPosition() - 1).get(lastGoodFragRef.getAlternativeFragmentNumber()).copy();

                                //Adjust the offsets so that we now look for a further occurrence of the fragment to the left
                                //or right of the earlier match.
                                fragment.setMinOffset(Math.max(fragment.getMinOffset() - (int) lastGoodFragRef.getOffsetFound()- fragment.getNumBytes(), 0));
                                fragment.setMaxOffset(fragment.getMaxOffset() - (int) lastGoodFragRef.getOffsetFound() - fragment.getNumBytes());
                                if(fragment.getMaxOffset() < 0) {
                                    break;
                                }

                                //Check for a further occurrence of the fragment beyond the last match position
                                if (searchDirection == 1) {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, lastGoodFragRef.getPositionInFile(),
                                                    rightBytePos, true, searchDirection,
                                                    lastGoodFragRef.getFragmentPosition(), fragment);
                                } else {
                                    tempFragEnd =
                                            this.endBytePosForSeqFrag(bytes, leftBytePos,
                                                    lastGoodFragRef.getPositionInFile(), true, searchDirection,
                                                    lastGoodFragRef.getFragmentPosition(), fragment);
                                }

                                if (tempFragEnd > -1L) {
                                    tempEndPos[numEndPos] = tempFragEnd + searchDirection;
                                    //Add the newly found fragment instance to the top of the stack and reset the loop
                                    // position to resume checking for further fragments from that point.
                                    iFragPos = lastGoodFragRef.getFragmentPosition();
                                    iAlt = lastGoodFragRef.getAlternativeFragmentNumber();
                                    //Get the offset of this new instance of the current fragment from the previous
                                    //fragment, or main sequence if this is the first fragment.
                                    long newOffSetFoundFromPreviousMatch = (lastGoodFragRef.getPositionInFile() - tempFragEnd) + lastGoodFragRef.getOffsetFound() +1;
                                    FragmentHit fragmentHit = new FragmentHit(iFragPos, iAlt, tempEndPos[numEndPos], newOffSetFoundFromPreviousMatch);
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

/*        if (finalOffsetFoundPositions != null) {
            if(fragmentHits !=null  && fragmentHits.size() > 0) {
                FragmentHit[] hitsByFilePosition = new FragmentHit[fragmentHits.size()];
                fragmentHits.toArray(hitsByFilePosition);
                Arrays.sort(hitsByFilePosition);
            }
        }*/

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
    // CHECKSTYLE:ON

    // This method exists to cater for situations where the first (or only)  left fragment in a subsequence
    // occurs more than once to the left of the main byte sequence.  In these cases, the initial check will find the
    // instance closest to the main byte sequence that meets the fragment's offset requirements. However, if this
    // instance is beyond the maximum offset for the sequence as a whole, the whole sequence would previously be set to
    // not match - even though there may be further instances of the leftmost fragment at less than or equal to the
    // maximum sequence offset. So we need to check for any further occurrences to see if there is one that occurs in
    // the byte stream at a positionInFile at or before the allowed maximum offset.
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
            if(fragmentPositions.length  > 0) {
                currentRightBytePos = fragmentPositions[0] - 1;
            }
        } while (fragmentPositions.length > 0 && fragmentPositions[fragmentPositions.length -1] > maxOffset);


        if (fragmentPositions.length == 0 || fragmentPositions[0] > maxOffset || fragmentPositions[0] < minOffset) {
            return true;
        } else {
            return false;
        }
    }


    private boolean checkRightFragmentForInvalidOffset(WindowReader windowReader, final long leftBytePos,
                                                       final long rightBytePos, final long maxOffset,
                                                       final long minOffset, List<List<SideFragment>>  rightFragOpt,
                                                            OffsetAndFilePositions lastOffsetFoundPosition) {
        long[] fragmentPositions;
        long currentLeftBytePos = leftBytePos;


        do {
            fragmentPositions =
                    bytePosForRightFragments(windowReader, currentLeftBytePos,
                            rightBytePos, 1, 0, rightFragOpt, lastOffsetFoundPosition);
            if(fragmentPositions.length  > 0) {
                currentLeftBytePos = fragmentPositions[0] + 1;
            }
        } while (fragmentPositions.length > 0 && fragmentPositions[0] < (rightBytePos - maxOffset));

        if (fragmentPositions.length == 0 ||
                fragmentPositions[0] < (rightBytePos - maxOffset) ||
                fragmentPositions[0] > (rightBytePos - minOffset)) {
            return true;
        } else {
            return false;
        }
    }

    private LeftFragment getRawLeftFragment(final int theIndex) {
        return leftFragments.get(theIndex);
    }

    private RightFragment getRawRightFragment(final int theIndex) {
        return rightFragments.get(theIndex);
    }

    // Helper class to hold history details of fragment matches for a given file.
    private class FragmentHit implements Comparable<FragmentHit> {

        int fragmentIndex;
        int alternativeFragmentNumber;
        long positionInFile;
        long offsetFound;

        public FragmentHit(int fragmentIndex, int alternativeFragmentNumber, long positionInFile, long offsetFound) {
            this.fragmentIndex = fragmentIndex;
            this.alternativeFragmentNumber = alternativeFragmentNumber;
            this.positionInFile = positionInFile;
            this.offsetFound = offsetFound;
        }

        public int getAlternativeFragmentNumber() {
            return alternativeFragmentNumber;
        }


        public int getFragmentPosition() {
            return fragmentIndex;
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

            if(this.getPositionInFile() > other.getPositionInFile()) {
                result = 1;
            } else if (this.getPositionInFile() < other.getPositionInFile()) {
                result = -1;
            } else {
                result = 0;
            }
            return result;
        }
    }

 /*   private class OffsetAndFilePositions {

        private static final long NO_OFFSET_POSITION_FOUND = -1;
        private long[][] offsetPositions;


        public OffsetAndFilePositions() {}

        public OffsetAndFilePositions(List<List<SideFragment>> fragments) {
            this.offsetPositions = new long[fragments.size()][];
            for (int i = 0; i < fragments.size(); i++) {
                offsetPositions[i] = new long[fragments.get(i).size()];
                for(int j = 0; j < offsetPositions[i].length; j++) {
                    offsetPositions[i][j] = NO_OFFSET_POSITION_FOUND;
                }
            }
        }

        public void setPosition(int fragPos, int altPos, long value) {
            this.offsetPositions[fragPos][altPos] = value;
        }

        public long getPosition(int fragPos, int altPos) {
            return this.offsetPositions[fragPos][altPos];
        }

        public long getFirstPositionInFile ()  {
            if (offsetPositions == null) {
                return -1;
            }

            long temp = Long.MAX_VALUE;
            for(int i = 0;i<offsetPositions.length; i++) {
                for(int j = 0;j<offsetPositions[i].length; j++) {
                    if(offsetPositions[i][j] < temp) {
                        temp = offsetPositions[i][j];
                    }
                }
            }
            return temp;

        }

        public OffsetAndFilePositions getLast() {

            long[] last = offsetPositions[offsetPositions.length -1];
            offsetPositions = new long[1][];
            offsetPositions[0] = last;
            return this;
        }
    }*/

    private class OffsetAndFilePositions {

        private static final int NO_OFFSET_POSITION_FOUND = -1;
        private int[] offsetPositions;
        private long[] filePositions;


        public OffsetAndFilePositions() {}

        public OffsetAndFilePositions(List<SideFragment> fragments) {
            this.offsetPositions = new int[fragments.size()];
            this.filePositions = new long[fragments.size()];
            for (int i = 0; i < fragments.size(); i++) {
                offsetPositions[i] = NO_OFFSET_POSITION_FOUND;
                filePositions[i] = NO_OFFSET_POSITION_FOUND;
            }
        }

        public void setPosition(int altPos, int offsetPos, long filePos) {
            this.offsetPositions[altPos] = offsetPos;
            this.filePositions[altPos] = filePos;
        }

        public long getOffsetPosition(int altPos) {
            return this.offsetPositions[altPos];
        }

        public long getFilePosition(int altPos) {
            return this.filePositions[altPos];
        }

        public long getFirstPositionInFile ()  {
            if (offsetPositions == null) {
                return -1;
            }

            long temp = NO_OFFSET_POSITION_FOUND;
            for(int i = 0; i< offsetPositions.length; i++) {
                if(offsetPositions[i] > temp) {
                    temp = offsetPositions[i];
                }
            }
            return temp;
        }
    }
}
