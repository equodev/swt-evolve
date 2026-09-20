package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterNative;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The desktop-native surface with more than one window: what it asks the native bridge for, how the
 * extra windows are driven, and what a close gesture on one of them means.
 *
 * <p>Like {@link ShellCloseContractFlutterTest}, the native windows are stubbed behind the bridge's
 * seams, so the whole round trip is observable in the JVM without an engine.
 */
@Tag("flutter-it")
class DeskMultiWindowFlutterTest {

    private Display display;
    private final Map<String, String> savedProperties = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        for (String key : new String[]{WindowPolicy.MODE_PROPERTY, WindowPolicy.DIALOGS_PROPERTY,
                WindowPolicy.MODAL_PROPERTY, "dev.equo.swt.mode"}) {
            savedProperties.put(key, System.getProperty(key));
        }
        System.clearProperty(WindowPolicy.DIALOGS_PROPERTY);
        System.clearProperty(WindowPolicy.MODAL_PROPERTY);
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
    }

    @AfterEach
    void tearDown() {
        WindowPolicy.setResolver(null);
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        savedProperties.forEach((key, value) -> {
            if (value == null) System.clearProperty(key);
            else System.setProperty(key, value);
        });
        savedProperties.clear();
    }

    // ---- what the native bridge is asked for --------------------------------------------------------

    @Test
    @DisplayName("a detached shell asks for a top-level window rooted at that shell")
    void opensAWindowRootedAtTheShell() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");
        detached.setSize(500, 360);

        assertThat(desk.opened)
                .as("the window hosts a Flutter client rooted at the shell, not at the Display: "
                        + "that is what makes it render this shell and nothing else")
                .hasSize(1);
        OpenedWindow window = desk.opened.get(0);
        assertThat(window.widgetName).isEqualTo("Shell");
        assertThat(window.widgetId).isEqualTo(detached.hashCode());
        assertThat(window.width).isGreaterThan(0);
        assertThat(window.height).isGreaterThan(0);
    }

    @Test
    @DisplayName("the Display's own window is still the one rooted at the Display")
    void theMainWindowIsUnchanged() {
        TestDeskBridge desk = install();
        openShell("Main");

        assertThat(desk.opened)
                .as("the main shell fills the window the Display already has; nothing extra opens")
                .isEmpty();
    }

    @Test
    @DisplayName("each detached shell gets its own window, and each its own engine")
    void severalWindows() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell first = openShell("First");
        Shell second = openShell("Second");
        Shell third = openShell("Third");

        assertThat(desk.opened).extracting(w -> w.widgetId)
                .containsExactly((long) first.hashCode(), (long) second.hashCode(),
                        (long) third.hashCode());
    }

    @Test
    @DisplayName("a detached window's view is handed to its shell")
    void bindsTheViewToTheShell() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");

        assertThat(desk.boundViews)
                .as("a Dart-backed control has no handle of its own, so an application reaching for "
                        + "one would otherwise get an empty handle that swallows what it asks for")
                .containsKey(detached);
    }

    // ---- driving them ---------------------------------------------------------------------------------

    @Test
    @DisplayName("one pass costs one detached pump, however many windows are open")
    void oneDetachedPumpPerPass() {
        TestDeskBridge desk = install();
        openShell("Main");
        openShell("First");
        openShell("Second");
        openShell("Third");
        desk.pumps.clear();

        desk.onUpdate();

        // A pump drains the process-wide queue and then spins the main run loop for a couple of
        // milliseconds so the engines get time. One call gives every window that time, so doing it
        // per window paid the spin again for nothing and made an event-loop turn cost more the more
        // windows were open — the application visibly slowed down as each one opened.
        assertThat(desk.pumps.values().stream().mapToInt(Integer::intValue).sum())
                .as("the cost of a pass must not grow with the number of windows")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("every detached window is pumped in turn, so none is left unheard")
    void everyWindowIsPumpedInTurn() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell first = openShell("First");
        Shell second = openShell("Second");
        Shell third = openShell("Third");
        desk.pumps.clear();

        for (int pass = 0; pass < 3; pass++) desk.onUpdate();

        // Each still has to be asked, because the close status is the one thing that is per-window.
        assertThat(desk.pumps.get(handleOf(desk, first)))
                .as("a window that is never pumped never reports the close the user asked for")
                .isEqualTo(1);
        assertThat(desk.pumps.get(handleOf(desk, second))).isEqualTo(1);
        assertThat(desk.pumps.get(handleOf(desk, third))).isEqualTo(1);
    }

    @Test
    @DisplayName("a detached shell's title and geometry reach its own window")
    void windowOperationsReachTheRightWindow() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");
        long handle = handleOf(desk, detached);

        detached.setText("Renamed");
        detached.setBounds(80, 60, 700, 500);

        assertThat(desk.titles.get(handle)).isEqualTo("Renamed");
        assertThat(desk.bounds.get(handle)).isEqualTo(new Rectangle(80, 60, 700, 500));
    }

    @Test
    @DisplayName("maximizing a detached shell maximizes its window, not the application's")
    void maximizeIsScopedToItsOwnWindow() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell detached = openShell("Detached");

        detached.setMaximized(true);

        assertThat(desk.states.get(handleOf(desk, detached)))
                .containsExactly(ShellWindow.STATE_MAXIMIZED);
        assertThat(desk.mainWindowStates)
                .as("the application's own window must not move when a detached one is maximized")
                .isEmpty();
        assertThat(main.getMaximized()).isFalse();
    }

    // ---- closing --------------------------------------------------------------------------------------

    @Test
    @DisplayName("closing a detached window closes only that shell, and it can be vetoed")
    void closeGestureIsVetoableAndScoped() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell detached = openShell("Detached");
        AtomicInteger closes = new AtomicInteger();
        detached.addListener(SWT.Close, e -> {
            closes.incrementAndGet();
            e.doit = false;
        });

        desk.reportOnNextPump(handleOf(desk, detached), FlutterNative.PUMP_CLOSE_REQUESTED);
        desk.onUpdate();
        drainEventLoop();

        assertThat(closes.get())
                .as("the title-bar close on a detached window is SWT.Close on its shell")
                .isEqualTo(1);
        assertThat(detached.isDisposed())
                .as("doit = false keeps it alive, exactly as on the application's own window")
                .isFalse();
        assertThat(main.isDisposed())
                .as("closing one window must not touch the others")
                .isFalse();
    }

    @Test
    @DisplayName("an accepted close on a detached window disposes its shell and its window")
    void acceptedCloseTearsTheWindowDown() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell detached = openShell("Detached");
        long handle = handleOf(desk, detached);

        desk.reportOnNextPump(handle, FlutterNative.PUMP_CLOSE_REQUESTED);
        desk.onUpdate();
        drainEventLoop();

        assertThat(detached.isDisposed()).isTrue();
        assertThat(desk.disposed)
                .as("the window's own Flutter engine goes with it; a bare close leaves it running")
                .contains(handle);
        assertThat(main.isDisposed()).isFalse();
    }

    @Test
    @DisplayName("a detached window that is already gone takes its shell with it")
    void windowAlreadyGoneDisposesItsShell() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell detached = openShell("Detached");

        desk.reportOnNextPump(handleOf(desk, detached), -1); // crashed, or an OS-forced close
        desk.onUpdate();
        drainEventLoop();

        assertThat(detached.isDisposed())
                .as("there is no window left to render it into, so a veto cannot be honoured")
                .isTrue();
        assertThat(main.isDisposed()).isFalse();
    }

    @Test
    @DisplayName("hiding a detached shell keeps its window, instead of destroying and rebuilding one")
    void hidingAShellDoesNotChurnItsWindow() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");
        assertThat(desk.opened).hasSize(1);

        // A shell hidden and shown again is ordinary during a layout -- the e4 workbench does it to
        // a detached part. Answering that by destroying the window and building a new one loses the
        // window's identity and position, and if the flip repeats it is an endless rebuild: a shell
        // closed this way is not recorded as lost, so the next pass opens yet another window for it.
        detached.setVisible(false);
        desk.onUpdate();
        drainEventLoop();
        detached.setVisible(true);
        desk.onUpdate();
        drainEventLoop();

        assertThat(desk.opened)
                .as("the same window, hidden and shown again -- not a second one")
                .hasSize(1);
        assertThat(desk.disposed)
                .as("nothing was torn down, so nothing has to be rebuilt")
                .isEmpty();
    }

    @Test
    @DisplayName("one window per shell, even when opening one re-enters the update pass")
    void reentrantUpdateOpensOneWindow() {
        TestDeskBridge desk = install();
        openShell("Main");
        // Opening a native window pumps, and a pump can dispatch an update. The shell is not in the
        // map until createShellWindow returns, so a re-entrant pass sees it as windowless and opens
        // a second window, orphaning the first -- which nothing then closes.
        desk.reenterOnNextOpen();
        openShell("Detached");
        drainEventLoop();

        assertThat(desk.opened)
                .as("a shell has one window, whatever re-enters while it is being opened")
                .hasSize(1);
    }

    @Test
    @DisplayName("closing the application's own window does not disturb the detached ones")
    void mainWindowCloseIsScopedToTheMainShell() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell detached = openShell("Detached");
        AtomicInteger detachedCloses = new AtomicInteger();
        detached.addListener(SWT.Close, e -> detachedCloses.incrementAndGet());

        desk.mainPumpStatus.add(FlutterNative.PUMP_CLOSE_REQUESTED);
        desk.onUpdate();
        drainEventLoop();

        assertThat(main.isDisposed()).isTrue();
        assertThat(detachedCloses.get())
                .as("the gesture was on one window; every other shell is untouched by it")
                .isZero();
    }

    @Test
    @DisplayName("disposing the Display shuts every window's engine down")
    void displayDisposeShutsEveryEngine() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell first = openShell("First");
        Shell second = openShell("Second");
        long firstHandle = handleOf(desk, first);
        long secondHandle = handleOf(desk, second);

        display.dispose();

        assertThat(desk.disposed)
                .as("an engine left running crashes the process on teardown")
                .contains(firstHandle, secondHandle);
    }

    // ---- harness ---------------------------------------------------------------------------------------

    private long handleOf(TestDeskBridge desk, Shell shell) {
        return desk.opened.stream()
                .filter(w -> w.widgetId == shell.hashCode())
                .map(w -> w.handle)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no window was opened for " + shell.getText()));
    }

    private Shell openShell(String title) {
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setText(title);
        shell.setVisible(true);
        return shell;
    }

    private void drainEventLoop() {
        while (display.readAndDispatch()) {
            /* drain */
        }
    }

    private TestDeskBridge install() {
        System.setProperty("dev.equo.swt.mode", "desktop");
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestDeskBridge bridge = new TestDeskBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** One {@code FlutterNative.initialize} the bridge asked for. */
    private static final class OpenedWindow {
        final long handle;
        final long widgetId;
        final String widgetName;
        final int width;
        final int height;

        OpenedWindow(long handle, long widgetId, String widgetName, int width, int height) {
            this.handle = handle;
            this.widgetId = widgetId;
            this.widgetName = widgetName;
            this.width = width;
            this.height = height;
        }
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

    /** The desktop bridge with every native call recorded instead of made. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        final List<OpenedWindow> opened = new ArrayList<>();
        final Map<Long, Integer> pumps = new LinkedHashMap<>();
        final Map<Long, String> titles = new LinkedHashMap<>();
        final Map<Long, Rectangle> bounds = new LinkedHashMap<>();
        final Map<Long, List<Integer>> states = new LinkedHashMap<>();
        final Map<Long, List<Boolean>> visibility = new LinkedHashMap<>();
        final Map<Shell, Long> boundViews = new LinkedHashMap<>();
        final List<Long> disposed = new ArrayList<>();
        final List<Integer> mainWindowStates = new ArrayList<>();
        final Deque<Integer> mainPumpStatus = new ArrayDeque<>();

        private final Map<Long, Deque<Integer>> scriptedStatus = new LinkedHashMap<>();
        private long nextHandle = 1000;
        private boolean mainWindowUp = true;

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        void reportOnNextPump(long handle, int status) {
            scriptedStatus.computeIfAbsent(handle, h -> new ArrayDeque<>()).add(status);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display); // no native window, just the comm handshake
        }

        // The Display's own window: up, pumped from a script, and never really disposed.

        @Override
        protected boolean hasWindowSurface() {
            return mainWindowUp;
        }

        @Override
        protected int pumpWindow() {
            Integer status = mainPumpStatus.poll();
            return status == null ? 0 : status;
        }

        @Override
        protected void disposeNativeWindow() {
            mainWindowUp = false;
        }

        @Override
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null; // a stood-in window sits nowhere; only a real one has a screen position
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }

        // The Display's own window has no handle here, so every operation on it is recorded rather
        // than forwarded; anything else falls through to the real detached-window routing.

        @Override
        public void setWindowTitle(DartControl control, String title) {
            if (isMainShellControl(control)) return;
            super.setWindowTitle(control, title);
        }

        @Override
        public void setBounds(DartControl control, Rectangle bounds) {
            if (isMainShellControl(control)) return;
            super.setBounds(control, bounds);
        }

        @Override
        public void setWindowMaximized(DartControl control, boolean maximized) {
            if (isMainShellControl(control)) {
                mainWindowStates.add(maximized ? ShellWindow.STATE_MAXIMIZED : ShellWindow.STATE_NORMAL);
                return;
            }
            super.setWindowMaximized(control, maximized);
        }

        @Override
        public void setWindowMinimized(DartControl control, boolean minimized) {
            if (isMainShellControl(control)) {
                mainWindowStates.add(minimized ? ShellWindow.STATE_MINIMIZED : ShellWindow.STATE_NORMAL);
                return;
            }
            super.setWindowMinimized(control, minimized);
        }

        @Override
        public void setWindowFullScreen(DartControl control, boolean fullScreen) {
            if (isMainShellControl(control)) {
                mainWindowStates.add(fullScreen ? ShellWindow.STATE_FULLSCREEN : ShellWindow.STATE_NORMAL);
                return;
            }
            super.setWindowFullScreen(control, fullScreen);
        }

        private boolean isMainShellControl(DartControl control) {
            return control instanceof DartShell dartShell
                    && isMainShell(forDisplay, (Shell) dartShell.getApi());
        }

        // The detached windows: handed out as fake handles, with every call recorded.

        @Override
        protected long openNativeWindow(long widgetId, String widgetName, int width, int height) {
            long handle = ++nextHandle;
            opened.add(new OpenedWindow(handle, widgetId, widgetName, width, height));
            if (reenterOnOpen) {
                // What the real bridge does here: opening a window pumps, and a pump dispatches
                // whatever is queued -- including another update for this same Display.
                reenterOnOpen = false;
                onUpdate();
            }
            return handle;
        }

        private boolean reenterOnOpen;

        void reenterOnNextOpen() {
            reenterOnOpen = true;
        }

        @Override
        protected void bindShellWindowView(Shell shell, long context) {
            boundViews.put(shell, context);
        }

        @Override
        protected void setShellWindowVisible(long context, boolean visible) {
            visibility.computeIfAbsent(context, c -> new ArrayList<>()).add(visible);
        }

        @Override
        protected int pumpShellWindow(long context) {
            pumps.merge(context, 1, Integer::sum);
            Deque<Integer> scripted = scriptedStatus.get(context);
            Integer status = scripted == null ? null : scripted.poll();
            return status == null ? 0 : status;
        }

        @Override
        protected void disposeShellWindow(long context) {
            disposed.add(context);
        }

        @Override
        protected void setShellWindowTitle(long context, String title) {
            titles.put(context, title);
        }

        @Override
        protected void setShellWindowBounds(long context, Rectangle b) {
            bounds.put(context, new Rectangle(b.x, b.y, b.width, b.height));
        }

        @Override
        protected void setShellWindowState(long context, int state) {
            states.computeIfAbsent(context, c -> new ArrayList<>()).add(state);
        }
    }
}
