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
package uk.gov.nationalarchives.droid.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.bind.JAXBException;

import uk.gov.nationalarchives.droid.core.SignatureParseException;

/**
 * A simple class that reads and parses a container signature file once,
 * and caches the container signature definitions created, so the file isn't repeatedly parsed.
 */
public class ContainerSignatureFileReader {

    private String filePath = "";
    private ContainerSignatureDefinitions definitions;

    /**
     * Empty bean constructor.
     */
    public ContainerSignatureFileReader() {
    }

    /**
     * Parameterized constructor.
     *
     * @param filepath The path of the container signature file to parse.
     */
    public ContainerSignatureFileReader(String filepath) {
        this.filePath = filepath;
    }

    /**
     * Constructs a ContainerSignatureFileReader from a Path.
     * @param path The path to the signature file.
     */
    public ContainerSignatureFileReader(Path path) {
        this.filePath = path.toFile().getPath();
    }

    /**
     * Sets the file path of the container signature file.
     * @param filePath The file path of the signature file.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return The file path of the container signature file.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return The parsed definitions of the container signature file.
     * @throws SignatureParseException If there was any problem reading or parsing the signature file.
     */
    public synchronized ContainerSignatureDefinitions getDefinitions() throws SignatureParseException {
        if (definitions == null) {
            try {
                ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
                try (FileInputStream containerFileStream = new FileInputStream(new File(filePath))) {
                    definitions = parser.parse(containerFileStream);
                }
            } catch (JAXBException | IOException ex) {
                throw new SignatureParseException(ex.getMessage(), ex);
            }
        }
        return definitions;
    }
}
