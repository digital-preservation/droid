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
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.domesdaybook.reader.ByteReader;

//CHECKSTYLE:OFF - getting wrong import order - no idea why.
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//CHECKSTYLE:ON

import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFileUtils;

/**
 * @author rflitcroft
 *
 */
public class GZipIdentificationRequest implements IdentificationRequest {

    private static final int BUFFER_CACHE_CAPACITY = 16;
    private static final int CAPACITY = 32 * 1024; 

    private final String extension;
    private final String fileName;
    private long size;

    private CachedBytes cachedBinary;
    private File tempFile;

    private final int bufferCapacity;
    private final int lruCapacity;
    private File tempDir;
    
    private RequestMetaData requestMetaData;
    private final RequestIdentifier identifier;
    
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Constructs a new GZip file resource.
     * @param metaData the name of the tar entry
     * @param identifier request identification object
     * @param tempDir the location to write temp files.
     */
    public GZipIdentificationRequest(RequestMetaData metaData, RequestIdentifier identifier, File tempDir) {
        this(metaData, identifier, BUFFER_CACHE_CAPACITY, CAPACITY, tempDir);
    }

    /**
     * Constructs a new GZip file resource.
     * @param metaData the name of the tar entry
     * @param identifier request identification object
     * @param bufferCapacity the buffer cache capacity
     * @param lruCapacity the cache block size
     * @param tempDir the location to write temp files.
     */
    GZipIdentificationRequest(RequestMetaData metaData, RequestIdentifier identifier,
            int lruCapacity, int bufferCapacity, File tempDir) {
        this.identifier = identifier;
        
        String path = identifier.getUri().getSchemeSpecificPart();
        //extension = FilenameUtils.getExtension(path);
        extension = ResourceUtils.getExtension(path);
        fileName = FilenameUtils.getName(path);
        this.bufferCapacity = bufferCapacity;
        this.lruCapacity = lruCapacity;
        this.tempDir = tempDir;
        this.requestMetaData = metaData;

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void open(InputStream in) throws IOException {
        /* using normal stream access and CachedByteArrays */
        byte[] firstBuffer = new byte[bufferCapacity];
        int bytesRead = ResourceUtils.readBuffer(in, firstBuffer);
        if (bytesRead < 1) {
            firstBuffer = new byte[0];
            cachedBinary = new CachedByteArray(firstBuffer, 0);
            size = 0L;
        } else if (bytesRead < bufferCapacity) {
            // size the buffer to the amount of bytes available:
            // firstBuffer = Arrays.copyOf(firstBuffer, bytesRead);
            cachedBinary = new CachedByteArray(firstBuffer, bytesRead);
            size = (long) bytesRead;
        } else {
            cachedBinary = new CachedByteArrays(lruCapacity, bufferCapacity, firstBuffer, bufferCapacity);
            tempFile = ArchiveFileUtils.writeEntryToTemp(tempDir, firstBuffer, in);
            cachedBinary.setSourceFile(tempFile);
            size = tempFile.length();
        }                
        /* using nio and byte buffers:
        ReadableByteChannel channel = Channels.newChannel(in);
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferCapacity);
        
        int bytesRead = 0;
        do {
            bytesRead = channel.read(buffer);
        } while (bytesRead >= 0 && buffer.hasRemaining());
        
        cachedBinary = new CachedByteBuffers(lruCapacity, bufferCapacity, buffer);
        if (buffer.limit() == buffer.capacity()) {
            tempFile = ArchiveFileUtils.writeEntryToTemp(tempDir, buffer, channel);
            cachedBinary.setSourceFile(tempFile);
            size = tempFile.length();
            
        } else {
            size = buffer.limit();
        }
        */
    }

    /**
     * Releases resources for this resource.
     * @throws IOException if the resource could not be closed
     */
    @Override
    public final void close() throws IOException {
        try {
            if (cachedBinary != null) {
                cachedBinary.close();
                cachedBinary = null;
            }
        } finally {
            if (tempFile != null) {
                if (!tempFile.delete() && tempFile.exists()) {
                    String message = String.format("Could not delete temporary file [%s] for gzip entry [%s]. "
                            + "Will try to delete on exit.",
                            tempFile.getAbsolutePath(), identifier.getUri().toString());
                    log.warn(message);
                    tempFile.deleteOnExit();
                }
                tempFile = null;
            }
        }
    }
    
    /**
     * Really ensure that temporary files are deleted, if the close method is not called.  
     * Do not rely on this - this is just a double-double safety measure to avoid leaving 
     * temporary files hanging around.
     * {@inheritDoc}
     */
    @Override
    //CHECKSTYLE:OFF
    public void finalize() throws Throwable {
    //CHECKSTYLE:ON
        try {
            close();
        } finally {
            super.finalize();
        }
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public final byte getByte(long position) {
        return cachedBinary.readByte(position);
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
     * @return the internal binary cache;
     */
    final CachedBytes getCache() {
        return cachedBinary;
    }
    
    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public final InputStream getSourceInputStream() throws IOException {
        return cachedBinary.getSourceInputStream();
    }
    
    /**
     * @return the tempFile
     */
    final File getTempFile() {
        return tempFile;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public final ByteReader getReader() {
        return cachedBinary;
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public final File getSourceFile() throws IOException {
        if (tempFile == null) {
            final InputStream stream = cachedBinary.getSourceInputStream();
            try {
                tempFile = ResourceUtils.createTemporaryFileFromStream(tempDir, stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
        return tempFile;
    }

}
