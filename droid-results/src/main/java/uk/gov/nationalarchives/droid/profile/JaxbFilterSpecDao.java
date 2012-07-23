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
