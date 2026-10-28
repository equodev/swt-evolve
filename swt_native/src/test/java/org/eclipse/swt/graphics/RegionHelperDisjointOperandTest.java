package org.eclipse.swt.graphics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Operands that meet none of the region's rectangles, or that bound no area at all. A GC clipped to
 * such a region paints nothing, so an operation that kept the untouched rectangle instead would let
 * a clip through that should hold everything back.
 */
class RegionHelperDisjointOperandTest {

    /** Two rectangles far enough apart that one operand can hit the first and miss the second. */
    private static List<int[]> twoRectangles() {
        List<int[]> rects = new ArrayList<>();
        RegionHelper.add(rects, 0, 0, 10, 10);
        RegionHelper.add(rects, 100, 100, 10, 10);
        return rects;
    }

    @Test
    void intersecting_with_a_disjoint_rectangle_empties_the_region() {
        List<int[]> rects = twoRectangles();

        RegionHelper.intersect(rects, 500, 500, 20, 20);

        assertThat(RegionHelper.isEmpty(rects)).isTrue();
        assertThat(RegionHelper.flatten(rects)).isEmpty();
    }

    @Test
    void intersecting_keeps_only_the_rectangles_the_operand_meets() {
        List<int[]> rects = twoRectangles();

        RegionHelper.intersect(rects, 5, 5, 200, 200);

        assertThat(RegionHelper.flatten(rects)).containsExactly(5, 5, 5, 5, 100, 100, 10, 10);
    }

    @Test
    void intersecting_with_a_disjoint_region_empties_the_region() {
        List<int[]> rects = twoRectangles();
        List<int[]> other = new ArrayList<>();
        RegionHelper.add(other, 500, 500, 20, 20);

        RegionHelper.intersectRegion(rects, other);

        assertThat(RegionHelper.isEmpty(rects)).isTrue();
        assertThat(RegionHelper.flatten(rects)).isEmpty();
    }

    @Test
    void intersecting_two_regions_keeps_every_pair_that_overlaps() {
        List<int[]> rects = twoRectangles();
        List<int[]> other = new ArrayList<>();
        RegionHelper.add(other, 5, 5, 10, 10);
        RegionHelper.add(other, 105, 105, 10, 10);

        RegionHelper.intersectRegion(rects, other);

        assertThat(RegionHelper.flatten(rects)).containsExactly(5, 5, 5, 5, 105, 105, 5, 5);
    }

    @Test
    void subtracting_a_zero_height_rectangle_leaves_the_region_alone() {
        List<int[]> rects = twoRectangles();

        RegionHelper.subtract(rects, 0, 0, 10, 0);

        assertThat(RegionHelper.flatten(rects)).containsExactly(0, 0, 10, 10, 100, 100, 10, 10);
    }

    @Test
    void subtracting_a_polygon_of_fewer_than_two_points_leaves_the_region_alone() {
        // A single point bounds no area, so there is nothing to take away.
        List<int[]> rects = twoRectangles();

        RegionHelper.subtractPolygon(rects, new int[]{ 5, 5 }, 2);

        assertThat(RegionHelper.flatten(rects)).containsExactly(0, 0, 10, 10, 100, 100, 10, 10);
    }

    @Test
    void intersecting_with_a_rectangle_that_shares_columns_but_no_rows_empties_the_region() {
        // Overlapping on one axis only is still no overlap: the operand covers the columns of both
        // rectangles and the rows of neither.
        List<int[]> rects = twoRectangles();

        RegionHelper.intersect(rects, 0, 500, 200, 20);

        assertThat(RegionHelper.isEmpty(rects)).isTrue();
    }

    @Test
    void intersecting_with_a_region_that_shares_columns_but_no_rows_empties_the_region() {
        List<int[]> rects = twoRectangles();
        List<int[]> other = new ArrayList<>();
        RegionHelper.add(other, 0, 500, 200, 20);

        RegionHelper.intersectRegion(rects, other);

        assertThat(RegionHelper.isEmpty(rects)).isTrue();
    }
}
