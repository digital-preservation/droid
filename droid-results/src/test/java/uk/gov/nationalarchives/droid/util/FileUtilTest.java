/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class FileUtilTest {

    @Test
    public void testFormatFileSize() {
        
        final long kb12 = 12L * 1024L;
        final long mb12 = 12L * 1000L * 1024L;
        final long gb12 = 12L * 1024L * 1024L * 1024L;
        final long tb12 = 12L * 1024L * 1024L * 1024L * 1024L;
        
        
        assertEquals("12 bytes", FileUtil.formatFileSize(12L, 3));
        assertEquals("12 KB", FileUtil.formatFileSize(kb12, 3));
        assertEquals("11.7 MB", FileUtil.formatFileSize(mb12, 1));
        assertEquals("12 GB", FileUtil.formatFileSize(gb12, 3));
        assertEquals("12,288 GB", FileUtil.formatFileSize(tb12, 3));
    }
}
