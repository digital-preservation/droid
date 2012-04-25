/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;

import java.io.Writer;


/**
 * @author rflitcroft
 *
 */
public interface ReportXmlWriter {
    
    /**
     * Writes XML report data to the output stream specified.
     * @param report the report to serialze
     * @param out the output stream to serialize to
     */
    void writeReport(Report report, Writer out);

}
