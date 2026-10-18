package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where the layout puts the rows of a text before the render side has measured it: the indent
 * moves the first row of a paragraph, the wrap indent the rows below it (and both narrow the room
 * a row has), and the bounds of a range cover every row the range touches.
 */
@Tag("native-unit")
public class TextLayoutRowsNativeTest {

    private static TextLayout layout(Display display, String text) {
        Font font = new DartFont(display, "System", 12, SWT.NORMAL, null).getApi();
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(text);
        return layout;
    }

    private static int secondRowStart(TextLayout layout) {
        int[] offsets = layout.getLineOffsets();
        assertThat(offsets.length).as("the text wraps").isGreaterThan(2);
        return offsets[1];
    }

    @Test
    public void the_indent_moves_the_first_row_of_every_paragraph() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "abc\ndef");
        int plainWidth = layout.getBounds().width;

        layout.setIndent(10);

        assertThat(layout.getLocation(0, false).x).isEqualTo(10);
        assertThat(layout.getLocation(4, false).x).isEqualTo(10);
        assertThat(layout.getBounds().width).as("as on Cocoa, the bounds leave the indent out").isEqualTo(plainWidth);
        assertThat(layout.getLineBounds(1).x).isEqualTo(10);
    }

    @Test
    public void a_point_inside_the_indented_row_finds_its_character() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "abc");
        layout.setIndent(10);
        Point second = layout.getLocation(1, false);

        assertThat(layout.getOffset(second.x + 1, second.y + 1, null)).isEqualTo(1);
        assertThat(layout.getOffset(0, 1, null)).isEqualTo(0);
    }

    @Test
    public void the_wrap_indent_moves_the_rows_below_the_first() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "word ".repeat(30));
        layout.setWidth(80);
        int start = secondRowStart(layout);
        int plain = layout.getLocation(start, false).x;

        layout.setWrapIndent(20);

        int[] offsets = layout.getLineOffsets();
        assertThat(layout.getLocation(0, false).x).isEqualTo(0);
        assertThat(layout.getLocation(offsets[1], false).x).isEqualTo(plain + 20);
    }

    @Test
    public void an_indent_leaves_a_wrapped_row_less_room() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "word ".repeat(30));
        layout.setWidth(80);
        int rows = layout.getLineCount();

        layout.setIndent(40);
        layout.setWrapIndent(40);

        assertThat(layout.getLineCount()).isGreaterThan(rows);
    }

    @Test
    public void the_bounds_of_a_range_cover_every_row_it_touches() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "word ".repeat(30));
        layout.setWidth(80);
        int rows = layout.getLineCount();
        int lineHeight = layout.getLineBounds(0).height;

        assertThat(layout.getBounds(0, 3).height).isEqualTo(lineHeight);
        assertThat(layout.getBounds(0, layout.getText().length() - 1).height).isEqualTo(rows * lineHeight);
        Rectangle middle = layout.getBounds(layout.getLineOffsets()[1], layout.getLineOffsets()[3] - 1);
        assertThat(middle.y).isEqualTo(lineHeight);
        assertThat(middle.height).isEqualTo(2 * lineHeight);
    }

    @Test
    public void a_range_inside_one_row_stays_inside_that_row() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "abc\ndef");

        Rectangle bounds = layout.getBounds(5, 6);

        assertThat(bounds.y).isEqualTo(layout.getLineBounds(1).y);
        assertThat(bounds.height).isEqualTo(layout.getLineBounds(1).height);
        assertThat(bounds.x).isGreaterThan(0);
    }

    @Test
    public void a_style_with_a_larger_font_makes_its_row_taller() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "abc\ndef\nghi");
        int plain = layout.getLineBounds(0).height;
        Font big = new DartFont(display, "System", 40, SWT.NORMAL, null).getApi();

        layout.setStyle(new TextStyle(big, null, null), 4, 6);

        assertThat(layout.getLineBounds(0).height).isEqualTo(plain);
        assertThat(layout.getLineBounds(1).height).isGreaterThan(plain);
        assertThat(layout.getLineBounds(1).y).isEqualTo(plain);
        assertThat(layout.getLineBounds(2).y).isEqualTo(plain + layout.getLineBounds(1).height);
        assertThat(layout.getLineBounds(2).height).isEqualTo(plain);
        assertThat(layout.getLocation(8, false).y).isEqualTo(layout.getLineBounds(2).y);
        assertThat(layout.getBounds().height).isEqualTo(layout.getLineBounds(2).y + plain);
        assertThat(layout.getOffset(1, layout.getLineBounds(1).y + 1, null)).isEqualTo(4);
        assertThat(layout.getOffset(1, layout.getLineBounds(2).y + 1, null)).isEqualTo(8);
    }

    @Test
    public void a_style_over_the_whole_text_makes_every_row_taller() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "abc\ndef");
        int plain = layout.getLineBounds(0).height;
        Font big = new DartFont(display, "System", 40, SWT.NORMAL, null).getApi();

        layout.setStyle(new TextStyle(big, null, null), 0, 6);

        assertThat(layout.getLineBounds(0).height).isGreaterThan(plain);
        assertThat(layout.getLineBounds(1).height).isGreaterThan(plain);
    }

    @Test
    public void the_metrics_of_a_row_are_those_of_its_tallest_font() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "abc\ndef\nghi");
        FontMetrics plain = layout.getLineMetrics(0);
        Font big = new DartFont(display, "System", 40, SWT.NORMAL, null).getApi();

        layout.setStyle(new TextStyle(big, null, null), 4, 6);

        assertThat(layout.getLineMetrics(0).getAscent()).isEqualTo(plain.getAscent());
        assertThat(layout.getLineMetrics(1).getAscent()).isGreaterThan(plain.getAscent());
        assertThat(layout.getLineMetrics(1).getHeight()).isEqualTo(layout.getLineBounds(1).height);
    }

    @Test
    public void the_spacing_follows_every_row_and_the_last_one_too() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "a\nb\nc");
        int plain = layout.getLineBounds(0).height;
        int plainText = layout.getBounds().height;

        layout.setSpacing(8);

        assertThat(layout.getLineBounds(0).height).isEqualTo(plain);
        assertThat(layout.getLineBounds(1).y).isEqualTo(plain + 8);
        assertThat(layout.getLocation(4, false).y).isEqualTo(2 * (plain + 8));
        assertThat(layout.getBounds().height).isEqualTo(plainText + 3 * 8);
        assertThat(layout.getBounds(0, 0).height).isEqualTo(plain + 8);
        assertThat(layout.getBounds(2, 4).height).isEqualTo(2 * plain + 8);
        assertThat(layout.getOffset(1, layout.getLineBounds(1).y + 1, null)).isEqualTo(2);
    }

    @Test
    public void a_tab_stop_is_measured_from_the_left_edge_whatever_the_indent() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = layout(display, "abc\tdef");
        layout.setTabs(new int[] { 60 });
        int stop = layout.getLocation(4, false).x;

        layout.setIndent(10);

        assertThat(stop).isEqualTo(60);
        assertThat(layout.getLocation(4, false).x).isEqualTo(60);
        assertThat(layout.getLocation(0, false).x).isEqualTo(10);
    }
}
