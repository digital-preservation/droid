/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
 *  - fix issue where starting sequence should match at a known position (related to more
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
 * position options are dealt with.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.domesdaybook.expression.compiler.sequence.SequenceMatcherCompiler;
import net.domesdaybook.expression.parser.ParseException;
import net.domesdaybook.matcher.sequence.SequenceMatcher;
import net.domesdaybook.matcher.sequence.searcher.BoyerMooreHorspoolSearcher;
import net.domesdaybook.matcher.sequence.searcher.SequenceMatcherSearcher;
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
 */
public class SubSequence extends SimpleElement {

    private static final String SEQUENCE_PARSE_ERROR = "The signature sub-sequence [%s] could not be parsed. "
        + "The error returned was [%s]"; 
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
    private SequenceMatcherSearcher searcher;

    private final List<List<SideFragment>> orderedLeftFragments = new ArrayList<List<SideFragment>>();
    private final List<List<SideFragment>> orderedRightFragments = new ArrayList<List<SideFragment>>();
    private boolean backwardsSearch;
    private boolean isInvalidSubSequence;
    
    private LeftFragment getRawLeftFragment(final int theIndex) {
        return leftFragments.get(theIndex);
    }

    private RightFragment getRawRightFragment(final int theIndex) {
        return rightFragments.get(theIndex);
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
        try {
            final String transformedSequence = FragmentRewriter.rewriteFragment(seq);
            SequenceMatcherCompiler compiler = new SequenceMatcherCompiler();
            matcher = compiler.compile(transformedSequence);
            searcher = new BoyerMooreHorspoolSearcher(matcher);
        } catch (ParseException ex) {
            final String warning = String.format(SEQUENCE_PARSE_ERROR, seq, ex.getMessage());
            getLog().warn(warning);
            //throw new IllegalArgumentException(seq, ex);
            isInvalidSubSequence = true;
        }
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
     * @deprecated min frag length not used anymore
     * @param theLength not used.
     */
    @Deprecated
    public void setMinFragLength(int theLength) {
    }    

    /**
     * Note: unclear whether this is used anymore.
     * 
     * @param theLength The minimum length of a fragment.
     */
    /*
    public final void setMinFragLength(final int theLength) {
        this.minFragLength = theLength;
    }
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
     * @return the number of fragment positions for either the left or right fragments.
     */
    public final int getNumFragmentPositions(final boolean leftFrag) {
        return leftFrag ? this.numLeftFragmentPositions
                        : this.numRightFragmentPositions;
    }

    /**
     * 
     * @param leftFrag true to return information about the left fragments, false to return
     * information about the right fragments.
     * @param thePosition The fragment position to retrieve number of fragments for.
     * @return the number of alternative fragments for the given left or right position.
     */
    public final int getNumAlternativeFragments(final boolean leftFrag, final int thePosition) {
        return leftFrag ? this.orderedLeftFragments.get(thePosition - 1).size()
                        : this.orderedRightFragments.get(thePosition - 1).size();
    }

    /**
     * 
     * @param leftFrag true to return information about the left fragments, false to return
     * information about the right fragments.     * 
     * @param thePosition The fragment position to retrieve number of fragments for.
     * @param alternateIndex The index of the fragment alternative at the given left or right position.
     * @return The fragment alternative at the given left or right position.
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


    /*
     * Re-orders the left and right sequence fragments in increasing position order.
     * Also calculates the minimum and maximum lengths a fragment can have.
     */
    //CHECKSTYLE:OFF - this method is far too long.
    private void processSequenceFragments() {
    //CHECKSTYLE:ON
        /* Left fragments */
        //Determine the number of fragment subsequences there are
        int numPositions = 0;
        for (int i = 0; i < leftFragments.size(); i++) {
            final int currentPosition = this.getRawLeftFragment(i).getPosition();
            if (currentPosition > numPositions) {
                numPositions = currentPosition;
            }
        }

        //initialise all necessary fragment lists (one for each position)
        for (int i = 0; i < numPositions; i++) { //loop through fragment positions
            final List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
            orderedLeftFragments.add(alternativeFragments);
        }

        //Add fragments to new structure
        for (int i = 0; i < leftFragments.size(); i++) {  //loop through all fragments
            final SideFragment fragment = this.getRawLeftFragment(i);
            final int currentPosition = fragment.getPosition();
            orderedLeftFragments.get(currentPosition - 1).add(fragment);
        }

        // Optimise alternative sequences of single bytes into a byte-class,
        // instead of being a set of alternatives.
        for (int fragPos = 0; fragPos < orderedLeftFragments.size(); fragPos++) { // loop through all positions:
            final List<SideFragment> fragmentsToMatch = orderedLeftFragments.get(fragPos);
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
                    orderedLeftFragments.set(fragPos, newList);
                }
            }
        }

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

        this.numLeftFragmentPositions = orderedLeftFragments.size();

        //clear out unnecessary info
        this.leftFragments = null;

        /* Right fragments */
        //Determine the number of fragment subsequences there are
        numPositions = 0;
        for (int i = 0; i < rightFragments.size(); i++) {
            final int currentPosition = this.getRawRightFragment(i).getPosition();
            if (currentPosition > numPositions) {
                numPositions = currentPosition;
            }
        }

        //initialise all necessary fragment lists (one for each position)
        for (int i = 0; i < numPositions; i++) { //loop through fragment positions
            final List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
            orderedRightFragments.add(alternativeFragments);
        }

        //Add fragments to new structure
        for (int i = 0; i < rightFragments.size(); i++) {  //loop through all fragments
            final SideFragment fragment = this.getRawRightFragment(i);
            final int currentPosition = fragment.getPosition();
            orderedRightFragments.get(currentPosition - 1).add(fragment);
        }

        // Optimise alternative sequences of single bytes into a byte-class,
        // instead of being a set of alternatives.
        for (int fragPos = 0; fragPos < orderedRightFragments.size(); fragPos++) { // loop through all positions:
            final List<SideFragment> fragmentsToMatch = orderedRightFragments.get(fragPos);
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
                    SideFragment newFrag = new RightFragment();
                    newFrag.setPosition(frag.getPosition());
                    newFrag.setMinOffset(frag.getMinOffset());
                    newFrag.setMaxOffset(frag.getMaxOffset());
                    expression.append(']');
                    newFrag.setFragment(expression.toString());
                    List<SideFragment> newList = new ArrayList<SideFragment>();
                    newList.add(newFrag);
                    orderedRightFragments.set(fragPos, newList);
                }
            }
        }

        // Calculate minimum size of right fragments:
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

        this.numRightFragmentPositions = orderedRightFragments.size();
        //clear out unnecessary info
        this.rightFragments = null;
        
        isInvalidSubSequence = isInvalidSubSequence ? true : checkForInvalidFragments();
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
     * @param position The position to begin searching from.
     * @param targetFile The file to search in.
     * @param maxBytesToScan The maximum amount of bytes to read from
     * the beginning or end of the file.  If negative, scanning is unlimited.
     * @param bofSubsequence Indicates when subsequence is anchored to BOF
     * @param eofSubsequence Indicates when subsequence is anchored to EOF
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

            // Define the length of the file and the pattern, minus one to get an offset from a zero index position.
            final long lastBytePositionInFile = targetFile.getNumBytes() - 1;
            
            //final int lastBytePositionInAnchor = sequence.length -1;
            final int matchLength = matcher.length();
            final int lastBytePositionInAnchor = matchLength - 1;

            // Define the smallest and greatest possible byte position in the file we could match at:
            // the first possible byte position is the start of the file plus the minimum amount of 
            // left fragments to check before this sequence.
            final long firstPossibleBytePosition = minLeftFragmentLength; 
            // the last possible byte position is the end of the file, minus the minimum 
            // right fragments to check after this sequence.
            final long lastPossibleBytePosition = lastBytePositionInFile - minRightFragmentLength; 

            // Provide two implementations of the same algorithm -
            // one for forward searching, the other for backwards searching.
            // Although the differences between them are very small, DROID spends the majority of its time here,
            // so even small performance improvements add up quickly.

            final net.domesdaybook.reader.ByteReader reader = targetFile.getReader();

            if (backwardsSearch) {
                
                // Define the search window relative to our starting position:
                final long maximumPossibleStartingPosition =
                    position - minRightFragmentLength - lastBytePositionInAnchor;
                final long startSearchWindow = maximumPossibleStartingPosition - this.getMinSeqOffset();
                final int rightFragmentWindow = maxRightFragmentLength - minRightFragmentLength;
                long endSearchWindow = fullFileScan 
                    ? 0 
                    : maximumPossibleStartingPosition - this.getMaxSeqOffset() - rightFragmentWindow;

                // Limit the maximum bytes to scan.
                if (maxBytesToScan > 0 && endSearchWindow < lastBytePositionInFile - maxBytesToScan) {
                    endSearchWindow  = lastBytePositionInFile - maxBytesToScan;
                }

                // If we're starting outside a possible match position, 
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
                    matchPosition = searcher.searchBackwards(reader, matchPosition, endSearchWindow);
                    if (matchPosition != -1) {
                        boolean matchFound = true;
                        // Check that any right fragments, behind our sequence, match.
                        if (hasRightFragments) { 
                            final long[] rightFragmentPositions = 
                                bytePosForRightFragments(reader, matchPosition + matchLength, 
                                    targetFile.getFileMarker(), 1, 0);
                            matchFound = rightFragmentPositions.length > 0;
                        }
                        if (matchFound) {
                            // Check that any left fragments, before our sequence, match.
                            if (hasLeftFragments) { 
                                final long[] leftFragmentPositions =
                                    bytePosForLeftFragments(reader, 0, matchPosition - 1, -1, 0);
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
                // Define the search window relative to our starting position:
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

                // If we're starting outside a possible match position, 
                // don't continue:
                if (startSearchWindow < firstPossibleBytePosition) {
                    return false;
                }

                // Ensure the end position doesn't run over the end of the file,
                // if it's shorter than the sequence we're trying to check.
                if (endSearchWindow > lastPossibleBytePosition) {
                    endSearchWindow = lastPossibleBytePosition;
                }

                long matchPosition = startSearchWindow;
                while (matchPosition <= endSearchWindow) {
                    matchPosition = searcher.searchForwards(reader, matchPosition, endSearchWindow);
                    if (matchPosition != -1) {
                        boolean matchFound = true;
                        if (hasLeftFragments) { // Check that any left fragments, behind our sequence match:
                            final long[] leftFragmentPositions = 
                                bytePosForLeftFragments(reader, targetFile.getFileMarker(),
                                    matchPosition - matchLength, -1, 0);
                            matchFound = leftFragmentPositions.length > 0;
                            
//                            // check BOF max seq offset (bugfix)
                            if (matchFound
                                    && bofSubsequence
                                    && leftFragmentPositions[0] > this.maxSeqOffset) {
                                matchFound = false;
                            }
                        }
                        if (matchFound) {
                            if (hasRightFragments) { // Check that any right fragments after our sequence match:
                                final long[] rightFragmentPositions = 
                                    bytePosForRightFragments(reader, matchPosition + 1, lastBytePositionInFile, 1, 0);
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
        }
        //CHECKSTYLE:ON
        return entireSequenceFound;
    }


    /**
     * Searches for the left fragments of this subsequence between the given byte
     * positions in the file.  Either returns the last byte taken up by the
     * identified sequences or returns -2 if no match was found
     *
     * @param targetFile      the binary file to be identified
     * @param leftBytePos     left-most byte position of allowed search window on file
     * @param rightBytePos    right-most byte position of allowed search window on file
     * @param searchDirection 1 for a left to right search, -1 for right to left
     * @param offsetRange     range of possible start positions in the direction of searchDirection
     * @return A long array containing all possible matching positions for the left fragments.
     */
    //CHECKSTYLE:OFF - way, way, way too complex.
    private long[] bytePosForLeftFragments(final net.domesdaybook.reader.ByteReader bytes, final long leftBytePos, final long rightBytePos,
            final int searchDirection, final int offsetRange) {
    //CHECKSTYLE:ON
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
            //array to store possible end positions after this fragment position has been examined
            long[] tempEndPos = new long[numAltFrags * numOptions]; 

            int numEndPos = 0;
            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end positions
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
                    //eliminate any repeated end positions
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
            // no possible positions found, return 0 length array
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

    /**
     * Searches for the right fragments of this subsequence between the given byte
     * positions in the file.  Either returns the last byte taken up by the
     * identified sequences or returns -2 if no match was found
     *
     * @param bytes           the binary file to be identified
     * @param leftBytePos     left-most byte position of allowed search window on file
     * @param rightBytePos    right-most byte position of allowed search window on file
     * @param searchDirection 1 for a left to right search, -1 for right to left
     * @param offsetRange     range of possible start positions in the direction of searchDirection
     * @return
     */
    //CHECKSTYLE:OFF - way, way, way too complex.
    private long[] bytePosForRightFragments(final net.domesdaybook.reader.ByteReader bytes, final long leftBytePos, final long rightBytePos,
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
            //array to store possible end positions after this fragment position has been examined
            long[] tempEndPos = new long[numAltFrags * numOptions]; 
            int numEndPos = 0;


            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end positions
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
                    //eliminate any repeated end positions
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
            // no possible positions found, return 0 length array
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
     * between the leftmost and rightmost byte positions that are given.
     * returns the end position of the found sequence or -1 if it is not found
     *
     * @param targetFile      The file that is being reviewed for identification
     * @param leftEndBytePos  leftmost position in file at which to search
     * @param rightEndBytePos rightmost postion in file at which to search-
     * @param leftFrag        flag to indicate whether looking at left or right fragments
     * @param searchDirection direction in which search is carried out (1 for left to right, -1 for right to left)
     * @param fragPos         position of left/right sequence fragment to use
     * @param fragIndex       index of fragment within the position (where alternatives exist)
     * @return
     */
    //CHECKSTYLE:OFF too long and complex.
    private long endBytePosForSeqFrag(final net.domesdaybook.reader.ByteReader bytes, 
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

        // set up start and end positions for searches taking into account min and max offsets
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
        //compare sequence with file contents directly at fileMarker position
        //boolean subSeqFound = false;
        //while ((!subSeqFound) && ((searchDirectionL) * (lastStartPosInFile - startPosInFile) >= 0L)) {
        while (searchDirectionL * (lastStartPosInFile - startPosInFile) >= 0L) {
            if (fragment.matchesBytes(bytes, startPosInFile - byteOffset)) {
                endPosInFile = startPosInFile + (numBytes * searchDirectionL) - searchDirectionL;
                break;
            }
            startPosInFile += searchDirectionL;
        }
        return endPosInFile;  //this is -1 unless subSeqFound = true
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

        // If we have more than one fragment at a position, it's a list of alternatives:
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
}
