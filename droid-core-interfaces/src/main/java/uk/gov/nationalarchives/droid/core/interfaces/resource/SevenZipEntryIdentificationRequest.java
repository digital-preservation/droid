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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;

//CHECKSTYLE:OFF - getting wrong import order - no idea why.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//CHECKSTYLE:ON

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;




/**
 *
 */
public class SevenZipEntryIdentificationRequest implements IdentificationRequest<InputStream> {

    private static final int TOP_TAIL_CAPACITY = 2 * 1024 * 1024; // hold 2Mb cache on either end of zip entry.

    private long size;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private WindowReader reader;
    private final RequestIdentifier identifier;
    private RequestMetaData requestMetaData;
    private final String extension;
    private final String fileName;
    private final Path tempDir;


    /**
     *
     * @param metaData m
     * @param identifier i
     * @param tempDirLocation t
     */
    public SevenZipEntryIdentificationRequest(final RequestMetaData metaData, final RequestIdentifier identifier, final Path tempDirLocation) {
        this.identifier = identifier;
        this.requestMetaData = metaData;
        this.size = requestMetaData.getSize();
        this.tempDir = tempDirLocation;
        fileName = metaData.getName();
        extension = ResourceUtils.getExtension(fileName);
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

    @Override
    public final String getFileName() {
        return fileName;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public final void close() throws IOException {
        reader.close();
    }

    @Override
    public final InputStream getSourceInputStream() throws IOException {
        return new ReaderInputStream(reader, false);
    }

    /**
     *
     * @param in i
     * @throws IOException if open unsuccessful
     */
    public final void open(final InputStream in) throws IOException {
        reader = ResourceUtils.getStreamReader(in, tempDir, TOP_TAIL_CAPACITY, true);
        reader.length();
    }

    @Override
    public RequestMetaData getRequestMetaData() {
        return requestMetaData;
    }

    @Override
    public final RequestIdentifier getIdentifier() {
        return identifier;
    }


}
