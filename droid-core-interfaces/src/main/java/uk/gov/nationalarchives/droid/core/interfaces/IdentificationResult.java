/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces;

import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;


/**
 * Encapsulates a DROID identification result.
 * @author rflitcroft
 *
 */
public interface IdentificationResult {
    
    /**
     * @return the PUID
     */
    String getPuid();
    
    /**
     * 
     * @return the external ID
     */
    String getExtId();
    
    /**
     * 
     * @return the name of the format
     */
    String getName();
    
    /**
     * 
     * @return the mime types
     */
    String getMimeType();

    
    /**
     * 
     * @return The version.
     */
    String getVersion();
    
    /**
     * 
     * @return the identification method
     */
    IdentificationMethod getMethod();
    


    /**
     * 
     * @return the request meta data.
     */
    RequestMetaData getMetaData();
    
    /**
     * @return the request identifier
     * @return
     */
    RequestIdentifier getIdentifier();

}
