/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.signature;

import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * Callback interface to implemented by calsses which handle Formats.
 * @author rflitcroft
 *
 */
public interface FormatCallback {
    
    /**
     * Invoked when a format needs handling.
     * @param format the format to handle
     */
    void onFormat(Format format);

}
