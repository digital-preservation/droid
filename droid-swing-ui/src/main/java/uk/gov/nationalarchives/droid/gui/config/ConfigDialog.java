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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.apache.commons.collections.list.SetUniqueList;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableMap;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.openide.util.NbBundle;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.gui.GlobalContext;

/**
 *
 * @author rflitcroft
 */
public class ConfigDialog extends JDialog {
    /** The Cancel response. */
    public static final int CANCEL = 0;

    /** The OK response. */
    public static final int OK = 1;

    private static final long serialVersionUID = -707594144365104572L;
    
    private int response;
    private Map<String, Object> props = new HashMap<String, Object>();
    private GlobalContext context;
    
    private ObservableMap<String, Object> globalConfig = ObservableCollections.observableMap(props);
    
    private ObservableList<String> allBinarySigFiles = 
        ObservableCollections.observableList(SetUniqueList.decorate(new ArrayList()));
    
    private ObservableList<String> allContainerSigFiles = 
        ObservableCollections.observableList(SetUniqueList.decorate(new ArrayList()));

    /*
    private ObservableList<String> allTextSigFiles = 
        ObservableCollections.observableList(SetUniqueList.decorate(new ArrayList()));
    */
    
    /** 
     * Creates new form ConfigDialog.
     * @param owner the owner of the dialog 
     * @param context the droid global context
     */
    public ConfigDialog(Frame owner, GlobalContext context) {
        super(owner, true);
        this.context = context;
        init(context.getGlobalConfig().getPropertiesMap());
        initComponents();
        setPanelComponents(autoUpdatePanel, autoUpdateCheckbox.isSelected());
        updateFrequencyTextBox.setEnabled(updateScheduleRadioButton.isSelected() && autoUpdateCheckbox.isSelected());
        setLocationRelativeTo(owner);
    }
    
    /**
     * Initialises the dialog with all the settings.
     * @param properties the settings
     */
    public void init(Map<String, Object> properties) {
        response = CANCEL;
        
        props.putAll(properties);

        final Map<SignatureType, SortedMap<String, SignatureFileInfo>> allSignatureFiles = 
            context.getActionFactory().newListSignatureFilesAction().list();
        
        Object defaultSigFileVersion = globalConfig.get(DroidGlobalProperty.DEFAULT_BINARY_SIG_FILE_VERSION.getName());
        allBinarySigFiles.addAll(allSignatureFiles.get(SignatureType.BINARY).keySet());
        if (!allBinarySigFiles.isEmpty() && allBinarySigFiles.contains(defaultSigFileVersion)) {
            globalConfig.put(DroidGlobalProperty.DEFAULT_BINARY_SIG_FILE_VERSION.getName(), defaultSigFileVersion);
        }
        
        // init container signature combo
        Object defaultContainerSigFileVersion = 
            globalConfig.get(DroidGlobalProperty.DEFAULT_CONTAINER_SIG_FILE_VERSION.getName());
        allContainerSigFiles.addAll(allSignatureFiles.get(SignatureType.CONTAINER).keySet());
        if (!allContainerSigFiles.isEmpty() && allContainerSigFiles.contains(defaultContainerSigFileVersion)) {
            globalConfig.put(DroidGlobalProperty.DEFAULT_CONTAINER_SIG_FILE_VERSION.getName(), 
                    defaultContainerSigFileVersion);
        }
        
        /*
        // init text signature combo
        Object defaultTextSigFileVersion = 
            globalConfig.get(DroidGlobalProperty.DEFAULT_TEXT_SIG_FILE_VERSION.getName());
        allTextSigFiles.addAll(allSignatureFiles.get(SignatureType.TEXT).keySet());
        if (!allTextSigFiles.isEmpty() && allTextSigFiles.contains(defaultTextSigFileVersion)) {
            globalConfig.put(DroidGlobalProperty.DEFAULT_TEXT_SIG_FILE_VERSION.getName(), defaultTextSigFileVersion);
        }
        */
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

        updateFrequencyButtonGroup = new ButtonGroup();
        csvExportButtonGroup = new ButtonGroup();
        buttonGroupExtension = new ButtonGroup();
        jPanel4 = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        jScrollPane1 = new JScrollPane();
        generalTabbedPane1 = new JTabbedPane();
        jPanel3 = new JPanel();
        jLabel3 = new JLabel();
        defaultThrottleTextBox1 = new JFormattedTextField();
        jLabel4 = new JLabel();
        jLabel11 = new JLabel();
        defaultSigFileComboBox1 = new JComboBox();
        processArchivesCheckBox1 = new JCheckBox();
        generateHashCheckBox = new JCheckBox();
        containerSigCombo = new JComboBox();
        containerSigFileLabel = new JLabel();
        jTextField2 = new JTextField();
        jLabel9 = new JLabel();
        rowPerFileButton2 = new JRadioButton();
        rowPerFormatButton2 = new JRadioButton();
        jPanel5 = new JPanel();
        jLabel5 = new JLabel();
        updateUrlTextBox = new JTextField();
        autoUpdatePanel = new JPanel();
        updateFrequencyTextBox = new JSpinner();
        jLabel6 = new JLabel();
        autoSetDefaultSignatureFileCheckBox = new JCheckBox();
        updateOnStartupRadioButton = new JRadioButton();
        updateScheduleRadioButton = new JRadioButton();
        askToDownloadCheckBox = new JCheckBox();
        autoUpdateCheckbox = new JCheckBox();
        jLabel7 = new JLabel();
        proxySettingsButton = new JButton();
        containerSigUrl = new JTextField();
        jLabel2 = new JLabel();
        jLabel1 = new JLabel();
        jTextField1 = new JTextField();
        jPanel2 = new JPanel();
        rowPerFileButton1 = new JRadioButton();
        rowPerFormatButton1 = new JRadioButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.title")); // NOI18N
        setMinimumSize(new Dimension(500, 440));

        okButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.okButton.text")); // NOI18N
        okButton.setPreferredSize(new Dimension(65, 23));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);

        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cancelButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, okButton});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, okButton});

        jScrollPane1.setMinimumSize(new Dimension(450, 350));
        jScrollPane1.setPreferredSize(new Dimension(507, 290));

        generalTabbedPane1.setMinimumSize(new Dimension(470, 280));
        generalTabbedPane1.setPreferredSize(new Dimension(470, 280));

        jPanel3.setPreferredSize(new Dimension(500, 380));

        jLabel3.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.text")); // NOI18N
        defaultThrottleTextBox1.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));

        Binding binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.defaultThrottle\"]}"), defaultThrottleTextBox1, BeanProperty.create("value"));
        bindingGroup.addBinding(binding);


        jLabel4.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel2.text")); // NOI18N
        jLabel11.setLabelFor(defaultSigFileComboBox1);

        jLabel11.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel10.text")); // NOI18N
        ELProperty eLProperty = ELProperty.create("${allBinarySigFiles}");
        JComboBoxBinding jComboBoxBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, this, eLProperty, defaultSigFileComboBox1);
        bindingGroup.addBinding(jComboBoxBinding);
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.defaultBinarySigFileVersion\"]}"), defaultSigFileComboBox1, BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);


        processArchivesCheckBox1.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processArchivesCheckBox.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processArchives\"]}"), processArchivesCheckBox1, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);


        processArchivesCheckBox1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processArchivesCheckBox1ActionPerformed(evt);
            }
        });

        generateHashCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.generateHashCheckBox.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.generateHash\"]}"), generateHashCheckBox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        generateHashCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generateHashCheckBoxActionPerformed(evt);
            }
        });

        eLProperty = ELProperty.create("${allContainerSigFiles}");
        jComboBoxBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, this, eLProperty, containerSigCombo);
        bindingGroup.addBinding(jComboBoxBinding);
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.defaultContainerSigFileVersion\"]}"), containerSigCombo, BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        containerSigCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                containerSigComboActionPerformed(evt);
            }
        });

        containerSigFileLabel.setLabelFor(containerSigCombo);

        containerSigFileLabel.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.containerSigFileLabel.text_1")); // NOI18N
        jTextField2.setHorizontalAlignment(JTextField.RIGHT);

        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.maxBytesToScan\"]}"), jTextField2, BeanProperty.create("text"));
        bindingGroup.addBinding(binding);


        jLabel9.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel9.text")); // NOI18N
        buttonGroupExtension.add(rowPerFileButton2);

        rowPerFileButton2.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.rowPerFileButton2.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ELProperty.create("${!globalConfig[\"profile.matchAllExtensions\"]}"), rowPerFileButton2, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        buttonGroupExtension.add(rowPerFormatButton2);

        rowPerFormatButton2.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.rowPerFormatButton2.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.matchAllExtensions\"]}"), rowPerFormatButton2, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);

        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING, false)
                            .addComponent(jLabel11, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(containerSigFileLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(defaultSigFileComboBox1, 0, 334, Short.MAX_VALUE)
                            .addComponent(containerSigCombo, 0, 334, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(generateHashCheckBox)
                            .addComponent(processArchivesCheckBox1))
                        .addGap(14, 14, 14))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jTextField2, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                        .addGap(14, 14, 14))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(rowPerFileButton2)
                            .addComponent(rowPerFormatButton2))
                        .addContainerGap(204, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(defaultThrottleTextBox1, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jLabel4)
                        .addContainerGap(243, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(defaultSigFileComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(containerSigCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(containerSigFileLabel))
                .addGap(18, 18, 18)
                .addComponent(processArchivesCheckBox1)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(generateHashCheckBox)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(jLabel9)
                    .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(rowPerFileButton2)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(rowPerFormatButton2)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(defaultThrottleTextBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(48, Short.MAX_VALUE))
        );

        generalTabbedPane1.addTab(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel2.TabConstraints.tabTitle"), jPanel3); // NOI18N
        jPanel5.setPreferredSize(new Dimension(544, 290));
        jPanel5.setRequestFocusEnabled(false);

        jLabel5.setLabelFor(updateUrlTextBox);


        jLabel5.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel5.toolTipText")); // NOI18N
        updateUrlTextBox.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.updateUrlTextBox.toolTipText")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"pronom.update.url\"]}"), updateUrlTextBox, BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        updateUrlTextBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateUrlTextBoxActionPerformed(evt);
            }
        });

        autoUpdatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 11), UIManager.getDefaults().getColor("windowText")), NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.autoUpdatePanel.border.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 11), UIManager.getDefaults().getColor("textText"))); // NOI18N
        autoUpdatePanel.setEnabled(false);

        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.frequency.days\"]}"), updateFrequencyTextBox, BeanProperty.create("value"));
        bindingGroup.addBinding(binding);
        binding = Bindings.createAutoBinding(UpdateStrategy.READ, updateScheduleRadioButton, ELProperty.create("${selected}"), updateFrequencyTextBox, BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);



        jLabel6.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel4.text")); // NOI18N
        autoSetDefaultSignatureFileCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.autoSetDefaultSignatureFileCheckBox.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.autoSetDefault\"]}"), autoSetDefaultSignatureFileCheckBox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        autoSetDefaultSignatureFileCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                autoSetDefaultSignatureFileCheckBoxActionPerformed(evt);
            }
        });

        updateFrequencyButtonGroup.add(updateOnStartupRadioButton);

        updateOnStartupRadioButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.updateOnStartupRadioButton.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.frequency.startup\"]}"), updateOnStartupRadioButton, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        updateOnStartupRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateOnStartupRadioButtonActionPerformed(evt);
            }
        });

        updateFrequencyButtonGroup.add(updateScheduleRadioButton);

        updateScheduleRadioButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.updateScheduleRadioButton.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_ONCE, this, ELProperty.create("${!globalConfig[\"update.frequency.startup\"]}"), updateScheduleRadioButton, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);


        askToDownloadCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.askToDownloadCheckBox.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.downloadPrompt\"]}"), askToDownloadCheckBox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        GroupLayout autoUpdatePanelLayout = new GroupLayout(autoUpdatePanel);
        autoUpdatePanel.setLayout(autoUpdatePanelLayout);

        autoUpdatePanelLayout.setHorizontalGroup(
            autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(autoUpdatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(autoUpdatePanelLayout.createSequentialGroup()
                        .addComponent(updateScheduleRadioButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(updateFrequencyTextBox, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jLabel6))
                    .addComponent(updateOnStartupRadioButton))
                .addGap(33, 33, 33)
                .addGroup(autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(autoSetDefaultSignatureFileCheckBox)
                    .addComponent(askToDownloadCheckBox))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        autoUpdatePanelLayout.setVerticalGroup(
            autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(autoUpdatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, autoUpdatePanelLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(updateScheduleRadioButton)
                        .addComponent(updateFrequencyTextBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6))
                    .addGroup(Alignment.TRAILING, autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(autoUpdatePanelLayout.createSequentialGroup()
                            .addComponent(askToDownloadCheckBox)
                            .addGap(11, 11, 11)
                            .addComponent(autoSetDefaultSignatureFileCheckBox))
                        .addComponent(updateOnStartupRadioButton))))
        );

        autoUpdateCheckbox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.autoUpdateCheckbox.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.autoCheck\"]}"), autoUpdateCheckbox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);



        autoUpdateCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                autoUpdateItemStateChanged(evt);
            }
        });

        jLabel7.setFont(new Font("Tahoma", 1, 11));
        jLabel7.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel6.text")); // NOI18N
        proxySettingsButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.proxySettingsButton.text")); // NOI18N
        proxySettingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                proxySettingsButtonActionPerformed(evt);
            }
        });

        containerSigUrl.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.containerSigUrl.toolTipText")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"container.update.url\"]}"), containerSigUrl, BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel2.setLabelFor(containerSigUrl);



        jLabel2.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel2.text_1")); // NOI18N
        jLabel2.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel2.toolTipText")); // NOI18N
        jLabel1.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.text_1")); // NOI18N
        jLabel1.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.toolTipText")); // NOI18N
        jTextField1.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.toolTipText")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"puid.urlPattern\"]}"), jTextField1, BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);


        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(autoUpdatePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(20, 20, 20)
                        .addComponent(updateUrlTextBox, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
                    .addComponent(proxySettingsButton, Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(containerSigUrl, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 326, GroupLayout.PREFERRED_SIZE))
                    .addComponent(autoUpdateCheckbox, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(updateUrlTextBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(containerSigUrl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(proxySettingsButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoUpdateCheckbox)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(autoUpdatePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jPanel5Layout.linkSize(SwingConstants.VERTICAL, new Component[] {containerSigUrl, updateUrlTextBox});

        generalTabbedPane1.addTab(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel3.TabConstraints.tabTitle"), jPanel5); // NOI18N
        jPanel2.setBorder(BorderFactory.createTitledBorder(null, NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel2.border.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 0, 11), new Color(0, 0, 0))); // NOI18N
        csvExportButtonGroup.add(rowPerFileButton1);

        rowPerFileButton1.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.rowPerFileButton1.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ, this, ELProperty.create("${!globalConfig[\"export.rowPerFormat\"]}"), rowPerFileButton1, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        csvExportButtonGroup.add(rowPerFormatButton1);

        rowPerFormatButton1.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.rowPerFormatButton1.text")); // NOI18N
        binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"export.rowPerFormat\"]}"), rowPerFormatButton1, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        rowPerFormatButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rowPerFormatButton1ActionPerformed(evt);
            }
        });

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);

        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(rowPerFileButton1)
                    .addComponent(rowPerFormatButton1))
                .addContainerGap(330, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rowPerFileButton1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(rowPerFormatButton1)
                .addContainerGap(237, Short.MAX_VALUE))
        );

        generalTabbedPane1.addTab(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel2.TabConstraints.tabTitle_1"), jPanel2); // NOI18N
        jScrollPane1.setViewportView(generalTabbedPane1);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                        .addGap(10, 10, 10))
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        response = OK;
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void proxySettingsButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_proxySettingsButtonActionPerformed
        UpdateProxyConfigDialog proxyDialog = new UpdateProxyConfigDialog(this, props);
        proxyDialog.setVisible(true);
        if (proxyDialog.getResponse() == UpdateProxyConfigDialog.OK) {
            globalConfig.putAll(proxyDialog.getProperties());
        }
}//GEN-LAST:event_proxySettingsButtonActionPerformed

    private void updateOnStartupRadioButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_updateOnStartupRadioButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_updateOnStartupRadioButtonActionPerformed

    private void autoSetDefaultSignatureFileCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_autoSetDefaultSignatureFileCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_autoSetDefaultSignatureFileCheckBoxActionPerformed

    private void updateUrlTextBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_updateUrlTextBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_updateUrlTextBoxActionPerformed

    private void containerSigComboActionPerformed(ActionEvent evt) {//GEN-FIRST:event_containerSigComboActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_containerSigComboActionPerformed

    private void generateHashCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_generateHashCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_generateHashCheckBoxActionPerformed

    private void processArchivesCheckBox1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processArchivesCheckBox1ActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_processArchivesCheckBox1ActionPerformed

    private void rowPerFormatButton1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rowPerFormatButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rowPerFormatButton1ActionPerformed

    private void setPanelComponents(JPanel panel, boolean enabled) {
        panel.setEnabled(enabled);
        for (Component c : panel.getComponents()) {
            c.setEnabled(enabled);
        }
    }
    
    private void autoUpdateItemStateChanged(ItemEvent evt) {                                            
        setPanelComponents(autoUpdatePanel, evt.getStateChange() == ItemEvent.SELECTED);
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateFrequencyTextBox.setEnabled(updateScheduleRadioButton.isSelected());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox askToDownloadCheckBox;
    private JCheckBox autoSetDefaultSignatureFileCheckBox;
    private JCheckBox autoUpdateCheckbox;
    private JPanel autoUpdatePanel;
    private ButtonGroup buttonGroupExtension;
    private JButton cancelButton;
    private JComboBox containerSigCombo;
    private JLabel containerSigFileLabel;
    private JTextField containerSigUrl;
    private ButtonGroup csvExportButtonGroup;
    private JComboBox defaultSigFileComboBox1;
    private JFormattedTextField defaultThrottleTextBox1;
    private JTabbedPane generalTabbedPane1;
    private JCheckBox generateHashCheckBox;
    private JLabel jLabel1;
    private JLabel jLabel11;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel9;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JScrollPane jScrollPane1;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JButton okButton;
    private JCheckBox processArchivesCheckBox1;
    private JButton proxySettingsButton;
    private JRadioButton rowPerFileButton1;
    private JRadioButton rowPerFileButton2;
    private JRadioButton rowPerFormatButton1;
    private JRadioButton rowPerFormatButton2;
    private ButtonGroup updateFrequencyButtonGroup;
    private JSpinner updateFrequencyTextBox;
    private JRadioButton updateOnStartupRadioButton;
    private JRadioButton updateScheduleRadioButton;
    private JTextField updateUrlTextBox;
    private BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    
    /**
     * @return the dialog response
     */
    public int getResponse() {
        return response;
    }

    /**
     * @return the defaultThrottleTextBox
     */
    JFormattedTextField getDefaultThrottleTextBox() {
        return defaultThrottleTextBox1;
    }

    /**
     * @return the cancelButton
     */
    JButton getCancelButton() {
        return cancelButton;
    }
    
    /**
     * @return the okButton
     */
    JButton getOkButton() {
        return okButton;
    }

    /**
     * @return the globalConfig
     */
    public Map<String, Object> getGlobalConfig() {
        return globalConfig;
    }

    /**
     * 
     * @return collection of available signature files.
     */
    public List<String> getAllBinarySigFiles() {
        return allBinarySigFiles;
    }
    
    /**
     * @return the allContainerSigFiles
     */
    public List<String> getAllContainerSigFiles() {
        return allContainerSigFiles;
    }
    
    /**
     * @return the allTextSigFiles
     */
    /*
    public List<String> getAllTextSigFiles() {
        return allTextSigFiles;
    }
    */

}
