/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.io.IOException;


import uk.gov.nationalarchives.droid.profile.ProfileSpec;
import uk.gov.nationalarchives.droid.results.handlers.ProgressMonitor;

/**
 * @author rflitcroft
 * 
 */
public interface ProfileSpecWalker {

    /**
     * Starts a walk.
     * @param profileSpec the profile spec to walk
     * @param walkState the state of the walk. 
     * @throws IOException any IOException 
     */
    void walk(ProfileSpec profileSpec, ProfileWalkState walkState) throws IOException;
    
    /**
     * 
     * @return the progress monitor.
     */
    ProgressMonitor getProgressMonitor();

    /**
     * 
     */
    void cancel();

    /**
     * @return the file event handler
     */
    FileEventHandler getFileEventHandler();

}
