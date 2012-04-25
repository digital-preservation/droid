/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
