/**
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
package uk.gov.nationalarchives.droid.util;

import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rflitcroft
 * 
 */
public final class FileUtil {

    /**
     * 
     */
    private static final int BYTES_IN_KILOBYTE = 1024;
    private static final SimpleFileVisitor<Path> DELETE_DIR_VISITOR = new DeleteDirVisitor();

    private FileUtil() {
    }

    /**
     * Determines if a file is a symbolic link.
     * 
     * @param file
     *            the file to determine
     * @return true ifthe file is a symbolic links; false otherwise.
     * @throws IOException
     *             if the file could not be read.
     */
    public static boolean isSymbolicLink(final Path file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        return Files.isSymbolicLink(file);
    }

    /**
     * Formats a file size in hmnan readable form.
     * @param fileSize the size of the file
     * @param decimalPos the number iof decimal places
     * @return a formatted file size.
     */
    public static String formatFileSize(long fileSize, int decimalPos) {
        NumberFormat fmt = NumberFormat.getNumberInstance();
        if (decimalPos >= 0) {
            fmt.setMaximumFractionDigits(decimalPos);
        }
        
        String formattedSize;
        final double size = fileSize;
        double val = size / (BYTES_IN_KILOBYTE * BYTES_IN_KILOBYTE * BYTES_IN_KILOBYTE);
        if (val > 1) {
            formattedSize = fmt.format(val).concat(" GB");
        } else {
            val = size / (BYTES_IN_KILOBYTE * BYTES_IN_KILOBYTE);
            if (val > 1) {
                formattedSize = fmt.format(val).concat(" MB");
            } else {
                val = size / BYTES_IN_KILOBYTE;
                if (val > 1) {
                    formattedSize = fmt.format(val).concat(" KB");
                } else {
                    formattedSize = fmt.format(size).concat(" bytes");
                }
            }
        }
        
        return formattedSize;
    }

    /**
     * Get just the filename part of the path.
     *
     * @param path The path to get the filename from
     *
     * @return The filename
     */
    public static String fileName(final Path path) {
        return path.getFileName().toString();
    }

    /**
     * Determine the last modified time of a file or directory.
     *
     * @param path The path to get the last modified date of
     *
     * @return The last modified time of the file or directory or null
     */
    public static FileTime lastModifiedQuietly(final Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Determine the size of a file or directory.
     *
     * @param path The path to get the size of
     *
     * @return The size of the file or directory, or -1 if the file size cannot be determined
     */
    public static long sizeQuietly(final Path path) {
        long size = -1;
        try {
            if (!Files.isDirectory(path)) {
                size = Files.size(path);
            } else {
                final DirSizeVisitor dirSizeVisitor = new DirSizeVisitor();
                Files.walkFileTree(path, dirSizeVisitor);
                size = dirSizeVisitor.totalSize();
            }
        } catch (final IOException ioe) {
            size = -1;
        }
        return size;
    }

    /**
     * Deletes a path from the filesystem.
     *
     * If the path is a directory its contents
     * will be recursively deleted before it itself
     * is deleted
     *
     * This method will never throw an IOException, it
     * instead returns `false` if an error occurs
     * whilst removing a file or directory
     *
     * Note that removal of a directory is not an atomic-operation
     * and so if an error occurs during removal, some of the directories
     * descendants may have already been removed
     *
     * @param path The path to delete
     *
     * @return false if an error occurred, true otherwise
     */
    public static boolean deleteQuietly(final Path path) {
        boolean result;
        try {
            if (!Files.isDirectory(path)) {
                result = Files.deleteIfExists(path);
            } else {
                Files.walkFileTree(path, DELETE_DIR_VISITOR);
            }
            result = true;
        } catch (final IOException ioe) {
            result = false;
        }
        return result;
    }

    /**
     * Makes directories on the filesystem the filesystem.
     *
     * This method will never throw an IOException, it
     * instead returns `false` if an error occurs
     * whilst creating a file or directory
     *
     * Note that creation of a directory is not an atomic-operation
     * and so if an error occurs during creation, some of the directories
     * descendants may have already been created
     *
     * @param dir The directory path to create
     *
     * @return false if an error occurred, true otherwise
     */
    public static boolean mkdirsQuietly(final Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            return true;
        } catch (final IOException ioe) {
            return false;
        }
    }

    /**
     * List files and directories in a path.
     *
     * @param path The path to list the children of
     * @param recursive true to recursively descend into sub-directories
     * @param fileFilter A file filter, or null
     *
     * @return A list of the encountered paths
     */
    public static List<Path> listFilesQuietly(final Path path, final boolean recursive, final FileFilter fileFilter) {
        try {
            return listFiles(path, recursive, fileFilter);
        } catch (final IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * List files and directories in a path.
     *
     * @param path The path to list the children of
     * @param recursive true to recursively descend into sub-directories
     * @param fileFilter A file filter, or null
     *
     * @return A list of the encountered paths
     *
     * @throws IOException if an error occurs
     */
    public static List<Path> listFiles(final Path path, final boolean recursive, final FileFilter fileFilter) throws IOException {
        final List<Path> results = new ArrayList<>();
        listFiles(path, recursive, asDirFilter(fileFilter), results);
        return results;
    }

    /**
     * List files and directories in a path.
     *
     * @param path The path to list the children of
     * @param recursive true to recursively descend into sub-directories
     * @param filter A directory stream filter, or null
     *
     * @return A list of the encountered paths
     */
    public static List<Path> listFilesQuietly(final Path path, final boolean recursive, final DirectoryStream.Filter<Path> filter) {
        try {
            return listFiles(path, recursive, filter);
        } catch (final IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * List files and directories in a path.
     *
     * @param path The path to list the children of
     * @param recursive true to recursively descend into sub-directories
     * @param filter A directory stream filter, or null
     *
     * @return A list of the encountered paths
     *
     * @throws IOException if an error occurs
     */
    public static List<Path> listFiles(final Path path, final boolean recursive, final DirectoryStream.Filter<Path> filter) throws IOException {
        final List<Path> results = new ArrayList<>();
        listFiles(path, recursive, filter, results);
        return results;
    }

    private static DirectoryStream.Filter<Path> asDirFilter(final FileFilter fileFilter) {
        if (fileFilter == null) {
            return null;
        } else {
            return new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(final Path entry) throws IOException {
                    return fileFilter.accept(entry.toFile());
                }
            };
        }
    }

    private static void listFiles(final Path path, final boolean recursive, final DirectoryStream.Filter<Path> filter,
            final List<Path> results) throws IOException {
        DirectoryStream<Path> dirStream = null;

        try {
            if (filter == null) {
                dirStream = Files.newDirectoryStream(path);
            } else {
                dirStream = Files.newDirectoryStream(path, filter);
            }
            for (final Path child : dirStream) {
                if (recursive && Files.isDirectory(child)) {
                    listFiles(child, recursive, filter, results);
                } else {
                    results.add(child);
                }
            }
        } catch (AccessDeniedException exception) {
            // do nothing, managed in FileEventHandler
        } finally {
            if (dirStream != null) {
                dirStream.close();
            }
        }
    }

    private static class DirSizeVisitor extends SimpleFileVisitor<Path> {
        private long size;

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            size += Files.size(file);
            return FileVisitResult.CONTINUE;
        }

        public long totalSize() {
            return size;
        }
    }

    private static class DeleteDirVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.deleteIfExists(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }

            Files.deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
