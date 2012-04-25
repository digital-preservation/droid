/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui;

/**
 *
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to.
 * 
 * @author adash
 */
public class ProfileTabComponent extends JPanel {

    private static final int BORDER_RIGHT = 5;

    private static final long serialVersionUID = -6007225851097990188L;
    
    /**
     * Gives a button a border when the mouse hovers over.
     */
    private static final MouseListener BUTTON_MOUSE_LISTENER = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton btn = (AbstractButton) component;
                btn.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton btn = (AbstractButton) component;
                btn.setBorderPainted(false);
            }
        }
    };

    private ProfileForm parent;
    private JButton button;

    /**
     * 
     * @param parent the profile form which will own the this tab component.
     */
    public ProfileTabComponent(final ProfileForm parent) {
        // unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        this.parent = parent;
        setOpaque(false);

        final JLabel label = new JLabel();
        label.setText(parent.getName());
        parent.addPropertyChangeListener("name", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                label.setText(evt.getNewValue().toString());
            }
        });
        
        add(label);
        // add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, BORDER_RIGHT));

        // tab button
        button = new CloseTabButton();
        button.setVisible(false);
        add(button);
        // add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

    }
    
    /**
     * Makes the button visible.
     * @param visible whether the button should be visible or not.
     */
    public void setButtonVisible(boolean visible) {
        button.setVisible(visible);
    }

    /**
     * Button to close a tab. 
     * @author rflitcroft
     *
     */
    private class CloseTabButton extends JButton implements ActionListener {

        /** */
        private static final int DELTA = 6;
        /** */
        private static final int BUTTON_SIZE = 17;
        private static final long serialVersionUID = -5319241984819540576L;

        private final BasicStroke stroke = new BasicStroke(2);
        
        public CloseTabButton() {
            setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            setToolTipText("close this profile");
            // Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            // Make it transparent
            setContentAreaFilled(false);
            // No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // Making nice rollover effect
            // we use the same listener for all buttons
            addMouseListener(BUTTON_MOUSE_LISTENER);
            setRolloverEnabled(true);
            // Close the proper tab by clicking the button
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            parent.closeProfile();
        }

        // paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            // shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(stroke);
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.RED);
            }
            g2.drawLine(DELTA, DELTA, getWidth() - DELTA - 1, getHeight()
                    - DELTA - 1);
            g2.drawLine(getWidth() - DELTA - 1, DELTA, DELTA, getHeight()
                    - DELTA - 1);
            g2.dispose();
        }
    }

}
