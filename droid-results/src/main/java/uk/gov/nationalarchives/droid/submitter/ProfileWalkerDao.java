/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;

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
public class ProfileWalkerDao {

    
    private static final String FILE_WALKER_XML = "profile_progress.xml";

    private final Log log = LogFactory.getLog(getClass());
    private final JAXBContext context;
    private String profileHomeDir;
    
    /**
     * @throws JAXBException if the JAXBContext could not be instantiated.
     */
    public ProfileWalkerDao() throws JAXBException {
        
        context = JAXBContext.newInstance(new Class[] {
            ProfileWalkState.class,
        });
    }
    
    /**
     * Loads a walkState from persistent XML.
     * @return walk state
     */
    public ProfileWalkState load() {
        
        File xml = new File(profileHomeDir, FILE_WALKER_XML);
        if (xml.exists()) {
            try {
                Unmarshaller unmarshaller = context.createUnmarshaller();
                ProfileWalkState walkState = (ProfileWalkState) unmarshaller.unmarshal(xml);
                return walkState;
            } catch (JAXBException e) {
                log.error(e);
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        
        return new ProfileWalkState();
    }

    /**
     * Saves a file walker to XML.
     * @param walkState the file walker to save
     */
    public void save(ProfileWalkState walkState) {
        
        File xml = new File(profileHomeDir, FILE_WALKER_XML);
        
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(walkState, xml);
        } catch (PropertyException e) {
            log.error(e);
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     */
    public void delete() {
        File xml = new File(profileHomeDir, FILE_WALKER_XML);
        if (!xml.delete() && xml.exists()) {
            String message = String.format("Could not delete file walker xml file: %s. "
                    + "Will try to delete on exit.", xml.getAbsolutePath());
            log.warn(message);
            xml.deleteOnExit();
        }
    }
    
    /**
     * @param profileHomeDir the profileHomeDir to set
     */
    public void setProfileHomeDir(String profileHomeDir) {
        this.profileHomeDir = profileHomeDir;
    }

}
