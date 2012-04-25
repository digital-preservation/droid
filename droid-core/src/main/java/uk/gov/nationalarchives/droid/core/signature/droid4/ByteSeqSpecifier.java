/*
 ** ByteSeqSpecifier.java
 *
 * � The National Archives 2005-2006.  All rights reserved.
 * See Licence.txt for full licence details.
 *
 * Developed by:
 * Tessella Support Services plc
 * 3 Vineyard Chambers
 * Abingdon, OX14 3PX
 * United Kingdom
 * http://www.tessella.com
 *
 * Tessella/NPD/4826
 * PRONOM 4
 *
 * $Id: ByteSeqSpecifier.java,v 1.7 2006/03/13 15:15:28 linb Exp $
 *
 * $Log: ByteSeqSpecifier.java,v $
 * Revision 1.7  2006/03/13 15:15:28  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.6  2006/02/13 09:26:16  gaur
 * Fixed bug in searching files from EOF, after first STS round
 *
 * Revision 1.5  2006/02/09 15:04:37  gaur
 * Corrected formatting
 *
 * Revision 1.4  2006/02/07 17:16:22  linb
 * - Change fileReader to ByteReader in formal parameters of methods
 * - use new static constructors
 * - Add detection of if a filePath is a URL or not
 *
 * Revision 1.3  2006/02/07 11:30:04  gaur
 * Added support for endianness of signature
 *
 * Revision 1.2  2006/02/03 16:54:41  gaur
 * We now allow general wildcards of arbitrary endianness: e.g., [!~A1B1:C1D1]
 *
 * Revision 1.1  2006/02/02 17:17:04  gaur
 * Initial version.  Functionality not yet complete, but should be sufficient to emulate the old behaviour.
 *
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
