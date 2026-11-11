package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.function.IntPredicate;

import static dev.equo.swt.harness.WebDisplayHarness.BLUE;
import static dev.equo.swt.harness.WebDisplayHarness.GREEN;
import static dev.equo.swt.harness.WebDisplayHarness.RED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A canvas that scrolls the way draw2d's FigureCanvas does: Canvas.scroll moves what is on screen,
 * then a redraw names only the strip the scroll exposed, and the Paint draws the content at the new
 * origin. Checked in pixels, since what the client shows outside that strip is what the scroll left.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CanvasScrollRepaintFlutterTest {

    private static final int SIZE = 200, BAND = 100;

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void clear() {
        for (Control c : shell.getChildren()) c.dispose();
        flutter.settle();
    }

    /**
     * Content three bands long, red, green and blue, drawn at {@link #origin}: stacked from the top
     * when {@code vertical}, else side by side from the left.
     */
    private final class ScrolledCanvas {
        final Canvas canvas;
        final boolean vertical;
        final Color[] bands = {new Color(display, 255, 0, 0), new Color(display, 0, 255, 0), new Color(display, 0, 0, 255)};
        int origin;

        ScrolledCanvas(boolean vertical) {
            this.vertical = vertical;
            canvas = new Canvas(shell, SWT.NO_BACKGROUND);
            canvas.setBounds(10, 10, SIZE, SIZE);
            canvas.addListener(SWT.Paint, e -> {
                for (int i = 0; i < bands.length; i++) {
                    e.gc.setBackground(bands[i]);
                    if (vertical) e.gc.fillRectangle(0, i * BAND - origin, SIZE, BAND);
                    else e.gc.fillRectangle(i * BAND - origin, 0, BAND, SIZE);
                }
            });
        }

        /** FigureCanvas.scrollToY or scrollToX: move the pixels by {@code d}, then repaint the strip they uncovered. */
        void scroll(int d) {
            if (vertical) canvas.scroll(0, 0, 0, d, SIZE, SIZE - d, true);
            else canvas.scroll(0, 0, d, 0, SIZE - d, SIZE, true);
            origin += d;
            if (vertical) canvas.redraw(0, SIZE - d, SIZE, d, true);
            else canvas.redraw(SIZE - d, 0, d, SIZE, true);
        }

        /** Share of the middle of the canvas's first or second band painted in {@code colour}. */
        double share(int band, IntPredicate colour) throws Exception {
            double start = (band * BAND + BAND / 4.0) / SIZE, length = BAND / 2.0 / SIZE;
            return vertical ? flutter.colourShare(canvas, colour, 0.25, start, 0.5, length)
                    : flutter.colourShare(canvas, colour, start, 0.25, length, 0.5);
        }
    }

    private void scrolledThenStripRepainted(boolean vertical) throws Exception {
        ScrolledCanvas c = new ScrolledCanvas(vertical);
        flutter.settle();
        assertThat(c.share(0, RED)).as("sanity: the first band before scrolling is red").isGreaterThan(0.9);
        assertThat(c.share(1, GREEN)).as("sanity: the second band before scrolling is green").isGreaterThan(0.9);

        c.scroll(BAND);
        c.canvas.update();
        flutter.settle();

        assertThat(c.share(0, GREEN)).as("the first band, which the scroll moved, is green").isGreaterThan(0.9);
        assertThat(c.share(1, BLUE)).as("the second band, which the strip repainted, is blue").isGreaterThan(0.9);
    }

    @Test
    @DisplayName("a canvas scrolled down and repainted in the strip it exposed shows the content at the new origin")
    void scrolledDownThenStripRepainted() throws Exception {
        scrolledThenStripRepainted(true);
    }

    @Test
    @DisplayName("a canvas scrolled right and repainted in the strip it exposed shows the content at the new origin")
    void scrolledRightThenStripRepainted() throws Exception {
        scrolledThenStripRepainted(false);
    }
}
