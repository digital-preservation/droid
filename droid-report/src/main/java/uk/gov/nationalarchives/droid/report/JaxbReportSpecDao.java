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
package uk.gov.nationalarchives.droid.report;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.report.interfaces.ReportSpec;
import uk.gov.nationalarchives.droid.report.interfaces.ReportSpecDao;

/**
 * @author rflitcroft
 *
 */
public class JaxbReportSpecDao implements ReportSpecDao {

    private final Log log = LogFactory.getLog(getClass());
    
    private JAXBContext context;
    
    /**
     * Default constructor.
     * @throws JAXBException if the JaxB context failed to initialise
     */
    public JaxbReportSpecDao() throws JAXBException {
        context = JAXBContext.newInstance(ReportSpec.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ReportSpec readReportSpec(String filePath) {
        try {
            Reader reader = new FileReader(filePath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (ReportSpec) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            log.error(e);
            return null;
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
    
}
