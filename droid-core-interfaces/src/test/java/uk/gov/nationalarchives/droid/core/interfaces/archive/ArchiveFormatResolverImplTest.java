/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author rflitcroft
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:archive-spring.xml")
public class ArchiveFormatResolverImplTest {

    @Autowired
    private ArchiveFormatResolverImpl formatResolver;
    
    @Test
    public void testForPuid() {
        assertEquals("ZIP", formatResolver.forPuid("x-fmt/412"));
        assertEquals("ZIP", formatResolver.forPuid("x-fmt/263"));
        assertEquals("TAR", formatResolver.forPuid("x-fmt/265"));
        assertEquals("GZ", formatResolver.forPuid("x-fmt/266"));
        assertNull(formatResolver.forPuid(""));
    }
}
