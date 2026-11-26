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

import static org.assertj.core.api.Assertions.assertThat;

/** The Paints a turn asks for run in one pass and reach the client in one message. */
@Tag("flutter-it")
class PaintPassFlutterTest {

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

    private Canvas canvas(int x) {
        Canvas c = new Canvas(shell, SWT.NONE);
        c.setBounds(x, 0, 100, 100);
        return c;
    }

    @Test
    void aControlRedrawnFromAnothersPaintIsPaintedInTheSamePassAndMessage() {
        // As a ruler follows the text it numbers.
        Canvas text = canvas(0), ruler = canvas(150);
        int[] rulerPaints = { 0 };
        text.addListener(SWT.Paint, e -> {
            e.gc.drawLine(0, 0, 50, 10);
            ruler.redraw();
        });
        ruler.addListener(SWT.Paint, e -> {
            rulerPaints[0]++;
            e.gc.drawLine(0, 0, 10, 50);
        });
        shell.open();
        drain();
        rulerPaints[0] = 0;
        int before = bridge.comm.messages.get();

        text.redraw();
        drain();

        assertThat(rulerPaints[0]).as("the ruler painted once").isEqualTo(1);
        assertThat(bridge.comm.messages.get() - before).as("the text's and the ruler's paints, one message").isEqualTo(1);
    }

    @Test
    void aControlThatRedrawsItselfWhilePaintingIsPaintedAgainInTheNextPass() {
        // A pass never runs a control twice, so it ends.
        Canvas c = canvas(0);
        int[] paints = { 0 };
        c.addListener(SWT.Paint, e -> {
            paints[0]++;
            e.gc.drawLine(0, 0, paints[0], 10);
            if (paints[0] % 2 == 1) c.redraw();
        });
        shell.open();
        drain();
        paints[0] = 0;
        int before = bridge.comm.messages.get();

        c.redraw();
        drain();

        assertThat(paints[0]).as("painted, then once more in the next pass").isEqualTo(2);
        assertThat(bridge.comm.messages.get() - before).as("a message per pass").isEqualTo(2);
    }
}
