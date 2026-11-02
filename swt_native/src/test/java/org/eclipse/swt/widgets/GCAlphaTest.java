package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.graphics.GC;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Every op is drawn with the alpha in force when the application issued it, and alpha 0 (draw
 * nothing) has to be distinguishable from an alpha nobody set (draw opaque).
 */
@ExtendWith(Mocks.class)
class GCAlphaTest {

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
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Canvas canvas = new Canvas(shell, org.eclipse.swt.SWT.NONE);
        canvas.setBounds(0, 0, 400, 400);
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
        bridge.comm.sent.clear();
        return new GC(canvas);
    }

    /** The GC's traffic, read after disposing it: a paint's ops are batched until then. */
    private List<GcFrames.Entry> wireAfterDisposing(GC gc) {
        gc.dispose();
        return BatchEntries.of(bridge.comm.sent);
    }

    /** Whether any value in [wire] sets the alpha to [alpha]. */
    private static boolean carriesAlpha(List<GcFrames.Entry> wire, int alpha) {
        List<Object> values = wire.stream().map(GcFrames.Entry::value).toList();
        return !WireValues.find(values, m -> m.containsKey("alpha") && WireValues.same(m.get("alpha"), alpha)).isEmpty();
    }

    @Test
    @DisplayName("alpha 0 reaches Flutter instead of looking like an alpha nobody set")
    void alphaZeroIsSent() {
        GC gc = freshGc();
        gc.setAlpha(0);
        gc.fillRectangle(0, 0, 75, 75);
        assertThat(carriesAlpha(wireAfterDisposing(gc), 0))
                .as("a shape the application made fully transparent must not travel as opaque")
                .isTrue();
    }

    @Test
    @DisplayName("each op carries the alpha in force when it was issued")
    void everyAlphaChangeReachesTheWire() {
        GC gc = freshGc();
        gc.setAlpha(20);
        gc.fillOval(0, 0, 50, 50);
        gc.setAlpha(200);
        gc.fillOval(50, 0, 50, 50);
        List<GcFrames.Entry> wire = wireAfterDisposing(gc);
        assertThat(wire).as("sanity: both ops reached the wire").anyMatch(e -> e.channel().contains("fillOval"));
        assertThat(carriesAlpha(wire, 20))
                .as("the first oval's alpha")
                .isTrue();
        assertThat(carriesAlpha(wire, 200))
                .as("the second oval's alpha")
                .isTrue();
    }
}
