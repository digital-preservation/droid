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

/**
 * Abstract archive walker which walks archive entries.
 * @param <T> the entry type
 * @author rflitcroft
 *
 */
public abstract class ArchiveFileWalker<T> {

    /**
     * Invoked whenever an entry is encountered.
     * @param entry the entry encountered
     * @throws IOException if the archive input stream could not be read
     */
    protected abstract void handleEntry(T entry) throws IOException;
    
    /**
     * Walks the Iterable archive.
     * @param iterable an iterable archive.
     * @throws IOException if the the archive source could not be read
     */
    protected final void walk(Iterable<T> iterable) throws IOException {
        try {
            walkInternal(iterable);
        } catch (CancelException cancel) {
            handleCancelled(cancel);
        }
    }
    
    
    private void walkInternal(Iterable<T> archiveFile) throws IOException {
        for (T entry : archiveFile) {
            checkIfCancelled(entry);
            handleEntry(entry);
        }
    }
    
    /**
     * Determines if the walk has been cancelled.
     * This implementation always returns false.
     * @return true if the walk has been cancelled; false otherwise.
     */
    protected boolean isCancelled() {
        return false;
    }
    
    /**
     * Handles a cancelled walk.
     * @param entry the entry that would be processed next
     * @throws IOException if the archive source could not be read.
     */
    private void checkIfCancelled(T entry) throws IOException {
        if (isCancelled()) {
            throw new CancelException("Cancelled on entry [" + entry + "]");
        }
    }
    
    /**
     * Handles a cancelled walk.
     * @param e the exception that was thrown when the walks was cancelled
     * @throws CancelException if the cancel exception was propagated
     */
    protected void handleCancelled(CancelException e) throws CancelException {
        throw e;
    }
    
    /**
     * Thrown when a walk is cancelled.
     * @author rflitcroft
     *
     */
    public static final class CancelException extends IOException {

        private static final long serialVersionUID = -8123033733467072095L;

        /**
         * @param message the message
         */
        public CancelException(String message) {
            super(message);
        }
        
    }
    
}
