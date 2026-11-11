package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A canvas painted from a buffer image it drew once, as JFace's ruler columns paint the editor's
 * line numbers and annotations: later paints blit the same buffer without drawing it again. Checked
 * in pixels, since the client draws a placeholder rather than failing when it cannot find the image.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BufferedCanvasRepaintFlutterTest {

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
        settle();
    }

    private void settle() {
        flutter.settle();
    }

    private double redShare(Control w) throws Exception {
        return flutter.colourShare(w, WebDisplayHarness.RED);
    }

    /**
     * A canvas that draws its buffer in {@link #fill} on its first paint, and again only when told
     * to ({@link #stale}), and blits it on every paint.
     */
    private static final class BufferedCanvas {
        final Canvas canvas;
        Image buffer;
        Color fill;
        boolean stale;
        int bufferDraws;

        BufferedCanvas(Composite parent, Color fill) {
            this.fill = fill;
            canvas = new Canvas(parent, SWT.NONE);
            canvas.setBounds(10, 10, 60, 200);
            canvas.addListener(SWT.Paint, e -> {
                if (buffer == null) buffer = new Image(parent.getDisplay(), 60, 200);
                if (bufferDraws == 0 || stale) {
                    GC gc = new GC(buffer);
                    gc.setBackground(this.fill);
                    gc.fillRectangle(0, 0, 60, 200);
                    gc.dispose();
                    bufferDraws++;
                    stale = false;
                }
                e.gc.drawImage(buffer, 0, 0);
            });
            canvas.addListener(SWT.Dispose, e -> {
                if (buffer != null) buffer.dispose();
            });
        }
    }

    @Test
    @DisplayName("a canvas repainted from the buffer it drew once still shows the buffer")
    void repaintedFromItsBuffer() throws Exception {
        Color red = new Color(display, 255, 0, 0);
        BufferedCanvas ruler = new BufferedCanvas(shell, red);
        settle();
        assertThat(redShare(ruler.canvas)).as("sanity: the first paint shows the buffer").isGreaterThan(0.9);

        for (int repaint = 0; repaint < 3; repaint++) {
            ruler.canvas.redraw();
            ruler.canvas.update();
            settle();
            assertThat(redShare(ruler.canvas)).as("repaint %d, the buffer blitted again", repaint).isGreaterThan(0.9);
        }
        assertThat(ruler.bufferDraws).as("the buffer is drawn once and only blitted after").isEqualTo(1);
    }

    @Test
    @DisplayName("a buffered canvas hidden and shown again shows its buffer")
    void hiddenAndShownAgain() throws Exception {
        Color red = new Color(display, 255, 0, 0);
        Composite holder = new Composite(shell, SWT.NONE);
        holder.setBounds(0, 0, 300, 250);
        BufferedCanvas ruler = new BufferedCanvas(holder, red);
        settle();
        assertThat(redShare(ruler.canvas)).as("sanity: the first paint shows the buffer").isGreaterThan(0.9);

        holder.setVisible(false);
        settle();
        holder.setVisible(true);
        settle();

        assertThat(redShare(ruler.canvas)).as("shown again, from the same buffer").isGreaterThan(0.9);
    }

    @Test
    @DisplayName("a canvas blitting its buffer drawn again shows what the buffer holds now")
    void bufferDrawnAgain() throws Exception {
        // The canvas's paint is the same blit both times; only the buffer under the name changed,
        // as a ruler's buffer does when the editor scrolls.
        BufferedCanvas ruler = new BufferedCanvas(shell, new Color(display, 255, 0, 0));
        settle();
        assertThat(redShare(ruler.canvas)).as("sanity: the first paint shows the buffer").isGreaterThan(0.9);

        ruler.fill = new Color(display, 0, 255, 0);
        ruler.stale = true;
        ruler.canvas.redraw();
        ruler.canvas.update();
        settle();

        assertThat(ruler.bufferDraws).as("sanity: the buffer was drawn again").isEqualTo(2);
        assertThat(flutter.colourShare(ruler.canvas, WebDisplayHarness.GREEN)).as("the blit shows the buffer as drawn again").isGreaterThan(0.9);
    }
}
