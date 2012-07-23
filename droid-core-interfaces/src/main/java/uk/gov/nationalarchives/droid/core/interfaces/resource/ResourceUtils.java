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
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.io.FilenameUtils;

/**
 * 
 * @author mpalmer
 *
 */
public final class ResourceUtils {

    private static final int BUFFER_SIZE = 8192;
    
    private static final int NINENTYEIGHT = 98;
    private static final int THIRTYTHREE = 33;
    private static final int NINENTYFOUR = 94;

    private static final int HEX_F = 0xF;
    
    private static final int HEX_7F = 0x7F;

    private static final int UNSIGNED_RIGHT_SHIFT_BY_4 = 4;
    private static final int UNSIGNED_RIGHT_SHIFT_BY_11 = 11;
    private static final int UNSIGNED_RIGHT_SHIFT_BY_18 = 18;
    private static final int UNSIGNED_RIGHT_SHIFT_BY_25 = 25;    
    private static final int ARRAYLENGTH = 5;
    
    /**
     * Private constructor to prevent construction of static utility class.
     */
    private ResourceUtils() {
        throw new UnsupportedOperationException("ExtensionUtils is a static utility class and cannot be constructed.");
    }
    
    /**
     * @param filename The name of the file to get the extension from.
     * @return String The file extension of the supplied filename.
     */
    public static String getExtension(String filename) {
        final String nameOnly = FilenameUtils.getName(filename);
        final int dotPos = nameOnly.lastIndexOf('.');
        return dotPos > 0 ? nameOnly.substring(dotPos + 1) : "";
    }
    
    /**
     * @param tempDir The temp directory to create the temporary file in.
     * @param stream An input stream from which to create a file.
     * @return A file containing the input stream.
     * @throws IOException if the temp dir does not exist or something else goes wrong.
     */
    public static File createTemporaryFileFromStream(File tempDir, InputStream stream) throws IOException {
        final File tempFile = File.createTempFile("droid-temp~", null, tempDir);
        // NEVER use deleteOnExit() for long running processes.
        // It can cause the JVM to track the files to delete, which 
        // is a memory leak for long running processes.  Leaving the code and comments in 
        // here as a warning to any future developers.
        // Temporary files created must be deleted by the code requesting the file
        // once they are no longer needed.
        // DO NOT USE!!!: tempFile.deleteOnExit();
        try {
            final OutputStream out = new FileOutputStream(tempFile);
            try {
                byte[] buf = new byte[BUFFER_SIZE];
                int len;
                while ((len = stream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                return tempFile;
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            // Don't leave temp files lying around.
            if (tempFile != null) {
                tempFile.delete();
            }
            throw ex;
        }
    }
    
    
    /**
     * 
     * @param in  An input stream
     * @param buffer A byte buffer.
     * @return The number of bytes read.
     * @throws IOException if something bad happens
     */
    public static int readBuffer(InputStream in, byte[] buffer) throws IOException {
        int totalBytesRead = 0;
        int bytesToRead = buffer.length;
        while (totalBytesRead < bytesToRead) {
            int numRead = in.read(buffer, totalBytesRead, bytesToRead - totalBytesRead);
            if (numRead == -1) { break; }
            totalBytesRead += numRead;
        }
        return totalBytesRead;
    }
    
    /**
     * 
     * @param file  A random access file.
     * @param buffer A byte buffer.
     * @return The number of bytes read.
     * @throws IOException if something bad happens
     */
    public static int readBuffer(RandomAccessFile file, byte[] buffer) throws IOException {
        int totalBytesRead = 0;
        int bytesToRead = buffer.length;
        while (totalBytesRead < bytesToRead) {
            int numRead = file.read(buffer, totalBytesRead, bytesToRead - totalBytesRead);
            if (numRead == -1) { break; }
            totalBytesRead += numRead;
        }
        return totalBytesRead;
    }    
    
    
    private static long printableValue(long value) {
        return (value < NINENTYFOUR) ? value + THIRTYTHREE : value + NINENTYEIGHT;
    }
    
    /**
     * COnverts an long to base 128 integer.
     * @param value Value to convet to base 128 integer.
     * @return Base 128Integer.
     */

    public static String getBase128Integer(long value) {
        // Use printable characters in this range:
        // ASCII & UTF-8: 33 - 126 (no space) = 94 values.
        // ISO Latin 1 & UTF-8: 192 - 226 = 34 values.
        // Map 0-93 to 33-126
        // Map 93-127 to 192-226
        char[] values = new char[ARRAYLENGTH];
        
        int i = 0;
        values[i++] = (char) printableValue((value >>> UNSIGNED_RIGHT_SHIFT_BY_25) & HEX_7F); // bits 26-32
        values[i++] = (char) printableValue((value >>> UNSIGNED_RIGHT_SHIFT_BY_18) & HEX_7F); // bits 19-25
        values[i++] = (char) printableValue((value >>> UNSIGNED_RIGHT_SHIFT_BY_11) & HEX_7F); // bits 12-18
        values[i++] = (char) printableValue((value >>> UNSIGNED_RIGHT_SHIFT_BY_4) & HEX_7F); // bits 5-11
        values[i++] = (char) printableValue(value & HEX_F); // bits 1-4
        return new String(values);
    }    
    
    
    /**
     * Attempts to delete each temporary file in a directory.
     * 
     * @param tempDir The temporary directory to delete files in.
     */
    public static void attemptToDeleteTempFiles(File tempDir) {
        FileFilter tmpFileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile() && FilenameUtils.isExtension(f.getName(), "tmp"); 
            }
        };
        if (tempDir != null) {
            File[] files = tempDir.listFiles(tmpFileFilter);
            for (File file : files) {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }
    }    
}
