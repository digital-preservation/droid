package uk.gov.nationalarchives.droid.gui.filechooser;

import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.Serial;
import java.util.Date;
import java.util.List;

public class S3TreeSelectionListener implements TreeSelectionListener {

    private static final int FILE_COLUMN_INDEX = 0;
    private static final int SIZE_COLUMN_INDEX = 1;
    private static final int DATE_COLUMN_INDEX = 2;

    private static final int WIDE_COLUMN = 200;
    private static final int NARROW_COLUMN = 100;

    private static final char QUOTE = '"';

    private static final Object[] COLUMN_NAMES = new Object[] {
            "Name", "Size", "Last modified", };


    private static final Class<?>[] TYPES = new Class [] {
            File.class, Long.class, Date.class, };


    private final FileSystemView fileSystemView;
    private final JTable table;
    private final JTree tree;
    private final List<File> selectedFiles;
    private final JTextField selectedFilesTextBox;

    public S3TreeSelectionListener(final FileSystemView fileSystemView, final JTable table, final JTree tree, List<File> selectedFiles, JTextField selectedFilesTextBox) {
        this.fileSystemView = fileSystemView;
        this.table = table;
        this.tree = tree;
        this.selectedFiles = selectedFiles;
        this.selectedFilesTextBox = selectedFilesTextBox;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreePath treePath = e.getNewLeadSelectionPath();
        if (treePath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            File f = (File) node.getUserObject();
            final File[] listFiles = fileSystemView.getFiles(f, false);
            if (listFiles != null) {
                table.setModel(updateTable(listFiles));
                initColumnModel();
                tree.expandPath(treePath);

            } else {
                table.setModel(new DefaultTableModel());
            }

            selectedFiles.clear();
            selectedFiles.add(f);
            selectedFilesTextBox.setText(QUOTE + fileSystemView.getSystemDisplayName(f) + QUOTE);

        }
    }

    private TableModel updateTable(File[] contents) {


        DefaultTableModel tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {

            @Serial
            private static final long serialVersionUID = 7704239139807661229L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return TYPES[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (File f : contents) {
            tableModel.addRow(new Object[] {
                    f,
                    f.isDirectory() ? null : f.length(),
                    f.lastModified() == 0 ? null : new Date(f.lastModified()),
            });
        }



        return tableModel;
    }

    private void initColumnModel() {
        final TableColumnModel columnModel = table.getColumnModel();
        final TableColumn fileColumn = columnModel.getColumn(FILE_COLUMN_INDEX);

        fileColumn.setPreferredWidth(WIDE_COLUMN);
        fileColumn.setHeaderValue(
                NbBundle.getMessage(
                        S3ResourceSelectorDialog.class, "ResourceSelector.table.columnModel.title0")); // NOI18N

        columnModel.getColumn(SIZE_COLUMN_INDEX).setPreferredWidth(NARROW_COLUMN);
        columnModel.getColumn(SIZE_COLUMN_INDEX).setHeaderValue(
                NbBundle.getMessage(
                        S3ResourceSelectorDialog.class, "ResourceSelector.table.columnModel.title1")); // NOI18N

        columnModel.getColumn(DATE_COLUMN_INDEX).setPreferredWidth(WIDE_COLUMN);
        columnModel.getColumn(DATE_COLUMN_INDEX).setHeaderValue(
                NbBundle.getMessage(S3ResourceSelectorDialog.class,
                        "ResourceSelector.table.columnModel.title3")); // NOI18N
    }
}
