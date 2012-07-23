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
package uk.gov.nationalarchives.droid.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

/**
 * Window that displays a splash screen.
 * @author rflitcroft
 *
 */
public class DroidSplashScreen extends JWindow {

    private static final long serialVersionUID = 796921676301517853L;

    /**
     * Constructs a splash screen with the image from the URL specified.
     * @param imageUrl the URL of the image
     * @param owner the owner of the spash screen
     */
    public DroidSplashScreen(URL imageUrl, Frame owner) {
        super();
        
        // set cursor to hourglass
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        setCursor(hourglassCursor);
    
        //Define the splash screen
        ImageIcon image = new ImageIcon(imageUrl);
        JLabel label = new JLabel(image);
    
        getContentPane().add(label, BorderLayout.CENTER);
        
        addMouseListener(new MouseAdapter() {
            /**
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                dispose();
            }
        });
        
        pack();
        setLocationRelativeTo(null);
    }

}
