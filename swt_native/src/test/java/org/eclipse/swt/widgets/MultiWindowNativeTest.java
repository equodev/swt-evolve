package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.ShellWindow;
import dev.equo.swt.WindowPolicy;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The window registry {@link DisplayBridge} keeps for shells {@link WindowPolicy} detached: which
 * windows exist, what the client is told about them, and where a window operation on such a shell
 * goes.
 *
 * <p>Surface-agnostic on purpose. Opening a real window is the one thing the two surfaces do
 * differently, so it is the one thing stubbed here; everything else below is shared by both and has
 * to behave identically whether a native window or a browser window is on the other end.
 */
@Tag("native-unit")
class MultiWindowNativeTest {

    private Display display;
    private final java.util.Map<String, String> savedProperties = new java.util.HashMap<>();

    @BeforeEach
    void setUp() {
        for (String key : new String[]{WindowPolicy.MODE_PROPERTY, WindowPolicy.DIALOGS_PROPERTY,
                WindowPolicy.MODAL_PROPERTY}) {
            savedProperties.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
    }

    @AfterEach
    void tearDown() {
        WindowPolicy.setResolver(null);
        savedProperties.forEach((key, value) -> {
            if (value == null) System.clearProperty(key);
            else System.setProperty(key, value);
        });
        savedProperties.clear();
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    // ---- the default costs nothing -----------------------------------------------------------------

    @Test
    @DisplayName("under the default policy no window is ever opened")
    void singleWindowOpensNothing() {
        TestBridge bridge = install();
        Shell main = openShell();
        Shell second = openShell();

        assertThat(bridge.created).isEmpty();
        assertThat(bridge.windowedShells()).isEmpty();
        assertThat(latestDisplayState(bridge))
                .as("the client is told nothing it would have to act on")
                .doesNotContain("windowedShellIds");
        assertThat(main.isDisposed() || second.isDisposed()).isFalse();
    }

    // ---- opening ------------------------------------------------------------------------------------

    @Test
    @DisplayName("a second top-level shell is given a window, and the main shell is not")
    void detachesTheSecondTopLevelShell() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        Shell main = openShell();
        Shell detached = openShell();

        assertThat(bridge.created).extracting(w -> w.shell).containsExactly(detached);
        assertThat(bridge.windowedShells())
                .as("the main shell is the Display's own window")
                .containsExactly(detached)
                .doesNotContain(main);
    }

    @Test
    @DisplayName("the client is told which shells it must not draw")
    void namesTheDetachedShellsToTheClient() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();

        assertThat(latestDisplayState(bridge))
                .as("drawn here as well as in its own window, the shell would be on screen twice")
                .contains("\"windowedShellIds\":[" + detached.hashCode() + "]");
    }

    @Test
    @DisplayName("a detached shell is never elected the main shell")
    void detachedShellIsNotElectedMain() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        Shell main = openShell();
        Shell detached = openShell();

        assertThat(bridge.mainShell((DartDisplay) display.getImpl()))
                .as("a shell driving its own window cannot also be slaved to the Display's")
                .isEqualTo(main)
                .isNotEqualTo(detached);
    }

    @Test
    @DisplayName("the main shell keeps driving the Display's window after the first one is detached")
    void mainShellSurvivesADetach() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        Shell main = openShell();
        openShell();

        assertThat(latestDisplayState(bridge))
                .as("the client still has to know which shell fills this window")
                .contains("\"mainShellId\":" + main.hashCode());
    }

    @Test
    @DisplayName("a shell that is not visible yet gets no window")
    void invisibleShellGetsNoWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        TestBridge bridge = install();
        openShell();
        new Shell(display, SWT.SHELL_TRIM); // created, never opened

        assertThat(bridge.created)
                .as("a window for a shell the application has not shown would appear out of nowhere")
                .isEmpty();
    }

    @Test
    @DisplayName("a window is opened once, not once per Display update")
    void windowIsOpenedOnce() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();

        detached.setText("still the same shell");
        detached.setBounds(10, 10, 300, 200);
        pushDisplayUpdate();

        assertThat(bridge.created).hasSize(1);
    }

    @Test
    @DisplayName("a window the surface refuses to open leaves the shell drawn inline")
    void refusedWindowLeavesTheShellInline() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        bridge.refuseToOpen = true;
        openShell();
        Shell detached = openShell();

        assertThat(bridge.windowedShells()).isEmpty();
        assertThat(latestDisplayState(bridge))
                .as("named as windowed with no window to draw it, the shell would exist nowhere")
                .doesNotContain("\"windowedShellIds\":[" + detached.hashCode());
    }

    // ---- closing ------------------------------------------------------------------------------------

    @Test
    @DisplayName("a shell with no trim is drawn inline even under the mode that detaches everything")
    void trimlessShellsStayInline() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        TestBridge bridge = install();
        openShell();

        // A toolkit uses a shell with no trim as a container, not as a window: a splash painted edge
        // to edge, and an off-screen shell a layout parks widgets under. Neither can be honoured in a
        // window of its own, because the OS or the browser frames it regardless -- and the user ends
        // up with a blank window for something that was never meant to be seen.
        Shell splash = new Shell(display, SWT.NO_TRIM);
        splash.setText("Splash");
        splash.setVisible(true);

        Shell parking = new Shell(display, SWT.NONE);
        parking.setBounds(0, 10000, 800, 600);
        parking.setVisible(true);

        assertThat(bridge.created)
                .as("nothing with neither a title, a border nor a close button is a window a user "
                        + "can be given")
                .isEmpty();
        assertThat(bridge.windowedShells()).isEmpty();
    }

    @Test
    @DisplayName("a trimless shell that asks for a window of its own still gets one")
    void trimlessShellCanStillOptIn() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        TestBridge bridge = install();
        openShell();

        Shell chromeless = new Shell(display, SWT.NO_TRIM);
        chromeless.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        chromeless.setVisible(true);

        assertThat(bridge.windowedShells())
                .as("the per-shell override is read before the shape is judged")
                .containsExactly(chromeless);
    }

    @Test
    @DisplayName("hiding a detached shell hides its window, and keeps it")
    void hidingHidesTheWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();
        RecordingWindow window = bridge.created.get(0);

        detached.setVisible(false);

        assertThat(window.visibility)
                .as("a hidden shell must not leave a window on screen")
                .endsWith(false);
        assertThat(window.closed)
                .as("but taking it down would rebuild it on the next show -- a new engine, a lost "
                        + "position, and an endless cycle when a layout flips visibility repeatedly")
                .isFalse();
        assertThat(bridge.windowedShells()).containsExactly(detached);
    }

    @Test
    @DisplayName("disposing a detached shell takes its window down")
    void disposingClosesTheWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();
        RecordingWindow window = bridge.created.get(0);

        detached.dispose();

        assertThat(window.closed).isTrue();
        assertThat(bridge.windowedShells()).isEmpty();
    }

    @Test
    @DisplayName("a shell shown again gets the window it already had")
    void reshowingReopensTheWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();

        detached.setVisible(false);
        detached.setVisible(true);

        assertThat(bridge.created)
                .as("the window it already had, shown again -- not a second one")
                .hasSize(1);
        assertThat(bridge.created.get(0).visibility).endsWith(true);
        assertThat(bridge.windowedShells()).containsExactly(detached);
    }

    @Test
    @DisplayName("a shell whose window went away on its own goes with it")
    void shellFollowsItsLostWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();
        RecordingWindow window = bridge.created.get(0);

        window.alive = false; // the user closed the OS window, or it crashed
        pushDisplayUpdate();
        drainEventLoop();

        assertThat(bridge.windowedShells()).isEmpty();
        assertThat(window.closed)
                .as("closing a window the OS already took down is a call into nothing")
                .isFalse();
        assertThat(detached.isDisposed())
                .as("its window is gone, so the shell has nowhere left to be drawn")
                .isTrue();
    }

    @Test
    @DisplayName("a shell whose window went away is not handed a fresh one")
    void lostWindowIsNotReopened() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        openShell();
        bridge.created.get(0).alive = false;

        pushDisplayUpdate(); // notices the loss, and must not replace it in the same pass
        pushDisplayUpdate();

        assertThat(bridge.created)
                .as("reopening a window the user just closed is the one certainly wrong reaction")
                .hasSize(1);
    }

    @Test
    @DisplayName("disposing the Display takes every extra window down")
    void displayDisposeClosesEveryWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        TestBridge bridge = install();
        openShell();
        openShell();
        openShell();
        List<RecordingWindow> windows = new ArrayList<>(bridge.created);
        assertThat(windows).hasSize(2);

        display.dispose();

        assertThat(windows).allMatch(w -> w.closed, "every window closed with the Display");
    }

    // ---- window operations --------------------------------------------------------------------------

    @Test
    @DisplayName("a detached shell's title, state and geometry drive its own window")
    void windowOperationsReachTheShellsOwnWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();
        RecordingWindow window = bridge.created.get(0);

        detached.setText("Detached");
        detached.setBounds(40, 50, 640, 480);
        detached.setMaximized(true);
        detached.setMaximized(false);

        assertThat(window.title).isEqualTo("Detached");
        assertThat(window.bounds).isEqualTo(new Rectangle(40, 50, 640, 480));
        assertThat(window.states)
                .as("a maximize on a detached shell is a maximize of its window, not of the Display's")
                .containsExactly(ShellWindow.STATE_MAXIMIZED, ShellWindow.STATE_NORMAL);
    }

    @Test
    @DisplayName("an operation on an inline shell reaches no window")
    void inlineShellDrivesNoWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        Shell main = openShell();
        openShell();
        RecordingWindow window = bridge.created.get(0);

        main.setText("Main");
        main.setMaximized(true);

        assertThat(window.title).isNull();
        assertThat(window.states).isEmpty();
    }

    @Test
    @DisplayName("a viewport reported by a detached window sizes its shell, and is not echoed back")
    void reportedViewportSizesTheShellWithoutEchoing() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();
        detached.setBounds(100, 120, 300, 200);
        RecordingWindow window = bridge.created.get(0);
        window.bounds = null;

        ClientReadyPayload p = new ClientReadyPayload();
        p.width = 500;
        p.height = 400;
        p.isFirst = true;
        bridge.comm.fireContaining("Shell/" + detached.hashCode() + "/ClientReady", p);

        assertThat(detached.getBounds())
                .as("the shell is slaved to the window that draws it, keeping where the app put it")
                .isEqualTo(new Rectangle(100, 120, 500, 400));
        assertThat(window.bounds)
                .as("the window already has this size; echoing it back is the resize loop")
                .isNull();
    }

    @Test
    @DisplayName("a detached window's first handshake gets the shell described to it in full")
    void detachedWindowIsSentItsShell() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();
        String channel = "Shell/" + detached.hashCode();
        bridge.comm.sent.clear();

        ClientReadyPayload p = new ClientReadyPayload();
        p.width = 400;
        p.height = 300;
        p.isFirst = true;
        bridge.comm.fireContaining(channel + "/ClientReady", p);
        drainEventLoop();

        // This client is rooted at the shell and never listens on Display/{id}, the one frame a
        // shell is normally delivered inside. Without a description of its own it would render an
        // empty window forever.
        // With the id and the type name in it: this client's only knowledge of the shell is this
        // frame, and it cannot decode one that does not say which widget it is about.
        assertThat(bridge.comm.sent)
                .as("the window that draws this shell has to be given it")
                .anyMatch(f -> f.event.equals(channel)
                        && f.json.contains("\"id\":" + detached.hashCode())
                        && f.json.contains("\"swt\":\"Shell\""));
    }

    @Test
    @DisplayName("a detached window's first handshake gets the configuration too")
    void detachedWindowIsSentTheConfiguration() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        openShell();
        Shell detached = openShell();
        bridge.comm.sent.clear();

        ClientReadyPayload p = new ClientReadyPayload();
        p.width = 400;
        p.height = 300;
        p.isFirst = true;
        bridge.comm.fireContaining("Shell/" + detached.hashCode() + "/ClientReady", p);
        drainEventLoop();

        // Its own client, sent nothing so far. Without the configuration it renders the shell
        // against defaults: the application's theme is lost, and the window chrome it is supposed
        // to draw itself is not drawn at all — while the native side has already hidden the
        // system chrome on the strength of the same setting.
        assertThat(bridge.comm.sent)
                .as("a window with no configuration renders the right widgets the wrong way")
                .anyMatch(f -> f.event.equals("swt.evolve.properties"));
    }

    @Test
    @DisplayName("an application rule detaches the shells it names, and only those")
    void resolverDrivesTheRegistry() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        Shell main = openShell();
        WindowPolicy.setResolver((s, isMain) ->
                s.getText() != null && s.getText().startsWith("Detach") ? Boolean.TRUE : null);

        Shell named = new Shell(main, SWT.DIALOG_TRIM | SWT.RESIZE);
        named.setText("Detach me 1");
        named.setVisible(true);

        Shell other = new Shell(main, SWT.DIALOG_TRIM | SWT.RESIZE);
        other.setText("Keep me 1");
        other.setVisible(true);

        assertThat(bridge.windowedShells())
                .as("the rule outranks the mode, which would have kept a child shell inline")
                .containsExactly(named);
    }

    @Test
    @DisplayName("a rule installed after a shell is open still reaches the next one")
    void resolverAppliesToLaterShells() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        TestBridge bridge = install();
        Shell main = openShell();

        Shell before = new Shell(main, SWT.DIALOG_TRIM);
        before.setText("Detach me 0");
        before.setVisible(true);
        assertThat(bridge.windowedShells()).as("no rule yet").isEmpty();

        WindowPolicy.setResolver((s, isMain) ->
                s.getText() != null && s.getText().startsWith("Detach") ? Boolean.TRUE : null);

        Shell after = new Shell(main, SWT.DIALOG_TRIM);
        after.setText("Detach me 1");
        after.setVisible(true);

        assertThat(bridge.windowedShells())
                .as("the shell opened after the rule was installed is judged by it")
                .contains(after);
    }

    // ---- harness ------------------------------------------------------------------------------------

    private void pushDisplayUpdate() {
        DartDisplay dd = (DartDisplay) display.getImpl();
        dd.displayBridge.sendDisplayUpdate(dd);
    }

    /** Run the queued UI work (a shell close posted from the registry) to completion. */
    private void drainEventLoop() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

    /** A visible top-level shell, which is what makes the policy and the registry consider it. */
    private Shell openShell() {
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setVisible(true);
        return shell;
    }

    private String latestDisplayState(TestBridge bridge) {
        String channel = "Display/" + display.hashCode();
        List<RecordingComm.Frame> frames =
                bridge.comm.sent.stream().filter(f -> f.event.equals(channel)).toList();
        assertThat(frames).as("no Display frame was ever produced").isNotEmpty();
        return frames.get(frames.size() - 1).json;
    }

    private TestBridge install() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestBridge bridge = new TestBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    /** A Display-level bridge whose only surface is a record of what it was asked to do. */
    private static final class TestBridge extends DisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<RecordingWindow> created = new ArrayList<>();
        boolean refuseToOpen;

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

        @Override
        protected boolean supportsShellWindows() {
            return true;
        }

        @Override
        protected ShellWindow createShellWindow(Shell shell) {
            if (refuseToOpen) return null;
            RecordingWindow window = new RecordingWindow(shell);
            created.add(window);
            return window;
        }

        // Widened so the test can assert on the registry and on who is main.
        @Override
        protected java.util.Set<Shell> windowedShells() {
            return super.windowedShells();
        }

        @Override
        protected Shell mainShell(DartDisplay display) {
            return super.mainShell(display);
        }
    }

    /** What a surface was asked to do to one shell's window. */
    private static final class RecordingWindow implements ShellWindow {
        final Shell shell;
        final List<Integer> states = new ArrayList<>();
        final List<Boolean> visibility = new ArrayList<>();
        String title;
        Rectangle bounds;
        boolean closed;
        boolean alive = true;

        RecordingWindow(Shell shell) {
            this.shell = shell;
        }

        @Override
        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public void setBounds(Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        public void setState(int state) {
            states.add(state);
        }

        @Override
        public void setVisible(boolean visible) {
            visibility.add(visible);
        }

        @Override
        public void close() {
            closed = true;
            alive = false;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }
    }
}
