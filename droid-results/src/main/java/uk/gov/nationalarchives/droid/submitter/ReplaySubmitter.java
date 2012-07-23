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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.results.handlers.ResultHandlerDao;

/**
 * Suubmits Resources in the replay queue to droid.
 * @author rflitcroft
 * 
 */
public class ReplaySubmitter {

    /**
     * 
     */
    private static final int FIVE = 5;
    private FileEventHandler fileEventHandler;
    private SubmissionQueue submissionQueue;
    private ResultHandlerDao resultHandlerDao;
    
    /**
     * Re-submits all unfinished archival nodes as new Identification requests.
     * All existing data under those nodes are destroyed.
     * 
     */
    public void replay() {
        
        SubmissionQueueData queueData = submissionQueue.list();
        if (queueData != null) {
            // de-duplicate any resources with the same ancestor ID
            Set<Long> ancestorIds = new HashSet<Long>();
            List<RequestIdentifier> identifiers = queueData.getReplayUris();
            for (Iterator<RequestIdentifier> it = identifiers.iterator(); it.hasNext();) {
                RequestIdentifier identifier = it.next();
                ancestorIds.add(identifier.getAncestorId());
            }
            
            // Delete and re-submit all the distinct nodes as new requests
            for (Long ancestorId : ancestorIds) {
                ProfileResourceNode node = resultHandlerDao.loadNode(ancestorId);
                ResourceId parentId = getParentResourceId(node);
                resultHandlerDao.deleteNode(node.getId());
                File file = new File(node.getUri());
                fileEventHandler.onEvent(file, parentId, null);
            }
        }
    }

    
    private ResourceId getParentResourceId(ProfileResourceNode node) {
        ResourceId id = null;
        Long parentId = node.getParentId();
        if (parentId != null) {
            String parentPrefix = node.getPrefix();
            if (parentPrefix.length() >= FIVE) {
                parentPrefix = parentPrefix.substring(0, parentPrefix.length() - FIVE);
            } else {
                parentPrefix = "";
            }
            id = new ResourceId(parentId, parentPrefix);
        }
        return id;
    }
    
    
    /**
     * @param fileEventHandler the fileEventHandler to set
     */
    public void setFileEventHandler(FileEventHandler fileEventHandler) {
        this.fileEventHandler = fileEventHandler;
    }
    
    /**
     * @param submissionQueue the submissionQueue to set
     */
    public void setSubmissionQueue(SubmissionQueue submissionQueue) {
        this.submissionQueue = submissionQueue;
    }
    
    /**
     * @param resultHandlerDao the resultHandlerDao to set
     */
    public void setResultHandlerDao(ResultHandlerDao resultHandlerDao) {
        this.resultHandlerDao = resultHandlerDao;
    }
}
