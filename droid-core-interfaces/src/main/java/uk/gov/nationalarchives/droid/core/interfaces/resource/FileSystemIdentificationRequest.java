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

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.cache.TopAndTailFixedLengthCache;
import net.byteseek.io.reader.cache.WindowCache;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

/**
 * Encapsulated the binary data for a file system identification request.
 * @author rflitcroft, mpalmer
 *
 */
public class FileSystemIdentificationRequest implements IdentificationRequest<File> {

    private static final int TOP_TAIL_BUFFER_CAPACITY = 8 * 1024 * 1024; // buffer 8Mb on the top and tail of files.

    private final String extension;
    private final String fileName;
    private final long size;
    private WindowReader fileReader;
    private final RequestIdentifier identifier;
    private RequestMetaData requestMetaData;
    private File file;

    /**
     * Constructs a new identification request.
     * @param metaData the metaData about the binary.
     * @param identifier the request's identifier
     */
    public FileSystemIdentificationRequest(final RequestMetaData metaData, final RequestIdentifier identifier) {
        this.identifier = identifier;
        requestMetaData = metaData;
        size = metaData.getSize();
        fileName = metaData.getName();
        extension = ResourceUtils.getExtension(fileName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void open(final File theFile) throws IOException {
        // Use a caching strategy that uses soft references, to allow the GC to reclaim
        // cached file bytes in low memory conditions.
        final WindowCache cache = new TopAndTailFixedLengthCache(theFile.length(), TOP_TAIL_BUFFER_CAPACITY);
        fileReader = new FileReader(theFile, cache);
        ((FileReader) fileReader).useSoftWindows(true);
        this.file = theFile;
        fileReader.getWindow(0); // force read of first block to generate any IO exceptions.
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
     */
    @Override
    public final void close() throws IOException {
        file = null;
        fileReader.close();
    }

    //TODO:MP: can we return something backed by the file reader, which has already cached a lot of the file?
    /**
     * {@inheritDoc}
     * @throws IOException  on failure to get InputStream
     */
    @Override
    public final InputStream getSourceInputStream() throws IOException {
        return new ReaderInputStream(fileReader, false);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final RequestMetaData getRequestMetaData() {
        return requestMetaData;
    }

    /**
     * @return the identifier
     */
    public final RequestIdentifier getIdentifier() {
        return identifier;
    }


    @Override
    public byte getByte(long position) throws IOException {
        final int result = fileReader.readByte(position);
        if (result < 0) {
            throw new IOException("No byte at position " + position);
        }
        return (byte) result;
    }

    @Override
    public WindowReader getWindowReader() {
        return fileReader;
    }
}
