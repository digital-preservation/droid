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
package uk.gov.nationalarchives.droid.gui.filechooser;

import org.openide.util.NbBundle;
import uk.gov.nationalarchives.droid.core.interfaces.http.S3ClientFactory;
import uk.gov.nationalarchives.droid.util.FileUtil;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.Serial;
import java.text.DateFormat;
import java.util.List;
import java.util.*;

/**
 *
 * @author rflitcroft
 */
public class S3ResourceSelectorDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = -6826324560370115692L;

    private static final char QUOTE = '"';

    private final FileSystemView fsv;

    private final List<File> selectedFiles = new ArrayList<>();

    private int response = JFileChooser.CANCEL_OPTION;

    /**
     * Creates new form ResourceSelector.
     *
     * @param parent          parent window
     * @param s3ClientFactory a factory to create an S3 client
     */
    public S3ResourceSelectorDialog(Window parent, S3ClientFactory s3ClientFactory) {
        super(parent);
        fsv = new S3FileSystemView(s3ClientFactory.getS3Client());
        initComponents();
        initTree();
        initTable();
        pack();
    }

    /**
     * Shows the file chooser Dialog.
     * @param parent the dialog's parent
     * @return the response.
     */
    public int showDialog(Component parent) {
        setLocationRelativeTo(parent);
        response = JFileChooser.CANCEL_OPTION;
        setVisible(true);
        return response;
    }

    /**
     * @return the response
     */
    public int getResponse() {
        return response;
    }


    /**
     * @return the selectedFiles
     */
    public List<File> getSelectedFiles() {
        return selectedFiles;
    }

    private DefaultTreeModel getTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new VirtualFile("Virtual File System"), true);
        return new DefaultTreeModel(rootNode, true);

    }

    private void initTree() {
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();

        File[] roots = fsv.getRoots();
        for (File rootFile : roots) {
            boolean rootAllowsChildren = rootFile.isDirectory();
            final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(rootFile, rootAllowsChildren);
            if (rootAllowsChildren) {
                rootNode.add(newChild);
            }
        }

        treeModel.nodeStructureChanged(rootNode);
        tree.addTreeWillExpandListener(new S3TreeWillExpandListener(fsv, getParent()));
        tree.addTreeSelectionListener(new S3TreeSelectionListener(fsv, table, tree, selectedFiles, selectedFilesTextBox));
    }


    private void initTable() {
        table.setDefaultRenderer(VirtualFile.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                VirtualFile f = (VirtualFile) value;
                setIcon(fsv.getSystemIcon(f));
                setText(fsv.getSystemDisplayName(f));
            }
        });

        table.setDefaultRenderer(Long.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(value == null ? "" : FileUtil.formatFileSize((Long) value, 1));
            }
        });

        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value == null ? "" : DateFormat.getDateTimeInstance().format(value));
            }
        });

        table.addMouseListener(new DoubleClickMouseAdapter());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedRows = table.getSelectedRows();

                selectedFiles.clear();
                for (int i : selectedRows) {
                    selectedFiles.add((VirtualFile) table.getValueAt(i, 0));
                }
                selectedFilesTextBox.setText(toText(selectedFiles));
            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && table.getSelectedColumnCount() == 1) {
                    exploreTableFolder(table.getSelectedRow());
                }
            }
        });

    }

    private String toText(List<File> files) {
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            sb.append(QUOTE).append(fsv.getSystemDisplayName(f)).append(QUOTE).append(' ');
        }
        return sb.toString();
    }

    private class DoubleClickMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                int clickedRow = table.rowAtPoint(event.getPoint());
                if (clickedRow != -1) {
                    exploreTableFolder(clickedRow);
                }
            }
        }
    }

    private void exploreTableFolder(int rowIndex) {
        VirtualFile f = (VirtualFile) table.getValueAt(rowIndex, 0);
        if (f.isDirectory()) {
            final TreePath selectionPath = tree.getSelectionPath();
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)
                selectionPath.getLastPathComponent();
            for (Enumeration<TreeNode> e = treeNode.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
                if (n.getUserObject().equals(f)) {
                    final TreePath path = new TreePath(n.getPath());
                    tree.expandPath(path);
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                    break;
                }
            }
        }
    }

    private class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = -971847725978939749L;

        @Override
        public Component getTreeCellRendererComponent(JTree parentTree, Object value,
                boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            VirtualFile f = (VirtualFile) ((DefaultMutableTreeNode) value).getUserObject();
            if (f.exists()) {
                setIcon(fsv.getSystemIcon(f));
                final String systemDisplayName = fsv.getSystemDisplayName(f);
                setText(systemDisplayName.isEmpty() ? f.getPath() : systemDisplayName);
                if (sel) {
                    setForeground(getTextSelectionColor());
                } else {
                    setForeground(getTextNonSelectionColor());
                }
                selected = sel;
                return this;
            }
            return super.getTreeCellRendererComponent(parentTree, value, sel, expanded, leaf, row, hasFocus);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        jPanel1 = new JPanel();
        jSplitPane1 = new JSplitPane();
        jScrollPane2 = new JScrollPane();
        table = new JTable();
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        jLabel1 = new JLabel();
        jScrollPane1 = new JScrollPane();
        tree = new JTree();
        jPanel4 = new JPanel();
        selectedFilesTextBox = new JTextField();
        subfoldersCheckBox = new JCheckBox();
        jLabel2 = new JLabel();
        okButton = new JButton();
        cancelButton = new JButton();

        setTitle(NbBundle.getMessage(S3ResourceSelectorDialog.class, "ResourceSelectorDialog.title")); // NOI18N
        jSplitPane1.setDividerLocation(200);

        table.setModel(new DefaultTableModel());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(4, 1));
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        jScrollPane2.setViewportView(table);

        jSplitPane1.setRightComponent(jScrollPane2);


        jPanel3.setBorder(new SoftBevelBorder(BevelBorder.RAISED));

        jLabel1.setText(NbBundle.getMessage(S3ResourceSelectorDialog.class, "ResourceSelector.jLabel1.text")); // NOI18N
        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                .addContainerGap())
        );

        tree.setModel(getTreeModel());
        tree.setLargeModel(true);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        jScrollPane1.setViewportView(tree);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane1))
        );

        jSplitPane1.setLeftComponent(jPanel2);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addContainerGap())
        );

        selectedFilesTextBox.setEditable(false);

        selectedFilesTextBox.setText(NbBundle.getMessage(S3ResourceSelectorDialog.class, "ResourceSelector.jTextField1.text")); // NOI18N
        subfoldersCheckBox.setSelected(true);


        subfoldersCheckBox.setText(NbBundle.getMessage(S3ResourceSelectorDialog.class, "ResourceSelector.jCheckBox1.text")); // NOI18N
        jLabel2.setText(NbBundle.getMessage(S3ResourceSelectorDialog.class, "ResourceSelector.jLabel2.text")); // NOI18N
        okButton.setText(NbBundle.getMessage(S3ResourceSelectorDialog.class, "ResourceSelector.okButton.text")); // NOI18N
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(NbBundle.getMessage(S3ResourceSelectorDialog.class, "ResourceSelector.cancelButton.text")); // NOI18N
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
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(subfoldersCheckBox, GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                    .addComponent(selectedFilesTextBox, GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(cancelButton)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, okButton});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel2))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(Alignment.BASELINE)
                            .addComponent(okButton)
                            .addComponent(selectedFilesTextBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(cancelButton)
                    .addComponent(subfoldersCheckBox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, okButton});

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jPanel1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        response = JFileChooser.APPROVE_OPTION;
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton cancelButton;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JSplitPane jSplitPane1;
    private JButton okButton;
    private JTextField selectedFilesTextBox;
    private JCheckBox subfoldersCheckBox;
    private JTable table;
    private JTree tree;
    // End of variables declaration//GEN-END:variables

}
