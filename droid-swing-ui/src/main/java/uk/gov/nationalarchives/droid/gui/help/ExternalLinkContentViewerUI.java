/**
 * <p>Copyright (c) The National Archives 2005-2010.  All rights reserved.
 * See Licence.txt for full licence details.
 * <p/>
 *
 * <p>DROID DCS Profile Tool
 * <p/>
 */
package uk.gov.nationalarchives.droid.gui.help;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.help.JHelpContentViewer;
import javax.help.plaf.basic.BasicContentViewerUI;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;

import uk.gov.nationalarchives.droid.gui.DialogUtils;

/**
 * A UI subclass that will open external links (website or mail links) in an
 * external browser.
 */
public class ExternalLinkContentViewerUI extends BasicContentViewerUI {

    /**
     * @param context
     *            JHelpContentViewer.
     */
    public ExternalLinkContentViewerUI(JHelpContentViewer context) {
        super(context);
    }

    /**
     * @param x
     *            JComponent
     * @return ExternalLinkContentViewerUI
     */
    public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
        return new ExternalLinkContentViewerUI((JHelpContentViewer) x);
    }

    /**
     * @param he
     *            hiperlik Event.
     * @see javax.help.plaf.basic.BasicContentViewerUI#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
     */
    public void hyperlinkUpdate(HyperlinkEvent he) {

        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URL u = he.getURL();
            if (u.getProtocol().equalsIgnoreCase("mailto") || u.getProtocol().equalsIgnoreCase("http")
                    || u.getProtocol().equalsIgnoreCase("ftp")) {
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(u.toURI());
                        } catch (MalformedURLException e1) {
                            DialogUtils.showGeneralErrorDialog(new Frame(), "MalformedURLException", "Invalid URL.");
                        } catch (IOException e1) {
                            DialogUtils.showGeneralErrorDialog(new Frame(), "IOException", "Resource not found.");
                        } catch (URISyntaxException uriSyntaxEx) {
                            DialogUtils.showGeneralErrorDialog(new Frame(), "URISyntaxException", "Invalid URI.");
                        }
                    }
                }
            } else {
                super.hyperlinkUpdate(he);

            }
        }

    }

}
