package uk.gov.nationalarchives.droid.submitter;

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.S3IdentificationRequest;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;

import java.net.URI;
import java.util.Date;

public class S3EventHandler {

    private AsynchDroid droidCore;
    private SubmissionThrottle submissionThrottle;

    public S3EventHandler(final AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }


    public void onS3Event(AbstractProfileResource resource) {
        // Prepare the metadata
        // TODO find the real size - it is a pain to get it from S3 from within droid-results
        // TODO get the real modification time from S3
        RequestMetaData metaData = new RequestMetaData(-1L, new Date(0).getTime(), resource.getName());

        // Prepare the identifier
        RequestIdentifier identifier = new RequestIdentifier(resource.getUri());
        identifier.setParentResourceId(null);
        identifier.setResourceId(null);

        // Prepare the request
        IdentificationRequest<URI> request = new S3IdentificationRequest(metaData, identifier);

        // For now, don't filter out any requests
        if (droidCore.passesIdentificationFilter(request)) {
            try {
                droidCore.submit(request);
                submissionThrottle.apply();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setSubmissionThrottle(SubmissionThrottle submissionThrottle) {
        this.submissionThrottle = submissionThrottle;
    }

    public void setDroidCore(AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }
}
