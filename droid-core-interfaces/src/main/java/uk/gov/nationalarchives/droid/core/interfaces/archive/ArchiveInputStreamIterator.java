/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
