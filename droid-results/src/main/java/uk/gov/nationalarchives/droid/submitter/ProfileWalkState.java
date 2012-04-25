/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.DirectoryProfileResource;
import uk.gov.nationalarchives.droid.profile.FileProfileResource;

/**
 * @author rflitcroft
 *
 */
@XmlRootElement(name = "ProfileWalk")
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileWalkState {

    @XmlElementRefs({
        @XmlElementRef(name = "File", type = FileProfileResource.class), 
        @XmlElementRef(name = "Dir", type = DirectoryProfileResource.class) })
    private AbstractProfileResource currentResource;
    
    @XmlElement(name = "FileWalker")
    private FileWalker currentFileWalker;
    
    @XmlAttribute(name = "Status")
    private WalkStatus walkStatus = WalkStatus.NOT_STARTED;
    
    /**
     * Default Constructor. 
     */
    ProfileWalkState() {
    }
    
    /**
     * @return the currentFileWalker
     */
    public FileWalker getCurrentFileWalker() {
        return currentFileWalker;
    }
    
    /**
     * @return the currentResource
     */
    public AbstractProfileResource getCurrentResource() {
        return currentResource;
    }
    
    /**
     * @param currentFileWalker the currentFileWalker to set
     */
    public void setCurrentFileWalker(FileWalker currentFileWalker) {
        this.currentFileWalker = currentFileWalker;
    }
    
    /**
     * @param currentResource the currentResource to set
     */
    public void setCurrentResource(AbstractProfileResource currentResource) {
        this.currentResource = currentResource;
    }
    
    /**
     * @param walkStatus the walkStatus to set
     */
    public void setWalkStatus(WalkStatus walkStatus) {
        this.walkStatus = walkStatus;
    }
    
    /**
     * @return the walkStatus
     */
    public WalkStatus getWalkStatus() {
        return walkStatus;
    }
    
    /**
     * Walk status.
     * @author rflitcroft
     *
     */
    public static enum WalkStatus {
        /** Walk has not started. */
        NOT_STARTED,
        
        /** Walk in progress. */
        IN_PROGRESS,
        
        /** Walk finished. */
        FINISHED;
    }
    
}
