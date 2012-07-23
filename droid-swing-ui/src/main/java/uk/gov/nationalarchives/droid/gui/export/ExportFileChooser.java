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
package uk.gov.nationalarchives.droid.gui.export;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author rflitcroft
 */
public class ExportFileChooser extends JFileChooser {

    private static final String CSV_EXT = "csv";
    
    private static final long serialVersionUID = -3733290468455212962L;

    private FileNameExtensionFilter csvFilter;

    @Override
    protected void setup(FileSystemView view) {
        super.setup(view);

        setAcceptAllFileFilterUsed(true);
        csvFilter = new FileNameExtensionFilter("Comma separated values (*.csv)", CSV_EXT);
        addChoosableFileFilter(csvFilter);
    }

        /**
     * Pops up a warning dialog if the selected file exists.
     */
    @Override
    public void approveSelection() {

        int confirm = JOptionPane.YES_OPTION;
        if (!getSelectedFile().exists() && getFileFilter().equals(csvFilter)) {
            final String filename = getSelectedFile().getName();
            String ext = FilenameUtils.getExtension(filename);
            if (!CSV_EXT.equals(ext)) {
                setSelectedFile(new File(getSelectedFile().getParentFile(), 
                        filename + "." + CSV_EXT));
            }
        }

        if (getSelectedFile().exists()) {
            confirm = JOptionPane.showConfirmDialog(
                    this,
                    "The file you have selected already exists. Do you wish to overwrite it?",
                    "File exists warning",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
        } 
        

        if (confirm == JOptionPane.YES_OPTION) {
            super.approveSelection();
        } else if (confirm == JOptionPane.CANCEL_OPTION) {
            super.cancelSelection();
        }
    }

}
