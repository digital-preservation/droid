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
package uk.gov.nationalarchives.droid.core.interfaces.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Utilities.
 * @author rflitcroft
 *
 */
public final class ArchiveFileUtils {

    /**
     * 
     */
    private static final String TEMP_FILENAME_PREFIX = "droid-archive~";
    private static final String SSP_DELIMITER = ":/";
    private static final String ARCHIVE_DELIMITER = "!/";
    private static final int WRITE_BUFFER_CAPACITY = 8192;

    private ArchiveFileUtils() { };
    
    /**
     * Builds a URI for a zip file entry.
     * @param parent the parent zip file.
     * @param zipEntry the zip entry
     * @return the URI
     */
    public static URI toZipUri(URI parent, String zipEntry) {
        
        String parentScheme = parent.getScheme();
        String parentSsp = parent.getSchemeSpecificPart();
        
        try {
            return new URI("zip:" + parentScheme,
                    parentSsp + ARCHIVE_DELIMITER + FilenameUtils.separatorsToUnix(zipEntry), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Builds a URI for a tar file entry.
     * @param parent the parent tar file.
     * @param tarEntry the tar entry
     * @return the URI
     */
    public static URI toTarUri(URI parent, String tarEntry) {
        String parentScheme = parent.getScheme();
        String parentSsp = parent.getSchemeSpecificPart();

        try {
            return new URI("tar:" + parentScheme, 
                    parentSsp + ARCHIVE_DELIMITER + FilenameUtils.separatorsToUnix(tarEntry), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Write contents of <code>buffer</code> to a temporary file, followed by the remaining bytes
     * in <code>channel</code>.
     * <p/>
     * <p>The bytes are read from <code>buffer</code> from the current position to its limit.
     *
     * @param buffer  contains the contents of the channel read so far
     * @param channel the rest of the channel
     * @param tempDir the directory in which to create the temp file
     * @return <code>File</code> object for the temporary file.
     * @throws java.io.IOException if there is a problem writing to the file
     */
    public static File writeEntryToTemp(File tempDir, ByteBuffer buffer, 
            ReadableByteChannel channel) throws IOException {
        final File tempFile = File.createTempFile(TEMP_FILENAME_PREFIX, null, tempDir);
        // NEVER use deleteOnExit() for long running processes.
        // It can cause the JVM to track the files to delete, which 
        // is a memory leak for long running processes.  Leaving the code and comments in 
        // here as a warning to any future developers.
        // Temporary files created must be deleted by the code requesting the file
        // once they are no longer needed.
        // DO NOT USE!!!: tempFile.deleteOnExit();
        final FileChannel out = (new FileOutputStream(tempFile)).getChannel();
        
        try {
            out.write(buffer);
    
            final ByteBuffer buf = ByteBuffer.allocate(WRITE_BUFFER_CAPACITY);
            buf.clear();
            while (channel.read(buf) >= 0 || buf.position() != 0) {
                buf.flip();
                out.write(buf);
                buf.compact();    // In case of partial write
            }
            return tempFile;
            //CHECKSTYLE:OFF
        } catch (RuntimeException ex) {
            //CHECKSTYLE:ON
            // don't leave temp files lying around if something went wrong.
            if (out != null) {
                out.close();
            }
            if (channel != null) {
                channel.close();
            }
            if (tempFile != null) {
                tempFile.delete();
            }
            throw ex;
        } finally {
            out.close();
        }
    }

    /**
     * Write contents of <code>buffer</code> to a temporary file, followed by the remaining bytes
     * in <code>channel</code>.
     * <p/>
     * <p>The bytes are read from <code>buffer</code> from the current position to its limit.
     *
     * @param tempDir the directory in which to create the temp file
     * @param buffer the initial buffer containing the first part of the file.
     * @param in the input stream containing the rest of the file to write out.
     * @return <code>File</code> object for the temporary file.
     * @throws java.io.IOException if there is a problem writing to the file
     */
    public static File writeEntryToTemp(File tempDir, byte[] buffer, 
            InputStream in) throws IOException {
        final File tempFile = File.createTempFile(TEMP_FILENAME_PREFIX, null, tempDir);
        // NEVER use deleteOnExit() for long running processes.
        // It can cause the JVM to track the files to delete, which 
        // is a memory leak for long running processes.  Leaving the code and comments in 
        // here as a warning to any future developers.
        // Temporary files created must be deleted by the code requesting the file
        // once they are no longer needed.
        // DO NOT USE!!!: tempFile.deleteOnExit();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
            final byte[] buf = new byte[WRITE_BUFFER_CAPACITY];
            try {
                // write the first buffer out:
                out.write(buffer);
                int bytesRead = in.read(buf);
                while (bytesRead > 0) {
                    out.write(buf, 0, bytesRead);
                    bytesRead = in.read(buf);
                }
                return tempFile;
            } finally {
                if (out != null) {
                    out.close();
                }
            }
          //CHECKSTYLE:OFF
        } catch (RuntimeException ex) {
          //CHECKSTYLE:ON
            // don't leave temp files lying around if something went wrong.
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (tempFile != null) {
                tempFile.delete();
            }
            throw ex;
        }
    }
    
    
    
    /**
     * @param parent the container file
     * @return a GZIP URI
     */
    public static URI toGZipUri(URI parent) {
        
        String parentScheme = parent.getScheme();
        String parentSsp = parent.getSchemeSpecificPart();
        
        try {
            return new URI("gz:" + parentScheme,
                    parentSsp + ARCHIVE_DELIMITER
                    + GzipUtils.getUncompressedFilename(FilenameUtils.getName(parent.getSchemeSpecificPart())), null); 
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param requestUri a uri
     * @return the URI needed to replay this uri.
     */
    public static URI toReplayUri(URI requestUri) {
        String originSsp = StringUtils.substringBetween(requestUri.toString(), SSP_DELIMITER, "!");
        String scheme = StringUtils.substringBefore(requestUri.toString(), SSP_DELIMITER);
        if (originSsp != null) {
            return URI.create(StringUtils.substringAfterLast(scheme, ":") + SSP_DELIMITER + originSsp);
        }
        
        return requestUri;
    }
    
    /**
     * 
     * @param path The path of a directory
     * @return String[] a string array containing the parent folders, each of
     *         which is a path in its own right (not just the names of each individual folder)
     */
    public static List<String> getAncestorPaths(String path) {
        ArrayList<String> paths = new ArrayList<String>();
        if (path != null && !path.isEmpty()) {
            String processPath = path;
            int lastSeparator = processPath.length() - 1;
            while (lastSeparator >= 0) {
                String separator = path.substring(lastSeparator, lastSeparator + 1);
                processPath = processPath.substring(0, lastSeparator);
                paths.add(processPath + separator);
                lastSeparator = FilenameUtils.indexOfLastSeparator(processPath);
            }
        }
        return paths; 
    }

}
