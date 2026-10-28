package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the window events the client reports do to a shell: each reaches the shell's listeners as the
 * window manager's would, through the event loop, and none reaches a shell that is gone by then.
 */
@Tag("flutter-it")
class ShellClientEventsFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell main;
    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        web = new TestWebBridge(dd);
        dd.setBridge(web);
        web.start(dd);
        main = new Shell(display);
        main.open();
        shell = new Shell(main, SWT.DIALOG_TRIM);
        shell.setBounds(100, 120, 300, 200);
        shell.open();
        runLoop();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void fromClient(String event, Event e) {
        fromClient(shell, event, e);
    }

    private void fromClient(Shell target, String event, Event e) {
        web.comm.fireContaining(FlutterBridge.event(target.getImpl(), "Shell", event), e);
    }

    private void runLoop() {
        while (display.readAndDispatch()) {
        }
    }

    private List<Integer> record(int... types) {
        List<Integer> events = new ArrayList<>();
        for (int type : types)
            shell.addListener(type, e -> events.add(e.type));
        return events;
    }

    @Test
    void anActivationReachesTheShell() {
        main.setActive();
        runLoop();
        List<Integer> events = record(SWT.Activate);
        fromClient("Activate", new Event());
        runLoop();
        assertThat(events).containsExactly(SWT.Activate);
    }

    // The client reports the opened shell's activation when its first control takes the focus, or on
    // the first press when it has none; open() already announced it, and no native announces it twice.
    @Test
    void theClientReportingTheActivationOpenMadeAnnouncesNoSecondOne() {
        List<Integer> events = record(SWT.Activate);
        fromClient("Activate", new Event());
        runLoop();
        assertThat(events).isEmpty();
    }

    @Test
    void anActivationAfterTheClientDeactivatedTheShellReachesIt() {
        List<Integer> events = record(SWT.Activate);
        fromClient("Deactivate", new Event());
        fromClient("Activate", new Event());
        runLoop();
        assertThat(events).containsExactly(SWT.Activate);
    }

    @Test
    void anActivationAfterTheClientActivatedAnotherShellReachesIt() {
        List<Integer> events = record(SWT.Activate);
        fromClient(main, "Activate", new Event());
        fromClient("Activate", new Event());
        runLoop();
        assertThat(events).containsExactly(SWT.Activate);
    }

    // Disposing the active shell hands activation back to the previous one, which the client then
    // reports once that shell's focus scope regains the focus.
    @Test
    void theClientReportingTheActivationADisposedShellHandedBackAnnouncesNoSecondOne() {
        List<Integer> events = new ArrayList<>();
        main.addListener(SWT.Activate, e -> events.add(e.type));
        shell.dispose();
        runLoop();
        fromClient(main, "Activate", new Event());
        runLoop();
        assertThat(events).containsExactly(SWT.Activate);
    }

    // Both shells opened before the client reported either: only the one opened last waits for its
    // report, since nothing in a report tells a late one from a return to the shell that opened with
    // nothing to focus. Each of the two reports is then announced as an activation of its own.
    @Test
    void theClientReportingBothOpenedShellsInOrderAnnouncesEachReport() {
        List<String> events = new ArrayList<>();
        main.addListener(SWT.Activate, e -> events.add("main Activate"));
        shell.addListener(SWT.Activate, e -> events.add("shell Activate"));
        fromClient(main, "Activate", new Event());
        fromClient(shell, "Activate", new Event());
        runLoop();
        assertThat(events).containsExactly("main Activate", "shell Activate");
    }

    @Test
    void aDeactivationReachesTheShellAndKeepsItsFocus() {
        Text field = new Text(shell, SWT.SINGLE);
        field.setFocus();
        runLoop();
        List<Integer> events = record(SWT.Deactivate);
        fromClient("Deactivate", new Event());
        runLoop();
        assertThat(events).containsExactly(SWT.Deactivate);
        assertThat(((DartDecorations) shell.getImpl()).savedFocus).isSameAs(field);
    }

    @Test
    void iconifyAndDeiconifyReachTheShell() {
        List<Integer> events = record(SWT.Iconify, SWT.Deiconify);
        fromClient("Iconify", new Event());
        fromClient("Deiconify", new Event());
        runLoop();
        assertThat(events).containsExactly(SWT.Iconify, SWT.Deiconify);
    }

    @Test
    void aCloseAsksTheShellAndDisposesIt() {
        List<Integer> events = record(SWT.Close);
        fromClient("Close", new Event());
        runLoop();
        assertThat(events).containsExactly(SWT.Close);
        assertThat(shell.isDisposed()).isTrue();
    }

    @Test
    void aCloseAListenerRefusesKeepsTheShell() {
        shell.addListener(SWT.Close, e -> e.doit = false);
        fromClient("Close", new Event());
        runLoop();
        assertThat(shell.isDisposed()).isFalse();
    }

    @Test
    void theBoundsTheWindowReportsBecomeTheShells() {
        Event e = new Event();
        e.x = 140;
        e.y = 160;
        e.width = 320;
        e.height = 240;
        fromClient("SetBounds", e);
        runLoop();
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(140, 160, 320, 240));
    }

    @Test
    void anEventForAShellDisposedBeforeItRunsIsDropped() {
        List<Integer> events = record(SWT.Iconify);
        fromClient("Iconify", new Event());
        fromClient("Close", new Event());
        fromClient("Activate", new Event());
        fromClient("Deactivate", new Event());
        fromClient("Deiconify", new Event());
        fromClient("SetBounds", new Event());
        shell.dispose();
        runLoop();
        assertThat(events).isEmpty();
    }

    @Test
    void anActivationWhoseListenerDisposesTheShellMovesNoFocus() {
        main.setActive();
        runLoop();
        List<Integer> events = record(SWT.FocusIn);
        shell.addListener(SWT.Activate, e -> shell.dispose());
        fromClient("Activate", new Event());
        runLoop();
        assertThat(shell.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }

    @Test
    void theBoundsTheShellAlreadyHasMoveAndResizeNothing() {
        List<Integer> events = record(SWT.Move, SWT.Resize);
        Rectangle bounds = shell.getBounds();
        Event e = new Event();
        e.x = bounds.x;
        e.y = bounds.y;
        e.width = bounds.width;
        e.height = bounds.height;
        fromClient("SetBounds", e);
        runLoop();
        assertThat(events).isEmpty();
        assertThat(shell.getBounds()).isEqualTo(bounds);
    }

    @Test
    void eachChangeOfTheBoundsTheWindowReportsBecomesTheShells() {
        Rectangle b = shell.getBounds();
        List<Integer> events = record(SWT.Resize);
        int[][] reports = { { b.x, b.y + 20, b.width, b.height }, { b.x, b.y + 20, b.width + 20, b.height },
                { b.x, b.y + 20, b.width + 20, b.height + 20 } };
        for (int[] r : reports) {
            Event e = new Event();
            e.x = r[0];
            e.y = r[1];
            e.width = r[2];
            e.height = r[3];
            fromClient("SetBounds", e);
            runLoop();
        }
        assertThat(events).containsExactly(SWT.Resize, SWT.Resize);
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(b.x, b.y + 20, b.width + 20, b.height + 20));
    }
}
