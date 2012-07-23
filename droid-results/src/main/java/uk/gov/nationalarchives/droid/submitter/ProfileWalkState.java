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
