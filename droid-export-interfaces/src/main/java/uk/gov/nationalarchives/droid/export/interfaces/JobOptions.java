/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export.interfaces;

import java.util.Map;

/**
 * Options for batch jobs and their associated readers and writers.
 * How are we going to get a job if we don't know our Job options?
 * @author rflitcroft
 *
 */
public class JobOptions {

    private Map<String, Object> parameters;
    
    /**
     * 
     * @param parameters a map of parameters.
     */
    public JobOptions(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    /**
     * @param key the parameter key
     * @return the parameter specified.
     * 
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    /**
     * 
     * @param key the key
     * @return true if the context contained the key
     */
    public boolean containsKey(String key) {
        return parameters.containsKey(key);
    }
}
