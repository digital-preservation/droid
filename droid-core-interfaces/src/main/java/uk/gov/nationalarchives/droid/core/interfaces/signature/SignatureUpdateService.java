/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.signature;

import java.io.File;

import org.apache.commons.configuration.event.ConfigurationListener;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;

/**
 * @author rflitcroft
 *
 */
public interface SignatureUpdateService extends ConfigurationListener, ProxySubscriber {

    /**
     * Imports a signature file.
     * @param targetDir the target directory for the signature file.
     * @return the signature file's meta-data.
     * @throws SignatureServiceException if the service call failed
     */
    SignatureFileInfo importSignatureFile(File targetDir) throws SignatureServiceException;
    
    /**
     * Gets the latest version info from the signature registry via a proxy.
     * @param currentVersion the currentVersion of the signature.
     * @return latest version info.
     * @throws SignatureServiceException if the service call failed
     */
    SignatureFileInfo getLatestVersion(int currentVersion) throws SignatureServiceException;

    /**
     * Initialises from config.
     * @param config the config to initialise from
     */
    void init(DroidGlobalConfig config);

}
