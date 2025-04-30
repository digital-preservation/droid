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
package uk.gov.nationalarchives.droid.gui.ui;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.exception.ComponentLookupException;
import org.assertj.swing.exception.WaitTimedOutError;
import org.assertj.swing.fixture.*;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Timeout;
import org.netbeans.swing.outline.Outline;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.gov.nationalarchives.droid.core.interfaces.config.DroidGlobalConfig;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;
import uk.gov.nationalarchives.droid.gui.ProfileForm;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.swing.core.Robot;

import static org.assertj.swing.timing.Pause.pause;
import static org.junit.Assert.assertEquals;

public class SwingUiTestUtils {
    
    final FrameFixture frame;
    final Robot robot;
    
    public SwingUiTestUtils(FrameFixture frame, Robot robot) {
        this.frame = frame;
        this.robot = robot;
    }

    static final GenericTypeMatcher<JLabel> progressBarMatcher = new GenericTypeMatcher<>(JLabel.class) {
        @Override
        protected boolean isMatching(JLabel component) {
            return "Initialising profile...".equals(component.getText());
        }
    };

    static final GenericTypeMatcher<JLabel> saveProfileMatcher = new GenericTypeMatcher<>(JLabel.class) {
        @Override
        protected boolean isMatching(JLabel component) {
            return "Saving profile...".equals(component.getText());
        }
    };

    void waitForProgressLabelInvisible(JLabelFixture labelFixture) {

        Condition condition = new Condition("Wait for invisible initialising progress bar label") {
            public boolean test() {
                return !labelFixture.target().isVisible();
            }
        };
        pause(condition, Timeout.timeout(60000));
    }

    void waitForProfileProgressBarRemovedOrCompleted() {
        Condition condition = new Condition("Wait for removal of profile progress bar") {
            public boolean test() {
                try {
                    JProgressBarFixture progressBarFixture = frame.progressBar("profileProgress");
                    return progressBarFixture.target().getValue() == 100;
                } catch (ComponentLookupException ignored) {
                    return true;
                }
            }
        };
        pause(condition, Timeout.timeout(60000));
    }

    List<JPanel> getProfilePanels() {
        return robot.finder().findAll(new GenericTypeMatcher<>(JPanel.class) {
            @Override
            protected boolean isMatching(JPanel jpanel) {
                return jpanel.getName() != null && jpanel.getName().startsWith("Untitled-");
            }
        }).stream().toList();
    }

    static void setAutoUpdateFalse() {
        try {
            RuntimeConfig.configureRuntimeEnvironment();
            DroidGlobalConfig config = new DroidGlobalConfig();
            config.init();
            config.update(Map.of("update.autoCheck", false));

        } catch (IOException | ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    void runProfile() {
        frame.button("Add File").click();
        String path = "/home/test/test-subfolders";
        selectFolder(path);
        frame.button("Start").click();
        waitForProfileProgressBarRemovedOrCompleted();
    }

    void generateReport(String reportName) {
        frame.button("Report").click();
        DialogFixture reportDialog = findVisibleDialog();
        JComboBoxFixture reportSelectCombo = reportDialog.comboBox("Report select combo");
        reportSelectCombo.selectItem(reportName);
        JTableFixture profileSelectTable = reportDialog.table("Profile select table");
        JTableCellFixture firstProfile = profileSelectTable.cell(TableCell.row(0).column(0));
        firstProfile.click();
        reportDialog.button("Report dialog generate").click();
        try {
            frame.progressBar("Generate report progress bar").waitUntilValueIs(90);
        } catch (ComponentLookupException | WaitTimedOutError ignored) {}

        robot.waitForIdle();
    }

    Path exportReport(String exportType, String suffix) {
        JButton exportButton = robot.finder().findByName("Report export button", JButton.class);
        JButtonFixture exportButtonFixture = new JButtonFixture(robot, exportButton);
        exportButtonFixture.click();
        JFileChooserFixture fileChooser = new JFileChooserFixture(robot, robot.finder().findByName("Report export file chooser", JFileChooser.class));
        JComboBoxFixture exportTypeCombo = new JComboBoxFixture(robot, fileChooser.robot().finder().find(new GenericTypeMatcher<>(JComboBox.class) {
            @Override
            protected boolean isMatching(JComboBox component) {
                return component.getName() == null && component.getSelectedItem() != null && component.getSelectedItem().getClass().getPackageName().equals("uk.gov.nationalarchives.droid.gui.report");
            }
        }));
        exportTypeCombo.selectItem(exportType);
        String fileName = UUID.randomUUID().toString().split("-")[0];
        Path profilePath = Path.of("/home/profiles");
        fileChooser.setCurrentDirectory(profilePath.toFile());
        fileChooser.fileNameTextBox().enterText(fileName);
        fileChooser.approve();
        return profilePath.resolve(fileName + "." + suffix);
    }

    static Document getDocument(Path filePath) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(filePath.toFile());
    }

    void selectMenuItem(String firstItem, String secondItem) {
        JMenuItemFixture firstItemFixture = frame.menuItem(firstItem);
        firstItemFixture.requireVisible();
        firstItemFixture.click();
        JMenuItemFixture secondItemFixture = frame.menuItem(secondItem);
        secondItemFixture.requireVisible();
        secondItemFixture.click();
    }

    JFileChooserFixture getFileChooser() {
        DialogFixture visibleDialog = findVisibleDialog();
        return visibleDialog.fileChooser("Profile File Chooser");
    }

    List<ResultRow> getResultRows() {
        return getResultRows(true, List.of("/home/test"));
    }

    List<ResultRow> getResultRows(boolean waitForProgressBar, List<String> cellsToClick) {
        if (waitForProgressBar) {
            waitForProfileProgressBarRemovedOrCompleted();
        }
        JTableFixture outlineFixture = getResultsTableFixture();
        cellsToClick.forEach(cellName -> outlineFixture.cell(cellName).doubleClick());

        int rowCount = outlineFixture.rowCount();
        List<ResultRow> resultRows = new ArrayList<>();
        for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
            JTable target = outlineFixture.target();
            String name = target.getValueAt(rowIdx, 0).toString();
            var resultRow = new ResultRow(
                    name,
                    target.getValueAt(rowIdx, 1).toString(),
                    target.getValueAt(rowIdx, 2).toString(),
                    target.getValueAt(rowIdx, 3).toString(),
                    target.getValueAt(rowIdx, 4).toString(),
                    target.getValueAt(rowIdx, 5).toString(),
                    target.getValueAt(rowIdx, 6).toString(),
                    target.getValueAt(rowIdx, 7).toString(),
                    target.getValueAt(rowIdx, 8).toString(),
                    target.getValueAt(rowIdx, 9).toString()
            );
            resultRows.add(resultRow);
        }
        return resultRows;
    }

    JTableFixture getResultsTableFixture() {
        List<JPanel> profilePanels = getProfilePanels();
        assertEquals("Only a single profile tab should show", 1, profilePanels.size());
        ProfileForm profilePanel = (ProfileForm) profilePanels.getFirst();
        JPanelFixture panel = frame.panel(profilePanel.getName());
        Outline outline = panel.robot().finder().findByType(panel.target(), Outline.class, true);
        JTableFixture outlineFixture = new JTableFixture(robot, outline);
        outlineFixture.requireVisible();
        return outlineFixture;
    }

    record ResultRow(String resource, String extension, String size, String lastModified, String ids, String format,
                     String version, String mimeType, String puid, String identificationType) {
    }

    static GenericTypeMatcher<JTree> matchTreeByFirstChild() {
        return new GenericTypeMatcher<>(JTree.class) {
            @Override
            protected boolean isMatching(JTree component) {
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) component.getModel().getRoot();
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(0);
                return child.toString().equals("/");
            }
        };
    }

    void selectFolder(String path) {
        selectFolder(path, true);
    }

    void selectFolder(String path, boolean includeSubfolders) {
        DialogFixture dialog = findVisibleDialog();
        JTreeFixture tree = dialog.tree(matchTreeByFirstChild());
        dialog.requireModal();
        dialog.requireVisible();
        if (!includeSubfolders) {
            dialog.checkBox("Sub-folders checkbox").click();
        }
        String[] paths = path.substring(1).split("/");
        int idx = 0;
        String currentValue = tree.node(idx).select().value();
        for (String eachPath : paths) {
            while (!currentValue.equals(eachPath)) {
                idx++;
                currentValue = tree.node(idx).value();
            }
            tree.node(idx).select();
        }
        dialog.button("Resource selector ok button").click();
    }

    DialogFixture findVisibleDialog() {
        return frame.dialog(new GenericTypeMatcher<>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog component) {
                return component.isVisible() && component.isShowing();
            }
        });
    }
    
    
}
