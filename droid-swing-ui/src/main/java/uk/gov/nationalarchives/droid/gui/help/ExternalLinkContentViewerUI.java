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
    @Override
    public void hyperlinkUpdate(final HyperlinkEvent he) {

        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URL u = he.getURL();
            if ("mailto".equalsIgnoreCase(u.getProtocol()) || "http".equalsIgnoreCase(u.getProtocol())
                    || "ftp".equalsIgnoreCase(u.getProtocol())) {
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
