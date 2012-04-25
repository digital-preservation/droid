/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report;

import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.report.interfaces.Report;
import uk.gov.nationalarchives.droid.report.interfaces.ReportXmlWriter;

/**
 * @author rflitcroft
 *
 */
public class JaxbReportXmlWriter implements ReportXmlWriter {
    
    private JAXBContext context; 
    private Log log = LogFactory.getLog(this.getClass());

    /**
     * @throws JAXBException if the JAXBContext could not be instantiated.
     */
    public JaxbReportXmlWriter() throws JAXBException {
        
        context = JAXBContext.newInstance(new Class[] {
            Report.class,
        });
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void writeReport(Report report, Writer writer) {
        
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(report, writer);
        } catch (JAXBException e) {
            log.error(e);
            throw new RuntimeException(e.getMessage(), e);
        }
        
    };
    
}
