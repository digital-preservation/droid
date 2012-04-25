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

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationMethod;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultImpl;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.core.interfaces.ResultHandler;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 * @author rflitcroft
 *
 */
public class DirectoryEventHandler {

    private ResultHandler resultHandler;
    
    /**
     * Handles a directory.
     * @param dir the directory to handle
     * @param parentId the directory's parent id
     * @param depth the depth of the directory in the tree
     * @param restricted true if access to the directory was restricted, false otherwise
     * @return the id of the directory
     */
    public ResourceId onEvent(File dir, ResourceId parentId, int depth, boolean restricted) {
        IdentificationResultImpl result = new IdentificationResultImpl();
        result.setMethod(IdentificationMethod.NULL);
        
        RequestMetaData metaData = new RequestMetaData(
                dir.length(),
                dir.lastModified(),
                depth == 0 ? dir.getAbsolutePath() : dir.getName());
        
        RequestIdentifier identifier = new RequestIdentifier(dir.toURI());
        identifier.setParentResourceId(parentId);
        result.setRequestMetaData(metaData);
        result.setIdentifier(identifier);
        return resultHandler.handleDirectory(result, parentId, restricted);
    }

    /**
     * @param resultHandler the resultHandler to set
     */
    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

}
