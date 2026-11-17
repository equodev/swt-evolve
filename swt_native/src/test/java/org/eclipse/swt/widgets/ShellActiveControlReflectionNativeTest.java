package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Embedding bridges tell a shell which embedded control is active through the package-private
 * {@code Shell.setActiveControl(Control)}, looked up by reflection on {@code Shell} itself, as every
 * platform's SWT declares it there.
 */
@Tag("native-unit")
class ShellActiveControlReflectionNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestBridge bridge = new TestBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("Shell declares setActiveControl(Control) and setActiveControl(Control, int)")
    void declaredOnShell() throws Exception {
        assertThat(Shell.class.getDeclaredMethod("setActiveControl", Control.class)).isNotNull();
        assertThat(Shell.class.getDeclaredMethod("setActiveControl", Control.class, int.class)).isNotNull();
    }

    @Test
    @DisplayName("setActiveControl reached by reflection activates the control and deactivates the previous one")
    void reflectiveCallMovesActivation() throws Exception {
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        Button first = new Button(shell, SWT.PUSH);
        Button second = new Button(shell, SWT.PUSH);
        List<String> events = new ArrayList<>();
        first.addListener(SWT.Activate, e -> events.add("first Activate"));
        first.addListener(SWT.Deactivate, e -> events.add("first Deactivate"));
        second.addListener(SWT.Activate, e -> events.add("second Activate"));

        Method setActiveControl = Shell.class.getDeclaredMethod("setActiveControl", Control.class);
        setActiveControl.setAccessible(true);
        setActiveControl.invoke(shell, first);
        setActiveControl.invoke(shell, second);

        assertThat(events).containsExactly("first Activate", "first Deactivate", "second Activate");
    }

    @Test
    @DisplayName("a control activated by reflection is not activated again when the focus follows it")
    void focusFollowingTheReflectiveCallAnnouncesNothingMore() throws Exception {
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        new Button(shell, SWT.PUSH);
        Button second = new Button(shell, SWT.PUSH);
        shell.setVisible(true);
        List<String> events = new ArrayList<>();
        second.addListener(SWT.Activate, e -> events.add("second Activate"));
        second.addListener(SWT.Deactivate, e -> events.add("second Deactivate"));

        Method setActiveControl = Shell.class.getDeclaredMethod("setActiveControl", Control.class);
        setActiveControl.setAccessible(true);
        setActiveControl.invoke(shell, second);
        assertThat(second.forceFocus()).as("the focus moves to the activated control").isTrue();
        ControlHelper.clientFocusIn((DartControl) second.getImpl());

        assertThat(events).containsExactly("second Activate");
    }

    /** A Display-level bridge, the kind desktop and web run on, with nothing behind its channel. */
    private static final class TestBridge extends DisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }
}
