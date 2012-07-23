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
