package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Natively a modal dialog swallows the rest of the gesture that opened it: the operating system
 * routes the following clicks to the new window, so the control underneath never sees them. The
 * render side already refuses a pointer aimed behind a modal, but it cannot recall input it has
 * already sent -- and Evolve hands each event of a gesture to its own asyncExec, which a dialog's
 * modal loop keeps draining. These scenarios reproduce that ordering: the events are queued while
 * nothing is modal, the dialog opens, and only then is the queue drained.
 */
@Tag("flutter-it")
class ModalShellSwallowsInputFlutterTest {

    private Display display;
    private RecordingBridge bridge;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    /** Delivers what Flutter already sent, the way the dialog's modal loop would. */
    private void drain() {
        while (display.readAndDispatch()) {
            // deliver every queued event
        }
    }

    private void flutterSends(Widget widget, String listener, String event) {
        bridge.comm.fireContaining(FlutterBridge.event(widget.getImpl(), listener, event), new Event());
    }

    private void openDisplay() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @Test
    void inFlightInput_isWithheldFromTheControlTheDialogCovers() {
        openDisplay();

        Shell main = new Shell(display);
        Table table = new Table(main, SWT.NONE);
        main.open();

        int[] doubleClicks = {0};
        table.addListener(SWT.MouseDoubleClick, e -> doubleClicks[0]++);

        flutterSends(table, "Mouse", "MouseDoubleClick");
        drain();
        assertThat(doubleClicks[0])
                .as("with nothing modal open the control receives its own input")
                .isEqualTo(1);

        // The tail of the gesture: sent by Flutter before the dialog exists, delivered after.
        flutterSends(table, "Mouse", "MouseDoubleClick");
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.open();
        drain();
        assertThat(doubleClicks[0])
                .as("input queued before the dialog opened must not reach the table behind it")
                .isEqualTo(1);

        dialog.close();
        flutterSends(table, "Mouse", "MouseDoubleClick");
        drain();
        assertThat(doubleClicks[0])
                .as("input resumes once the dialog is gone")
                .isEqualTo(2);
    }

    /**
     * Clicking as fast as the pointer allows. JFace's ColumnViewer activates its cell editor for
     * every count except 2, so a burst activates it on the 1 and again on the 3 no matter how
     * faithful the count is; what stops the repeat natively is that the first dialog takes the
     * pointer. Only the click that opened the dialog may be delivered.
     */
    @Test
    void aBurstOfClicks_reachesTheTableOnlyUntilTheDialogOpens() {
        openDisplay();

        Shell main = new Shell(display);
        Table table = new Table(main, SWT.NONE);
        main.open();

        int[] downs = {0};
        table.addListener(SWT.MouseDown, e -> downs[0]++);

        // Flutter sends the whole burst before Java has processed any of it.
        for (int i = 0; i < 6; i++) {
            flutterSends(table, "Mouse", "MouseDown");
        }
        // The first one is what opened the editor's dialog.
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.open();
        drain();

        assertThat(downs[0])
                .as("the rest of the burst belongs to the dialog, not to the table behind it")
                .isZero();
    }

    @Test
    void modalShell_stillReceivesItsOwnInput() {
        openDisplay();

        Shell main = new Shell(display);
        new Table(main, SWT.NONE);
        main.open();

        Shell dialog = new Shell(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        Button ok = new Button(dialog, SWT.PUSH);
        dialog.open();

        int[] clicks = {0};
        ok.addListener(SWT.MouseDown, e -> clicks[0]++);

        flutterSends(ok, "Mouse", "MouseDown");
        drain();
        assertThat(clicks[0])
                .as("a control inside the modal shell is not blocked by it")
                .isEqualTo(1);
    }

    /**
     * Motion is deliberately not withheld: the exit that clears a hover state is itself in flight
     * when a dialog opens, so dropping it would strand the control the pointer left.
     */
    @Test
    void motionIsNotWithheld_soAHoverCanStillBeCleared() {
        openDisplay();

        Shell main = new Shell(display);
        Table table = new Table(main, SWT.NONE);
        main.open();

        int[] exits = {0};
        table.addListener(SWT.MouseExit, e -> exits[0]++);

        flutterSends(table, "MouseTrack", "MouseExit");
        Shell dialog = new Shell(main, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.open();
        drain();

        assertThat(exits[0])
                .as("the control the pointer left must still be told, or it stays hovered")
                .isEqualTo(1);
    }
}
