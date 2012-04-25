/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import javax.xml.bind.annotation.XmlElement;

/**
 * Memento for progress monitor state.
 * @author rflitcroft
 *
 */
public class ProgressState {

    @XmlElement(name = "Target")
    private long target;
    
    @XmlElement(name = "Count")
    private long count;
    
    /**
     * 
     */
    public ProgressState() { }

    /**
     * @param target the target
     * @param count the current count
     */
    public ProgressState(long target, long count) { 
        this.target = target;
        this.count = count;
    }
    
    
    /**
     * @return the count
     */
    public long getCount() {
        return count;
    }
    
    /**
     * @return the target
     */
    public long getTarget() {
        return target;
    }
    

}
