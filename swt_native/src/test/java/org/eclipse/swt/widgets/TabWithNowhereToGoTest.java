package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Tab is a traversal only when the shell holds a control that takes the focus from it; with nowhere
 * to go, every native delivers the key as a KeyDown. These are the controls that count.
 */
@ExtendWith(Mocks.class)
class TabWithNowhereToGoTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("labels, groups, sashes and an empty Link are no tab stop")
    void controlsTabPassesOver() {
        Composite parent = new Composite(Mocks.shell(), SWT.NONE);
        new Label(parent, SWT.NONE).setText("name");
        new Group(new Composite(parent, SWT.NONE), SWT.NONE);
        new Sash(parent, SWT.VERTICAL);
        new Link(parent, SWT.NONE).setText("no link here");

        assertThat(ControlHelper.hasTabStop(parent)).isFalse();
    }

    @Test
    @DisplayName("a button, a field or a Link with a link anywhere inside is a tab stop")
    void controlsTabFocuses() {
        Composite parent = new Composite(Mocks.shell(), SWT.NONE);
        Composite nested = new Composite(parent, SWT.NONE);
        new Label(parent, SWT.NONE);
        new Button(nested, SWT.PUSH).setText("OK");
        assertThat(ControlHelper.hasTabStop(parent)).isTrue();

        Composite withLink = new Composite(Mocks.shell(), SWT.NONE);
        new Link(withLink, SWT.NONE).setText("see <a>the docs</a>");
        assertThat(ControlHelper.hasTabStop(withLink)).isTrue();
    }

    @Test
    @DisplayName("a hidden or disabled control is no tab stop")
    void hiddenOrDisabledIsNot() {
        Composite parent = new Composite(Mocks.shell(), SWT.NONE);
        new Button(parent, SWT.PUSH).setVisible(false);
        new Text(parent, SWT.BORDER).setEnabled(false);

        assertThat(ControlHelper.hasTabStop(parent)).isFalse();
    }
}
