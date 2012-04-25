/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.container;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.gov.nationalarchives.droid.core.SignatureParseException;

/**
 * @author rflitcroft
 *
 */
public class ContainerSignatureSaxParser {

    private JAXBContext context;
    
    /**
     * @throws JAXBException if the JAXB context could not be initialised.
     * 
     */
    public ContainerSignatureSaxParser() throws JAXBException {
        context = JAXBContext.newInstance(new Class[] {
            ContainerSignatureDefinitions.class,
        });
    }
    
    /**
     * 
     * @param in the input stream to parse
     * @return List of container signatures
     * @throws SignatureParseException if the container signature file could not be processed.
     */
    public ContainerSignatureDefinitions parse(InputStream in) throws SignatureParseException {
        
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ContainerSignatureDefinitions definitions = (ContainerSignatureDefinitions) unmarshaller.unmarshal(in);
            return definitions;
        } catch (JAXBException e) {
            throw new SignatureParseException(e.getMessage(), e);
        }
    }

}
