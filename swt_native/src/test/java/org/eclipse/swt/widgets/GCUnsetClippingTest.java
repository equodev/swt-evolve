package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A GC that was never given a clipping region draws everywhere; one given an empty region draws
 * nothing. Both travel as the same field, so "unset" has to reach Flutter as absent — a
 * zero-rectangle default is indistinguishable from an empty region the application asked for, and
 * SWT's own {@code Rectangle.intersection} returns exactly that for disjoint rectangles.
 */
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class GCUnsetClippingTest {

    private static final String ZERO_RECT = "\"clipping\":{\"x\":0,\"y\":0,\"width\":0,\"height\":0}";

    private RecordingBridge bridge;
    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        asyncQueue.clear();
    }

    /** A GC on a Dart-backed Canvas — the drawable an application paints through. */
    private GC freshGc() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Canvas canvas = new Canvas(shell, org.eclipse.swt.SWT.NONE);
        canvas.setBounds(0, 0, 200, 100);
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
        bridge.comm.sent.clear();
        return new GC(canvas);
    }

    /** The GC's traffic, read after disposing it: a paint's ops are batched until then. */
    private String wireAfterDisposing(GC gc) {
        gc.drawLine(0, 0, 10, 10);
        gc.dispose();
        String wire = bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
        assertThat(wire).as("sanity: the GC's ops reached the wire").contains("drawLine");
        return wire;
    }

    @Test
    @DisplayName("a GC that was never clipped does not report an empty clipping region")
    void unsetClippingIsAbsent() {
        assertThat(wireAfterDisposing(freshGc()))
                .as("an unset clipping must not travel as a zero rectangle — that is what an "
                        + "empty region looks like, and it would discard every op")
                .doesNotContain(ZERO_RECT);
    }

    @Test
    @DisplayName("an explicitly empty clipping region still reaches Flutter")
    void emptyClippingIsSent() {
        GC gc = freshGc();
        gc.setClipping(new Rectangle(0, 0, 0, 0));
        assertThat(wireAfterDisposing(gc))
                .as("an application that clips everything away must be able to say so")
                .contains(ZERO_RECT);
    }

    @Test
    @DisplayName("a null clipping path leaves an unset region unset")
    void clippingPathDoesNotResurrectAZeroRectangle() {
        GC gc = freshGc();
        gc.setClipping((Path) null);
        assertThat(wireAfterDisposing(gc)).doesNotContain(ZERO_RECT);
    }
}
