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
package uk.gov.nationalarchives.droid.profile;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for a profile.
 */
@XmlRootElement(name = "Profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileInstance {

    private static final Log LOG = LogFactory.getLog(ProfileInstance.class);

    @XmlAttribute(name = "Id")
    private String uuid;

    @XmlElement(name = "CreatedDate", required = true)
    private Date dateCreated;

    @XmlElement(name = "Name", required = true)
    private String name;

    @XmlElement(name = "ProfileSpec", required = true)
    private ProfileSpec profileSpec;

    @XmlElement(name = "State", required = true)
    private ProfileState profileState;

    @XmlElement(name = "SignatureFileVersion", required = true)
    private Integer signatureFileVersion;

    @XmlElement(name = "ContainerSignatureFileVersion", required = true)
    private Integer containerSignatureFileVersion;

    @XmlElement(name = "TextSignatureFileVersion", required = false)
    private Integer textSignatureFileVersion;

    @XmlTransient
    private File loadedFrom;

    @XmlTransient
    private boolean dirty = true;

    //FIXME: by making profiles dependent on a particular
    // implementation of filter, it causes problems for other
    // parts of the code which want to use different filter
    // implementations.  
    @XmlElement(name = "Filter", required = true)
    private FilterImpl filter;
    
    @XmlElement(name = "Throttle")
    private int throttle;
    
    @XmlElement(name = "SignatureFileName")
    private String signatureFileName;
    
    @XmlElement(name = "ContainerSignatureFileName")
    private String containerSignatureFileName;

    @XmlElement(name = "TextSignatureFileName")
    private String textSignatureFileName;

    @XmlElement(name = "EndDate")
    private Date profileEndDate;

    
    @XmlElement(name = "StartDate")
    private Date profileStartDate;
    
    @XmlTransient
    private ProfileState transientState;
    
    @XmlElement(name = "Progress")
    private ProgressState progress;

    @XmlElement(name = "GenerateHash")
    private Boolean generateHash;

    @XmlElement(name = "HashAlgorithm")
    private String hashAlgorithm;

    @XmlElement(name = "ProcessArchiveFiles")
    private Boolean processArchiveFiles;

    @XmlElement(name = "ProcessWebArchiveFiles")
    private Boolean processWebArchiveFiles;

    @XmlElement(name = "MaxBytesToScan")
    private Long maxBytesToScan;
    
    @XmlElement(name = "MatchAllExtensions")
    private Boolean matchAllExtensions;
    
    @XmlTransient
    private Set<ProfileEventListener> eventListeners = new HashSet<ProfileEventListener>();
    
    /**
     * Constructs a profile instance in a default state.
     * @param state the default state.
     */
    ProfileInstance(ProfileState state) {
        this();
        transientState = state;
    }

    /**
     * Default constructor.
     */
    ProfileInstance() {
        dateCreated = new Date();
    }

    /**
     * Getter method for filter.
     * 
     * @return Filter.
     */
    public FilterImpl getFilter() {
        if (filter == null) {
            filter = new FilterImpl();
        }
        return filter;
    }

    /**
     * Setter method for filter.
     * @param filter to set.
     */
    public void setFilter(FilterImpl filter) {
        this.filter = filter;
    }

    /**
     * Setter.
     * 
     * @param dateCreated
     *            the date created
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Getter.
     * 
     * @return the date created
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid
     *            the uuid to set
     */
    void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the profileSpec
     */
    public ProfileSpec getProfileSpec() {
        return profileSpec;
    }

    /**
     * @param profileSpec
     *            the profileSpec to set
     */
    public void setProfileSpec(ProfileSpec profileSpec) {
        this.profileSpec = profileSpec;
        dirty = true;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a resource to this profile spec.
     * 
     * @param resource
     *            the resource to add.
     * @return true if the resource was added, false otherwise
     */
    public boolean addResource(AbstractProfileResource resource) {
        boolean success = profileSpec.addResource(resource);
        if (success) {
            dirty = true;
            fireListeners();
        }
        return success;
    }

    /**
     * @param uri
     *            the URI of the resource to remove
     * @return trus if the resource was remioved, false otherwise.
     */
    public boolean removeResource(URI uri) {
        boolean success = profileSpec.removeResource(uri);
        if (success) {
            dirty = true;
            fireListeners();
        }
        return success;
    }

    /**
     * Manages the internal state.
     */
    void start() {
        setProfileStartDate(new Date());
        changeState(ProfileState.RUNNING);
        dirty = true;
    }

    /**
     * Manages the internal state.
     */
    void finish() {
        
        setProfileEndDate(new Date());
        changeState(ProfileState.FINISHED);
    }

    /**
     * Manages the internal state.
     */
    void stop() {
        changeState(ProfileState.STOPPED);
    }

    /** Changes the profile state.
     * 
     * @param newState the new state.
     */
    public void changeState(ProfileState newState) {

        LOG.info(String.format("Attempting state change [%s] to [%s]",
                getState(), newState));

        if (!getState().allowedNextStates().contains(newState)) {
            throw new IllegalStateException(String.format(
                    "Illegal attempt to transition state from [%s] to [%s]", getState(),
                    newState));
        }
        
        if (newState.isTransient()) {
            transientState = newState;
        } else {
            transientState = null;
            profileState = newState;
        }
        fireListeners();
    }
    
    /**
     * Fires event listeners.
     */
    void fireListeners() {
        for (ProfileEventListener listener : eventListeners) {
            listener.fireEvent(this);
        }
    }

    /**
     * @return the signatureFile version
     */
    public Integer getSignatureFileVersion() {
        return signatureFileVersion;
    }

    /**
     * @param signatureFileVersion
     *            the signatureFile to set
     */
    public void setSignatureFileVersion(Integer signatureFileVersion) {
        this.signatureFileVersion = signatureFileVersion;
    }

    /**
     * @return the loadedFrom
     */
    public File getLoadedFrom() {
        return loadedFrom;
    }

    /**
     * @param loadedFrom
     *            the loadedFrom to set
     */
    public void setLoadedFrom(File loadedFrom) {
        this.loadedFrom = loadedFrom;
    }

    /**
     * @return true if the profile has unsaved changes, false otherwise
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * @return the profile's state
     */
    public ProfileState getState() {
        return transientState != null ? transientState : profileState;
    }

    /**
     * Manages the internal state.
     */
    public void onLoad() {
        dirty = false;
        fireListeners();
        //profileState = ProfileState.STOPPED;
    }

    /**
     * Manages the internal state.
     */
    public void onSave() {
        dirty = false;
    }

    /**
     * @param dirty
     *            the dirty to set
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        fireListeners();
    }

    /**
     * @param throttle the throttle to set
     */
    void setThrottle(int throttle) {
        this.throttle = throttle;
    }
    
    /**
     * @return the throttle
     */
    public int getThrottle() {
        return throttle;
    }

    /**
     * @return the signatureFileName
     */
    public String getSignatureFileName() {
        return signatureFileName;
    }

    /**
     * @param signatureFileName the signatureFileName to set
     */
    public void setSignatureFileName(String signatureFileName) {
        this.signatureFileName = signatureFileName;
    }

    /**
     * @return the profileEndDate
     */
    public Date getProfileEndDate() {
        return profileEndDate;
    }

    /**
     * @param profileEndDate the profileEndDate to set
     */
    public void setProfileEndDate(Date profileEndDate) {
        this.profileEndDate = profileEndDate;
    }

    /**
     * @return the profileStartDate
     */
    public Date getProfileStartDate() {
        return profileStartDate;
    }

    /**
     * @param profileStartDate the profileStartDate to set
     */
    public void setProfileStartDate(Date profileStartDate) {
        this.profileStartDate = profileStartDate;
    }
    
    /**
     * 
     * @param listener the listener to add.
     */
    public void addEventListener(ProfileEventListener listener) {
        eventListeners.add(listener);
    }
    
    /**
     * @return the progress
     */
    public ProgressState getProgress() {
        return progress;
    }
    
    /**
     * @param progress the progress to set
     */
    public void setProgress(ProgressState progress) {
        this.progress = progress;
    }

    /**
     * @param containerSignatureFileName the containerSignatureFileName to set
     */
    public void setContainerSignatureFileName(String containerSignatureFileName) {
        this.containerSignatureFileName = containerSignatureFileName;
    }
    
    /**
     * @param containerSignatureFileVersion the containerSignatureFileVersion to set
     */
    public void setContainerSignatureFileVersion(Integer containerSignatureFileVersion) {
        this.containerSignatureFileVersion = containerSignatureFileVersion;
    }
    
    /**
     * @param textSignatureFileName the textSignatureFileName to set
     */
    public void setTextSignatureFileName(String textSignatureFileName) {
        this.textSignatureFileName = textSignatureFileName;
    }
    
    /**
     * @param textSignatureFileVersion the textSignatureFileVersion to set
     */
    public void setTextSignatureFileVersion(Integer textSignatureFileVersion) {
        this.textSignatureFileVersion = textSignatureFileVersion;
    }
    
    /**
     * @return the containerSignatureFileName
     */
    public String getContainerSignatureFileName() {
        return containerSignatureFileName;
    }
    
    /**
     * @return the containerSignatureFileVersion
     */
    public Integer getContainerSignatureFileVersion() {
        return containerSignatureFileVersion;
    }
    
    /**
     * @return the textSignatureFileName
     */
    public String getTextSignatureFileName() {
        return textSignatureFileName;
    }
    
    /**
     * @return the textSignatureFileVersion
     */
    public Integer getTextSignatureFileVersion() {
        return textSignatureFileVersion;
    }
    
    /**
     * 
     * @return Whether to generate a hash value or not.
     */
    public Boolean getGenerateHash() {
        return generateHash;
    }
    
    /**
     * 
     * @param generateHash Whether to generate a hash value or not.
     */
    public void setGenerateHash(boolean generateHash) {
        this.generateHash = generateHash;
    }

    /**
     *
     * @return name of algorithm for hash generation.
     */
    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    /**
     *
     * @param hashAlgorithm Algorithm to use to generate hashes.
     */
    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }
    /**
     * 
     * @return Whether to process archive files or not.
     */
    public Boolean getProcessArchiveFiles() {
        return processArchiveFiles;
    }

    /**
     *
     * @return Whether to process webarchive files or not.
     */
    public Boolean getProcessWebArchiveFiles() { return processWebArchiveFiles; }
    /**
     * 
     * @param processArchiveFiles Whether to process archive files or not.
     */
    public void setProcessArchiveFiles(boolean processArchiveFiles) {
        this.processArchiveFiles = processArchiveFiles;
    }
    /**
     *
     * @param processWebArchiveFiles Whether to process web archive files or not.
     */
    public void setProcessWebArchiveFiles(boolean processWebArchiveFiles) {
        this.processWebArchiveFiles = processWebArchiveFiles;
    }
    /**
     * 
     * @return The maximum bytes to scan from the beginning and end of a file, 
     *         or negative meaning unlimited scanning.
     */
    public Long getMaxBytesToScan() {
        return maxBytesToScan;
    }
    
    /**
     * 
     * @param maxBytesToScan The maximum bytes to scan from the beginning and end of a file, 
     *         or negative meaning unlimited scanning.
     */
    public void setMaxBytesToScan(long maxBytesToScan) {
        this.maxBytesToScan = maxBytesToScan;
    }
    
    /**
     * 
     * @param matchAllExtensions Whether to match all extensions, or just ones without another
     * signature defined for it.
     */
    public void setMatchAllExtensions(Boolean matchAllExtensions) {
        this.matchAllExtensions = matchAllExtensions;
    }
    
    /**
     * 
     * @return matchAllExtensions Whether to match all extensions, or just ones without another
     * signature defined for it.
     */
    public Boolean getMatchAllExtensions() {
        return matchAllExtensions;
    }
    
}
