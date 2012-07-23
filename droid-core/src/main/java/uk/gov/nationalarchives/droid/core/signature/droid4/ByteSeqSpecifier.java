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

import uk.gov.nationalarchives.droid.core.signature.ByteReader;


/**
 * Defines the permissible values to be taken by a specific sequence of bytes.  For example, it might specify that
 * two bytes in succession must be between 8080 and 808F (inclusive)
 * @deprecated DROID 4 version.
 * @author Richard Gault, Tessella
 */
@Deprecated
public class ByteSeqSpecifier {

    // Private members
    private byte[] minSeq;             // The minimum (inclusive) value which the sequence can take: 80, 80 in the example in the header (except that we take off 128 before storing a value in the array, since bytes are unsigned)
    private byte[] maxSeq;             // The maximum (inclusive) value which the sequence can take: 80, 8F in the example in the header
    private boolean negate;            // If true, negates the sense of the test (in the example in the header, it would specify that the two bytes must be outside the range 8080-808f)

    /* Getter */
    public int getNumBytes() {
        return minSeq.length;
    }   // Will always be the same as maxSeq.length

    /**
     * Creates a new instance of ByteSeqSpecifier
     *
     * @param asciiRep A StringBuffer whose initial portion will be an ASCII representation of the bytes specifier.  This will be
     *                 altered so that this initial portion is removed.
     */
    public ByteSeqSpecifier(StringBuffer asciiRep) throws Exception {
        String specifier;    // The string of characters defining the bytes specifier (excluding any square brackets)

        // First off, handle the case of a simple specifier: A2, for example.
        if (asciiRep.charAt(0) != '[') {
            specifier = asciiRep.substring(0, 2);
            asciiRep.delete(0, 2);
        } else {
            // We have a non-trivial byte sequence Specifier.  Extract it from the front of asciiRep
            specifier = asciiRep.substring(1, asciiRep.indexOf("]"));
            asciiRep.delete(0, specifier.length() + 2);
        }

        negate = false;
        // Does the specifier begin with a ! (indicating negation)?  Remove it if so.
        while (specifier.charAt(0) == '!' || specifier.charAt(0) == '~') {
            if (specifier.charAt(0) == '!') {
                negate = !negate;
            }
            specifier = specifier.substring(1);
        }

        // Does the specifier contain a : (indicating a range)?  If so, set minRage and maxRange to be the strings on either side.
        // If not, set them both to be the same: the whole of specifier.
        String minRange;
        String maxRange;
        int colonPos = specifier.indexOf(':');
        if (colonPos >= 0) {
            minRange = specifier.substring(0, colonPos);
            maxRange = specifier.substring(colonPos + 1);
        } else {
            minRange = specifier;
            maxRange = specifier;
        }

        // Sanity check that minRange and maxRange are the same length
        if (minRange.length() != maxRange.length()) {
            throw new Exception("Invalid internal signature supplied");
        }

        // We may now assume that both minRange and maxRange contain pairs of characters representing concrete bytes.  Extract and
        // store them in our two arrays
        int seqLength = minRange.length() / 2;
        minSeq = new byte[seqLength];
        maxSeq = new byte[seqLength];
        for (int i = 0; i < seqLength; i++) {
            int byteVal = Integer.parseInt(minRange.substring(2 * i, 2 * (i + 1)), 16);
            minSeq[i] = (byte) (byteVal + Byte.MIN_VALUE);
            byteVal = Integer.parseInt(maxRange.substring(2 * i, 2 * (i + 1)), 16);
            maxSeq[i] = (byte) (byteVal + Byte.MIN_VALUE);
        }
    }


    /**
     * Determines whether or not a given portion of a binary file matches the sequence of bytes we specify.
     *
     * @param file      The file we're currently testing
     * @param startPos  The position of the first byte in the file to examine
     * @param direction +1 (left to right) or -1 (right to left).  The overall direction which our caller is searching in
     * @param bigEndian True iff the signature we are matching is big-endian
     * @return true iff the portion matches
     *         <p/>
     *         Note: In an ideal world, we would hold bigEndian as a private member, set up on construction.  However, the framework
     *         used during parsing of the XML file does not lend itself to easily fetching information from a grandparent
     *         element.  Consequently, we parse the byte sequence specifier in ignorance of its endianness, and wait until
     *         we try to match against a specific byte sequence (here) to find out how minSeq and maxSeq should be interpreted.
     */
    public boolean matchesByteSequence(ByteReader file, long startPos, int direction, boolean bigEndian) {
        try {
            // We have to perform the comparison from big-end to little-end.  Consequently, if we're reading
            // from right to left but using big-endian-ness, or if we're reading from left-to-right but using
            // little-endian-ness, we have to search through our sequence backwards -- that is, left-to-right
            // in the former case, or right-to-left in the latter.
            if (!bigEndian && direction == 1) {
                direction = -1;
                startPos += this.getNumBytes() - 1;
            } else if (bigEndian && direction == -1) {
                direction = 1;
                startPos = startPos - this.getNumBytes() + 1;
            }
            int arrayPos = (direction == 1) ? 0 : this.getNumBytes() - 1;

            // Loop through the sequence, checking to ensure that the contents of the binary file >= the minimum sequence
            for (int fileOffset = 0; 0 <= arrayPos && arrayPos < this.getNumBytes(); fileOffset += direction, arrayPos += direction) {
                // Read the corresponding byte from the file.  Because this is stored in 2s complement form, we need to
                // convert it to the same form that minSeq is stored in
                int fileByte = file.getByte(startPos + fileOffset);
                if (fileByte < 0) {
                    fileByte += 256;
                }
                fileByte += Byte.MIN_VALUE;

                if (fileByte < minSeq[arrayPos]) {
                    // We're outside the allowed range.
                    return negate;
                } else if (fileByte > minSeq[arrayPos]) {
                    // The whole of the sequence is definitely greater than minSeq.  Go on and see if it's less than maxSeq.
                    break;
                }
            }

            // Repeat the previous loop, but this time checking to ensure that the contents of the binary file <= the maximum sequence
            arrayPos = (direction == 1) ? 0 : this.getNumBytes() - 1;
            for (int fileOffset = 0; arrayPos >= 0 && arrayPos < this.getNumBytes(); fileOffset += direction, arrayPos += direction) {
                int fileByte = file.getByte(startPos + fileOffset);
                if (fileByte < 0) {
                    fileByte += 256;
                }
                fileByte += Byte.MIN_VALUE;

                if (fileByte > maxSeq[arrayPos]) {
                    return negate;
                } else if (fileByte < maxSeq[arrayPos]) {
                    break;
                }
            }

            return !negate;
        } catch (Exception e) {
            // This is most likely to occur if we run off the end of the file.  (In practice, this method shouldn't be called
            // unless we have enough bytes to read, but this is belt and braces.)
            return false;
        }
    }
}
