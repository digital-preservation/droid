package uk.gov.nationalarchives.droid.submitter;

import software.amazon.awssdk.services.s3.S3Uri;
import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.HttpIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.S3IdentificationRequest;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;

import java.net.URI;
import java.util.Date;

public class HttpEventHandler {

    private AsynchDroid droidCore;
    private SubmissionThrottle submissionThrottle;

    public HttpEventHandler(final AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }


    public void onHttpEvent(AbstractProfileResource resource) {
        RequestMetaData metaData = new RequestMetaData(-1L, new Date(0).getTime(), resource.getUri().getPath());

        // Prepare the identifier
        RequestIdentifier identifier = new RequestIdentifier(resource.getUri());
        identifier.setParentResourceId(null);
        identifier.setResourceId(null);

        // Prepare the request
        IdentificationRequest<URI> request = new HttpIdentificationRequest(metaData, identifier);

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
