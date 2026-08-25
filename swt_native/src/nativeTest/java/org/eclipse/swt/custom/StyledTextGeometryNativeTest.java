package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The StyledText position API answers from the geometry table the render side pushes in
 * front of each Paint request. These tests feed {@link StyledTextHelper#applyTextGeometry}
 * a table for the two-line document {@code "ab\ncd"} (10 px per character, 15 px per line)
 * and assert the query helpers repeat it back, mapped into widget space by margins and
 * scroll offsets — and refuse to answer once the table is stale (character count changed).
 */
@Tag("native-unit")
public class StyledTextGeometryNativeTest {

    private static DartStyledText widget(int charCount) {
        DartStyledText st = mock(DartStyledText.class);
        st.content = mock(StyledTextContent.class);
        when(st.getCharCount()).thenReturn(charCount);
        return st;
    }

    private static Map<String, Object> table() {
        return Map.of(
                "charCount", 5,
                "contentWidth", 20.0,
                "contentHeight", 30.0,
                "lines", List.of(
                        Map.of("l", 0, "s", 0, "e", 2, "x", 0.0, "y", 0.0, "w", 20.0, "h", 15.0,
                                "cx", List.of(0.0, 10.0, 20.0)),
                        Map.of("l", 1, "s", 3, "e", 5, "x", 0.0, "y", 15.0, "w", 20.0, "h", 15.0,
                                "cx", List.of(0.0, 10.0, 20.0))));
    }

    @Test
    public void point_at_offset_comes_from_the_table_in_widget_space() {
        DartStyledText st = widget(5);
        st.leftMargin = 3;
        st.topMargin = 5;
        st.horizontalScrollOffset = 2;
        when(st.getVerticalScrollOffset()).thenReturn(7);
        StyledTextHelper.applyTextGeometry(st, table());

        // offset 4 = second char of line 1: content (10, 15) -> widget (10+3-2, 15-7+5)
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 4)).isEqualTo(new Point(11, 13));
        // end-of-document offset answers from the last boundary
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 5)).isEqualTo(new Point(21, 13));
    }

    @Test
    public void line_pixel_and_line_index_are_inverse_through_the_table() {
        DartStyledText st = widget(5);
        st.topMargin = 5;
        when(st.getVerticalScrollOffset()).thenReturn(7);
        StyledTextHelper.applyTextGeometry(st, table());

        assertThat(StyledTextHelper.geometryLinePixel(st, 1)).isEqualTo(15 - 7 + 5);
        // one past the last line answers the content bottom, like the estimate path
        assertThat(StyledTextHelper.geometryLinePixel(st, 2)).isEqualTo(30 - 7 + 5);
        assertThat(StyledTextHelper.geometryLineIndex(st, 13)).isEqualTo(1);
        // above the content clamps to the first line
        assertThat(StyledTextHelper.geometryLineIndex(st, -100)).isEqualTo(0);
    }

    @Test
    public void offset_at_point_snaps_to_the_nearest_character_boundary() {
        DartStyledText st = widget(5);
        st.leftMargin = 3;
        st.topMargin = 5;
        st.horizontalScrollOffset = 2;
        when(st.getVerticalScrollOffset()).thenReturn(7);
        StyledTextHelper.applyTextGeometry(st, table());

        // widget (12, 13) -> content (11, 15): line 1, nearest boundary 10 -> offset 3 + 1
        assertThat(StyledTextHelper.geometryOffsetAtPoint(st, 12, 13)).isEqualTo(4);
        // below the content: not answerable, the estimate path decides
        assertThat(StyledTextHelper.geometryOffsetAtPoint(st, 12, 1000)).isNull();
    }

    @Test
    public void text_bounds_span_visual_lines_in_widget_space() {
        DartStyledText st = widget(5);
        st.leftMargin = 3;
        st.horizontalScrollOffset = 2;
        st.topMargin = 5;
        when(st.getVerticalScrollOffset()).thenReturn(7);
        StyledTextHelper.applyTextGeometry(st, table());

        // [0, 4] covers both lines fully up to x=10 on line 1, but line 0 reaches x=20
        assertThat(StyledTextHelper.geometryTextBounds(st, 0, 4))
                .isEqualTo(new Rectangle(0 + 3 - 2, 0 - 7 + 5, 20, 30));
    }

    @Test
    public void a_stale_table_answers_nothing() {
        DartStyledText st = widget(5);
        StyledTextHelper.applyTextGeometry(st, table());
        when(st.getCharCount()).thenReturn(6);

        assertThat(StyledTextHelper.geometryPointAtOffset(st, 1)).isNull();
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
        assertThat(StyledTextHelper.geometryLineIndex(st, 0)).isNull();
        assertThat(StyledTextHelper.geometryOffsetAtPoint(st, 0, 0)).isNull();
        assertThat(StyledTextHelper.geometryTextBounds(st, 0, 1)).isNull();
    }

    @Test
    public void vertical_indent_sits_inside_the_line_box() {
        DartStyledText st = widget(5);
        // line 1 carries a 34 px vertical indent: box [15, 64), glyphs at 15 + 34 = 49
        StyledTextHelper.applyTextGeometry(st, Map.of(
                "charCount", 5,
                "contentWidth", 20.0,
                "contentHeight", 64.0,
                "lines", List.of(
                        Map.of("l", 0, "s", 0, "e", 2, "x", 0.0, "y", 0.0, "w", 20.0, "h", 15.0,
                                "cx", List.of(0.0, 10.0, 20.0)),
                        Map.of("l", 1, "s", 3, "e", 5, "x", 0.0, "y", 15.0, "w", 20.0, "h", 49.0,
                                "vi", 34.0, "cx", List.of(0.0, 10.0, 20.0)))));

        // text-position queries answer at the glyphs, below the indent
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 3)).isEqualTo(new Point(0, 49));
        assertThat(StyledTextHelper.geometryTextBounds(st, 3, 5))
                .isEqualTo(new Rectangle(0, 49, 20, 15));
        // line queries answer the box: its top pixel, and the whole band including the indent
        assertThat(StyledTextHelper.geometryLinePixel(st, 1)).isEqualTo(15);
        assertThat(StyledTextHelper.geometryLineIndex(st, 20)).isEqualTo(1);
    }

    @Test
    public void a_vertical_indent_delta_patches_the_stored_table_in_place() {
        DartStyledText st = widget(5);
        StyledTextHelper.applyTextGeometry(st, table());

        StyledTextHelper.applyLineVerticalIndentDelta(st, 0, 34);

        // line 0's glyphs move below the new indent; its box top stays put
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 0)).isEqualTo(new Point(0, 34));
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isEqualTo(0);
        // line 1 shifts down whole, and the content bottom follows
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 3)).isEqualTo(new Point(0, 49));
        assertThat(StyledTextHelper.geometryLinePixel(st, 2)).isEqualTo(64);

        // removing the indent restores the original geometry
        StyledTextHelper.applyLineVerticalIndentDelta(st, 0, -34);
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 0)).isEqualTo(new Point(0, 0));
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 3)).isEqualTo(new Point(0, 15));
        assertThat(StyledTextHelper.geometryLinePixel(st, 2)).isEqualTo(30);
    }

    @Test
    public void a_table_without_char_positions_still_answers_line_queries() {
        DartStyledText st = widget(5);
        StyledTextHelper.applyTextGeometry(st, Map.of(
                "charCount", 5,
                "contentWidth", 20.0,
                "contentHeight", 30.0,
                "lines", List.of(
                        Map.of("l", 0, "s", 0, "e", 2, "x", 0.0, "y", 0.0, "w", 20.0, "h", 15.0),
                        Map.of("l", 1, "s", 3, "e", 5, "x", 0.0, "y", 15.0, "w", 20.0, "h", 15.0))));

        assertThat(StyledTextHelper.geometryLinePixel(st, 1)).isEqualTo(15);
        assertThat(StyledTextHelper.geometryLineIndex(st, 20)).isEqualTo(1);
        // x-precision queries defer to the estimate path
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 4)).isNull();
        assertThat(StyledTextHelper.geometryTextBounds(st, 0, 4)).isNull();
    }
}
