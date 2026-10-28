package org.eclipse.swt.graphics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A region given itself as the operand: {@code region.subtract(region)} is how a caller empties one. */
class RegionHelperSelfOperandTest {

    private static List<int[]> twoRectangles() {
        List<int[]> rects = new ArrayList<>();
        RegionHelper.add(rects, 0, 0, 10, 10);
        RegionHelper.add(rects, 20, 20, 5, 5);
        return rects;
    }

    @Test
    void subtracting_a_region_from_itself_empties_it() {
        List<int[]> rects = twoRectangles();

        RegionHelper.subtractRegion(rects, rects);

        assertThat(RegionHelper.isEmpty(rects)).isTrue();
    }

    @Test
    void adding_a_region_to_itself_keeps_its_area() {
        List<int[]> rects = twoRectangles();

        RegionHelper.addRegion(rects, rects);

        assertThat(RegionHelper.getBounds(rects)).isEqualTo(new Rectangle(0, 0, 25, 25));
        assertThat(RegionHelper.contains(rects, 22, 22)).isTrue();
        assertThat(RegionHelper.contains(rects, 15, 15)).isFalse();
    }
}
