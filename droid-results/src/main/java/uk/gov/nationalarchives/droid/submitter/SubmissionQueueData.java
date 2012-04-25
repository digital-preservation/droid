/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 * @author rflitcroft
 * Persistent XML serializable queue data.
 *
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "SubmissionQueue")
public class SubmissionQueueData {

    @XmlElement(name = "Resource")
    private List<RequestIdentifier> uris = new ArrayList<RequestIdentifier>();
    
    /**
     * Default Constructor .
     */
    public SubmissionQueueData() { }
    
    /**
     * 
     * @param uri the URI to add
     */
    public void add(RequestIdentifier uri) {
        uris.add(uri);
    }
    
    /**
     * 
     * @param uri the URi to remove
     */
    public void remove(RequestIdentifier uri) {
        uris.remove(uri);
    }
    
    /**
     * Returns distinct URIs
     * @return the uris
     */
    List<RequestIdentifier> getReplayUris() {
        return uris;
    }
}
