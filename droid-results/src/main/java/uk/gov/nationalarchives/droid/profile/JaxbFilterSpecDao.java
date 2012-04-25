/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.profile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author a-mpalmer
 *
 */
public class JaxbFilterSpecDao implements FilterSpecDao {


    private final Log log = LogFactory.getLog(getClass());
    private final JAXBContext context;
    
    /**
     * @throws JAXBException if the JAXBContext could not be instantiated.
     */
    public JaxbFilterSpecDao() throws JAXBException {
        
        context = JAXBContext.newInstance(new Class[] {
            FilterSpec.class,
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public FilterImpl loadFilter(InputStream in) {
        
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            FilterSpec spec = (FilterSpec) unmarshaller.unmarshal(in);
            return spec.getFilter();
        } catch (JAXBException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveFilter(FilterImpl filter, OutputStream output) {
        OutputStream out = null;
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            out = new BufferedOutputStream(output);
            FilterSpec spec = new FilterSpec(filter);
            m.marshal(spec, out);
        } catch (PropertyException e) {
            log.error(e);
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            closeOutputStream(out);
        }
    }

    /**
     * @param out
     */
    private void closeOutputStream(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                log.error("Error closing output stream: " + e.getMessage(), e);
            }
        }
    }    
    
}
