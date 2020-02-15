/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
package uk.gov.nationalarchives.droid.results.handlers;

import java.net.URI;

import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProfileResultObserver;

/**
 * @author rflitcroft
 * 
 */
public interface ProgressMonitor {

    /**
     * A static progress monitor that does no monitoring.
     */
    ProgressMonitor NO_MONITORING = new ProgressMonitor() {
    };

    /** value for indeterminate answer. */
    int INDETERMINATE_PROGRESS = -1;

    /**
     * Sets the maximum count.
     * 
     * @param count
     *            the maximum count to set
     */
    default void setTargetCount(long count) {
    }

    /**
     * Gets the progress percentage: -1 indicates indeterminate progress.
     * 
     * @return the progress as a percentage.
     * 
     */
    default int getProgressPercentage() {
        return 0;
    }

    /**
     * @return the maximum count.
     */
    default long getProfileSize() {
        return 0;
    }

    /**
     * 
     * @return the number of identifications made.
     */
    default long getIdentificationCount() {
        return 0;
    }

    /**
     * Sets an observer to fired whenever the progress percentage increments.
     * 
     * @param observer
     *            the observer to set
     */
    default void setPercentIncrementObserver(ProgressObserver observer) {
    }

    /**
     * @param uri
     *            the URI of the job.
     */
    default void startJob(URI uri) {
    }

    /**
     * @param uri
     *            the URI of the job
     */
    default void stopJob(ProfileResourceNode uri) {
    }

    /**
     * @param resultObserver ResultObserver.
     */
    default void setResultObserver(ProfileResultObserver resultObserver) {
    }

    /**
     * Initialised the progress monitor to some initial state.
     * @param targetCount the target count
     * @param currentCount the actual count
     */
    default void initialise(long targetCount, long currentCount) {
    }

    /**
     * @return the target number of identifications (100%)
     */
    default long getTargetCount() {
        return 0;
    }
    
}
