/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export.interfaces;

import java.util.List;
import java.util.concurrent.Future;

import uk.gov.nationalarchives.droid.core.interfaces.filter.Filter;


/**
 * @author rflitcroft
 *
 */
public interface ExportManager {

    /**
     * Exports one or more profiles to a CSV file.
     * 
     * FIXME:
     * The only reason this interface takes an optional filter
     * is so that the command line can pass in one of its own 
     * filters.  This is because various parts of the code
     * use a particular implementation of Filter (FilterImpl)
     * rather than using the Filter interface (and modifying
     * it appropriately to be generally useful).
     * The upshot is that you cannot set a generic Filter 
     * on a given profile, only a FilterImpl, so if you want
     * to use a different kind of filter (as the command line 
     * does), you have to override the use of it in each profile,
     * rather than just setting it on the profile directly.
     * 
     * @param profileIds the list of profiles to export.
     * @param destination the destination filename
     * @param filter optional filter
     * @param options the options for export.
     * @return future for cancelling the task. 
     */
    Future<?> exportProfiles(List<String> profileIds, String destination, Filter filter, ExportOptions options);

}
