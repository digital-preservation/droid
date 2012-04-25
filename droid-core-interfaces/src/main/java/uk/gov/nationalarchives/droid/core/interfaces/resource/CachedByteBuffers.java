/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Map;

import net.domesdaybook.reader.ByteReader;

//CHECKSTYLE:OFF getting wrong order of import style warnings.
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.map.LazyMap;
//CHECKSTYLE:ON

/**
 * Wraps a binary data source to allow efficient Random Access reading.
 * @author rflitcroft
 *
 */
public final class CachedByteBuffers implements ByteReader, CachedBytes {

    private static final String READ_ONLY = "r";

    private final Map<Long, ByteBuffer> cache;
    private File source;
    private RandomAccessFile raf;
    private final int bufferCapacity;
    private FileChannel channel;
    private long currentBufferStart;
    private long currentBufferEnd;
    private ByteBuffer currentBuffer;
    
    /**
     * Creates a new Cached Binary.
     * @param blocks the maximum number of blocks to be held
     * @param blockCapacity the capacity of each block
     * @param blockZero the first block
     */
    @SuppressWarnings("unchecked")
    public CachedByteBuffers(int blocks, int blockCapacity, ByteBuffer blockZero) {
        cache = LazyMap.decorate(new LRUMap(blocks), new CacheTransformer());
        bufferCapacity = blockCapacity;
        blockZero.flip();
        cache.put(0L, blockZero);
        currentBuffer = blockZero;
        currentBufferStart = 0;
        currentBufferEnd = bufferCapacity - 1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceFile(File sourceFile) throws FileNotFoundException {
        this.source = sourceFile;
        raf = new RandomAccessFile(source, READ_ONLY);
        channel = raf.getChannel();
    }
    
    /**
     * Transformer for building a buffer.
     * @author rflitcroft
     *
     */
    private final class CacheTransformer implements Transformer {
        @Override
        public Object transform(Object input) {
            Long block = (Long) input;
            try {
                ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
                // determine the block
                raf.seek(block);
                channel.read(buffer);
                
                // If the buffer is not full, don't let anyone read past here.
                buffer.flip();
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
        return currentBuffer.get((int) (position - currentBufferStart));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    /**
     * Gets the internal buffer cache.
     * @return the internal buffer cache
     */
    Map<Long, ByteBuffer> getBuffers() {
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
     * {@inheritDoc}
     */
    @Override
    public InputStream getSourceInputStream() throws IOException {
        InputStream in;
        
        if (raf == null) {
            ByteBuffer blockZero = cache.get(0L);
            blockZero.rewind();
            byte[] bytes = new byte[blockZero.limit()];
            blockZero.get(bytes);
            in = new ByteArrayInputStream(bytes);
        } else {
            RandomAccessFile newRaf = new RandomAccessFile(source, READ_ONLY);
            FileChannel chan = newRaf.getChannel();
            chan.position(0);
            in = Channels.newInputStream(chan);
        }
        
        return in;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public File getSourceFile() {
        return source;
    }

}
