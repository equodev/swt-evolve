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

    @Test
    void aResizeTrackerFlipsWhenThePointerCrossesTheOtherVerticalEdge() {
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20), new Rectangle(30, 10, 10, 20)});
        Point[] edge = new Point[1];
        tracker.addListener(SWT.Resize, e -> edge[0] = null);

        open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 95, 100);
            display.asyncExec(() -> {
                Point left = shell.toDisplay(5, 20);
                pointer(tracker, left.x + 50, left.y);
                display.asyncExec(() -> {
                    Point right = shell.toDisplay(55, 20);
                    pointer(tracker, right.x - 30, right.y);
                    release(tracker);
                });
            });
        });

        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(25, 10, 9, 20), new Rectangle(34, 10, 4, 20));
    }

    @Test
    void aResizeTrackerFlipsWhenThePointerCrossesTheOtherHorizontalEdge() {
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20), new Rectangle(10, 30, 20, 10)});

        open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 100, 95);
            display.asyncExec(() -> {
                Point top = shell.toDisplay(20, 5);
                pointer(tracker, top.x, top.y + 50);
                display.asyncExec(() -> {
                    Point bottom = shell.toDisplay(20, 55);
                    pointer(tracker, bottom.x, bottom.y - 30);
                    release(tracker);
                });
            });
        });

        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(10, 25, 20, 9), new Rectangle(10, 34, 20, 4));
    }

    @Test
    void aSingleRectangleFlipsWithoutProportionsToMirror() {
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});

        open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 95, 95);
            display.asyncExec(() -> {
                Point corner = shell.toDisplay(5, 5);
                pointer(tracker, corner.x + 40, corner.y + 40);
                display.asyncExec(() -> {
                    Point other = shell.toDisplay(40, 40);
                    pointer(tracker, other.x - 20, other.y - 20);
                    release(tracker);
                });
            });
        });

        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(20, 20, 10, 10));
    }

    @Test
    void aCursorSetWhileResizingReplacesTheResizeCursor() {
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        tracker.addListener(SWT.Resize, e -> tracker.setCursor(display.getSystemCursor(SWT.CURSOR_HAND)));

        boolean ended = open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 104, 100);
            display.timerExec(50, () -> release(tracker));
        });

        assertThat(ended).isTrue();
        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(10, 10, 24, 20));
    }

    /** A Tracker resizing one way only starts on that edge, and does not flip past the opposite one. */
    @Test
    void aOneWayResizeTrackerDoesNotFlip() {
        int[][] cases = {
                // style, pointer delta x, delta y, then the push past the opposite edge
                {SWT.LEFT, 30, 0}, {SWT.RIGHT, -30, 0}, {SWT.UP, 0, 30}, {SWT.DOWN, 0, -30}};
        for (int[] c : cases) {
            Tracker tracker = new Tracker(shell, SWT.RESIZE | c[0]);
            tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
            open(tracker, () -> {
                pointer(tracker, 100, 100);
                pointer(tracker, 100 + c[1], 100 + c[2]);
                release(tracker);
            });
            assertThat(tracker.getRectangles()).as("style " + c[0]).containsExactly(new Rectangle(10, 10, 20, 20));
            tracker.dispose();
        }
    }

    @Test
    void aTrackerMovingRightAndDownOnlyIgnoresTheOtherWays() {
        Tracker tracker = new Tracker(shell, SWT.RIGHT | SWT.DOWN);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});

        open(tracker, () -> {
            pointer(tracker, 100, 100);
            pointer(tracker, 90, 90);
            pointer(tracker, 95, 95);
            release(tracker);
        });

        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(15, 15, 20, 20));
    }

    @Test
    void aTrackerWithoutRectanglesStillAnnouncesThePointer() {
        for (int style : new int[] {SWT.NONE, SWT.RESIZE}) {
            Tracker tracker = new Tracker(shell, style);
            listen(tracker);
            heard.clear();
            open(tracker, () -> {
                pointer(tracker, 100, 100);
                pointer(tracker, 120, 130);
                display.post(key(SWT.KeyDown, SWT.ARROW_UP, 0));
                display.asyncExec(() -> release(tracker));
            });
            assertThat(heard).as("style " + style).hasSize(3);
            assertThat(tracker.getRectangles()).isEmpty();
            tracker.dispose();
        }
    }

    @Test
    void aResizeTrackerOnTheDisplayOrWithACursorOfItsOwn() {
        Tracker onDisplay = new Tracker(display, SWT.RESIZE | SWT.LEFT | SWT.DOWN);
        onDisplay.setRectangles(new Rectangle[] {new Rectangle(50, 50, 20, 20)});
        open(onDisplay, () -> {
            pointer(onDisplay, 100, 100);
            pointer(onDisplay, 90, 110);
            release(onDisplay);
        });
        assertThat(onDisplay.getRectangles()).containsExactly(new Rectangle(40, 50, 30, 30));
        onDisplay.dispose();

        Tracker upOnly = new Tracker(shell, SWT.RESIZE | SWT.UP);
        upOnly.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        upOnly.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
        open(upOnly, () -> {
            pointer(upOnly, 100, 100);
            pointer(upOnly, 100, 95);
            release(upOnly);
        });
        assertThat(upOnly.getRectangles()).containsExactly(new Rectangle(10, 5, 20, 25));
    }

    @Test
    void aTrackerWithACursorAndAnInterruptedThreadKeepsTracking() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        tracker.setCursor(display.getSystemCursor(SWT.CURSOR_CROSS));
        try {
            boolean ended = open(tracker, () -> {
                Thread.currentThread().interrupt();
                display.timerExec(100, () -> {
                    pointer(tracker, 100, 100);
                    pointer(tracker, 101, 102);
                    release(tracker);
                });
            });
            assertThat(ended).isTrue();
            assertThat(tracker.getRectangles()).containsExactly(new Rectangle(11, 12, 20, 20));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void aTrackerDisposedDuringTheLoopEndsIt() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        listen(tracker);

        boolean ended = open(tracker, () -> {
            pointer(tracker, 100, 100);
            release(tracker);
            client(tracker, "Tracker/cancel", new Event());
            tracker.dispose();
        });

        assertThat(ended).as("ended unless cancelled, as upstream ends it at the release").isTrue();
        assertThat(heard).as("what the client sent before the dispose is dropped").isEmpty();
        assertThat(sentToShell("Tracker/close")).isTrue();
    }

    /** The keys queued after the one that ends or disposes the tracker are the focus control's again. */
    @Test
    void theKeysAfterTheOneThatEndsTheTrackerGoToTheFocus() {
        Text focused = new Text(shell, SWT.SINGLE);
        web.setFocus((DartControl) focused.getImpl());
        List<Integer> focusedHeard = new ArrayList<>();
        focused.addListener(SWT.KeyDown, e -> focusedHeard.add(e.keyCode));
        for (int ending : new int[] {SWT.ESC, 'd'}) {
            focusedHeard.clear();
            Tracker tracker = new Tracker(shell, SWT.NONE);
            tracker.addListener(SWT.KeyDown, e -> {
                if (e.keyCode == 'd')
                    tracker.dispose();
            });
            open(tracker, () -> {
                display.post(key(SWT.KeyDown, 'c', 0));
                display.post(key(SWT.KeyDown, ending, 0));
                display.post(key(SWT.KeyDown, 'b', 0));
            });
            while (display.readAndDispatch()) {
            }
            assertThat(focusedHeard).as("ended by " + ending).containsExactly(Integer.valueOf('b'));
            if (!tracker.isDisposed())
                tracker.dispose();
        }
    }

    @Test
    void aShellDisposedDuringTheLoopEndsItWithNothingToTell() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        boolean ended = open(tracker, shell::dispose);
        assertThat(ended).isTrue();
        assertThat(sentToShell("Tracker/close")).isFalse();
    }

    @Test
    void aParentDisposedBeforeOpenEndsItAtOnce() {
        Composite composite = new Composite(shell, SWT.NONE);
        Tracker tracker = new Tracker(composite, SWT.NONE);
        composite.dispose();
        assertThat(open(tracker, () -> {
        })).isTrue();
        assertThat(sentToShell("Tracker/open")).isFalse();
    }

    @Test
    void theKeysGoBackToTheFocusWhenAListenerThrowsOutOfTheLoop() {
        Text focused = new Text(shell, SWT.SINGLE);
        web.setFocus((DartControl) focused.getImpl());
        List<Integer> focusedHeard = new ArrayList<>();
        focused.addListener(SWT.KeyDown, e -> focusedHeard.add(e.keyCode));
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.addListener(SWT.Move, e -> {
            throw new IllegalStateException("listener failed");
        });

        try {
            open(tracker, () -> pointer(tracker, 100, 100));
        } catch (RuntimeException expected) {
        }
        display.post(key(SWT.KeyDown, 'a', 0));
        while (display.readAndDispatch()) {
        }

        assertThat(focusedHeard).containsExactly(Integer.valueOf('a'));
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
    void aTrackerOnTheDisplayFollowsTheActiveShell() {
        Shell other = new Shell(display);
        other.setBounds(0, 0, 200, 200);
        other.open();
        Tracker tracker = new Tracker(display, SWT.NONE);
        open(tracker, () -> release(tracker));
        Shell active = display.getActiveShell();
        assertThat(active).isNotNull();
        String channel = "/" + active.hashCode() + "/Tracker/open";
        assertThat(web.comm.sent.stream().anyMatch(f -> f.event.contains(channel))).isTrue();
        tracker.dispose();
    }

    /** A Shell that takes no focus is never the active one, but the pointer is still seen over it. */
    @Test
    void aTrackerOnTheDisplayWithNoActiveShellFollowsAVisibleOne() {
        shell.setVisible(false);
        Shell noFocus = new Shell(display, SWT.NO_FOCUS);
        noFocus.setBounds(0, 0, 200, 200);
        noFocus.setVisible(true);
        assertThat(display.getActiveShell()).isNull();
        Tracker tracker = new Tracker(display, SWT.NONE);

        open(tracker, () -> release(tracker));

        String channel = "/" + noFocus.hashCode() + "/Tracker/open";
        assertThat(web.comm.sent.stream().anyMatch(f -> f.event.contains(channel))).isTrue();
        tracker.dispose();
    }

    @Test
    void anArrowUpSteps() {
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 20, 5, 5)});
        open(tracker, () -> {
            display.post(key(SWT.KeyDown, SWT.ARROW_UP, SWT.CTRL));
            display.asyncExec(() -> release(tracker));
        });
        assertThat(tracker.getRectangles()).containsExactly(new Rectangle(10, 19, 5, 5));
    }

    @Test
    void aTrackerDisposedByAListenerOfAnArrowIsCancelled() {
        for (int style : new int[] {SWT.NONE, SWT.RESIZE}) {
            Tracker tracker = new Tracker(shell, style);
            tracker.setRectangles(new Rectangle[] {new Rectangle(10, 20, 5, 5)});
            tracker.addListener(SWT.Move, e -> tracker.dispose());
            tracker.addListener(SWT.Resize, e -> tracker.dispose());
            boolean ended = open(tracker, () -> display.post(key(SWT.KeyDown, SWT.ARROW_RIGHT, 0)));
            assertThat(ended).as("style " + style).isFalse();
        }
    }

    @Test
    void aResizeTrackerDisposedByItsListenerIsCancelled() {
        Tracker tracker = new Tracker(shell, SWT.RESIZE);
        tracker.setRectangles(new Rectangle[] {new Rectangle(10, 10, 20, 20)});
        tracker.addListener(SWT.Resize, e -> tracker.dispose());
        assertThat(open(tracker, () -> pointer(tracker, 100, 100))).isFalse();
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

        open(tracker, () -> {
            display.post(key(SWT.KeyDown, SWT.ARROW_RIGHT, 0));
            display.post(key(SWT.KeyUp, SWT.ARROW_RIGHT, 0));
            display.post(key(SWT.KeyDown, SWT.ARROW_DOWN, SWT.CTRL));
            display.asyncExec(() -> release(tracker));
        });

        assertThat(heard.get(0)).isEqualTo("Move " + (anchor.x + 9) + "," + anchor.y + " " + rects(new Rectangle(19, 10, 20, 20)));
        assertThat(tracker.getRectangles()).as("the release steps nothing, as on Win32 and GTK").containsExactly(new Rectangle(19, 11, 20, 20));
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
