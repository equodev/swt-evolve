package dev.equo.swt.awt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.PaintEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * With the front-buffer redirect on, the embedding paints an off-screen OpenGL canvas only when the
 * canvas or the host asks for it, and pushes each frame with the canvas's front-buffer drawing on top.
 */
class OffscreenCanvasPushTest {

    private static final int W = 40;
    private static final int H = 30;

    /** An off-screen capable canvas that records the clip of each paint (null = unclipped). */
    static final class CountingCanvas extends NestedHeavyweightTest.OffscreenCapableCanvas {
        final List<Rectangle> clips = new ArrayList<>();

        @Override
        public boolean isShowing() {
            return true;
        }

        @Override
        public void paint(Graphics g) {
            clips.add(g.getClipBounds());
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /** The embedding's lightweight content root. */
    static final class Root extends Container {
        @Override
        public boolean isLightweight() {
            return true;
        }
    }

    private final CountingCanvas canvas = new CountingCanvas();
    private final Root root = new Root();
    private final Runnable push = () -> EvolveSwingHost.paintHeavyweights(root, new int[W * H], W, H, 1, 1);
    private String redirect;

    @BeforeEach
    void setUp() {
        redirect = System.setProperty(GlFrontBuffer.PROPERTY, "true");
        canvas.setBounds(3, 4, 10, 8);
        canvas.setOffScreenBuffer(true);
        root.add(canvas);
        EvolveSwingHost.HEAVYWEIGHT_PUSHES.put(root, () -> {});
    }

    @AfterEach
    void tearDown() {
        EvolveSwingHost.HEAVYWEIGHT_PUSHES.remove(root);
        if (redirect == null) System.clearProperty(GlFrontBuffer.PROPERTY);
        else System.setProperty(GlFrontBuffer.PROPERTY, redirect);
    }

    @Test
    void aPlainPushReusesTheCanvasLastPaint() {
        push.run();
        push.run();

        assertThat(canvas.clips).hasSize(1);
        assertThat(canvas.clips.get(0)).isNull();
    }

    @Test
    void aRepaintTheCanvasAsksForPaintsOnlyThatArea() {
        push.run();

        EvolveSwingHost.heavyweightRepainted(new PaintEvent(canvas, PaintEvent.UPDATE, new Rectangle(1, 2, 3, 4)));
        push.run();
        push.run();

        assertThat(canvas.clips).hasSize(2);
        assertThat(canvas.clips.get(1)).isEqualTo(new Rectangle(1, 2, 3, 4));
    }

    @Test
    void aForcedRepaintPaintsTheWholeCanvas() {
        push.run();

        EvolveSwingHost.forceRepaint(root, push).run();

        assertThat(canvas.clips).hasSize(2);
        assertThat(canvas.clips.get(1)).isNull();
    }

    @Test
    void thePushedFrameCarriesTheCanvasFrontBufferDrawing() {
        int[] base = new int[W * H];
        Arrays.fill(base, 0xFF203040);
        int[] overlay = new int[2 + 10 * 8];
        overlay[0] = 10;
        overlay[1] = 8;
        overlay[2 + 2 * 10 + 1] = 0x0000FFFF;

        int[] frame = EvolveSwingHost.withFrontBufferOverlays(root, base, W, H, 1, 1,
                c -> c == canvas ? overlay : null);

        assertThat(frame[(4 + 2) * W + 3 + 1]).isEqualTo(0xFF203040 ^ 0x0000FFFF);
        assertThat(Arrays.stream(frame).filter(p -> p != 0xFF203040)).hasSize(1);
        assertThat(base).containsOnly(0xFF203040);
    }
}
