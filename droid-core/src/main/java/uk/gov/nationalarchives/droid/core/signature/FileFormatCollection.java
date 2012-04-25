/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
/*
 * The National Archives 2005-2006.  All rights reserved.
 * See Licence.txt for full licence details.
 *
 * Developed by:
 * Tessella Support Services plc
 * 3 Vineyard Chambers
 * Abingdon, OX14 3PX
 * United Kingdom
 * http://www.tessella.com
 *
 * Tessella/NPD/4305
 * PRONOM 4
 *
 * $History: FileFormatCollection.java $
 * 
 * *****************  Version 2  *****************
 * User: Walm         Date: 5/04/05    Time: 18:07
 * Updated in $/PRONOM4/FFIT_SOURCE/signatureFile
 * review headers
 *
 */
package uk.gov.nationalarchives.droid.core.signature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * Holds a collection of {@link FileFormat} objects.
 *
 * @author Martin Waller
 * @author Matt Palmer
 * @version 6.0.0
 */
public class FileFormatCollection extends SimpleElement {
    private List<FileFormat> formats = new ArrayList<FileFormat>();
    private Map<String, FileFormat> puidFormats = new HashMap<String, FileFormat>();
    
    /* setters */
    

    /**
     * @param format A file format to add to the collection.
     */
    public final void addFileFormat(final FileFormat format) {
        formats.add(format);
        puidFormats.put(format.getPUID(), format);
    }

    /**
     * 
     * @param formatList A list of file formats to set for the collection.
     */
    public final void setFileFormats(final List<FileFormat> formatList) {
        formats.clear();
        puidFormats.clear();
        for (FileFormat format : formatList) {
            addFileFormat(format);
        }
    }

    /* getters */
    
    /**
     * @return The list of file formats held by this collection.
     */
    public final List<FileFormat> getFileFormats() {
        return formats;
    }
    
    /**
     * 
     * @param puid The puid
     * @return A file format for that puid.
     */
    public FileFormat getFormatForPUID(final String puid) {
        return puidFormats.get(puid);
    }
    
}
