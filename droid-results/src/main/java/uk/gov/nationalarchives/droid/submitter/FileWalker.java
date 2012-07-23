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
package uk.gov.nationalarchives.droid.submitter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.list.TransformedList;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.gov.nationalarchives.droid.core.interfaces.ResourceId;

/**
 * A file walker which supports resume.
 * @author rflitcroft
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class FileWalker extends DirectoryWalker {

   
    /**
     * 
     */
    private static final String FILE_SYSTEM_UNAVAILABLE = "File system appears to be unavailable for file: [%s]";

    private static final Transformer FILE_TO_URI_TRANSFORMER = new Transformer() {
        @Override
        public Object transform(Object input) {
            return ((File) input).toURI();
        }
    };
    
    @XmlTransient
    private Log log = LogFactory.getLog(this.getClass());    
    
    @XmlElement(name = "RootUri")
    private URI root;

    @XmlAttribute(name = "Recursive")
    private boolean recursive;
    
    @XmlElementWrapper(name = "Progress")
    @XmlElement(name = "ProgressEntry")
    private Deque<ProgressEntry> progress;
    
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
    public FileWalker(URI root, boolean recursive) {
        super(null, recursive ? -1 : 1);
        this.recursive = recursive;
        this.root = root;
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
            progress = new ArrayDeque<ProgressEntry>();
        }
        
        walk(new File(root), null);
    }
    
    private static List<ProgressEntry> reverseProgress(Deque<ProgressEntry> progress) {
        List<ProgressEntry> reversed = new ArrayList<ProgressEntry>();
        for (Iterator<ProgressEntry> it = progress.descendingIterator(); it.hasNext();) {
            ProgressEntry entry = it.next();
            reversed.add(entry);
        }
        return reversed;
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void handleFile(File file, int depth, Collection results) 
        throws IOException {
        
        if (!SubmitterUtils.isFileSystemAvailable(file, root)) {
            log.error(String.format(FILE_SYSTEM_UNAVAILABLE, file.getAbsolutePath()));
            throw new IOException(file.getAbsolutePath());
        }
        
        if (fastForward) {
            if (recoveryRoad.get(depth - 1).containsChild(file.toURI())) {
                // FOUND IT!!
                fastForward = false;
            } else {
                return;
            }
        }
        
        ProgressEntry progressEntry = progress.peek();
        if (file.isFile()) {
            fileHandler.handle(file, depth, progressEntry);
        }
        progressEntry.removeChild(file.toURI());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected boolean handleDirectory(File dir, int depth, Collection results) throws IOException {
        boolean processDir = true;
        
        if (!SubmitterUtils.isFileSystemAvailable(dir, root)) {
            log.error(String.format(FILE_SYSTEM_UNAVAILABLE, dir.getAbsolutePath()));
            throw new IOException(dir.getAbsolutePath());
        }

        URI dirUri = dir.toURI();
        
        if (fastForward) {
            if (!(depth < recoveryRoad.size() && recoveryRoad.get(depth).getUri().equals(dirUri))) {
                // This directory is NOT on our road to recovery.
                if (recoveryRoad.get(depth - 1).containsChild(dirUri)) {
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
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void handleDirectoryStart(File directory, int depth, Collection results) 
        throws IOException {
        
        // if we are fast forwarding, then just keep going...
        if (fastForward) {
            return; 
        }
        
        ProgressEntry parent = progress.peek();
        List unprocessedChildren = Collections.EMPTY_LIST;
        ResourceId directoryId;
        
        if (recursive || depth == 0) {
            File[] children = directory.listFiles();
            if (children == null) {
                directoryId = restrictedDirectoryHandler.handle(directory, depth, parent);
            } else {
                unprocessedChildren = TransformedList.decorate(new ArrayList<URI>(), FILE_TO_URI_TRANSFORMER); 
                unprocessedChildren.addAll(Arrays.asList(children));
                directoryId = directoryHandler.handle(directory, depth, parent);
            }
        } else {
            directoryId = directoryHandler.handle(directory, depth, parent);
        }
        progress.push(new ProgressEntry(directory.toURI(), directoryId, unprocessedChildren));
    }
    
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void handleDirectoryEnd(File directory, int depth, Collection results) {
        
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
            progress.peek().removeChild(directory.toURI());
        }
    }
    
    /**
     * @param fileHandler the fileHandler to set
     */
    public void setFileHandler(FileWalkerHandler fileHandler) {
        this.fileHandler = fileHandler;
    }
    
    /**
     * @param directoryHandler the directoryHandler to set
     */
    public void setDirectoryHandler(FileWalkerHandler directoryHandler) {
        this.directoryHandler = directoryHandler;
    }
    
    /**
     * @param restrictedDirectoryHandler the restrictedDirectoryHandler to set
     */
    public void setRestrictedDirectoryHandler(FileWalkerHandler restrictedDirectoryHandler) {
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
    void setProgress(Deque<ProgressEntry> progress) {
        this.progress = progress;
    }
    
    /**
     * A progress entry.
     * @author rflitcroft
     *
     */
    public static final class ProgressEntry {
        
        @XmlAttribute(name = "Id")
        private long id;
        
        @XmlAttribute(name = "Prefix")
        private String prefix;
        
        @XmlElement(name = "Uri")
        private URI uri;
        
        @XmlElementWrapper(name = "Children")
        @XmlElement(name = "ChildUri")
        private List<URI> children;
        
        /**
         * Default constructor.
         */
        ProgressEntry() { }
        
        /**
         * @param uri the URI of the entry
         * @param id the ID of the entry
         * @param prefix the prefix of the entry
         * @param children the entry's children
         */
        ProgressEntry(URI uri, long id, String prefix, List<URI> children) {
            this.uri = uri;
            this.id = id;
            this.prefix = prefix;
            this.children = children;
        }
        
        /**
         * @param uri the URI of the entry
         * @param resourceId the ResourceId of the entry
         * @param children the entry's children
         */
        ProgressEntry(URI uri, ResourceId resourceId, List<URI> children) {
            if (resourceId == null) {
                throw new IllegalArgumentException("Cannot construct a ProgressEntry with a null ResourceId");
            }
            this.uri = uri;
            this.id = resourceId.getId();
            this.prefix = resourceId.getPath();
            this.children = children;
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
         * @return the URI of the entry
         */
        public URI getUri() {
            return uri;
        }
        
        /**
         * @param childUri the child uri to remove
         */
        private void removeChild(URI childUri) {
            children.remove(childUri);
        }
        
        /**
         * 
         * @param childUri the child URi to check
         * @return true if the progress entry contains the child specified.
         */
        public boolean containsChild(URI childUri) {
            return children != null && children.contains(childUri);
        }
    }
    
}
