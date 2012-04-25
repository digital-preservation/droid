/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
        
        private File root;
        private boolean recursive;

        public LukeFileWalker(URI root, boolean recursive) {
            super(null, recursive ? -1 : 1);
            this.recursive = recursive;
            this.root = new File(root);
        }
        
        public void walk() throws IOException {
            super.walk(root, null);
        }
        
        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void handleFile(File file, int depth, Collection results) {
            count++;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected boolean handleDirectory(File directory, int depth,
                Collection results) {
            count++;
            return recursive || depth == 0;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected boolean handleIsCancelled(File file, int depth, Collection results) {
            return cancelled;
        }
    }
}
