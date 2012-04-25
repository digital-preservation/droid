/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.hash;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class MD5HashGeneratorTest {

    private MD5HashGenerator hashGenerator;
    
    @Before
    public void setup() {
        hashGenerator = new MD5HashGenerator();
    }
    
    @Test
    public void testGenerateHashFromInputStream() throws IOException {
        
        InputStream in = getClass().getClassLoader().getResourceAsStream("hash/commons-collections-3.2.1-bin.zip");
        String hash = hashGenerator.hash(in);
        assertEquals("23925dbfaf3c266b487c264c1a643b51", hash);
    }
}
