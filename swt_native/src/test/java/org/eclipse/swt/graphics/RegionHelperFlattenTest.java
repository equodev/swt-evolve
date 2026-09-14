package org.eclipse.swt.graphics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A region reaches Flutter as its rectangles, not as its bounds. A control set to a region is
 * clipped to it, and applications use one for outlines -- the Eclipse workbench frames its
 * drag-and-drop feedback with a full-window shell whose region is four thin bars -- so clipping to
 * the bounds instead would paint over the whole area the region exists to exclude.
 */
class RegionHelperFlattenTest {

    private static List<int[]> region() {
        return new ArrayList<>();
    }

    @Test
    void an_empty_region_flattens_to_nothing() {
        assertThat(RegionHelper.flatten(region())).isEmpty();
    }

    @Test
    void a_rectangle_flattens_to_its_four_values_in_order() {
        List<int[]> rects = region();
        RegionHelper.add(rects, 3, 5, 40, 20);

        assertThat(RegionHelper.flatten(rects)).containsExactly(3, 5, 40, 20);
    }

    @Test
    void rectangles_are_laid_out_end_to_end() {
        List<int[]> rects = region();
        RegionHelper.add(rects, 0, 0, 10, 10);
        RegionHelper.add(rects, 100, 200, 30, 40);

        assertThat(RegionHelper.flatten(rects)).containsExactly(0, 0, 10, 10, 100, 200, 30, 40);
    }

    @Test
    void a_frame_keeps_its_hole() {
        // How the workbench builds its feedback: one rectangle with the middle taken out. Flattening
        // has to carry the four bars that survive -- collapsing them back to one rectangle is exactly
        // the bug this guards, since the hole is the whole point of the shape.
        List<int[]> rects = region();
        RegionHelper.add(rects, 0, 0, 100, 100);
        RegionHelper.subtract(rects, 2, 2, 96, 96);

        int[] flat = RegionHelper.flatten(rects);

        assertThat(flat.length % 4).isZero();
        assertThat(flat.length / 4).isEqualTo(rects.size()).isGreaterThan(1);
        assertThat(RegionHelper.contains(rects, 1, 50)).isTrue();
        assertThat(RegionHelper.contains(rects, 50, 50)).isFalse();
    }

    @Test
    void the_flattened_run_describes_the_same_area_the_region_does() {
        List<int[]> rects = region();
        RegionHelper.add(rects, 10, 10, 80, 60);
        RegionHelper.subtract(rects, 30, 30, 20, 20);

        int[] flat = RegionHelper.flatten(rects);

        for (int at = 0; at < flat.length; at += 4) {
            int x = flat[at], y = flat[at + 1], w = flat[at + 2], h = flat[at + 3];
            assertThat(w).isPositive();
            assertThat(h).isPositive();
            assertThat(RegionHelper.contains(rects, x, y)).isTrue();
            assertThat(RegionHelper.contains(rects, x + w - 1, y + h - 1)).isTrue();
        }
        assertThat(RegionHelper.contains(rects, 35, 35)).isFalse();
    }
}
