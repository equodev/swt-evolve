package dev.equo.swt.awt;

import org.junit.jupiter.api.Test;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An off-screen OpenGL canvas renders its whole scene on every paint, which wipes what it drew
 * outside a paint. It is painted again only where it was damaged.
 */
class CanvasDamageTest {

    /** Paints the canvas in one colour and records the clip of each paint (null = unclipped). */
    private static final class Painter implements Consumer<Graphics2D> {
        final List<Rectangle> clips = new ArrayList<>();
        int rgb;

        Painter(int rgb) {
            this.rgb = rgb;
        }

        @Override
        public void accept(Graphics2D g) {
            clips.add(g.getClipBounds());
            g.setColor(new Color(rgb));
            g.fillRect(0, 0, 100, 100);
        }
    }

    @Test
    void withNothingDamagedTheLastImageIsReusedWithoutPainting() {
        Canvas canvas = new Canvas();
        Painter painter = new Painter(0x0000FF);

        BufferedImage first = CanvasDamage.image(canvas, 20, 10, 1, 1, painter);
        BufferedImage second = CanvasDamage.image(canvas, 20, 10, 1, 1, painter);

        assertThat(second).isSameAs(first);
        assertThat(painter.clips).hasSize(1);
        assertThat(painter.clips.get(0)).isNull();
    }

    @Test
    void aRepaintTheCanvasAsksForPaintsOnlyThatArea() {
        Canvas canvas = new Canvas();
        Painter painter = new Painter(0x0000FF);
        CanvasDamage.image(canvas, 20, 10, 1, 1, painter);

        painter.rgb = 0xFF0000;
        CanvasDamage.repaintRequested(canvas, new Rectangle(2, 3, 4, 5));
        BufferedImage image = CanvasDamage.image(canvas, 20, 10, 1, 1, painter);

        assertThat(painter.clips.get(1)).isEqualTo(new Rectangle(2, 3, 4, 5));
        assertThat(image.getRGB(3, 4)).isEqualTo(0xFFFF0000);
        assertThat(image.getRGB(10, 4)).isEqualTo(0xFF0000FF);
    }

    @Test
    void aForcedRepaintPaintsTheWholeCanvas() {
        Container root = new Container();
        Canvas canvas = new Canvas();
        root.add(canvas);
        Painter painter = new Painter(0x0000FF);
        CanvasDamage.image(canvas, 20, 10, 1, 1, painter);
        CanvasDamage.repaintRequested(canvas, new Rectangle(0, 0, 1, 1));

        CanvasDamage.damageAll(root);
        CanvasDamage.image(canvas, 20, 10, 1, 1, painter);

        assertThat(painter.clips.get(1)).isNull();
    }

    @Test
    void aRepaintAskedForByAnAncestorPaintsTheWholeCanvas() {
        Container applet = new Container();
        Canvas canvas = new Canvas();
        applet.add(canvas);
        Painter painter = new Painter(0x0000FF);
        CanvasDamage.image(canvas, 20, 10, 1, 1, painter);

        CanvasDamage.repaintRequested(applet, new Rectangle(0, 0, 1, 1));
        CanvasDamage.image(canvas, 20, 10, 1, 1, painter);

        assertThat(painter.clips.get(1)).isNull();
    }

    @Test
    void aNewSizePaintsTheWholeCanvas() {
        Canvas canvas = new Canvas();
        Painter painter = new Painter(0x0000FF);
        CanvasDamage.image(canvas, 20, 10, 1, 1, painter);

        BufferedImage image = CanvasDamage.image(canvas, 30, 10, 1, 1, painter);

        assertThat(painter.clips).hasSize(2);
        assertThat(painter.clips.get(1)).isNull();
        assertThat(image.getWidth()).isEqualTo(30);
    }

    @Test
    void theDamagedAreaIsInTheCanvasOwnCoordinatesAtAnyScale() {
        Canvas canvas = new Canvas();
        Painter painter = new Painter(0x0000FF);
        CanvasDamage.image(canvas, 30, 15, 1.5, 1.5, painter);

        painter.rgb = 0xFF0000;
        CanvasDamage.repaintRequested(canvas, new Rectangle(2, 2, 2, 2));
        BufferedImage image = CanvasDamage.image(canvas, 30, 15, 1.5, 1.5, painter);

        assertThat(image.getRGB(4, 4)).isEqualTo(0xFFFF0000);
        assertThat(image.getRGB(7, 7)).isEqualTo(0xFF0000FF);
    }
}
