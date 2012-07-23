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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.domesdaybook.reader.ByteReader;

/**
 * An interface for objects which can cache bytes from an
 * input stream.  It the extends {@link ByteReader} interface
 * to provide access to the bytes cached in it. 
 * 
 * @author Matt Palmer
 *
 */
public interface CachedBytes extends ByteReader {

    
    /**
     * Sets the optional Random Access File for the whole binary.
     * @param sourceFile the binary data source.
     * @throws IOException if the source file was not found or could not close previous file.
     */
    void setSourceFile(File sourceFile) throws IOException;

    /**
     * Closes the internal Random Access File.
     * @throws IOException if the file could not be closed.
     */
    void close() throws IOException;

    /**
     * @return the source input stream
     * @throws IOException if there was an exception reading the source
     */
    InputStream getSourceInputStream() throws IOException;

    /**
     * Returns a source file (if any) for this cached binary.
     * If the file size is less than the size of a single cache block, 
     * the source file may not be set, and this method will return null.
     *  
     * @return The source file, or null if not set.
     */
    File getSourceFile();

}
