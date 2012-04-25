/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author a-mpalmer
 *
 */
public interface FilterSpecDao {

    /**
     * 
     * @param filter  The filter object to persist
     * @param output The file to save the filter object to.
     */
    void saveFilter(FilterImpl filter, OutputStream output);
    
    /**
     * 
     * @param input The file to load a filter object from.
     * @return A filterimpl object loaded from a file.
     */
    FilterImpl loadFilter(InputStream input);
    
}
