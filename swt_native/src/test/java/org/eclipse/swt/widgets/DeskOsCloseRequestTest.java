package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.DesktopAppearanceStub;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterNative;
import dev.equo.swt.WindowPolicy;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The OS asks the Display's window to close (Alt+F4, the taskbar's close) while a top-level window
 * drawn inside it is the active one. Natively that window has an OS window of its own and is the one
 * asked; here every top-level Shell drawn inline shares the Display's window.
 */
class DeskOsCloseRequestTest {

    private Display display;
    private String savedMode;
    private String savedDesktop;
    private DesktopAppearanceStub desktop;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        if (desktop != null) desktop.close();
        restore(ConfigFlags.MODE_PROPERTY, savedMode);
        restore("dev.equo.swt.desktop", savedDesktop);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private CloseRequestingBridge desktopDisplay() {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        savedDesktop = System.getProperty("dev.equo.swt.desktop");
        desktop = new DesktopAppearanceStub("light");
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        CloseRequestingBridge bridge = new CloseRequestingBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** A Swing island's top-level frame: a parentless trimmed Shell kept in the Display's window. */
    private Shell islandFrame(List<Shell> closeRequests) {
        Shell frame = new Shell(display, SWT.SHELL_TRIM);
        frame.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        frame.setText("Contents");
        frame.addListener(SWT.Close, e -> {
            e.doit = false;
            closeRequests.add(frame);
        });
        frame.setBounds(250, 200, 500, 300);
        frame.open();
        return frame;
    }

    private void drain() {
        while (display.readAndDispatch()) {}
    }

    private Shell mainShell(List<Shell> closeRequests) {
        Shell main = new Shell(display);
        main.addListener(SWT.Close, e -> {
            e.doit = false;
            closeRequests.add(main);
        });
        main.setBounds(0, 0, 800, 500);
        main.open();
        return main;
    }

    private void activate(Shell shell) {
        shell.setActive();
        drain();
        assertThat(display.getActiveShell()).isSameAs(shell);
    }

    private void requestOsClose(CloseRequestingBridge bridge) {
        bridge.closeRequested = true;
        bridge.pumpEngineOnly();
        drain();
    }

    @Test
    void anOsCloseWithAnInlineFrameActiveAsksTheFrame() {
        CloseRequestingBridge bridge = desktopDisplay();
        List<Shell> closeRequests = new ArrayList<>();
        mainShell(closeRequests);
        Shell frame = islandFrame(closeRequests);
        activate(frame);

        requestOsClose(bridge);

        assertThat(closeRequests).as("the shells asked to close").containsExactly(frame);
    }

    @Test
    void anOsCloseWithAnInlineDialogActiveAsksTheDialog() {
        CloseRequestingBridge bridge = desktopDisplay();
        List<Shell> closeRequests = new ArrayList<>();
        Shell main = mainShell(closeRequests);
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        dialog.addListener(SWT.Close, e -> closeRequests.add(dialog));
        dialog.setBounds(200, 150, 300, 200);
        dialog.open();
        activate(dialog);

        requestOsClose(bridge);

        assertThat(closeRequests).as("the shells asked to close").containsExactly(dialog);
        assertThat(dialog.isDisposed()).as("an accepted close disposes the dialog").isTrue();
        assertThat(main.isDisposed()).isFalse();
    }

    @Test
    void anOsCloseAfterTheWindowLostTheOsFocusAsksTheDialogThatHadIt() {
        CloseRequestingBridge bridge = desktopDisplay();
        List<Shell> closeRequests = new ArrayList<>();
        Shell main = mainShell(closeRequests);
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        dialog.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        dialog.addListener(SWT.Close, e -> {
            e.doit = false;
            closeRequests.add(dialog);
        });
        Text field = new Text(dialog, SWT.BORDER);
        field.setBounds(10, 10, 120, 24);
        dialog.setBounds(200, 150, 300, 200);
        dialog.open();
        activate(dialog);
        field.forceFocus();
        drain();
        // The window manager deactivates the window before it delivers Alt+F4's close.
        bridge.windowFocused = false;
        assertThat(display.getActiveShell()).isNull();

        requestOsClose(bridge);

        assertThat(closeRequests).as("the shells asked to close").containsExactly(dialog);
    }

    @Test
    void anOsCloseWithTheMainShellActiveAsksTheMainShell() {
        CloseRequestingBridge bridge = desktopDisplay();
        List<Shell> closeRequests = new ArrayList<>();
        Shell main = mainShell(closeRequests);
        islandFrame(closeRequests);
        activate(main);

        requestOsClose(bridge);

        assertThat(closeRequests).as("the shells asked to close").containsExactly(main);
    }

    @Test
    void anOsCloseWithAnInlineShellWithoutCloseButtonActiveAsksTheMainShell() {
        CloseRequestingBridge bridge = desktopDisplay();
        List<Shell> closeRequests = new ArrayList<>();
        Shell main = mainShell(closeRequests);
        Shell tool = new Shell(main, SWT.TITLE);
        tool.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        tool.addListener(SWT.Close, e -> {
            e.doit = false;
            closeRequests.add(tool);
        });
        tool.setBounds(200, 150, 300, 200);
        tool.open();
        activate(tool);

        requestOsClose(bridge);

        assertThat(closeRequests).as("the shells asked to close").containsExactly(main);
    }

    @Test
    void anOsCloseWithADialogOfAWindowedShellActiveAsksTheMainShell() {
        CloseRequestingBridge bridge = desktopDisplay();
        List<Shell> closeRequests = new ArrayList<>();
        Shell main = mainShell(closeRequests);
        Shell windowed = new Shell(display, SWT.SHELL_TRIM);
        bridge.windowed = windowed;
        windowed.setBounds(100, 100, 600, 400);
        windowed.open();
        Shell dialog = new Shell(windowed, SWT.DIALOG_TRIM);
        dialog.addListener(SWT.Close, e -> {
            e.doit = false;
            closeRequests.add(dialog);
        });
        dialog.setBounds(200, 150, 300, 200);
        dialog.open();
        activate(dialog);

        requestOsClose(bridge);

        assertThat(closeRequests).as("the shells asked to close").containsExactly(main);
    }

    /** The desktop bridge with its window stood in: a pump reports the close it is told to. */
    private static final class CloseRequestingBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        boolean closeRequested;
        /** Stands for a shell drawn in an OS window of its own. */
        Shell windowed;

        CloseRequestingBridge(DartDisplay display) {
            super(display);
        }

        @Override
        boolean isShellWindowed(Shell shell) {
            return shell == windowed || super.isShellWindowed(shell);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
        }

        /** Up only while the pump reports the close: no other path reaches a window that is not there. */
        @Override
        protected boolean hasNativeWindow() {
            return closeRequested;
        }

        boolean windowFocused = true;

        /** The Display's window has the OS focus, as it has when the person presses Alt+F4. */
        @Override
        public boolean hostsActiveWindow(Object shell) {
            return windowFocused && shell instanceof Shell && !((Shell) shell).isDisposed();
        }

        @Override
        protected int pumpWindow() {
            if (!closeRequested) return 0;
            closeRequested = false;
            return FlutterNative.PUMP_CLOSE_REQUESTED;
        }
    }
}
