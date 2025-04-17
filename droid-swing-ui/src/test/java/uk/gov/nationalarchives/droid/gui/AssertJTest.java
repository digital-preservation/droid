package uk.gov.nationalarchives.droid.gui;

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
        frame.button(matchButtonByText("New")).click();
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
