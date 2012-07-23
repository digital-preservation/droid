/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
