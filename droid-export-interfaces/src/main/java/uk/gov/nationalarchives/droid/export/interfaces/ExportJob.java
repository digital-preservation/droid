/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.export.interfaces;


/**
 * @author rflitcroft
 *
 */
public interface ExportJob {

    /**
     * Executes the job.
     * @param options the job options - How are we going to execute a job if we don't know our job options?
     */
    void executeJob(JobOptions options);

}
