package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.ShellWindow;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The states the desktop Display's window is asked for when its main shell changes state. */
class DeskWindowStateTest {

    private Display display;
    /** The desktop bridge switches the JVM to desktop mode; the tests that run after this one must not inherit it. */
    private final String savedMode = ConfigFlags.mode();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else ConfigFlags.setMode(savedMode);
    }

    @Test
    void unMinimizingTheMainShellGivesTheWindowBackTheStateTheShellStillHas() {
        TestDeskBridge desk = install();
        Shell shell = new Shell(display);

        shell.setFullScreen(true);
        shell.setMinimized(true);
        shell.setMinimized(false);
        shell.setFullScreen(false);
        shell.setMaximized(true);
        shell.setMinimized(true);
        shell.setMinimized(false);

        assertThat(desk.fullScreen).containsExactly(true, false);
        assertThat(desk.states).containsExactly(
                ShellWindow.STATE_MINIMIZED, ShellWindow.STATE_FULLSCREEN,
                ShellWindow.STATE_MAXIMIZED, ShellWindow.STATE_MINIMIZED, ShellWindow.STATE_MAXIMIZED);
    }

    private TestDeskBridge install() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestDeskBridge bridge = new TestDeskBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** The desktop bridge with its window stood in by a record of what it was asked. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Integer> states = new ArrayList<>();
        final List<Boolean> fullScreen = new ArrayList<>();

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected Point shellWindowOrigin(long context) {
            return null;
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }

        @Override
        protected boolean hasWindowSurface() {
            return true;
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        protected void setShellWindowState(long context, int state) {
            states.add(state);
        }

        @Override
        protected void forwardWindowFullScreen(boolean fullScreen) {
            this.fullScreen.add(fullScreen);
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }
    }
}
