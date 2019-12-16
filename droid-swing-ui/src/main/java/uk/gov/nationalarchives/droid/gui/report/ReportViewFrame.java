/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gsi.gov.uk>
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
package uk.gov.nationalarchives.droid.gui.report;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.xml.transform.TransformerException;

import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

import uk.gov.nationalarchives.droid.gui.util.DroidImageUtils;
import uk.gov.nationalarchives.droid.report.ReportTransformer;

/**
 *
 * @author rflitcroft
 */
//CHECKSTYLE:OFF
// FIXME: Data Abstraction Coupling is too high (max 9, this has 10)
// Gotta say - I don't see that this is a particularly complex form, but
// maybe some refactoring could be done.  
// Turning checkstyle off temporarily to make this next release.  
public class ReportViewFrame extends JFrame {
    //CHECKSTYLE:ON
    private static final long serialVersionUID = 9212026527186933180L;
    private static final String UTF8 = "UTF-8";

    private ReportTransformer reportTransformer;
    private ExportReportAction exportAction;
    
    private Path reportFile;
    private List<Path> xslTransforms;
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** 
     * Creates new form ReportViewDialog.
     * 
     * @param parent this dialog's parent
     * 
     */
    public ReportViewFrame(final Frame parent) {
        //super(parent);
        //setModal(true);
        initComponents();
        setIconImage(DroidImageUtils.getScaledImageIcon("/uk/gov/nationalarchives/droid/icons/DROID16.png",
                DroidImageUtils.SMALL_ICON_WIDTH_HEIGHT, DroidImageUtils.SMALL_ICON_WIDTH_HEIGHT).getImage());


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                if (reportFile != null) {
                    if (Files.exists(reportFile)) {
                        try {
                            Files.deleteIfExists(reportFile);
                        } catch (final IOException ex) {
                            String message = String.format("Could not delete report file: %s. "
                                            + "Will try to delete on exit.",
                                    reportFile.toAbsolutePath().toString());
                            log.warn(message);
                            reportFile.toFile().deleteOnExit();
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Renders the report xml in the dialog.
     * 
     * @param reportXml the report to render
     * @param transforms a list of xsl files that can transform the report xml.
     */
    public void renderReport(final Path reportXml, final List<Path> transforms) {
        
        this.reportFile = reportXml;
        this.xslTransforms = transforms;

        // BNO, Nov 2016: Now we use a specific decoder and  InputStreamReader to force UTF-8 encoding
        // (previously we used a FileWriter uses OS default encoding - this could lead to XML that was non UTF8
        // despite the declaration saying it was, and a SAXParseException when processing the report)
        try (final Reader reader = Files.newBufferedReader(reportXml, UTF_8);
                final StringWriter out = new StringWriter()) {
            //Reader sourceReader = new FileReader(reportXml);
            reportTransformer.transformUsingXsl(reader, "Web page.html.xsl", out);

            //CHECKSTYLE:OFF - need to catch Exception
            try (final InputStream in = new ByteArrayInputStream(out.getBuffer().toString().getBytes(UTF8))) {
                xHTMLPanel1.setDocument(in, "");
            } catch (final Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
            }
            //CHECKSTYLE:ON

        } catch (final IOException | TransformerException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
        pack();
        setLocationRelativeTo(getParent());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        exportButton = new JButton();
        closeButton = new JButton();
        fSScrollPane1 = new FSScrollPane();
        xHTMLPanel1 = new XHTMLPanel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(NbBundle.getMessage(ReportViewFrame.class, "ReportViewDialog.title")); // NOI18N
        exportButton.setText(NbBundle.getMessage(ReportViewFrame.class, "ReportViewDialog.exportButton.text")); // NOI18N
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        closeButton.setText(NbBundle.getMessage(ReportViewFrame.class, "ReportViewDialog.closeButton.text")); // NOI18N
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        GroupLayout xHTMLPanel1Layout = new GroupLayout(xHTMLPanel1);
        xHTMLPanel1.setLayout(xHTMLPanel1Layout);
        xHTMLPanel1Layout.setHorizontalGroup(
            xHTMLPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 627, Short.MAX_VALUE)
        );
        xHTMLPanel1Layout.setVerticalGroup(
            xHTMLPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 505, Short.MAX_VALUE)
        );

        fSScrollPane1.setViewportView(xHTMLPanel1);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fSScrollPane1, GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(exportButton)
                        .addGap(18, 18, 18)
                        .addComponent(closeButton, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {closeButton, exportButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fSScrollPane1, GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(closeButton))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {closeButton, exportButton});

    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void exportButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        exportAction.setDroidReportXml(reportFile);
        exportAction.execute(this, xslTransforms);
    }//GEN-LAST:event_exportButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton closeButton;
    private JButton exportButton;
    private FSScrollPane fSScrollPane1;
    private XHTMLPanel xHTMLPanel1;
    // End of variables declaration//GEN-END:variables
    
    /**
     * @param reportTransformer the reportTransformer to set
     */
    public void setReportTransformer(ReportTransformer reportTransformer) {
        this.reportTransformer = reportTransformer;
    }
    
    /**
     * @param exportAction the exportAction to set
     */
    public void setExportAction(ExportReportAction exportAction) {
        this.exportAction = exportAction;
    }

}
