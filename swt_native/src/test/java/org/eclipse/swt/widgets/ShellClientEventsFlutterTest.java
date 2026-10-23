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
        web.comm.fireContaining(FlutterBridge.event(shell.getImpl(), "Shell", event), e);
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
}
