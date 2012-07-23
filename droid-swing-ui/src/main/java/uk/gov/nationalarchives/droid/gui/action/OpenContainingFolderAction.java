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
package uk.gov.nationalarchives.droid.gui.action;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;


/**
 * @author a-mpalmer
 *
 */
public class OpenContainingFolderAction {

    private static final String FILE_URI_PREFIX = "file:/";
    
    /**
     * Constructor.
     */
    public OpenContainingFolderAction() {
    }
    
    /**
     * Opens the selected resource's containing folders.
     * @param nodes - the list of nodes to open.
     */
    public void open(final List<ProfileResourceNode> nodes) {
        if (Desktop.isDesktopSupported()) {
            Set<String> folderPaths = getClosestFolderPaths(nodes);
            openFolders(folderPaths);
        }
    }
    
    private void openFolders(final Set<String> locations) {
        Desktop desktop = Desktop.getDesktop();
        for (String path : locations) {
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                try {
                    desktop.open(folder);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    private Set<String> getClosestFolderPaths(final List<ProfileResourceNode> nodes) {
        final Set<String> closestFolderPaths = new HashSet<String>();
        for (ProfileResourceNode node : nodes) {
            String closestFolderPath = getClosestFolderPath(getClosestFilePath(node.getUri()));
            if (closestFolderPath != null && !closestFolderPath.isEmpty()) {
                closestFolderPaths.add(closestFolderPath);
            }
        }
        return closestFolderPaths;
    }
    
    private String getClosestFolderPath(final String filePath) {
        String closestFolderPath = null;
        final File theFile = new File(filePath);
        if (theFile.exists()) {
            File parentFolder = theFile.getParentFile();
            closestFolderPath = parentFolder == null ? null : parentFolder.getAbsolutePath();
        }
        return closestFolderPath;
    }
    
    private String getClosestFilePath(final URI location) {
        final String path = java.net.URLDecoder.decode(location.toString());
        final int filePrefixEnd = path.indexOf(FILE_URI_PREFIX) + FILE_URI_PREFIX.length();
        final int firstPling = path.indexOf('!', filePrefixEnd);
        final int endOfFilePath = firstPling > 0 ? firstPling : path.length();
        final String uriPath = path.substring(filePrefixEnd, endOfFilePath); 
        // does it have a windows drive letter or UNC path?
        return path.charAt(filePrefixEnd + 1) == ':' || path.startsWith("///") 
            ? uriPath 
            : '/' + uriPath; // a unix path, which will require an initial forward slash:
    }
    
   
}
