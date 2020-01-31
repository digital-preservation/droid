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
 * Matt Palmer, The National Archives 2009-2010.
 * Bug fixes and performance optimisations.
 *
 * Tessella Support Services plc
 * 3 Vineyard Chambers
 * Abingdon, OX14 3PX
 * United Kingdom
 * http://www.tessella.com
 *
 * Tessella/NPD/4305
 * PRONOM 4
 *
 * $Id: sideFragment.java,v 1.6 2006/03/13 15:15:29 linb Exp $
 *
 * $Log: sideFragment.java,v $
 * Revision 1.6  2006/03/13 15:15:29  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.5  2006/02/09 15:04:37  gaur
 * Corrected formatting
 *
 * Revision 1.4  2006/02/07 11:30:04  gaur
 * Added support for endianness of signature
 *
 * Revision 1.3  2006/02/03 16:54:42  gaur
 * We now allow general wildcards of numseqsarbitrary endianness: e.g., [!~A1B1:C1D1]
 *
 * Revision 1.2  2006/02/02 17:15:47  gaur
 * Started migration to being able to handle byte specifier wildcards.
 * This version should have the same functionality as the old one (but making use of the new ByteSeqSpecifier class).
 *
 *
 * $History: sideFragment.java $
 *
 * *****************  Version 4  *****************
 * User: Walm         Date: 17/05/05   Time: 12:48
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * wait for end of element tag before setting its content via the
 * completeElementContent method
 *
 * *****************  Version 3  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid6;

import java.io.IOException;
import java.util.List;

import net.byteseek.compiler.CompileException;
import net.byteseek.compiler.matcher.SequenceMatcherCompiler;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.matcher.MatchResult;
import net.byteseek.matcher.sequence.SequenceMatcher;
import net.byteseek.searcher.Searcher;
import net.byteseek.searcher.bytes.ByteMatcherSearcher;
import net.byteseek.searcher.sequence.horspool.HorspoolFinalFlagSearcher;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;


/**
 * A SideFragment is any fragment of a subsequence which
 * cannot be searched for using the BoyerHooreHorspool 
 * algorithm.  Typically, this means parts of the subsequence
 * with gaps {n-m}, and alternatives (A|B|C).
 * 
 * <p>A subsequence is defined by the longest anchoring sequence
 * which can be searched for, with any side fragments to the left
 * and right of it checked for after the anchoring sequence is 
 * found. </p> 
 *
 * @author Martin Waller
 * @author Matt Palmer
 * @version 6.0.0
 * @author Brian O Reilly
 * @version 6.2.2
 *
 */
public class SideFragment extends SimpleElement implements Cloneable {
    
    private static final String FRAGMENT_PARSE_ERROR = "The signature fragment [%s] could not be parsed. "
        + "The error returned was [%s]";

    private static final SequenceMatcherCompiler EXPRESSION_COMPILER = new SequenceMatcherCompiler();

    private int myPosition;
    private int myMinOffset;
    private int myMaxOffset;
    private SequenceMatcher matcher;
    private Searcher searcher;
    private boolean isInvalidFragment;

    /**
     * default constructor.
     */
    public SideFragment() {
    }

    /**
     * @param matcher THe SequenceMatcher which the SideFragment will match.
     * @param minOffset The minimum offset to begin looking for this fragment
     * @param maxOffset The maximum offset to begin looking for this fragment
     * @param position the position in file of this fragment
     */
    public SideFragment(SequenceMatcher matcher, int minOffset, int maxOffset, int position) {
        this.matcher = matcher;
        this.myMinOffset = minOffset;
        this.myMaxOffset = maxOffset;
        this.myPosition  = position;
        buildSearcher();
    }

    /**
     * @param toCopy sideFragment to copy
     */
    public SideFragment(final SideFragment toCopy) {
        this.matcher = toCopy.matcher;
        this.myMinOffset = toCopy.myMinOffset;
        this.myMaxOffset = toCopy.myMaxOffset;
        this.myPosition = toCopy.myPosition;
        buildSearcher();
    }

    /* setters */
    /**
     * @param thePosition the positionInFile of the fragment in the
     * list of SideFragments held to the left or right of a
     * subsequence.  
     * 
     * Individual fragments can have the same positionInFile as each other -
     * this is how alternatives are represented -
     * as different fragments with the same positionInFile.
     */
    public final void setPosition(final int thePosition) {
        this.myPosition = thePosition;
    }

    /**
     * A minimum offset is the amount of bytes to skip before
     * looking for this fragment.
     *   
     * @param theMinOffset The minimum offset to begin looking for this fragment.
     */
    public final void setMinOffset(final int theMinOffset) {
        this.myMinOffset = theMinOffset;
        // ensure the maximum is never less than then minimum.
        if (this.myMaxOffset < this.myMinOffset) {
            this.myMaxOffset = theMinOffset;
        }
    }

    /**
     * A maximum offset is the largest amount of bytes to look
     * in for this fragment.  If the maximum offset is greater
     * than the minimum offset, then a range of bytes will be
     * searched for this fragment.
     * 
     * @param theMaxOffset The maximum offset to begin lookiing for this fragment.
     */
    public final void setMaxOffset(final int theMaxOffset) {
        this.myMaxOffset = theMaxOffset;
        // ensure the minimum is never greater than the maximum.
        if (this.myMinOffset > this.myMaxOffset) {
            this.myMinOffset = theMaxOffset;
        }
    }

    /**
     * 
     * @param expression The regular expression defining the fragment.
     */
    public final void setFragment(final String expression) {
        try {
            final String transformed = FragmentRewriter.rewriteFragment(expression);
            matcher = EXPRESSION_COMPILER.compile(transformed);
            buildSearcher();
        } catch (CompileException ex) {
            final String warning = String.format(FRAGMENT_PARSE_ERROR, expression, ex.getMessage());
            isInvalidFragment = true;
            getLog().warn(warning);            
        }
    }

    private void buildSearcher() {
        if (matcher.length() == 1) {
            searcher = new ByteMatcherSearcher(matcher.getMatcherForPosition(0));
        } else {
            searcher = new HorspoolFinalFlagSearcher(matcher);
        }
    }

    /**
     * Returns the SequenceMatcher object which matches the fragment.
     *
     * @return the SequenceMatcher object which matches the fragment.
     */
    public final SequenceMatcher getMatcher() {
        return matcher; // SequenceMatchers are immutable, so can just return it directly without fear.
    }
    
    /**
     * 
     * @return Whether the fragment managed to be assembled correctly.
     */
    public boolean isInvalidFragment() {
        return isInvalidFragment;
    }

    @Override
    public final void setAttributeValue(final String name, final String value) {
        if ("Position".equals(name)) {
            setPosition(Integer.parseInt(value));
        } else if ("MinOffset".equals(name)) {
            setMinOffset(Integer.parseInt(value));
        } else if ("MaxOffset".equals(name)) {
            setMaxOffset(Integer.parseInt(value));
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    /**
     * 
     * @return the positionInFile of this fragment.
     */
    public final int getPosition() {
        return myPosition;
    }

    /**
     * A minimum offset is the amount of bytes to skip before
     * looking for this fragment.
     * 
     * @return The minimum offset to begin looking for this fragment.
     */
    public final int getMinOffset() {
        return myMinOffset;
    }

    /**
     * A maximum offset is the largest amount of bytes to look
     * in for this fragment.  If the maximum offset is greater
     * than the minimum offset, then a range of bytes will be
     * searched for this fragment.
     * 
     * @return The maximum offset to look for this fragment.
     */
    public final int getMaxOffset() {
        return myMaxOffset;
    }

    /**
     * 
     * @return The number of bytes matched by this fragment.
     */
    public final int getNumBytes() {
        return matcher == null ? 0 : matcher.length();
    }

    /**
     * Set the sideFragment sequence.
     * This will have been stored in the text attribute by the setText method.
     * Then transforms the input string into a list of matching objects.
     */
    @Override
    public final void completeElementContent() {
        setFragment(this.getText());
    }

    
    
    /**
     * Matches the fragment against the positionInFile in the ByteReader given.
     * 
     * @param bytes The byte reader to match the bytes with.
     * @param matchFrom The positionInFile to match from.
     * @return Whether the fragment matches at the positionInFile given.
     * @throws IOException If a problem occurs reading the underlying file or stream
     */
    public final boolean matchesBytes(final WindowReader bytes, final long matchFrom) throws IOException {
        return matcher.matches(bytes, matchFrom);
    }

    /**
     * Finds the fragment looking forwards from 'from' up to 'to'.
     *
     * @param bytes a Byteseek WindowReader object
     * @param from the positionInFile within bytes from which to start searching
     * @param to the positionInFile within bytes at which to end searching
     * @return A list of match results
     * @throws IOException If a problem occurs reading the underlying file or stream
     */
    public final List<MatchResult> findFragmentForwards(final WindowReader bytes,
                                                        final long from, final long to) throws IOException {
        return searcher.searchForwards(bytes, from, to);
    }

    /**
     * Finds the fragment looking backwards from 'from' back to 'to'.
     *
     * @param bytes a Byteseek WindowReader object
     * @param from the positionInFile within bytes from which to start searching
     * @param to the positionInFile within bytes at which to end searching
     * @return A list of match results
     * @throws IOException If a problem occurs reading the underlying file or stream
     */
    public final List<MatchResult> findBackwards(final WindowReader bytes,
                                                 final long from, final long to) throws IOException {
        return searcher.searchBackwards(bytes, from, to);
    }


    /**
     * Returns a regular expression representation of this fragment.
     * 
     * @param prettyPrint whether to pretty print the regular expression.
     * @return a regular expression defining this fragment,
     * but minus any offsets defined here (handled by the parent subsequence).
     */
    public final String toRegularExpression(final boolean prettyPrint) {
        return matcher == null ? "" : matcher.toRegularExpression(prettyPrint);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + toRegularExpression(true) + ']';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Creates a clone of the side fragment on whihc it is invoked.
     * @return A deep clone of the target SideFragment
     */
    public SideFragment copy() {
        SideFragment copy = null;
        try {
            copy =  (SideFragment) this.clone();
        }  catch (CloneNotSupportedException e) {
            System.out.println("Error cloning fragment " + this.toString() + ":" + e.getMessage());
        }
        return  copy;
    }
}
