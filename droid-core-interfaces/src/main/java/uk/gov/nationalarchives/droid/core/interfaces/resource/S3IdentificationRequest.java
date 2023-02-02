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
import java.util.Iterator;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import net.byteseek.io.reader.ReaderInputStream;
import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.FileReader;
import net.byteseek.io.reader.cache.TopAndTailFixedLengthCache;
import net.byteseek.io.reader.cache.WindowCache;
import net.byteseek.io.reader.windows.Window;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;

public class S3IdentificationRequest implements IdentificationRequest<Path> {

    private static final int TOP_TAIL_BUFFER_CAPACITY = 8 * 1024 * 1024; // buffer 8Mb on the top and tail of files.

    private final String extension;
    private final String fileName;
    private final long size;
    private WindowReader fileReader;
    private final RequestIdentifier identifier;
    private RequestMetaData requestMetaData;
    private Path file;

    /**
     * Constructs a new identification request.
     * @param metaData the metaData about the binary.
     * @param identifier the request's identifier
     */
    public S3IdentificationRequest(final RequestMetaData metaData, final RequestIdentifier identifier)
    {
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
    public final void open(final Path theFile) throws IOException 
    {
    	// Do nothing
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

    /**
     * {@inheritDoc}
     * @throws IOException  on failure to get InputStream
     */
    @Override
    public final InputStream getSourceInputStream() throws IOException 
    {
    	// Create the S3 client
    	final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
    	
    	// Wrap the URI in an S3 URI
    	final AmazonS3URI amazonS3URI = new AmazonS3URI(identifier.getUri());
    	
    	final S3Object s3object = s3Client.getObject(new GetObjectRequest(amazonS3URI.getBucket(), amazonS3URI.getKey())); 
    	
    	return s3object.getObjectContent();
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
    public WindowReader getWindowReader() 
    {
    	return new WindowReader() {

			@Override
			public void close() throws IOException 
			{
				// Do nothing
			}

			@Override
			public Iterator<Window> iterator() 
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int readByte(long position) throws IOException 
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Window getWindow(long position) throws IOException 
			{
				return new Window() 
				{

					@Override
					public byte getByte(int position) throws IOException 
					{
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public byte[] getArray() throws IOException 
					{
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public long getWindowPosition() 
					{
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public long getWindowEndPosition() 
					{
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public long getNextWindowPosition() 
					{
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public int length() 
					{
						return 1024;
					}};
			}

			@Override
			public int getWindowOffset(long position) 
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public long length() throws IOException 
			{
				// TODO Auto-generated method stub
				return 0;
			}};
    }

    /**
     * Return file associate with identification reques.
     * @return File
     */
    public Path getFile() {
        return file;
    }
}
