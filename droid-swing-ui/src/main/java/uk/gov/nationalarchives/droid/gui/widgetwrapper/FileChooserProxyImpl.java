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
package uk.gov.nationalarchives.droid.gui.widgetwrapper;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;

/**
 * Wraps a Swing filechooser.
 * @author rflitcroft
 *
 */
public class FileChooserProxyImpl implements FileChooserProxy {

    private JFileChooser fileChooser;
    private Component parent;
    
    /**
     * 
     * @param parent this component's parent
     * @param fileChooser the file chooser to wrap
     */
    public FileChooserProxyImpl(Component parent, JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        this.parent = parent;
    }
        
    @Override
    public File getSelectedFile() {
        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.exists() && !"droid".equals(FilenameUtils.getExtension(selectedFile.getName()))) {
            selectedFile = new File(selectedFile.getPath() + ".droid");    
        }
        return selectedFile;
    }

    @Override
    public int getResponse() {
        switch (fileChooser.showSaveDialog(parent)) {
            case JFileChooser.APPROVE_OPTION:
                return FileChooserProxy.APPROVE;
            default:
                return FileChooserProxy.CANCEL;
        }
    }
    
}
