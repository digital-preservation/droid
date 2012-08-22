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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;

import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalProperty;
import uk.gov.nationalarchives.droid.gui.action.CloseProfileAction;
import uk.gov.nationalarchives.droid.gui.action.OpenContainingFolderAction;
import uk.gov.nationalarchives.droid.gui.action.SaveProfileWorker;
import uk.gov.nationalarchives.droid.gui.treemodel.TreeUtils;
import uk.gov.nationalarchives.droid.gui.treemodel.DefaultMutableTreeNodeComparator;
import uk.gov.nationalarchives.droid.gui.treemodel.DirectoryComparableLong;
import uk.gov.nationalarchives.droid.gui.treemodel.ExpandingTreeListener;
import uk.gov.nationalarchives.droid.gui.treemodel.NodeRenderer;
import uk.gov.nationalarchives.droid.gui.treemodel.OutlineColumn;
import uk.gov.nationalarchives.droid.gui.treemodel.OutlineComparableComparator;
import uk.gov.nationalarchives.droid.gui.treemodel.ProfileRowModel;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.FileChooserProxy;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.FileChooserProxyImpl;
import uk.gov.nationalarchives.droid.gui.widgetwrapper.JOptionPaneProxy;
import uk.gov.nationalarchives.droid.gui.worker.DroidJob;
import uk.gov.nationalarchives.droid.profile.ProfileEventListener;
import uk.gov.nationalarchives.droid.profile.ProfileInstance;
import uk.gov.nationalarchives.droid.profile.ProfileManager;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

/**
 * 
 * @author rflitcroft
 */
public class ProfileForm extends JPanel {

    /** */
    private static final int MAX_LEVELS_TO_EXPAND = 3;

    private static final long serialVersionUID = 1671584434169040994L;

    private DefaultTreeModel treeModel;
    private OutlineModel mdl;
    private ProfileInstance profile;
    private DroidMainFrame droidMainUi;
    private DroidUIContext context;
    private ProfileEventListener listener;
    private ProfileTabComponent profileTab;
    private DroidJob job;
    
    private MultiIdentificationDialog multiIdentificationDialog;

    private final String puidValuePrefix = "<html><a href=\"\">";
    private final String puidValueSuffix = "</a></html>";

    private Map<Long, DefaultMutableTreeNode> inMemoryNodes = new HashMap<Long, DefaultMutableTreeNode>();

    /**
     * 
     * @param droidMainUi
     *            the droid ui frame
     * @param context
     *            the droid ui context
     * @param listener
     *            a profile event listener
     */
    public ProfileForm(DroidMainFrame droidMainUi, DroidUIContext context, ProfileEventListener listener) {
        this.droidMainUi = droidMainUi;
        this.context = context;
        this.listener = listener;
        initComponents();
        // final NodeSelectionListener nodeRefreshListener =
        // new NodeSelectionListener(context, droidMainUi.getProfileManager());
        // getResultsOutline().getSelectionModel().addListSelectionListener(nodeRefreshListener);
        // getResultsOutline().addKeyListener(nodeRefreshListener);
        profileTab = new ProfileTabComponent(this);
        multiIdentificationDialog = new MultiIdentificationDialog(this);
        initOutline();
    }

    /**
     * 
     * @param droidMainUi
     *            the droidf main ui frame
     * @param context
     *            the droid ui context
     * @param profile
     *            a profile instance
     * @param listener
     *            a profile event listener
     */
    public ProfileForm(DroidMainFrame droidMainUi, DroidUIContext context, ProfileInstance profile,
            ProfileEventListener listener) {
        this(droidMainUi, context, listener);
        this.profile = profile;
    }

    //CHECKSTYLE:OFF too many statements
    private void initOutline() {
    //CHECKSTYLE:ON
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);
        treeModel = new DefaultTreeModel(root, true);
        mdl = DefaultOutlineModel.createOutlineModel(treeModel, new ProfileRowModel(), true, "Resource");
        final OutlineMouseAdapter mouseAdapter = new OutlineMouseAdapter();
        resultsOutline.addMouseListener(mouseAdapter);
        resultsOutline.addMouseMotionListener(mouseAdapter);
        resultsOutline.setVisible(true);
        //resultsOutline.setRenderDataProvider(new ProfileResultsRenderData());
        resultsOutline.setRootVisible(false);

        TreeWillExpandListener expandingTreeListener = new ExpandingTreeListener(droidMainUi.getProfileManager(), this);
        mdl.getTreePathSupport().addTreeWillExpandListener(expandingTreeListener);
        
        resultsOutline.setModel(mdl);
        TableColumnModel columnModel = resultsOutline.getColumnModel();

        ETableColumn nodeColumn0 = (ETableColumn) columnModel.getColumn(0);
        Color backColor = resultsOutline.getBackground();
        nodeColumn0.setNestedComparator(new DefaultMutableTreeNodeComparator(nodeColumn0));
        nodeColumn0.setCellRenderer(new NodeRenderer(backColor));
        resultsOutline.setDefaultRenderer(Object.class, new NodeRenderer(backColor));

        resultsOutline.setShowHorizontalLines(false);
        resultsOutline.setShowVerticalLines(true);
        resultsOutline.setGridColor(TreeUtils.getDarkerColor(backColor));
        
        OutlineColumn[] columns = OutlineColumn.values();
        for (int i = 0; i < columns.length; i++) {
            ETableColumn nodeColumn = (ETableColumn) columnModel.getColumn(i + 1);
            nodeColumn.setNestedComparator(new OutlineComparableComparator(nodeColumn));

            TableCellRenderer cellRenderer = OutlineColumn.values()[i].getRenderer(backColor);
            if (cellRenderer != null) {
                nodeColumn.setCellRenderer(cellRenderer);
            }
        } 
        
        // VITAL! We do not want to recreate columns after we have set them up
        // with their comparators!
        resultsOutline.setAutoCreateColumnsFromModel(false);
        
        // Sort ascending on first resource column by default:
        nodeColumn0 = (ETableColumn) columnModel.getColumn(0);
        int modelIndex = nodeColumn0.getModelIndex();
        resultsOutline.setColumnSorted(modelIndex, true, 1);
        
        
        //((DefaultTreeModel) treeModel).reload();

        jScrollPane1.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (resultsOutline.getPreferredSize().width <= jScrollPane1.getViewport().getExtentSize().width) {
                    resultsOutline.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                } else {
                    resultsOutline.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        PopupAddFilesAndFolders = new javax.swing.JMenuItem();
        PopupRemoveFilesOrFolders = new javax.swing.JMenuItem();
        PopupSeparator1 = new javax.swing.JSeparator();
        PopupOpenContainingFolder = new javax.swing.JMenuItem();
        PopupMenuCopyToClipboard = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        PopupMenuExpandChildren = new javax.swing.JMenuItem();
        PopupMenuExpandNextThree = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultsOutline = new org.netbeans.swing.outline.Outline();
        jPanel3 = new javax.swing.JPanel();
        statusProgressPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        statusProgressBar = new javax.swing.JProgressBar();
        throttlePanel = new javax.swing.JPanel();
        throttleSlider = new javax.swing.JSlider();
        throttleLabel = new javax.swing.JLabel();
        progressPanel = new javax.swing.JPanel();
        profileProgressBar = new javax.swing.JProgressBar();

        jPopupMenu1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jPopupMenu1PopupMenuWillBecomeVisible(evt);
            }
        });
        jPopupMenu1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPopupMenu1ComponentShown(evt);
            }
        });

        PopupAddFilesAndFolders.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Add small.png"))); // NOI18N
        PopupAddFilesAndFolders.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.PopupAddFilesAndFolders.text")); // NOI18N
        PopupAddFilesAndFolders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PopupAddFilesAndFoldersActionPerformed(evt);
            }
        });
        jPopupMenu1.add(PopupAddFilesAndFolders);

        PopupRemoveFilesOrFolders.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Remove small.png"))); // NOI18N
        PopupRemoveFilesOrFolders.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.PopupRemoveFilesOrFolders.text")); // NOI18N
        PopupRemoveFilesOrFolders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PopupRemoveFilesOrFoldersActionPerformed(evt);
            }
        });
        jPopupMenu1.add(PopupRemoveFilesOrFolders);
        jPopupMenu1.add(PopupSeparator1);

        PopupOpenContainingFolder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/icons/Icon_External_Link.png"))); // NOI18N
        PopupOpenContainingFolder.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.PopupOpenContainingFolder.text")); // NOI18N
        PopupOpenContainingFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PopupOpenContainingFolderActionPerformed(evt);
            }
        });
        jPopupMenu1.add(PopupOpenContainingFolder);

        PopupMenuCopyToClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/uk/gov/nationalarchives/droid/OldIcons/Clipboard small.png"))); // NOI18N
        PopupMenuCopyToClipboard.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.PopupMenuCopyToClipboard.text")); // NOI18N
        PopupMenuCopyToClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PopupMenuCopyToClipboardActionPerformed(evt);
            }
        });
        jPopupMenu1.add(PopupMenuCopyToClipboard);
        jPopupMenu1.add(jSeparator1);

        PopupMenuExpandChildren.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.PopupMenuExpandChildren.text")); // NOI18N
        PopupMenuExpandChildren.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PopupMenuExpandChildrenActionPerformed(evt);
            }
        });
        jPopupMenu1.add(PopupMenuExpandChildren);

        PopupMenuExpandNextThree.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.PopupMenuExpandNextThree.text")); // NOI18N
        PopupMenuExpandNextThree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PopupMenuExpandNextThreeActionPerformed(evt);
            }
        });
        jPopupMenu1.add(PopupMenuExpandNextThree);

        resultsOutline.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultsOutline.setFillsViewportHeight(true);
        resultsOutline.setSelectVisibleColumnsLabel(org.openide.util.NbBundle.getMessage(ProfileForm.class, "results.columns.select")); // NOI18N
        jScrollPane1.setViewportView(resultsOutline);

        statusLabel.setLabelFor(statusProgressBar);
        statusLabel.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.statusLabel.text")); // NOI18N

        javax.swing.GroupLayout statusProgressPanelLayout = new javax.swing.GroupLayout(statusProgressPanel);
        statusProgressPanel.setLayout(statusProgressPanelLayout);
        statusProgressPanelLayout.setHorizontalGroup(
            statusProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusProgressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusProgressPanelLayout.setVerticalGroup(
            statusProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusProgressPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusLabel))
                .addContainerGap())
        );

        throttlePanel.setVisible(false);

        throttleSlider.setMaximum(1000);
        throttleSlider.setMinorTickSpacing(100);
        throttleSlider.setPaintTicks(true);
        throttleSlider.setToolTipText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.throttleSlider.toolTipText")); // NOI18N
        throttleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                throttleSliderStateChanged(evt);
            }
        });

        throttleLabel.setText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.throttleLabel.text")); // NOI18N

        javax.swing.GroupLayout throttlePanelLayout = new javax.swing.GroupLayout(throttlePanel);
        throttlePanel.setLayout(throttlePanelLayout);
        throttlePanelLayout.setHorizontalGroup(
            throttlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(throttlePanelLayout.createSequentialGroup()
                .addComponent(throttleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(throttleSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addContainerGap())
        );
        throttlePanelLayout.setVerticalGroup(
            throttlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(throttlePanelLayout.createSequentialGroup()
                .addGroup(throttlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(throttleSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(throttlePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(throttleLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        profileProgressBar.setToolTipText(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.profileProgressBar.toolTipText")); // NOI18N
        profileProgressBar.setString(org.openide.util.NbBundle.getMessage(ProfileForm.class, "ProfileForm.profileProgressBar.string")); // NOI18N
        profileProgressBar.setStringPainted(true);

        javax.swing.GroupLayout progressPanelLayout = new javax.swing.GroupLayout(progressPanel);
        progressPanel.setLayout(progressPanelLayout);
        progressPanelLayout.setHorizontalGroup(
            progressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(profileProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE))
        );
        progressPanelLayout.setVerticalGroup(
            progressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(profileProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusProgressPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(throttlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(throttlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusProgressPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 826, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void PopupOpenContainingFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PopupOpenContainingFolderActionPerformed
        openSelectedFolders();
    }//GEN-LAST:event_PopupOpenContainingFolderActionPerformed

    private void PopupAddFilesAndFoldersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PopupAddFilesAndFoldersActionPerformed
        droidMainUi.jButtonAddFileActionPerformed(evt);
    }//GEN-LAST:event_PopupAddFilesAndFoldersActionPerformed

    private void PopupRemoveFilesOrFoldersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PopupRemoveFilesOrFoldersActionPerformed
        droidMainUi.jButtonRemoveFilesAndFolderActionPerformed(evt);
    }//GEN-LAST:event_PopupRemoveFilesOrFoldersActionPerformed

    private void jPopupMenu1ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPopupMenu1ComponentShown
        PopupAddFilesAndFolders.setEnabled(droidMainUi.getAddEnabled());
        PopupRemoveFilesOrFolders.setEnabled(droidMainUi.getRemoveEnabled());
    }//GEN-LAST:event_jPopupMenu1ComponentShown

    private void jPopupMenu1PopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenu1PopupMenuWillBecomeVisible
        PopupAddFilesAndFolders.setEnabled(droidMainUi.getAddEnabled());
        PopupRemoveFilesOrFolders.setEnabled(droidMainUi.getRemoveEnabled());
        PopupOpenContainingFolder.setEnabled(anyRowsSelected());
        PopupMenuCopyToClipboard.setEnabled(anyRowsSelected());
    }//GEN-LAST:event_jPopupMenu1PopupMenuWillBecomeVisible

    private void PopupMenuCopyToClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PopupMenuCopyToClipboardActionPerformed
        copySelectedToClipboard();
    }//GEN-LAST:event_PopupMenuCopyToClipboardActionPerformed

    private void PopupMenuExpandChildrenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PopupMenuExpandChildrenActionPerformed
       expandSelectedNodes(false);
    }//GEN-LAST:event_PopupMenuExpandChildrenActionPerformed

    private void PopupMenuExpandNextThreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PopupMenuExpandNextThreeActionPerformed
        expandSelectedNodes(true);
    }//GEN-LAST:event_PopupMenuExpandNextThreeActionPerformed

    private void throttleSliderStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_throttleSliderStateChanged
        throttleLabel.setText(String.format("Throttle: %s ms", throttleSlider.getValue()));
        context.getProfileManager().setThrottleValue(profile.getUuid(), throttleSlider.getValue());

    }// GEN-LAST:event_throttleSliderStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem PopupAddFilesAndFolders;
    private javax.swing.JMenuItem PopupMenuCopyToClipboard;
    private javax.swing.JMenuItem PopupMenuExpandChildren;
    private javax.swing.JMenuItem PopupMenuExpandNextThree;
    private javax.swing.JMenuItem PopupOpenContainingFolder;
    private javax.swing.JMenuItem PopupRemoveFilesOrFolders;
    private javax.swing.JSeparator PopupSeparator1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JProgressBar profileProgressBar;
    private javax.swing.JPanel progressPanel;
    private org.netbeans.swing.outline.Outline resultsOutline;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JProgressBar statusProgressBar;
    private javax.swing.JPanel statusProgressPanel;
    private javax.swing.JLabel throttleLabel;
    private javax.swing.JPanel throttlePanel;
    private javax.swing.JSlider throttleSlider;
    // End of variables declaration//GEN-END:variables


    /**
     * Copies selected rows to the system clipboard.
     */
    public void copySelectedToClipboard() {
        final TransferHandler handler = resultsOutline.getTransferHandler();
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Clipboard clipboard = tk.getSystemClipboard();
        handler.exportToClipboard(resultsOutline, clipboard, TransferHandler.COPY);
    }
    
    /**
     * @return the results outline
     */
    public Outline getResultsOutline() {
        return resultsOutline;
    }

    /**
     * 
     * @return the progress bar
     */
    public JProgressBar getProfileProgressBar() {
        return profileProgressBar;
    }

    /**
     * 
     * @return The status progress bar
     */
    public JProgressBar getStatusProgressBar() {
        return statusProgressBar;
    }

    /**
     * 
     * @return the status label
     */
    public JLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * @return the treeModel
     */
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * @param profile
     *            the profile to set
     */
    public void setProfile(ProfileInstance profile) {
        this.profile = profile;
        profile.addEventListener(listener);
        listener.fireEvent(profile);
    }

    /**
     * @return the profile
     */
    public ProfileInstance getProfile() {
        return profile;
    }

    /**
     * Closes a profile.
     */
    public void closeProfile() {
        CloseProfileAction closeAction = new CloseProfileAction(droidMainUi.getProfileManager(), context, this);
        JOptionPaneProxy dialog = new JOptionPaneProxy() {
            @Override
            public int getResponse() {
                int result = JOptionPane.showConfirmDialog(ProfileForm.this, "Save this profile?", "Warning",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                int response = JOptionPaneProxy.CANCEL;
                if (result == JOptionPane.YES_OPTION) {
                    response = JOptionPaneProxy.YES;
                } else if (result == JOptionPane.NO_OPTION) {
                    response = JOptionPaneProxy.NO;
                }

                return response;
            }
        };

        closeAction.setUserOptionDialog(dialog);
        final JFileChooser fileChooser = context.getProfileFileChooser();
        FileChooserProxy chooserProxy = new FileChooserProxyImpl(this, fileChooser);
        closeAction.setSaveAction(new SaveProfileWorker(droidMainUi.getProfileManager(), this, chooserProxy));
        closeAction.start();
    }

    /**
     * Saves a profile.
     * 
     * @param saveAs
     *            whether to show a file chooser dialog
     */
    public void saveProfile(boolean saveAs) {
        final JFileChooser fileChooser = context.getProfileFileChooser();
        fileChooser.setDialogTitle(String.format("Save profile '%s'", getName()));
        FileChooserProxy dialog = new FileChooserProxyImpl(this, fileChooser);
        File loadedFrom = getProfile().getLoadedFrom();
        fileChooser.setSelectedFile(loadedFrom != null ? loadedFrom : new File(getName()));

        SaveProfileWorker worker = new SaveProfileWorker(droidMainUi.getProfileManager(), this, dialog);
        worker.start(saveAs);
    }

    /**
     * Updates widgets before a save operation.
     */
    public void beforeSave() {
        statusLabel.setText("Saving profile...");
        statusProgressBar.setValue(0);
        statusProgressBar.setIndeterminate(false);
        statusLabel.setVisible(true);
        statusProgressBar.setVisible(true);
    }
    
    
    private void openSelectedFolders() {
        OpenContainingFolderAction openAction = new OpenContainingFolderAction();
        openAction.open(getSelectedNodes());
    }

    /**
     * Updates widgets after a save operation.
     */
    public void afterSave() {
        statusLabel.setVisible(false);
        statusProgressBar.setVisible(false);
    }

    /**
     * Updates state after loading.
     */
    public void afterLoad() {
        throttleSlider.setValue(profile.getThrottle());
        throttlePanel.setVisible(true);
        getStatusProgressBar().setVisible(false);
        getStatusLabel().setVisible(false);
        listener.fireEvent(getProfile());
        droidMainUi.updateFilterControls();
    }

    /**
     * Updates state after creating new profile.
     */
    public void afterCreate() {
        throttleSlider.setValue(profile.getThrottle());
        throttlePanel.setVisible(true);
    }

    /**
     * ] Sets the state change listener.
     * 
     * @param stateChangeListener
     *            the listener to set.
     */
    public void setStateChangeListener(ProfileEventListener stateChangeListener) {
        this.listener = stateChangeListener;
    }

    /**
     * Starts a profile.
     */
    public void start() {
        ProfileManager profileManager = droidMainUi.getProfileManager();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        inMemoryNodes.put(-1L, rootNode);

        job = new DroidJob();
        job.setProfileManager(profileManager);
        job.setProfileForm(this);
        job.start();
    }

    /**
     * Stops a profile.
     */
    public void stop() {
        ProfileManager profileManager = droidMainUi.getProfileManager();
        profileManager.stop(getProfile().getUuid());
        job.cancel(true);
    }

    /**
     * @return the profileTab
     */
    public ProfileTabComponent getProfileTab() {
        return profileTab;
    }

    /**
     * @return the inMemoryNodes
     */
    public Map<Long, DefaultMutableTreeNode> getInMemoryNodes() {
        return inMemoryNodes;
    }

    /**
     * @return the throttleSlider
     */
    public JSlider getThrottleSlider() {
        return throttleSlider;
    }

    /**
     * @return the throttleLabel
     */
    JLabel getThrottleLabel() {
        return throttleLabel;
    }

    /**
     * @return the listener
     */
    public ProfileEventListener getListener() {
        return listener;
    }

    /**
     * @return the progressPanel
     */
    public JPanel getProgressPanel() {
        return progressPanel;
    }
    
    /**
     * 
     * @return the throttlePanel.
     */
    public JPanel getThrottlePanel() {
        return throttlePanel;
    }
    
    /**
     * @return the droidMainUi
     */
    DroidMainFrame getDroidMainUi() {
        return droidMainUi;
    }

    /**
     * 
     * @return A list of the profile resource nodes selected in the outline.
     */
    public List<ProfileResourceNode> getSelectedNodes() {
        List<ProfileResourceNode> results = new ArrayList<ProfileResourceNode>();
        Outline outline = getResultsOutline();
        int[] selectedRows = outline.getSelectedRows();
        for (int i = selectedRows.length; i > 0; i--) {
            int index = selectedRows[i - 1];
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) outline
                    .getValueAt(index, 0);
            ProfileResourceNode prn = (ProfileResourceNode) treeNode
                    .getUserObject();
            results.add(prn);
        }
        return results;
    }
    
    /**
     * 
     * @return whether any rows are selected in the profile.
     */
    public boolean anyRowsSelected() {
        Outline outline = getResultsOutline();
        return outline.getSelectedRows().length > 0;
    }
    
    
    /**
     * Expands the selected nodes in the tree.
     * @param recursive whether to expand all children.
     */
    public void expandSelectedNodes(boolean recursive) {
        Outline outline = getResultsOutline();
        int[] selectedRows = outline.getSelectedRows();
        for (int i = selectedRows.length; i > 0; i--) {
            int index = selectedRows[i - 1];
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) outline
                    .getValueAt(index, 0);
            expandNode(treeNode, recursive, 1);
        }
    }
    
    
    private void expandNode(DefaultMutableTreeNode treeNode, boolean recursive, int level) {
        if (treeNode.getAllowsChildren()) {
            if (treeNode.getChildCount() == 0) {
                TreePath path = new TreePath(treeNode.getPath());
                mdl.getTreePathSupport().expandPath(path);
            }
            if (recursive && level <= MAX_LEVELS_TO_EXPAND) {
                for (int childIndex = 0; childIndex < treeNode.getChildCount(); childIndex++) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(childIndex);
                    expandNode(child, recursive, level + 1);
                }
            }
        }
    }
    
    /**
     * Generates a URL to an external page from a PUID. 
     * @param puid the puid
     * @return URL to a resource
     */
    String getPronumURLPrefix(String puid) {
        // get it from configuration.
        String puidUrl = droidMainUi.getGlobalContext().getGlobalConfig().getProperties().getString(
                DroidGlobalProperty.PUID_URL_PATTERN.getName());
        return String.format(puidUrl, puid);
    }

    private class OutlineMouseAdapter extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {

            if (e.getButton() == MouseEvent.BUTTON3) {
                Point mousePoint = e.getPoint();
                int rowIndex = resultsOutline.rowAtPoint(mousePoint);
                if (rowIndex > -1 && !resultsOutline.isRowSelected(rowIndex)) {
                    resultsOutline.setRowSelectionInterval(rowIndex, rowIndex);
                }
                jPopupMenu1.show(resultsOutline, e.getX(), e.getY());
            } else {
                Point mousePoint = e.getPoint();
                int colIndex = resultsOutline.columnAtPoint(mousePoint);
                int rowIndex = resultsOutline.rowAtPoint(mousePoint);
                int colModelIndex = resultsOutline.convertColumnIndexToModel(resultsOutline.columnAtPoint(mousePoint));
    
                if (colModelIndex == OutlineColumn.PUID.ordinal() + 1) {
                    Object cellObj = resultsOutline.getValueAt(rowIndex, colIndex);
                    if (cellObj != null) {
                        String cellValue = cellObj.toString();
                        cellValue = cellValue.replace(puidValuePrefix, "");
                        cellValue = cellValue.replace(puidValueSuffix, "");
                        cellValue.trim();
                        if (cellValue.startsWith("\"")) {
                            String[] puids = StringUtils.split(cellValue, ",");
                            for (String puid : puids) {
                                String unquotedPuid = StringUtils.strip(puid, "\" ");
                                openURL(getPronumURLPrefix(unquotedPuid));
                            }
                        } else {
                            if (cellValue.length() > 0) {
                                openURL(getPronumURLPrefix(cellValue));
                            }
                        }
                    }
                }
            }
        }
        
           
        public void openURL(String puidUrl) {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        URL url = new URL(puidUrl);
                        desktop.browse(url.toURI());
                    } catch (MalformedURLException e1) {
                        DialogUtils
                                .showGeneralErrorDialog(droidMainUi, "MalformedURLException", "Invalid URL.");
                    } catch (IOException e1) {
                        DialogUtils.showGeneralErrorDialog(droidMainUi, "IOException", "Resource not found.");
                    } catch (URISyntaxException uriSyntaxEx) {
                        DialogUtils.showGeneralErrorDialog(droidMainUi, "URISyntaxException", "Invalid URI.");
                    }
                }
            }
        }
        
        
        @Override
        public void mouseMoved(MouseEvent e) {
            
            final Point mousePoint = e.getPoint();
            final int colIndex = resultsOutline.columnAtPoint(mousePoint);
            final int rowIndex = resultsOutline.rowAtPoint(mousePoint);
            final int colModelIndex = resultsOutline.convertColumnIndexToModel(
                    resultsOutline.columnAtPoint(mousePoint));
            final Object cellObject = resultsOutline.getValueAt(rowIndex, colIndex);
            if (cellObject != null) {
                String cellValue = cellObject.toString();
                resultsOutline.setToolTipText(cellValue);
                
                if (colModelIndex == 0) {
                    ProfileResourceNode resourceNode = (ProfileResourceNode) ((DefaultMutableTreeNode) 
                            cellObject).getUserObject();
                    resultsOutline.setToolTipText(java.net.URLDecoder.decode(resourceNode.getUri().toString()));
                }
                
                if (colModelIndex == OutlineColumn.PUID.ordinal() + 1) {
                    cellValue = resultsOutline.getValueAt(rowIndex, colIndex).toString();
                    cellValue = cellValue.replace(puidValuePrefix, "");
                    cellValue = cellValue.replace(puidValueSuffix, "");
                    cellValue.trim();
                    if (cellValue.length() > 0) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                } else if (colModelIndex == OutlineColumn.IDENTIFICATION_COUNT.ordinal() + 1) {
                    DirectoryComparableLong value = (DirectoryComparableLong) 
                        resultsOutline.getValueAt(resultsOutline.rowAtPoint(e.getPoint()),
                            resultsOutline.columnAtPoint(e.getPoint()));
                    if (value.getSource() != null && value.getSource() > 1) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                
                repaint();
            }
        }
        
        /**
         * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            Point mousePoint = e.getPoint();
            int colIndex = resultsOutline.columnAtPoint(mousePoint);
            int rowIndex = resultsOutline.rowAtPoint(mousePoint);
            int colModelIndex = resultsOutline.convertColumnIndexToModel(resultsOutline.columnAtPoint(mousePoint));

            if (colModelIndex == OutlineColumn.IDENTIFICATION_COUNT.ordinal() + 1) {
                DirectoryComparableLong count = (DirectoryComparableLong) resultsOutline
                    .getValueAt(rowIndex, colIndex);
                if (count != null && count.getSource() != null && count.getSource() > 1) {
                    int rowModelIndex = resultsOutline.convertRowIndexToModel(rowIndex);
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) mdl.getValueAt(rowModelIndex, 0);
                    ProfileResourceNode node = (ProfileResourceNode) treeNode.getUserObject();
                    multiIdentificationDialog.showDialog(node);
                }
            }
        }
    }

}
