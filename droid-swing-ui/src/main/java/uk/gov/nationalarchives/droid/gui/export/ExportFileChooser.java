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
package uk.gov.nationalarchives.droid.gui.export;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FilenameUtils;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOutputOptions;

/**
 *
 * @author rflitcroft
 */
public class ExportFileChooser extends JFileChooser {

    private static final String CSV_EXT = "csv";
    private static final String JSON_EXT = "json";

    private static final long serialVersionUID = -3733290468455212962L;

    private ExportOutputOptions exportOutputOptions;

    @Override
    protected void setup(FileSystemView view) {
        super.setup(view);
        setAcceptAllFileFilterUsed(true);
    }

    /**
     *
     * @param exportOutputOptions The output options
     */
    public void setExportOutputOptions(final ExportOutputOptions exportOutputOptions) {
        this.exportOutputOptions = exportOutputOptions;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        FileNameExtensionFilter extensionFilter = getFileNameExtensionFilter();
        addChoosableFileFilter(extensionFilter);
        setFileFilter(extensionFilter);
        return super.showSaveDialog(parent);
    }

    private FileNameExtensionFilter getFileNameExtensionFilter() {
        if (exportOutputOptions == ExportOutputOptions.JSON_OUTPUT) {
            return new FileNameExtensionFilter("JSON (*.json)", JSON_EXT);
        } else {
            return new FileNameExtensionFilter("Comma separated values (*.csv)", CSV_EXT);
        }
    }

    /**
     * Pops up a warning dialog if the selected file exists.
     */
    @Override
    public void approveSelection() {
        FileNameExtensionFilter extensionFilter = getFileNameExtensionFilter();
        int confirm = JOptionPane.YES_OPTION;
        if (!getSelectedFile().exists() && getFileFilter().getDescription().equals(extensionFilter.getDescription())) {
            final String filename = getSelectedFile().getName();
            String ext = FilenameUtils.getExtension(filename);
            String expectedExtension = exportOutputOptions == ExportOutputOptions.JSON_OUTPUT ? JSON_EXT : CSV_EXT;
            if (!expectedExtension.equals(ext)) {
                setSelectedFile(new File(getSelectedFile().getParentFile(), 
                        filename + "." + expectedExtension));
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
