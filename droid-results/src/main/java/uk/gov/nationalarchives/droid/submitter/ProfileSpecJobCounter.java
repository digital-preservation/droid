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
package uk.gov.nationalarchives.droid.submitter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.io.DirectoryWalker;

import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.ProfileSpec;

/**
 * Callable task to estimate the number of identification jobs to be run in a profile spec.
 * @author richard
 *
 */
public class ProfileSpecJobCounter implements Callable<Long> {

    /**
     * Limit of depth to progress.
     */
    public static final int PROGRESS_DEPTH_LIMIT = 4; // If set to -1, do all files and folders.

    private ProfileSpec profileSpec;
    private long count;
    private volatile boolean cancelled;

    /**
     * @param profileSpec the profile spec to size
     */
    public ProfileSpecJobCounter(ProfileSpec profileSpec) {
        this.profileSpec = profileSpec;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Long call() throws IOException {
        
        for (AbstractProfileResource resource : profileSpec.getResources()) {
            if (cancelled) {
                break;
            }
            if (resource.isDirectory()) {
                LukeFileWalker walker = new LukeFileWalker(resource.getUri(), resource.isRecursive());
                walker.walk();
            } else {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Cancels the counter.
     */
    public void cancel() {
        cancelled = true;
    }
    
    /**
     * File walker for the profile spec resources.
     * @author rflitcroft
     *
     */
    private final class LukeFileWalker extends DirectoryWalker {
        
        private java.io.File root;
        private boolean recursive;

        public LukeFileWalker(final URI root, final boolean recursive) {
            super(null, recursive ? PROGRESS_DEPTH_LIMIT : 1);
            this.recursive = recursive;
            this.root = Paths.get(root).toFile();
        }
        
        public void walk() throws IOException {
            super.walk(root, null);
        }
        
        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void handleFile(final java.io.File file, final int depth, final Collection results) {
            count++;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected boolean handleDirectory(final java.io.File directory, final int depth, final Collection results) {
            count++;
            return recursive || depth == 0;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected boolean handleIsCancelled(final java.io.File file, final int depth, final Collection results) {
            return cancelled;
        }
    }
}
