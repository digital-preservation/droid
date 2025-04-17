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

import org.apache.commons.io.FileUtils;
import org.assertj.swing.core.GenericTypeMatcher;

import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.*;
import static org.assertj.swing.timing.Pause.pause;

import org.assertj.swing.fixture.*;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AssertJTest extends AssertJSwingJUnitTestCase {
    FrameFixture frame;

    private final  GenericTypeMatcher<JLabel> progressBarMatcher = new GenericTypeMatcher<>(JLabel.class) {
        @Override
        protected boolean isMatching(JLabel component) {
            return "Initialising profile...".equals(component.getText());
        }
    };

    private void waitForProgressBarInvisible(JLabelFixture labelFixture) {

        Condition condition = new Condition("Wait for invisible progress bar") {
          public boolean test() {
              return !labelFixture.target().isVisible();
          }
        };
        pause(condition);
    }

    private void cleanProfiles() {
        Path droidHomePath = Paths.get(System.getProperty("user.home"), ".droid6");
        try {
            FileUtils.deleteDirectory(droidHomePath.resolve("profiles").toFile());
            FileUtils.deleteDirectory(droidHomePath.resolve("profile_templates").toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GenericTypeMatcher<JButton> matchButtonByText(String text) {
        return new GenericTypeMatcher<>(JButton.class) {
            @Override
            protected boolean isMatching(JButton jButton) {
                return jButton.getText().equals(text);
            }
        };
    }

    @Override
    protected void onSetUp() {
        cleanProfiles();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        application(DroidMainFrame.class).start();
        frame = findFrame(new GenericTypeMatcher<>(Frame.class, true) {
            protected boolean isMatching(Frame frame) {
                return frame != null && frame.getTitle().startsWith("DROID");
            }
        }).using(robot());

        waitForProgressBarInvisible(frame.label(progressBarMatcher));
    }

    @Test
    public void should_render_new_profile_tab_when_new_is_clicked() {
        frame.button("New Profile").click();
        JPanelFixture panel = frame.panel("Untitled-2");
        waitForProgressBarInvisible(panel.label(progressBarMatcher));
    }

    @Test
    public void should_render_new_profile_tab_when_file_new_is_clicked() {
        frame.menuItem("File").click();
        JMenuItemFixture newProfile = frame.menuItem("New");
        newProfile.requireVisible();
        newProfile.click();
        JPanelFixture panel = frame.panel("Untitled-4");
        waitForProgressBarInvisible(panel.label(progressBarMatcher));
    }
}
