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
import uk.gov.nationalarchives.droid.core.interfaces.resource.TarEntryIdentificationRequest;

/**
 * @author rflitcroft
 *
 */
public class TarEntryRequestFactory extends AbstractArchiveRequestFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public final IdentificationRequest newRequest(RequestMetaData metaData, RequestIdentifier identifier) {
        final IdentificationRequest request =
            new TarEntryIdentificationRequest(metaData, identifier, getTempDirLocation());
        
        return request;
    }

}
