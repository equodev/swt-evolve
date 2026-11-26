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
 * Moves the client reports faster than the UI thread handles them merge, as GTK compresses motion: a drag whose
 * every move costs more than the next takes to arrive follows the pointer instead of replaying where it was.
 */
@Tag("flutter-it")
class MoveCompressionFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private final List<String> heard = new ArrayList<>();

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
        e.stateMask = SWT.BUTTON1;
        bridge.comm.fireContaining(FlutterBridge.event((DartWidget) control.getImpl(), group, name), e);
    }

    private void move(Control control, int y) {
        report(control, "MouseMove", "MouseMove", 10, y);
    }

    private Canvas canvas(String name, int y) {
        Canvas c = new Canvas(shell, SWT.NONE);
        c.setBounds(0, y, 200, 100);
        c.addListener(SWT.MouseMove, e -> heard.add(name + " move " + e.y));
        c.addListener(SWT.MouseDown, e -> heard.add(name + " down " + e.y));
        c.addListener(SWT.MouseUp, e -> heard.add(name + " up " + e.y));
        return c;
    }

    private Canvas opened() {
        Canvas c = canvas("c", 0);
        shell.open();
        drain();
        heard.clear();
        return c;
    }

    @Test
    void movesReportedBeforeTheFirstIsHandledReachTheApplicationAsOneAtTheLast() {
        Canvas c = opened();
        move(c, 1);
        move(c, 2);
        move(c, 3);
        drain();
        assertThat(heard).containsExactly("c move 3");
    }

    @Test
    void aMoveReportedOnceTheOneBeforeIsHandledIsAMoveOfItsOwn() {
        Canvas c = opened();
        move(c, 1);
        drain();
        move(c, 2);
        move(c, 3);
        drain();
        assertThat(heard).containsExactly("c move 1", "c move 3");
    }

    @Test
    void movesOnEitherSideOfAPressOrReleaseStayOnTheirSide() {
        Canvas c = opened();
        move(c, 1);
        move(c, 2);
        report(c, "Mouse", "MouseDown", 10, 2);
        move(c, 3);
        move(c, 4);
        report(c, "Mouse", "MouseUp", 10, 4);
        move(c, 5);
        drain();
        assertThat(heard).containsExactly("c move 2", "c down 2", "c move 4", "c up 4", "c move 5");
    }

    @Test
    void movesOverDifferentControlsAreEachDelivered() {
        Canvas a = canvas("a", 0);
        Canvas b = canvas("b", 100);
        shell.open();
        drain();
        heard.clear();
        move(a, 1);
        move(b, 2);
        move(a, 3);
        drain();
        assertThat(heard).containsExactly("a move 1", "b move 2", "a move 3");
    }

    private long answers() {
        return bridge.comm.sent.stream().filter(f -> f.event.contains("swt.evolve.move.handled")).count();
    }

    @Test
    void aClientThatAskedHearsOnceWhenTheMovesItReportedAreHandled() {
        Canvas c = opened();
        // As the client asks, on the global channel the bridge wires for it.
        dev.equo.swt.MoveCompression.asked(bridge.comm);
        move(c, 1);
        move(c, 2);
        drain();
        assertThat(heard).containsExactly("c move 2");
        assertThat(answers()).isEqualTo(1);

        drain();
        assertThat(answers()).as("nothing more was handled").isEqualTo(1);
    }

    @Test
    void aMoveAndEverythingItCausedReachTheClientAsOneMessageWithTheAnswerLast() {
        Canvas c = opened();
        c.addListener(SWT.Paint, e -> e.gc.drawLine(0, 0, c.getSize().x, 10));
        c.addListener(SWT.MouseMove, e -> {
            c.setSize(200 + e.y, 100);
            c.redraw();
        });
        dev.equo.swt.MoveCompression.asked(bridge.comm);
        drain();
        bridge.comm.sent.clear();
        int before = bridge.comm.messages.get();

        move(c, 5);
        drain();

        assertThat(bridge.comm.messages.get() - before).as("one message").isEqualTo(1);
        List<String> channels = dev.equo.swt.harness.GcFrames.channels(bridge.comm.sent);
        assertThat(channels).as("the paint, the records (the canvas's new size), then the answer")
                .anyMatch(ch -> ch.startsWith("GC/"))
                .contains(dev.equo.swt.WidgetWire.CHANNEL)
                .last().asString().contains("swt.evolve.move.handled");
        assertThat(channels.indexOf(channels.stream().filter(ch -> ch.startsWith("GC/")).findFirst().orElseThrow()))
                .isLessThan(channels.indexOf(dev.equo.swt.WidgetWire.CHANNEL));
    }

    @Test
    void aClientThatDidNotAskHearsNothing() {
        Canvas c = opened();
        move(c, 1);
        drain();
        assertThat(heard).containsExactly("c move 1");
        assertThat(answers()).isZero();
    }
}
