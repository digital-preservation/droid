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
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 * @author rflitcroft
 *
 */
public class JaxBSubmissionQueueDao implements SubmissionQueue {

    private final Log log = LogFactory.getLog(getClass());

    private String targetFileName;
    private final JAXBContext context;
 
    private SubmissionQueueData queue = new SubmissionQueueData();

    /**
     * @throws JAXBException if the JAXBContext could not be instantiated.
     */
    public JaxBSubmissionQueueDao() throws JAXBException {
        
        context = JAXBContext.newInstance(new Class[] {
            SubmissionQueueData.class,
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubmissionQueueData list() {
        
        File in = new File(targetFileName);
        if (in.exists()) {
            try {
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (SubmissionQueueData) unmarshaller.unmarshal(in);
            } catch (JAXBException e) {
                log.error(e);
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void save() {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(queue, new File(targetFileName));
        } catch (JAXBException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * @param targetFileName the targetFileName to set
     */
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void add(RequestIdentifier request) {
        queue.add(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove(RequestIdentifier request) {
        queue.remove(request);
    }
    
}
