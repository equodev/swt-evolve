package org.eclipse.swt.custom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The geometry rows the client pushes, read as its {@code GeometryRowWriter} writes them: a pixel
 * value is a step in whole hundredths from the row before, or a double where it is not a whole
 * hundredth, and a row after such a value steps from zero.
 */
class StyledTextGeometryRowReaderTest {

    @Test
    @DisplayName("pixels that are not whole hundredths read back as written, and the next row steps from zero")
    void doublesReadBackAndTheNextRowStartsAfresh() {
        Push push = new Push(3);
        // Row 0: line 0, offsets [0, 4]; x and height are not whole hundredths.
        push.row(0, 0, 4).exact(0.125).hundredths(0).hundredths(4000).exact(15.333).noCharX();
        // Row 1: line 1, offsets [5, 9]. Its top steps from zero, as the row before ended on a double.
        push.row(1, 1, 4).hundredths(0).hundredths(1533).hundredths(3000).hundredths(1500).noCharX();
        // Row 2: line 2, offsets [10, 14]; its top is not a whole hundredth, its height steps from the row before.
        push.row(1, 1, 4).hundredths(0).exact(30.5).hundredths(3000).hundredths(0).noCharX();

        StyledTextHelper.GeometryPush read = StyledTextHelper.readPush(push.bytes());

        assertThat(read.lines).hasSize(3);
        StyledTextHelper.VisualLine first = read.lines[0];
        assertThat(first.x).isEqualTo(0.125);
        assertThat(first.h).isEqualTo(15.333);
        assertThat(first.w).isEqualTo(40.0);
        StyledTextHelper.VisualLine second = read.lines[1];
        assertThat(second.logicalLine).isEqualTo(1);
        assertThat(second.start).isEqualTo(5);
        assertThat(second.end).isEqualTo(9);
        assertThat(second.y).isEqualTo(15.33);
        assertThat(second.h).isEqualTo(15.0);
        StyledTextHelper.VisualLine third = read.lines[2];
        assertThat(third.y).isEqualTo(30.5);
        assertThat(third.h).isEqualTo(15.0);
    }

    @Test
    @DisplayName("a row's character positions read back as written, doubles among them")
    void characterPositionsReadBack() {
        Push push = new Push(1);
        push.row(0, 0, 2).hundredths(0).hundredths(0).hundredths(1600).hundredths(1500)
                .charX().hundredths(0).exact(7.777).hundredths(1600);

        StyledTextHelper.VisualLine row = StyledTextHelper.readPush(push.bytes()).lines[0];

        assertThat(row.charX).containsExactly(0.0, 7.777, 16.0);
    }

    /** A whole push of {@code rows} rows, written as the client writes one. */
    private static final class Push {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Push(int rows) {
            out.write(0);          // a whole table, not a splice
            zigzag(1);             // version
            zigzag(100);           // charCount
            float64(400);          // contentWidth
            float64(600);          // contentHeight
            varlong(rows);
        }

        /** Starts a row: the line it is on, the offset it starts at and its length, as steps. */
        Push row(int lineStep, int startStep, int length) {
            zigzag(lineStep);
            zigzag(startStep);
            zigzag(length);
            return this;
        }

        /** A pixel value as a step in hundredths from the one it is read against. */
        Push hundredths(long step) {
            long z = (step << 1) ^ (step >> 63);
            varlong(z << 1);
            return this;
        }

        /** A pixel value that is not a whole hundredth. */
        Push exact(double value) {
            varlong(1);
            float64(value);
            return this;
        }

        /** Ends a row with no character positions; the vertical indent is 0. */
        Push noCharX() {
            zigzag(0);
            out.write(0);
            return this;
        }

        /** Ends a row's pixels: the vertical indent is 0 and its three character positions follow. */
        Push charX() {
            zigzag(0);
            out.write(1);
            varlong(3);
            return this;
        }

        byte[] bytes() {
            return out.toByteArray();
        }

        private void zigzag(long v) {
            varlong((v << 1) ^ (v >> 63));
        }

        private void varlong(long v) {
            while ((v & ~0x7FL) != 0) {
                out.write((int) ((v & 0x7F) | 0x80));
                v >>>= 7;
            }
            out.write((int) v);
        }

        private void float64(double d) {
            long bits = Double.doubleToLongBits(d);
            for (int i = 0; i < 8; i++) out.write((int) (bits >>> (8 * i)));
        }
    }
}
