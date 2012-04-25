/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * Interface for Creating new identification requests.
 * @author rflitcroft
 *
 */
public interface IdentificationRequestFactory {

    /**
     * @param metaData metadata about the request
     * @param identifier a request identifier
     * @return a new identification request
     * 
     */
    IdentificationRequest newRequest(RequestMetaData metaData, RequestIdentifier identifier);
    
}
