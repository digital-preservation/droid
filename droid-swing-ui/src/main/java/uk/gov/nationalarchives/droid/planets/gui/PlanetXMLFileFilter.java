/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.planets.gui;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;




/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about.
 * 
 * @author Alok Kumar Dash
 */
public class PlanetXMLFileFilter extends FileFilter {

    private Hashtable filters;
    private String description;
    private String fullDescription;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all files are
     * accepted.
     */
    public PlanetXMLFileFilter() {
        this.filters = new Hashtable();
    }

    /**
     * Creates a file filter that accepts the given file type. Example: new
     * ExampleFileFilter("xml", "XML documents");
     * Note that the "." before the extension is not needed. If provided, it
     * will be ignored.
     * @param extension Extension to be filtered.
     * @param description Description of the extension to be filtered.
     */
    public PlanetXMLFileFilter(String extension, String description) {
        this();
        if (extension != null) {
            addExtension(extension);
        }
        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Return true if this file should be shown in the directory pane, false if
     * it shouldn't. Files that begin with "." are ignored.
     * @param f File
     * @return boolean Boolean value.
     */
    @Override
    public boolean accept(File f) {
        
        boolean accept = false;
        if (f != null) {
            if (f.isDirectory()) {
                accept =  true;
            }
            String extension = getExtension(f);
            if (extension != null && filters.get(getExtension(f)) != null) {
                accept =  true;
            }
        }
        return accept;
    }

    /**
     * Return the extension portion of the file's name .
     * @param f File for which extension is requested.
     * @return String The Extension.
     */
    public String getExtension(File f) {
        if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
        }
        return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     * 
     * For example: the following code will create a filter that filters out all
     * files except those that end in ".xml" 
     * PlanetXMLFileFilter filter = new PlanetXMLFileFilter();
     * filter.addExtension("xml");
     * Note that the "." before the extension is not needed and will be ignored.
     * @param extension Extension.
     * 
     */
    public void addExtension(String extension) {
        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }

    /**
     * Returns the human readable description of this filter. For example:
     * "JPEG and GIF Image Files (*.jpg, *.gif)"
     * 
     * @return String description.
     */
    @Override
    public String getDescription() {
        if (fullDescription == null) {
            if (description == null || isExtensionListInDescription()) {
                fullDescription = description == null ? "(" : description
                        + " (";
                // build the description from the extension list
                Enumeration extensions = filters.keys();
                if (extensions != null) {
                    fullDescription += "." + (String) extensions.nextElement();
                    while (extensions.hasMoreElements()) {
                        fullDescription += ", ."
                                + (String) extensions.nextElement();
                    }
                }
                fullDescription += ")";
            } else {
                fullDescription = description;
            }
        }
        return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For example:
     * filter.setDescription("Planets XMLDocuments");
     * @param description Description to set.
     */
    public void setDescription(String description) {
        this.description = description;
        fullDescription = null;
    }

    /**
     * Determines whether the extension list (.xml) should show up in
     * the human readable description.
     * Only relevant if a description was provided in the constructor or using
     * setDescription();
     * @param b to set extension visibility in readable format.
     */
    public void setExtensionListInDescription(boolean b) {
        useExtensionsInDescription = b;
        fullDescription = null;
    }

    /**
     * Returns whether the extension list (.xml) should show up in
     * the human readable description.
     * Only relevant if a description was provided in the constructor or using
     * setDescription();
     * @return boolean if visible in redable format.
     */
    public boolean isExtensionListInDescription() {
        return useExtensionsInDescription;
    }
}
