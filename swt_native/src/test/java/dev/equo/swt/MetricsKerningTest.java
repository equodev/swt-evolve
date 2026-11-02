package dev.equo.swt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** A kerning pair's characters may be a comma or a minus sign, so the table is read by position. */
class MetricsKerningTest {

    private static Metrics withKerning(String kerning) {
        return new Metrics(1, 0.2, 1.2, 5, new double[95], kerning);
    }

    @Test
    void readsEachPairsAdjustment() {
        Metrics metrics = withKerning("Te-1953,To-1709,f\"732,");

        assertThat(metrics.kerning('T', 'e')).isCloseTo(-0.1953, within(1e-9));
        assertThat(metrics.kerning('T', 'o')).isCloseTo(-0.1709, within(1e-9));
        assertThat(metrics.kerning('f', '"')).isCloseTo(0.0732, within(1e-9));
    }

    @Test
    void readsPairsOfCommasMinusSignsAndDigits() {
        Metrics metrics = withKerning(",--12,-1-3,1-5,,,7,");

        assertThat(metrics.kerning(',', '-')).isCloseTo(-0.0012, within(1e-9));
        assertThat(metrics.kerning('-', '1')).isCloseTo(-0.0003, within(1e-9));
        assertThat(metrics.kerning('1', '-')).isCloseTo(0.0005, within(1e-9));
        assertThat(metrics.kerning(',', ',')).isCloseTo(0.0007, within(1e-9));
    }

    @Test
    void aPairTheTableLeavesOutHasNone() {
        Metrics metrics = withKerning("Te-1953,");

        assertThat(metrics.kerning('e', 'T')).isZero();
        assertThat(metrics.kerning('T', 0x2014)).as("a character the table has no glyphs for").isZero();
        assertThat(withKerning(null).kerning('T', 'e')).as("a face without kerning").isZero();
    }
}
