package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.windows.Window;

public class S3WindowReader implements WindowReader
{
	static final int BUFFER_SIZE = 1024;
	
	class S3Window implements Window 
	{
		private final long windowPosition;
		private final byte[] buffer;
		
		public S3Window(long windowPosition) throws IOException 
		{
			this.windowPosition = windowPosition;
			
			int bufferSize = BUFFER_SIZE;
			if (windowPosition + bufferSize >= size)
				bufferSize = (int)(size - windowPosition);
			
			// Pick off the bucket and the object key from the URI
			GetObjectRequest getS3ObjectRequest = new GetObjectRequest(amazonS3URI.getBucket(), amazonS3URI.getKey());
			
			// Tell S3 which chunk we would like to download
			getS3ObjectRequest.setRange(windowPosition, bufferSize);
			
			// Use the request to obtain a reference to the S3 object
			final S3Object s3object = s3Client.getObject(getS3ObjectRequest);
			
			// Create an in-memory stream to handle the arrival of the data we requested from S3
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			
			// Kick off the download of the data
			S3ObjectInputStream inputStream = s3object.getObjectContent();

			// Copy over the data from the S3 stream to the in-memory stream 
			int nRead;
		    byte[] data = new byte[4];
		    while ((nRead = inputStream.read(data, 0, data.length)) != 0) 
		        byteArrayOutputStream.write(data, 0, nRead);
		    byteArrayOutputStream.flush();
		    
		    // Shut the S3 stream
		    inputStream.close();
		    
		    // Store the received data in this Window's buffer
		    buffer = byteArrayOutputStream.toByteArray();
		}

		@Override
		public byte getByte(int position) throws IOException 
		{
			System.out.println("Window getByte called");
			return buffer[position];
		}

		@Override
		public byte[] getArray() throws IOException 
		{
			System.out.println("Window getArray called");
			return buffer;
		}

		@Override
		public long getWindowPosition() 
		{
			System.out.println("Window getWindowPosition called");
			return windowPosition;
		}

		@Override
		public long getWindowEndPosition() 
		{
			System.out.println("Window getWindowEndPosition called");
			return windowPosition + buffer.length;
		}

		@Override
		public long getNextWindowPosition() 
		{
			System.out.println("Window getNextWindowPosition called");
			return windowPosition + buffer.length + 1;
		}

		@Override
		public int length() 
		{
			return buffer.length;
		}

		public boolean contains(long position) 
		{
			if (position < windowPosition)
				return false;
			if (position >= windowPosition + buffer.length)
				return false;
			return true;
		}
	}
	
	private final AmazonS3URI amazonS3URI;
	private final AmazonS3 s3Client;
	private final long size;
	
	// A cache of windows
	private final List<S3Window> windows = new ArrayList<>();
	
	public S3WindowReader(AmazonS3 s3Client, URI uri) 
	{
    	System.out.println("S3WindowReader <init> called");
    	
    	this.s3Client = s3Client; 
    	amazonS3URI = new AmazonS3URI(uri);
    	
    	size = s3Client.getObjectMetadata(amazonS3URI.getBucket(), amazonS3URI.getKey()).getContentLength();
	}

	@Override
	public void close() throws IOException 
	{
		System.out.println("WindowReader close called");
		// Do nothing
	}

	@Override
	public Iterator<Window> iterator() 
	{
		System.out.println("WindowReader iterator called");
		return null;
	}

	@Override
	public int readByte(long position) throws IOException 
	{
		System.out.println("WindowReader readByte called");
		return 0;
	}
	
	@Override
	public Window getWindow(long position) throws IOException 
	{
		System.out.println("WindowReader getWindow called " + position);
		
		if (position >= size)
			return null;
		
		for (S3Window window : windows)
			if (window.contains(position))
				return window;
		
		final S3Window result = new S3Window(position);
		windows.add(result);
		return result;
	}

	@Override
	public int getWindowOffset(long position) 
	{
		final int result = (int)(position / BUFFER_SIZE);
		System.out.println("WindowReader getWindowOffset " +  position + " called, result " + result);
		return result;
	}

	@Override
	public long length() throws IOException 
	{
		System.out.println("WindowReader length called " + size);
		return size;
	}
}
