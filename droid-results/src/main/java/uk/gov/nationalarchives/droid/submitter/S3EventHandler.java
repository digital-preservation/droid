package uk.gov.nationalarchives.droid.submitter;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationErrorType;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.http.S3ClientFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.S3IdentificationRequest;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;

import java.util.Date;

public class S3EventHandler {

    private AsynchDroid droidCore;
    private SubmissionThrottle submissionThrottle;
    private ResultHandler resultHandler;
    private DroidGlobalConfig config;
    private final S3Client s3Client;

    public S3EventHandler(final AsynchDroid droidCore, SubmissionThrottle submissionThrottle, ResultHandler resultHandler, DroidGlobalConfig config) {
        this.droidCore = droidCore;
        this.submissionThrottle = submissionThrottle;
        this.resultHandler = resultHandler;
        S3ClientFactory s3ClientFactory = new S3ClientFactory(config);
        this.s3Client = s3ClientFactory.getS3Client();
    }


    public void onS3Event(AbstractProfileResource resource, ResourceId parentResource) {

        // Prepare the metadata
        // TODO find the real size - it is a pain to get it from S3 from within droid-results
        // TODO get the real modification time from S3
        RequestMetaData metaData = new RequestMetaData(-1L, new Date(0).getTime(), resource.getName());

        // Prepare the identifier
        RequestIdentifier identifier = new RequestIdentifier(resource.getUri());
        identifier.setParentResourceId(parentResource);
        // Prepare the request
        IdentificationRequest<S3Uri> request = new S3IdentificationRequest(metaData, identifier, s3Client);

        if (droidCore.passesIdentificationFilter(request)) {
            try {
                droidCore.submit(request);
                submissionThrottle.apply();
            } catch (InterruptedException e) {
                resultHandler.handleError(new IdentificationException(request, IdentificationErrorType.OTHER, e));
            }
        }
    }

    public void setSubmissionThrottle(SubmissionThrottle submissionThrottle) {
        this.submissionThrottle = submissionThrottle;
    }

    public void setDroidCore(AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    public DroidGlobalConfig getConfig() {
        return config;
    }

    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }

    public S3Client getS3Client() {
        return s3Client;
    }
}
