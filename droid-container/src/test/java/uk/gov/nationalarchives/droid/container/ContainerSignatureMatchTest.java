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
package uk.gov.nationalarchives.droid.container;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author rflitcroft
 *
 */
public class ContainerSignatureMatchTest {

    private ContainerSignatureMatch match;
    
    @Test
    public void testConstructorSetsSignature() {
        
        ContainerSignature sig = mock(ContainerSignature.class);
        match = new ContainerSignatureMatch(sig, -1L);
        assertEquals(sig, match.getSignature());
    }
    
    @Test
    public void testMatchFileEntryWhenNoMatchExists() {
        
        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("entry0", new ContainerFile());
        
        
        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);
        
        match = new ContainerSignatureMatch(sig, -1L);
        
        match.matchFileEntry("entry1");
        assertFalse(match.isMatch());
        
    }

    @Test
    public void testMatchFileEntryWhenAllMatchesExist() {
        
        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("entry1", new ContainerFile());
        files.put("entry2", new ContainerFile());
        
        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);
        
        match = new ContainerSignatureMatch(sig, -1L);
        assertFalse(match.isMatch());
        
        match.matchFileEntry("entry1");
        assertFalse(match.isMatch());
        
        match.matchFileEntry("entry2");
        assertTrue(match.isMatch());
    }
    
    @Ignore
    @Test
    public void testNeedsTextMatch() {
        ContainerFile file1 = mock(ContainerFile.class);
        //when(file1.getTextSignature()).thenReturn("sig");
        when(file1.getPath()).thenReturn("entry1");
        
        ContainerFile file2 = mock(ContainerFile.class);
        //when(file2.getTextSignature()).thenReturn(null);
        when(file1.getPath()).thenReturn("entry2");
        
        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("entry1", file1);
        files.put("entry2", file2);
        
        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);

        match = new ContainerSignatureMatch(sig, -1L);
        
        //assertTrue(match.needsTextMatch("entry1"));
        //assertFalse(match.needsTextMatch("entry2"));
    }
    
    @Ignore
    @Test
    public void testMatchTextContent() {
        ContainerFile file1 = mock(ContainerFile.class);
        //when(file1.getTextSignature()).thenReturn("sig");
        when(file1.getPath()).thenReturn("entry1");
        
        ContainerFile file2 = mock(ContainerFile.class);
        //when(file2.getTextSignature()).thenReturn(null);
        when(file1.getPath()).thenReturn("entry2");
        
        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("entry1", file1);
        files.put("entry2", file2);
        
        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);

        match = new ContainerSignatureMatch(sig, -1L);
        
        //match.matchTextContent("entry1", "fig");
        //assertFalse(match.isMatch());

        match.matchFileEntry("entry2");
        assertFalse(match.isMatch());

        //match.matchTextContent("entry1", "sig");
        //assertTrue(match.isMatch());
        
    }

    @Ignore
    @Test(expected = NullPointerException.class)
    public void testMatchTextContentWithFileWithoutTextSignature() {
        ContainerFile file1 = mock(ContainerFile.class);
        //when(file1.getTextSignature()).thenReturn("sig");
        when(file1.getPath()).thenReturn("entry1");
        
        ContainerFile file2 = mock(ContainerFile.class);
        //when(file2.getTextSignature()).thenReturn(null);
        when(file1.getPath()).thenReturn("entry2");
        
        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("entry1", file1);
        files.put("entry2", file2);
        
        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);

        match = new ContainerSignatureMatch(sig, -1L);
        
        //match.matchTextContent("entry2", "sig");
    }
}
