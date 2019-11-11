/*
 * Copyright (c) 2019, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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

import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.BINARY;
import static uk.gov.nationalarchives.droid.core.signature.compiler.SignatureType.CONTAINER;

public class SigUtils {

    private static BinarySignatureXMLParser<ByteSequence> xmlParser = new BinarySignatureXMLParser<>();

    public static SignatureType getSigFileType(Document doc) {
        SignatureType returnValue = null;
        if (doc != null) {
            Element root = doc.getDocumentElement();
            switch (root.getNodeName()) {
                case "ContainerSignatureMapping":   returnValue = CONTAINER; break;
                case "FFSignatureFile":             returnValue = BINARY; break;
            }
        }
        return returnValue;
    }

    public static ContainerSignatureDefinitions readContainerSignatures(String filename) {
        ContainerSignatureDefinitions containerSignatureDefinitions = null;
        if (filename != null) {
            final Path containerSignaturesFile = Paths.get(filename);
            if (!Files.exists(containerSignaturesFile)) {
                throw new IllegalArgumentException("Container signature file not found");
            }
            try(final InputStream in = new BufferedInputStream(Files.newInputStream(containerSignaturesFile))) {
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

    public static FFSignatureFile readBinarySignatures(String filename) {
        File theFile = new File(filename);
        SignatureFileParser parser = new SignatureFileParser();
        try {
            return parser.parseSigFile(theFile.toPath());
        } catch (SignatureParseException e) {
            throw new IllegalArgumentException("Can't parse the binary signature file: " + filename);
        }
    }

    public static ByteSequence parseByteSequenceXML(Element byteSequenceElement) throws SignatureParseException {
        return xmlParser.fromXmlElement(byteSequenceElement);
    }


}
