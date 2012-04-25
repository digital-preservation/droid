/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.pronom.signaturefile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;


/**
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xml-fragment", propOrder = {"element"})
public class XmlFragment {

    @XmlAnyElement
    protected Element element;

    /**
     * @return the element
     */
    public Element getElement() {
        return element;
    }

    /**
     * @param element the element to set
     */
    public void setElement(Element element) {
        this.element = element;
    }
    
    
}
