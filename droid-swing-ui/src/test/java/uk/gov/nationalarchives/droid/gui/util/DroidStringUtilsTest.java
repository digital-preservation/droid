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
