package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
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

import java.util.List;
import java.util.OptionalInt;

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
    private List<GcFrames.Entry> wire() {
        return BatchEntries.of(bridge.comm.sent);
    }

    /** What each entry of the GC's traffic carries. */
    private List<Object> values() {
        return wire().stream().map(GcFrames.Entry::value).toList();
    }

    /** The y the first {@code drawLine} of the paint starts at. */
    private OptionalInt firstLineY() {
        return wire().stream()
                .filter(e -> e.channel().contains("drawLineintintintint"))
                .mapToInt(e -> ((Number) ((List<?>) e.value()).get(1)).intValue())
                .findFirst();
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

        assertThat(WireValues.containsText(values(), "Thursday 07:25 PM")).as("the plain cell").isTrue();
        assertThat(WireValues.containsText(values(), "Fix the commit list")).as("the styled cell").isTrue();
    }

    @Test
    @DisplayName("each styled run carries its own foreground")
    void styledRunsAreDrawnSeparately() {
        GC gc = freshGc();
        TextLayout layout = new TextLayout(gc.getDevice());
        layout.setText("refactor gc");
        // Not a system colour: the mocked display answers every one with the same Color, which is
        // also the canvas's foreground the GC starts from.
        layout.setStyle(new TextStyle(null, new Color(gc.getDevice(), 255, 0, 0), null), 0, 7);
        layout.draw(gc, 0, 0);
        layout.dispose();
        gc.dispose();

        assertThat(WireValues.containsText(values(), "refactor")).as("the styled run").isTrue();
        assertThat(WireValues.containsText(values(), " gc")).as("the unstyled run").isTrue();
        assertThat(foregrounds()).as("one foreground per run").hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("an underline sits just under the baseline of its run, not at the bottom of the line")
    void underlineHugsTheBaseline() {
        GC gc = freshGc();
        // A link styled with its control's font, smaller than the layout's own, which sizes the line.
        Font small = new Font(gc.getDevice(), "System", 8, SWT.NORMAL);
        Font big = new Font(gc.getDevice(), "System", 24, SWT.NORMAL);
        TextLayout layout = new TextLayout(gc.getDevice());
        layout.setFont(big);
        layout.setText("id appointment_id");
        TextStyle link = new TextStyle(small, null, null);
        link.underline = true;
        layout.setStyle(link, 3, layout.getText().length() - 1);
        int baseline = layout.getLineMetrics(0).getAscent();
        int lineHeight = layout.getLineBounds(0).height;
        layout.draw(gc, 0, 0);
        layout.dispose();
        gc.dispose();
        small.dispose();
        big.dispose();

        OptionalInt line = firstLineY();
        assertThat(line.isPresent()).as("the underline reached the wire").isTrue();
        assertThat(baseline + 1).as("sanity: the line has room below the run's baseline").isLessThan(lineHeight - 1);
        assertThat(line.getAsInt()).isEqualTo(baseline + 1);
    }

    @Test
    @DisplayName("a strikeout crosses the middle of its run, not of the line")
    void strikeoutCrossesItsRun() {
        GC gc = freshGc();
        Font small = new Font(gc.getDevice(), "System", 8, SWT.NORMAL);
        TextLayout alone = new TextLayout(gc.getDevice());
        alone.setFont(small);
        alone.setText("dropped");
        FontMetrics run = alone.getLineMetrics(0);
        alone.dispose();
        // A layout font much taller than the run, so the run's middle sits well below the line's.
        Font big = new Font(gc.getDevice(), "System", 24, SWT.NORMAL);
        TextLayout layout = new TextLayout(gc.getDevice());
        layout.setFont(big);
        layout.setText("kept dropped");
        TextStyle struck = new TextStyle(small, null, null);
        struck.strikeout = true;
        layout.setStyle(struck, 5, layout.getText().length() - 1);
        int runTop = layout.getLineMetrics(0).getAscent() - run.getAscent();
        int lineHeight = layout.getLineBounds(0).height;
        layout.draw(gc, 0, 0);
        layout.dispose();
        gc.dispose();
        small.dispose();
        big.dispose();

        OptionalInt line = firstLineY();
        assertThat(line.isPresent()).as("the strikeout reached the wire").isTrue();
        assertThat(runTop + run.getHeight() / 2).as("sanity: the run's middle is below the line's").isGreaterThan(lineHeight / 2);
        assertThat(line.getAsInt()).isEqualTo(runTop + run.getHeight() / 2);
    }

    /** The distinct foreground colours the GC state carried over this paint. */
    private java.util.Set<Object> foregrounds() {
        java.util.Set<Object> seen = new java.util.LinkedHashSet<>();
        for (java.util.Map<String, Object> state : WireValues.find(values(), m -> m.containsKey("foreground"))) {
            seen.add(state.get("foreground"));
        }
        return seen;
    }
}
