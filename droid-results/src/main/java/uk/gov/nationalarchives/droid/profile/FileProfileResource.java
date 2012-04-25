/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.io.File;
import java.util.Date;


import javax.xml.bind.annotation.XmlRootElement;

//import org.apache.commons.io.FilenameUtils;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;

/**
 * Defines a specification for a profile. This can be a single file, a
 * directory, a recursed directory or a URL.
 * 
 * @author rflitcroft
 * 
 */
@XmlRootElement(name = "File")
public class FileProfileResource extends AbstractProfileResource {

    /**
     * Default Constructor.
     */
    FileProfileResource() {
    }

    /**
     * 
     * @param file
     *            the file to represent.
     */
    public FileProfileResource(File file) {
        setUri(file.toURI());
        setName(file.getName());
        setSize(file.length());
        setLastModifiedDate(new Date(file.lastModified()));
        //setExtension(FilenameUtils.getExtension(file.getName()));
        setExtension(ResourceUtils.getExtension(file.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return false;
    }
}
