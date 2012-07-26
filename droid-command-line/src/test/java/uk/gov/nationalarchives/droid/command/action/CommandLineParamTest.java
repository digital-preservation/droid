/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
