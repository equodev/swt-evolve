package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Every op is drawn with the alpha in force when the application issued it, and alpha 0 (draw
 * nothing) has to be distinguishable from an alpha nobody set (draw opaque).
 */
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
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
        Shell shell = Mocks.swtShell();
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
    private String wireAfterDisposing(GC gc) {
        gc.dispose();
        return bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
    }

    @Test
    @DisplayName("alpha 0 reaches Flutter instead of looking like an alpha nobody set")
    void alphaZeroIsSent() {
        GC gc = freshGc();
        gc.setAlpha(0);
        gc.fillRectangle(0, 0, 75, 75);
        assertThat(wireAfterDisposing(gc))
                .as("a shape the application made fully transparent must not travel as opaque")
                .contains("\"alpha\":0");
    }

    @Test
    @DisplayName("each op carries the alpha in force when it was issued")
    void everyAlphaChangeReachesTheWire() {
        GC gc = freshGc();
        gc.setAlpha(20);
        gc.fillOval(0, 0, 50, 50);
        gc.setAlpha(200);
        gc.fillOval(50, 0, 50, 50);
        String wire = wireAfterDisposing(gc);
        assertThat(wire).as("sanity: both ops reached the wire").contains("fillOval");
        assertThat(wire)
                .as("the first oval's alpha")
                .contains("\"alpha\":20");
        assertThat(wire)
                .as("the second oval's alpha")
                .contains("\"alpha\":200");
    }
}
