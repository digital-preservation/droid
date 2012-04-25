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
public interface Aggregator {

    /**
     * Adds the report data given to an aggregator.
     * @param reportData the data to include in an aggregate.
     */
    void aggregate(ReportData reportData);
}
