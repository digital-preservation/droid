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
package uk.gov.nationalarchives.droid.profile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.schlichtherle.util.zip.BasicZipFile;
import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipOutputStream;

import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;

/**
 * @author rflitcroft
 * 
 */
public class ProfileDiskAction {

    private static final int UNITY_PERCENT = 100;
    private static final int BUFFER_SIZE = 8192;
    private static final char FORWARD_SLASH = '/';
    private static final char BACKWARD_SLASH = '\\';

    
    private final Log log = LogFactory.getLog(getClass());
    
    /**
     * Saves the profile to disk by zipping it up.
     * 
     * @param baseDir
     *            the base direcytory of the zip operation
     * @param destination
     *            the ntarget zip file
     * @param callback
     *            a progress observer, notified on progress
     * @throws IOException
     *             if a file IO operation failed
     */
    public void saveProfile(String baseDir, java.io.File destination,
            ProgressObserver callback) throws IOException {

        log.info(String.format("Saving profile [%s] to [%s]", baseDir, destination));
        
        File output = new File(destination.getAbsolutePath() + ".tmp~");
        if (!output.delete() && output.exists()) {
            String message = String.format("Could not delete original profile file: %s. "
                    + "Will try to delete on exit.", output.getAbsolutePath());
            log.warn(message);
            output.deleteOnExit();
        }

        if (!output.createNewFile()) {
            throw new IOException(String.format("Error creating tmp file [%s]", output));
        }

        ProfileWalker profileWalker = new ProfileWalker(new File(baseDir), output, callback);

        try {
            profileWalker.save();
            callback.onProgress(UNITY_PERCENT);
            if (destination.exists()) {
                if (!destination.delete()) {
                    throw new IOException(String.format("Error removing old file [%s]", destination));
                }
            }
            if (!output.renameTo(destination)) {
                throw new IOException(String.format("Error creating saved file [%s]", destination));
            }
        } finally {
            profileWalker.close();
        }
    }

    /**
     * Walks directories and files in a profile.
     * 
     * @author rflitcroft
     * 
     */
    private final class ProfileWalker extends DirectoryWalker {

        private String sourcePath;
        private File destination;
        //private ZipArchiveOutputStream out;
        private ZipOutputStream out;
        private File source;
        private ProgressObserver callback;
        private final long bytesToProcess;
        private long bytesProcessed;

        /**
         *
         */
        public ProfileWalker(File source, File destination,
                ProgressObserver callback) {
            sourcePath = source.getAbsolutePath() + File.separator;
            this.source = source;
            this.destination = destination;
            this.callback = callback;

            bytesToProcess = FileUtils.sizeOfDirectory(source);
        }

        /**
         * @throws IOException
         * 
         */
        public void close() throws IOException {
            out.close();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void handleStart(File startDirectory, Collection results)
            throws IOException {

            //out = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));
            out = new ZipOutputStream(new FileOutputStream(destination));
            out.setMethod(ZipEntry.DEFLATED); 
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void handleEnd(Collection results) throws IOException {
            out.close();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected boolean handleDirectory(File directory, int depth,
                Collection results) {
            return true;
        }
        
        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void handleDirectoryStart(File directory, int depth, Collection results) {
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void handleFile(File file, int depth, Collection results)
            throws IOException {

            String entryPath = getUnixStylePath(StringUtils.substringAfter(file
                    .getAbsolutePath(), sourcePath));

            //ZipArchiveEntry entry = (ZipArchiveEntry) out.createArchiveEntry(file, entryPath);
            //out.putArchiveEntry(entry);
            ZipEntry entry = new ZipEntry(entryPath);
            out.putNextEntry(entry);

            final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            try {
                bytesProcessed = writeFile(in, out, callback, bytesProcessed, bytesToProcess);
            } finally {
                //out.closeArchiveEntry();
                out.closeEntry();
                in.close();
            }

        }

        protected void save() throws IOException {
            walk(source, Collections.EMPTY_LIST);
        }
    }

    /**
     * Loads a droid file from disk.
     * 
     * @param source
     *            the droid file to load
     * @param destination
     *            the target directory where the source should be unpacked
     * @param observer
     *            a progress observer which is notified on progress
     * @throws IOException
     *             if the file operations failed
     */
    public void load(File source, File destination, ProgressObserver observer)
        throws IOException {

        // Delete any remnants of this expanded profile
        if (destination.exists()) {
            FileUtils.deleteDirectory(destination);
        }

        BasicZipFile zip = new BasicZipFile(source);
        
        try {
            // count the zip entries so we can do progress bar
            long totalSize = 0L;
            for (Enumeration<? extends ZipEntry> it = zip.entries(); it
                    .hasMoreElements();) {
                ZipEntry e = it.nextElement();
                totalSize += e.getSize();
            }

            long bytesSoFar = 0L;
            for (Enumeration<? extends ZipEntry> it = zip.entries(); it
                    .hasMoreElements();) {
                ZipEntry entry = it.nextElement();
                
                // zip entries can be created on windows or unix, and can retain
                // the path separator for that platform.  We must ensure that
                // the paths we find inside the zip file will work on the platform
                // we are running on.  Technically, zip entry paths should be 
                // created using the unix separator, no matter what platform they
                // are created on - but this is not always done correctly.
                final String entryName = getPlatformSpecificPath(entry.getName());
                
                File expandedFile = new File(destination + File.separator
                        + entryName);

                BufferedInputStream in = new BufferedInputStream(zip
                        .getInputStream(entry));

                FileUtils.forceMkdir(expandedFile.getParentFile());
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(expandedFile));

                bytesSoFar = readFile(in, out, observer, bytesSoFar, totalSize);
            }
            observer.onProgress(UNITY_PERCENT);
        } finally {
            zip.close();
        }

    }
    
    /**
     * Replace all path separators in the path with the correct one for the 
     * platform we are running on.  This enables us to correctly recreate the
     * directory structure for zip entries on the platform we are running on,
     * even if the zip file was created using path separators from another platform.
     * 
     * @param path the path to be made platform specific.
     * @return a platform specific path.
     */
    private String getPlatformSpecificPath(final String path) {
        // NOTE: there is a "bug" in the Java String replaceAll function, noted as such in the
        // the java bug databases.  
        // If the replacement string is a backslash (platform specific file separator in windows), 
        // then the replace function thinks it's merely the first character in an escaped string, 
        // and should be doubled up.  Needless to say, we can't simply double up all path separators, 
        // as the forward slash would replace with //, instead of /, whereas \\ would give \.
        // So, the following generates an error when the File separator is a backslash.
        //final String platformPath = path.replaceAll("[\\\\/]", File.separator);
        String platformPath = null;
        switch (File.separatorChar) {
            case BACKWARD_SLASH:
                platformPath = path.replace(FORWARD_SLASH, BACKWARD_SLASH);
                break;
            case FORWARD_SLASH:
                platformPath = path.replace(BACKWARD_SLASH, FORWARD_SLASH);
                break;
            default: // file separator is not a Windows OR Unix convention...
                platformPath = path.replaceAll("[\\\\/]", File.separator);
        }
        return platformPath; 
    }
    
    
    /**
     * Replaces any back slashes with forward slashes, to give a unix style path.
     * Zip files should use the / instead of the \ separator (no matter what platform
     * they are created on), as this will work on both platforms with zip software.
     * @param path of file
     * @return String a unix style path (all separators are forward slashes)
     */
    private String getUnixStylePath(final String path) {
        return path.replace(BACKWARD_SLASH, FORWARD_SLASH);
    }
    
    
    private long readFile(BufferedInputStream in, BufferedOutputStream out,
            ProgressObserver observer, long bytesSoFar, long totalSize) 
        throws IOException {
        long totalBytesRead = bytesSoFar;
        try {
            
            int bytesIn = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesIn = in.read(buffer)) != -1) {
                totalBytesRead += bytesIn;
                int progressSoFar = (int) ((UNITY_PERCENT * totalBytesRead) / totalSize);
                observer.onProgress(progressSoFar);
                out.write(buffer, 0, bytesIn);
            }
        } finally {
            try {
                in.close();
            } finally {
                out.close();
            }
                        
        }
        return totalBytesRead;
    }
    
    private long writeFile(BufferedInputStream in, ZipOutputStream out, //ZipArchiveOutputStream out,
            ProgressObserver observer, long bytesSoFar, long totalSize) 
        throws IOException {
        long totalBytesWritten = bytesSoFar;
        int bytesIn = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesIn = in.read(buffer)) != -1) {
            totalBytesWritten += bytesIn;
            int progressSoFar = (int) ((UNITY_PERCENT * totalBytesWritten) / totalSize);
            observer.onProgress(progressSoFar);
            out.write(buffer, 0, bytesIn);
        }
        return totalBytesWritten;
    }
}
