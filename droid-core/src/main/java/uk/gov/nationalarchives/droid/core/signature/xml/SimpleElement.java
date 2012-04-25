/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * holds the basic details of an element read from an XML file.
 *
 * @author Martin Waller
 * @version 4.0.0
 */
public class SimpleElement {

    private Log log = LogFactory.getLog(this.getClass());

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
    protected Log getLog() {
        return log;
    }
}
