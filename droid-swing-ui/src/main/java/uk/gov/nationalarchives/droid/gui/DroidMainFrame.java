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
package uk.gov.nationalarchives.droid.gui;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.joda.time.DateTime;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;
import uk.gov.nationalarchives.droid.core.interfaces.resource.ResourceUtils;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureType;
import uk.gov.nationalarchives.droid.export.interfaces.ExportOptions;
import uk.gov.nationalarchives.droid.gui.action.ActionFactory;
import uk.gov.nationalarchives.droid.gui.action.AddFilesAndFoldersAction;
import uk.gov.nationalarchives.droid.gui.action.ApplyFilterToTreeTableAction;
import uk.gov.nationalarchives.droid.gui.action.ExitAction;
import uk.gov.nationalarchives.droid.gui.action.LoadProfileWorker;
import uk.gov.nationalarchives.droid.gui.action.NewProfileAction;
import uk.gov.nationalarchives.droid.gui.action.OpenContainingFolderAction;
import uk.gov.nationalarchives.droid.gui.action.RemoveFilesAndFoldersAction;
import uk.gov.nationalarchives.droid.gui.action.StopRunningProfilesAction;
import uk.gov.nationalarchives.droid.gui.config.ConfigDialog;
import uk.gov.nationalarchives.droid.gui.config.InstallSignatureFileAction;
import uk.gov.nationalarchives.droid.gui.config.SignatureInstallDialog;
import uk.gov.nationalarchives.droid.gui.event.ButtonManager;
import uk.gov.nationalarchives.droid.gui.export.ExportAction;
import uk.gov.nationalarchives.droid.gui.export.ExportDialog;
import uk.gov.nationalarchives.droid.gui.export.ExportFileChooser;
import uk.gov.nationalarchives.droid.gui.export.ExportProgressDialog;
import uk.gov.nationalarchives.droid.gui.filechooser.ProfileFileChooser;
import uk.gov.nationalarchives.droid.gui.filechooser.ResourceSelectorDialog;
import uk.gov.nationalarchives.droid.gui.filter.FilterDialog;
import uk.gov.nationalarchives.droid.gui.filter.FilterFileChooser;
import uk.gov.nationalarchives.droid.gui.help.AboutDialog;
import uk.gov.nationalarchives.droid.gui.help.AboutDialogData;
import uk.gov.nationalarchives.droid.gui.report.ReportAction;
import uk.gov.nationalarchives.droid.gui.report.ReportDialog;
import uk.gov.nationalarchives.droid.gui.report.ReportProgressDialog;
import uk.gov.nationalarchives.droid.gui.report.ReportViewFrame;
import uk.gov.nationalarchives.droid.gui.signature.CheckSignatureUpdateAction;
import uk.gov.nationalarchives.droid.gui.signature.UpdateSignatureAction;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.ProfileSelectionDialog;
import uk.gov.nationalarchives.droid.planets.gui.PlanetXMLFileFilter;
import uk.gov.nationalarchives.droid.planets.gui.PlanetXMLProgressDialog;
import uk.gov.nationalarchives.droid.profile.FilterImpl;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileManagerException;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;
import uk.gov.nationalarchives.droid.profile.ProfileState;
import uk.gov.nationalarchives.droid.report.ReportTransformerImpl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * @author Alok Kumar Dash
 */
public class DroidMainFrame extends JFrame {

    private static final String ERROR_TITLE = "Error";

    private static final String STATE = "state";

    private static final String TWO_STRINGS_SEP_BY_SPACE = "%s %s";

    private static final String PROFILE_FOLDER_NAME = "profiles";

    private static final long serialVersionUID = 8170787911864425667L;

    private Logger log = LoggerFactory.getLogger(getClass());

    private ProfileManager profileManager;
    private DroidUIContext droidContext;
    private JFileChooser profileFileChooser = new ProfileFileChooser();
    private JFileChooser filterFileChooser;
    private ResourceSelectorDialog resourceFileChooser;
    private ButtonManager buttonManager;
    private GlobalContext globalContext;
    private ExportFileChooser exportFileChooser;
    private SignatureInstallDialog signatureInstallDialog;
    private ReportDialog reportDialog;
    private AboutDialog aboutDialog;
    
    private Set<ExitListener> exitListeners = new HashSet<ExitListener>();

    /**
     * Creates new form DroidMainFrame.
     */
    public DroidMainFrame() {
        super();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        RuntimeConfig.configureRuntimeEnvironment();
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        EventQueue.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            DroidMainFrame main = new DroidMainFrame();
            main.setVisible(false);
            main.init();
            main.setVisible(true);
            main.checkSignatureUpdates();
            main.createDefaultProfile();
        });
    }

    /**
     * 
     */
    public void checkSignatureUpdates() {
        final PropertiesConfiguration properties = globalContext.getGlobalConfig().getProperties();
        final FileHandler propertiesFileHandler = globalContext.getGlobalConfig().getPropertiesFileHandler();
        boolean autoCheck = properties.getBoolean(DroidGlobalProperty.UPDATE_AUTO_CHECK.getName());
        boolean checkNow = properties.getBoolean(DroidGlobalProperty.UPDATE_ON_STARTUP.getName());

        if (autoCheck) {
            String logMessage = "Checking for new signature updates on startup.";
            if (!checkNow) {
                long lastUpdated = properties.getLong(DroidGlobalProperty.LAST_UPDATE_CHECK.getName());
                int updateInterval = properties.getInt(DroidGlobalProperty.UPDATE_FREQUENCY_DAYS.getName());
                DateTime lastUpdateTime = new DateTime(lastUpdated);
                checkNow = lastUpdateTime.plusDays(updateInterval).isBeforeNow();
                logMessage = String.format("Checking for new signature updates -last update check was on: %s",
                        lastUpdateTime.toString("E yyyy-MM-dd"));
            }

            if (checkNow) {
                log.info(logMessage);
                final ActionFactory actionFactory = globalContext.getActionFactory();
                final CheckSignatureUpdateAction checkUpdatedSignatureAction = actionFactory
                        .newCheckSignatureUpdateAction();

                checkUpdatedSignatureAction.start(this);
                Map<SignatureType, SignatureFileInfo> availableUpdates = 
                    checkUpdatedSignatureAction.getSignatureFileInfos();

                // do the download, prompting if necesssary
                if (!checkUpdatedSignatureAction.hasError() 
                        && availableUpdates != null && !availableUpdates.isEmpty()) {
                    boolean showPrompt = properties.getBoolean("update.downloadPrompt");
                    
                    
                    Collection<SignatureFileInfo> filesToUpdate = showPrompt 
                            ? promptForUpdate(availableUpdates.values())
                            : availableUpdates.values();
                    
                    if (!filesToUpdate.isEmpty()) {
                        UpdateSignatureAction downloadAction = actionFactory.newSignaureUpdateAction();
                        downloadAction.setUpdates(filesToUpdate);
                        downloadAction.start(this);
                    }
                }
                
                properties.setProperty(DroidGlobalProperty.LAST_UPDATE_CHECK.getName(), System.currentTimeMillis());
                try {
                    propertiesFileHandler.save();
                } catch (ConfigurationException e) {
                    log.warn("Could not save the last update check time to the file: " + propertiesFileHandler.getPath());
                }
            }
        }
    }

    private void logSystemInformation() {
        AboutDialogData aboutDialogData = populateAboutDialogData();
        log.info("DROID Version {}", aboutDialogData.getDroidVersion());
        log.info("Build TimeStamp {}", aboutDialogData.getBuildTimeStamp());
        log.info("Java Version {}", aboutDialogData.getJavaVersion());
        log.info("Java Location {}", aboutDialogData.getJavaLocation());
        log.info("Operating System {}", aboutDialogData.getOperatingSystem());
        log.info("DROID Folder {}", aboutDialogData.getDroidFolder());
        log.info("Log Folder {}", aboutDialogData.getLogFolder());
    }

    /**
     * @param signatureFileInfos
     * @return signature files infos
     */
    private List<SignatureFileInfo> promptForUpdate(final Collection<SignatureFileInfo> signatureFileInfos) {
        List<SignatureFileInfo> filesToUpdate = new ArrayList<>();
        
        for (SignatureFileInfo sigFileInfo : signatureFileInfos) {
            if (sigFileInfo.hasError()) {
                DialogUtils.showSignatureUpdateErrorDialog(this, sigFileInfo.getError());
            } else {
                if (DialogUtils.showUpdateAvailableDialog(this, sigFileInfo) == JOptionPane.YES_OPTION) {
                    filesToUpdate.add(sigFileInfo);
                }
            }
        }
        
        return filesToUpdate;
    }

    public void createDefaultProfile() {
        final NewProfileAction newProfileAction = 
            new NewProfileAction(droidContext, profileManager, jProfilesTabbedPane);
        newProfileAction.addPropertyChangeListener(evt -> {
            if (STATE.equals(evt.getPropertyName()) && evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                exitListeners.remove(newProfileAction);
            }
        });
        exitListeners.add(newProfileAction);
        final ProfileForm profileForm = new ProfileForm(this, droidContext, buttonManager);
        try {
            newProfileAction.init(profileForm);
            newProfileAction.execute();
        } catch (ProfileManagerException e) {
            DialogUtils.showGeneralErrorDialog(this, ERROR_TITLE, e.getMessage());
        }
    }

    public void init() {
        log.info("Starting DROID.");
        logSystemInformation();
        URL icon = getClass().getResource("/uk/gov/nationalarchives/droid/icons/DROID16.gif");
        setIconImage(new ImageIcon(icon).getImage());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });


        initComponents();
        setLocationRelativeTo(null);

        helpMenuItem.addActionListener(evt -> {
            try {
                URI uri = globalContext.getGlobalConfig().getHelpPagesDir().resolve("indexs.html").toUri();
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        globalContext = new SpringGuiContext();
        profileManager = globalContext.getProfileManager();
        droidContext = new DroidUIContext(jProfilesTabbedPane, profileManager);
        exportFileChooser = new ExportFileChooser();
        filterFileChooser = new FilterFileChooser(globalContext.getGlobalConfig().getFilterDir().toFile());
        signatureInstallDialog = new SignatureInstallDialog(this);
        resourceFileChooser = new ResourceSelectorDialog(this);
        resourceFileChooser.setModal(true);

        AboutDialogData data = populateAboutDialogData();
        aboutDialog = new AboutDialog(this, true, data);

        reportDialog = new ReportDialog(this);

        initButtons();
    }

    private AboutDialogData populateAboutDialogData() {
        String version = NbBundle.getMessage(DroidMainFrame.class, "product.version");
        String buildTime = NbBundle.getMessage(DroidMainFrame.class, "product.build.time");
        String javaVersion = String.format(TWO_STRINGS_SEP_BY_SPACE,
                System.getProperty("java.runtime.name"), System.getProperty("java.runtime.version"));
        String javaLocation = System.getProperty("java.home");
        String osName = String.format(TWO_STRINGS_SEP_BY_SPACE,
                System.getProperty("os.name"), System.getProperty("os.version"));
        String droidUserDir = System.getProperty(RuntimeConfig.DROID_USER);
        String droidLogDir = System.getProperty(RuntimeConfig.LOG_DIR);
        return new AboutDialogData.AboutDialogDataBuilder()
                .withDroidVersion(version)
                .withBuildTimeStamp(buildTime)
                .withJavaVersion(javaVersion)
                .withJavaLocation(javaLocation)
                .withOsName(osName)
                .withDroidFolder(droidUserDir)
                .withLogFolder(droidLogDir)
                .build();
    }

    public long getProfileCount() {
        Path profilesPath = Paths.get(System.getProperty(RuntimeConfig.DROID_USER), PROFILE_FOLDER_NAME);
        if (!Files.exists(profilesPath)) {
            return -1;
        }
        try {
            long profileCount = Arrays.stream(new File(profilesPath.toUri()).listFiles()).filter(File::isDirectory).count();
            return profileCount;
        } catch (IOError | SecurityException  re) {
            //if we can't get the count, it should not stop droid from working, hence swallow the exception
            log.warn("Failed to get profile count because " + re.getMessage());
            return -1;
        }
    }

    private void initButtons() {
        buttonManager = new ButtonManager(droidContext);
        buttonManager.addCreateComponent(jButtonNewProfile);
        buttonManager.addCreateComponent(jMenuItemNew);

        buttonManager.addLoadComponent(jButtonOpenProfile);
        buttonManager.addLoadComponent(jMenuItemOpen);

        buttonManager.addSaveComponent(jButtonSaveProfile);
        buttonManager.addSaveComponent(jMenuSave);

        buttonManager.addSaveAsComponent(jMenuSaveAs);

        buttonManager.addRunComponent(jButtonStart);
        buttonManager.addRunComponent(jMenuItemStart);

        buttonManager.addStopComponent(jButtonStop);
        buttonManager.addStopComponent(jMenuItemStop);

        buttonManager.addResourceComponent(jButtonAddFile);
        buttonManager.addResourceComponent(jButtonRemoveFilesAndFolder);
        buttonManager.addResourceComponent(jMenuItemAddFileOrFolders);
        buttonManager.addResourceComponent(jMenuItemRemoveFolder);

        buttonManager.addExportComponent(jMenuItemExport);
        buttonManager.addExportComponent(jButtonExport);

        buttonManager.addFilterComponent(jMenuEditFilter);
        buttonManager.addFilterComponent(filterEnabledMenuItem);
        buttonManager.addFilterComponent(jButtonFilter);
        buttonManager.addFilterComponent(jMenuItemCopyFilterToAll);

        buttonManager.addFilterEnabledComponent(jFilterOnCheckBox);

        //buttonManager.addReportComponent(jMenuPlanetsXML);
        buttonManager.addReportComponent(jButtonReport);
        buttonManager.addReportComponent(generateReportMenuItem);

        buttonManager.fireEvent(null);
    }

    // </editor-fold>
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProfilesTabbedPane = new javax.swing.JTabbedPane();
        droidToolBar = new javax.swing.JToolBar();
        jButtonNewProfile = new javax.swing.JButton();
        jButtonOpenProfile = new javax.swing.JButton();
        jButtonSaveProfile = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        jButtonAddFile = new javax.swing.JButton();
        jButtonRemoveFilesAndFolder = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        jButtonStart = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jSeparator11 = new javax.swing.JToolBar.Separator();
        jButtonFilter = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jFilterOnCheckBox = new javax.swing.JCheckBox();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        jButtonReport = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemNew = new javax.swing.JMenuItem();
        jMenuItemOpen = new javax.swing.JMenuItem();
        jMenuSave = new javax.swing.JMenuItem();
        jMenuSaveAs = new javax.swing.JMenuItem();
        jMenuItemExport = new javax.swing.JMenuItem();
        jMenuQuit = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemAddFileOrFolders = new javax.swing.JMenuItem();
        jMenuItemRemoveFolder = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemOpenFolder = new javax.swing.JMenuItem();
        jMenuItemCopyToClipboard = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemExpandChildren = new javax.swing.JMenuItem();
        jMenuItemExpandNextThree = new javax.swing.JMenuItem();
        jMenuRun = new javax.swing.JMenu();
        jMenuItemStart = new javax.swing.JMenuItem();
        jMenuItemStop = new javax.swing.JMenuItem();
        jMenuFilter = new javax.swing.JMenu();
        filterEnabledMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jMenuEditFilter = new javax.swing.JMenuItem();
        jMenuItemCopyFilterToAll = new javax.swing.JMenuItem();
        jMenuReport = new javax.swing.JMenu();
        generateReportMenuItem = new javax.swing.JMenuItem();
        jMenuTools = new javax.swing.JMenu();
        jSeparator7 = new javax.swing.JSeparator();
        updateNowMenuItem = new javax.swing.JMenuItem();
        signatureInstallMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        settingsMenuItem = new javax.swing.JMenuItem();
        jhelp = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(DroidMainFrame.class, "main.title")); // NOI18N

        jProfilesTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jProfilesTabbedPane.setAutoscrolls(true);
        jProfilesTabbedPane.setMaximumSize(null);
        jProfilesTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jProfilesTabbedPaneStateChanged(evt);
            }
        });

        droidToolBar.setFloatable(false);
        droidToolBar.setRollover(true);
        droidToolBar.setMargin(new java.awt.Insets(4, 4, 4, 4));
        droidToolBar.setMaximumSize(new java.awt.Dimension(0, 0));
        droidToolBar.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                droidToolBarPropertyChange(evt);
            }
        });

        jButtonNewProfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/New.png"))); // NOI18N
        jButtonNewProfile.setName("New Profile");
        jButtonNewProfile.setText("New");
        jButtonNewProfile.setToolTipText("Create new profile");
        jButtonNewProfile.setFocusable(false);
        jButtonNewProfile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNewProfile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNewProfile.addActionListener(evt -> jButtonNewProfileActionPerformed(evt));
        droidToolBar.add(jButtonNewProfile);

        jButtonOpenProfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Open file.png"))); // NOI18N
        jButtonOpenProfile.setText("Open");
        jButtonOpenProfile.setName("Open");
        jButtonOpenProfile.setToolTipText("Open existing profile");
        jButtonOpenProfile.setFocusable(false);
        jButtonOpenProfile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOpenProfile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonOpenProfile.addActionListener(evt -> jButtonOpenProfileActionPerformed(evt));
        droidToolBar.add(jButtonOpenProfile);

        jButtonSaveProfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Save.png"))); // NOI18N
        jButtonSaveProfile.setText("Save");
        jButtonSaveProfile.setName("Save");
        jButtonSaveProfile.setToolTipText("Save profile");
        jButtonSaveProfile.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Save disabled.png"))); // NOI18N
        jButtonSaveProfile.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Save disabled.png"))); // NOI18N
        jButtonSaveProfile.setFocusable(false);
        jButtonSaveProfile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSaveProfile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSaveProfile.addActionListener(evt -> jButtonSaveProfileActionPerformed(evt));
        droidToolBar.add(jButtonSaveProfile);

        jButtonExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Export.png"))); // NOI18N
        jButtonExport.setText("Export");
        jButtonExport.setName("Export");
        jButtonExport.setToolTipText("Export results");
        jButtonExport.setFocusable(false);
        jButtonExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExport.addActionListener(evt -> jButtonExportActionPerformed(evt));
        droidToolBar.add(jButtonExport);

        jSeparator13.setSeparatorSize(new java.awt.Dimension(20, 40));
        droidToolBar.add(jSeparator13);

        jButtonAddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Add.png"))); // NOI18N
        jButtonAddFile.setText("Add");
        jButtonAddFile.setName("Add File");
        jButtonAddFile.setToolTipText("Add files or folders to profile");
        jButtonAddFile.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Add disabled.png"))); // NOI18N
        jButtonAddFile.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Add disabled.png"))); // NOI18N
        jButtonAddFile.setFocusable(false);
        jButtonAddFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonAddFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonAddFile.addActionListener(evt -> jButtonAddFileActionPerformed(evt));
        droidToolBar.add(jButtonAddFile);

        jButtonRemoveFilesAndFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Remove.png"))); // NOI18N
        jButtonRemoveFilesAndFolder.setText("Remove");
        jButtonRemoveFilesAndFolder.setName("Remove");
        jButtonRemoveFilesAndFolder.setToolTipText("Remove files/folders from profile");
        jButtonRemoveFilesAndFolder.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Remove disabled.png"))); // NOI18N
        jButtonRemoveFilesAndFolder.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Remove disabled.png"))); // NOI18N
        jButtonRemoveFilesAndFolder.setFocusable(false);
        jButtonRemoveFilesAndFolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRemoveFilesAndFolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRemoveFilesAndFolder.addActionListener(evt -> jButtonRemoveFilesAndFolderActionPerformed(evt));
        droidToolBar.add(jButtonRemoveFilesAndFolder);

        jSeparator12.setSeparatorSize(new java.awt.Dimension(20, 40));
        droidToolBar.add(jSeparator12);

        jButtonStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Play.png"))); // NOI18N
        jButtonStart.setText("Start");
        jButtonStart.setName("Start");
        jButtonStart.setToolTipText("Run identification");
        jButtonStart.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Play disabled.png"))); // NOI18N
        jButtonStart.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Play disabled.png"))); // NOI18N
        jButtonStart.setFocusable(false);
        jButtonStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonStart.addActionListener(evt -> jButtonStartActionPerformed(evt));
        droidToolBar.add(jButtonStart);

        jButtonStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Pause.png"))); // NOI18N
        jButtonStop.setText("Pause");
        jButtonStop.setName("Pause");
        jButtonStop.setToolTipText("Pause identification");
        jButtonStop.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Pause disabled.png"))); // NOI18N
        jButtonStop.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Pause disabled.png"))); // NOI18N
        jButtonStop.setFocusable(false);
        jButtonStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonStop.addActionListener(evt -> jButtonStopActionPerformed(evt));
        droidToolBar.add(jButtonStop);

        jSeparator11.setSeparatorSize(new java.awt.Dimension(20, 40));
        droidToolBar.add(jSeparator11);

        jButtonFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/icons/Filter.png"))); // NOI18N
        jButtonFilter.setText("Filter");
        jButtonFilter.setToolTipText("Define and apply filter to results");
        jButtonFilter.setFocusable(false);
        jButtonFilter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonFilter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonFilter.addActionListener(evt -> jButtonFilterActionPerformed(evt));
        droidToolBar.add(jButtonFilter);

        jPanel1.setMaximumSize(new java.awt.Dimension(30, 60));
        jPanel1.setMinimumSize(new java.awt.Dimension(30, 60));
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(30, 60));

        jFilterOnCheckBox.setText("On");
        jFilterOnCheckBox.setFocusable(false);
        jFilterOnCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jFilterOnCheckBox.setInheritsPopupMenu(true);
        jFilterOnCheckBox.setMaximumSize(new java.awt.Dimension(28, 55));
        jFilterOnCheckBox.setMinimumSize(new java.awt.Dimension(30, 55));
        jFilterOnCheckBox.setPreferredSize(new java.awt.Dimension(30, 55));
        jFilterOnCheckBox.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jFilterOnCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jFilterOnCheckBox.addActionListener(evt -> jFilterOnCheckBoxActionPerformed(evt));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jFilterOnCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jFilterOnCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        droidToolBar.add(jPanel1);

        jSeparator10.setSeparatorSize(new java.awt.Dimension(20, 40));
        droidToolBar.add(jSeparator10);

        jButtonReport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Report.png"))); // NOI18N
        jButtonReport.setText("Report");
        jButtonReport.setName("Report");
        jButtonReport.setToolTipText("Generate a statistical report over the open profiles");
        jButtonReport.setFocusable(false);
        jButtonReport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonReport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonReport.addActionListener(evt -> jButtonReportActionPerformed(evt));
        droidToolBar.add(jButtonReport);

        jMenuBar1.setPreferredSize(new java.awt.Dimension(100, 25));

        jMenuFile.setMnemonic('F');
        jMenuFile.setText("File");
        jMenuFile.setName("File");
        jMenuFile.setActionCommand("file");
        jMenuFile.addActionListener(evt -> jMenuFileActionPerformed(evt));

        jMenuItemNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/New small.png"))); // NOI18N
        jMenuItemNew.setText("New");
        jMenuItemNew.setName("New");
        jMenuItemNew.setToolTipText("New profile");
        jMenuItemNew.addActionListener(evt -> jMenuItemNewActionPerformed(evt));
        jMenuFile.add(jMenuItemNew);

        jMenuItemOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Open file small.png"))); // NOI18N
        jMenuItemOpen.setText("Open");
        jMenuItemOpen.setToolTipText("Open a profile");
        jMenuItemOpen.addActionListener(evt -> jMenuItemOpenActionPerformed(evt));
        jMenuFile.add(jMenuItemOpen);

        jMenuSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Save small.png"))); // NOI18N
        jMenuSave.setText("Save");
        jMenuSave.setToolTipText("Save a profile");
        jMenuSave.setActionCommand("save");
        jMenuSave.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/icons/Save Blue 16 d g.gif"))); // NOI18N
        jMenuSave.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/icons/Save Blue 16 d g.gif"))); // NOI18N
        jMenuSave.addActionListener(evt -> jMenuSaveActionPerformed(evt));
        jMenuFile.add(jMenuSave);

        jMenuSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Save As small.png"))); // NOI18N
        jMenuSaveAs.setText("Save As...");
        jMenuSaveAs.setName("Save As...");
        jMenuSaveAs.setToolTipText("Save a profile to a specified file");
        jMenuSaveAs.setActionCommand("save");
        jMenuSaveAs.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Save As small disabled.png"))); // NOI18N
        jMenuSaveAs.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Save As small disabled.png"))); // NOI18N
        jMenuSaveAs.addActionListener(evt -> jMenuSaveAsActionPerformed(evt));
        jMenuFile.add(jMenuSaveAs);

        jMenuItemExport.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Export small.png"))); // NOI18N
        jMenuItemExport.setText("Export all...");
        jMenuItemExport.setToolTipText("Export profiles");
        jMenuItemExport.setActionCommand("export");
        jMenuItemExport.addActionListener(evt -> jMenuItemExportActionPerformed(evt));
        jMenuFile.add(jMenuItemExport);

        jMenuQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Quit Small.png"))); // NOI18N
        jMenuQuit.setText("Quit");
        jMenuQuit.addActionListener(evt -> jMenuQuitActionPerformed(evt));
        jMenuFile.add(jMenuQuit);
        jMenuFile.add(jSeparator4);

        jMenuBar1.add(jMenuFile);

        jMenuEdit.setMnemonic('E');
        jMenuEdit.setText("Edit");
        jMenuEdit.setName("Edit");
        jMenuEdit.setActionCommand("edit");
        jMenuEdit.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                jMenuEditMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        jMenuItemAddFileOrFolders.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemAddFileOrFolders.setName("Add file/folders");
        jMenuItemAddFileOrFolders.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Add small.png"))); // NOI18N
        jMenuItemAddFileOrFolders.setText("Add file/folders");
        jMenuItemAddFileOrFolders.setToolTipText("Add files or folders to a profile");
        jMenuItemAddFileOrFolders.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Add small disabled.png"))); // NOI18N
        jMenuItemAddFileOrFolders.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Add small disabled.png"))); // NOI18N
        jMenuItemAddFileOrFolders.addActionListener(evt -> jMenuItemAddFileOrFoldersActionPerformed(evt));
        jMenuEdit.add(jMenuItemAddFileOrFolders);

        jMenuItemRemoveFolder.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemRemoveFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Remove small.png"))); // NOI18N
        jMenuItemRemoveFolder.setText("Remove files/folders");
        jMenuItemRemoveFolder.setName("Remove files/folders");
        jMenuItemRemoveFolder.setToolTipText("Remove files or folders from a profile");
        jMenuItemRemoveFolder.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Remove small disabled.png"))); // NOI18N
        jMenuItemRemoveFolder.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Remove small disabled.png"))); // NOI18N
        jMenuItemRemoveFolder.addActionListener(evt -> jMenuItemRemoveFolderActionPerformed(evt));
        jMenuEdit.add(jMenuItemRemoveFolder);
        jMenuEdit.add(jSeparator1);

        jMenuItemOpenFolder.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemOpenFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/icons/Icon_External_Link.png"))); // NOI18N
        jMenuItemOpenFolder.setText("Open containing folder...");
        jMenuItemOpenFolder.addActionListener(evt -> OpenContainingFolder(evt));
        jMenuEdit.add(jMenuItemOpenFolder);

        jMenuItemCopyToClipboard.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemCopyToClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Clipboard small.png"))); // NOI18N
        jMenuItemCopyToClipboard.setText("Copy to clipboard");
        jMenuItemCopyToClipboard.setName("Copy to clipboard");
        jMenuItemCopyToClipboard.addActionListener(evt -> jMenuItemCopyToClipboardActionPerformed(evt));
        jMenuEdit.add(jMenuItemCopyToClipboard);
        jMenuEdit.add(jSeparator2);

        jMenuItemExpandChildren.setText("Expand");
        jMenuItemExpandChildren.addActionListener(evt -> jMenuItemExpandChildrenActionPerformed(evt));
        jMenuEdit.add(jMenuItemExpandChildren);

        jMenuItemExpandNextThree.setText("Expand next three levels");
        jMenuItemExpandNextThree.addActionListener(evt -> jMenuItemExpandNextThreeActionPerformed(evt));
        jMenuEdit.add(jMenuItemExpandNextThree);

        jMenuBar1.add(jMenuEdit);

        jMenuRun.setMnemonic('R');
        jMenuRun.setText("Run");
        jMenuRun.setName("Run");
        jMenuRun.setActionCommand("run");

        jMenuItemStart.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Play small.png"))); // NOI18N
        jMenuItemStart.setText("Start identification");
        jMenuItemStart.setName("Start identification");
        jMenuItemStart.setToolTipText("Start identifying files in the profile");
        jMenuItemStart.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Play small disabled.png"))); // NOI18N
        jMenuItemStart.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Play small disabled.png"))); // NOI18N
        jMenuItemStart.addActionListener(evt -> jMenuItemStartActionPerformed(evt));
        jMenuRun.add(jMenuItemStart);

        jMenuItemStop.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Pause small.png"))); // NOI18N
        jMenuItemStop.setText("Pause identification");
        jMenuItemStop.setName("Pause identification");
        jMenuItemStop.setToolTipText("Pause identification in the profile");
        jMenuItemStop.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Pause small disabled.png"))); // NOI18N
        jMenuItemStop.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Pause small disabled.png"))); // NOI18N
        jMenuItemStop.addActionListener(evt -> jMenuItemStopActionPerformed(evt));
        jMenuRun.add(jMenuItemStop);

        jMenuBar1.add(jMenuRun);

        jMenuFilter.setMnemonic('l');
        jMenuFilter.setText("Filter");
        jMenuFilter.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                jMenuFilterMenuSelected(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
        });

        filterEnabledMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        filterEnabledMenuItem.setText("Filter on");
        filterEnabledMenuItem.setToolTipText("Toggles filtering on or off in the profile");
        filterEnabledMenuItem.addActionListener(evt -> filterEnabledMenuItemActionPerformed(evt));
        jMenuFilter.add(filterEnabledMenuItem);
        jMenuFilter.add(jSeparator8);

        jMenuEditFilter.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuEditFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Filter small.png"))); // NOI18N
        jMenuEditFilter.setText("Edit filter...");
        jMenuEditFilter.setToolTipText("Edit the filter for a profile");
        jMenuEditFilter.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Filter small disabled.png"))); // NOI18N
        jMenuEditFilter.addActionListener(evt -> jMenuEditFilterActionPerformed(evt));
        jMenuFilter.add(jMenuEditFilter);

        jMenuItemCopyFilterToAll.setText("Copy filter to all profiles...");
        jMenuItemCopyFilterToAll.setToolTipText("Copies the filter in the current profile to all open profiles");
        jMenuItemCopyFilterToAll.addActionListener(evt -> jMenuItemCopyFilterToAllActionPerformed(evt));
        jMenuFilter.add(jMenuItemCopyFilterToAll);

        jMenuBar1.add(jMenuFilter);

        jMenuReport.setMnemonic('p');
        jMenuReport.setText("Report");
        jMenuReport.setActionCommand("report");

        generateReportMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        generateReportMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Report small.png"))); // NOI18N
        generateReportMenuItem.setText("Generate Report...");
        generateReportMenuItem.setToolTipText("Generates a report in DROID XML format");
        generateReportMenuItem.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Report small disabled.png"))); // NOI18N
        generateReportMenuItem.addActionListener(evt -> generateReportMenuItemActionPerformed(evt));
        jMenuReport.add(generateReportMenuItem);

        jMenuBar1.add(jMenuReport);

        jMenuTools.setMnemonic('T');
        jMenuTools.setText("Tools");
        jMenuTools.setName("Tools");
        jMenuTools.addActionListener(evt -> jMenuToolsActionPerformed(evt));
        jMenuTools.add(jSeparator7);

        updateNowMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        updateNowMenuItem.setText("Check for signature updates...");
        updateNowMenuItem.setToolTipText("Checks to see if there are updated signatures");
        updateNowMenuItem.addActionListener(evt -> updateNowMenuItemActionPerformed(evt));
        jMenuTools.add(updateNowMenuItem);

        signatureInstallMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        signatureInstallMenuItem.setText("Install signature file...");
        signatureInstallMenuItem.setToolTipText("Installs a signature file from your local file system");
        signatureInstallMenuItem.addActionListener(evt -> signatureInstallMenuItemActionPerformed(evt));
        jMenuTools.add(signatureInstallMenuItem);
        jMenuTools.add(jSeparator6);

        settingsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        settingsMenuItem.setText("Preferences...");
        settingsMenuItem.setName("Preferences");
        settingsMenuItem.setToolTipText("Set the DROID preferences");
        settingsMenuItem.addActionListener(evt -> settingsMenuItemActionPerformed(evt));
        jMenuTools.add(settingsMenuItem);

        jMenuBar1.add(jMenuTools);

        jhelp.setMnemonic('H');
        jhelp.setText("Help");
        jhelp.setActionCommand("help");

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        helpMenuItem.setText("Help");
        jhelp.add(helpMenuItem);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(evt -> aboutMenuItemActionPerformed(evt));
        jhelp.add(aboutMenuItem);

        jMenuBar1.add(jhelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(droidToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
            .addComponent(jProfilesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(droidToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProfilesTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OpenContainingFolder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenContainingFolder
        OpenContainingFolderAction openFolders = new OpenContainingFolderAction();
        openFolders.open(getSelectedNodes());

    }//GEN-LAST:event_OpenContainingFolder

    private void jMenuItemCopyToClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyToClipboardActionPerformed
       ProfileForm selectedProfile = droidContext.getSelectedProfile();
       selectedProfile.copySelectedToClipboard();
    }//GEN-LAST:event_jMenuItemCopyToClipboardActionPerformed

    private void jMenuEditMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_jMenuEditMenuSelected
        ProfileForm selectedProfile = droidContext.getSelectedProfile();
        jMenuItemOpenFolder.setEnabled(selectedProfile.anyRowsSelected());
        jMenuItemCopyToClipboard.setEnabled(selectedProfile.anyRowsSelected());
    }//GEN-LAST:event_jMenuEditMenuSelected

    private void jMenuItemExpandChildrenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExpandChildrenActionPerformed
        ProfileForm selectedProfile = droidContext.getSelectedProfile();
        selectedProfile.expandSelectedNodes(false);
    }//GEN-LAST:event_jMenuItemExpandChildrenActionPerformed

    private void jMenuItemExpandNextThreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExpandNextThreeActionPerformed
        ProfileForm selectedProfile = droidContext.getSelectedProfile();
        selectedProfile.expandSelectedNodes(true);
    }//GEN-LAST:event_jMenuItemExpandNextThreeActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private List<ProfileResourceNode> getSelectedNodes() {
        ProfileForm selectedProfile = droidContext.getSelectedProfile();
        return selectedProfile.getSelectedNodes();
    }

    private void generateReportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_generateReportMenuItemActionPerformed
        generateReport();
    }// GEN-LAST:event_generateReportMenuItemActionPerformed

    private void generateReport() {
        reportDialog.showDialog();
        if (reportDialog.isApproved()) {
            ReportAction action = globalContext.getActionFactory().newReportAction();
            action.setReportSpec(reportDialog.getSelectedReportSpec());
            action.setProfileIds(reportDialog.getSelectedProfileIds());
            // action.setTargetFile(reportDialog.getTarget());
            ReportProgressDialog reportProgressDialog = new ReportProgressDialog(this, action);
            ReportViewFrame reportViewDialog = new ReportViewFrame(this);
            //FIXME: the report transformer is defined as a singleton bean in the export report
            // action configured through spring.  Here we are instantiating a new specific
            // transformer - there was a bug in that this one did not have the droid config
            // object configured.  For the time being, just set up this transformer correctly.
            ReportTransformerImpl transformer = new ReportTransformerImpl();
            transformer.setConfig(globalContext.getGlobalConfig());
            reportViewDialog.setReportTransformer(transformer);


            action.setProgressDialog(reportProgressDialog);
            action.setViewDialog(reportViewDialog);
            action.execute();
            reportProgressDialog.showDialog();
        }
    }

    private void jFilterOnCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jFilterOnCheckBoxActionPerformed
        setFilterStatus(jFilterOnCheckBox.isSelected());
    }// GEN-LAST:event_jFilterOnCheckBoxActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonExportActionPerformed
        export();
    }// GEN-LAST:event_jButtonExportActionPerformed

    private void jButtonReportActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonReportActionPerformed
        generateReport();
    }// GEN-LAST:event_jButtonReportActionPerformed

    private void jMenuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuSaveAsActionPerformed
        saveProfileAsAction(evt);
    }// GEN-LAST:event_jMenuSaveAsActionPerformed

    private void jMenuQuitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuQuitActionPerformed
        exit();
    }// GEN-LAST:event_jMenuQuitActionPerformed

    private void filterEnabledMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_filterEnabledMenuItemActionPerformed
        setFilterStatus(filterEnabledMenuItem.isSelected());
    }// GEN-LAST:event_filterEnabledMenuItemActionPerformed

    private void setFilterStatus(boolean filterStatus) {
        FilterImpl filter = getFilter();
        if (filter != null) {
            filter.setEnabled(filterStatus);
        }
        ProfileForm profileToFilter = droidContext.getSelectedProfile();
        ApplyFilterToTreeTableAction applyFilter = new ApplyFilterToTreeTableAction(profileToFilter, profileManager);
        applyFilter.applyFilter();
        updateFilterControls();
    }

    private FilterImpl getFilter() {
        FilterImpl result = null;
        ProfileForm form = droidContext.getSelectedProfile();
        if (form != null) {
            ProfileInstance instance = form.getProfile();
            if (instance != null) {
                result = instance.getFilter();
            }
        }
        return result;
    }

    /**
     * Updates the filter controls based on the currently loaded profile tab.
     */
    public void updateFilterControls() {

        FilterImpl filter = getFilter();
        if (filter == null || !filter.hasCriteria()) {
            filterEnabledMenuItem.setSelected(false);
            filterEnabledMenuItem.setEnabled(false);
            jFilterOnCheckBox.setSelected(false);
            jFilterOnCheckBox.setEnabled(false);
        } else {
            filterEnabledMenuItem.setSelected(filter.isEnabled());
            jFilterOnCheckBox.setSelected(filter.isEnabled());
        }
    }

    /*
     * This appears to be a hack to make sure that the filter enabled sub-menu
     * is set appropriately before it becomes visible, by intercepting the
     * parent menu selection.
     */
    private void jMenuFilterMenuSelected(javax.swing.event.MenuEvent evt) {// GEN-FIRST:event_jMenuFilterMenuSelected

        FilterImpl filter = getFilter();
        if (filter == null || !filter.hasCriteria()) {
            filterEnabledMenuItem.setSelected(false);
            filterEnabledMenuItem.setEnabled(false);
            jFilterOnCheckBox.setSelected(false);
            jFilterOnCheckBox.setEnabled(false);
        } else {
            // filterEnabledMenuItem.setSelected(filter.isEnabled());
            // jFilterOnCheckBox.setSelected(filter.isEnabled());
            filterEnabledMenuItem.setEnabled(true);
            if (filter.isEnabled()) {
                filterEnabledMenuItem.setSelected(true);
                jFilterOnCheckBox.setSelected(true);
            } else {
                filterEnabledMenuItem.setSelected(false);
                jFilterOnCheckBox.setSelected(false);
            }
        }
    }// GEN-LAST:event_jMenuFilterMenuSelected

    private void signatureInstallMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_signatureInstallMenuItemActionPerformed
        signatureInstallDialog.setLocationRelativeTo(this);
        signatureInstallDialog.setVisible(true);
        if (signatureInstallDialog.getResponse() == SignatureInstallDialog.OK) {
            InstallSignatureFileAction action = globalContext.getActionFactory().newInstallSignatureFileAction();
            action.setFileName(signatureInstallDialog.getSignatureFilename());
            action.setUseAsDefault(signatureInstallDialog.isDefault());
            action.execute(this);
        }

    }// GEN-LAST:event_signatureInstallMenuItemActionPerformed

    private void jMenuEditFilterActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuFilterActionPerformed
        FilterImpl filter = getFilter();
        FilterDialog dialog = new FilterDialog(this, true, filter, droidContext, profileManager, filterFileChooser);
        // dialog.setTitle("Filter Selection dialog.");
        dialog.setVisible(true);
        updateFilterControls();

    }// GEN-LAST:event_jMenuFilterActionPerformed

    private void jButtonOpenProfileActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonOpenProfileActionPerformed
        openProfileAction(evt);
        updateFilterControls();
    }// GEN-LAST:event_jButtonOpenProfileActionPerformed

    private void jMenuItemNewActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemNewActionPerformed

        jButtonNewProfileActionPerformed(evt);

    }// GEN-LAST:event_jMenuItemNewActionPerformed

    private void jMenuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemOpenActionPerformed
        openProfileAction(evt);
        updateFilterControls();
    }// GEN-LAST:event_jMenuItemOpenActionPerformed

    private void jMenuSaveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuSaveActionPerformed
        saveProfileAction(evt);
    }// GEN-LAST:event_jMenuSaveActionPerformed

    private void jMenuItemExportActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemExportActionPerformed
        export();
    }// GEN-LAST:event_jMenuItemExportActionPerformed

    private void jMenuExitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuExitActionPerformed
        exit();
    }// GEN-LAST:event_jMenuExitActionPerformed

    private void jMenuItemStartActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemStartActionPerformed
        startProfile();
    }// GEN-LAST:event_jMenuItemStartActionPerformed

    private void jMenuItemStopActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemStopActionPerformed
        stopProfile();
    }// GEN-LAST:event_jMenuItemStopActionPerformed

    private void jMenuItemAddFileOrFoldersActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemAddFileOrFoldersActionPerformed

        jButtonAddFileActionPerformed(evt);

    }// GEN-LAST:event_jMenuItemAddFileOrFoldersActionPerformed

    private void jMenuItemRemoveFolderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemRemoveFolderActionPerformed

        jButtonRemoveFilesAndFolderActionPerformed(evt);

    }// GEN-LAST:event_jMenuItemRemoveFolderActionPerformed

    private void jButtonSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonSaveProfileActionPerformed
        saveProfileAction(evt);
    }// GEN-LAST:event_jButtonSaveProfileActionPerformed

    private void jProfilesTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_jProfilesTabbedPaneStateChanged
        ProfileForm profileForm = droidContext.getSelectedProfile();
        buttonManager.fireEvent(profileForm == null ? null : profileForm.getProfile());
        updateFilterControls();

    }// GEN-LAST:event_jProfilesTabbedPaneStateChanged

    private void droidToolBarPropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_droidToolBarPropertyChange

        // TODO add your handling code here:
    }// GEN-LAST:event_droidToolBarPropertyChange

    private void jMenuFileActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuFileActionPerformed

    }// GEN-LAST:event_jMenuFileActionPerformed

    private void jButtonFilterActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonFilterActionPerformed
        FilterImpl filter = getFilter();
        FilterDialog dialog = new FilterDialog(this, true, filter, droidContext, profileManager, filterFileChooser);
        // dialog.setTitle("Filter Selection dialog.");
        dialog.setVisible(true);
        updateFilterControls();

    }// GEN-LAST:event_jButtonFilterActionPerformed

    private void jMenuItemCopyFilterToAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemCopyFIlterToAllActionPerformed
        FilterImpl filter = getFilter();
        Collection<ProfileForm> profileForms = droidContext.allProfiles();
        for (ProfileForm profileForm : profileForms) {
            profileForm.getProfile().setFilter((FilterImpl) filter.clone());
            ApplyFilterToTreeTableAction filterProfile = new ApplyFilterToTreeTableAction(profileForm, profileManager);
            filterProfile.applyFilter();
        }

    }// GEN-LAST:event_jMenuItemCopyFIlterToAllActionPerformed

    private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_settingsMenuItemActionPerformed
        // create the dialog and initialise the dialog's values
        Map<String, Object> settings = globalContext.getGlobalConfig().getPropertiesMap();

        ConfigDialog configDialog = new ConfigDialog(this, globalContext);
        try {
            configDialog.init(settings);
            configDialog.setVisible(true);
            if (configDialog.getResponse() == ConfigDialog.OK) {
                try {
                    globalContext.getGlobalConfig().update(configDialog.getGlobalConfig());
                } catch (ConfigurationException e) {
                    log.error("Error updating properties: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(configDialog, NbBundle.getMessage(ConfigDialog.class,
                                    "ConfigDialog.error.text"),
                            NbBundle.getMessage(ConfigDialog.class, "ConfigDialog.error.title"), JOptionPane.ERROR_MESSAGE);
                }
            }

            if (configDialog.getCreateNewProfile()) {
                createAndInitNewProfile();
            }
        } finally {
            configDialog.dispose();
        }
    }// GEN-LAST:event_settingsMenuItemActionPerformed

    private void jMenuToolsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuToolsActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jMenuToolsActionPerformed

    private void updateNowMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_updateNowMenuItemActionPerformed
        final ActionFactory actionFactory = globalContext.getActionFactory();
        final CheckSignatureUpdateAction checkUpdatedSignatureAction = actionFactory.newCheckSignatureUpdateAction();

        checkUpdatedSignatureAction.start(this);
        Map<SignatureType, SignatureFileInfo> availableUpdates = checkUpdatedSignatureAction.getSignatureFileInfos();

        if (!checkUpdatedSignatureAction.hasError() && !checkUpdatedSignatureAction.isCancelled()) {
            // do the download, prompting if necesssary
            if (!availableUpdates.isEmpty()) {
                if (!promptForUpdate(availableUpdates.values()).isEmpty()) {
                    UpdateSignatureAction downloadAction = actionFactory.newSignaureUpdateAction();
                    downloadAction.setUpdates(availableUpdates.values());
                    downloadAction.start(this);
                }
            } else {
                DialogUtils.showUpdateUnavailableDialog(this);
            }
        }
    }// GEN-LAST:event_updateNowMenuItemActionPerformed

    private void jMenuPlanetsXMLActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuPlanetsXMLActionPerformed

        String profileId = droidContext.getSelectedProfile().getProfile().getUuid();

        if (droidContext.getSelectedProfile().getProfile().getState().equals(ProfileState.FINISHED)
                || droidContext.getSelectedProfile().getProfile().getState().equals(ProfileState.STOPPED)) {
            String filePath = "";
            JFileChooser c = new JFileChooser();
            c.setDialogTitle("Please provide name of the file.");
            PlanetXMLFileFilter fileFilter = new PlanetXMLFileFilter("xml", "Planet XML");
            c.addChoosableFileFilter(fileFilter);
            int rVal = c.showSaveDialog(this);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                if (fileFilter.getExtension(c.getSelectedFile()) == null) {
                    filePath = c.getCurrentDirectory().toString() + File.separator + c.getSelectedFile().getName()
                            + ".xml";
                } else {
                    filePath = c.getSelectedFile().getPath();
                }
                // if file dose not exist.
                if (!new File(filePath).isFile()) {
                    // Call PlanetXMLDIalog with the file path.
                    PlanetXMLProgressDialog planetXMLProgressDialog = new PlanetXMLProgressDialog(this, true, filePath,
                            profileId, globalContext.getReportManager());
                    planetXMLProgressDialog.show();
                    // if Overwrite option is selected.
                } else if (JOptionPane.showConfirmDialog(this, "File already exists. Do you want to overwrite.",
                        "File already exists", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    PlanetXMLProgressDialog planetXMLProgressDialog = new PlanetXMLProgressDialog(this, true, filePath,
                            profileId, globalContext.getReportManager());
                    planetXMLProgressDialog.setLocationRelativeTo(this);
                    planetXMLProgressDialog.show();

                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selected profile not paused or finished.", "Profile state error",
                    JOptionPane.ERROR_MESSAGE);

        }

    }// GEN-LAST:event_menuPlanetsXMLActionPerformed

    private void jButtonNewProfileActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonNewProfileActionPerformed

        createAndInitNewProfile();

    }// GEN-LAST:event_jButtonNewProfileActionPerformed

    private void createAndInitNewProfile() {
        final NewProfileAction newProfileAction = new NewProfileAction(droidContext, profileManager, jProfilesTabbedPane);
        newProfileAction.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (STATE.equals(evt.getPropertyName()) && evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                    exitListeners.remove(newProfileAction);
                }
            }
        });
        exitListeners.add(newProfileAction);

        try {
            newProfileAction.init(new ProfileForm(this, droidContext, buttonManager));
            newProfileAction.execute();
        } catch (ProfileManagerException e) {
            DialogUtils.showGeneralErrorDialog(this, ERROR_TITLE, e.getMessage());
        }
    }

    public void jButtonAddFileActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonAddFileActionPerformed

        int returnVal = resourceFileChooser.showDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            List<File> selectedFiles = resourceFileChooser.getSelectedFiles();
            addFilesAndFolders(selectedFiles);
        }
    }// GEN-LAST:event_jButtonAddFileActionPerformed


    /**
     * Adds a list of files to the profile.
     *
     * @param files The list of files to add.
     */
    public void addFilesAndFolders(List<File> files) {
        FileSystemSelectionValidator validator = new FileSystemSelectionValidator(files);
        if (validator.isSelectionValid()) {
            AddFilesAndFoldersAction action = new AddFilesAndFoldersAction(droidContext, profileManager);
            action.add(files, resourceFileChooser.isSelectionRecursive());
        } else {
            String error = validator.getErrorMessage();
            DialogUtils.showGeneralErrorDialog(this, ERROR_TITLE, error);
            return;
        }
    }

    public void jButtonRemoveFilesAndFolderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonRemoveFilesAndFolderActionPerformed
        if (droidContext.getSelectedProfile().getResultsOutline().getSelectedRows().length == 0) {
            DialogUtils.showNothingIsSelectedForRemoveDialog(this);
        }
        RemoveFilesAndFoldersAction removeAction = new RemoveFilesAndFoldersAction(droidContext, profileManager);
        removeAction.remove();

    }// GEN-LAST:event_jButtonRemoveFilesAndFolderActionPerformed

    /**
     *
     * @return true if the add file or folders menu item is enabled.
     */
    public boolean getAddEnabled() {
        return jMenuItemAddFileOrFolders.isEnabled();
    }

    /**
     *
     * @return true if the remove file or folders menu item is enabled.
     */
    public boolean getRemoveEnabled() {
        return jMenuItemRemoveFolder.isEnabled();
    }

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonStartActionPerformed
        startProfile();

    }// GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonStopActionPerformed
        stopProfile();
    }// GEN-LAST:event_jButtonStopActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JToolBar droidToolBar;
    private javax.swing.JCheckBoxMenuItem filterEnabledMenuItem;
    private javax.swing.JMenuItem generateReportMenuItem;
    private javax.swing.JMenuItem helpMenuItem;
    protected javax.swing.JButton jButtonAddFile;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonFilter;
    private javax.swing.JButton jButtonNewProfile;
    protected javax.swing.JButton jButtonOpenProfile;
    protected javax.swing.JButton jButtonRemoveFilesAndFolder;
    private javax.swing.JButton jButtonReport;
    protected javax.swing.JButton jButtonSaveProfile;
    protected javax.swing.JButton jButtonStart;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JCheckBox jFilterOnCheckBox;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenuItem jMenuEditFilter;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuFilter;
    protected javax.swing.JMenuItem jMenuItemAddFileOrFolders;
    private javax.swing.JMenuItem jMenuItemCopyFilterToAll;
    private javax.swing.JMenuItem jMenuItemCopyToClipboard;
    private javax.swing.JMenuItem jMenuItemExpandChildren;
    private javax.swing.JMenuItem jMenuItemExpandNextThree;
    protected javax.swing.JMenuItem jMenuItemExport;
    protected javax.swing.JMenuItem jMenuItemNew;
    protected javax.swing.JMenuItem jMenuItemOpen;
    private javax.swing.JMenuItem jMenuItemOpenFolder;
    protected javax.swing.JMenuItem jMenuItemRemoveFolder;
    protected javax.swing.JMenuItem jMenuItemStart;
    protected javax.swing.JMenuItem jMenuItemStop;
    private javax.swing.JMenuItem jMenuQuit;
    private javax.swing.JMenu jMenuReport;
    private javax.swing.JMenu jMenuRun;
    protected javax.swing.JMenuItem jMenuSave;
    protected javax.swing.JMenuItem jMenuSaveAs;
    private javax.swing.JMenu jMenuTools;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jProfilesTabbedPane;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JMenu jhelp;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JMenuItem signatureInstallMenuItem;
    private javax.swing.JMenuItem updateNowMenuItem;
    // End of variables declaration//GEN-END:variables

    private void openProfileAction(ActionEvent event) {

        int result = profileFileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            final Path selectedFile = profileFileChooser.getSelectedFile().toPath();

            if (!droidContext.selectProfileWithSource(selectedFile)) {
                // Give the tab with this profile the focus...
                LoadProfileWorker worker = new LoadProfileWorker(profileManager, droidContext, jProfilesTabbedPane);
                worker.setProfileFile(selectedFile);
                worker.init(new ProfileForm(this, droidContext, buttonManager));
                worker.execute();
            }
        }
    }

    private void saveProfileAction(ActionEvent event) {
        final ProfileForm profileForm = droidContext.getSelectedProfile();
        profileForm.saveProfile(false);
    }

    private void saveProfileAsAction(ActionEvent event) {
        final ProfileForm profileForm = droidContext.getSelectedProfile();
        profileForm.saveProfile(true);
    }

    private void startProfile() {
        final ProfileForm profileForm = droidContext.getSelectedProfile();

        if (profileForm.getProfile().getFilter().isEnabled()) {
            profileForm.getProfile().getFilter().setEnabled(false);

            ApplyFilterToTreeTableAction refreshTreeTable = new ApplyFilterToTreeTableAction(profileForm,
                    this.profileManager);
            refreshTreeTable.applyFilter();
            updateFilterControls();
        }
        profileForm.start();
    }

    private void stopProfile() {
        final ProfileForm profileForm = droidContext.getSelectedProfile();
        profileForm.stop();
    }

    private void export() {
        ExportDialog exportOptions = new ExportDialog(this);
        if (globalContext.getGlobalConfig().getBooleanProperty(DroidGlobalProperty.CSV_EXPORT_ROW_PER_FORMAT)) {
            exportOptions.setExportOptions(ExportOptions.ONE_ROW_PER_FORMAT);
        } else {
            exportOptions.setExportOptions(ExportOptions.ONE_ROW_PER_FILE);
        }

        exportOptions.setDefaultTemplatesFolder(globalContext.getGlobalConfig().getExportTemplatesDir());
        exportOptions.showDialog();
        if (exportOptions.isApproved()) {
            String columnNames = exportOptions.getColumnsToExport();
            if (columnNames.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No columns for export were selected.",
                        "Export warning", JOptionPane.WARNING_MESSAGE);
            } else {
                exportFileChooser.setExportOutputOptions(exportOptions.getExportOutputOptions());
                int response = exportFileChooser.showSaveDialog(this);

                if (response == JFileChooser.APPROVE_OPTION) {
                    List<String> profileIds = new ArrayList<String>();
                    profileIds.addAll(exportOptions.getSelectedProfileIds());
                    //for (ProfileForm profileForm : droidContext.allProfiles()) {
                    //    profileIds.add(profileForm.getProfile().getUuid());
                    // }

                    final ExportAction exportAction = globalContext.getActionFactory().newExportAction();

                    final ExportProgressDialog exportDialog = new ExportProgressDialog(this, exportAction);

                    exportAction.setDestination(exportFileChooser.getSelectedFile());
                    exportAction.setProfileIds(profileIds);
                    exportAction.setExportOptions(exportOptions.getExportOptions());
                    exportAction.setExportOutputOptions(exportOptions.getExportOutputOptions());
                    exportAction.setOutputEncoding(exportOptions.getOutputEncoding());
                    exportAction.setBom(exportOptions.isBom());
                    exportAction.setQuoteAllFields(exportOptions.getQuoteAllColumns());
                    exportAction.setColumnsToWrite(columnNames);
                    exportAction.setExportTemplatePath(exportOptions.getTemplatePath());

                    exportAction.setCallback(action -> {
                        try {
                            exportDialog.setVisible(false);
                            exportDialog.dispose();
                            action.get();
                            JOptionPane.showMessageDialog(DroidMainFrame.this, "Export Complete.", "Export Complete",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (ExecutionException e) {
                            DialogUtils.showGeneralErrorDialog(DroidMainFrame.this, "Export Error", e.getCause()
                                    .getMessage());
                        } catch (InterruptedException e) {
                            DialogUtils.showGeneralErrorDialog(DroidMainFrame.this, "Export Interrupted", e.getCause()
                                    .getMessage());
                        } catch (CancellationException e) {
                            log.info("Export cancelled");
                        }
                    });

                    exportAction.execute();
                    exportDialog.setVisible(true);
                }
            }
        }
    }

    private void exit() {
        for (ExitListener exitListener : exitListeners) {
            exitListener.onExit();
        }

        StopRunningProfilesAction stopRunningAction = new StopRunningProfilesAction(profileManager, droidContext, this);
        if (stopRunningAction.execute()) {
            ProfileSelectionDialog dialog = new SaveAllProfilesDialog(this, droidContext.allDirtyProfiles());
            final ExitAction action = new ExitAction(droidContext, dialog, profileManager);
            action.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (STATE.equals(evt.getPropertyName()) && evt.getNewValue().equals(SwingWorker.StateValue.DONE)
                            && !action.isCancelled()) {
                        
                        
                        setVisible(false);
                        log.info("Closing DROID.");
                        final Path tempDir = globalContext.getGlobalConfig().getTempDir();
                        ResourceUtils.attemptToDeleteTempFiles(tempDir);
                        // CHECKSTYLE:OFF
                        System.exit(0);
                        // CHECKSTYLE:ON
                    }
                }
            });

            action.start();
        }
    }

    
    
    
    /**
     * 
     * @return the profile manager
     */
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    /**
     * @return the profile file chooser
     */
    public JFileChooser getProfileFileChooser() {
        return profileFileChooser;
    }

    /**
     * @return the globalContext
     */
    public GlobalContext getGlobalContext() {
        return globalContext;
    }

    /**
     * @return the droidContext
     */
    public DroidUIContext getDroidContext() {
        return droidContext;
    }

}
