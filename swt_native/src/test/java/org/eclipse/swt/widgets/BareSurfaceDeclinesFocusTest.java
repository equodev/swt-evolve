package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.ViewForm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A press or Tab focuses an empty Canvas or Composite natively only while it hooks key events; the
 * native controls that extend Composite take the focus regardless.
 */
@ExtendWith(Mocks.class)
class BareSurfaceDeclinesFocusTest {

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

    /** An application's own panel: a Composite subclass. */
    private static final class AppPanel extends Composite {
        AppPanel(Composite parent) {
            super(parent, SWT.NONE);
        }
    }

    @Test
    @DisplayName("an empty Composite, Canvas or application panel with no key listener declines the focus")
    void bareSurfacesDecline() {
        Shell shell = Mocks.shell();
        assertThat(DisplayBridge.declinesClientFocus(new Composite(shell, SWT.NONE))).isTrue();
        assertThat(DisplayBridge.declinesClientFocus(new Canvas(shell, SWT.NONE))).isTrue();
        assertThat(DisplayBridge.declinesClientFocus(new AppPanel(shell))).isTrue();
    }

    @Test
    @Disabled("declining leaves the client's focus on the surface; for these widgets that strands the keyboard")
    @DisplayName("SWT's own plain-Composite custom widgets decline the focus when empty, as natively")
    void customCompositesDecline() {
        Shell shell = Mocks.shell();
        assertThat(DisplayBridge.declinesClientFocus(new SashForm(shell, SWT.NONE))).isTrue();
        assertThat(DisplayBridge.declinesClientFocus(new ScrolledComposite(shell, SWT.NONE))).isTrue();
        assertThat(DisplayBridge.declinesClientFocus(new ViewForm(shell, SWT.NONE))).isTrue();
        assertThat(DisplayBridge.declinesClientFocus(new CBanner(shell, SWT.NONE))).isTrue();
    }

    @Test
    @DisplayName("NO_FOCUS declines the focus even with a key listener")
    void noFocusDeclines() {
        Shell shell = Mocks.shell();
        Composite composite = new Composite(shell, SWT.NO_FOCUS);
        composite.addListener(SWT.KeyDown, e -> { });
        assertThat(DisplayBridge.declinesClientFocus(composite)).isTrue();
    }

    @Test
    @DisplayName("a key listener, a child or EMBEDDED keeps the focus on the surface")
    void surfacesThatTakeTheFocus() {
        Shell shell = Mocks.shell();
        Composite listening = new Composite(shell, SWT.NONE);
        listening.addListener(SWT.KeyDown, e -> { });
        Composite parent = new Composite(shell, SWT.NONE);
        new Button(parent, SWT.PUSH);
        assertThat(DisplayBridge.declinesClientFocus(listening)).isFalse();
        assertThat(DisplayBridge.declinesClientFocus(parent)).isFalse();
        assertThat(DisplayBridge.declinesClientFocus(new Composite(shell, SWT.EMBEDDED))).isFalse();
    }

    @Test
    @DisplayName("the native controls that extend Composite, and the shell, keep the focus")
    void nativeControlsKeepIt() {
        Shell shell = Mocks.shell();
        assertThat(DisplayBridge.declinesClientFocus(new Table(shell, SWT.NONE))).isFalse();
        assertThat(DisplayBridge.declinesClientFocus(new Spinner(shell, SWT.NONE))).isFalse();
        assertThat(DisplayBridge.declinesClientFocus(new TabFolder(shell, SWT.NONE))).isFalse();
        assertThat(DisplayBridge.declinesClientFocus(new Table(shell, SWT.NONE) {
            @Override
            protected void checkSubclass() {
            }
        })).isFalse();
        assertThat(DisplayBridge.declinesClientFocus(shell)).isFalse();
    }
}
