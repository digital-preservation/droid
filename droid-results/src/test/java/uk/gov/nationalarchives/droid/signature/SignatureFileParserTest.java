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
package uk.gov.nationalarchives.droid.signature;

import java.io.File;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.nationalarchives.droid.core.interfaces.signature.ErrorCode;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileException;
import uk.gov.nationalarchives.droid.profile.referencedata.Format;

/**
 * @author rflitcroft
 * 
 */
public class SignatureFileParserTest {

    @Test
    public void testParseNonExistentSigFileGivesSignatureFileException() {

        assertFalse(new File("i_do_not_exist").exists());

        URI uri = new File("i_do_not_exist").toURI();
        try {
            SignatureParser parser = new SaxSignatureFileParser(uri);
            parser.formats(null);
            fail("Expected SignatureFileException.");
        } catch (SignatureFileException e) {
            assertEquals("Signature file does not exist [" + uri + "]", e
                    .getMessage());
            assertEquals(ErrorCode.FILE_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void testParseDirectoryAsSigFileGivesSignatureFileException() {
        assertTrue(new File("test_sig_files").isDirectory());

        URI uri = new File("test_sig_files").toURI();
        try {
            SignatureParser parser = new SaxSignatureFileParser(uri);
            parser.formats(null);
            fail("Expected SignatureFileException.");
        } catch (SignatureFileException e) {
            assertEquals("Invalid signature file [" + uri + "]", e.getMessage());
            assertEquals(ErrorCode.INVALID_SIGNATURE_FILE, e.getErrorCode());
        }
    }

    @Test
    @Ignore("Hard to validate doc when no grammar specified!")
    public void testParseInvalidSigFileGivesSignatureFileException() {
        URI uri = new File("test_sig_files/not_valid_sig_file.xml").toURI();
        try {
            SignatureParser parser = new SaxSignatureFileParser(uri);
            parser.formats(null);
            fail("Expected SignatureFileException.");
        } catch (SignatureFileException e) {
            assertEquals("Invalid signature file [" + uri + "]", e.getMessage());
            assertEquals(ErrorCode.INVALID_SIGNATURE_FILE, e.getErrorCode());
        }
    }

    @Test
    public void testParseMalformedSigFileGivesSignatureFileException() {
        assertTrue(new File("test_sig_files").exists());
        URI uri = new File("test_sig_files/malformed.xml").toURI();
        try {
            SignatureParser parser = new SaxSignatureFileParser(uri);
            parser.formats(null);
            fail("Expected SignatureFileException.");
        } catch (SignatureFileException e) {
            assertEquals("Invalid signature file [" + uri + "]", e.getMessage());
            assertEquals(ErrorCode.INVALID_SIGNATURE_FILE, e.getErrorCode());
        }
    }

    @Test
    public void testParseAllFileFormatsGivesCollectionOfAllFileFormats()
        throws SignatureFileException {
        URI uri = new File("test_sig_files/DROID_SignatureFile_V26.xml")
                .toURI();
        SaxSignatureFileParser parser = new SaxSignatureFileParser(uri);

        FormatCallback callback = mock(FormatCallback.class);

        ArgumentCaptor<Format> formatCaptor = ArgumentCaptor
                .forClass(Format.class);
        parser.formats(callback);

        verify(callback, times(689)).onFormat(formatCaptor.capture());

        List<Format> formats = formatCaptor.getAllValues();
        boolean foundPDF = false;
        for (Format format : formats) {
            if ("fmt/145".equals(format.getPuid())) {
                assertEquals(
                        "Acrobat PDF/X - Portable Document Format - Exchange 1:2001",
                        format.getName());
                assertEquals("application/pdf", format.getMimeType());
                foundPDF = true;
            }
        }

        assertTrue(foundPDF);
    }
}
