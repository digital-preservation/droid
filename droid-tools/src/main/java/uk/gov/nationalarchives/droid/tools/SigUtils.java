/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.gov.nationalarchives.droid.container.BinarySignatureXMLParser;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.SignatureFileParser;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType;
import uk.gov.nationalarchives.droid.core.signature.droid6.ByteSequence;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;


/**
 * A static utility class with some useful methods for processing signature files.
 */
public final class SigUtils {

    private static final BinarySignatureXMLParser<ByteSequence> XML_PARSER = new BinarySignatureXMLParser<>();

    /**
     * Private constructor for static utility class.
     */
    private SigUtils() {
    }

    /**
     * Returns whether a signature file is a binary or container signature file.
     * @param doc The XML document.
     * @return The signature type of a signature file, or null if it isn't one of those.
     */
    public static SignatureType getSigFileType(Document doc) {
        SignatureType returnValue = null;
        if (doc != null) {
            Element root = doc.getDocumentElement();
            switch (root.getNodeName()) {
                case "ContainerSignatureMapping":   returnValue = SignatureType.CONTAINER; break;
                case "FFSignatureFile":             returnValue = SignatureType.BINARY; break;
                default: returnValue = null;
            }
        }
        return returnValue;
    }

    /**
     * Returns ContainerSignatureDefinitions from a container signature file.
     * @param filename The name of the container signature file.
     * @return ContainerSignatureDefinitions from a container signature file.
     */
    public static ContainerSignatureDefinitions readContainerSignatures(String filename) {
        ContainerSignatureDefinitions containerSignatureDefinitions = null;
        if (filename != null) {
            final Path containerSignaturesFile = Paths.get(filename);
            if (!Files.exists(containerSignaturesFile)) {
                throw new IllegalArgumentException("Container signature file not found");
            }
            try (final InputStream in = new BufferedInputStream(Files.newInputStream(containerSignaturesFile))) {
                final ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
                return parser.parse(in);
            } catch (final SignatureParseException e) {
                throw new IllegalArgumentException("Can't parse container signature file: " + filename);
            } catch (final IOException | JAXBException ioe) {
                throw new IllegalArgumentException(ioe);
            }
        }
        return null;
    }

    /**
     * Returns FFSignatureFile object from a binary signature file.
     * @param filename The name of the binary signature file.
     * @return FFSignatureFile object from a binary signature file.
     */
    public static FFSignatureFile readBinarySignatures(String filename) {
        File theFile = new File(filename);
        SignatureFileParser parser = new SignatureFileParser();
        try {
            return parser.parseSigFile(theFile.toPath());
        } catch (SignatureParseException e) {
            throw new IllegalArgumentException("Can't parse the binary signature file: " + filename);
        }
    }

    /**
     * Parses a ByteSequence XML fragment into a ByteSequence object.
     * @param byteSequenceElement A ByteSequence XML element.
     * @return A ByteSequence object parsed from an XML element.
     * @throws SignatureParseException If there is a problem parsing the XML element.
     */
    public static ByteSequence parseByteSequenceXML(Element byteSequenceElement) throws SignatureParseException {
        return XML_PARSER.fromXmlElement(byteSequenceElement);
    }


}
