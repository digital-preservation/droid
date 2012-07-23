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
package uk.gov.nationalarchives.droid.profile;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author rflitcroft
 *
 */
public enum ProfileState {
    
    /** @return The profile is running. */
    RUNNING(true) { @Override 
        ProfileState[] nextStates() {
            return new ProfileState[] {STOPPED, FINISHED};
        }
    },
    
    /** @return The profile has been stopped. */
    STOPPED(false, true) { @Override ProfileState[] nextStates() {
            return new ProfileState[] {RUNNING, SAVING, FINISHED};
        }
    },
    
    /** @return The profile is saving to disk. */
    SAVING(true) { @Override ProfileState[] nextStates() {
            return new ProfileState[] {VIRGIN, STOPPED, FINISHED};
        }
    },
    
    /** @return The profile is loading from disk. */
    LOADING(true) { @Override ProfileState[] nextStates() {
            return new ProfileState[] {VIRGIN, STOPPED, FINISHED};
        }
    },
    
    /** @return The profile is being initialised. */
    INITIALISING(true) { @Override ProfileState[] nextStates() {
            return new ProfileState[] {VIRGIN, STOPPED, FINISHED};
        }
    }, 
    
    /** @return The profile has never been run. */
    VIRGIN(false) { @Override ProfileState[] nextStates() {
            return new ProfileState[] {RUNNING, SAVING};
        }
    },

    /** @return The profile has finished. */
    FINISHED(false, true) { @Override ProfileState[] nextStates() {
            return new ProfileState[] {SAVING};
        }
    }; 

    private boolean isTransient;
    private boolean reportable;
    
    private ProfileState(boolean isTransient) {
        this.isTransient = isTransient;
    }
    
    private ProfileState(boolean isTransient, boolean reportable) {
        this.isTransient = isTransient;
        this.reportable = reportable;
    }

    /**
     * @return the valid next states for the current state.
     */
    abstract ProfileState[] nextStates();
    
    /**
     * @return the valid next states for the current state.
     */
    public Collection<ProfileState> allowedNextStates() {
        return Arrays.asList(nextStates());
    }
    
    /**
     * @return True if this is a transient state, false otherwise.
     * @return
     */
    public boolean isTransient() {
        return isTransient;
    }
    
    /**
     * 
     * @return true if theis profile state allows reporting; false otherwise.
     */
    public boolean isReportable() {
        return reportable;
    }
    
}
