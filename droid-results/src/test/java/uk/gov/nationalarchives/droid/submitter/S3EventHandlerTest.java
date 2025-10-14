/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.submitter;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.http.S3ClientFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.resource.S3IdentificationRequest;
import uk.gov.nationalarchives.droid.profile.S3ProfileResource;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3EventHandlerTest {
    @Test
    public void testS3EventHandlerProvidesCorrectMetadata() {
        ArgumentCaptor<IdentificationRequest<S3Uri>> droidCoreCaptor = ArgumentCaptor.forClass(IdentificationRequest.class);
        S3EventHandler s3EventHandler = getS3EventHandler(droidCoreCaptor, 100, true);
        S3ProfileResource s3ProfileResource = new S3ProfileResource("s3://bucket/object");

        s3EventHandler.onS3Event(s3ProfileResource, null);
        List<IdentificationRequest<S3Uri>> allValues = droidCoreCaptor.getAllValues();

        RequestMetaData requestMetaData = allValues.getFirst().getRequestMetaData();
        assertEquals(Long.valueOf(1), requestMetaData.getSize());
        assertEquals(Long.valueOf(0), requestMetaData.getTime());
        assertEquals("object", requestMetaData.getName());
    }

    @Test
    public void testS3EventHandlerUsesCorrectWindowSizeForSmallMaxBytes() {
        ArgumentCaptor<IdentificationRequest<S3Uri>> droidCoreCaptor = ArgumentCaptor.forClass(IdentificationRequest.class);
        S3EventHandler s3EventHandler = getS3EventHandler(droidCoreCaptor, 100, true);
        S3ProfileResource s3ProfileResource = new S3ProfileResource("s3://bucket/object");

        s3EventHandler.onS3Event(s3ProfileResource, null);
        List<IdentificationRequest<S3Uri>> allValues = droidCoreCaptor.getAllValues();

        assertEquals(100, ((S3IdentificationRequest) allValues.getFirst()).getWindowSize());
    }

    @Test
    public void testS3EventHandlerUsesCorrectWindowSizeForLargeMaxBytes() {
        ArgumentCaptor<IdentificationRequest<S3Uri>> droidCoreCaptor = ArgumentCaptor.forClass(IdentificationRequest.class);
        S3EventHandler s3EventHandler = getS3EventHandler(droidCoreCaptor, 100 * 1024 * 1024, true);
        S3ProfileResource s3ProfileResource = new S3ProfileResource("s3://bucket/object");

        s3EventHandler.onS3Event(s3ProfileResource, null);
        List<IdentificationRequest<S3Uri>> allValues = droidCoreCaptor.getAllValues();

        assertEquals(4 * 1024 * 1024, ((S3IdentificationRequest) allValues.getFirst()).getWindowSize());
    }

    @Test
    public void testS3EventHandlerUsesCorrectWindowSizeForInfiniteBytes() {
        ArgumentCaptor<IdentificationRequest<S3Uri>> droidCoreCaptor = ArgumentCaptor.forClass(IdentificationRequest.class);
        S3EventHandler s3EventHandler = getS3EventHandler(droidCoreCaptor, 100 * 1024 * 1024, true);
        S3ProfileResource s3ProfileResource = new S3ProfileResource("s3://bucket/object");

        s3EventHandler.onS3Event(s3ProfileResource, null);
        List<IdentificationRequest<S3Uri>> allValues = droidCoreCaptor.getAllValues();

        assertEquals(4 * 1024 * 1024, ((S3IdentificationRequest) allValues.getFirst()).getWindowSize());
    }

    @Test
    public void testS3EventHandlerDoesNotSubmitIfItDoesNotPassFilter() {
        ArgumentCaptor<IdentificationRequest<S3Uri>> droidCoreCaptor = ArgumentCaptor.forClass(IdentificationRequest.class);
        S3EventHandler s3EventHandler = getS3EventHandler(droidCoreCaptor, 100 * 1024 * 1024, false);
        S3ProfileResource s3ProfileResource = new S3ProfileResource("s3://bucket/object");

        s3EventHandler.onS3Event(s3ProfileResource, null);
        List<IdentificationRequest<S3Uri>> allValues = droidCoreCaptor.getAllValues();

        assertEquals(0, allValues.size());
    }

    private static S3EventHandler getS3EventHandler(ArgumentCaptor<IdentificationRequest<S3Uri>> droidCoreCaptor, int maxBytesToScan, boolean passesIdentification) {
        AsynchDroid droidCore = mock(AsynchDroid.class);
        SubmissionThrottle submissionThrottle = mock(SubmissionThrottle.class);
        ResultHandler resultHandler = mock(ResultHandler.class);
        DroidGlobalConfig droidGlobalConfig = mock(DroidGlobalConfig.class);
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();

        when(droidCore.passesIdentificationFilter(any())).thenReturn(passesIdentification);

        when(droidCore.submit(droidCoreCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
        try {
            propertiesConfiguration.read(new StringReader("profile.maxBytesToScan=" + maxBytesToScan + "\nupdate.proxy=false"));
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
        when(droidGlobalConfig.getProperties()).thenReturn(propertiesConfiguration);

        S3ClientFactory s3ClientFactory = mock(S3ClientFactory.class);
        S3Client s3Client = mock(S3Client.class);
        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder().contentLength(1L).lastModified(Instant.EPOCH).build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);
        when(s3ClientFactory.getS3Client()).thenReturn(s3Client);
        return new S3EventHandler(droidCore, submissionThrottle, resultHandler, droidGlobalConfig, s3ClientFactory);
    }
}
