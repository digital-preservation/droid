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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstrat Iterator for iterating over an input stream.
 * @author rflitcroft
 * 
 * @param <T> the type returned by next().
 * @param <U> the type of InputStream to iterate over
 *
 */
public abstract class ArchiveInputStreamIterator<T, U extends InputStream> implements Iterator<T> {

    private final U in;
    private T next;
    
    /**
     * @param in the input stream to iterate over.
     */
    protected ArchiveInputStreamIterator(U in) {
        this.in = in;
    }
    
    /**
     * {@inheritDoc}
     * @throws ArchiveIterationException if it cannot read from the stream (RuntimeException) 
     */
    @Override
    public final boolean hasNext() {
        if (next == null) {
            try {
                next = getNextEntry(in);
            } catch (IOException e) {
                throw new ArchiveIterationException(e.getMessage(), e);
            }
        }
        
        return next != null;
    }
    
    /**
     * Accessor for the underlying input stream.
     * @return the underlying input stream.
     */
    protected final U getInputStream() {
        return in;
    }
    
    /**
     * Gets the next entry from the stream given.
     * @param stream the stream
     * @return the next archived entry
     * @throws IOException if the stream could not be read
     */
    protected abstract T getNextEntry(U stream) throws IOException;

    /**
     * {@inheritDoc}
     * @throws ArchiveIterationException if it cannot read from the stream (RuntimeException)
     */
    @Override
    public final T next() {
        T nextEntry;
        
        if (next != null) {
            nextEntry = next;
            next = null;
        } else {
            try {
                nextEntry = getNextEntry(in);
            } catch (IOException e) {
                throw new ArchiveIterationException(e.getMessage(), e);
            }
        }
        
        if (nextEntry == null) {
            throw new NoSuchElementException();
        }
        
        return nextEntry;
    }

    /**
     * Not implemented.
     */
    @Override
    public final void remove() {
        throw new UnsupportedOperationException("Remove is not supported.");
    }
   
}
