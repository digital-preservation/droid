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
