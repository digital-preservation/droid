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
package uk.gov.nationalarchives.droid.core.signature.droid4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds a subsequence for a byte sequence
 * also contains most of the logic for identifying files
 *
 * @author Martin Waller
 * @version 4.0.0
 * @deprecated DROID 4 version. 
 */
@Deprecated
public class SubSequence extends SimpleElement {

    private Log log = LogFactory.getLog(this.getClass());
    private String reference;
    private int parentSignature;
    private boolean bigEndian = true;
    private ByteSequence parentByteSequence;
    int position;
    int minSeqOffset = 0;
    int maxSeqOffset = 0;
    int minFragLength;
    String sequence;
    //        shiftFunction shift;
    long[] shiftFunction = new long[256];
    List<LeftFragment> leftFragments = new ArrayList<LeftFragment>();
    List<RightFragment> rightFragments = new ArrayList<RightFragment>();
    byte[] byteSequence;
    List<List<SideFragment>> orderedLeftFragments = new ArrayList<List<SideFragment>>();
    List<List<SideFragment>> orderedRightFragments = new ArrayList<List<SideFragment>>();
    static boolean showProgress = false;

    public ByteSequence getByteSequence() {
        return parentByteSequence;
    }

    public void setByteSequence(ByteSequence byteSequence) {
        this.parentByteSequence = byteSequence;
    }

    /**
     * is this a EOF sub squence
     * If this subsequence does not match the
     * we can reject the entire signature
     *
     * @return boolean
     */
    public boolean isEOF() {
        return (reference.equalsIgnoreCase("EOFoffset") && (position == 1));
    }

    /**
     * is this a BOF sub squence
     * If this subsequence does not match the
     * we can reject the entire signature
     *
     * @return boolean
     */
    public boolean isBOF() {
        return (reference.equalsIgnoreCase("BOFoffset") && (position == 1));
    }

    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Get the id of the internal
     * signature that this sequence belongs to
     *
     * @return
     */
    public int getParentSignature() {
        return parentSignature;
    }

    public void setParentSignature(int parentSignature) {
        this.parentSignature = parentSignature;
    }

    /* setters */
    public void addLeftFragment(LeftFragment lf) {
        leftFragments.add(lf);
    }

    public void addRightFragment(RightFragment lf) {
        rightFragments.add(lf);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setShift(Shift theShift) {
        int theShiftByte = theShift.getShiftByte();
        if (theShiftByte >= 0 && theShiftByte < 128) {
            this.shiftFunction[theShiftByte + 128] = theShift.getShiftValue();
        } else if (theShiftByte >= 128 && theShiftByte < 256) {
            this.shiftFunction[theShiftByte - 128] = theShift.getShiftValue();
        }
    }

    public void setDefaultShift(String theValue) {
        for (int i = 0; i < 256; i++) {
            this.shiftFunction[i] = Long.parseLong(theValue);
        }
    }

    public void setSequence(String seq) {
        this.sequence = seq;
        int seqLength = seq.length() / 2;
        if (2 * seqLength != seq.length()) {
            log.error("A problem - sequence of odd length was found: " + seq);
            System.out.println("A problem - sequence of odd length was found: " + seq);
        }
        byteSequence = new byte[seqLength];
        for (int i = 0; i < seqLength; i++) {
            int byteVal = Integer.parseInt(seq.substring(2 * i, 2 * (i + 1)), 16);
            byteSequence[i] = (byteVal > Byte.MAX_VALUE) ? (byte) (byteVal - 256) : (byte) byteVal;
        }
    }

    public void setMinSeqOffset(int theOffset) {
        this.minSeqOffset = theOffset;
        if (this.maxSeqOffset < this.minSeqOffset) {
            this.maxSeqOffset = this.minSeqOffset;
        }
    }

    public void setMaxSeqOffset(int theOffset) {
        this.maxSeqOffset = theOffset;
        if (this.maxSeqOffset < this.minSeqOffset) {
            this.maxSeqOffset = this.minSeqOffset;
        }
    }

    public void setMinFragLength(int theLength) {
        this.minFragLength = theLength;
    }

    @Override
    public void setAttributeValue(String name, String value) {
        if (name.equals("Position")) {
            setPosition(Integer.parseInt(value));
        } else if (name.equals("SubSeqMinOffset")) {
            setMinSeqOffset(Integer.parseInt(value));
        } else if (name.equals("SubSeqMaxOffset")) {
            setMaxSeqOffset(Integer.parseInt(value));
        } else if (name.equals("MinFragLength")) {
            setMinFragLength(Integer.parseInt(value));
        } else {
            unknownAttributeWarning(name, this.getElementName());
        }
    }

    /* getters */
    public int getNumFragmentPositions(boolean leftFrag) {
        if (leftFrag) {
            return this.orderedLeftFragments.size();
        } else {
            return this.orderedRightFragments.size();
        }
    }

    public int getNumAlternativeFragments(boolean leftFrag, int thePosition) {
        if (leftFrag) {
            return this.orderedLeftFragments.get(thePosition - 1).size();
        } else {
            return this.orderedRightFragments.get(thePosition - 1).size();
        }
    }

    public SideFragment getFragment(boolean leftFrag, int thePosition, int theIndex) {
        if (leftFrag) {
            return (SideFragment) ((ArrayList) this.orderedLeftFragments.get(thePosition - 1)).get(theIndex);
        } else {
            return (SideFragment) ((ArrayList) this.orderedRightFragments.get(thePosition - 1)).get(theIndex);
        }
    }

    public long getShift(byte theByteValue) {
        // this.ShiftFunction is a long[256] array
        return this.shiftFunction[theByteValue + 128];
    }

    public String getSequence() {
        return sequence;
    }

    public byte getByte(int theIndex) {
        return byteSequence[theIndex];
    }

    public int getNumBytes() {
        return byteSequence.length;
    }

    public List<LeftFragment> getLeftFragments() {
        return leftFragments;
    }

    public List<RightFragment> getRightFragments() {
        return rightFragments;
    }

    public LeftFragment getRawLeftFragment(int theIndex) {
        return leftFragments.get(theIndex);
    }

    public RightFragment getRawRightFragment(int theIndex) {
        return rightFragments.get(theIndex);
    }

    public int getPosition() {
        return position;
    }

    public int getMinSeqOffset() {
        return minSeqOffset;
    }

    public int getMaxSeqOffset() {
        return maxSeqOffset;
    }

    public int getMinFragLength() {
        return minFragLength;
    }

    /**
     * Re-orders the left and right sequence fragments in increasing position order
     * this method must be after the signature file has been parsed and
     * before running any file identifications
     */
    public void prepareSeqFragments() {

        /* Left fragments */
        //Determine the number of fragment subsequences there are
        int numFrags = 0;
        for (int i = 0; i < leftFragments.size(); i++) {
            int currentPosition = this.getRawLeftFragment(i).getPosition();
            if (currentPosition > numFrags) {
                numFrags = currentPosition;
            }
        }

        //initialise all necessary fragment lists (one for each position)
        for (int i = 0; i < numFrags; i++) { //loop through fragment positions
            List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
            orderedLeftFragments.add(alternativeFragments);
        }

        //Add fragments to new structure
        for (int i = 0; i < leftFragments.size(); i++) {  //loop through all fragments
            int currentPosition = this.getRawLeftFragment(i).getPosition();
            orderedLeftFragments.get(currentPosition - 1).add(this.getRawLeftFragment(i));
        }

        //clear out unecessary info
        this.leftFragments = null;

        /* Right fragments */
        //Determine the number of fragment subsequences there are
        numFrags = 0;
        for (int i = 0; i < rightFragments.size(); i++) {
            int currentPosition = this.getRawRightFragment(i).getPosition();
            if (currentPosition > numFrags) {
                numFrags = currentPosition;
            }
        }

        //initialise all necessary fragment lists (one for each position)
        for (int i = 0; i < numFrags; i++) { //loop through fragment positions
            List<SideFragment> alternativeFragments = new ArrayList<SideFragment>();
            orderedRightFragments.add(alternativeFragments);
        }

        //Add fragments to new structure
        for (int i = 0; i < rightFragments.size(); i++) {  //loop through all fragments
            int currentPosition = this.getRawRightFragment(i).getPosition();
            orderedRightFragments.get(currentPosition - 1).add(this.getRawRightFragment(i));
        }

        //clear out unecessary info
        this.rightFragments = null;

    }

    /**
     * Searches for this subsequence after the current file marker position in the file.
     * Moves the file marker to the end of this subsequence.
     *
     * @param targetFile   the binary file to be identified
     * @param reverseOrder true if file is being searched from right to left
     * @param bigEndian    True iff our parent signature is big-endian
     * @return boolean
     */
    public boolean isFoundAfterFileMarker(ByteReader targetFile, boolean reverseOrder, boolean bigEndian) {
        boolean subSeqFound = false;
        try {
            long fileSize = targetFile.getNumBytes() - 1;
            int searchDirection = reverseOrder ? -1 : 1;
            //get the current file marker
            long startPosInFile = targetFile.getFileMarker();
            //Add the minimum offset before start of sequence and update the file marker accordingly
            startPosInFile = startPosInFile + (long) (searchDirection * this.getMinSeqOffset());
            if (fileSize < startPosInFile - 1) {
                // We're looking for a sequence of bytes at an offset which is longer than the file itself
                return false;
            }
            targetFile.setFileMarker(startPosInFile);
            //start searching for main sequence after the minimum length of the relevant fragments
            startPosInFile = startPosInFile + (long) (searchDirection * this.getMinFragLength());
            int numSeqBytes = this.getNumBytes();

            boolean missMatchFound;
            int byteLoopStart = reverseOrder ? numSeqBytes - 1 : 0;
            int byteLoopEnd = reverseOrder ? 0 : numSeqBytes - 1;

            try {
                while (!subSeqFound) {

                    //compare sequence with file contents directly at fileMarker position
                    missMatchFound = false;

                    // Start by checking the last byte in the window on the file.
                    // If this byte is different from the last byte in the subsequence,
                    // Then we may shift the window according to the value of this byte.
                    // In practice, this saves us from unnecessarily checking file bytes to calculate the shift.
                    byte lastByte = targetFile.getByte(startPosInFile + byteLoopEnd);
                    if (byteSequence[byteLoopEnd] != lastByte) {
                        startPosInFile += (this.shiftFunction[128 + lastByte] - 1);
                        if ((startPosInFile < 0L) || (startPosInFile > fileSize)) {
                            break;
                        }
                    } else {
                        // If the last bytes don't match, then check the rest.
                        for (int iByte = byteLoopStart; (!missMatchFound) && (iByte <= numSeqBytes - 1) && (iByte >= 0); iByte += searchDirection) {
                            missMatchFound = (byteSequence[iByte] != targetFile.getByte(startPosInFile + iByte - byteLoopStart));
                        }
                        if (!missMatchFound) { //subsequence was found at position fileMarker in the file
                            //Now search for fragments between original fileMarker and startPosInFile
                            if (reverseOrder) {
                                long rightFragEnd;
                                long[] rightFragEndArray = bytePosForRightFragments(targetFile, startPosInFile + 1, targetFile.getFileMarker(), 1, 0, bigEndian);
                                if (rightFragEndArray.length == 0) {
                                    missMatchFound = true;
                                } else {
                                    rightFragEnd = rightFragEndArray[0];
                                    long leftFragEnd;
                                    long[] leftFragEndArray = bytePosForLeftFragments(targetFile, 0, startPosInFile - numSeqBytes, -1, 0, bigEndian);
                                    if (leftFragEndArray.length == 0) {
                                        missMatchFound = true;
                                    } else {
                                        leftFragEnd = leftFragEndArray[0];
                                        targetFile.setFileMarker(leftFragEnd - 1L);
                                        subSeqFound = true;
                                    }
                                }

                            } else {  //search is in forward direction
                                long leftFragEnd;
                                long[] leftFragEndArray = bytePosForLeftFragments(targetFile, targetFile.getFileMarker(), startPosInFile - 1L, -1, 0, bigEndian);
                                if (leftFragEndArray.length == 0) {
                                    missMatchFound = true;
                                } else {
                                    leftFragEnd = leftFragEndArray[0];
                                    long rightFragEnd;
                                    long[] rightFragEndArray = bytePosForRightFragments(targetFile, startPosInFile + numSeqBytes, targetFile.getNumBytes() - 1L, 1, 0, bigEndian);
                                    if (rightFragEndArray.length == 0) {
                                        missMatchFound = true;
                                    } else {
                                        rightFragEnd = rightFragEndArray[0];
                                        targetFile.setFileMarker(rightFragEnd + 1L);
                                        subSeqFound = true;
                                    }
                                }
                            }
                        }

                        if (missMatchFound) {
                            // If a mismatch is found, then shift the window by a shift calculated from the value
                            // of the file byte occuring one place after the window position.
                            startPosInFile += this.shiftFunction[128 + targetFile.getByte(startPosInFile + (long) (searchDirection * numSeqBytes))];
                            if ((startPosInFile < 0L) || (startPosInFile > fileSize)) {
                                break;
                            }
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                // This only happens when the end of the file is reached.
                // This exception is allowed to be thrown to avoid repeatedly checking if the index is valid
                // and to hence improve the performace of DROID
            }
        } catch (IndexOutOfBoundsException e) {
                // This is thrown if targetFile is a URLByteReader, the embedded HeapByteBuffer will check for each access
                // and throw java.lang.IndexOutOfBoundsException if we are on or past the limit
        }
        return subSeqFound;
    }

    /**
     * Interpret the bytes in a file as an offset.
     * <p/>
     * The next <code>indirectOffsetLength()</code> bytes after <code>indirectOffsetLocation()</code> are interpreted
     * as an offset according to the endianness of the byte sequence.
     *
     * @param targetFile
     * @return
     */
    private int getIndirectOffset(ByteReader targetFile) {
        int offset = 0;
        long power = 1;

        long offsetLocation = this.getByteSequence().getIndirectOffsetLocation();

        if (this.getByteSequence().getReference().endsWith("EOFoffset")) {
            offsetLocation = targetFile.getNumBytes() - offsetLocation - 1;
        }

        int offsetLength = this.getByteSequence().getIndirectOffsetLength();

        // In the case of indirect BOF or indirect EOF bytesequences,
        // We need to get read the file to get the offset.
        if (this.isBigEndian()) {
            for (int i = offsetLength - 1; i > -1; i--) {
                Byte fileByte = targetFile.getByte(offsetLocation + i);
                int byteValue = fileByte.intValue();
                byteValue = (byteValue >= 0) ? byteValue : byteValue + 256;
                offset += power * byteValue;
                power *= 256;
            }
        } else {
            for (int i = 0; i < offsetLength; i++) {
                Byte fileByte = targetFile.getByte(offsetLocation + i);
                int byteValue = fileByte.intValue();
                byteValue = (byteValue >= 0) ? byteValue : byteValue + 256;
                offset += power * byteValue;
                power *= 256;
            }
        }

        return offset;
    }

    /**
     * Searches for this subsequence at the start of the current file.
     * Moves the file marker to the end of this subsequence.
     *
     * @param targetFile   the binary file to be identified
     * @param reverseOrder true if file is being searched from right to left
     * @param bigEndian    True iff our parent signature is big-endian
     * @return
     */
    public boolean isFoundAtStartOfFile(ByteReader targetFile, boolean reverseOrder, boolean bigEndian) {

        try {
            int searchDirection = reverseOrder ? -1 : 1;
            int minSeqOffset = this.getMinSeqOffset();
            int maxSeqOffset = this.getMaxSeqOffset();

            // Get any indirect offset
            if (this.reference.startsWith("Indirect")) {
                try {
                    int indirectOffset = this.getIndirectOffset(targetFile);
                    minSeqOffset += indirectOffset;
                    maxSeqOffset += indirectOffset;
                } catch (Exception e) {
                    // If an exception is thrown, we can assume that the file did not match the indirect offset
                    // eg. the indirect offset found  could be too large to be held in an int type
                    return false;
                }
            }

            long[] startPosInFile = new long[1];
            startPosInFile[0] = reverseOrder ? targetFile.getNumBytes() - minSeqOffset - 1 : minSeqOffset;
            boolean subseqFound = true;
            boolean leftFrag = true;

            if (reverseOrder) {
                leftFrag = false;
            }

            //match intial fragment
            if (reverseOrder) {
                startPosInFile = bytePosForRightFragments(targetFile, 0, startPosInFile[0], -1, (maxSeqOffset - minSeqOffset), bigEndian);
            } else {
                startPosInFile = bytePosForLeftFragments(targetFile, startPosInFile[0], targetFile.getNumBytes() - 1, 1, (maxSeqOffset - minSeqOffset), bigEndian);
            }
            int numOptions = startPosInFile.length;
            if (numOptions == 0) {
                subseqFound = false;
            } else {
                for (int i = 0; i < numOptions; i++) {
                    startPosInFile[i] += (long) searchDirection;
                }
            }

            //match main sequence
            if (subseqFound) {

                //move startPosInFile according to min offset of last fragment looked at
                int minOffset = 0;
                int maxOffset = 0;
                if (this.getNumFragmentPositions(leftFrag) > 0) {
                    minOffset = this.getFragment(leftFrag, 1, 0).getMinOffset();
                    maxOffset = this.getFragment(leftFrag, 1, 0).getMaxOffset();
                    for (int i = 0; i < numOptions; i++) {
                        startPosInFile[i] += (long) (minOffset * searchDirection);
                    }
                }

                //add new possible values for startPosInFile to allow for difference between maxOffset and minOffset
                int offsetRange = maxOffset - minOffset;
                if (offsetRange > 0) {
                    long[] newStartPosInFile = new long[numOptions * (offsetRange + 1)];
                    for (int i = 0; i <= offsetRange; i++) {
                        for (int j = 0; j < numOptions; j++) {
                            newStartPosInFile[j + i * numOptions] = startPosInFile[j] + (long) (i * searchDirection);
                        }
                    }
                    Arrays.sort(newStartPosInFile);
                    int newNumOptions = 1;
                    for (int i = 1; i < numOptions * (offsetRange + 1); i++) {
                        if (newStartPosInFile[i] > newStartPosInFile[newNumOptions - 1]) {
                            newStartPosInFile[newNumOptions] = newStartPosInFile[i];
                            newNumOptions++;
                        }
                    }
                    //now copy these back to the startPosInFile array (sorted in searchDirection)
                    numOptions = newNumOptions;
                    if (searchDirection > 1) {
                        System.arraycopy(newStartPosInFile, 0, startPosInFile, 0, numOptions);
                    } else {
                        //reverse order copy
                        for (int i = 0; i < numOptions; i++) {
                            startPosInFile[i] = newStartPosInFile[numOptions - 1 - i];
                        }
                    }

                }

                //check that the end of the file is not going to be reached
                int numSeqBytes = this.getNumBytes();
                long numBytesInFile = targetFile.getNumBytes();
                if (reverseOrder) {
                    //cutoff if startPosInFile is too close to start of file
                    for (int i = 0; i < numOptions; i++) {
                        if (startPosInFile[i] < ((long) numSeqBytes - 1L)) {
                            numOptions = i;
                        }
                    }
                } else {
                    //cutoff if startPosInFile is too close to end of file
                    for (int i = 0; i < numOptions; i++) {
                        if (startPosInFile[i] > (numBytesInFile - (long) numSeqBytes)) {
                            numOptions = i;
                        }
                    }
                }


                for (int iOption = 0; iOption < numOptions; iOption++) {
                    //compare sequence with file contents directly at fileMarker position
                    int byteLoopStart = reverseOrder ? numSeqBytes - 1 : 0;
                    int byteLoopEnd = reverseOrder ? 0 : numSeqBytes - 1;
                    long tempFileMarker = startPosInFile[iOption];
                    boolean provSeqMatch = true;

                    //check whether the file and signature sequences match
                    for (int iByte = byteLoopStart; (provSeqMatch) && (iByte <= numSeqBytes - 1) && (iByte >= 0); iByte += searchDirection) {
                        provSeqMatch = (byteSequence[iByte] == targetFile.getByte(tempFileMarker));
                        tempFileMarker += searchDirection;
                    }

                    if (!provSeqMatch) {
                        //no match
                        startPosInFile[iOption] = -2L;
                    } else {
                        //success: a match was found - update the startPosInFile
                        startPosInFile[iOption] = tempFileMarker;
                    }
                }

                //check the startPosInFile array: remove -2 values, reorder and remove duplicates
                Arrays.sort(startPosInFile, 0, numOptions);
                int newNumOptions = 0;
                long[] newStartPosInFile = new long[numOptions];
                if (numOptions > 0) {
                    if (startPosInFile[0] >= -1L) {
                        newStartPosInFile[0] = startPosInFile[0];
                        newNumOptions = 1;
                    }
                }
                for (int i = 1; i < numOptions; i++) {
                    if (startPosInFile[i] > startPosInFile[i - 1]) {
                        newStartPosInFile[newNumOptions] = startPosInFile[i];
                        newNumOptions++;
                    }
                }

                if (newNumOptions == 0) {
                    subseqFound = false;
                } else {
                    numOptions = newNumOptions;
                    if (searchDirection < 0) {
                        //for right to left search direction, reorder in reverse
                        for (int iOption = 0; iOption < numOptions; iOption++) {
                            startPosInFile[iOption] = newStartPosInFile[numOptions - 1 - iOption];
                        }
                    } else {
                        //for left to right search direction, copy over as is
                        System.arraycopy(newStartPosInFile, 0, startPosInFile, 0, numOptions);
                    }
                }
            }

            //match remaining sequence fragment
            long newValueStartPosInFile = 0L;
            if (subseqFound) {

                long[] newArrayStartPosInFile;
                if (reverseOrder) {
                    int i = 0;
                    subseqFound = false;
                    while (i < numOptions && !subseqFound) {
                        newArrayStartPosInFile = bytePosForLeftFragments(targetFile, 0L, startPosInFile[i], -1, 0, bigEndian);
                        if (newArrayStartPosInFile.length == 0) {
                            subseqFound = false;
                        } else {
                            subseqFound = true;
                            newValueStartPosInFile = newArrayStartPosInFile[0] - 1L;  //take away -1???
                        }
                        i++;
                    }
                } else {
                    int i = 0;
                    subseqFound = false;
                    while (i < numOptions && !subseqFound) {
                        newArrayStartPosInFile = bytePosForRightFragments(targetFile, startPosInFile[i], targetFile.getNumBytes() - 1L, 1, 0, bigEndian);
                        if (newArrayStartPosInFile.length == 0) {
                            subseqFound = false;
                        } else {
                            subseqFound = true;
                            newValueStartPosInFile = newArrayStartPosInFile[0] + 1L;  //take away +1????
                        }
                        i++;
                    }
                }
            }

            //update the file marker
            if (subseqFound) {
                targetFile.setFileMarker(newValueStartPosInFile);
            }

            return subseqFound;
        } catch (IndexOutOfBoundsException e) {
            // If an indirect offset points to a place that is after the end of the file,
            // Then this exception is thrown and it can be assumed that the signature is not compliant
            return false;
        }

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
     * @param bigEndian       True iff our parent signature is big-endian
     * @return
     */
    private long[] bytePosForLeftFragments(ByteReader targetFile, long leftBytePos, long rightBytePos,
            int searchDirection, int offsetRange, boolean bigEndian) {
        boolean leftFrag = true;
        long startPos = rightBytePos;
        int posLoopStart = 1;
        int numFragPos = this.getNumFragmentPositions(leftFrag);
        if (searchDirection == 1) {
            startPos = leftBytePos;
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
        for (int iFragPos = posLoopStart; (!seqNotFound) && (iFragPos <= numFragPos) && (iFragPos >= 1); iFragPos -= searchDirection) {
            int numAltFrags = this.getNumAlternativeFragments(leftFrag, iFragPos);
            long[] tempEndPos = new long[numAltFrags * numOptions]; //array to store possible end positions after this fragment position has been examined


            int numEndPos = 0;
            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end positions
                for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
                    long tempFragEnd;
                    if (searchDirection == 1) {
                        tempFragEnd = this.endBytePosForSeqFrag(targetFile, markerPos[iOption], rightBytePos, true, searchDirection, iFragPos, iAlt, bigEndian);
                    } else {
                        tempFragEnd = this.endBytePosForSeqFrag(targetFile, leftBytePos, markerPos[iOption], true, searchDirection, iFragPos, iAlt, bigEndian);
                    }
                    if (tempFragEnd > -1L) { // amatch has been found
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
            long[] outArray = new long[0];
            return outArray;

        } else {
            // return ordered array of possibilities
            long[] outArray = new long[numOptions];

            //convert values to negative temporarily so that reverse sort order can be obtained for a right to left search direction
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
                outArray[iOption] -= (long) searchDirection;
            }

            return outArray;
        }

    }

    /**
     * Searches for the right fragments of this subsequence between the given byte
     * positions in the file.  Either returns the last byte taken up by the
     * identified sequences or returns -2 if no match was found
     *
     * @param targetFile      the binary file to be identified
     * @param leftBytePos     left-most byte position of allowed search window on file
     * @param rightBytePos    right-most byte position of allowed search window on file
     * @param searchDirection 1 for a left to right search, -1 for right to left
     * @param offsetRange     range of possible start positions in the direction of searchDirection
     * @param bigEndian       True iff our parent signature is big-endian
     * @return
     */
    private long[] bytePosForRightFragments(ByteReader targetFile, long leftBytePos, long rightBytePos,
            int searchDirection, int offsetRange, boolean bigEndian) {
        boolean leftFrag = false;
        long startPos = leftBytePos;
        int posLoopStart = 1;
        int numFragPos = this.getNumFragmentPositions(leftFrag);
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
        for (int iFragPos = posLoopStart; (!seqNotFound) && (iFragPos <= numFragPos) && (iFragPos >= 1); iFragPos += searchDirection) {
            int numAltFrags = this.getNumAlternativeFragments(leftFrag, iFragPos);
            long[] tempEndPos = new long[numAltFrags * numOptions]; //array to store possible end positions after this fragment position has been examined
            int numEndPos = 0;
            for (int iOption = 0; iOption < numOptions; iOption++) {
                //will now look for all matching alternative sequence at the current end positions
                for (int iAlt = 0; iAlt < numAltFrags; iAlt++) {
                    long tempFragEnd;
                    if (searchDirection == -1) {
                        tempFragEnd = this.endBytePosForSeqFrag(targetFile, leftBytePos, markerPos[iOption], false, searchDirection, iFragPos, iAlt, bigEndian);
                    } else {
                        tempFragEnd = this.endBytePosForSeqFrag(targetFile, markerPos[iOption], rightBytePos, false, searchDirection, iFragPos, iAlt, bigEndian);
                    }
                    if (tempFragEnd > -1) { // amatch has been found
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
            long[] outArray = new long[0];
            return outArray;

        } else {
            // return ordered array of possibilities
            long[] outArray = new long[numOptions];

            //convert values to negative temporarily so that reverse sort order can be obtained for a right to left search direction
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
                outArray[iOption] -= (long) searchDirection;
            }

            return outArray;
        }

    }

    /**
     * searches for the specified fragment sequence
     * between the leftmost and rightmost byte positions that are given.
     * returns the end position of the found sequence or -1 if it is not found
     *
     * @param targetFile      The file that is being reviewed for identification
     * @param leftEndBytePos  leftmost position in file at which to search
     * @param rightEndBytePos rightmost postion in file at which to search
     * @param leftFrag        flag to indicate whether looking at left or right fragments
     * @param searchDirection direction in which search is carried out (1 for left to right, -1 for right to left)
     * @param fragPos         position of left/right sequence fragment to use
     * @param fragIndex       index of fragment within the position (where alternatives exist)
     * @param bigEndian       True iff out parent signature is big-endian
     * @return
     */
    private long endBytePosForSeqFrag(ByteReader targetFile, long leftEndBytePos, long rightEndBytePos,
            boolean leftFrag, int searchDirection, int fragPos, int fragIndex, boolean bigEndian) {
        long startPosInFile;
        long lastStartPosInFile;
        long endPosInFile = -1L;
        long searchDirectionL = (long) searchDirection;
        int numBytes;
        int minOffset;
        int maxOffset;

        // read in values
        numBytes = this.getFragment(leftFrag, fragPos, fragIndex).getNumBytes();
        if (leftFrag && (searchDirection == -1)) {
            minOffset = this.getFragment(leftFrag, fragPos, fragIndex).getMinOffset();
            maxOffset = this.getFragment(leftFrag, fragPos, fragIndex).getMaxOffset();
        } else if (!leftFrag && (searchDirection == 1)) {
            minOffset = this.getFragment(leftFrag, fragPos, fragIndex).getMinOffset();
            maxOffset = this.getFragment(leftFrag, fragPos, fragIndex).getMaxOffset();
        } else if (fragPos < this.getNumFragmentPositions(leftFrag)) {
            minOffset = this.getFragment(leftFrag, fragPos + 1, 0).getMinOffset();
            maxOffset = this.getFragment(leftFrag, fragPos + 1, 0).getMaxOffset();
        } else {
            minOffset = 0;
            maxOffset = 0;
        }

        // set up start and end positions for searches taking into account min and max offsets
        if (searchDirection == -1) {
            startPosInFile = rightEndBytePos - (long) minOffset;
            long lastStartPosInFile1 = leftEndBytePos + (long) numBytes - 1L;
            long lastStartPosInFile2 = rightEndBytePos - (long) maxOffset;
            lastStartPosInFile = (lastStartPosInFile1 < lastStartPosInFile2) ? lastStartPosInFile2 : lastStartPosInFile1;
        } else {
            startPosInFile = leftEndBytePos + (long) minOffset;
            long lastStartPosInFile1 = rightEndBytePos - (long) numBytes + 1L;
            long lastStartPosInFile2 = leftEndBytePos + (long) maxOffset;
            lastStartPosInFile = (lastStartPosInFile1 < lastStartPosInFile2) ? lastStartPosInFile1 : lastStartPosInFile2;
        }

        //keep searching until either the sequence fragment is found or until the end of the search area has been reached.
        //compare sequence with file contents directly at fileMarker position
        boolean subSeqFound = false;
        while ((!subSeqFound) && ((searchDirectionL) * (lastStartPosInFile - startPosInFile) >= 0L)) {
            boolean missMatchFound = false;
            int byteLoopStart;
            if (searchDirection == -1) {
                byteLoopStart = numBytes - 1;
            } else {
                byteLoopStart = 0;
            }
            SideFragment fragment = this.getFragment(leftFrag, fragPos, fragIndex);
            long tempFileMarker = startPosInFile;
            for (int i = (searchDirection == 1) ? 0 : fragment.getNumByteSeqSpecifiers() - 1; !missMatchFound && 0 <= i && i < fragment.getNumByteSeqSpecifiers(); i += searchDirection) {
                missMatchFound = !fragment.getByteSeqSpecifier(i).matchesByteSequence(targetFile, tempFileMarker, searchDirection, bigEndian);
                if (!missMatchFound) {
                    tempFileMarker += searchDirection * fragment.getByteSeqSpecifier(i).getNumBytes();
                }
            }
            if (!missMatchFound) { //subsequence fragment was found in the file
                subSeqFound = true;
                endPosInFile = tempFileMarker - searchDirectionL;
            } else {
                startPosInFile += searchDirectionL;
            }
        }
        return endPosInFile;  //this is -1 unless subSeqFound = true
    }

    public String toString() {
        return position + " seq=<" + sequence + ">" + "LLL" + orderedLeftFragments + "LLL" + "RRR" + orderedRightFragments + "RRR";
    }
}
