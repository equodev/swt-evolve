package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

/**
 * What a keystroke costs on this side in a large highlighted document: the edit, the restyle of the
 * line it lands on, as a source editor's presentation reconciler sends it, and the update that
 * carries both to the client. The edit sits near the top, so every style run after it moves.
 *
 * <p>Not an assertion of a number - machines differ. It prints, so a change to the edit path can
 * be measured before and after on the same machine.
 */
@Tag("bench")
@ExtendWith(Mocks.class)
class StyledTextTypingBenchTest {

    private static final int CLIENT = 1;
    private static final int LINES = 6500;
    private static final int WARMUP = 50;
    private static final int RUNS = Integer.getInteger("bench.runs", 200);

    private Color[] palette;

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
        FlutterBridge.set(new RecordingBridge());
        Serializer.enterConnection(CLIENT);
        palette = new Color[8];
        for (int i = 0; i < palette.length; i++) palette[i] = new Color(Mocks.device(), 30 * i, 255 - 30 * i, 100);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    void keystrokeInALargeDocument() {
        StyledText text = new StyledText(Mocks.shell(), SWT.MULTI);
        // An editor's viewport: what is sent of the styles is a band around it, not the document.
        text.setBounds(0, 0, 1000, 600);
        String line = "        final Money total = prices.get(sku).times(quantity); // line";
        StringBuilder doc = new StringBuilder();
        for (int i = 0; i < LINES; i++) doc.append(line).append('\n');
        text.setText(doc.toString());
        text.setStyleRanges(styles(0, LINES, line.length() + 1));
        DartStyledText impl = (DartStyledText) text.getImpl();
        WireFrames.record(impl, CLIENT);

        // The tenth line, past its indent.
        int lineStart = 9 * (line.length() + 1);
        int at = lineStart + 14;
        long[] times = new long[RUNS];
        for (int i = 0; i < WARMUP + RUNS; i++) {
            long start = System.nanoTime();
            text.replaceTextRange(at, 0, "x");
            text.replaceStyleRanges(lineStart, line.length() + 1, styles(lineStart, 1, line.length() + 2));
            WireFrames.record(impl, CLIENT);
            text.replaceTextRange(at, 1, "");
            text.replaceStyleRanges(lineStart, line.length(), styles(lineStart, 1, line.length() + 1));
            WireFrames.record(impl, CLIENT);
            long took = System.nanoTime() - start;
            if (i >= WARMUP) times[i - WARMUP] = took / 2;
        }
        Arrays.sort(times);
        System.out.printf("[bench] StyledText keystroke, %d lines, %d runs: median %.1f us, p90 %.1f us%n",
                LINES, text.getStyleRanges(false).length, times[RUNS / 2] / 1000.0, times[RUNS * 9 / 10] / 1000.0);
    }

    /**
     * Six runs a line, as a source highlighter colours one, over {@code count} lines of
     * {@code lineLength} (its newline included) from offset {@code start}.
     */
    private StyleRange[] styles(int start, int count, int lineLength) {
        int[] widths = {8, 6, 6, 6, 3, 0};
        StyleRange[] out = new StyleRange[count * widths.length];
        int n = 0;
        for (int l = 0; l < count; l++) {
            int lineStart = start + l * lineLength;
            int at = lineStart;
            for (int k = 0; k < widths.length; k++) {
                // The last run takes what is left of the line.
                int width = k == widths.length - 1 ? lineLength - 1 - (at - lineStart) : widths[k];
                out[n++] = new StyleRange(at, width, palette[(l + k) % palette.length], null);
                at += width;
            }
        }
        return out;
    }
}
