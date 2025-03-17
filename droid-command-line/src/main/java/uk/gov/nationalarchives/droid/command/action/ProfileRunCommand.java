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
package uk.gov.nationalarchives.droid.command.action;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.configuration.PropertiesConfiguration;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOutputOptions;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceFactory;
import uk.gov.nationalarchives.droid.profile.ProfileManagerException;
import uk.gov.nationalarchives.droid.profile.ProfileState;
import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * A command which identifies files and either stores them in a database, or writes them out to a file or the console.
 *
 * @author rflitcroft, mpalmer
 *
 */
public class ProfileRunCommand implements DroidCommand {

    private static final int SLEEP_TIME = 1000;

    private String destination;
    private String[] resources;
    private boolean recursive;
    private ProfileManager profileManager;
    private SignatureManager signatureManager;
    private ProfileResourceFactory profileResourceFactory;
    private PropertiesConfiguration propertyOverrides;
    private String binarySignaturesFileName;
    private String containerSignaturesFileName;
    private Filter resultsFilter;
    private Filter identificationFilter;

    @Override
    public void execute() throws CommandExecutionException {
        try {
            Map<SignatureType, SignatureFileInfo> sigs = getSignatureFiles();
            ProfileInstance profile = profileManager.createProfile(sigs, propertyOverrides);

            profile.setResultsFilter(resultsFilter);
            profile.setIdentificationFilter(identificationFilter);
            profile.changeState(ProfileState.VIRGIN);

            for (String resource : resources) {
                profile.addResource(getProfileResourceFactory().getResource(resource, recursive));
            }
            profileManager.setProgressObserver(profile.getUuid(), null);
            Future<?> future = profileManager.start(profile.getUuid());
            future.get();
            
            ProgressObserver progressCallback = new ProgressObserver() {
                @Override
                public void onProgress(Integer progress) {
                }
            };

            Thread.sleep(SLEEP_TIME);

            profileManager.save(profile.getUuid(), Paths.get(destination), progressCallback);
            profileManager.closeProfile(profile.getUuid());
            terminateJsonArray();
        } catch (ProfileManagerException | InterruptedException | IOException | SignatureFileException e) {
            throw new CommandExecutionException(e);
        } catch (ExecutionException e) {
            throw new CommandExecutionException(e.getCause());
        }

    }

    public Writer getWriter(String outputFilePath) throws IOException {
        if (outputFilePath != null && !outputFilePath.trim().isEmpty() && !outputFilePath.trim().equals("stdout")) {
            File outputFile = new File(outputFilePath);
            return new FileWriter(outputFile, true);
        }
        return new PrintWriter(System.out);
    }

    /**
     * If json output is configured, we need to close the json array after the profile has finished running.
     * This writes the closing square bracket to either the console or the file depending on which is configured.
     * @throws IOException if there is a problem opening the output file or writing to the writer.
     */
    public void terminateJsonArray() throws IOException {
        if (getProperties() != null && getProperties().getString(DroidGlobalProperty.EXPORT_OUTPUT_OPTIONS.getName()).equals(ExportOutputOptions.JSON_OUTPUT.name())) {
            String outputFilePath = getProperties().getString(DroidGlobalProperty.OUTPUT_FILE_PATH.getName());
            Writer writer = getWriter(outputFilePath);
            writer.write("]");
            writer.flush();
        }
    }

    /**
     * @return The default binary and container signatures, but will override with different ones if provided.
     * @throws SignatureFileException if there's a problem obtaining the signature files.
     */
    private Map<SignatureType, SignatureFileInfo> getSignatureFiles() throws SignatureFileException {
        Map<SignatureType, SignatureFileInfo> sigs = signatureManager.getDefaultSignatures();
        if (binarySignaturesFileName != null) {
            SignatureFileInfo binInfo = new SignatureFileInfo(0, false, SignatureType.BINARY);
            binInfo.setFile(Paths.get(binarySignaturesFileName));
            sigs.put(SignatureType.BINARY, binInfo);
        }
        if (containerSignaturesFileName != null && !containerSignaturesFileName.isEmpty()) {
            SignatureFileInfo contInfo = new SignatureFileInfo(0, false, SignatureType.CONTAINER);
            contInfo.setFile(Paths.get(containerSignaturesFileName));
            sigs.put(SignatureType.CONTAINER, contInfo);
        }
        return sigs;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return The destination file of the profile (either a profile or a csv file or blank/stdout for console).
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param resources the resources to set
     */
    public void setResources(String[] resources) {
        this.resources = resources;
    }

    /**
     * @return The resources to profile.
     */
    public String[] getResources() {
        return resources;
    }
    
    /**
     * @param profileManager the profileManager to set
     */
    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }
    
    /**
     * @param signatureManager the signatureManager to set
     */
    public void setSignatureManager(SignatureManager signatureManager) {
        this.signatureManager = signatureManager;
    }

    /**
     * @param recursive the recursive to set
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * @return whether the profile recursively looks inside sub folders.
     */
    public boolean getRecursive() {
        return recursive;
    }

    /**
     * @param profileResourceFactory the profileResourceFactory to set
     */
    public void setProfileResourceFactory(ProfileResourceFactory profileResourceFactory) {
        this.profileResourceFactory = profileResourceFactory;
    }

    /**
     * @param properties properties which should be used to override global default profile properties.
     */
    public void setProperties(PropertiesConfiguration properties) {
        this.propertyOverrides = properties;
    }

    /**
     * @return properties used to override profile defaults.
     */
    public PropertiesConfiguration getProperties() {
        return propertyOverrides;
    }

    /**
     * Set the signature file.
     *
     * @param signatureFile The signature file
     */
    public void setSignatureFile(final String signatureFile) {
        this.binarySignaturesFileName = signatureFile;
    }

    /**
     * Sets a filter to use to filter out results from CSV output.
     * @param filter the filter, or null if no filter required.
     */
    public void setResultsFilter(final Filter filter) {
        this.resultsFilter = filter;
    }

    /**
     * @return A filter for results, or null if not set.
     */
    public Filter getResultsFilter() {
        return resultsFilter;
    }

    /**
     * Sets a filter to use to filter out results submitted for identification.
     * @param filter the filter, or null if no filter required.
     */
    public void setIdentificationFilter(final Filter filter) {
        this.identificationFilter = filter;
    }

    /**
     * @return A filter that precedes identification of format, or null if not set.
     */
    public Filter getIdentificationFilter() {
        return identificationFilter;
    }

      /**
     * Set the container signature file.
     *
     * @param containerSignatureFile The Container Signature file
     */
    public void setContainerSignatureFile(final String containerSignatureFile) {
        this.containerSignaturesFileName = containerSignatureFile;
    }

    private ProfileResourceFactory getProfileResourceFactory() {
        if (profileResourceFactory == null) {
            profileResourceFactory = new ProfileResourceFactory();
        }
        return profileResourceFactory;
    }

}
