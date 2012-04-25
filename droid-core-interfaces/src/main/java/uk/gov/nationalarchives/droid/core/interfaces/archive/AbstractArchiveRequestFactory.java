/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.File;

/**
 * @author rflitcroft
 *
 */
public abstract class AbstractArchiveRequestFactory implements IdentificationRequestFactory {
 
    private File tempDirLocation;
    
    /**
     * @param tempDirLocation the tempDirLocation to set
     */
    public final void setTempDirLocation(File tempDirLocation) {
        this.tempDirLocation = tempDirLocation;
    }
    
    /**
     * @return the tempDirLocation
     */
    protected File getTempDirLocation() {
        return tempDirLocation;
    }
    
}
