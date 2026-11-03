package dev.equo.swt.awt;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Front-buffer drawing on an off-screen OpenGL canvas, such as an XOR tracking cursor, is read back
 * as a layer drawn over black and XORed onto the canvas's last painted frame.
 */
class FrontBufferOverlayTest {

    private static final int SCENE = 0xFF203040;
    private static final int CYAN = 0x0000FFFF;

    private static int[] frame(int w, int h) {
        int[] frame = new int[w * h];
        Arrays.fill(frame, SCENE);
        return frame;
    }

    /** An overlay of the given size, empty but for {@code bits} at each (x, y) pair. */
    private static int[] overlay(int w, int h, int bits, int... xy) {
        int[] overlay = new int[2 + w * h];
        overlay[0] = w;
        overlay[1] = h;
        for (int i = 0; i < xy.length; i += 2) overlay[2 + xy[i + 1] * w + xy[i]] = bits;
        return overlay;
    }

    @Test
    void theDrawingShowsAsItWouldOnScreen() {
        int[] frame = frame(10, 8);

        EvolveSwingHost.xorOverlay(frame, 10, 8, 2, 1, 4, 3, overlay(4, 3, CYAN, 1, 2));

        assertThat(frame[(1 + 2) * 10 + 2 + 1]).isEqualTo(SCENE ^ CYAN);
        assertThat(frame[(1 + 2) * 10 + 2 + 1] >>> 24).isEqualTo(0xFF);
        assertThat(frame[(1 + 2) * 10 + 2 + 2]).isEqualTo(SCENE);
        assertThat(frame[0]).isEqualTo(SCENE);
    }

    @Test
    void drawingItAgainErasesIt() {
        int[] frame = frame(10, 8);
        int[] cursor = overlay(4, 3, CYAN, 0, 0, 3, 2);

        EvolveSwingHost.xorOverlay(frame, 10, 8, 2, 1, 4, 3, cursor);
        EvolveSwingHost.xorOverlay(frame, 10, 8, 2, 1, 4, 3, cursor);

        assertThat(frame).isEqualTo(frame(10, 8));
    }

    @Test
    void theOverlayIsStretchedOverTheCanvasRegion() {
        int[] frame = frame(10, 8);

        EvolveSwingHost.xorOverlay(frame, 10, 8, 0, 0, 6, 4, overlay(4, 3, CYAN, 3, 0));

        assertThat(frame[5]).isEqualTo(SCENE ^ CYAN);
        assertThat(frame[4]).isEqualTo(SCENE);
        assertThat(frame[10 + 5]).isEqualTo(SCENE ^ CYAN);
    }

    @Test
    void whatFallsOutsideTheFrameIsDropped() {
        int[] frame = frame(4, 4);

        EvolveSwingHost.xorOverlay(frame, 4, 4, -2, 2, 4, 4, overlay(4, 4, CYAN, 0, 0, 3, 1, 2, 3));

        assertThat(frame[3 * 4 + 1]).isEqualTo(SCENE ^ CYAN);
        assertThat(Arrays.stream(frame).filter(p -> p != SCENE)).hasSize(1);
    }
}
