package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;

/**
 * A band's styles travel once as a palette and its runs name them; the names must say which palette
 * they belong to, since palette and names are sent as separate properties.
 */
class StyledTextBandPaletteTest {

    private static StyleRange run(int start, int length, Color foreground) {
        return new StyleRange(start, length, foreground, null);
    }

    @Test
    void every_run_keeps_its_own_place() {
        Color red = new Color(Mocks.device(), 255, 0, 0);
        // Positions travel with the names: held in the palette, same-style runs would collapse onto one offset.
        List<StyleRange> runs = Arrays.asList(run(0, 4, red), run(40, 6, red), run(900, 2, red));

        assertThat(StyledTextHelper.bandPaletteForTesting(runs)).hasSize(1);
        int[] names = StyledTextHelper.bandNamesForTesting(runs);

        assertThat(names).as("the palette's name, then a name and a place per run").hasSize(10);
        assertThat(new int[] { names[2], names[3] }).as("first run").containsExactly(0, 4);
        assertThat(new int[] { names[5], names[6] }).as("second run").containsExactly(40, 6);
        assertThat(new int[] { names[8], names[9] }).as("third run").containsExactly(900, 2);
    }

    @Test
    void a_style_drawn_by_many_runs_travels_once() {
        Color red = new Color(Mocks.device(), 255, 0, 0);
        Color blue = new Color(Mocks.device(), 0, 0, 255);
        List<StyleRange> runs = Arrays.asList(
                run(0, 4, red), run(0, 4, red), run(0, 4, blue), run(0, 4, red));

        assertThat(StyledTextHelper.bandPaletteForTesting(runs))
                .as("four runs at one place in two colours")
                .hasSize(2);
        int[] names = StyledTextHelper.bandNamesForTesting(runs);
        assertThat(names).as("the palette's own name, then a name and a place per run").hasSize(13);
        assertThat(new int[] { names[1], names[4], names[7], names[10] })
                .as("which style each run is drawn in")
                .containsExactly(0, 0, 1, 0);
    }

    @Test
    void an_edit_that_only_moves_runs_leaves_the_palette_as_it_was() {
        Color red = new Color(Mocks.device(), 255, 0, 0);
        Color blue = new Color(Mocks.device(), 0, 0, 255);
        // A keystroke ahead of every run: each one moves, none is restyled.
        List<StyleRange> before = Arrays.asList(run(10, 4, red), run(30, 2, blue));
        List<StyleRange> after = Arrays.asList(run(11, 4, red), run(31, 2, blue));

        assertThat(StyledTextHelper.bandNamesForTesting(after)[0])
                .as("the same palette, so it need not travel again")
                .isEqualTo(StyledTextHelper.bandNamesForTesting(before)[0]);
        assertThat(StyledTextHelper.bandPaletteForTesting(after))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(StyledTextHelper.bandPaletteForTesting(before));
    }

    @Test
    void names_built_for_one_palette_are_never_names_in_another() {
        Color red = new Color(Mocks.device(), 255, 0, 0);
        Color blue = new Color(Mocks.device(), 0, 0, 255);
        // Two bands whose runs name the same slots in the same order, over opposite palettes:
        // slot 0 is red in one and blue in the other.
        List<StyleRange> asRedFirst = Arrays.asList(run(0, 4, red), run(0, 4, blue));
        List<StyleRange> asBlueFirst = Arrays.asList(run(0, 4, blue), run(0, 4, red));

        int[] a = StyledTextHelper.bandNamesForTesting(asRedFirst);
        int[] b = StyledTextHelper.bandNamesForTesting(asBlueFirst);

        assertThat(new int[] { a[1], a[4] })
                .as("the runs name the same slots, which is what made this deliverable")
                .containsExactly(b[1], b[4]);
        assertThat(a[0])
                .as("so the palette's name is what has to differ, or the wrong one can be used")
                .isNotEqualTo(b[0]);
    }
}
