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
 * Tessella/NPD/4826
 * PRONOM 5a
 *
 * $Id: ByteReader.java,v 1.6 2006/03/13 15:15:28 linb Exp $
 *
 * $Log: ByteReader.java,v $
 * Revision 1.6  2006/03/13 15:15:28  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.5  2006/02/09 13:17:41  linb
 * Changed StreamByteReader to InputStreamByteReader
 * Refactored common code from UrlByteReader and InputStreamByteReader into
 * new class StreamByteReader, from which they both inherit
 * Updated javadoc
 *
 * Revision 1.4  2006/02/09 12:14:16  linb
 * Changed some javadoc to allow it to be created cleanly
 *
 * Revision 1.3  2006/02/08 12:51:53  linb
 * Added javadoc comments for file.
 *
 * Revision 1.2  2006/02/08 08:56:35  linb
 * - Added header comments
 *
 */
package uk.gov.nationalarchives.droid.core.signature;


/**
 * Interface for accessing the bytes from a file, URL or stream.
 * <p/>
 * Create an instance with <code>AbstractByteReader.newByteReader()</code>.
 *
 * @author linb
 */
public interface ByteReader {

    /* Setters for identification status */
    /**
     * Set identification status to Positive.
     */
    void setPositiveIdent();

    /**
     * Set identification status to Tentative.
     */
    void setTentativeIdent();

    /**
     * Set identification status to No identification.
     */
    void setNoIdent();

    /**
     * Set identification status to Error.
     */
    void setErrorIdent();

    /**
     * Checks whether the file has yet been classified.
     * @return is classified.
     */
    boolean isClassified();

    /**
     * Get classification of the file.
     * @return classification.
     */
    int getClassification();

    /**
     * Set identification warning.
     *
     * @param theWarning the warning message to use
     */
    void setIdentificationWarning(String theWarning);

    /**
     * Get any warning message created when identifying this file.
     * @return identification warning.
     */
    String getIdentificationWarning();

    /**
     * Add another hit to the list of hits for this file.
     *
     * @param theHit The <code>FileFormatHit</code> to be added
     */
    void addHit(FileFormatHit theHit);

    /**
     * Remove a hit from the list of hits for this file.
     *
     * @param theIndex Index of the hit to be removed
     */
    void removeHit(int theIndex);

    /**
     * Get number of file format hits.
     * @return number of hits.
     */
    int getNumHits();

    /**
     * Get a file format hit.
     *
     * @param theIndex index of the <code>FileFormatHit</code> to get
     * @return the hit associated with <code>theIndex</code>
     */
    FileFormatHit getHit(int theIndex);

    /**
     * Get file path of the associated file.
     * @return file path.
     */
    String getFilePath();

    /**
     * Get file name of the associated file.
     * @return file name.
     */
    String getFileName();


    /**
     * Position the file marker at a given byte position.
     * <p/>
     * The file marker is used to record how far through the file
     * the byte sequence matching algorithm has got.
     *
     * @param markerPosition The byte number in the file at which to position the marker
     */
    void setFileMarker(long markerPosition);

    /**
     * Gets the current position of the file marker.
     *
     * @return the current position of the file marker
     */
    long getFileMarker();

    /**
     * Get a byte from file.
     *
     * @param fileIndex position of required byte in the file
     * @return the byte at position <code>fileIndex</code> in the file
     */
    byte getByte(long fileIndex);

    /**
     * This is provided to avoid having to call getByte on this class.
     * Since getting bytes is called orders of magnitude more than anything
     * else it is extremely performance sensitive.
     * If the implementing class has direct access to bytes, return itself.
     * If not, return a child object which does implementing the 
     * net.domesdaybook.reader interface instead. 
     * @return An object which can read bytes.
     */
    net.domesdaybook.reader.ByteReader getReader();
    
    /**
     * Returns the number of bytes in the file.
     * @return number of bytes in the file.
     */
    long getNumBytes();
    /**
     * Returns the byte array buffer.
     *
     * @return the buffer associated with the file
     */
    byte[] getbuffer();

    /**
     * Closes any files associated with the ByteReader.
     */
    void close();
}
