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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author a-mpalmer
 *
 */
public final class CachedByteArray implements CachedBytes {

    private File source;
    private final byte[] bytes;
    private int maxSize;
    
    /**
     * 
     * @param bytes A byte array containing the bytes to read.
     * @param maxSize - the number of bytes possible to read.
     * Note: this is not checked on reading bytes for
     * performance reasons.  It is only used to return
     * an appropriate input stream if requested.
     */
    public CachedByteArray(byte[] bytes, int maxSize) {
        this.bytes = bytes;
        this.maxSize = maxSize;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getSourceFile() {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getSourceInputStream() throws IOException {
        byte[] buffer;
        if (bytes.length > maxSize) {
            buffer = Arrays.copyOf(bytes, maxSize);
        } else {
            buffer = bytes;
        }
        return new ByteArrayInputStream(buffer);
        // return new ByteArrayInputStream(bytes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceFile(File sourceFile) throws FileNotFoundException {
        this.source = sourceFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte(long position) {
        return bytes[(int) position];
    }

}
