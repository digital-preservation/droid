/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.signature;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureUpdateService;
import uk.gov.nationalarchives.pronom.PronomService;
import uk.gov.nationalarchives.pronom.Version;

/**
 * @author rflitcroft
 * 
 */
public class PronomSignatureService implements SignatureUpdateService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private PronomService pronomService;
    private String filenamePattern;

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public SignatureFileInfo importSignatureFile(final Path targetDir) throws SignatureServiceException {
        final Element sigFile = pronomService.getSignatureFileV1().getElement();

        // get the version number, which needs to be part of the filename...
        final int version = Integer.valueOf(sigFile.getAttribute("Version"));
        final boolean deprecated = Boolean
                .valueOf(sigFile.getAttribute("Deprecated"));

        final SignatureFileInfo sigInfo = new SignatureFileInfo(version, deprecated, SignatureType.BINARY);
        final String fileName = String.format(filenamePattern, version);

        final Path outputFile = targetDir.resolve(fileName);
        try (final Writer writer = Files.newBufferedWriter(outputFile, UTF_8)) {
            final XMLSerializer serializer = new XMLSerializer(writer,
                    new OutputFormat(Method.XML, "UTF-8", true));
            serializer.serialize(sigFile);
            sigInfo.setFile(outputFile);
        } catch (final IOException e) {
            throw new SignatureServiceException(e);
        }

        return sigInfo;
    }

    /**
     * @param pronomService
     *            the pronomService to set
     */
    public void setPronomService(PronomService pronomService) {
        this.pronomService = pronomService;
    }

    /**
     * @param filenamePattern
     *            the filename pattern to set
     */
    public void setFilenamePattern(String filenamePattern) {
        this.filenamePattern = filenamePattern;

    }

    /**
     * @param currentVersion - the current version of the signature file.
     * @return a SignatureFileInfo object representing the current version on
     *         the pronom website.
     */
    @Override
    public SignatureFileInfo getLatestVersion(int currentVersion) {
        Holder<Version> version = new Holder<Version>();
        Holder<Boolean> deprecated = new Holder<Boolean>();

        pronomService.getSignatureFileVersionV1(version, deprecated);
        

        SignatureFileInfo info = new SignatureFileInfo(version.value
                .getVersion(), deprecated.value.booleanValue(), SignatureType.BINARY);
        return info;

    }

    /**
     * Sets the endpoint URL.
     * @param url the url to set
     */
    void setEndpointUrl(String url) {
        ((BindingProvider) pronomService).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                url); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt) {
        final String propertyName = evt.getPropertyName();
        if (propertyName.equals(DroidGlobalProperty.BINARY_UPDATE_URL.getName())) {
            setEndpointUrl((String) evt.getPropertyValue());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onProxyChange(ProxySettings proxySettings) {
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnection(ConnectionType.CLOSE);
        httpClientPolicy.setAllowChunking(true);
        httpClientPolicy.setCacheControl("no-cache");
        
        if (proxySettings.isEnabled()) {
            httpClientPolicy.setProxyServer(proxySettings.getProxyHost());
            httpClientPolicy.setProxyServerPort(proxySettings.getProxyPort());
            httpClientPolicy.setProxyServerType(ProxyServerType.HTTP);
        } else {
            httpClientPolicy.setProxyServer(null);
            httpClientPolicy.setProxyServerPort(null);
            httpClientPolicy.setProxyServerType(null);
        }
        
        Client client = ClientProxy.getClient(pronomService);
        
        HTTPConduit http = (HTTPConduit) client.getConduit();
        http.setClient(httpClientPolicy);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init(DroidGlobalConfig config) {
        setEndpointUrl(config.getProperties().getString(DroidGlobalProperty.BINARY_UPDATE_URL.getName()));
    }
    
}
