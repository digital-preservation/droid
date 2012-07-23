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
 * The National Archives 2005-2006.  All rights reserved.
 * See Licence.txt for full licence details.
 *
 * Developed by:
 * Tessella Support Services plc
 * 3 Vineyard Chambers
 * Abingdon, OX14 3PX
 * United Kingdom
 * http://www.tessella.com
 *
 * Tessella/NPD/4305
 * PRONOM 4
 *
 * $Id: InternalSignature.java,v 1.5 2006/03/13 15:15:29 linb Exp $
 *
 * $Log: InternalSignature.java,v $
 * Revision 1.5  2006/03/13 15:15:29  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.4  2006/02/08 16:06:35  gaur
 * Moved endianness from internal signatures to byte sequences
 *
 * Revision 1.3  2006/02/07 17:16:22  linb
 * - Change fileReader to IdentificationResults in formal parameters of methods
 * - use new static constructors
 * - Add detection of if a filePath is a URL or not
 *
 * Revision 1.2  2006/02/07 11:30:04  gaur
 * Added support for endianness of signature
 *
 *
 * $History: InternalSignature.java $
 * 
 * *****************  Version 3  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature.droid6;

import java.io.IOException;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * A binary signature which can match one or more file formats.
 * 
 * <p/>It is composed of a list of {@link ByteSequence} objects,
 * which are like regular expressions that can match bytes.
 * 
 * <p/>A signature runs each ByteSequence against a target file, 
 * until one of them doesn't match, or all of them do. 
 * 
 * <p/>It orders the ByteSequence objects to try to ensure 
 * optimal matching behaviour - e.g. the ones anchored to the 
 * start of the file are done before ones at the end of the file.
 *
 * @author Martin Waller
 * @author Matt Palmer
 * @version 6.0.0
 */
public class InternalSignature extends SimpleElement {

    /**
     * 00000001
     */
    private static final int BIT1 = 1;
    /**
     * 00000010
     */
    private static final int BIT2 = 2;
    /**
     * 00000100
     */
    private static final int BIT3 = 4;
    /**
     * 00001000
     */
    private static final int BIT4 = 8;
    /**
     * 00010000
     */
    private static final int BIT5 = 16;

    /**
     * A space.
     */
    private static final String SPACE = " ";
    
    private List<ByteSequence> byteSequences = new ArrayList<ByteSequence>();
    private int intSigID;
    private boolean specificity;
    private final List<FileFormat> fileFormatList = new ArrayList<FileFormat>();
    private int sortOrder;
    private boolean isInvalidSignature;
    
    /* setters */
    /**
     * @param byteSequence A byte sequence to add to the internal signature.
     *
     */
    public final void addByteSequence(final ByteSequence byteSequence) {
        byteSequences.add(byteSequence);
    }

    /**
     * 
     * @return The sort order for this byte sequence which defines
     * the most performant order to apply it in relative to other byte sequences
     * defined in this signature.
     */
    public final int getSortOrder() {
        return sortOrder;
    }
    
   /**
    * Does anything that needs to be done before this signature is used.
    */
    public final void prepareForUse() {
        isInvalidSignature = prepareByteSequences();
    }
    
    /**
     * 
     * @return Whether the signature is valid or not.
     */
    public boolean isInvalidSignature() {
        return isInvalidSignature;
    }
    

    /**
     * Returns a string of all the puids this signature can match.
     * 
     * @return All the puids which this signature can match.
     */
    private String getFileFormatPUIDs() {
        StringBuffer formats = new StringBuffer();
        final int endOfFileFormats = fileFormatList.size();
        for (int fileFormatIndex = 0;
              fileFormatIndex < endOfFileFormats; fileFormatIndex++) {
            final FileFormat format = fileFormatList.get(fileFormatIndex);
            if (fileFormatIndex > 0) {
                formats.append(SPACE);
            }
            formats.append(format.getPUID());
        }
        return formats.toString();
    }


    private String getFileFormatNames() {
        StringBuffer formats = new StringBuffer();
        final int endOfFileFormats = fileFormatList.size();
        for (int fileFormatIndex = 0;
              fileFormatIndex < endOfFileFormats; fileFormatIndex++) {
            final FileFormat format = fileFormatList.get(fileFormatIndex);
            if (fileFormatIndex > 0) {
                formats.append(SPACE);
            }
            formats.append(format.getName());
        }
        return formats.toString();
    }

    
    private boolean prepareByteSequences() {
        final int endIndex = byteSequences.size();
        boolean hasOnlyVarSequences = true;
        for (int byteSequenceIndex = 0; byteSequenceIndex < endIndex; byteSequenceIndex++) {
            ByteSequence byteSequence = byteSequences.get(byteSequenceIndex); 
            byteSequence.prepareForUse();
            if (byteSequence.isInvalidByteSequence()) {
                return true;
            }
            if (byteSequence.isAnchoredToBOF() || byteSequence.isAnchoredToEOF()) {
                hasOnlyVarSequences = false;
            }
        }
        if (hasOnlyVarSequences) {
            getLog().warn(getPerformanceWarningMessage());
        }
        // must call reorderByteSequences after first preparing byte sequences for use
        // as it relies on their sort orders defined when preparing them for use.
        reorderByteSequences();
        calculateSignatureSortOrder();
        return false;
    }
    
    private String getPerformanceWarningMessage() {
        return String.format("Signature [id:%d] will always scan up to maximum bytes.  "
                + "Matches formats: %s", intSigID, getFileFormatDescriptions());
    }
    
    /**
     * 
     * @return a string describing the file formats matched by this signature.
     */
    public String getFileFormatDescriptions() {
        StringBuilder builder = new StringBuilder();
        for (FileFormat format : fileFormatList) {
            String formatInfo = String.format(" [Name:%s] [PUID:%s]  ", format.getName(), format.getPUID());
            builder.append(formatInfo);
        }
        return builder.toString();
    }

    /*
     * Reset the bytesequences after reordering (to ensure BOF and EOF sequences are checked first
     *
     * @param byteSequences sequence
     */
    private void reorderByteSequences() {
        Collections.sort(byteSequences, new ByteSequenceComparator());
    }

    private void calculateSignatureSortOrder() {
         /* Assign a sort order on the basis of what kinds of byte sequences appear in the signature:
         *  This is currently defined as follows, where:
         *
         *  1      B  = BOF sequence
         *  2      B* = BOF sequence followed by * subsequence
         *  4      V  = Variable sequence potential full file scan from beginning = *B
         *  8      E  = EOF sequence
         *  16     E* = EOF sequence followed by * subsequence
         * 
         * We assign sort order by treating these as bits to group the different sequences together.
         * and to order them in a way that maximises cache hits on the file reads.
         */
        int sortBits = 0;
        final List<ByteSequence> sequences = byteSequences;
        final int noOfSequences = sequences.size();
        for (int byteSequenceIndex = 0; byteSequenceIndex < noOfSequences; byteSequenceIndex++) {
            final ByteSequence seq = sequences.get(byteSequenceIndex);
            if (seq.isAnchoredToBOF()) {
                if (seq.getNumberOfSubSequences() == 1) {
                    sortBits = sortBits | BIT1;  // B sequence
                } else {
                    sortBits = sortBits | BIT2;  // B* sequence
                }
            } else if (seq.isAnchoredToEOF()) {
                if (seq.getNumberOfSubSequences() == 1) {
                    sortBits = sortBits | BIT4; // E sequence
                } else {
                    sortBits = sortBits | BIT5; // E* sequence
                }
            } else {
                sortBits = sortBits | BIT3;    // V sequence
            }
        }
        this.sortOrder = sortBits;
    }

    /**
     * 
     * @param theFileFormat A file format which this signature matches.
     */
    public final void addFileFormat(final FileFormat theFileFormat) {
        fileFormatList.add(theFileFormat);
    }

    /**
     * Removes the file format from the internal signature.
     * 
     * @param format The file format to remove.
     */
    public final void removeFileFormat(final FileFormat format) {
        fileFormatList.remove(format);
    }    
    
    /**
     * 
     * @param theIntSigID The ID of this signature.
     */
    public final void setID(final String theIntSigID) {
        this.intSigID = Integer.parseInt(theIntSigID);
    }

    /**
     * @deprecated Specificity is not used in DROID 5 and above.
     * @param specificity The specificity of this signature.
     */
    @Deprecated
    public final void setSpecificity(final String specificity) {
        this.specificity = "specific".equalsIgnoreCase(specificity);
    }

    @Override
    public final void setAttributeValue(final String name, final String value) {
        if ("ID".equals(name)) {
            setID(value);
        } else if ("Specificity".equals(name)) {
            setSpecificity(value);
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    
    /**
     * @return The byte sequences comprising this signature.
     */
    public final List<ByteSequence> getByteSequences() {
        return byteSequences;
    }

    /**
     * 
     * @return The number of file formats this signature matches.
     */
    public final int getNumFileFormats() {
        return fileFormatList.size();
    }

    /**
     * 
     * @param theIndex The index of the file format to get.
     * @return A file format this signature matches
     */
    public final FileFormat getFileFormat(final int theIndex) {
        return fileFormatList.get(theIndex);
    }

    /**
     * 
     * @return The id of this signature.
     */
    public final int getID() {
        return intSigID;
    }

    /**
     * @deprecated Specificity is not used in DROID 5 and above.
     * @return Whether this signature is specific or generic.
     */
    @Deprecated
    public final boolean isSpecific() {
        return specificity;
    }


    /**
     * Indicates whether the internal signature matches the target file.
     *
     * @param targetFile the binary file to be identified
     * @param maxBytesToScan how many bytes should be scanned from the
     * beginning or end of each file.  If negative, scanning is unlimited.
     * @return Whether the signature matches the target file or not.
     */
    public final boolean matches(final ByteReader targetFile, final long maxBytesToScan) {
        boolean matchResult = true;
        final List<ByteSequence> sequences = byteSequences;
        final int numseqs = sequences.size();
        //check each byte sequence in turn - stop as soon as one is found to be non-compliant
        for (int sequenceIndex = 0; matchResult && sequenceIndex < numseqs; sequenceIndex++) {
            matchResult = sequences.get(sequenceIndex).matches(targetFile, maxBytesToScan);
        }
        return matchResult;
    }

    @Override
    public final String toString() {
        return intSigID + "(" + specificity + ")" + byteSequences;
    }

    
    /**
     *
     * @return A list of regular expressions for each byte sequence in this signature.
     */
    public List<String> toRegularExpressions() {
        final List<String> regularExpressions = new ArrayList<String>();
        final List<ByteSequence> sequences = byteSequences;
        final int numseqs = sequences.size();
        for (int sequenceIndex = 0; sequenceIndex < numseqs; sequenceIndex++) {
            final ByteSequence sequence = sequences.get(sequenceIndex);
            final String reference = sequence.getReference();
            final String expression = sequence.toRegularExpression(false);
            final String pretty = sequence.toRegularExpression(true);
            final String row = String.format("%s\t%s\t%s", reference, pretty, expression);
            regularExpressions.add(row);
        }
        return regularExpressions;
    }


    /**
     * Writes out the regular expressions and file formats for each byte sequence to a writer.
     * @param writer The writer to write the signature sequences out to.
     */
    public final void debugWriteOutSignatureSequences(final Writer writer) {
        List<String> regExes = toRegularExpressions();
        final String formats = getFileFormatPUIDs();
        final String formatNames = getFileFormatNames();
        final String prefix = String.format("%d\t%s\t%s\t", intSigID, formats, formatNames);
        for (int listIndex = 0; listIndex < regExes.size(); listIndex++) {
            final String output = prefix + regExes.get(listIndex);
            try {
                //System.out.println(output);
                writer.write(output + "\n");
            } catch (IOException ex) {
                getLog().error(ex.getMessage());
            }
        }
    }

}
