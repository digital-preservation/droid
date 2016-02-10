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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 * Identification request encapsulating a zipped resource.
 * @author rflitcroft, mpalmer, boreilly
 *
 */
public class ZipEntryIdentificationRequest implements IdentificationRequest<InputStream> {

    private  static final int TOP_TAIL_CAPACITY = 2 * 1024 * 1024; // hold 2Mb cache on either end of zip entry.

    private final String extension;
    private final String fileName;
    private final RequestMetaData requestMetaData;
    private final RequestIdentifier identifier;

    private File tempDir;
    private Long size;
    private WindowReader reader;

    private boolean closeStream = true;


    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Constructs a new Zip file resource.
     * @param metaData meta data about the request
     * @param identifier request identifier
     * @param tempDir the location to write temp files.
     */
    public ZipEntryIdentificationRequest(RequestMetaData metaData, RequestIdentifier identifier,
                                  File tempDir) {
        this.identifier = identifier;
        this.size = metaData.getSize();
        this.fileName = metaData.getName();
        this.extension = ResourceUtils.getExtension(fileName);
        this.tempDir = tempDir;
        this.requestMetaData = metaData;
    }

    /**
     * Constructs a new Zip file resource.
     * @param metaData meta data about the request
     * @param identifier request identifier
     * @param tempDir the location to write temp files.
     * @param closeStream Whether to close the underlying input stream when the reader created in [open] is closed.
     */
    public ZipEntryIdentificationRequest(RequestMetaData metaData, RequestIdentifier identifier,
                                         File tempDir, boolean closeStream) {
        this(metaData, identifier, tempDir);
        this.closeStream = closeStream;

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void open(final InputStream in) throws IOException {
        reader = ResourceUtils.getStreamReader(in, tempDir, TOP_TAIL_CAPACITY, closeStream);
        // Force read of entire input stream to build reader and remove dependence on source input stream.
        final long readSize = reader.length(); // getting the size of a reader backed by a stream forces a stream read.
        if (readSize != size) {
            String resourceIdentifier = "";
            if (getIdentifier() != null && getIdentifier().getUri() != null) {
                resourceIdentifier = getIdentifier().getUri().toString();
            }

            log.warn("The zip entry " + resourceIdentifier + " states it is " + size
                    + " in length, but reading it produced: " + readSize);
            size = readSize;
        }
    }
    
    /**
     * Releases resources for this resource.
     * @throws IOException if the resource could not be closed
     */
    @Override
    public final void close() throws IOException {
        reader.close();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final String getExtension() {
        return extension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getFileName() {
        return fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long size() {
        return size;
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public final InputStream getSourceInputStream() throws IOException {
        return new ReaderInputStream(reader, false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final RequestMetaData getRequestMetaData() {
        return requestMetaData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final RequestIdentifier getIdentifier() {
        return identifier;
    }


    @Override
    public byte getByte(long position) throws IOException {
        final int result = reader.readByte(position);
        if (result < 0) {
            throw new IOException("No byte at position " + position);
        }
        return (byte) result;
    }

    @Override
    public WindowReader getWindowReader() {
        return reader;
    }
}
