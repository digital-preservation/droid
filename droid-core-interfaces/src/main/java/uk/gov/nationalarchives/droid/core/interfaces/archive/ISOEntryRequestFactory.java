package uk.gov.nationalarchives.droid.core.interfaces.archive;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ISOImageIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

import java.io.InputStream;

/**
 * Created by rhubner on 2/13/17.
 */
public class ISOEntryRequestFactory extends AbstractArchiveRequestFactory<InputStream> {
    @Override
    public IdentificationRequest<InputStream> newRequest(RequestMetaData metaData, RequestIdentifier identifier) {


        return new ISOImageIdentificationRequest(metaData, identifier, getTempDirLocation());
    }
}
