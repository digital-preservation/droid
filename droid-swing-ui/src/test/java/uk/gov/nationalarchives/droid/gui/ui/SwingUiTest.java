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

import org.apache.commons.lang3.StringUtils;
import org.assertj.swing.core.GenericTypeMatcher;

import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.*;
import static org.junit.Assert.*;
import static uk.gov.nationalarchives.droid.gui.ui.SwingUiTestUtils.*;

import org.assertj.swing.core.MouseClickInfo;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.*;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.gov.nationalarchives.droid.gui.DroidMainFrame;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class SwingUiTest extends AssertJSwingJUnitTestCase {
    private FrameFixture frame;
    private SwingUiTestUtils utils;

    static final String TEST_HOME = "/home/test";

    @Override
    protected void onSetUp() {
        setAutoUpdateFalse();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.getWidth(), screenSize.getHeight() - 400);
        application(DroidMainFrame.class).start();
        frame = findFrame(new GenericTypeMatcher<>(Frame.class, true) {
            protected boolean isMatching(Frame frame) {
                return frame != null && frame.getTitle().startsWith("DROID");
            }
        }).using(robot());
        frame.target().setLocation(0, 0);
        frame.resizeTo(screenSize);
        utils = new SwingUiTestUtils(frame, robot());
        utils.waitForInvisibleProgressLabel(frame.label(progressBarMatcher));
        robot().waitForIdle();
    }

    @Test
    public void should_render_new_profile_tab_when_new_is_clicked() {
        int existingPanelCount = utils.getProfilePanels().size();
        frame.button("New Profile").click();
        var newProfilePanels = utils.getProfilePanels();
        newProfilePanels.stream()
                .filter(Component::isShowing)
                .forEach(panel -> utils.waitForInvisibleProgressLabel(frame.panel(panel.getName()).label(progressBarMatcher)));
        Assert.assertEquals(utils.getProfilePanels().size(), existingPanelCount + 1);
        robot().waitForIdle();
    }

    @Test
    public void should_render_new_profile_tab_when_control_n_is_pressed() {
        int existingPanelCount = utils.getProfilePanels().size();
        robot().pressKey(KeyEvent.VK_CONTROL);
        robot().pressAndReleaseKey(KeyEvent.VK_N);
        robot().releaseKey(KeyEvent.VK_CONTROL);
        var newProfilePanels = utils.getProfilePanels();
        newProfilePanels.stream()
                .filter(Component::isShowing)
                .forEach(panel -> utils.waitForInvisibleProgressLabel(frame.panel(panel.getName()).label(progressBarMatcher)));
        Assert.assertEquals(utils.getProfilePanels().size(), existingPanelCount + 1);
        robot().waitForIdle();
    }

    @Test
    public void should_render_new_profile_tab_when_file_new_is_clicked() {
        int existingPanelCount = utils.getProfilePanels().size();
        utils.selectMenuItem("File", "New");
        var newProfilePanels = utils.getProfilePanels();
        newProfilePanels.stream()
                .filter(Component::isShowing)
                .forEach(panel -> utils.waitForInvisibleProgressLabel(frame.panel(panel.getName()).label(progressBarMatcher)));
        Assert.assertEquals(utils.getProfilePanels().size(), existingPanelCount + 1);
        robot().waitForIdle();
    }

    @Test
    public void should_add_files_with_add_files_button() {
        frame.button("Add File").click();
        utils.selectFolder(TEST_HOME);
        List<SwingUiTestUtils.ResultRow> resultRows = utils.getResultRows(false, List.of(TEST_HOME));
        Assert.assertEquals(resultRows.size(), 1);
    }

    @Test
    public void should_add_files_with_edit_menu() {
        utils.selectMenuItem("Edit", "Add file/folders");

        utils.selectFolder(TEST_HOME);
        List<SwingUiTestUtils.ResultRow> resultRows = utils.getResultRows(false, List.of(TEST_HOME));
        Assert.assertEquals(resultRows.size(), 1);
    }

    @Test
    public void should_show_correct_results_on_10000_files() {
        frame.button("Add File").click();
        utils.selectFolder(TEST_HOME);
        frame.button("Start").click();
        List<SwingUiTestUtils.ResultRow> resultRows = utils.getResultRows().stream().filter(row -> !row.puid().isEmpty()).toList();
        for (SwingUiTestUtils.ResultRow resultRow : resultRows) {
            assertEquals(resultRow.puid(), "x-fmt/111");
            assertEquals(resultRow.mimeType(), "text/plain");
            assertEquals(resultRow.identificationType(), "Extension");
            assertEquals(resultRow.version(), "");
            assertEquals(resultRow.format(), "Plain Text File");
            assertEquals(resultRow.ids(), "1");
            assertEquals(resultRow.size(), "5");
            assertEquals(resultRow.extension(), "txt");
            assertTrue(resultRow.resource().startsWith(TEST_HOME + "/file-"));
        }

    }

    @Test
    public void should_show_subfolders_by_default() {
        String path = TEST_HOME + "/test-subfolders";
        frame.button("Add File").click();
        utils.selectFolder(path);
        frame.button("Start").click();
        List<SwingUiTestUtils.ResultRow> resultRows = utils.getResultRows(true, List.of(path, "subfolder1"));
        List<String> expectedPaths = List.of(path + "/file1.txt", path + "/subfolder1/file2.txt");
        var pathsMatch = resultRows.stream()
                .filter(row -> !row.puid().isEmpty())
                .map(SwingUiTestUtils.ResultRow::resource)
                .allMatch(expectedPaths::contains);
        assertTrue(pathsMatch);
    }

    @Test
    public void should_not_show_subfolders_if_box_unchecked() {
        String path = TEST_HOME + "/test-subfolders";
        frame.button("Add File").click();
        utils.selectFolder(path, false);
        frame.button("Start").click();
        List<SwingUiTestUtils.ResultRow> resultRows = utils.getResultRows(true, List.of(path, "subfolder1"));
        Assert.assertEquals(resultRows.stream().filter(row -> !row.puid().isEmpty()).count(), 1);
    }

    @Test
    public void should_remove_files_when_remove_is_clicked() {
        frame.button("Add File").click();
        String path = TEST_HOME;
        utils.selectFolder(path);
        JTableFixture resultsTableFixture = utils.getResultsTableFixture();
        resultsTableFixture.cell(path).click();
        frame.button("Remove").click();
        List<SwingUiTestUtils.ResultRow> resultRows = utils.getResultRows(false, List.of());
        Assert.assertEquals(0, resultRows.size());
    }

    @Test
    public void should_remove_files_when_edit_remove_is_clicked() {
        frame.button("Add File").click();
        String path = TEST_HOME;
        utils.selectFolder(path);
        JTableFixture resultsTableFixture = utils.getResultsTableFixture();
        resultsTableFixture.cell(path).click();
        utils.selectMenuItem("Edit", "Remove files/folders");
        List<SwingUiTestUtils.ResultRow> resultRows = utils.getResultRows(false, List.of());
        Assert.assertEquals(0, resultRows.size());
    }

    @Test
    public void pause_and_resume_button_should_pause_and_resume() {
        frame.button("Add File").click();
        utils.selectFolder(TEST_HOME);
        JButtonFixture startButton = frame.button("Start");
        JButtonFixture pauseButton = frame.button("Pause");
        startButton.click();
        Assert.assertFalse(startButton.isEnabled());
        assertTrue(pauseButton.isEnabled());
        pauseButton.click();
        Assert.assertFalse(pauseButton.isEnabled());
        assertTrue(startButton.isEnabled());
        startButton.click();
        Assert.assertFalse(startButton.isEnabled());
        assertTrue(pauseButton.isEnabled());
        utils.waitForProfileProgressBarRemovedOrCompleted();
    }

    @Test
    public void pause_and_resume_menu_items_should_pause_and_resume() {
        frame.button("Add File").click();
        utils.selectFolder(TEST_HOME);
        JMenuItemFixture runMenuItem = frame.menuItem("Run");
        JMenuItemFixture startIdentification = frame.menuItem("Start identification");
        JMenuItemFixture pauseIdentification = frame.menuItem("Pause identification");
        runMenuItem.click();
        startIdentification.click();
        Assert.assertFalse(startIdentification.isEnabled());
        assertTrue(pauseIdentification.isEnabled());
        runMenuItem.click();
        pauseIdentification.click();
        Assert.assertFalse(pauseIdentification.isEnabled());
        assertTrue(startIdentification.isEnabled());
        runMenuItem.click();
        startIdentification.click();
        Assert.assertFalse(startIdentification.isEnabled());
        assertTrue(pauseIdentification.isEnabled());
        utils.waitForProfileProgressBarRemovedOrCompleted();
    }

    @Test
    public void a_paused_profile_can_be_saved_loaded_and_resumed() {
        String fileName = UUID.randomUUID().toString();
        frame.button("Add File").click();
        utils.selectFolder(TEST_HOME);
        JButtonFixture startButton = frame.button("Start");
        JButtonFixture pauseButton = frame.button("Pause");
        JButtonFixture saveButton = frame.button("Save");
        startButton.click();
        pauseButton.click();
        saveButton.click();
        JFileChooserFixture saveProfileFileChooser = utils.getFileChooser();
        Path profilesDirectory = Path.of("/home/profiles");
        saveProfileFileChooser.setCurrentDirectory(profilesDirectory.toFile());
        saveProfileFileChooser.fileNameTextBox().enterText(fileName);
        saveProfileFileChooser.approve();
        frame.button("Open").click();
        JFileChooserFixture openProfileFileChooser = utils.getFileChooser();
        openProfileFileChooser.setCurrentDirectory(profilesDirectory.toFile());
        openProfileFileChooser.selectFile(profilesDirectory.resolve(fileName + ".droid").toFile());
        openProfileFileChooser.approve();
        startButton.click();
        utils.waitForProfileProgressBarRemovedOrCompleted();
    }

    @Test
    public void profiles_can_be_saved_with_file_save() throws IOException {
        String fileName = UUID.randomUUID().toString();
        Path profileDirectory = Path.of("/home/profiles");
        Path profileFilePath = profileDirectory.resolve(fileName + ".droid");
        utils.selectMenuItem("File", "Save As...");

        JFileChooserFixture saveAsProfileFileChooser = utils.getFileChooser();
        saveAsProfileFileChooser.setCurrentDirectory(profileDirectory.toFile());
        saveAsProfileFileChooser.fileNameTextBox().enterText(fileName);
        saveAsProfileFileChooser.approve();

        utils.waitForInvisibleProgressLabel(frame.label(saveProfileMatcher));
        assertTrue(Files.exists(profileFilePath));
        Files.deleteIfExists(profileFilePath);
    }

    @Test
    public void should_copy_results_information_to_clipboard() throws IOException, UnsupportedFlavorException {
        frame.button("Add File").click();
        String path = TEST_HOME + "/test-subfolders";
        utils.selectFolder(path);
        frame.button("Start").click();
        JTableFixture resultsTableFixture = utils.getResultsTableFixture();
        resultsTableFixture.cell(path).doubleClick();
        robot().waitForIdle();
        resultsTableFixture.cell("file1.txt").rightClick();
        frame.menuItem("Copy to clipboard").click();
        Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        String transferData = (String) contents.getTransferData(DataFlavor.stringFlavor);

        assertTrue(transferData.contains("Plain Text File"));
        assertTrue(transferData.contains("Extension"));
        assertTrue(transferData.contains("x-fmt/111"));
    }

    @Test
    public void removing_column_with_right_click_removes_column() {
        JTableFixture resultsTableFixture = utils.getResultsTableFixture();
        resultsTableFixture.tableHeader().clickColumn("Resource", MouseClickInfo.rightButton());
        JPopupMenuFixture popup = new JPopupMenuFixture(robot(), robot().finder().findByType(JPopupMenu.class, true));
        Component[] component = popup.target().getComponents();
        System.out.println(component.length);
        for (Component c : component) {
            if (Objects.requireNonNull(c) instanceof JCheckBoxMenuItem checkBoxMenuItem) {
                if (checkBoxMenuItem.isSelected()) {
                    GuiActionRunner.execute(() -> checkBoxMenuItem.doClick());
                }
            }
        }
        robot().pressAndReleaseKey(KeyEvent.VK_ESCAPE);
        Assert.assertEquals(1, resultsTableFixture.target().getColumnCount());
    }

    @Test
    public void profile_export_follows_default_preferences() {
        utils.selectMenuItem("Tools", "Preferences");
        DialogFixture visibleDialog = utils.findVisibleDialog();
        visibleDialog.resizeHeightTo(visibleDialog.target().getHeight() - 20);
        JTabbedPaneFixture preferencesPane = visibleDialog.tabbedPane("Preferences pane");
        JTabbedPaneFixture exportDefaults = preferencesPane.selectTab("Export Defaults");
        JRadioButton selectedRadioButton = exportDefaults.robot().finder()
                .findAll(component -> component.getClass().equals(JRadioButton.class) && component.isVisible() && ((JRadioButton) component).getText().startsWith("One row per"))
                .stream()
                .map(button -> (JRadioButton)button)
                .filter(AbstractButton::isSelected).toList().getFirst();
        String selectedButtonText = selectedRadioButton.getText();
        visibleDialog.button("Preferences ok").click();

        utils.runProfile();
        frame.button("Export").click();
        DialogFixture exportProfileDialog = utils.findVisibleDialog();
        Assert.assertTrue(exportProfileDialog.radioButton(selectedButtonText).isEnabled());
    }

    @Test
    public void profile_export_follows_newly_set_preference() {
        utils.selectMenuItem("Tools", "Preferences");
        DialogFixture visibleDialog = utils.findVisibleDialog();
        JTabbedPaneFixture preferencesPane = visibleDialog.tabbedPane("Preferences pane");
        JTabbedPaneFixture exportDefaults = preferencesPane.selectTab("Export Defaults");
        JRadioButton unSelectedRadioButton = exportDefaults.robot().finder()
                .findAll(component -> component.getClass().equals(JRadioButton.class) && component.isVisible() && ((JRadioButton) component).getText().startsWith("One row per"))
                .stream()
                .map(button -> (JRadioButton)button)
                .filter(button -> !button.isSelected()).toList().getFirst();
        String unSelectedButtonText = unSelectedRadioButton.getText();
        JRadioButtonFixture radioButtonFixture = new JRadioButtonFixture(robot(), robot().finder().findByName(unSelectedRadioButton.getName(), JRadioButton.class, true));
        radioButtonFixture.click();
        visibleDialog.button("Preferences ok").click();

        utils.runProfile();
        robot().waitForIdle();
        frame.button("Export").click();
        DialogFixture exportProfileDialog = utils.findVisibleDialog();
        Assert.assertTrue(exportProfileDialog.radioButton(unSelectedButtonText).isEnabled());
        exportProfileDialog.button("Export cancel").click();
    }

    @Test
    public void non_reportable_profiles_are_not_selectable_in_report_dialog() {
        utils.runProfile();
        frame.button("New Profile").click();
        utils.getProfilePanels().stream()
                .filter(Component::isShowing)
                .forEach(panel -> utils.waitForInvisibleProgressLabel(frame.panel(panel.getName()).label(progressBarMatcher)));
        frame.button("Report").click();
        DialogFixture reportDialog = utils.findVisibleDialog();
        JTableFixture profileSelectTable = reportDialog.table("Profile select table");
        JTableCellFixture firstProfile = profileSelectTable.cell(TableCell.row(0).column(0));
        JTableCellFixture secondProfile = profileSelectTable.cell(TableCell.row(1).column(0));

        List<JTableCellFixture> sortedList = new ArrayList<>();
        sortedList.add(firstProfile);
        sortedList.add(secondProfile);
        sortedList.sort(Comparator.comparing(o -> Integer.valueOf(o.editor().getName().replace("Untitled-", ""))));
        Assert.assertEquals("false", sortedList.getFirst().value());
        Assert.assertEquals("false", sortedList.getLast().value());
        firstProfile.click();
        secondProfile.click();
        Assert.assertEquals("true", sortedList.getFirst().value());
        Assert.assertEquals("false", sortedList.getLast().value());
        reportDialog.button("Report dialog cancel").click();
    }

    @Test
    public void report_dialog_shows_expected_reports() {
        utils.runProfile();
        frame.button("Report").click();
        DialogFixture reportDialog = utils.findVisibleDialog();
        JComboBoxFixture reportSelectCombo = reportDialog.comboBox("Report select combo");
        String[] expectedContents = new String[] {
                "Comprehensive breakdown",
                "File count and sizes",
                "File count and sizes by file extension",
                "File count and sizes by file format PUID",
                "File count and sizes by mime type",
                "File count and sizes by month last modified",
                "File count and sizes by year and month last modified",
                "File count and sizes by year last modified",
                "Total count of files and folders",
                "Total unreadable files",
                "Total unreadable folders"
        };
        String[] contents = reportSelectCombo.contents();
        Assert.assertArrayEquals(expectedContents, contents);
        reportDialog.button("Report dialog cancel").click();
    }

    @Test
    public void comprehensive_report_exports_correctly() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        utils.runProfile();
        utils.generateReport("Comprehensive breakdown");
        XPath xPath = XPathFactory.newInstance().newXPath();
        Path planetsFilePath = utils.exportReport("Planets XML (*.xml)", "xml");

        Document planetsDoc = getDocument(planetsFilePath);

        assertEquals("10", xPath.evaluate("//totalSize", planetsDoc));
        assertEquals("x-fmt/111", xPath.evaluate("//byFormat/formatItem/PUID", planetsDoc));
        assertEquals("2", xPath.evaluate("//byYear/yearItem/numFiles", planetsDoc));

        Path txtFilePath = utils.exportReport("Text (*.txt)", "txt");
        String exportTxt = Files.readString(txtFilePath);
        assertTrue(exportTxt.contains("Group values:\tx-fmt/111\tPlain Text File\t\ttext/plain"));
        assertEquals(7, StringUtils.countMatches(exportTxt, "Profile totals\t2\t10\t5\t5\t5"));

        Path htmlFilePath = utils.exportReport("Web page (*.html)", "html");
        Document htmlDoc = getDocument(htmlFilePath);
        assertEquals("10", xPath.evaluate("//td[text() = '10']", htmlDoc));
        assertEquals("5", xPath.evaluate("//td[text() = '5']", htmlDoc));
        assertEquals("x-fmt/111", xPath.evaluate("//td[@class = 'groupvalues' and text() = 'x-fmt/111']", htmlDoc));
        assertEquals("Plain Text File", xPath.evaluate("//td[@class = 'groupvalues' and text() = 'Plain Text File']", htmlDoc));

        Path droidXml = utils.exportReport("DROID Report XML (*.xml)", "xml");
        String summaryPath = "//ReportItem[Specification/Description='File count and sizes']/Groups/Group/GroupAggregateSummary/";
        Document droidDoc = getDocument(droidXml);
        assertEquals("2", xPath.evaluate(summaryPath + "Count", droidDoc));
        assertEquals("10", xPath.evaluate(summaryPath + "Sum", droidDoc));
        assertEquals("5", xPath.evaluate(summaryPath + "Max", droidDoc));
        NodeList formatInfo = (NodeList) xPath.evaluate("//ReportItem[Specification/Description='File sizes per PUID']/Groups/Group/Values/Value/text()", droidDoc, XPathConstants.NODESET);
        assertEquals("x-fmt/111", formatInfo.item(0).getTextContent());
        assertEquals("Plain Text File", formatInfo.item(1).getTextContent());
        assertEquals("text/plain", formatInfo.item(2).getTextContent());

        Path pdfFilePath = utils.exportReport("Adobe Portable Document Format (*.pdf)", "pdf");
        byte[] pdfArr = Files.readAllBytes(pdfFilePath);
        assertArrayEquals(Arrays.copyOfRange(pdfArr, 0, 4), new byte[] {37, 80, 68, 70});
    }

    @Test
    public void file_count_reports_exports_correctly() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String[] reportNames = new String[] {
                "File count and sizes",
                "File count and sizes by file extension",
                "File count and sizes by file format PUID",
                "File count and sizes by mime type",
                "File count and sizes by month last modified",
                "File count and sizes by year and month last modified",
                "File count and sizes by year last modified",
        };
        utils.runProfile();

        for (String reportName : reportNames) {
            System.out.println(reportName);
            utils.generateReport(reportName);
            XPath xPath = XPathFactory.newInstance().newXPath();

            Path txtFilePath = utils.exportReport("Text (*.txt)", "txt");
            String exportTxt = Files.readString(txtFilePath);
            assertEquals(1, StringUtils.countMatches(exportTxt, "Profile totals\t2\t10\t5\t5\t5"));

            Path htmlFilePath = utils.exportReport("Web page (*.html)", "html");
            Document htmlDoc = getDocument(htmlFilePath);
            assertEquals("10", xPath.evaluate("//td[text() = '10']", htmlDoc));
            assertEquals("5", xPath.evaluate("//td[text() = '5']", htmlDoc));

            Path droidXml = utils.exportReport("DROID Report XML (*.xml)", "xml");
            String summaryPath = "//ReportItem[Specification/Description='" + reportName + "']/Groups/Group/GroupAggregateSummary/";
            Document droidDoc = getDocument(droidXml);
            assertEquals("2", xPath.evaluate(summaryPath + "Count", droidDoc));
            assertEquals("10", xPath.evaluate(summaryPath + "Sum", droidDoc));
            assertEquals("5", xPath.evaluate(summaryPath + "Max", droidDoc));

            Path pdfFilePath = utils.exportReport("Adobe Portable Document Format (*.pdf)", "pdf");
            byte[] pdfArr = Files.readAllBytes(pdfFilePath);
            assertArrayEquals(Arrays.copyOfRange(pdfArr, 0, 4), new byte[] {37, 80, 68, 70});
            new JButtonFixture(robot(), robot().finder().findByName("Report export close", JButton.class)).click();
        }
    }

    @Test
    public void total_count_reports_exports_correctly() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String[] reportNames = new String[] {
                "Total count of files and folders",
                "Total unreadable files",
                "Total unreadable folders"
        };
        utils.runProfile();

        for (String reportName : reportNames) {
            System.out.println(reportName);
            utils.generateReport(reportName);
            XPath xPath = XPathFactory.newInstance().newXPath();

            Path txtFilePath = utils.exportReport("Text (*.txt)", "txt");
            String exportTxt = Files.readString(txtFilePath);
            assertTrue(exportTxt.contains(reportName));

            Path htmlFilePath = utils.exportReport("Web page (*.html)", "html");
            Document htmlDoc = getDocument(htmlFilePath);
            assertEquals(reportName, xPath.evaluate("//h2[text() = '" + reportName + "']", htmlDoc));

            Path droidXml = utils.exportReport("DROID Report XML (*.xml)", "xml");
            Document droidDoc = getDocument(droidXml);
            assertEquals(reportName, xPath.evaluate("//Report/Title", droidDoc));

            Path pdfFilePath = utils.exportReport("Adobe Portable Document Format (*.pdf)", "pdf");
            byte[] pdfArr = Files.readAllBytes(pdfFilePath);
            assertArrayEquals(Arrays.copyOfRange(pdfArr, 0, 4), new byte[] {37, 80, 68, 70});
            new JButtonFixture(robot(), robot().finder().findByName("Report export close", JButton.class)).click();
        }
    }
}