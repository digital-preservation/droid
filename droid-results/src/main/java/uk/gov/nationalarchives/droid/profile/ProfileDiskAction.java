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
package uk.gov.nationalarchives.droid.profile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
import net.java.truevfs.comp.zip.ZipOutputStream;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.results.handlers.ProgressObserver;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * @author rflitcroft
 * 
 */
public class ProfileDiskAction {

    private static final int UNITY_PERCENT = 100;
    private static final int BUFFER_SIZE = 8192;
    private static final char FORWARD_SLASH = '/';
    private static final char BACKWARD_SLASH = '\\';

    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
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
    public void saveProfile(final Path baseDir, final Path destination,
            final ProgressObserver callback) throws IOException {

        log.info(String.format("Saving profile [%s] to [%s]", baseDir, destination));
        
        final Path output = destination.resolveSibling(destination.getFileName().toString() + ".tmp~");
        if (!FileUtil.deleteQuietly(output)) {
            String message = String.format("Could not delete original profile file: %s. "
                    + "Will try to delete on exit.", output.toAbsolutePath().toString());
            log.warn(message);
            output.toFile().deleteOnExit();
        }

        if (!Files.exists(output)) {
            try {
                Files.createFile(output);
            } catch (FileAlreadyExistsException e) {
                throw new IOException(String.format("Error creating tmp file [%s]", output));
            }
        }

        final ProfileWalker profileWalker = new ProfileWalker(baseDir, output, callback);

        try {
            profileWalker.save();
            callback.onProgress(UNITY_PERCENT);
            if (Files.exists(destination)) {
                if (!FileUtil.deleteQuietly(destination)) {
                    throw new IOException(String.format("Error removing old file [%s]", destination));
                }
            }
            if (!Files.exists(Files.move(output, destination))) {
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
        private Path destination;
        private ZipOutputStream out;
        private Path source;
        private ProgressObserver callback;
        private final long bytesToProcess;
        private long bytesProcessed;

        /**
         * @param source
         * @param destination
         * @param callback
         */
        public ProfileWalker(final Path source, final Path destination,
                final ProgressObserver callback) {
            this.source = source;
            this.destination = destination;
            this.callback = callback;

            bytesToProcess = FileUtil.sizeQuietly(source);
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
        protected void handleStart(final File startDirectory, final Collection results)
            throws IOException {
            out = new ZipOutputStream(Files.newOutputStream(destination));
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
        protected void handleDirectoryStart(final File directory, final int depth, final Collection results) {
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void handleFile(final File file, final int depth, final Collection results)
            throws IOException {

            final String entryPath = getUnixStylePath(StringUtils.substringAfter(file.getAbsolutePath(), source.toAbsolutePath().toString() + File.separator));

            final ZipEntry entry = new ZipEntry(entryPath);
            out.putNextEntry(entry);

            try (final InputStream in = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                bytesProcessed = writeFile(in, out, callback, bytesProcessed, bytesToProcess);
            } finally {
                out.closeEntry();
            }

        }

        protected void save() throws IOException {
            walk(source.toFile(), Collections.EMPTY_LIST);
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
    public void load(final Path source, final Path destination, ProgressObserver observer)
        throws IOException {

        // Delete any remnants of this expanded profile
        if (Files.exists(destination)) {
            FileUtil.deleteQuietly(destination);
        }

        final ZipFile zip = new ZipFile(source.toFile().toPath());
        
        try {
            // count the zip entries so we can do progress bar
            long totalSize = 0L;
            final Enumeration<? extends ZipEntry> zipEntries = zip.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry e = zipEntries.nextElement();
                totalSize += e.getSize();
            }

            long bytesSoFar = 0L;
            final Enumeration<? extends ZipEntry> it = zip.entries();
            while (it.hasMoreElements()) {
                ZipEntry entry = it.nextElement();
                
                // zip entries can be created on windows or unix, and can retain
                // the path separator for that platform.  We must ensure that
                // the paths we find inside the zip file will work on the platform
                // we are running on.  Technically, zip entry paths should be 
                // created using the unix separator, no matter what platform they
                // are created on - but this is not always done correctly.
                final String entryName = getPlatformSpecificPath(entry.getName());
                
                final Path expandedFile = destination.resolve(entryName);

                Files.createDirectories(expandedFile.getParent());

                try (final InputStream in = new BufferedInputStream(zip.getInputStream(entry.getName()));
                        final OutputStream out = new BufferedOutputStream(Files.newOutputStream(expandedFile))) {
                    bytesSoFar = readFile(in, out, observer, bytesSoFar, totalSize);
                }
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
    
    
    private long readFile(final InputStream in, final OutputStream out,
            final ProgressObserver observer, final long bytesSoFar, final long totalSize)
        throws IOException {
        long totalBytesRead = bytesSoFar;
        int bytesIn = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesIn = in.read(buffer)) != -1) {
            totalBytesRead += bytesIn;
            int progressSoFar = (int) ((UNITY_PERCENT * totalBytesRead) / totalSize);
            observer.onProgress(progressSoFar);
            out.write(buffer, 0, bytesIn);
        }
        return totalBytesRead;
    }
    
    private long writeFile(final InputStream in, final ZipOutputStream out, final ProgressObserver observer,
                           final long bytesSoFar, final long totalSize)
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
