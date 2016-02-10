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
package uk.gov.nationalarchives.droid.core.interfaces;

import java.io.IOException;
import java.io.InputStream;

import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import net.byteseek.io.reader.WindowReader;

/**
 * Encapsulates an identification request.
 *
 * @author rflitcroft
 *
 * @param <T> The type of byte source to open from.
 */
public interface IdentificationRequest<T> {


    /**
     * Returns a byte at the position specified.
     * @param position the position of the byte .
     * @return the byte at that position.
     * @throws IOException if there was a problem reading the byte.
     */
    byte getByte(long position) throws IOException;

    /**
     * Returns a window reader for the byte source represented by this identification request.
     *
     * @return A window reader for the bytes represented by this identification request.
     */
    WindowReader getWindowReader();
    
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
     * Opens the request's input stream for reading.
     * @param bytesource The byte source to open from.
     * @throws IOException if the input stream could not be opened
     */
    //void open(InputStream in) throws IOException;
    void open(T bytesource) throws IOException;

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
