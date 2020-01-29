/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
 * � The National Archives 2005-2006.  All rights reserved.
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
 * SimpleElement.java
 *
 */
package uk.gov.nationalarchives.droid.core.signature.xml;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * holds the basic details of an element read from an XML file.
 *
 * @author Martin Waller
 * @version 4.0.0
 */
public class SimpleElement {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String myText = "";

    /* setters */
    
    /**
     * @param theText the text to set for the element.
     */
    public void setText(String theText) {
        this.myText += theText;
    }

    /**
     * Implementations override this method.
     * 
     * @param name The name of the attribute to set.
     * @param value The value of the attribute.
     */
    public void setAttributeValue(String name, String value) {
        unknownAttributeWarning(name, this.getElementName());
    }

    /* getters */
    /**
     * @return the text of the element.
     */
    public String getText() {
        return myText.trim();
    }

    /**
     * 
     * @return The element name.
     */
    public String getElementName() {
        String className = this.getClass().getName();
        className = className.substring(className.lastIndexOf(".") + 1);
        return className;
    }

    /**
     * method to be overridden in cases where the element content needs 
     * to be specified only when the end of element tag is reached.
     */
    public void completeElementContent() {
    }
    
    /**
     * Displays a special warning for unknown XML attributes when reading
     * XML files.
     *
     * @param unknownAttribute The name of the attribute which was not recognised
     * @param containerElement The name of the element which contains the unrecognised attribute
     */
    public void unknownAttributeWarning(String unknownAttribute, String containerElement) {
        final String warning = "WARNING: Unknown XML attribute " + unknownAttribute + " found for " + containerElement;
        log.debug(warning);
    }    
    
    /**
     * Displays a general warning.
     *
     * @param theWarning The text to be displayed
     */
    public void generalWarning(String theWarning) {
        String theMessage = "WARNING: " + theWarning.replaceFirst("java.lang.Exception: ", "");
        log.debug(theMessage);
    }    
    
    /**
     * 
     * @return the log object owned by SimpleElement.
     */
    protected Logger getLog() {
        return log;
    }
}
