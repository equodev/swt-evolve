package dev.equo.swt.awt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The off-screen frame reports its size in points but allocates its pixel buffer at the display
 * scale, and tells the content the two differ through the {@code scaleX}/{@code scaleY} arguments of
 * {@code LightweightContent.imageBufferReset}. Reading the buffer as if those were the same number
 * copies the top-left corner of a magnified image: on a 150% display the embedded panel came out
 * oversized with its right-hand content clipped away entirely.
 *
 * <p>These run without a display: what is under test is the indexing, not AWT's rendering.
 */
class SwingHostBufferScaleTest {

    /** A buffer whose every pixel encodes its own (col,row), so a mis-indexed copy is identifiable. */
    private static int[] bufferOf(int strideWidth, int height) {
        int[] buf = new int[strideWidth * height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < strideWidth; col++) {
                buf[row * strideWidth + col] = (row << 16) | col;
            }
        }
        return buf;
    }

    @Test
    void anUnscaledDisplayLeavesTheExtentAlone() {
        assertThat(EvolveSwingHost.pixelExtent(420, 1.0)).isEqualTo(420);
    }

    @Test
    void aScaledDisplayGivesThePixelExtentNotThePointOne() {
        assertThat(EvolveSwingHost.pixelExtent(420, 1.5)).isEqualTo(630);
        assertThat(EvolveSwingHost.pixelExtent(208, 1.5)).isEqualTo(312);
        assertThat(EvolveSwingHost.pixelExtent(420, 1.25)).isEqualTo(525);
    }

    @Test
    void anExtentNeverCollapsesToZero() {
        // A frame can report 0 before it is laid out; a zero-sized copy would index out of bounds.
        assertThat(EvolveSwingHost.pixelExtent(0, 1.5)).isEqualTo(1);
    }

    @Test
    void theWholeScaledFrameIsCopiedOut() {
        // 420x208 points at 1.5 => a 630x312 buffer, which is also its stride here.
        int w = 630, h = 312;
        int[] snapshot = EvolveSwingHost.copyFrameBuffer(bufferOf(w, h), w, w, h);

        assertThat(snapshot).hasSize(w * h);
        // The far corner is the pixel a point-sized read would have clipped off.
        assertThat(snapshot[(h - 1) * w + (w - 1)]).isEqualTo(((h - 1) << 16) | (w - 1));
    }

    @Test
    void readingAScaledBufferAtItsPointWidthTakesTheTopLeftCrop() {
        // The regression this guards: stride is in pixels (630) while the frame's own width is in
        // points (420). Asking for 420 columns yields the left 2/3 of each of the top 2/3 of rows —
        // the magnified crop that put the embedded button outside the canvas.
        int strideW = 630, h = 312, pointW = 420, pointH = 208;
        int[] cropped = EvolveSwingHost.copyFrameBuffer(bufferOf(strideW, h), strideW, pointW, pointH);

        assertThat(cropped).hasSize(pointW * pointH);
        // Row 0 stops at column 419: everything to its right is gone.
        assertThat(cropped[pointW - 1]).isEqualTo(pointW - 1);
        // And the last row copied is 207, not 311 — the bottom third never arrives.
        assertThat(cropped[(pointH - 1) * pointW]).isEqualTo((pointH - 1) << 16);
    }

    @Test
    void paddingCarriedByTheStrideIsDropped() {
        // A buffer wider than the frame (row padding) must still yield exactly w columns per row.
        int strideW = 700, w = 630, h = 4;
        int[] snapshot = EvolveSwingHost.copyFrameBuffer(bufferOf(strideW, h), strideW, w, h);

        assertThat(snapshot).hasSize(w * h);
        for (int row = 0; row < h; row++) {
            assertThat(snapshot[row * w]).as("row %d starts at its own column 0", row)
                    .isEqualTo(row << 16);
        }
    }
}
