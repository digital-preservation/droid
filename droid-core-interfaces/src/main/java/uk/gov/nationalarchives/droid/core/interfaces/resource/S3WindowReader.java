package uk.gov.nationalarchives.droid.core.interfaces.resource;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import net.byteseek.io.reader.WindowReader;
import net.byteseek.io.reader.windows.Window;

public class S3WindowReader implements WindowReader
{
	class S3Window implements Window 
	{
		private final long startingByte;
		private final byte[] buffer;
		
		public S3Window(long startingByte) throws IOException 
		{
			this.startingByte = startingByte;
			this.buffer = new byte[1024];
			s3object.getObjectContent().read(buffer);
		}

		@Override
		public byte getByte(int position) throws IOException 
		{
			System.out.println("Window getByte called");
			return 0;
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
			return 0;
		}

		@Override
		public long getWindowEndPosition() 
		{
			System.out.println("Window getWindowEndPosition called");
			return 0;
		}

		@Override
		public long getNextWindowPosition() 
		{
			System.out.println("Window getNextWindowPosition called");
			return 0;
		}

		@Override
		public int length() 
		{
			System.out.println("Window length called");
			return 1024;
		}
	}
	
	private final AmazonS3URI amazonS3URI;
	private final AmazonS3 s3Client;
	private S3Object s3object;
	
	private S3Window currentWindow; 
	
	public S3WindowReader(URI uri) 
	{
    	System.out.println("S3WindowReader <init> called");
    	
    	s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
    	amazonS3URI = new AmazonS3URI(uri);
    	s3object = s3Client.getObject(new GetObjectRequest(amazonS3URI.getBucket(), amazonS3URI.getKey())); 
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
		System.out.println("WindowReader getWindow called");
		
		// TODO apply the position
		if (currentWindow != null)
			return currentWindow;
		currentWindow = new S3Window(position);
		return currentWindow;
	}

	@Override
	public int getWindowOffset(long position) 
	{
		System.out.println("WindowReader getWindowOffset called");
		return 0;
	}

	@Override
	public long length() throws IOException 
	{
		System.out.println("WindowReader length called");
		return 0;
	}
}
