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
package uk.gov.nationalarchives.droid.submitter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;
import uk.gov.nationalarchives.droid.util.FileUtil;

/**
 * A file walker which supports resume.
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class FileWalker {

    private static final String FILE_SYSTEM_UNAVAILABLE = "File system appears to be unavailable for file: [%s]";

    private Logger log = LoggerFactory.getLogger(this.getClass());
    

    private URI root;

    @XmlAttribute(name = "Recursive")
    private boolean recursive;
    
    @XmlElementWrapper(name = "Progress")
    @XmlElement(name = "ProgressEntry")
    private Deque<ProgressEntry> progress;

    private String topLevelAbsolutePath;
    private FileWalkerHandler fileHandler;
    private FileWalkerHandler directoryHandler;
    private FileWalkerHandler restrictedDirectoryHandler;
    private boolean fastForward;
    private List<ProgressEntry> recoveryRoad;
    
    /**
     * Default Constructor.
     */
    FileWalker() { }
    
    /**
     * @param root the root of the walk
     * @param recursive if the Filewalker should operate recursively
     */
    public FileWalker(final URI root, final boolean recursive) {
        this.recursive = recursive;
        setRootUri(root);
    }

    /**
     * Parameterized constructor.
     * @param root the root of the walk.
     * @param recursive if the walker should walk recursively.
     * @param fileHandler The handler for files.
     * @param directoryHandler The handler for directories.
     * @param restrictedDirectoryHandler The handler for restricted directories.
     */
    public FileWalker(final URI root, final boolean recursive, final FileWalkerHandler fileHandler,
                      final FileWalkerHandler directoryHandler, final FileWalkerHandler restrictedDirectoryHandler) {
        this.recursive = recursive;
        setRootUri(root);
        setFileHandler(fileHandler);
        setDirectoryHandler(directoryHandler);
        setRestrictedDirectoryHandler(restrictedDirectoryHandler);
    }

    /**
     *
     * @return the current root Uri.
     */
    @XmlElement(name = "RootUri")
    public URI getRootUri() {
        return root;
    }

    /**
     *
     * @param rootUri the Uri to set.
     */
    public void setRootUri(final URI rootUri) {
        this.root = rootUri;
        this.topLevelAbsolutePath = Paths.get(root).toAbsolutePath().toString();
    }

    /**
     * (Re)starts the file walker.
     * @throws IOException if an IO exception occured
     */
    public void walk() throws IOException {
        if (progress != null) {
            // fast-forward to the recovery point
            fastForward = true;
            recoveryRoad = reverseProgress(progress);
        } else {
            // initialise an empty progress queue and start from scratch
            progress = new ArrayDeque<>();
        }
        
        walk(Paths.get(root), 0);
    }

    private static List<ProgressEntry> reverseProgress(final Deque<ProgressEntry> progress) {
        final List<ProgressEntry> reversed = new ArrayList<>();
        for (final Iterator<ProgressEntry> it = progress.descendingIterator(); it.hasNext();) {
            final ProgressEntry entry = it.next();
            reversed.add(entry);
        }
        return reversed;
    }

    private void walk(final Path directory, final int depth) throws IOException {
        if (handleDirectory(directory, depth)) {
            final List<Path> children = FileUtil.listFiles(directory, false, (DirectoryStream.Filter<Path>) null);
            handleDirectoryStart(directory, depth, children.toArray(new Path[children.size()]));
            if (recursive || depth == 0) {
                final int childDepth = depth + 1;
                for (final Path child : children) {
                    if (Files.isDirectory(child)) {
                        walk(child, childDepth);
                    } else {
                        handleFile(child, childDepth);
                    }
                }
            }
            handleDirectoryEnd(directory, depth);
        } else { // can't access children - restricted directory.
            handleRestrictedDirectory(directory, depth);
        }
    }

    /**
     *
     * @param dir file.
     * @param depth depth to which to check
     * @return false if directory yet to be processed, otherwise true
     * @throws IOException An error occurs in accessing the resource
     */
    protected boolean handleDirectory(final Path dir, final int depth) throws IOException {
        boolean processDir = true;

        if (!Files.isReadable(dir)) {
            return false;
        }

        if (!SubmitterUtils.isFileSystemAvailable(dir, topLevelAbsolutePath)) {
            log.error(String.format(FILE_SYSTEM_UNAVAILABLE, dir.toAbsolutePath().toString()));
            throw new IOException(dir.toAbsolutePath().toString());
        }

        if (fastForward) {
            if (!(depth < recoveryRoad.size() && recoveryRoad.get(depth).getFile().equals(dir))) {
                // This directory is NOT on our road to recovery.
                if (recoveryRoad.get(depth - 1).containsChild(dir)) {
                    // This directory is yet to be processed
                    fastForward = false;
                } else {
                    // Not interested - skip it.
                    processDir = false;
                }
            }
        }
        return processDir;
    }

    /**
     *
     * @param directory directory to handle.
     * @param depth depth to which to check
     * @param children array of files
     * @throws IOException An error occurs in accessing the resource
     */
    protected void handleDirectoryStart(final Path directory, final int depth, final Path[] children)
        throws IOException {
        
        // if we are fast forwarding, then just keep going...
        if (fastForward) {
            return; 
        }

        final ProgressEntry parent = progress.peek();
        final ResourceId directoryId = directoryHandler.handle(directory, depth, parent);
        progress.push(new ProgressEntry(directory, directoryId, children));
    }

    /**
     *
     * @param file file to hanndle.
     * @param depth level to whhich to check
     * @throws IOException  An error occurs in accessing the resource
     */
    protected void handleFile(final Path file, final int depth)
        throws IOException {

        if (!SubmitterUtils.isFileSystemAvailable(file, topLevelAbsolutePath)) {
            log.error(String.format(FILE_SYSTEM_UNAVAILABLE, file.toAbsolutePath().toString()));
            throw new IOException(file.toAbsolutePath().toString());
        }

        if (fastForward) {
            if (recoveryRoad.get(depth - 1).containsChild(file)) {
                // FOUND IT!!
                fastForward = false;
            } else {
                return;
            }
        }

        final ProgressEntry progressEntry = progress.peek();
        if (!Files.isDirectory(file)) {
            fileHandler.handle(file, depth, progressEntry);
        }
        progressEntry.removeChild(file);
    }

    /**
     *
     * @param directory directory.
     * @param depth depth to which to check.
     */
    protected void handleDirectoryEnd(final Path directory, final int depth) {
        
        if (fastForward) {
            // Indicates a failure to recover from an expected directory, so we
            // should resume here instead.
            fastForward = false;
            // trim the progress until this directory is current
            while (progress.size() - 1 > depth) {
                progress.pop();
            }
        }
        
        progress.pop();
        if (!progress.isEmpty()) {
            progress.peek().removeChild(directory);
        }
    }


    private void handleRestrictedDirectory(final Path directory, final int depth) throws IOException {
        // if we are fast forwarding, then just keep going...
        if (fastForward) {
            return;
        }

        ProgressEntry parent = progress.peek();
        restrictedDirectoryHandler.handle(directory, depth, parent);
        if (!progress.isEmpty()) {
            progress.peek().removeChild(directory);
        }
    }
    
    /**
     * @param fileHandler the fileHandler to set
     */
    public void setFileHandler(final FileWalkerHandler fileHandler) {
        this.fileHandler = fileHandler;
    }
    
    /**
     * @param directoryHandler the directoryHandler to set
     */
    public void setDirectoryHandler(final FileWalkerHandler directoryHandler) {
        this.directoryHandler = directoryHandler;
    }
    
    /**
     * @param restrictedDirectoryHandler the restrictedDirectoryHandler to set
     */
    public void setRestrictedDirectoryHandler(final FileWalkerHandler restrictedDirectoryHandler) {
        this.restrictedDirectoryHandler = restrictedDirectoryHandler;
    }
    
    /**
     * @return the progress
     */
    Deque<ProgressEntry> progress() {
        return progress;
    }
    
    /**
     * @param progress the progress to set
     */
    void setProgress(final Deque<ProgressEntry> progress) {
        this.progress = progress;
    }


    /**
     * A progress entry.
     * @author rflitcroft
     *
     */
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class ProgressEntry {
        
        @XmlAttribute(name = "Id")
        private long id;
        
        @XmlAttribute(name = "Prefix")
        private String prefix;
        
        private Path directory;


        private Path[] children;

        /**
         * Default constructor.
         */
        ProgressEntry() { }
        
        /**
         * @param directory the File of the directory
         * @param id the ID of the directory
         * @param prefix the prefix of the directory
         * @param children the directory's children
         */
        ProgressEntry(final Path directory, final long id, final String prefix, final Path[] children) {
            this.directory = directory;
            this.id = id;
            this.prefix = prefix;
            this.children = children;
        }


        
        /**
         * @param directory the File of the directory
         * @param resourceId the ResourceId of the directory
         * @param children the directory's children
         */
        ProgressEntry(final Path directory, final ResourceId resourceId, final Path[] children) {
            if (resourceId == null) {
                throw new IllegalArgumentException("Cannot construct a ProgressEntry with a null ResourceId");
            }
            this.directory = directory;
            this.id = resourceId.getId();
            this.prefix = resourceId.getPath();
            this.children = children;
        }

        /**
         *
         * @return the current directory as a URI.
         */
        @XmlElement(name = "Uri")
        public URI getUri() {
            return this.directory.toUri();
        }

        /**
         * Sets the current directory as a URI.
         * @param theDirectory a URI representing the directory to set.
         */
        public void setUri(final URI theDirectory) {
            this.directory = Paths.get(theDirectory);
        }

        /**
         * Gets a list of child URIs.
         * @return   A list of child URIs
         */
        @XmlElementWrapper(name = "Children")
        @XmlElement(name = "ChildUri")
        public List<URI> getChildUri() {
            final List<URI> result = new ArrayList<>();
            if (children != null) {
                for (final Path child : children) {
                    if (child != null) {
                        result.add(child.toUri());
                    }
                }
            }
            return result;
        }

        /**
         * Set child URLs of the current one in the tree.
         * @param childURIs List of chile URLs
         */
        public void setChildUri(final List<URI> childURIs) {
            if (childURIs != null) {
                this.children = new Path[childURIs.size()];
                for (int i = 0; i < childURIs.size(); i++) {
                    children[i] = Paths.get(childURIs.get(i));
                }
            }
        }

        /**
         * @return the id of the entry
         */
        public long getId() {
            return id;
        }
        
        /**
         * 
         * @return The prefix of the entry.
         */
        public String getPrefix() {
            return prefix;
        }
        
        /**
         * 
         * @return A resource id for the progress entry.
         */
        public ResourceId getResourceId() {
            return new ResourceId(id, prefix);
        }
        
        /**
         * @return the File of the directory.
         */
        public Path getFile() {
            return directory;
        }
        
        /**
         * @param child the child uri to remove
         */
        private void removeChild(final Path child) {
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    if (child.equals(children[i])) {
                        children[i] = null;
                        break;
                    }
                }
            }
        }
        
        /**
         * 
         * @param child the child file to check
         * @return true if the progress entry contains the child specified.
         */
        public boolean containsChild(final Path child) {
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    if (child.equals(children[i])) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
