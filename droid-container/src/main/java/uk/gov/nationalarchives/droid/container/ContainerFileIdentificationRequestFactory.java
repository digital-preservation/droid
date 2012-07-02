/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.io.File;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class ContainerFileIdentificationRequestFactory implements IdentificationRequestFactory {

    private File tempDirLocation;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final IdentificationRequest newRequest(RequestMetaData metaData, RequestIdentifier identifier) {
        return new ContainerFileIdentificationRequest(getTempDirLocation());
    }
    
    /**
     * @param tempDir the tempDir to set
     */
    public void setTempDirLocation(File tempDir) {
        this.tempDirLocation = tempDir;
    }

    public File getTempDirLocation() {
        synchronized(this) {
            if(tempDirLocation == null) {
                tempDirLocation = new File(System.getProperty("java.io.tmpdir"));
            }
        }
        return tempDirLocation;
    }
}
