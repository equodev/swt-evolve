package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A key that moves a control away from under a pointer that does not move, or back under it: as on GTK, the
 * crossings reach the application once the key's listeners have run and before the KeyUp of that key, and the ones
 * the client reports for them a frame later are dropped.
 */
@Tag("flutter-it")
class ControlMovedOffStillPointerFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
        shell.setBounds(0, 0, 300, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    private void report(Control control, String group, String name, int x, int y) {
        Event e = new Event();
        e.x = x;
        e.y = y;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) control.getImpl(), group, name), e);
    }

    private static void listen(Control control, String name, List<String> heard) {
        int[] types = {SWT.MouseEnter, SWT.MouseExit, SWT.MouseHover, SWT.KeyDown, SWT.KeyUp};
        String[] names = {"MouseEnter", "MouseExit", "MouseHover", "KeyDown", "KeyUp"};
        for (int i = 0; i < types.length; i++) {
            String event = names[i];
            control.addListener(types[i], e -> heard.add(name + " " + event));
        }
    }

    private Canvas cursorInParent(List<String> heard) {
        Composite parent = new Composite(shell, SWT.NONE);
        parent.setBounds(0, 0, 200, 200);
        Canvas cursor = new Canvas(parent, SWT.NONE);
        cursor.setBounds(0, 0, 200, 20);
        shell.open();
        drain();
        listen(parent, "parent", heard);
        listen(cursor, "cursor", heard);
        cursor.addListener(SWT.KeyDown, e -> {
            cursor.setLocation(0, cursor.getLocation().y == 0 ? 40 : 0);
            heard.add("cursor moved");
        });
        report(cursor, "MouseMove", "MouseMove", 100, 10);
        report(cursor, "MouseTrack", "MouseEnter", 100, 10);
        drain();
        return cursor;
    }

    private void pressAndRelease(Control control) {
        report(control, "Key", "KeyDown", 0, 0);
        report(control, "Key", "KeyUp", 0, 0);
        drain();
    }

    @Test
    void theCrossingsOfAControlAKeyMovesOffTheStillPointerFollowTheKeysListenersAndPrecedeItsKeyUp() {
        List<String> heard = new ArrayList<>();
        Canvas cursor = cursorInParent(heard);

        pressAndRelease(cursor);
        report(cursor, "MouseTrack", "MouseExit", 100, 10);
        report(cursor.getParent(), "MouseTrack", "MouseEnter", 100, 10);
        drain();

        assertThat(heard).containsExactly("cursor MouseEnter", "cursor KeyDown", "cursor moved", "cursor MouseExit",
                "parent MouseEnter", "cursor KeyUp");
    }

    @Test
    void theCrossingsOfAControlAKeyMovesBackUnderTheStillPointerFollowTheKeysListenersAndPrecedeItsKeyUp() {
        List<String> heard = new ArrayList<>();
        Canvas cursor = cursorInParent(heard);
        pressAndRelease(cursor);
        report(cursor, "MouseTrack", "MouseExit", 100, 10);
        report(cursor.getParent(), "MouseTrack", "MouseEnter", 100, 10);
        drain();
        heard.clear();

        pressAndRelease(cursor);
        report(cursor.getParent(), "MouseTrack", "MouseExit", 100, 10);
        report(cursor, "MouseTrack", "MouseEnter", 100, 10);
        drain();

        assertThat(heard).containsExactly("cursor KeyDown", "cursor moved", "parent MouseExit", "cursor MouseEnter",
                "cursor KeyUp");
    }

    @Test
    void aControlMovedOffAndBackBeforeTheClientSawEitherKeepsThePointerCrossingsTheClientReportsNext() {
        List<String> heard = new ArrayList<>();
        Canvas cursor = cursorInParent(heard);
        pressAndRelease(cursor);
        pressAndRelease(cursor);
        heard.clear();

        report(cursor, "MouseMove", "MouseMove", 100, 30);
        report(cursor, "MouseTrack", "MouseExit", 100, 30);
        report(cursor.getParent(), "MouseTrack", "MouseEnter", 100, 30);
        drain();

        assertThat(heard).containsExactly("cursor MouseExit", "parent MouseEnter");
    }
}
