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
package uk.gov.nationalarchives.droid.signature;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ErrorCode;
import uk.gov.nationalarchives.droid.core.interfaces.signature.ProxySettings;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManagerException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureUpdateService;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 *
 */
public class SignatureManagerImpl implements SignatureManager {

    private static final String INVALID_SIGNATURE_FILE = "Invalid signature file [%s]";
    
    private static Map<SignatureType, DroidGlobalProperty> defaultVersionProperties = new HashMap<>();
    static {
        defaultVersionProperties.put(SignatureType.BINARY, DroidGlobalProperty.DEFAULT_BINARY_SIG_FILE_VERSION);
        defaultVersionProperties.put(SignatureType.CONTAINER, DroidGlobalProperty.DEFAULT_CONTAINER_SIG_FILE_VERSION);
        defaultVersionProperties.put(SignatureType.TEXT, DroidGlobalProperty.DEFAULT_TEXT_SIG_FILE_VERSION);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private DroidGlobalConfig config;
    private Map<SignatureType, SignatureUpdateService> signatureUpdateServices;
    private ProxySettings proxySettings = new ProxySettings();

    /**
     * Empty bean constructor.
     */
    public SignatureManagerImpl() {
    }

    /**
     * Parameterized constructor.
     * @param config The droid global config.
     * @param updateServices The update services to use.
     */
    public SignatureManagerImpl(DroidGlobalConfig config, Map<SignatureType, SignatureUpdateService> updateServices) {
        setConfig(config);
        setSignatureUpdateServices(updateServices);
    }

    /**
     * Initailisation post-construct.
     */
    public void init() {
        config.getProperties().addConfigurationListener(proxySettings);
        Configuration configuration = config.getProperties();
        
        proxySettings = new ProxySettings();
        proxySettings.setEnabled(configuration.getBoolean(DroidGlobalProperty.UPDATE_USE_PROXY.getName()));
        proxySettings.setProxyHost(configuration.getString(DroidGlobalProperty.UPDATE_PROXY_HOST.getName()));
        proxySettings.setProxyPort(configuration.getInt(DroidGlobalProperty.UPDATE_PROXY_PORT.getName()));
        proxySettings.setEnabled(configuration.getBoolean(DroidGlobalProperty.UPDATE_USE_PROXY.getName()));
        
        config.getProperties().addConfigurationListener(proxySettings);
        
        for (SignatureUpdateService subscriber : signatureUpdateServices.values()) {
            subscriber.init(config);
            proxySettings.addProxySubscriber(subscriber);
            config.getProperties().addConfigurationListener(subscriber);
        }
        
        proxySettings.notifyProxySubscribers();
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public Map<SignatureType, SortedMap<String, SignatureFileInfo>> getAvailableSignatureFiles() {
        
        final Path binSigFileDir = config.getSignatureFileDir();
        final Path containerSigFileDir = config.getContainerSignatureDir();
        //File textSigFileDir = config.getTextSignatureFileDir();
        
        final DirectoryStream.Filter<Path> xmlExtensionFilter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(final Path path) {
                return (!Files.isDirectory(path)) && FileUtil.fileName(path).endsWith(".xml");
            }
        };
        
        final Map<SignatureType, SortedMap<String, SignatureFileInfo>> availableSigFiles =
            new HashMap<>();
        
        SignatureInfoParser parser = new SignatureInfoParser();
        
        final String errorMessagePattern = "Unreadable signature file [%s]";
        
        final SortedMap<String, SignatureFileInfo> binSigFiles = new TreeMap<>();
        for (final Path file : FileUtil.listFilesQuietly(binSigFileDir, false, xmlExtensionFilter)) {
            String fileName = FilenameUtils.getBaseName(FileUtil.fileName(file));
            try {
                binSigFiles.put(fileName, forBinarySigFile(file, parser));
            } catch (SignatureFileException e) {
                log.warn(String.format(errorMessagePattern, file));
            }
        }
        
        final SortedMap<String, SignatureFileInfo> containerSigFiles = new TreeMap<>();
        for (final Path file : FileUtil.listFilesQuietly(containerSigFileDir, false, xmlExtensionFilter)) {
            String fileName = FilenameUtils.getBaseName(FileUtil.fileName(file));
            try {
                containerSigFiles.put(fileName, forSimpleVersionedFile(file, SignatureType.CONTAINER));
            } catch (SignatureFileException e) {
                log.warn(String.format(errorMessagePattern, file));
            }
        }
        
        /*
        SortedMap<String, SignatureFileInfo> textSigFiles = new TreeMap<String, SignatureFileInfo>();
        for (File file : textSigFileDir.listFiles(xmlExtensionFilter)) {
            String fileName = FilenameUtils.getBaseName(file.getName());
            try {
                textSigFiles.put(fileName, forSimpleVersionedFile(file, SignatureType.TEXT));
            } catch (SignatureFileException e) {
                log.warn(String.format(errorMessagePattern, file));
            }
        }
        */
        
        availableSigFiles.put(SignatureType.BINARY, binSigFiles);
        availableSigFiles.put(SignatureType.CONTAINER, containerSigFiles);
        //availableSigFiles.put(SignatureType.TEXT, textSigFiles);

        return availableSigFiles;
    }
    
    private static SignatureFileInfo forBinarySigFile(final Path file, final SignatureInfoParser parser)
            throws SignatureFileException {
        final SignatureFileInfo signatureFileInfo = parser.parse(file);
        signatureFileInfo.setFile(file);
        return signatureFileInfo;
    }

    private static SignatureFileInfo forSimpleVersionedFile(final Path file, final SignatureType type)
        throws SignatureFileException {
        // parse the version from the filename
        final String filename = FilenameUtils.getBaseName(FileUtil.fileName(file));
        try {
            final int version = Integer.valueOf(StringUtils.substringAfterLast(filename, "-"));
            final SignatureFileInfo signatureFileInfo = new SignatureFileInfo(version, false, type);
            signatureFileInfo.setFile(file);
            return signatureFileInfo;
        } catch (final NumberFormatException e) {
            final String message = String.format("Invalid signature filename [%s]", FileUtil.fileName(file));
            throw new SignatureFileException(message, e, ErrorCode.INVALID_SIGNATURE_FILE);
        }
    }
    
    /**
     * @param config the config to set
     */
    public void setConfig(DroidGlobalConfig config) {
        this.config = config;
    }
    
    private static final class SignatureInfoParser {

        SignatureFileInfo parse(final Path sigFile) throws SignatureFileException {
        
            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            final SignatureFileVersionHandler handler = new SignatureFileVersionHandler();
            try {
                final SAXParser saxParser = saxParserFactory.newSAXParser();
                saxParser.parse(sigFile.toFile(), handler);
                throw new SignatureFileException(String.format(
                        INVALID_SIGNATURE_FILE, sigFile), ErrorCode.INVALID_SIGNATURE_FILE);
            } catch (final ValidSignatureFileException e) {
                return e.getInfo();
            } catch (final SAXException e) {
                throw new SignatureFileException(String.format(
                        INVALID_SIGNATURE_FILE, sigFile), e,
                        ErrorCode.INVALID_SIGNATURE_FILE);
            } catch (final ParserConfigurationException | IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
    
    /**
     * Handler for &lt;FFSignatureFile&gt; elements.
     * 
     */
    private static final class SignatureFileVersionHandler extends DefaultHandler {

        private static final String ROOT_ELEMENT = "FFSignatureFile";

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {

            if (ROOT_ELEMENT.equals(qName)) {
                int version = Integer.valueOf(attributes.getValue("Version"));
                SignatureFileInfo info = new SignatureFileInfo(version, false, SignatureType.BINARY);
                throw new ValidSignatureFileException(info);
            }
            
            throw new SAXException(
                    String.format("Invalid signature file - root element was not [%s]", ROOT_ELEMENT));
        }
        
    }
    
    /**
     * Exception thrown purely to stop SAX parsing when a valid signature file is found.
     * @author rflitcroft
     *
     */
    private static final class ValidSignatureFileException extends RuntimeException {

        private static final long serialVersionUID = 5955330716555328779L;
        private final SignatureFileInfo info;
        
        /**
         * @param info the signature file info
         */
        ValidSignatureFileException(SignatureFileInfo info) {
            this.info = info;
        }
        
        /**
         * @return the info
         */
        public SignatureFileInfo getInfo() {
            return info;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<SignatureType, SignatureFileInfo> getLatestSignatureFiles() {
        
        final Configuration properties = config.getProperties();
        properties.setProperty(DroidGlobalProperty.LAST_UPDATE_CHECK.getName(), System.currentTimeMillis());

        Map<SignatureType, SignatureFileInfo> latestSigFiles = new HashMap<SignatureType, SignatureFileInfo>();

        final Map<SignatureType, SortedMap<String, SignatureFileInfo>> availableSignatureFiles = 
            getAvailableSignatureFiles();
        
        for (Map.Entry<SignatureType, SignatureUpdateService> entry : signatureUpdateServices.entrySet()) {
            SignatureType type = entry.getKey();
            SignatureUpdateService updateService = entry.getValue();
            final Map<String, SignatureFileInfo> signaturesForType = availableSignatureFiles.get(type);
            try {
                int latestVersionForType = getLatestVersionForType(type, signaturesForType);
                SignatureFileInfo latestUpdate = updateService.getLatestVersion(latestVersionForType);
                if (latestUpdate != null
                    && latestUpdate.getVersion() > 0
                    && !signaturesForType.containsValue(latestUpdate)) {
                    latestSigFiles.put(type, latestUpdate);
                }
            } catch (SignatureServiceException e) {
                latestSigFiles.put(type, new SignatureFileInfo(e));
            }
        }
            
        return latestSigFiles;
    }
    
    private int getLatestVersionForType(SignatureType type, Map<String, SignatureFileInfo> signatures) {
        int result = 0;
        for (String key : signatures.keySet()) {
            SignatureFileInfo info = signatures.get(key);
            if (info.getVersion() > result) {
                result = info.getVersion();
            }
        }
        return result;
    }
    
    /**
     * @param signatureUpdateServices the signatureUpdateServices to set
     */
    public void setSignatureUpdateServices(Map<SignatureType, SignatureUpdateService> signatureUpdateServices) {
        this.signatureUpdateServices = signatureUpdateServices;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SignatureFileInfo downloadLatest(final SignatureType type) throws SignatureManagerException {
        final Path sigFileDir;
        switch (type) {
            case BINARY:
                sigFileDir = config.getSignatureFileDir();
                break;
            case CONTAINER:
                sigFileDir = config.getContainerSignatureDir();
                break;
            case TEXT:
                sigFileDir = config.getTextSignatureFileDir();
                break;
            default:
                throw new IllegalArgumentException("Invalid signature type : " + type);
        }
        
        
        try {
            SignatureFileInfo info = signatureUpdateServices.get(type).importSignatureFile(sigFileDir);
            final boolean autoSetDefaultSigFile = config.getBooleanProperty(DroidGlobalProperty.UPDATE_AUTOSET_DEFAULT);
            if (autoSetDefaultSigFile) {
                PropertiesConfiguration props = config.getProperties();
                props.setProperty(defaultVersionProperties.get(type).getName(), 
                        FilenameUtils.getBaseName(FileUtil.fileName(info.getFile())));
                try {
                    config.getProperties().save();
                } catch (ConfigurationException e) {
                    log.error(e.getMessage(), e);
                    throw new SignatureManagerException(e);
                }
            }
            return info;
        } catch (SignatureServiceException e) {
            throw new SignatureManagerException(e);
        }
    }
    
    /**
     * {@inheritDoc}
     * @throws SignatureFileException on failure to retrieve signatures
     */
    @Override
    public Map<SignatureType, SignatureFileInfo> getDefaultSignatures() throws SignatureFileException {
        
        Map<SignatureType, SignatureFileInfo> defaultSignatures = new HashMap<SignatureType, SignatureFileInfo>();
        
        final Map<SignatureType, SortedMap<String, SignatureFileInfo>> 
            availableSignatureFiles = getAvailableSignatureFiles();
        
        final String errorMessagePattern = 
            "Default signature file %s could not be found. Please check your signature settings.";

        for (Map.Entry<SignatureType, SortedMap<String, SignatureFileInfo>> sigFileEntry
                : availableSignatureFiles.entrySet()) {
            SignatureType type = sigFileEntry.getKey();
            Map<String, SignatureFileInfo> sigs = sigFileEntry.getValue();
            DroidGlobalProperty defaultSigFile = defaultVersionProperties.get(type);
            
            String defaultSigFileKey = config.getProperties().getString(defaultSigFile.getName());
            if (StringUtils.isNotBlank(defaultSigFileKey)) {
                SignatureFileInfo sigFileInfo = sigs.get(defaultSigFileKey);
                if (sigFileInfo == null) {
                    String errorMessage = String.format(errorMessagePattern, config.getProperties()
                            .getString(defaultVersionProperties.get(type).getName()));
                    throw new SignatureFileException(errorMessage, ErrorCode.FILE_NOT_FOUND);
                }
                defaultSignatures.put(type, sigFileInfo);
            }
        }

        return defaultSignatures;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SignatureFileInfo install(final SignatureType type, final Path signatureFile, final boolean setDefault)
            throws SignatureFileException {
        final SignatureInfoParser parser = new SignatureInfoParser();
        final SignatureFileInfo sigFileInfo = forBinarySigFile(signatureFile, parser);

        try {
            final Path newSignatureFile = Files.copy(signatureFile,
                    Paths.get(config.getSignatureFileDir().toString(), signatureFile.getFileName().toString()));
            sigFileInfo.setFile(newSignatureFile);
            
            if (setDefault) {
                config.getProperties().setProperty(defaultVersionProperties.get(type).getName(), 
                        FilenameUtils.getBaseName(FileUtil.fileName(newSignatureFile)));
            }

            return sigFileInfo;
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new SignatureFileException(e.getMessage(), e, ErrorCode.FILE_NOT_FOUND);
        }
    }
    
}
