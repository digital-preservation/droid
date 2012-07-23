/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.container.httpservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureUpdateService;

/**
 * @author rflitcroft
 *
 */
public class ContainerSignatureHttpService implements SignatureUpdateService {

    /**
     * 
     */
    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final String LAST_MODIFIED_HEADER = "Last-Modified";
    private static final String FILENAME_PATTERN = "container-signature-%s.xml";
    private static final String ERROR_MESSAGE_PATTERN = "The web server could not serve the signature file "
        + "from the address\n[%s]\nThe server gave the response [%s]";
    private static final String COULD_NOT_FIND_SERVER = "Could not contact the signature web server at\n%s";
    private static final String FILE_NOT_FOUND_404 = "The signature file was not found on the signature web"
        + "server at\n[%s]"; 
    
    private final Log log = LogFactory.getLog(getClass());

    private String endpointUrl;
    private HttpClient client = new HttpClient();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SignatureFileInfo getLatestVersion(int currentVersion) throws SignatureServiceException {
        GetMethod get = new GetMethod(endpointUrl);
        try {
            Date versionDate = getDateFromVersion(currentVersion);
            String dateString = DateUtil.formatDate(versionDate);
            get.addRequestHeader("If-Modified-Since", dateString);
            
            int statusCode = client.executeMethod(get);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new SignatureServiceException(
                        String.format(FILE_NOT_FOUND_404, endpointUrl));
            } else if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NOT_MODIFIED) {
                throw new SignatureServiceException(
                        String.format(ERROR_MESSAGE_PATTERN, endpointUrl, statusCode));
            }
            int version = getVersion(get);
            SignatureFileInfo signatureFileInfo = new SignatureFileInfo(version, false, SignatureType.CONTAINER);
            return signatureFileInfo;
        } catch (UnknownHostException e) {
            throw new SignatureServiceException(
                    String.format(COULD_NOT_FIND_SERVER, endpointUrl));
        } catch (IOException e) {
            throw new SignatureServiceException(e);
        } catch (DateParseException e) {
            throw new SignatureServiceException(e);
        } catch (ParseException e) {
            throw new SignatureServiceException(e);
        } finally {
            get.releaseConnection();
        }
    }
    
    private static int getVersion(HttpMethod httpMethod) throws DateParseException {
        int version = 0;
        Header header = httpMethod.getResponseHeader(LAST_MODIFIED_HEADER);
        if (header != null) {
            String lastModified = header.getValue();
            Date lastModifiedDate = DateUtil.parseDate(lastModified);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            version = Integer.parseInt(sdf.format(lastModifiedDate));
        }
        return version;
    }
    
    private static Date getDateFromVersion(int versionNumber) throws ParseException {
        String versionString = Integer.toString(versionNumber);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.parse(versionString);
    }
    
    /**
     * {@inheritDoc}
     * @throws SignatureServiceException 
     */
    @Override
    public SignatureFileInfo importSignatureFile(File targetDir) throws SignatureServiceException {
        GetMethod get = new GetMethod(endpointUrl);

        FileWriter writer = null;
        
        try {
            int statusCode = client.executeMethod(get);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new SignatureServiceException(
                        String.format(FILE_NOT_FOUND_404, endpointUrl));
            } else if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NOT_MODIFIED) {
                throw new SignatureServiceException(
                        String.format(ERROR_MESSAGE_PATTERN, endpointUrl, statusCode));
            }
            
            int version = getVersion(get);
            
            SignatureFileInfo signatureFileInfo = new SignatureFileInfo(version, false, SignatureType.CONTAINER);
            String fileName = String.format(FILENAME_PATTERN, version);

            final File targetFile = new File(targetDir, fileName);
            writer = new FileWriter(targetFile);
            IOUtils.copy(get.getResponseBodyAsStream(), writer);
            
            signatureFileInfo.setFile(targetFile);
            return signatureFileInfo;
            
        } catch (UnknownHostException e) {
            throw new SignatureServiceException(
                    String.format(COULD_NOT_FIND_SERVER, endpointUrl));
        } catch (IOException e) {
            throw new SignatureServiceException(e);
        } catch (DateParseException e) {
            throw new SignatureServiceException(e);
        } finally {
            get.releaseConnection();
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error("Error closing file writer", e);
            }
        }
        
    }
    
    /**
     * Sets the endpoint URL.
     * @param url the URL to set
     */
    void setEndpointUrl(String url) {
        this.endpointUrl = url;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void configurationChanged(ConfigurationEvent evt) {
        final String propertyName = evt.getPropertyName();
        if (propertyName.equals(DroidGlobalProperty.CONTAINER_UPDATE_URL.getName())) {
            setEndpointUrl((String) evt.getPropertyValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProxyChange(ProxySettings proxySettings) {
        
        if (proxySettings.isEnabled()) {
            client = new HttpClient();
            client.getHostConfiguration().setProxy(proxySettings.getProxyHost(), proxySettings.getProxyPort());
        } else {
            client = new HttpClient();
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init(DroidGlobalConfig config) {
        setEndpointUrl(config.getProperties().getString(DroidGlobalProperty.CONTAINER_UPDATE_URL.getName()));
    }
}
