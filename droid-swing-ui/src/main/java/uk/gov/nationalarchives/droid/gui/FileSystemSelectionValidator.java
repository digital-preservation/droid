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

package uk.gov.nationalarchives.droid.gui;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang.SystemUtils;

import sun.awt.shell.ShellFolder;

/**
 * Validator to validate that all selected files are profilable.
 */
public class FileSystemSelectionValidator {

    private final boolean isValid;
    private String errorMessage = "";
    private String errorTemplate = "The following folders cannot be added as they are not from the file system: ";

    /**
     * Constructor.
     * @param files list of File objects to validate
     */
    public FileSystemSelectionValidator(List<File> files) {
        boolean valid = true; //valid until proven otherwise
        //Note: We are importing ShellFolder from a restricted set. You can see a corresponding
        //suppression of a checkstyle validation. This is acceptable here as we are using it
        //specifically with Windows OS only.
        if (SystemUtils.IS_OS_WINDOWS) {
            List<File> nonFSFiles = files.stream().filter(item ->
                    !FileSystemView.getFileSystemView().isFileSystem(item)).collect(Collectors.toList());
            if (nonFSFiles.size() == 0) {
                valid = true;
            } else {
                errorMessage = errorTemplate + nonFSFiles.stream().map(
                        item -> ((ShellFolder) item).getDisplayName()).collect(Collectors.joining(","));
                valid = false;
            }
        }
        this.isValid = valid;
    }

    /**
     * Returns whether the current selection of File objects is valid for profiling.
     * @return true if all Files are profilable, false if any file is non-profilable
     */
    public boolean isSelectionValid() {
        return isValid;
    }

    /**
     * returns the error message associated with this set of file validation.
     * @return the error message containing details of the non-profilable files
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
