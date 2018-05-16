/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.submitter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 * @author rflitcroft
 *
 */
public class JaxBSubmissionQueueDao implements SubmissionQueue {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
        
        Path in = Paths.get(targetFileName);
        if (Files.exists(in)) {
            try {
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (SubmissionQueueData) unmarshaller.unmarshal(in.toFile());
            } catch (JAXBException e) {
                log.error(e.getErrorCode(), e);
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
            marshaller.marshal(queue, Paths.get(targetFileName).toFile());
        } catch (JAXBException e) {
            log.error(e.getErrorCode(), e);
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
