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
package uk.gov.nationalarchives.droid.gui.treemodel;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.SwingConstants;

/**
 * @author a-mpalmer
 *
 */
public class FileExtensionRenderer extends DefaultCellRenderer {

    private Icon warningIcon;
    
    /**
     * @param backColor the background color to render in.
     */
    public FileExtensionRenderer(Color backColor) {
        super(backColor);
        getRenderer().setHorizontalTextPosition(SwingConstants.LEFT);
        warningIcon = getIconResource("warning_extension_mismatch");
    }
    
    
    @Override
    /**
     * @param Object the object being rendered.
     * @return Icon the icon for a file extension mismatch.
     * @see uk.gov.nationalarchives.droid.gui.treemodel.DefaultCellRenderer#getIcon(java.lang.Object)
     */
    public Icon getIcon(Object value) {
        DirectoryComparableObject o = (DirectoryComparableObject) value;
        if (o.getExtensionMismatch()) {
            return warningIcon;
        }
        return null;
    }

}
