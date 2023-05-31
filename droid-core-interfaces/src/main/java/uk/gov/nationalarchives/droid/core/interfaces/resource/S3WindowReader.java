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

public class S3WindowReader implements WindowReader {
    private static final int BUFFER_SIZE = 1024;
    private static final int DWORD_SIZE = 4;

    class S3Window implements Window {
        private final long windowPosition;
        private final byte[] buffer;

        public S3Window(long windowPosition) throws IOException {
            this.windowPosition = windowPosition;

            int bufferSize = BUFFER_SIZE;
            if (windowPosition + bufferSize >= size) {
                bufferSize = (int) (size - windowPosition);
            }

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
            byte[] data = new byte[DWORD_SIZE];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                try {
                    byteArrayOutputStream.write(data, 0, nRead);
                } catch (RuntimeException re) {
                    System.out.println(re.getMessage());
                    throw re;
                }
            }
            byteArrayOutputStream.flush();

            // Shut the S3 stream
            inputStream.close();

            // Store the received data in this Window's buffer
            buffer = byteArrayOutputStream.toByteArray();
        }

        @Override
        public byte getByte(int position) throws IOException {
            System.out.println("Window getByte called");
            return buffer[position];
        }

        @Override
        public byte[] getArray() throws IOException {
            System.out.println("Window getArray called");
            return buffer;
        }

        @Override
        public long getWindowPosition() {
            System.out.println("Window getWindowPosition called");
            return windowPosition;
        }

        @Override
        public long getWindowEndPosition() {
            System.out.println("Window getWindowEndPosition called");
            return windowPosition + buffer.length;
        }

        @Override
        public long getNextWindowPosition() {
            System.out.println("Window getNextWindowPosition called");
            return windowPosition + buffer.length + 1;
        }

        @Override
        public int length() {
            return buffer.length;
        }

        public boolean contains(long position) {
            if (position < windowPosition) {
                return false;
            }
            return position < windowPosition + buffer.length;
        }
    }

    private final AmazonS3URI amazonS3URI;
    private final AmazonS3 s3Client;
    private final long size;

    // A cache of windows
    private final List<S3Window> windows = new ArrayList<>();

    public S3WindowReader(AmazonS3 s3Client, URI uri) {
        System.out.println("S3WindowReader <init> called");

        this.s3Client = s3Client;
        amazonS3URI = new AmazonS3URI(uri);

        size = s3Client.getObjectMetadata(amazonS3URI.getBucket(), amazonS3URI.getKey()).getContentLength();
    }

    @Override
    public void close() throws IOException {
        System.out.println("WindowReader close called");
        // Do nothing
    }

    @Override
    public Iterator<Window> iterator() {
        System.out.println("WindowReader iterator called");
        return null;
    }

    @Override
    public int readByte(long position) throws IOException {
        System.out.println("WindowReader readByte called");
        return 0;
    }

    @Override
    public Window getWindow(long position) throws IOException {
        System.out.println("WindowReader getWindow called " + position);

        if (position >= size) {
            return null;
        }

        for (S3Window window : windows) {
            if (window.contains(position)) {
                return window;
            }
        }

        final S3Window result = new S3Window(position);
        windows.add(result);
        return result;
    }

    @Override
    public int getWindowOffset(long position) {
        final int result = (int) (position / BUFFER_SIZE);
        System.out.println("WindowReader getWindowOffset " + position + " called, result " + result);
        return result;
    }

    @Override
    public long length() throws IOException {
        System.out.println("WindowReader length called " + size);
        return size;
    }
}
