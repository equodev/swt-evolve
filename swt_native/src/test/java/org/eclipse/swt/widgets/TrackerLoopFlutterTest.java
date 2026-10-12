package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What Tracker.open() does with what the client reports while it runs, held to what upstream's loop does with
 * the mouse and the keyboard: the rectangles follow the pointer, moved or resized as the style says, before the
 * listeners hear Move or Resize; Esc cancels, Return ends and an arrow steps; a listener that disposes the Tracker
 * cancels it. The client is the recording comm: each test fires its pointer positions and keys from a runnable
 * queued before open(), which the loop runs.
 */
@Tag("flutter-it")
class TrackerLoopFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell shell;
    private final List<String> heard = new ArrayList<>();

    @BeforeEach
    void setUp() {
        web = install(TestWebBridge::new);
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    // ---- harness ----------------------------------------------------------------------------------

    private static Event at(int x, int y) {
        Event e = new Event();
        e.x = x;
        e.y = y;
        return e;
    }

    private static Event key(int type, int keyCode, int stateMask) {
        Event e = new Event();
        e.type = type;
        e.keyCode = keyCode;
        e.stateMask = stateMask;
        return e;
    }

    private void client(Tracker tracker, String suffix, Event e) {
        web.comm.fireContaining("/" + tracker.hashCode() + "/" + suffix, e);
    }

    private void pointer(Tracker tracker, int x, int y) {
        client(tracker, "Control/Move", at(x, y));
    }

    private void release(Tracker tracker) {
        client(tracker, "Tracker/close", new Event());
    }

    /** Records each Move and Resize with where it says the pointer is and the rectangles it finds. */
    private void listen(Tracker tracker) {
        Listener listener = e -> heard.add((e.type == SWT.Move ? "Move " : "Resize ") + e.x + "," + e.y + " "
                + List.of(tracker.getRectangles()));
        tracker.addListener(SWT.Move, listener);
        tracker.addListener(SWT.Resize, listener);
    }

    /** Opens {@code tracker} with {@code client} queued to run inside its loop; a timer ends a loop left open. */
    private boolean open(Tracker tracker, Runnable client) {
        display.asyncExec(client);
        Runnable giveUp = () -> {
            if (!tracker.isDisposed())
                tracker.close();
        };
        display.timerExec(5000, giveUp);
        try {
            return tracker.open();
        } finally {
            display.timerExec(-1, giveUp);
        }
    }

    private static String rects(Rectangle... rectangles) {
        return List.of(rectangles).toString();
    }

    private boolean sentToShell(String suffix) {
        String channel = "/" + shell.hashCode() + "/" + suffix;
        return web.comm.sent.stream().anyMatch(f -> f.event.contains(channel));
    }

    // ---- the pointer --------------------------------------------------------------------------------

    @Test
    void theRectanglesFollowThePointerFromWhereItIsFirstReported() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20), new Rectangle(40, 10, 5, 5)});
        listen(tracker);

        boolean ended = open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 110, 105);
            pointer(tracker, 110, 105);
            release(tracker);
        });

        assertThat(ended).isTrue();
        assertThat(heard).containsExactly(
                "Move 100,100 " + rects(new Rectangle(10, 10, 20, 20), new Rectangle(40, 10, 5, 5)),
                "Move 110,105 " + rects(new Rectangle(20, 15, 20, 20), new Rectangle(50, 15, 5, 5)),
                "Move 110,105 " + rects(new Rectangle(20, 15, 20, 20), new Rectangle(50, 15, 5, 5)));
        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(20, 15, 20, 20), new Rectangle(50, 15, 5, 5));
        assertThat(display.getCursorLocation()).isEqualTo(new Point(110, 105));
    }

    @Test
    void theRectanglesMoveOnlyInTheDirectionsOfTheStyle() {
        Tracker tracker = new Tracker(shell, SWT.LEFT | SWT.UP);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        listen(tracker);

        open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 110, 120);
            pointer(tracker, 90, 95);
            release(tracker);
        });

        assertThat(heard).containsExactly(
                "Move 100,100 " + rects(new Rectangle(10, 10, 20, 20)),
                "Move 110,120 " + rects(new Rectangle(10, 10, 20, 20)),
                "Move 90,95 " + rects(new Rectangle(-10, -15, 20, 20)));
    }

    @Test
    void aResizeTrackerGrowsTheEdgeThePointerPullsAndSaysResize() {
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        listen(tracker);
        // Once an edge is taken, the pointer is put on it, as upstream warps it there.
        Point rightEdge = shell.toDisplay(40, 20);

        open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 110, 100);
            pointer(tracker, rightEdge.x + 15, rightEdge.y + 30);
            release(tracker);
        });

        assertThat(heard).containsExactly(
                "Resize 100,100 " + rects(new Rectangle(10, 10, 20, 20)),
                "Resize 110,100 " + rects(new Rectangle(10, 10, 30, 20)),
                "Resize " + (rightEdge.x + 15) + "," + (rightEdge.y + 30) + " " + rects(new Rectangle(10, 10, 45, 50)));
        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(10, 10, 45, 50));
    }

    @Test
    void aTrackerDisposedByItsListenerIsCancelled() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        tracker.addListener(SWT.Move, e -> tracker.dispose());

        boolean ended = open(tracker, () -> pointer(tracker, 100, 100));

        assertThat(ended).isFalse();
        assertThat(tracker.isDisposed()).isTrue();
        assertThat(sentToShell("Tracker/close")).as("the client is told the band is gone").isTrue();
    }

    @Test
    void theClientCancellingEndsItAsCancelled() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        assertThat(open(tracker, () -> client(tracker, "Tracker/cancel", new Event()))).isFalse();
        assertThat(open(tracker, () -> release(tracker))).as("the next open() starts afresh").isTrue();
    }

    @Test
    void aDisposedParentEndsTheLoopAndTheShellStillHearsTheEnd() {
        Composite composite = new Composite(shell, SWT.NONE);
        Tracker tracker = new Tracker(composite, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(1, 1, 1, 1)});

        boolean ended = open(tracker, composite::dispose);

        assertThat(ended).isTrue();
        assertThat(sentToShell("Tracker/open")).isTrue();
        assertThat(sentToShell("Tracker/close")).isTrue();
        assertThat(tracker.isDisposed()).isFalse();
    }

    // ---- a Tracker on the display -------------------------------------------------------------------

    @Test
    void aTrackerOnTheDisplayFollowsThePointerOverTheShell() {
        Tracker tracker = new Tracker(display, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(50, 50, 10, 10)});
        listen(tracker);

        boolean ended = open(tracker, () -> {
            pointer(tracker, 200, 200);
            pointer(tracker, 210, 220);
            release(tracker);
        });

        assertThat(ended).isTrue();
        assertThat(sentToShell("Tracker/open")).as("announced on the shell the pointer is seen over").isTrue();
        assertThat(sentToShell("Tracker/close")).isTrue();
        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(60, 70, 10, 10));
        tracker.dispose();
    }

    @Test
    void aTrackerOnTheDisplayWithNoShellToFollowWaitsForItsClose() {
        shell.setVisible(false);
        Tracker tracker = new Tracker(display, SWT.NONE);

        boolean ended = open(tracker, tracker::close);

        assertThat(ended).isTrue();
        assertThat(sentToShell("Tracker/open")).isFalse();
        tracker.dispose();
    }

    // ---- the keyboard -------------------------------------------------------------------------------

    @Test
    void escapeCancelsAndTheKeyGoesToTheTrackerAlone() {
        Text focused = new Text(shell, SWT.SINGLE);
        web.setFocus((DartControl) focused.getImpl());
        List<Integer> focusedHeard = new ArrayList<>();
        focused.addListener(SWT.KeyDown, e -> focusedHeard.add(e.keyCode));
        Tracker tracker = new Tracker(shell, SWT.NONE);
        List<Integer> trackerHeard = new ArrayList<>();
        tracker.addListener(SWT.KeyDown, e -> trackerHeard.add(e.keyCode));

        boolean ended = open(tracker, () -> display.post(key(SWT.KeyDown, SWT.ESC, 0)));

        assertThat(ended).isFalse();
        assertThat(trackerHeard).containsExactly((int) SWT.ESC);
        assertThat(focusedHeard).isEmpty();
        display.post(key(SWT.KeyDown, 'a', 0));
        while (display.readAndDispatch()) {
        }
        assertThat(focusedHeard).as("the keys go back to the focus once the loop is over").containsExactly(Integer.valueOf('a'));
    }

    @Test
    void returnAndTheKeypadEnterEndTheLoop() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        List<Integer> trackerHeard = new ArrayList<>();
        tracker.addListener(SWT.KeyDown, e -> trackerHeard.add(e.keyCode));
        assertThat(open(tracker, () -> display.post(key(SWT.KeyDown, SWT.CR, 0)))).isTrue();
        assertThat(open(tracker, () -> display.post(key(SWT.KeyDown, SWT.KEYPAD_CR, 0)))).isTrue();
        assertThat(trackerHeard).containsExactly((int) SWT.CR, SWT.KEYPAD_CR);
    }

    @Test
    void aKeyAListenerVetoesDoesNothing() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.addListener(SWT.KeyDown, e -> e.doit = e.keyCode != SWT.ESC);

        boolean ended = open(tracker, () -> {
            display.post(key(SWT.KeyDown, SWT.ESC, 0));
            display.asyncExec(() -> release(tracker));
        });

        assertThat(ended).isTrue();
    }

    @Test
    void anArrowStepsTheRectanglesNineOrOneWithControl() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        listen(tracker);
        Point anchor = shell.toDisplay(20, 10);
        // Cocoa acts on an arrow's release as well as on its press.
        int steps = "cocoa".equals(SWT.getPlatform()) ? 2 : 1;

        open(tracker, () -> {
            display.post(key(SWT.KeyDown, SWT.ARROW_RIGHT, 0));
            display.post(key(SWT.KeyUp, SWT.ARROW_RIGHT, 0));
            display.post(key(SWT.KeyDown, SWT.ARROW_DOWN, SWT.CTRL));
            display.asyncExec(() -> release(tracker));
        });

        int x = 10 + 9 * steps;
        assertThat(heard.get(0)).isEqualTo("Move " + (anchor.x + 9) + "," + anchor.y + " " + rects(new Rectangle(19, 10, 20, 20)));
        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(x, 11, 20, 20));
    }

    @Test
    void anArrowResizesAResizeTracker() {
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        listen(tracker);

        open(tracker, () -> {
            display.post(key(SWT.KeyDown, SWT.ARROW_LEFT, SWT.CTRL));
            display.asyncExec(() -> release(tracker));
        });

        assertThat(heard).hasSize(1);
        assertThat(heard.get(0)).startsWith("Resize ").endsWith(rects(new Rectangle(9, 10, 21, 20)));
    }

    @Test
    void aKeyFromTheFocusedControlsOwnChannelGoesToTheTracker() {
        Text focused = new Text(shell, SWT.SINGLE);
        web.setFocus((DartControl) focused.getImpl());
        List<Integer> focusedHeard = new ArrayList<>();
        focused.addListener(SWT.KeyDown, e -> focusedHeard.add(e.keyCode));
        Tracker tracker = new Tracker(shell, SWT.NONE);

        boolean ended = open(tracker, () -> web.comm.fireContaining("/" + focused.hashCode() + "/Key/KeyDown",
                key(SWT.KeyDown, SWT.ESC, 0)));

        assertThat(ended).isFalse();
        assertThat(focusedHeard).isEmpty();
    }

    @Test
    void aKeyFromTheDisplayChannelGoesToTheTrackerAndTheFocusIsToldNo() {
        web.registerDisplayKeyEvents((DartDisplay) display.getImpl());
        Text focused = new Text(shell, SWT.SINGLE);
        web.setFocus((DartControl) focused.getImpl());
        focused.addListener(SWT.KeyDown, e -> {
        });
        Tracker tracker = new Tracker(shell, SWT.NONE);
        List<Integer> trackerHeard = new ArrayList<>();
        tracker.addListener(SWT.KeyUp, e -> trackerHeard.add(e.keyCode));
        long displayId = display.hashCode();

        boolean ended = open(tracker, () -> {
            web.comm.fireContaining("Display/" + displayId + "/Key/KeyUp", key(SWT.KeyUp, 'x', 0));
            web.comm.fireContaining("Display/" + displayId + "/Key/KeyDown", key(SWT.KeyDown, SWT.CR, 0));
        });

        assertThat(ended).isTrue();
        assertThat(trackerHeard).containsExactly(Integer.valueOf('x'));
        assertThat(web.comm.sent.stream().anyMatch(f -> f.event.contains("/" + focused.hashCode() + "/key/verdict")
                && f.json.contains("false"))).isTrue();
    }

    /** See {@code ControlFocusRequestFlutterTest.install} — a Display whose bridge is the test bridge. */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
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

    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }
}
