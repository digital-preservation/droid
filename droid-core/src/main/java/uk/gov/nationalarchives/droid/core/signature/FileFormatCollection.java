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
