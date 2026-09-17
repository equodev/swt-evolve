package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * An application that owner-draws its own table onto a Composite reaches for a TextLayout as soon
 * as a cell needs styled runs, and stays on GC.drawString for the plain ones. Both end up on the
 * same GC, so both have to reach Flutter as draw ops.
 */
@Tag("native-unit")
class TextLayoutDrawNativeTest {

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** A GC on a Dart-backed Canvas — the drawable an application paints through. */
    private GC freshGc() {
        Display display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 400);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        bridge.comm.sent.clear();
        return new GC(canvas);
    }

    /** The GC's traffic, read after disposing it: a paint's ops are batched until then. */
    private String wire() {
        return bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
    }

    @Test
    @DisplayName("a row's plain cell and its styled cell both reach Flutter")
    void textLayoutDrawReachesTheWire() {
        GC gc = freshGc();
        gc.drawString("Thursday 07:25 PM", 300, 4);
        TextLayout layout = new TextLayout(gc.getDevice());
        layout.setText("Fix the commit list");
        layout.draw(gc, 20, 4);
        layout.dispose();
        gc.dispose();

        assertThat(wire()).as("the plain cell").contains("Thursday 07:25 PM");
        assertThat(wire()).as("the styled cell").contains("Fix the commit list");
    }

    @Test
    @DisplayName("each styled run carries its own foreground")
    void styledRunsAreDrawnSeparately() {
        GC gc = freshGc();
        TextLayout layout = new TextLayout(gc.getDevice());
        layout.setText("refactor gc");
        layout.setStyle(new TextStyle(null, gc.getDevice().getSystemColor(SWT.COLOR_RED), null), 0, 7);
        layout.draw(gc, 0, 0);
        layout.dispose();
        gc.dispose();

        assertThat(wire()).as("the styled run").contains("refactor");
        assertThat(wire()).as("the unstyled run").contains(" gc");
        assertThat(foregrounds()).as("one foreground per run").hasSizeGreaterThan(1);
    }

    /** The distinct foreground colours the GC state carried over this paint. */
    private java.util.Set<String> foregrounds() {
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        String w = wire();
        String key = "\"foreground\":";
        for (int i = w.indexOf(key); i >= 0; i = w.indexOf(key, i + 1)) {
            int end = w.indexOf('}', i);
            seen.add(w.substring(i, end < 0 ? w.length() : end));
        }
        return seen;
    }
}
