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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * @author rflitcroft
 *
 */
public class JaxbProfileSpecDao implements ProfileSpecDao {

    private static final String PROFILE_XML = "profile.xml";

    private final Log log = LogFactory.getLog(getClass());
    private final JAXBContext context;
    
    /**
     * @throws JAXBException if the JAXBContext could not be instantiated.
     */
    public JaxbProfileSpecDao() throws JAXBException {
        
        context = JAXBContext.newInstance(new Class[] {
            ProfileInstance.class,
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileInstance loadProfile(InputStream in) {
        
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ProfileInstance profile = (ProfileInstance) unmarshaller.unmarshal(in);
            return profile;
        } catch (JAXBException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProfile(ProfileInstance profile, File profileHomeDir) {
        
        File profileXml = new File(profileHomeDir, PROFILE_XML);
        
        OutputStream out = null;
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            out = new BufferedOutputStream(new FileOutputStream(profileXml));
            m.marshal(profile, out);
        } catch (PropertyException e) {
            log.error(e);
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
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
