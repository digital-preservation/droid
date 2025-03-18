/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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
import static org.mockito.Mockito.*;

import org.junit.Test;
import uk.gov.nationalarchives.droid.core.signature.ByteReader;
import uk.gov.nationalarchives.droid.core.signature.droid6.InternalSignatureCollection;

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

        String containerFileName = "test.zip";

        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);
        
        match = new ContainerSignatureMatch(sig, -1L);
        
        match.matchFileEntry("entry1", containerFileName);
        assertFalse(match.isMatch());
        
    }

    @Test
    public void testMatchFileEntryWhenAllMatchesExist() {
        
        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("entry1", new ContainerFile());
        files.put("entry2", new ContainerFile());

        String containerFileName = "test.zip";

        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);
        
        match = new ContainerSignatureMatch(sig, -1L);
        assertFalse(match.isMatch());
        
        match.matchFileEntry("entry1", containerFileName);
        assertFalse(match.isMatch());
        
        match.matchFileEntry("entry2", containerFileName);
        assertTrue(match.isMatch());
    }

    @Test
    public void testFileMatches() {
        ContainerMatchUtils.getContainerTestData().forEach(testData -> {
            Map<String, ContainerFile> files = new HashMap<>();
            files.put(testData.pattern(), new ContainerFile());

            ContainerSignature sig = mock(ContainerSignature.class);
            when(sig.getFiles()).thenReturn(files);

            testData.willMatch().forEach(willMatch -> {
                ContainerSignatureMatch match = new ContainerSignatureMatch(sig, -1L);
                match.matchFileEntry(willMatch, testData.containerName());
                assertTrue(match.isMatch());
            });

            testData.willNotMatch().forEach(willNotMatch -> {
                ContainerSignatureMatch match = new ContainerSignatureMatch(sig, -1L);
                match.matchFileEntry(willNotMatch, testData.containerName());
                assertFalse(match.isMatch());
            });
        });
    }

    @Test
    public void shouldMatchBinaryContentByFileNameWithTheFullPathWhenNoBinaryContent() {
        ByteReader binaryContent = null;
        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("header/siardversion/2.1/", new ContainerFile());

        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);

        ContainerSignatureMatch match = new ContainerSignatureMatch(sig, -1L);

        match.matchBinaryContent("header/siardversion/2.1/", binaryContent);
        assertTrue(match.isMatch());
    }

    @Test
    public void shouldDelegateToBinarySignatureMatcherWhenBinarySignatureExists() {
        ByteReader mockBinaryContent = mock(ByteReader.class);
        ContainerFile containerFile = mock(ContainerFile.class);

        Map<String, ContainerFile> files = new HashMap<String, ContainerFile>();
        files.put("header/siardversion/2.1/", containerFile);

        ContainerSignature sig = mock(ContainerSignature.class);
        when(sig.getFiles()).thenReturn(files);

        InternalSignatureCollection mockSignatures = mock(InternalSignatureCollection.class);
        when(containerFile.getCompiledBinarySignatures()).thenReturn(mockSignatures);

        ContainerSignatureMatch match = new ContainerSignatureMatch(sig, -1L);

        match.matchBinaryContent("header/siardversion/2.1/", mockBinaryContent);

        verify(mockSignatures, times(1)).getMatchingSignatures(mockBinaryContent, -1L);
    }

}
