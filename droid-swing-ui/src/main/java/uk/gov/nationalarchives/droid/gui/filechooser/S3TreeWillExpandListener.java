package uk.gov.nationalarchives.droid.gui.filechooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import java.awt.*;
import java.io.File;
import java.util.List;

public class S3TreeWillExpandListener implements TreeWillExpandListener {

    private final FileSystemView fileSystemView;

    private final Container parent;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public S3TreeWillExpandListener(FileSystemView fileSystemView, Container parent) {
        this.fileSystemView = fileSystemView;
        this.parent = parent;
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        node.removeAllChildren();
        File file = (File) node.getUserObject();
        try {
            File[] children = fileSystemView.getFiles(file, false);
            log.info("Found {} files", children.length);
            List<File> sortedChildren = ResourceDialogUtil.sortFiles(children);
            for (File f : sortedChildren) {
                if (f.isDirectory()) {
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(f, true);
                    node.add(child);
                }
            }
        } catch (S3Exception s3e) {
            log.error(s3e.getMessage(), s3e);
            JOptionPane.showMessageDialog(parent, "Credentials have expired. Please re-authenticate with AWS and try again.");

        }

    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}
}
