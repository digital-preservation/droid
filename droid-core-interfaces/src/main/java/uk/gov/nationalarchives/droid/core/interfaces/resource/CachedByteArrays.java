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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.map.LazyMap;


/**
 * @author Matt Palmer
 *
 */
public final class CachedByteArrays implements CachedBytes {

    private static final String READ_ONLY = "r";
    
    private final Map<Long, byte[]> cache;
    private File source;
    private RandomAccessFile raf;
    private int bufferCapacity;
    private long currentBufferStart;
    private long currentBufferEnd;
    private byte[] currentBuffer;
    private int limit; // if block is less than cache size.

    /**
     * Creates a new Cached Binary.
     * @param blocks the maximum number of blocks to be held
     * @param blockCapacity the capacity of each block
     * @param blockZero the first block
     * @param limit the size of the bytes in blockZero (may be smaller than blockZero).
     */
    @SuppressWarnings("unchecked")
    public CachedByteArrays(int blocks, int blockCapacity, byte[] blockZero, int limit) {
        cache = LazyMap.decorate(new LRUMap(blocks), new CacheTransformer());
        bufferCapacity = blockCapacity;
        cache.put(0L, blockZero);
        currentBuffer = blockZero;
        currentBufferStart = 0;
        currentBufferEnd = bufferCapacity - 1;
        this.limit = limit;
    }
    
    /**
     * Sets the optional Random Access File for the whole binary.
     * @param file the binary data.
     * @throws IOException if could not close previous random access file.
     */
    void setRaf(RandomAccessFile file) throws IOException {
        if (raf != null) {
            raf.close();
        }
        raf = file;
    }
    
    /**
     * Transformer for building a buffer.
     * @author rflitcroft
     *
     */
    private final class CacheTransformer implements Transformer {
        @Override
        public Object transform(Object input) {
            final Long block = (Long) input;
            try {
                final byte[] buffer = new byte[bufferCapacity];
                raf.seek(block);
                ResourceUtils.readBuffer(raf, buffer);
                return buffer;
            } catch (IOException e) {
                final String message = "Error reading from file into CachedByteArray.";
                throw new FunctorException(message, e);
            }
        }
    }
    
    /**
     * Gets the byte at the given position.
     * @param position the position, p
     * @return the byte at position p
     */
    @Override
    public byte readByte(long position) {
        if (position > currentBufferEnd || position < currentBufferStart) {
            final long blockStart = position - (position % bufferCapacity);
            currentBuffer = cache.get(blockStart);
            currentBufferStart = blockStart;
            currentBufferEnd = blockStart + bufferCapacity - 1;
        }
        return currentBuffer[(int) (position - currentBufferStart)];
    }
    

    /**
     * Closes the internal Random Access File.
     * @throws IOException if the file could not be closed.
     */
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    /**
     * Gets the internal buffer cache.
     * @return the internal buffer cache
     */
    Map<Long, byte[]> getBuffer() {
        return cache;
    }


    /**
     * Gets the internal random access file.
     * @return the internal random access file.
     */
    RandomAccessFile getRaf() {
        return raf;
    }

    /**
     * @return the source input stream
     * @throws IOException if there was an exception reading the source
     */
    public InputStream getSourceInputStream() throws IOException {
        InputStream in = null;
        try {
            if (raf == null) {
                byte[] bytes = cache.get(0L);
                if (bytes.length > limit) {
                    bytes = Arrays.copyOf(bytes, limit);
                }
                in = new ByteArrayInputStream(bytes);
            } else {
                in = new FileInputStream(source);
            }
            return in;
        //CHECKSTYLE:OFF    
        } catch (RuntimeException ex) {
        //CHECKSTYLE:ON
            if (in != null) {
                in.close();
            }
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getSourceFile() {
        return source;
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public void setSourceFile(File sourceFile) throws IOException {
        this.source = sourceFile;
        if (raf != null) {
            raf.close();
        }
        raf = new RandomAccessFile(source, READ_ONLY);
    }

}
