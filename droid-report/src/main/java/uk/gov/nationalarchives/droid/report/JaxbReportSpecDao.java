/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
