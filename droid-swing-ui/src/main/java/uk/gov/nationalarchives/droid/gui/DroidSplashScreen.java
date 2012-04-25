/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
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
