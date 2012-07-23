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

import java.io.IOException;
import java.util.concurrent.Future;


/**
 * @author rflitcroft
 *
 */
public interface AsynchDroid {

    /**
     * Submits a job asynchronously.
     * @param request the request to submit
     * @return future task
     */
    Future<IdentificationResultCollection> submit(final IdentificationRequest request);

    /**
     * Waits until in-flight jobs have finished.
     * @throws InterruptedException if the calling thread was interrupted.
     */
    void awaitIdle() throws InterruptedException;

    /**
     * Waits until in-flight jobs have finished AND all sub-tasks have finished.
     * @throws InterruptedException if the calling thread was interrupted.
     */
    void awaitFinished() throws InterruptedException;

    /**
     * Saves the state of the droid request queue.
     */
    void save();

    /**
     * Replays any items in the droid request queue.
     * 
     * @throws IOException if the replayed files could not be read.
     */
    void replay() throws IOException;

    
    /**
     * Sets the maximum number of bytes to scan.
     * Negative values mean unlimited.
     * 
     * @param maxBytes The maximum number of bytes to scan
     * or negative, meaning unlimited.
     */
    void setMaxBytesToScan(long maxBytes);
    
    
    /** 
     * 
     * @param matchAllExtensions Whether to match all the extensions, or just ones with no other signatures defined.
     */
    void setMatchAllExtensions(boolean matchAllExtensions);
    
}
