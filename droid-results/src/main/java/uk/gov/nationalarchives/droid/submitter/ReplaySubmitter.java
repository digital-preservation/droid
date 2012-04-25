/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
