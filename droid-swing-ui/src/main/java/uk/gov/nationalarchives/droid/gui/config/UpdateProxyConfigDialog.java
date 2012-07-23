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
package uk.gov.nationalarchives.droid.gui.config;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableMap;
import org.openide.util.NbBundle;

/**
 *
 * @author rflitcroft
 */
public class UpdateProxyConfigDialog extends JDialog {

    /** User responded with Cancel/Close. */
    public static final int CANCEL = 0;

    /** User responded with OK. */
    public static final int OK = 1;
    
    private static final long serialVersionUID = -343879341163622862L;

    private ObservableMap<String, Object> proxyConfig;
    
    private int response;

    /** 
     * Creates new form UpdateProxyConfigDialog.
     * @param parentFrame the parent frame
     * @param properties the properties that this dialog can update. 
     * No changes will be made to this map; use getProperties() to get the updates. 
     * 
     */
    public UpdateProxyConfigDialog(Window parentFrame, Map<String, Object> properties) {
        super(parentFrame);
        proxyConfig = ObservableCollections.observableMap(new HashMap<String, Object>(properties));
        initComponents();
        pack();
        
        setLocationRelativeTo(parentFrame);
        setPanelComponents(proxySettingsPanel, useProxyCheckbox.isSelected());
    }
    
    private void setPanelComponents(JPanel panel, boolean enabled) {
        panel.setEnabled(enabled);
        for (Component c : panel.getComponents()) {
            c.setEnabled(enabled);
        }
    }
    
    /**
     * @return the response
     */
    public int getResponse() {
        return response;
    }
    
    /**
     * 
     * @return the (updated) properties given to this dialog.
     */
    public Map<String, Object> getProperties() {
        return proxyConfig;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new BindingGroup();

        proxySettingsPanel = new JPanel();
        jLabel8 = new JLabel();
        proxyHostTextBox = new JTextField();
        jLabel9 = new JLabel();
        proxyPortTextBox = new JFormattedTextField();
        useProxyCheckbox = new JCheckBox();
        cancelButton = new JToggleButton();
        okButton = new JToggleButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(NbBundle.getMessage(UpdateProxyConfigDialog.class, "UpdateProxyConfigDialog.title")); // NOI18N
        setModal(true);

        proxySettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 11), UIManager.getDefaults().getColor("windowText")), NbBundle.getMessage(UpdateProxyConfigDialog.class, "UpdateProxyConfigDialog.proxySettingsPanel.border.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 11), UIManager.getDefaults().getColor("textText"))); // NOI18N
        proxySettingsPanel.setEnabled(false);


        jLabel8.setText(NbBundle.getMessage(UpdateProxyConfigDialog.class, "UpdateProxyConfigDialog.jLabel8.text")); // NOI18N
        Binding binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${properties[\"update.proxy.host\"]}"), proxyHostTextBox, BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);


        jLabel9.setText(NbBundle.getMessage(UpdateProxyConfigDialog.class, "UpdateProxyConfigDialog.jLabel9.text")); // NOI18N
        proxyPortTextBox.setFormatterFactory(new IntegerFormatterFactory());

        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${properties[\"update.proxy.port\"]}"), proxyPortTextBox, BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        GroupLayout proxySettingsPanelLayout = new GroupLayout(proxySettingsPanel);
        proxySettingsPanel.setLayout(proxySettingsPanelLayout);

        proxySettingsPanelLayout.setHorizontalGroup(
            proxySettingsPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(proxySettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proxySettingsPanelLayout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(proxySettingsPanelLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(proxyPortTextBox, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(proxyHostTextBox, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );
        proxySettingsPanelLayout.setVerticalGroup(
            proxySettingsPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(proxySettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proxySettingsPanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(proxyHostTextBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(proxySettingsPanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(proxyPortTextBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        useProxyCheckbox.setText(NbBundle.getMessage(UpdateProxyConfigDialog.class, "UpdateProxyConfigDialog.useProxyCheckbox.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${properties[\"update.proxy\"]}"), useProxyCheckbox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        useProxyCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                useProxyCheckboxItemStateChanged(evt);
            }
        });

        cancelButton.setText(NbBundle.getMessage(UpdateProxyConfigDialog.class, "UpdateProxyConfigDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText(NbBundle.getMessage(UpdateProxyConfigDialog.class, "UpdateProxyConfigDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(useProxyCheckbox)
                    .addComponent(proxySettingsPanel, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useProxyCheckbox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(proxySettingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, okButton});

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void useProxyCheckboxItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_useProxyCheckboxItemStateChanged
        setPanelComponents(proxySettingsPanel, evt.getStateChange() == ItemEvent.SELECTED);
}//GEN-LAST:event_useProxyCheckboxItemStateChanged

    private void okButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        response = OK;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        response = CANCEL;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JToggleButton cancelButton;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JToggleButton okButton;
    private JTextField proxyHostTextBox;
    private JFormattedTextField proxyPortTextBox;
    private JPanel proxySettingsPanel;
    private JCheckBox useProxyCheckbox;
    private BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    
    private static class IntegerFormatterFactory extends AbstractFormatterFactory {
        @Override
        public AbstractFormatter getFormatter(JFormattedTextField tf) {
            NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#0"));
            formatter.setValueClass(Integer.class);
            formatter.setAllowsInvalid(true);
            formatter.setCommitsOnValidEdit(false);
            formatter.setMinimum(0);
            return formatter;
        };
    }

}
