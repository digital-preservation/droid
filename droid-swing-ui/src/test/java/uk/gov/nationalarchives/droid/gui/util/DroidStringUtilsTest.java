/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class DroidStringUtilsTest {

    @Test
    public void testAbbreviate() {
        
        String s = "All work and no play makes Jack a dull boy";
        assertEquals("All w...dull boy", DroidStringUtils.abbreviate(s, 5, 16));
        assertEquals("All w... dull boy", DroidStringUtils.abbreviate(s, 5, 17));
        assertEquals("All w...a dull boy", DroidStringUtils.abbreviate(s, 5, 18));
        assertEquals("All w... makes Jack a dull boy", DroidStringUtils.abbreviate(s, 5, 30));
        assertEquals("A...", DroidStringUtils.abbreviate(s, 5, 4));
        assertEquals("All work and no play makes Jack a dull boy", DroidStringUtils.abbreviate(s, 5, s.length()));
        
        assertEquals("All work and no play makes Jack a dull boy", DroidStringUtils.abbreviate(s, 5, 60));
        assertEquals("All work and no p...", DroidStringUtils.abbreviate(s, 70, 20));
    }
}
