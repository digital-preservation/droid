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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author rflitcroft
 * 
 */
@XmlRootElement(name = "Dir")
public class DirectoryProfileResource extends FileProfileResource {

    @XmlAttribute(name = "Recursive")
    private boolean recursive;

    /**
     * Default Constructor.
     */
    DirectoryProfileResource() {
    }

    /**
     * 
     * @param file
     *            the directory to represent.
     * @param recursive
     *            whether the resource should recurse into subdirectorie
     */
    public DirectoryProfileResource(File file, boolean recursive) {
        super(file);
        this.recursive = recursive;
    }

    /**
     * @return the recursive
     */
    @Override
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        return true;
    }

}
