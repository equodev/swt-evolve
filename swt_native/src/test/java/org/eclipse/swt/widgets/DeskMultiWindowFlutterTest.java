package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.IntegerOn150;
import dev.equo.swt.FlutterNative;
import dev.equo.swt.ShellWindow;
import dev.equo.swt.WindowPolicy;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Point;
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
import java.util.function.Consumer;

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
    @DisplayName("a detached window opens at its shell's size in the window system's pixels")
    void opensAtTheShellsSizeInWindowPixels() {
        TestDeskBridge desk = install();
        // A 600-unit shell is drawn in 400 logical pixels.
        IntegerOn150.run(() -> {
            openShell("Main");
            Shell detached = new Shell(display, SWT.SHELL_TRIM);
            detached.setBounds(300, 150, 600, 450);
            detached.setVisible(true);

            OpenedWindow window = desk.opened.get(0);
            assertThat(window.width).isEqualTo(400);
            assertThat(window.height).isEqualTo(300);
        });
    }

    @Test
    @DisplayName("a detached window that reports while it is still opening is sent its configuration")
    void answersAReportMadeWhileTheWindowOpens() {
        TestDeskBridge desk = install();
        desk.clientReportsWhileOpening();
        openShell("Main");
        Shell detached = openShell("Detached");

        assertThat(desk.unansweredReports)
                .as("the client's first report arrives before the engine has finished starting; "
                        + "nobody listening then means it is never sent its scale or window chrome")
                .isEmpty();
        assertThat(desk.comm.sent).extracting(f -> f.event)
                .contains("swt.evolve.properties", "Shell/" + detached.hashCode());
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

    // ---- which window a shell is drawn into -----------------------------------------------------------
    //
    // A window the application opens on its own (a Swing dialog from embedded content) is owned by
    // the native window it is given; that ownership is what keeps it above the application window.

    private static final long MAIN_WINDOW_VIEW = 0x4708B6L;

    /** The Display's own window is never opened here, so its context stays 0. */
    private static final long MAIN_WINDOW_CONTEXT = 0L;

    @Test
    @DisplayName("the main shell is drawn into the Display's window")
    void mainShellIsInTheDisplayWindow() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");

        assertThat(desk.nativeWindowHandle(main)).isEqualTo(MAIN_WINDOW_VIEW);
    }

    @Test
    @DisplayName("a detached shell is drawn into its own window")
    void detachedShellIsInItsOwnWindow() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");

        assertThat(desk.nativeWindowHandle(detached)).isEqualTo(desk.opened.get(0).handle);
    }

    @Test
    @DisplayName("a shell drawn inside a detached one is in that one's window")
    void childOfDetachedShellIsInTheParentWindow() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");
        Shell child = new Shell(detached, SWT.SHELL_TRIM);
        child.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        child.setVisible(true);

        assertThat(desk.nativeWindowHandle(child)).isEqualTo(desk.opened.get(0).handle);
    }

    @Test
    @DisplayName("a shell drawn inside the Display's window is in that window")
    void shellInsideTheMainWindowIsInIt() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        dialog.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        dialog.setVisible(true);

        assertThat(desk.nativeWindowHandle(dialog)).isEqualTo(MAIN_WINDOW_VIEW);
    }

    @Test
    @DisplayName("with no native window there is nothing to own a window by")
    void noWindowNoHandle() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        desk.disposeNativeWindow();

        assertThat(desk.nativeWindowHandle(main)).isZero();
    }

    // ---- which window owns a detached one ------------------------------------------------------------
    //
    // A shell with a parent is an owned window natively: it stays above its owner, minimizes with it
    // and has no task-switcher entry of its own. The owner is the window its parent is drawn into.

    /** Stands in for the Display's own window, so being owned by it is distinguishable from none. */
    private static final long MAIN_WINDOW_OWNER = 0x5A11L;

    @Test
    @DisplayName("a parentless detached shell's window has no owner")
    void topLevelShellIsUnowned() {
        TestDeskBridge desk = install();
        openShell("Main");
        openShell("Detached");

        assertThat(desk.opened.get(0).owner).isZero();
    }

    @Test
    @DisplayName("a detached child of the main shell is owned by the Display's window")
    void childOfMainShellIsOwnedByTheDisplayWindow() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell child = new Shell(main, SWT.SHELL_TRIM);
        child.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        child.setVisible(true);

        assertThat(desk.opened).hasSize(1);
        assertThat(desk.opened.get(0).owner).isEqualTo(MAIN_WINDOW_OWNER);
    }

    @Test
    @DisplayName("a detached child of a detached shell is owned by that shell's window")
    void childOfDetachedShellIsOwnedByItsWindow() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");
        Shell child = new Shell(detached, SWT.SHELL_TRIM);
        child.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        child.setVisible(true);

        assertThat(desk.opened).hasSize(2);
        assertThat(desk.opened.get(1).owner).isEqualTo(desk.opened.get(0).handle);
    }

    @Test
    @DisplayName("a detached child of an inline shell is owned by the window that inline shell is in")
    void childOfInlineShellIsOwnedByTheWindowItIsDrawnInto() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell inline = new Shell(main, SWT.DIALOG_TRIM);
        inline.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        inline.setVisible(true);
        Shell child = new Shell(inline, SWT.SHELL_TRIM);
        child.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        child.setVisible(true);

        assertThat(desk.opened).hasSize(1);
        assertThat(desk.opened.get(0).owner).isEqualTo(MAIN_WINDOW_OWNER);
    }

    @Test
    @DisplayName("a detached child is re-owned once its parent gets a window of its own")
    void childIsReownedWhenItsParentGetsAWindow() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell parent = new Shell(main, SWT.SHELL_TRIM);
        parent.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        parent.setVisible(true);
        Shell child = new Shell(parent, SWT.SHELL_TRIM);
        child.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        child.setVisible(true);
        long childWindow = handleOf(desk, child);

        parent.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        parent.setVisible(false);
        parent.setVisible(true);
        child.setVisible(false);
        child.setVisible(true);

        assertThat(desk.owners.get(childWindow)).containsExactly(handleOf(desk, parent));
    }

    @Test
    @DisplayName("a detached child is re-owned by the next window up once its parent's window closes")
    void childIsReownedWhenItsParentLosesItsWindow() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        Shell parent = new Shell(main, SWT.SHELL_TRIM);
        parent.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        parent.setVisible(true);
        Shell child = new Shell(parent, SWT.SHELL_TRIM);
        child.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        child.setVisible(true);
        long childWindow = handleOf(desk, child);

        parent.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        parent.setVisible(false);
        parent.setVisible(true);
        child.setVisible(false);
        child.setVisible(true);

        assertThat(desk.owners.get(childWindow)).containsExactly(MAIN_WINDOW_OWNER);
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
    @DisplayName("a detached shell without text gets a window without a title")
    void untitledShellGetsAnUntitledWindow() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = new Shell(display, SWT.SHELL_TRIM);
        detached.setVisible(true);

        assertThat(desk.titles.get(handleOf(desk, detached)))
                .as("the window is created under the client's widget name; the shell's empty text replaces it")
                .isEmpty();
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

    @Test
    @DisplayName("a shell maximized before it opens gets a maximized window, not a screen-sized one")
    void maximizedBeforeOpenOpensMaximized() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = new Shell(display, SWT.SHELL_TRIM);
        detached.setMaximized(true);
        detached.setVisible(true);
        long handle = handleOf(desk, detached);

        assertThat(desk.states.get(handle)).containsExactly(ShellWindow.STATE_MAXIMIZED);
        assertThat(detached.getLocation())
                .as("a maximized shell sits at the screen origin; it is not cascaded like an unplaced one")
                .isEqualTo(new Point(0, 0));
    }

    @Test
    @DisplayName("a shell maximized before it opens restores to the bounds it had, not to the screen")
    void maximizedBeforeOpenRestoresToItsBounds() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = new Shell(display, SWT.SHELL_TRIM);
        detached.setBounds(100, 120, 800, 500);
        detached.setMaximized(true);
        detached.setVisible(true);
        long handle = handleOf(desk, detached);

        assertThat(desk.bounds.get(handle)).isEqualTo(new Rectangle(100, 120, 800, 500));
        assertThat(desk.states.get(handle)).containsExactly(ShellWindow.STATE_MAXIMIZED);
    }

    @Test
    @DisplayName("a shell made full-screen before it opens gets a full-screen window")
    void fullScreenBeforeOpenOpensFullScreen() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = new Shell(display, SWT.SHELL_TRIM);
        detached.setFullScreen(true);
        detached.setVisible(true);

        assertThat(desk.states.get(handleOf(desk, detached))).containsExactly(ShellWindow.STATE_FULLSCREEN);
        assertThat(detached.getLocation())
                .as("a full-screen shell is not moved to a cascaded spot")
                .isEqualTo(new Point(0, 0));
    }

    @Test
    @DisplayName("a shell minimized before it opens gets a minimized window")
    void minimizedBeforeOpenOpensMinimized() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = new Shell(display, SWT.SHELL_TRIM);
        detached.setMinimized(true);
        detached.setVisible(true);

        assertThat(desk.states.get(handleOf(desk, detached))).containsExactly(ShellWindow.STATE_MINIMIZED);
    }

    @Test
    @DisplayName("a shell opened in the normal state is not told a state at all")
    void normalShellGetsNoState() {
        TestDeskBridge desk = install();
        openShell("Main");
        Shell detached = openShell("Detached");

        assertThat(desk.states.get(handleOf(desk, detached))).isNull();
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

    // ---- hiding the shell that holds the Display's window ---------------------------------------------

    /** The flags an application runs with when every shell but its modals gets a window. */
    private void everyShellButModalsInItsOwnWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        System.setProperty(WindowPolicy.MODAL_PROPERTY, "inline");
    }

    @Test
    @DisplayName("hiding the main shell hands the Display's window to the visible top-level")
    void hidingTheMainShellHandsItsWindowOver() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        Shell welcome = openShell("Welcome");
        Shell workbench = openShell("Workbench");
        long workbenchWindow = handleOf(desk, workbench);

        welcome.setVisible(false);

        assertThat(desk.hostsAsMainShell(workbench))
                .as("a hidden shell must not keep the window: it would stay on screen with nothing "
                        + "of its own in it, and everything drawn inline would land there")
                .isTrue();
        assertThat(desk.disposed)
                .as("the shell is now drawn in the Display's window, so the one it had goes")
                .contains(workbenchWindow);
        assertThat(desk.nativeWindowHandle(workbench)).isEqualTo(MAIN_WINDOW_VIEW);
        assertThat(desk.titles.get(MAIN_WINDOW_CONTEXT))
                .as("the window now shows Workbench, so it carries Workbench's title")
                .isEqualTo("Workbench");
        assertThat(desk.mainWindowBounds).last().isEqualTo(workbench.getBounds());
        assertThat(desk.mainWindowVisibility())
                .as("the window still has a shell to show")
                .isEmpty();
    }

    @Test
    @DisplayName("hiding the only shell in the Display's window hides the window, and showing it back shows it")
    void hidingTheMainShellHidesItsWindow() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        Shell welcome = openShell("Welcome");
        Shell child = new Shell(welcome, SWT.SHELL_TRIM);
        child.setVisible(true);
        assertThat(desk.windowedShells()).contains(child);

        welcome.setVisible(false);
        assertThat(desk.mainWindowVisibility())
                .as("nothing is drawn in it any more; left up, it shows a stale frame")
                .containsExactly(false);

        welcome.setVisible(true);
        assertThat(desk.mainWindowVisibility()).containsExactly(false, true);
    }

    @Test
    @DisplayName("a main shell that has not been shown yet does not hide the Display's window")
    void startupDoesNotHideTheWindow() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        Shell splash = new Shell(display, SWT.NO_TRIM);
        splash.setVisible(true);
        Shell workbench = new Shell(display, SWT.SHELL_TRIM);
        splash.dispose();
        workbench.setVisible(true);

        assertThat(desk.mainWindowVisibility())
                .as("a splash disposed before the real shell opens is no reason to flash the window")
                .isEmpty();
        assertThat(desk.hostsAsMainShell(workbench)).isTrue();
    }

    @Test
    @DisplayName("taking the window over keeps the shell's size, growing the window by its chrome")
    void takeoverKeepsTheShellSize() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        Shell welcome = openShell("Welcome");
        Shell workbench = openShell("Workbench");
        workbench.setBounds(120, 120, 900, 568);
        desk.applyClientInsetTop(32);
        desk.mainWindowBounds.clear();

        welcome.setVisible(false);

        assertThat(desk.mainWindowBounds)
                .as("the window grows by the strip the client draws above the shell instead of the "
                        + "shell shrinking by it, and starts that much higher")
                .containsExactly(new Rectangle(120, 88, 900, 600));
        assertThat(workbench.getSize().y).isEqualTo(568);
    }

    @Test
    @DisplayName("the strip the client draws above the shell grows its window upwards by its height")
    void reportedInsetGrowsTheWindowAboveTheShell() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(100, 100, 800, 500);
        desk.mainWindowBounds.clear();

        desk.applyClientInsetTop(30);

        assertThat(desk.mainWindowBounds)
                .as("the shell is the area below the strip, so a shell placed before the strip was known "
                        + "gets a window that much taller, starting that much higher")
                .containsExactly(new Rectangle(100, 70, 800, 530));
    }

    @Test
    @DisplayName("an inline modal is drawn in the window of the shell it belongs to")
    void inlineModalFollowsItsParentsWindow() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        Shell welcome = openShell("Welcome");
        Shell workbench = openShell("Workbench");
        welcome.setVisible(false);

        Shell modal = new Shell(workbench, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        modal.setSize(400, 200);
        modal.setVisible(true);

        assertThat(desk.windowedShells())
                .as("a modal is drawn inline under these flags")
                .doesNotContain(modal);
        assertThat(windowedShellIdsSent(desk))
                .as("the client draws the modal itself, so it must not be told the modal is windowed")
                .doesNotContain((long) modal.hashCode());
        assertThat(desk.nativeWindowHandle(workbench))
                .as("the Display's client draws the inline modal, so its parent has to be the shell "
                        + "that client fills its window with")
                .isEqualTo(MAIN_WINDOW_VIEW);
    }

    @Test
    @DisplayName("closing a shell that took the window over hands it to the next top-level")
    void disposingTheTakerHandsTheWindowOn() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        Shell welcome = openShell("Welcome");
        Shell workbench = openShell("Workbench");
        welcome.setVisible(false);
        clientReportsViewport(desk, 1728, 1200);
        Shell second = openShell("Second workbench window");
        second.setBounds(300, 200, 700, 500);
        long secondWindow = handleOf(desk, second);
        desk.mainWindowBounds.clear();

        workbench.dispose();
        desk.onUpdate();
        drainEventLoop();

        assertThat(desk.hostsAsMainShell(second))
                .as("the only top-level left is drawn in the Display's window")
                .isTrue();
        assertThat(desk.disposed)
                .as("so the window it had goes, instead of standing next to the Display's")
                .contains(secondWindow);
        assertThat(desk.mainWindowBounds)
                .as("and the Display's window moves to where it was, instead of staying where the "
                        + "closed shell was, with nothing drawn in it")
                .containsExactly(new Rectangle(300, 200, 700, 500));
        assertThat(desk.titles.get(MAIN_WINDOW_CONTEXT)).isEqualTo("Second workbench window");
        assertThat(second.getSize())
                .as("never sized to the Display's window while still in its own")
                .isEqualTo(new Point(700, 500));
    }

    @Test
    @DisplayName("a top-level opened after the main shell was hidden gets a window of its own")
    void laterTopLevelGetsItsOwnWindow() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        Shell welcome = openShell("Welcome");
        openShell("Workbench");
        welcome.setVisible(false);

        Shell late = openShell("Late top-level");

        assertThat(desk.hostsAsMainShell(late)).isFalse();
        assertThat(desk.windowedShells()).contains(late);
    }

    @Test
    @DisplayName("a shell in a window of its own maps to the screen from its bounds, with no title bar")
    void windowedShellMapsWithoutATitleBar() {
        everyShellButModalsInItsOwnWindow();
        TestDeskBridge desk = install();
        openShell("Workbench");
        Shell second = openShell("Second workbench window");
        assertThat(desk.windowedShells()).contains(second);
        second.setBounds(1760, 332, 1100, 868);

        assertThat(second.toDisplay(0, 0))
                .as("its window draws the shell without a Flutter title bar, so a point mapped through "
                        + "one lands that much below what is painted")
                .isEqualTo(new Point(1760, 332));
    }

    // ---- a drag that leaves its window ---------------------------------------------------------------

    @Test
    @DisplayName("a drag over another window is relayed to the shell under the pointer, in its own coordinates")
    void dragOverAnotherWindowReachesItsShell() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        dragSource(source, new ArrayList<>());

        startDrag(source);
        dragMove(desk, source, 130, 250);

        assertThat(remoteDrags(desk, main))
                .as("the main window's client is the only one that can find the drop target there")
                .containsExactly("over@130,250");
    }

    @Test
    @DisplayName("a drag dropped on another window's DropTarget reaches it with the source's data")
    void dropOnAnotherWindowsDropTarget() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Composite inventory = new Composite(main, SWT.NONE);
        List<String> targetEvents = new ArrayList<>();
        DropTarget target = new DropTarget(inventory, DND.DROP_MOVE | DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                targetEvents.add("dragEnter");
            }

            @Override
            public void drop(DropTargetEvent event) {
                targetEvents.add("drop:" + event.data);
            }
        });
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        List<String> sourceEvents = new ArrayList<>();
        dragSource(source, sourceEvents);

        startDrag(source);
        dragMove(desk, source, 130, 250);
        targetClientSends(desk, target, "dragEnter");
        dragRelease(desk, source, 140, 260);

        assertThat(remoteDrags(desk, main)).containsExactly("over@130,250", "over@140,260", "drop@140,260");
        assertThat(sourceEvents).as("the other window has not dropped yet").containsExactly("dragStart");
        targetClientSends(desk, target, "drop");
        assertThat(targetEvents).containsExactly("dragEnter", "drop:row");
        assertThat(sourceEvents).containsExactly("dragStart", "dragEnd:" + DND.DROP_MOVE);
    }

    @Test
    @DisplayName("a drag dropped on another window ends once, with the drop's operation, whatever its client says after")
    void aCrossWindowDropEndsOnce() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        DropTarget target = new DropTarget(new Composite(main, SWT.NONE), DND.DROP_MOVE | DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        List<String> sourceEvents = new ArrayList<>();
        DragSource drag = dragSource(source, sourceEvents);

        startDrag(source);
        dragMove(desk, source, 130, 250);
        targetClientSends(desk, target, "dragEnter");
        dragRelease(desk, source, 140, 260);
        targetClientSends(desk, target, "drop");
        sourceClientEndsDrag(desk, drag);

        assertThat(sourceEvents).containsExactly("dragStart", "dragEnd:" + DND.DROP_MOVE);
    }

    @Test
    @DisplayName("a drag released over no window ends once, with nothing dropped, whatever its client says after")
    void aDragDroppedOutsideEveryWindowEndsOnce() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        List<String> sourceEvents = new ArrayList<>();
        DragSource drag = dragSource(source, sourceEvents);

        startDrag(source);
        dragMove(desk, source, 1000, 200);
        dragRelease(desk, source, 1000, 200);
        sourceClientEndsDrag(desk, drag);

        assertThat(sourceEvents).containsExactly("dragStart", "dragEnd:" + DND.DROP_NONE);
    }

    @Test
    @DisplayName("a drop another window finds no DropTarget for ends once, whatever the source's client says after")
    void aRemoteDropNoneEndsOnce() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        List<String> sourceEvents = new ArrayList<>();
        DragSource drag = dragSource(source, sourceEvents);

        startDrag(source);
        dragRelease(desk, source, 130, 250);
        inbound(desk, FlutterBridge.event(main.getImpl(), "DnD", "remoteDropNone"), pointAt(0, 0));
        sourceClientEndsDrag(desk, drag);

        assertThat(sourceEvents).containsExactly("dragStart", "dragEnd:" + DND.DROP_NONE);
    }

    @Test
    @DisplayName("a drag that returns to its own window leaves the shell it was over, even where that window covers it")
    void dragBackIntoItsWindowLeaves() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Shell dialog = openShell("Add Data");
        dialog.setBounds(600, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        dragSource(source, new ArrayList<>());

        startDrag(source);
        dragMove(desk, source, 130, 250);
        dragMove(desk, source, 700, 200);

        assertThat(remoteDrags(desk, main)).containsExactly("over@130,250", "leave@0,0");
    }

    @Test
    @DisplayName("a drag released over no other window ends with nothing dropped")
    void releaseOverNoOtherWindowEndsTheDrag() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        List<String> sourceEvents = new ArrayList<>();
        dragSource(source, sourceEvents);

        startDrag(source);
        dragRelease(desk, source, 1000, 200);

        assertThat(remoteDrags(desk, main)).isEmpty();
        assertThat(sourceEvents).containsExactly("dragStart", "dragEnd:" + DND.DROP_NONE);
    }

    @Test
    @DisplayName("a drop the other window finds no DropTarget for ends the drag with nothing dropped")
    void dropWithNoTargetThereEndsTheDrag() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        List<String> sourceEvents = new ArrayList<>();
        dragSource(source, sourceEvents);

        startDrag(source);
        dragRelease(desk, source, 130, 250);
        assertThat(sourceEvents).as("the other window has not answered yet").containsExactly("dragStart");
        inbound(desk, FlutterBridge.event(main.getImpl(), "DnD", "remoteDropNone"), pointAt(0, 0));

        assertThat(sourceEvents).containsExactly("dragStart", "dragEnd:" + DND.DROP_NONE);
    }

    @Test
    @DisplayName("the shell a drag was dropped on stops listening for its answer once the drag ends")
    void aFinishedDragLeavesNoDropAnswerHandler() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        DropTarget target = new DropTarget(new Composite(main, SWT.NONE), DND.DROP_MOVE | DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        List<String> sourceEvents = new ArrayList<>();
        dragSource(source, sourceEvents);
        String dropNone = FlutterBridge.event(main.getImpl(), "DnD", "remoteDropNone");

        startDrag(source);
        dragRelease(desk, source, 130, 250);
        assertThat(desk.comm.handlers).containsKey(dropNone);
        inbound(desk, dropNone, pointAt(0, 0));
        assertThat(desk.comm.handlers).as("answered").doesNotContainKey(dropNone);

        startDrag(source);
        dragMove(desk, source, 130, 250);
        targetClientSends(desk, target, "dragEnter");
        dragRelease(desk, source, 140, 260);
        assertThat(desk.comm.handlers).containsKey(dropNone);
        targetClientSends(desk, target, "drop");
        assertThat(sourceEvents).containsExactly(
                "dragStart", "dragEnd:" + DND.DROP_NONE, "dragStart", "dragEnd:" + DND.DROP_MOVE);
        assertThat(desk.comm.handlers).as("a DropTarget took the drop").doesNotContainKey(dropNone);
    }

    @Test
    @DisplayName("what another window's DropTarget answers reaches the window the drag started in, which draws the cursor")
    void remoteAnswerReachesTheSource() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Composite inventory = new Composite(main, SWT.NONE);
        DropTarget target = new DropTarget(inventory, DND.DROP_MOVE | DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_COPY;
            }
        });
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        dragSource(source, new ArrayList<>());

        startDrag(source);
        dragMove(desk, source, 130, 250);
        targetClientSends(desk, target, "dragEnter");
        dragMove(desk, source, 850, 50);

        assertThat(remoteAnswers(desk, source))
                .as("none until the DropTarget answers, then its answer, then none again over no window")
                .containsExactly(DND.DROP_NONE, DND.DROP_COPY, DND.DROP_NONE);
    }

    @Test
    @DisplayName("the drag image goes with the first move over each other window, which draws it")
    void dragImageGoesToTheWindowUnderThePointer() {
        TestDeskBridge desk = install();
        Shell main = openShell("Main");
        main.setBounds(0, 0, 800, 600);
        Shell dialog = openShell("Add Data");
        dialog.setBounds(900, 100, 400, 300);
        Composite source = new Composite(dialog, SWT.NONE);
        dragSource(source, new ArrayList<>());

        startDrag(source);
        Event image = new Event();
        image.segments = new int[]{137, 80, 78, 71};
        image.width = 40;
        image.height = 20;
        inbound(desk, FlutterBridge.event(source.getImpl(), "DragDetect", "dragImage"), image);
        dragMove(desk, source, 130, 250);
        dragMove(desk, source, 140, 260);
        dragMove(desk, source, 850, 50);
        dragMove(desk, source, 150, 270);

        String channel = FlutterBridge.eventName(main.getImpl(), "DnD/remoteDrag");
        List<Boolean> carriesImage = new ArrayList<>();
        for (RecordingComm.Frame frame : desk.comm.sent) {
            if (!frame.event.equals(channel) || !"over".equals(frame.state().get("text"))) continue;
            // Read back from the bytes sent: the PNG travels as bytes, not as a list of numbers.
            Object png = ((Map<?, ?>) dev.equo.swt.comm.TaggedValues.decode(frame.bytes, 0, frame.bytes.length))
                    .get("segments");
            carriesImage.add(png instanceof byte[]
                    && java.util.Arrays.equals((byte[]) png, new byte[]{(byte) 137, 80, 78, 71})
                    && Long.valueOf(40).equals(((Number) frame.state().get("width")).longValue()));
        }
        assertThat(carriesImage).containsExactly(true, false, true);
    }

    private List<Integer> remoteAnswers(TestDeskBridge desk, Control source) {
        String channel = FlutterBridge.eventName(source.getImpl(), "DragDetect/remoteAnswer");
        List<Integer> answers = new ArrayList<>();
        for (RecordingComm.Frame frame : desk.comm.sent) {
            if (!frame.event.equals(channel)) continue;
            Object sent = frame.state().get("detail");
            answers.add(sent instanceof Number ? ((Number) sent).intValue() : DND.DROP_NONE);
        }
        return answers;
    }

    private DragSource dragSource(Control control, List<String> events) {
        DragSource drag = new DragSource(control, DND.DROP_MOVE | DND.DROP_COPY);
        drag.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        drag.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent event) {
                events.add("dragStart");
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "row";
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                events.add("dragEnd:" + event.detail);
            }
        });
        return drag;
    }

    private void startDrag(Control control) {
        control.notifyListeners(SWT.DragDetect, new Event());
    }

    private void dragMove(TestDeskBridge desk, Control source, int x, int y) {
        inbound(desk, FlutterBridge.event(source.getImpl(), "DragDetect", "dragMove"), pointAt(x, y));
    }

    private void dragRelease(TestDeskBridge desk, Control source, int x, int y) {
        inbound(desk, FlutterBridge.event(source.getImpl(), "DragDetect", "dragRelease"), pointAt(x, y));
    }

    /** The source's client reports its drag over, as it does for every drag that ends there. */
    private void sourceClientEndsDrag(TestDeskBridge desk, DragSource drag) {
        inbound(desk, FlutterBridge.event(drag.getImpl(), "Drag", "dragFinished"), new Event());
    }

    /** What the window drawing {@code target} sends once it has found it under a relayed drag. */
    private void targetClientSends(TestDeskBridge desk, DropTarget target, String event) {
        inbound(desk, FlutterBridge.event(target.getImpl(), "Drop", event), pointAt(20, 30));
    }

    private static Event pointAt(int x, int y) {
        Event e = new Event();
        e.x = x;
        e.y = y;
        return e;
    }

    @SuppressWarnings("unchecked")
    private void inbound(TestDeskBridge desk, String channel, Event event) {
        Consumer<Event> handler = (Consumer<Event>) desk.comm.handlers.get(channel);
        assertThat(handler).as("nothing listens on " + channel).isNotNull();
        handler.accept(event);
        drainEventLoop();
    }

    /** The relayed drag phases {@code shell}'s client was sent, as {@code phase@x,y}. */
    private List<String> remoteDrags(TestDeskBridge desk, Shell shell) {
        String channel = FlutterBridge.eventName(shell.getImpl(), "DnD/remoteDrag");
        List<String> phases = new ArrayList<>();
        for (RecordingComm.Frame frame : desk.comm.sent) {
            if (!frame.event.equals(channel)) continue;
            Map<String, Object> values = frame.state();
            phases.add(values.get("text") + "@" + values.getOrDefault("x", 0) + "," + values.getOrDefault("y", 0));
        }
        return phases;
    }

    // ---- harness ---------------------------------------------------------------------------------------

    /** The windowed shell ids the last Display frame told the client, as sent. */
    private List<Long> windowedShellIdsSent(TestDeskBridge desk) {
        String channel = "Display/" + display.hashCode();
        List<RecordingComm.Frame> frames =
                desk.comm.sent.stream().filter(f -> f.event.equals(channel)).toList();
        assertThat(frames).as("no Display frame was ever produced").isNotEmpty();
        Object ids = frames.get(frames.size() - 1).state().get("windowedShellIds");
        if (!(ids instanceof List)) return List.of();
        return ((List<?>) ids).stream().map(id -> ((Number) id).longValue()).toList();
    }

    /** The Display's window reports its viewport, as its client does once it has drawn. */
    private void clientReportsViewport(TestDeskBridge desk, int width, int height) {
        ClientReadyPayload report = new ClientReadyPayload();
        report.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
        report.width = width;
        report.height = height;
        @SuppressWarnings("unchecked")
        Consumer<ClientReadyPayload> handler = (Consumer<ClientReadyPayload>)
                desk.comm.handlers.get("Display/" + display.hashCode() + "/ClientReady");
        handler.accept(report);
    }

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
        final long owner;
        final long widgetId;
        final String widgetName;
        final int width;
        final int height;

        OpenedWindow(long handle, long owner, long widgetId, String widgetName, int width, int height) {
            this.handle = handle;
            this.owner = owner;
            this.widgetId = widgetId;
            this.widgetName = widgetName;
            this.width = width;
            this.height = height;
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
        final Map<Long, List<Long>> owners = new LinkedHashMap<>();
        final Map<Shell, Long> boundViews = new LinkedHashMap<>();
        final List<Long> disposed = new ArrayList<>();
        final List<Integer> mainWindowStates = new ArrayList<>();
        final List<Rectangle> mainWindowBounds = new ArrayList<>();
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
        protected Point shellWindowOrigin(long context) {
            return null; // a stood-in window sits nowhere; only a real one has a screen position
        }

        @Override
        protected int workAreaTop(Rectangle window) {
            return 0;
        }

        @Override
        protected void wakeNativeWindow() {
            // a stood-in window has no handle to post into
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
            mainWindowBounds.add(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
        }

        /** Shows and hides of the Display's own window, recorded under its context. */
        List<Boolean> mainWindowVisibility() {
            return visibility.getOrDefault(MAIN_WINDOW_CONTEXT, List.of());
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
        protected long openNativeWindow(long owner, long widgetId, String widgetName, int width, int height) {
            long handle = ++nextHandle;
            opened.add(new OpenedWindow(handle, owner, widgetId, widgetName, width, height));
            if (clientReportsWhileOpening) {
                // The client runs on its own thread and reports as soon as its view has metrics,
                // which happens while the engine is still being created here.
                Shell opening = null;
                for (Shell s : forDisplay._shells()) if (s != null && s.hashCode() == widgetId) opening = s;
                String channel = "Shell/" + opening.hashCode() + "/ClientReady";
                @SuppressWarnings("unchecked")
                Consumer<ClientReadyPayload> handler = (Consumer<ClientReadyPayload>) comm.handlers.get(channel);
                if (handler == null) {
                    unansweredReports.add(channel);
                } else {
                    ClientReadyPayload report = new ClientReadyPayload();
                    report.wireFingerprint = dev.equo.swt.WireTable.FINGERPRINT;
                    report.width = width;
                    report.height = height;
                    report.isFirst = true;
                    handler.accept(report);
                }
            }
            if (reenterOnOpen) {
                // What the real bridge does here: opening a window pumps, and a pump dispatches
                // whatever is queued -- including another update for this same Display.
                reenterOnOpen = false;
                onUpdate();
            }
            return handle;
        }

        private boolean reenterOnOpen;

        private boolean clientReportsWhileOpening;
        final List<String> unansweredReports = new ArrayList<>();

        void clientReportsWhileOpening() {
            clientReportsWhileOpening = true;
        }

        void reenterOnNextOpen() {
            reenterOnOpen = true;
        }

        @Override
        protected void bindShellWindowView(Shell shell, long context) {
            boundViews.put(shell, context);
        }

        // A window's view stands in as its handle; the Display's own is a fixed one.

        @Override
        protected long shellWindowView(long context) {
            return context;
        }

        @Override
        protected long mainWindowView() {
            return MAIN_WINDOW_VIEW;
        }

        @Override
        protected long mainWindowContext() {
            return MAIN_WINDOW_OWNER;
        }

        @Override
        protected void setShellWindowOwner(long context, long owner) {
            owners.computeIfAbsent(context, c -> new ArrayList<>()).add(owner);
        }

        @Override
        protected void setShellWindowVisible(long context, boolean visible) {
            visibility.computeIfAbsent(context, c -> new ArrayList<>()).add(visible);
        }

        // A fake handle must never reach the runner: with it loaded in the same JVM, native code
        // would read it as a window. True is what the bridge answers when it cannot ask.
        @Override
        protected boolean windowActive(long context) {
            return true;
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
