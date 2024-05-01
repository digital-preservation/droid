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
package uk.gov.nationalarchives.droid.gui.config;

import org.apache.commons.collections4.list.SetUniqueList;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

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

    private static final String PRONOM_SIGNATURE_URL_KEY = "pronom.update.url";
    private static final String PRONOM_CONTAINER_URL_KEY = "container.update.url";
    private final List<String> propertiesAffectingNewProfile = new ArrayList<>();
    private boolean automaticallyCreateNewProfile;
    private int response;
    private final Map<String, Object> props = new HashMap<String, Object>();
    private final Map<String, Object> propsOnLaunch = new HashMap<String, Object>();
    private GlobalContext context;
    
    private ObservableMap<String, Object> globalConfig = ObservableCollections.observableMap(props);
    
    private ObservableList<String> allHashAlgorithms = 
        ObservableCollections.observableList(SetUniqueList.setUniqueList(new ArrayList()));
    
    private ObservableList<String> allBinarySigFiles = 
        ObservableCollections.observableList(SetUniqueList.setUniqueList(new ArrayList()));
    
    private ObservableList<String> allContainerSigFiles = 
        ObservableCollections.observableList(SetUniqueList.setUniqueList(new ArrayList()));

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
        populatePropertyListNeedingNewProfile();
        init(context.getGlobalConfig().getPropertiesMap());
        initComponents();
        setPanelComponents(autoUpdatePanel, autoUpdateCheckbox.isSelected());
        updateFrequencyTextBox.setEnabled(updateScheduleRadioButton.isSelected() && autoUpdateCheckbox.isSelected());
        setLocationRelativeTo(owner);
    }

    private void populatePropertyListNeedingNewProfile() {
        propertiesAffectingNewProfile.addAll(
                Arrays.asList(DroidGlobalProperty.DEFAULT_BINARY_SIG_FILE_VERSION.getName(), DroidGlobalProperty.DEFAULT_CONTAINER_SIG_FILE_VERSION.getName(),
                        DroidGlobalProperty.PROCESS_ZIP.getName(), DroidGlobalProperty.PROCESS_TAR.getName(),
                        DroidGlobalProperty.PROCESS_GZIP.getName(), DroidGlobalProperty.PROCESS_RAR.getName(),
                        DroidGlobalProperty.PROCESS_7ZIP.getName(), DroidGlobalProperty.PROCESS_ISO.getName(),
                        DroidGlobalProperty.PROCESS_BZIP2.getName(), DroidGlobalProperty.PROCESS_ARC.getName(),
                        DroidGlobalProperty.PROCESS_WARC.getName(), DroidGlobalProperty.GENERATE_HASH.getName(),
                        DroidGlobalProperty.HASH_ALGORITHM.getName(), DroidGlobalProperty.MAX_BYTES_TO_SCAN.getName(),
                        DroidGlobalProperty.EXTENSION_ALL.getName(), DroidGlobalProperty.DEFAULT_THROTTLE.getName()
            )
        );
    }

    /**
     * Initialises the dialog with all the settings.
     * @param properties the settings
     */
    public void init(Map<String, Object> properties) {
        response = CANCEL;

        props.putAll(properties);
        propsOnLaunch.putAll(properties); //copy of initial state to detect dirty later

        final Map<SignatureType, SortedMap<String, SignatureFileInfo>> allSignatureFiles = 
            context.getActionFactory().newListSignatureFilesAction().list();
        
        // initialise checksum list for dropdown
        //(overkill in this case, but using same mechanism as sig files for consistency)
        List<String> availableAlgorithms = (ArrayList<String>) properties.get("availableHashAlgorithms");

        //BNO: Get the default hash algorithm as specified in the config file (droid.properties).  Ideally
        // the property should be renamed to e.g. defaultHashAlgorithm but this may break existing code...
        Object defaultHashAlgorithm =
            globalConfig.get(DroidGlobalProperty.HASH_ALGORITHM.getName());
        allHashAlgorithms.addAll(availableAlgorithms);
        if (allHashAlgorithms.contains(defaultHashAlgorithm)) {
            globalConfig.put(DroidGlobalProperty.HASH_ALGORITHM.getName(), defaultHashAlgorithm);
        }
        
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
        generalTabbedPane1 = new JTabbedPane();
        jPanel3 = new JPanel();
        jLabel3 = new JLabel();
        defaultThrottleTextBox1 = new JFormattedTextField();
        jLabel4 = new JLabel();
        jLabel11 = new JLabel();
        defaultSigFileComboBox1 = new JComboBox();
        generateHashCheckBox = new JCheckBox();
        containerSigCombo = new JComboBox();
        containerSigFileLabel = new JLabel();
        jTextField2 = new JTextField();
        jLabel9 = new JLabel();
        rowPerFileButton2 = new JRadioButton();
        rowPerFormatButton2 = new JRadioButton();
        hashAlgorithmCombo = new JComboBox();
        jPanel4 = new JPanel();
        processZipCheckBox = new JCheckBox();
        processTarCheckBox = new JCheckBox();
        processGzipCheckBox = new JCheckBox();
        processRarCheckBox = new JCheckBox();
        process7zipCheckBox = new JCheckBox();
        processIsoCheckBox = new JCheckBox();
        processBzip2CheckBox = new JCheckBox();
        toggleArchivesButton = new JButton();
        archivesLabel = new JLabel();
        jPanel6 = new JPanel();
        webArchivesLabel = new JLabel();
        processArcCheckBox = new JCheckBox();
        processWarcCheckBox = new JCheckBox();
        toggleWebArchivesButton = new JButton();
        jPanel2 = new JPanel();
        jPanel1 = new JPanel();
        rowPerFileButton1 = new JRadioButton();
        rowPerFormatButton1 = new JRadioButton();
        jPanel5 = new JPanel();
        jLabel5 = new JLabel();
        updateUrlTextBox = new JTextField();
        autoUpdatePanel = new JPanel();
        updateFrequencyTextBox = new JSpinner();
        jLabel6 = new JLabel();
        updateOnStartupRadioButton = new JRadioButton();
        updateScheduleRadioButton = new JRadioButton();
        autoUpdateCheckbox = new JCheckBox();
        jLabel7 = new JLabel();
        proxySettingsButton = new JButton();
        containerSigUrl = new JTextField();
        jLabel2 = new JLabel();
        jLabel1 = new JLabel();
        jTextField1 = new JTextField();
        askToDownloadCheckBox = new JCheckBox();
        autoSetDefaultSignatureFileCheckBox = new JCheckBox();
        pronomUrlResetButton = new JButton();
        cancelButton = new JButton();
        okButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.title")); // NOI18N
        setMinimumSize(new Dimension(500, 600));

        jPanel3.setPreferredSize(new Dimension(500, 380));

        jLabel3.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.text")); // NOI18N

        defaultThrottleTextBox1.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));

        Binding binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this,
                        ELProperty.create("${globalConfig[\"profile.defaultThrottle\"]}"), defaultThrottleTextBox1, BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        jLabel4.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel2.text")); // NOI18N

        jLabel11.setLabelFor(defaultSigFileComboBox1);
        jLabel11.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel10.text")); // NOI18N

        ELProperty eLProperty = ELProperty.create("${allBinarySigFiles}");
        JComboBoxBinding jComboBoxBinding =
                SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, this, eLProperty, defaultSigFileComboBox1);
        bindingGroup.addBinding(jComboBoxBinding);
        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.defaultBinarySigFileVersion\"]}"),
                        defaultSigFileComboBox1, BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);


        generateHashCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.generateHashCheckBox.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.generateHash\"]}"),
                        generateHashCheckBox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        generateHashCheckBox.addActionListener(evt -> generateHashCheckBoxActionPerformed(evt));

        eLProperty = ELProperty.create("${allContainerSigFiles}");
        jComboBoxBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, this, eLProperty, containerSigCombo);
        bindingGroup.addBinding(jComboBoxBinding);
        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.defaultContainerSigFileVersion\"]}"),
                        containerSigCombo, BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        containerSigCombo.addActionListener(evt -> containerSigComboActionPerformed(evt));

        containerSigFileLabel.setLabelFor(containerSigCombo);
        containerSigFileLabel.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.containerSigFileLabel.text_1")); // NOI18N

        jTextField2.setHorizontalAlignment(JTextField.RIGHT);

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.maxBytesToScan\"]}"),
                        jTextField2, BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel9.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel9.text")); // NOI18N

        buttonGroupExtension.add(rowPerFileButton2);
        rowPerFileButton2.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.matchAllExtensions.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ, this, ELProperty.create("${!globalConfig[\"profile.matchAllExtensions\"]}"),
                        rowPerFileButton2, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        buttonGroupExtension.add(rowPerFormatButton2);
        rowPerFormatButton2.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.matchExtensionNoSig.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.matchAllExtensions\"]}"),
                        rowPerFormatButton2, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        eLProperty = ELProperty.create("${allHashAlgorithms}");
        jComboBoxBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, this, eLProperty, hashAlgorithmCombo);
        bindingGroup.addBinding(jComboBoxBinding);
        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.hashAlgorithm\"]}"),
                        hashAlgorithmCombo, BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        hashAlgorithmCombo.addActionListener(evt -> hashAlgorithmComboActionPerformed(evt));

        processZipCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processZipCheckBox.text")); // NOI18N
        processZipCheckBox.addActionListener(evt -> processZipCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processZip\"]}"),
                        processZipCheckBox, BeanProperty.create("selected"), "zipBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        processTarCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processTarCheckBox.text")); // NOI18N
        processTarCheckBox.addActionListener(evt -> processTarCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processTar\"]}"),
                        processTarCheckBox, BeanProperty.create("selected"), "tarBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);


        processGzipCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processGzipCheckBox.text")); // NOI18N
        processGzipCheckBox.addActionListener(evt -> processGzipCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processGzip\"]}"),
                        processGzipCheckBox, BeanProperty.create("selected"), "gzipBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        processRarCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processRarCheckBox.text")); // NOI18N
        processRarCheckBox.addActionListener(evt -> processRarCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processRar\"]}"),
                        processRarCheckBox, BeanProperty.create("selected"), "rarBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        process7zipCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.process7zipCheckBox.text")); // NOI18N
        process7zipCheckBox.addActionListener(evt -> process7zipCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.process7zip\"]}"),
                        process7zipCheckBox, BeanProperty.create("selected"), "7zipBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        processIsoCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processIsoCheckBox.text")); // NOI18N
        processIsoCheckBox.addActionListener(evt -> processIsoCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processIso\"]}"),
                        processIsoCheckBox, BeanProperty.create("selected"), "isoBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        processBzip2CheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processBzip2CheckBox.text")); // NOI18N
        processBzip2CheckBox.addActionListener(evt -> processBzip2CheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processBzip2\"]}"),
                        processBzip2CheckBox, BeanProperty.create("selected"), "bzip2Binding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        toggleArchivesButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.toggleArchivesButton.text")); // NOI18N
        toggleArchivesButton.addActionListener(evt -> toggleArchivesButtonActionPerformed(evt));

        archivesLabel.setLabelFor(containerSigCombo);
        archivesLabel.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.archivesLabel.text")); // NOI18N

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(toggleArchivesButton)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(processZipCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(processTarCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(processGzipCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(processRarCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(process7zipCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(processIsoCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(processBzip2CheckBox)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(archivesLabel, GroupLayout.PREFERRED_SIZE, 461, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 121, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(archivesLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(toggleArchivesButton)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(processZipCheckBox)
                    .addComponent(processTarCheckBox)
                    .addComponent(processGzipCheckBox)
                    .addComponent(process7zipCheckBox)
                    .addComponent(processRarCheckBox)
                    .addComponent(processIsoCheckBox)
                    .addComponent(processBzip2CheckBox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        webArchivesLabel.setLabelFor(containerSigCombo);
        webArchivesLabel.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.webArchivesLabel.text")); // NOI18N

        processArcCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processArcCheckBox.text")); // NOI18N
        processArcCheckBox.addActionListener(evt -> processArcCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processArc\"]}"),
                        processArcCheckBox, BeanProperty.create("selected"), "arcBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        processWarcCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.processWarcCheckBox.text")); // NOI18N
        processWarcCheckBox.addActionListener(evt -> processWarcCheckBoxActionPerformed(evt));

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"profile.processWarc\"]}"),
                        processWarcCheckBox, BeanProperty.create("selected"), "warcBinding");
        binding.setSourceNullValue(false);
        binding.setSourceUnreadableValue(false);
        bindingGroup.addBinding(binding);

        toggleWebArchivesButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.toggleWebArchivesButton.text")); // NOI18N
        toggleWebArchivesButton.addActionListener(evt -> toggleWebArchivesButtonActionPerformed(evt));

        GroupLayout jPanel6Layout = new GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING)
            .addComponent(webArchivesLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(processArcCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(processWarcCheckBox))
                    .addComponent(toggleWebArchivesButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(webArchivesLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(toggleWebArchivesButton)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(processArcCheckBox)
                    .addComponent(processWarcCheckBox)))
        );

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING, false)
                            .addComponent(jLabel11, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(containerSigFileLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(defaultSigFileComboBox1, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(containerSigCombo, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(generateHashCheckBox)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(hashAlgorithmCombo, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                        .addGap(20, 20, 20))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(rowPerFileButton2)
                            .addComponent(rowPerFormatButton2)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(defaultThrottleTextBox1, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(jLabel4)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(jPanel6, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(defaultSigFileComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(containerSigCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(containerSigFileLabel))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(generateHashCheckBox)
                    .addComponent(hashAlgorithmCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(rowPerFileButton2)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(rowPerFormatButton2)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(defaultThrottleTextBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(53, Short.MAX_VALUE))
        );

        generalTabbedPane1.addTab(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel2.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel1.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel1.border.title_1"))); // NOI18N

        csvExportButtonGroup.add(rowPerFileButton1);
        rowPerFileButton1.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.rowPerFileButton1.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ, this, ELProperty.create("${!globalConfig[\"export.rowPerFormat\"]}"),
                        rowPerFileButton1, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        csvExportButtonGroup.add(rowPerFormatButton1);
        rowPerFormatButton1.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.rowPerFormatButton1.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"export.rowPerFormat\"]}"),
                        rowPerFormatButton1, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        rowPerFormatButton1.addActionListener(evt -> rowPerFormatButton1ActionPerformed(evt));

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(rowPerFormatButton1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rowPerFileButton1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rowPerFileButton1)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(rowPerFormatButton1)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(596, Short.MAX_VALUE))
        );

        generalTabbedPane1.addTab(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel2.TabConstraints.tabTitle_1"), jPanel2); // NOI18N

        jPanel5.setPreferredSize(new Dimension(544, 290));
        jPanel5.setRequestFocusEnabled(false);

        jLabel5.setLabelFor(updateUrlTextBox);
        jLabel5.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel5.toolTipText")); // NOI18N

        updateUrlTextBox.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.updateUrlTextBox.toolTipText")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"pronom.update.url\"]}"),
                        updateUrlTextBox, BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        updateUrlTextBox.addActionListener(evt -> updateUrlTextBoxActionPerformed(evt));

        autoUpdatePanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createTitledBorder(""),
                        NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.autoUpdatePanel.border.title"))); // NOI18N
        autoUpdatePanel.setEnabled(false);

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.frequency.days\"]}"),
                        updateFrequencyTextBox, BeanProperty.create("value"));
        bindingGroup.addBinding(binding);
        binding = Bindings.createAutoBinding(UpdateStrategy.READ, updateScheduleRadioButton, ELProperty.create("${selected}"),
                updateFrequencyTextBox, BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel6.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel4.text")); // NOI18N

        updateFrequencyButtonGroup.add(updateOnStartupRadioButton);
        updateOnStartupRadioButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.updateOnStartupRadioButton.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.frequency.startup\"]}"),
                        updateOnStartupRadioButton, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        updateOnStartupRadioButton.addActionListener(evt -> updateOnStartupRadioButtonActionPerformed(evt));

        updateFrequencyButtonGroup.add(updateScheduleRadioButton);
        updateScheduleRadioButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.updateScheduleRadioButton.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_ONCE, this, ELProperty.create("${!globalConfig[\"update.frequency.startup\"]}"),
                        updateScheduleRadioButton, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        GroupLayout autoUpdatePanelLayout = new GroupLayout(autoUpdatePanel);
        autoUpdatePanel.setLayout(autoUpdatePanelLayout);
        autoUpdatePanelLayout.setHorizontalGroup(autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(autoUpdatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(updateOnStartupRadioButton)
                    .addGroup(autoUpdatePanelLayout.createSequentialGroup()
                        .addComponent(updateScheduleRadioButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(updateFrequencyTextBox, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jLabel6)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        autoUpdatePanelLayout.setVerticalGroup(autoUpdatePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(autoUpdatePanelLayout.createSequentialGroup()
                .addComponent(updateOnStartupRadioButton)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(autoUpdatePanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(updateFrequencyTextBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateScheduleRadioButton)
                    .addComponent(jLabel6))
                .addGap(12, 12, 12))
        );

        autoUpdateCheckbox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.autoUpdateCheckbox.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.autoCheck\"]}"),
                        autoUpdateCheckbox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        autoUpdateCheckbox.addItemListener(evt -> autoUpdateItemStateChanged(evt));

        jLabel7.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel6.text")); // NOI18N

        proxySettingsButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.proxySettingsButton.text")); // NOI18N
        proxySettingsButton.addActionListener(evt -> proxySettingsButtonActionPerformed(evt));

        containerSigUrl.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.containerSigUrl.toolTipText")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"container.update.url\"]}"),
                        containerSigUrl, BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel2.setLabelFor(containerSigUrl);
        jLabel2.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel2.text_1")); // NOI18N
        jLabel2.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel2.toolTipText")); // NOI18N

        jLabel1.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.text_1")); // NOI18N
        jLabel1.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.toolTipText")); // NOI18N

        jTextField1.setToolTipText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jLabel1.toolTipText")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"puid.urlPattern\"]}"),
                        jTextField1, BeanProperty.create("text_ON_ACTION_OR_FOCUS_LOST"));
        bindingGroup.addBinding(binding);

        askToDownloadCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.askToDownloadCheckBox.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.downloadPrompt\"]}"),
                        askToDownloadCheckBox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        autoSetDefaultSignatureFileCheckBox.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.autoSetDefaultSignatureFileCheckBox.text")); // NOI18N

        binding =
                Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, ELProperty.create("${globalConfig[\"update.autoSetDefault\"]}"),
                        autoSetDefaultSignatureFileCheckBox, BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        autoSetDefaultSignatureFileCheckBox.addActionListener(evt -> autoSetDefaultSignatureFileCheckBoxActionPerformed(evt));

        pronomUrlResetButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.pronomUrlResetButton.text")); // NOI18N
        pronomUrlResetButton.addActionListener(evt -> pronomUrlResetButtonActionPerformed(evt));

        GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pronomUrlResetButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(proxySettingsButton)
                .addContainerGap())
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(askToDownloadCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(autoSetDefaultSignatureFileCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(autoUpdateCheckbox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(autoUpdatePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel5))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(containerSigUrl)
                                    .addComponent(updateUrlTextBox)))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(0, 270, Short.MAX_VALUE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 143, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(jTextField1)))
                        .addContainerGap())))
        );
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING)
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
                .addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(proxySettingsButton)
                    .addComponent(pronomUrlResetButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoUpdateCheckbox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(askToDownloadCheckBox)
                .addGap(5, 5, 5)
                .addComponent(autoSetDefaultSignatureFileCheckBox)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(autoUpdatePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        generalTabbedPane1.addTab(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.jPanel3.TabConstraints.tabTitle"), jPanel5); // NOI18N

        cancelButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(evt -> cancelButtonActionPerformed(evt));

        okButton.setText(NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.okButton.text")); // NOI18N
        okButton.addActionListener(evt -> okButtonActionPerformed(evt));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(okButton))
                    .addComponent(generalTabbedPane1))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, okButton});

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalTabbedPane1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, okButton});

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        response = OK;
        if (isDirtyForNewProfile()) {
            int optionResponse = JOptionPane.showOptionDialog(
                    this,
                    NbBundle.getMessage(ConfigDialog.class, "CreateNewProfileQuestionDialog.body.text"),
                    NbBundle.getMessage(ConfigDialog.class, "CreateNewProfileQuestionDialog.title.text"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null
            );
            switch (optionResponse) {
                case JOptionPane.YES_OPTION:
                    automaticallyCreateNewProfile = true;
                    break;
                case JOptionPane.NO_OPTION:
                    automaticallyCreateNewProfile = false;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return;
            }
        }
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private boolean isDirtyForNewProfile() {
        for (String propertyName : propertiesAffectingNewProfile) {
            if (!Objects.equals(props.get(propertyName), propsOnLaunch.get(propertyName))) {
                return true;
            }
        }
        return false;
    }

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

    private void rowPerFormatButton1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rowPerFormatButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rowPerFormatButton1ActionPerformed

    private void pronomUrlResetButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_pronomUrlResetButtonActionPerformed
        String pronomSignatureUrl = context.getGlobalConfig().getDefaultProperties().getString(PRONOM_SIGNATURE_URL_KEY);
        String pronomContainerUrl = context.getGlobalConfig().getDefaultProperties().getString(PRONOM_CONTAINER_URL_KEY);
        updateUrlTextBox.setText(pronomSignatureUrl);
        containerSigUrl.setText(pronomContainerUrl);
    }//GEN-LAST:event_pronomUrlResetButtonActionPerformed

    private void toggleArchivesButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_toggleArchivesButtonActionPerformed
        if(!processTarCheckBox.isSelected()
                || !processZipCheckBox.isSelected()
                || !processGzipCheckBox.isSelected()
                || !processRarCheckBox.isSelected()
                || !process7zipCheckBox.isSelected()
                || !processIsoCheckBox.isSelected()
                || !processBzip2CheckBox.isSelected()
        ){
            processTarCheckBox.setSelected(true);
            processZipCheckBox.setSelected(true);
            processGzipCheckBox.setSelected(true);
            processRarCheckBox.setSelected(true);
            process7zipCheckBox.setSelected(true);
            processIsoCheckBox.setSelected(true);
            processBzip2CheckBox.setSelected(true);
        }else{
            processTarCheckBox.setSelected(false);
            processZipCheckBox.setSelected(false);
            processGzipCheckBox.setSelected(false);
            processRarCheckBox.setSelected(false);
            process7zipCheckBox.setSelected(false);
            processIsoCheckBox.setSelected(false);
            processBzip2CheckBox.setSelected(false);
        }
    }//GEN-LAST:event_toggleArchivesButtonActionPerformed

    private void processIsoCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processIsoCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processIsoCheckBoxActionPerformed

    private void process7zipCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_process7zipCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_process7zipCheckBoxActionPerformed

    private void processRarCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processRarCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processRarCheckBoxActionPerformed

    private void processGzipCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processGzipCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processGzipCheckBoxActionPerformed

    private void processTarCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processTarCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processTarCheckBoxActionPerformed

    private void processZipCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processZipCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processZipCheckBoxActionPerformed

    private void hashAlgorithmComboActionPerformed(ActionEvent evt) {//GEN-FIRST:event_hashAlgorithmComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hashAlgorithmComboActionPerformed

    private void containerSigComboActionPerformed(ActionEvent evt) {//GEN-FIRST:event_containerSigComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_containerSigComboActionPerformed

    private void generateHashCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_generateHashCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_generateHashCheckBoxActionPerformed

    private void processBzip2CheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processBzip2CheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processBzip2CheckBoxActionPerformed

    private void processArcCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processArcCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processArcCheckBoxActionPerformed

    private void processWarcCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_processWarcCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_processWarcCheckBoxActionPerformed

    private void toggleWebArchivesButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_toggleWebArchivesButtonActionPerformed
        if(!processArcCheckBox.isSelected() || !processWarcCheckBox.isSelected()){
            processArcCheckBox.setSelected(true);
            processWarcCheckBox.setSelected(true);
        }else{
            processArcCheckBox.setSelected(false);
            processWarcCheckBox.setSelected(false);
        }
    }//GEN-LAST:event_toggleWebArchivesButtonActionPerformed

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
    private JLabel archivesLabel;
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
    private JComboBox hashAlgorithmCombo;
    private JLabel jLabel1;
    private JLabel jLabel11;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel9;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JPanel jPanel6;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JButton okButton;
    private JCheckBox process7zipCheckBox;
    private JCheckBox processArcCheckBox;
    private JCheckBox processBzip2CheckBox;
    private JCheckBox processGzipCheckBox;
    private JCheckBox processIsoCheckBox;
    private JCheckBox processRarCheckBox;
    private JCheckBox processTarCheckBox;
    private JCheckBox processWarcCheckBox;
    private JCheckBox processZipCheckBox;
    private JButton pronomUrlResetButton;
    private JButton proxySettingsButton;
    private JRadioButton rowPerFileButton1;
    private JRadioButton rowPerFileButton2;
    private JRadioButton rowPerFormatButton1;
    private JRadioButton rowPerFormatButton2;
    private JButton toggleArchivesButton;
    private JButton toggleWebArchivesButton;
    private ButtonGroup updateFrequencyButtonGroup;
    private JSpinner updateFrequencyTextBox;
    private JRadioButton updateOnStartupRadioButton;
    private JRadioButton updateScheduleRadioButton;
    private JTextField updateUrlTextBox;
    private JLabel webArchivesLabel;
    private BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    /**
     * @return boolean indicating whether the user opted to create a new profile when this dialog closes
     */
    public boolean getCreateNewProfile() { return  automaticallyCreateNewProfile; }
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
     * @return the hashAlogorithm dropdown
     */
    JComboBox getHashAlgorithmCombo() {
        return hashAlgorithmCombo;
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
     *
     * @return collection of available hash algorithms.
     */
    public List<String> getAllHashAlgorithms() {
        return allHashAlgorithms;
    }

    /**
     * @return the allContainerSigFiles
     */
    public List<String> getAllContainerSigFiles() {
        return allContainerSigFiles;
    }
}
