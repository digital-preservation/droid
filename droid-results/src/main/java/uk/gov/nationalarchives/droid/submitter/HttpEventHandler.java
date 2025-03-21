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

import uk.gov.nationalarchives.droid.core.interfaces.AsynchDroid;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.resource.HttpIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.HttpUtils;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.throttle.SubmissionThrottle;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;

public class HttpEventHandler {

    private AsynchDroid droidCore;
    private SubmissionThrottle submissionThrottle;
    private DroidGlobalConfig config;

    public HttpEventHandler(final AsynchDroid droidCore, final DroidGlobalConfig config) {
        this.droidCore = droidCore;
        this.config = config;
    }


    public void onHttpEvent(AbstractProfileResource resource) {
        HttpUtils.HttpMetadata httpMetadata = new HttpUtils(getHttpClient(resource)).getHttpMetadata(resource.getUri());
        RequestMetaData metaData = new RequestMetaData(httpMetadata.fileSize(), httpMetadata.lastModified(), resource.getUri().getPath());

        // Prepare the identifier
        RequestIdentifier identifier = new RequestIdentifier(resource.getUri());
        identifier.setParentResourceId(null);
        identifier.setResourceId(null);

        // Prepare the request
        IdentificationRequest<URI> request = new HttpIdentificationRequest(metaData, identifier, getHttpClient(resource));

        if (droidCore.passesIdentificationFilter(request)) {
            try {
                droidCore.submit(request);
                submissionThrottle.apply();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public HttpClient getHttpClient(AbstractProfileResource resource) {
        ProxyUtils proxyUtils = new ProxyUtils(this.config);
        ProxySettings proxySettings = proxyUtils.getProxySettings(resource);
        HttpClient.Builder httpBuilder = HttpClient.newBuilder();
        HttpClient httpClient;
        if (proxySettings.isEnabled()) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(proxySettings.getProxyHost(), proxySettings.getProxyPort());
            httpClient = httpBuilder.proxy(ProxySelector.of(inetSocketAddress)).build();
        } else {
            httpClient = httpBuilder.build();
        }
        return httpClient;
    }

    public void setSubmissionThrottle(SubmissionThrottle submissionThrottle) {
        this.submissionThrottle = submissionThrottle;
    }

    public void setDroidCore(AsynchDroid droidCore) {
        this.droidCore = droidCore;
    }

    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }
}
