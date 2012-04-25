/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import java.io.File;

import uk.gov.nationalarchives.droid.profile.AbstractProfileResource;
import uk.gov.nationalarchives.droid.profile.DirectoryProfileResource;
import uk.gov.nationalarchives.droid.profile.FileProfileResource;

/**
 * @author rflitcroft
 *
 */
public class LocationResolver {

    /**
     * Resolves a location string to a profile resource.
     * @param location the resources location string
     * @param recursive if the resource should be recursed
     * @return a new profile resource
     */
    public AbstractProfileResource getResource(String location, boolean recursive) {
        
        File f = new File(location);
        if (f.isFile()) {
            return new FileProfileResource(f);
        } else if (f.isDirectory()) {
            return new DirectoryProfileResource(f, recursive);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unknown location [%s]", location));
        }
        
    }
}
