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
package uk.gov.nationalarchives.droid.core.interfaces;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import net.domesdaybook.reader.ByteReader;

/**
 * Encapsulates an identification request.
 * @author rflitcroft
 *
 */
public interface IdentificationRequest {

    /**
     * Returns a byte at the position specified.
     * @param position the position of the byte .
     * @return an array of bytes.
     */
    byte getByte(long position);
    
    /**
     * Returns a byte reader for fast access to the bytes by
     * the binary signature engine.  If the identification request can provide
     * fast access to the bytes directly, then it can implement the ByteReader
     * interface and return itself.  If it relies on a child object to cache and
     * return bytes, then that object should implement the ByteReader interface.
     * 
     * <p>Avoids an additional read byte method call on IdentificationRequest
     * if it relies on a child object to provide access to the bytes.
     * Even though each call is a tiny amount of time, the additional method
     * calls add up, as reading a byte is the most called method in the
     * entirety of droid - by orders of magnitude.  It makes a real difference
     * to the processing speed to avoid additional call overhead here.
     * Done only as a result of profiling the software.<p/>
     * 
     * @return A net.domesdaybook.reader.ByteReader object,
     */
    ByteReader getReader();
    
    /**
     * Returns the file name. 
     * @return the file name
     */
    String getFileName();
    
    /**
     * @return the size of the resource in bytes.
     */
    long size();

    /**
     * @return The file extension.
     */
    String getExtension();
    
    /**
     * Releases resources for this resource.
     * @throws IOException if the resource could not be closed
     */
    void close() throws IOException;

    /**
     * Gets the binary source of this request. THis is useful when we want 
     * to further process the binary, e.g. treat the source as an archive and submit
     * its contents.
     * 
     * @return an InputStream which will read the binary data which formed the source
     * of this request.  
     * @return
     * @throws IOException  if there was an error reading from the binary source
     */
    InputStream getSourceInputStream() throws IOException;
    

    /**
     * This method returns a file for the bytes underlying the identification request.
     * Implementations should make a best effort to return an actual file, rather than null.
     * This may involve creating a temporary file from the source input stream, if necessary.
     * 
     * @return A source file for this identification request. 
     * @throws IOException  if there was an error reading from the binary source 
     */
    File getSourceFile() throws IOException;
    
    
    /**
     * Opens the request's input stream for reading.
     * @param in the input stream to use.
     * @throws IOException if the input stream could not be opened
     */
    void open(InputStream in) throws IOException;

    /**
     * @return the meta data.
     */
    RequestMetaData getRequestMetaData();

    /**
     * Returns an object which is used to identify the request's source and its place in a node hierarchy.
     * @return the identifier
     */
    RequestIdentifier getIdentifier();
}
