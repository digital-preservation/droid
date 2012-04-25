/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.command.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class CommandLineParamTest {

    @Test
    public void testOptions() {
        Options options = CommandLineParam.options();
        assertEquals(CommandLineParam.values().length, options.getOptions().size());
    }
    
    @Test
    public void testExportOptionHasOneRequiredArgument() {
        Option option = CommandLineParam.EXPORT_ONE_ROW_PER_FILE.newOption();
        
        assertTrue("The export option requires an argument.", option.hasOptionalArg());
        assertEquals(1, option.getArgs());
        assertTrue("The export option has an optional argument.", option.hasArg());
    }

    @Test
    public void testProfilesOptionHasUnlimitedRequiredArguments() {
        Option option = CommandLineParam.PROFILES.newOption();
        
        assertTrue("The profile option requires arguments.", option.hasArg());
        assertFalse("The profile option requires arguments.", option.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }
    
    @Test
    public void testVersionHasNoArguments() {
        Option option = CommandLineParam.VERSION.newOption();
        
        assertFalse(option.hasArg());
        assertFalse(option.hasOptionalArg());
        assertEquals(0, option.getArgs());
    }

    @Test
    public void testHelpHasNoArguments() {
        Option option = CommandLineParam.HELP.newOption();
        
        assertFalse(option.hasArg());
        assertFalse(option.hasOptionalArg());
        assertEquals(0, option.getArgs());
    }
}
