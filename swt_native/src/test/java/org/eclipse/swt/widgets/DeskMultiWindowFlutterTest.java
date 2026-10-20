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
                .isEqualTo(new org.eclipse.swt.graphics.Point(0, 0));
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
                .isEqualTo(new org.eclipse.swt.graphics.Point(0, 0));
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
        welcome.setVisible(false);
        desk.mainWindowBounds.clear();

        // The window was given the shell's own size, so the client reports that less the strip it
        // draws above the shell.
        DartDisplay dd = (DartDisplay) display.getImpl();
        desk.applyClientViewport(dd, new Rectangle(0, 0, 900, 536), null, false);

        assertThat(desk.mainWindowBounds)
                .as("the window grows by the chrome instead of the shell shrinking by it")
                .containsExactly(new Rectangle(120, 120, 900, 600));
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
                .doesNotContain(String.valueOf(modal.hashCode()));
        assertThat(desk.nativeWindowHandle(workbench))
                .as("the Display's client draws the inline modal, so its parent has to be the shell "
                        + "that client fills its window with")
                .isEqualTo(MAIN_WINDOW_VIEW);
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

    // ---- harness ---------------------------------------------------------------------------------------

    /** The windowed shell ids the last Display frame told the client, as sent. */
    private String windowedShellIdsSent(TestDeskBridge desk) {
        String channel = "Display/" + display.hashCode();
        List<RecordingComm.Frame> frames =
                desk.comm.sent.stream().filter(f -> f.event.equals(channel)).toList();
        assertThat(frames).as("no Display frame was ever produced").isNotEmpty();
        java.util.regex.Matcher ids = java.util.regex.Pattern
                .compile("\"windowedShellIds\":\\[([^\\]]*)\\]")
                .matcher(frames.get(frames.size() - 1).json);
        return ids.find() ? ids.group(1) : "";
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
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null; // a stood-in window sits nowhere; only a real one has a screen position
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
