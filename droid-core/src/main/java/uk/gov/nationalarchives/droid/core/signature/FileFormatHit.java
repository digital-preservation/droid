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
 * $Id: FileFormatHit.java,v 1.4 2006/03/13 15:15:25 linb Exp $
 * 
 * $Log: FileFormatHit.java,v $
 * Revision 1.4  2006/03/13 15:15:25  linb
 * Changed copyright holder from Crown Copyright to The National Archives.
 * Added reference to licence.txt
 * Changed dates to 2005-2006
 *
 * Revision 1.3  2006/02/08 08:56:35  linb
 * - Added header comments
 *
 *
 * *$History: FileFormatHit.java $
 *
 * *****************  Version 4  *****************
 * User: Walm         Date: 5/04/05    Time: 18:08
 * Updated in $/PRONOM4/FFIT_SOURCE
 * review headers
 *
 */

package uk.gov.nationalarchives.droid.core.signature;

import uk.gov.nationalarchives.droid.core.signature.xml.SimpleElement;

/**
 * holds the description of a hit (format identification) on a file.
 *
 * @author Martin Waller
 * @version 4.0.0
 */
public class FileFormatHit extends SimpleElement {
    
    //hit type constants
    /**
     * Constant.
     */
    public static final int HIT_TYPE_POSITIVE_SPECIFIC = 10;
    /**
     * Constant.
     */
    public static final int HIT_TYPE_POSITIVE_GENERIC = 11;
    /**
     * Constant.
     */
    public static final int HIT_TYPE_TENTATIVE = 12;
    /**
     * Constant.
     */
    public static final int HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC = 15;
    /**
     * Constant.
     */
    public static final String HIT_TYPE_POSITIVE_SPECIFIC_TEXT = "Positive (Specific Format)";
    /**
     * Constant.
     */
    public static final String HIT_TYPE_POSITIVE_GENERIC_TEXT = "Positive (Generic Format)";
    /**
     * Constant.
     */
    public static final String HIT_TYPE_TENTATIVE_TEXT = "Tentative";
    /**
     * Constant.
     */
    public static final String HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC_TEXT = "Positive";
    
    /**
     * Constant.
     */
    public static final String FILEEXTENSIONWARNING = "Possible file extension mismatch";
    /**
     * Constant.
     */
    public static final String POSITIVEIDENTIFICATIONSTATUS = "Positively identified";
    /**
     * Constant.
     */
    public static final String TENTATIVEIDENTIFICATIONSTATUS = "Tentatively identified";
    /**
     * Constant.
     */
    public static final String UNIDENTIFIEDSTATUS = "Unable to identify";
    
    
    private String myHitWarning = "";
    private int myHitType;
    private FileFormat myHitFileFormat;

    /**
     * Creates a new blank instance of fileFormatHit.
     *
     * @param theFileFormat  The file format which has been identified
     * @param theType        The type of hit i.e. Positive/tentative
     * @param theSpecificity Flag is set to true for Positive specific hits
     * @param theWarning     A warning associated with the hit
     */
    public FileFormatHit(FileFormat theFileFormat, int theType, boolean theSpecificity, String theWarning) {
        myHitFileFormat = theFileFormat;
        if (theType == HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC) {
            if (theSpecificity) {
                myHitType = HIT_TYPE_POSITIVE_SPECIFIC;
            } else {
                myHitType = HIT_TYPE_POSITIVE_GENERIC;
            }
        } else {
            myHitType = theType;
        }
        this.setIdentificationWarning(theWarning);
    }

    /**
     * Default constructor.
     */
    public FileFormatHit() {
    }

    /**
     * Updates the warning message for a hit.
     * <p/>
     * Used by XML reader for IdentificationFile/FileFormatHit/IdentificationWarning element
     *
     * @param theWarning A warning associated with the hit
     */
    public void setIdentificationWarning(String theWarning) {
        myHitWarning = theWarning;
    }


    /**
     * get the fileFormat for the hit.
     *
     * @return The file format which was hit.
     */
    public FileFormat getFileFormat() {
        return myHitFileFormat;
    }

    /**
     * get the name of the fileFormat of this hit.
     *
     * @return The name of the file format which was hit.
     */
    public String getFileFormatName() {
        return myHitFileFormat.getName();
    }

    /**
     * get the version of the fileFormat of this hit.
     *
     * @return the version of the fileFormat of this hit
     */
    public String getFileFormatVersion() {
        return myHitFileFormat.getVersion();
    }

    /**
     * Get the mime type.
     *
     * @return the mime type.
     */
    public String getMimeType() {
        return myHitFileFormat.getMimeType();
    }

    /**
     * get the PUID of the fileFormat of this hit.
     *
     * @return the PUID of the fileFormat of this hit.
     */
    public String getFileFormatPUID() {
        return myHitFileFormat.getPUID();
    }

    /**
     * get the code of the hit type.
     *
     * @return the code of the hit type
     */
    public int getHitType() {
        return myHitType;
    }

    /**
     * get the name of the hit type.
     *
     * @return the name of the hit type.
     */
    public String getHitTypeVerbose() {
        String theHitType = "";
        if (myHitType == HIT_TYPE_POSITIVE_GENERIC) {
            theHitType = HIT_TYPE_POSITIVE_GENERIC_TEXT;
        } else if (myHitType == HIT_TYPE_POSITIVE_SPECIFIC) {
            theHitType = HIT_TYPE_POSITIVE_SPECIFIC_TEXT;
        } else if (myHitType == HIT_TYPE_TENTATIVE) {
            theHitType = HIT_TYPE_TENTATIVE_TEXT;
        } else if (myHitType == HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC) {
            theHitType = HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC_TEXT;
        }
        return theHitType;
    }

    /**
     * get any warning associated with the hit.
     *
     * @return any warning associated with the hit
     */
    public String getHitWarning() {
        return myHitWarning;
    }

    /**
     * For positive hits, this returns true if hit is Specific
     * or returns false if hit is Generic.
     * Meaningless for Tentative hits. (though returns false)
     *
     * @return true if hit is Specific, false if hit is Generic
     */
    public boolean isSpecific() {
        return myHitType == HIT_TYPE_POSITIVE_SPECIFIC;
    }


    /**
     * Populates the details of the IdentificationFile when 
     * it is read in from XML file.
     *
     * @param theName  Name of the attribute read in
     * @param theValue Value of the attribute read in
     */
    @Override
    public void setAttributeValue(String theName, String theValue) {
        if ("HitStatus".equals(theName)) {
            this.setStatus(theValue);
        } else if ("FormatName".equals(theName)) {
            this.setName(theValue);
        } else if ("FormatVersion".equals(theName)) {
            this.setVersion(theValue);
        } else if ("FormatPUID".equals(theName)) {
            this.setPUID(theValue);
        } else if ("HitWarning".equals(theName)) {
            this.setIdentificationWarning(theValue);
        } else {
            unknownAttributeWarning(theName, this.getElementName());
        }
    }

    /**
     * Set hit status.  
     * Used by XML reader for IdentificationFile/FileFormatHit/Status element
     *
     * @param value The value of the hit.status.
     */
    public void setStatus(String value) {
        //String value = element.getText();
        if (value.equals(HIT_TYPE_POSITIVE_GENERIC_TEXT)) {
            myHitType = HIT_TYPE_POSITIVE_GENERIC;
        } else if (value.equals(HIT_TYPE_POSITIVE_SPECIFIC_TEXT)) {
            myHitType = HIT_TYPE_POSITIVE_SPECIFIC;
        } else if (value.equals(HIT_TYPE_TENTATIVE_TEXT)) {
            myHitType = HIT_TYPE_TENTATIVE;
        } else if (value.equals(HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC_TEXT)) {
            myHitType = HIT_TYPE_POSITIVE_GENERIC_OR_SPECIFIC;
        } else {
            generalWarning("Unknown hit status listed: " + value);
        }
    }

    /**
     * Set hit format name.  
     * Used by XML reader for IdentificationFile/FileFormatHit/Name element
     *
     * @param value The value of the name.
     */
    public void setName(String value) {
        //if necessary, this creates a new dummy File format
        if (myHitFileFormat == null) {
            myHitFileFormat = new FileFormat();
        }
        myHitFileFormat.setAttributeValue("Name", value);
    }

    /**
     * Set hit format version.  
     * Used by XML reader for IdentificationFile/FileFormatHit/Version element
     *
     * @param value The value of the version.
     */
    public void setVersion(String value) {
        if (myHitFileFormat == null) {
            myHitFileFormat = new FileFormat();
        }
        myHitFileFormat.setAttributeValue("Version", value);
    }

    /**
     * Set hit format PUID.  
     * Used by XML reader for IdentificationFile/FileFormatHit/PUID element
     *
     * @param value The value of the PUID.
     */
    public void setPUID(String value) {
        if (myHitFileFormat == null) {
            myHitFileFormat = new FileFormat();
        }
        myHitFileFormat.setAttributeValue("PUID", value);
    }

    /**
     * Set hit format MIME type.
     * Used by XML reader for IdentificationFile/FileFormatHit/PUID element
     *
     * @param value The value of the mime type.
     */
    public void setMimeType(String value) {
        if (myHitFileFormat == null) {
            myHitFileFormat = new FileFormat();
        }
        myHitFileFormat.setAttributeValue("MIMEType", value);
    }


}
