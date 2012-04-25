/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.report.interfaces;


/**
 * @author rflitcroft
 *
 */
public interface ReportSpecDao {

    /**
     * Reads a report spec using the supplied file and returns a report spec.
     * @param filePath the file to read
     * @return a report spec
     */
    ReportSpec readReportSpec(String filePath);
}
