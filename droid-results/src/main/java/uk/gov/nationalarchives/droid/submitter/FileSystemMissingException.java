/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.submitter;

/**
 * @author rflitcroft
 * 
 * THrown when DROID determines that a file system is no longer readable.
 *
 */
public class FileSystemMissingException extends Exception {

    private static final long serialVersionUID = 7152768610612134961L;

    /**
     * 
     * @param message a message describing the exception
     */
    public FileSystemMissingException(String message) {
        super(message);
    }
}
