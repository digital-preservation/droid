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
 * @author a-mpalmer
 *
 */
public enum ExportOptions {
    
    /**
     * Exports one row of a csv file per format.
     */
    ONE_ROW_PER_FORMAT,
    
    /**
     * Exports one row of a csv file per file.
     */
    ONE_ROW_PER_FILE
}
