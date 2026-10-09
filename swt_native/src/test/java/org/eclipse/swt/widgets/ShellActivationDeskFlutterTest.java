package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins that a shell on the desktop-native surface is told it is active exactly when
 * {@code Display.getActiveShell()} says so: both follow the native window the OS makes key.
 *
 * <p>Natively a shell receives {@code SWT.Activate} when its window becomes the key window and
 * {@code SWT.Deactivate} when it stops being one, and {@code getActiveShell()} answers with that same
 * window, so the two never disagree: an application that is not frontmost gets no activation at all.
 * Announcing the activation from the shell's own bookkeeping instead hands a listener an active shell
 * that {@code getActiveShell()} reports as {@code null}.
 *
 * <p>The native window is stubbed behind the bridge's seams, as in {@link ShellCloseContractFlutterTest},
 * so whether the OS gives it the keyboard is scripted.
 */
@Tag("flutter-it")
class ShellActivationDeskFlutterTest {

    private Display display;
    private String savedMode;
    private final List<String> events = new ArrayList<>();
    private final Map<Shell, String> names = new IdentityHashMap<>();

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        if (savedMode == null)
            System.clearProperty("dev.equo.swt.mode");
        else
            System.setProperty("dev.equo.swt.mode", savedMode);
    }

    @Test
    void openingAShell_whileTheWindowIsNotKey_announcesNoActivation() {
        TestDeskBridge desk = install();
        desk.key = false;
        Shell shell = watched(new Shell(display), "shell");

        shell.open();
        drainEventLoop();

        assertThat(display.getActiveShell())
                .as("a window the OS does not give the keyboard to holds no active shell")
                .isNull();
        assertThat(events)
                .as("and so no shell in it is told it became active")
                .isEmpty();
    }

    @Test
    void theWindowBecomingKey_activatesTheShellGetActiveShellReports() {
        TestDeskBridge desk = install();
        desk.key = false;
        Shell shell = watched(new Shell(display), "shell");
        shell.open();
        drainEventLoop();

        desk.key = true;
        drainEventLoop();

        assertThat(events)
                .as("the window becoming key is the shell becoming active, reported once")
                .containsExactly("shell.Activate active=shell");
        assertThat(display.getActiveShell()).isSameAs(shell);
    }

    @Test
    void openingAShell_inTheKeyWindow_activatesItOnce() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell shell = watched(new Shell(display), "shell");

        shell.open();
        drainEventLoop();

        assertThat(events).containsExactly("shell.Activate active=shell");
    }

    @Test
    void theWindowLosingKey_deactivatesTheActiveShell() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell shell = watched(new Shell(display), "shell");
        shell.open();
        drainEventLoop();
        events.clear();

        desk.key = false;
        drainEventLoop();

        assertThat(events)
                .as("the window stopping being key is the active shell stopping being active")
                .containsExactly("shell.Deactivate active=null");
    }

    @Test
    void forceActive_onAWindowThatIsNotKey_asksTheOsToActivateIt() {
        TestDeskBridge desk = install();
        desk.key = false;
        Shell shell = watched(new Shell(display), "shell");
        shell.open();
        drainEventLoop();

        shell.forceActive();

        assertThat(desk.activationRequests)
                .as("forceActive makes the window key and brings the application forward, even from behind another one")
                .isEqualTo(1);
        assertThat(events)
                .as("the activation itself only arrives once the OS has made the window key")
                .isEmpty();

        desk.key = true;
        drainEventLoop();

        assertThat(events).containsExactly("shell.Activate active=shell");
    }

    @Test
    void theClientsEchoOfAnAnnouncedActivation_isNotASecondOne() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell shell = watched(new Shell(display), "shell");
        shell.open();
        drainEventLoop();

        clientReports(desk, shell, "Shell/Activate");

        assertThat(events)
                .as("a window that is already key does not become key again")
                .containsExactly("shell.Activate active=shell");
    }

    @Test
    void theClientsFocusEchoes_whileTheWindowIsNotKey_announceNothing() {
        TestDeskBridge desk = install();
        desk.key = false;
        Shell shell = watched(new Shell(display), "shell");
        shell.open();
        drainEventLoop();

        clientReports(desk, shell, "Shell/Activate");
        clientReports(desk, shell, "Shell/Deactivate");

        assertThat(events)
                .as("focus moving inside a window the OS is not giving the keyboard to is not activation")
                .isEmpty();
    }

    @Test
    void openingASecondShell_inTheKeyWindow_movesTheActivationOnce() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell first = watched(new Shell(display), "first");
        first.open();
        drainEventLoop();
        Shell second = watched(new Shell(first), "second");
        events.clear();

        second.open();
        drainEventLoop();
        clientReports(desk, first, "Shell/Deactivate");

        assertThat(eventNames())
                .as("the first shell is told once that it lost the activation the second one took")
                .containsExactly("first.Deactivate", "second.Activate");
    }

    @Test
    void aShellTakingTheActivationInsideTheKeyWindow_deactivatesTheOneThatHadIt() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell main = watched(new Shell(display), "main");
        main.open();
        drainEventLoop();
        Shell popup = watched(new Shell(main, SWT.NO_TRIM | SWT.ON_TOP), "popup");
        popup.setVisible(true);
        events.clear();

        // The client's own reports of the focus moving into the popup, as a click there sends them.
        clientReports(desk, popup, "Shell/Activate");
        clientReports(desk, main, "Shell/Deactivate");

        assertThat(events).containsExactly("popup.Activate active=popup", "main.Deactivate active=popup");
    }

    @Test
    void disposingTheActiveShell_inTheKeyWindow_handsTheActivationBack() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell first = watched(new Shell(display), "first");
        first.open();
        Shell second = new Shell(first);
        second.open();
        drainEventLoop();
        events.clear();

        second.dispose();
        drainEventLoop();

        assertThat(events).containsExactly("first.Activate active=first");
    }

    @Test
    void disposingAShellThatWasNeverActive_doesNotReactivateTheActiveOne() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell main = watched(new Shell(display), "main");
        main.open();
        drainEventLoop();
        Shell tip = new Shell(main, SWT.ON_TOP | SWT.TOOL | SWT.NO_FOCUS);
        tip.setVisible(true);
        events.clear();

        tip.dispose();
        drainEventLoop();

        assertThat(events)
                .as("the active shell never stopped being active, so it is not told it became so")
                .isEmpty();
    }

    @Test
    void disposingTheChildOfAShellBehindAnother_leavesTheFrontShellActive() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell back = watched(new Shell(display), "back");
        back.open();
        Shell front = watched(new Shell(display), "front");
        front.open();
        drainEventLoop();
        Shell tip = new Shell(back, SWT.ON_TOP | SWT.TOOL | SWT.NO_FOCUS);
        tip.setVisible(true);
        events.clear();

        tip.dispose();
        drainEventLoop();

        assertThat(events).isEmpty();
        assertThat(display.getActiveShell()).isSameAs(front);
    }

    @Test
    void disposingTheActiveShell_whileTheWindowIsNotKey_activatesNothing() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell first = watched(new Shell(display), "first");
        first.open();
        Shell second = new Shell(first);
        second.open();
        drainEventLoop();
        desk.key = false;
        drainEventLoop();
        events.clear();

        second.dispose();
        drainEventLoop();

        assertThat(events)
                .as("the shell it took over from only becomes active when the window does")
                .isEmpty();
    }

    @Test
    void openingAShell_asTheWindowStopsBeingKey_tellsNeitherShellAnything() {
        TestDeskBridge desk = install();
        desk.key = false;
        Shell first = watched(new Shell(display), "first");
        first.open();
        // Key again, but no pass has looked yet: nothing about it has been announced.
        desk.key = true;
        Shell second = watched(new Shell(first), "second");
        second.addListener(SWT.Show, e -> desk.key = false);

        second.open();

        assertThat(events)
                .as("a shell never told it is active is not told it stopped being, nor is a shell activated in a window that is not key")
                .isEmpty();
    }

    @Test
    void theWindowBecomingKey_withNoShellShowing_announcesNothing() {
        TestDeskBridge desk = install();
        desk.key = true;
        drainEventLoop();
        desk.key = false;
        drainEventLoop();
        Shell shell = watched(new Shell(display), "shell");

        assertThat(events).isEmpty();
        assertThat(display.getActiveShell()).isNull();
        shell.dispose();
    }

    @Test
    void forceActive_withoutANativeWindow_asksTheOsForNothing() {
        TestDeskBridge desk = install();
        desk.key = false;
        Shell shell = watched(new Shell(display), "shell");
        shell.open();
        drainEventLoop();
        Label label = new Label(shell, SWT.NONE);

        desk.activateWindow((DartControl) label.getImpl());
        desk.surface = false;
        shell.forceActive();

        assertThat(desk.activationRequests)
                .as("only a shell can be activated, and only through a window that is still there")
                .isZero();
    }

    @Test
    void forceActive_againstABridgeLibraryWithoutActivation_keepsWorking() {
        TestDeskBridge desk = install();
        desk.stubActivation = false;
        desk.key = true;
        Shell shell = watched(new Shell(display), "shell");
        shell.open();
        drainEventLoop();

        // The library loaded in this JVM has no Activate symbol: the first call finds out, the
        // second does not ask again; neither may break forceActive().
        shell.forceActive();
        shell.forceActive();

        assertThat(events).containsExactly("shell.Activate active=shell");
    }

    @Test
    void setActive_whoseFocusChangeDisposesTheShell_announcesNothing() {
        TestDeskBridge desk = install();
        desk.key = true;
        Shell first = watched(new Shell(display), "first");
        Text field = new Text(first, SWT.SINGLE);
        first.open();
        Shell second = new Shell(first);
        second.open();
        drainEventLoop();
        field.addListener(SWT.FocusIn, e -> first.dispose());
        events.clear();

        first.setActive();
        drainEventLoop();

        assertThat(first.isDisposed()).isTrue();
        assertThat(events).doesNotContain("first.Activate active=first");
    }

    @Test
    void forceActive_onASurfaceWithNoWindowOfItsOwn_activatesTheShellAsBefore() {
        savedMode = System.getProperty("dev.equo.swt.mode");
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        Shell shell = watched(new Shell(display), "shell");
        shell.open();

        shell.forceActive();

        assertThat(display.getActiveShell()).isSameAs(shell);
        assertThat(events).containsExactly("shell.Activate active=shell");
    }

    private Shell watched(Shell shell, String name) {
        names.put(shell, name);
        shell.addListener(SWT.Activate, e -> events.add(name + ".Activate active=" + nameOf(display.getActiveShell())));
        shell.addListener(SWT.Deactivate, e -> events.add(name + ".Deactivate active=" + nameOf(display.getActiveShell())));
        return shell;
    }

    private String nameOf(Shell active) {
        if (active == null) return "null";
        return names.getOrDefault(active, String.valueOf(active));
    }

    private List<String> eventNames() {
        return events.stream().map(e -> e.substring(0, e.indexOf(' '))).toList();
    }

    /** What the client sends when the focus moves into or out of the shell's focus scope. */
    private void clientReports(TestDeskBridge desk, Shell shell, String suffix) {
        desk.comm.fireContaining("/" + shell.hashCode() + "/" + suffix, new Event());
        drainEventLoop();
    }

    private void drainEventLoop() {
        for (int i = 0; i < 10; i++) {
            while (display.readAndDispatch()) {
                /* drain */
            }
        }
    }

    private TestDeskBridge install() {
        savedMode = System.getProperty("dev.equo.swt.mode");
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestDeskBridge bridge = new TestDeskBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** Desktop-native bridge with the native window stubbed: whether it is key is scripted. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();
        boolean key;
        boolean surface = true;
        boolean stubActivation = true;
        int activationRequests;

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected boolean windowActive(long context) {
            return key;
        }

        @Override
        protected void activateNativeWindow(long context) {
            if (stubActivation)
                activationRequests++;
            else
                super.activateNativeWindow(context);
        }

        @Override
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null; // a stood-in window sits nowhere; only a real one has a screen position
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display); // no native window, just the comm handshake
        }

        @Override
        protected boolean hasWindowSurface() {
            return surface;
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }
    }
}
