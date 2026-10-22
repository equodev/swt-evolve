package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What {@link StyledTextHelper} does with a geometry table that is malformed, does not fit the one held,
 * or is asked about something outside it: nothing is stored, the render side is asked for the whole
 * table, and the position queries answer {@code null} so the estimate decides.
 */
@Tag("native-unit")
public class StyledTextGeometryEdgesNativeTest {

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static DartStyledText widget(int charCount) {
        DartStyledText st = mock(DartStyledText.class);
        st.content = mock(StyledTextContent.class);
        when(st.getCharCount()).thenReturn(charCount);
        when(st.getApi()).thenReturn(mock(StyledText.class));
        return st;
    }

    private static Map<String, Object> row(int logicalLine, int start, int end, double y, double h, List<Double> charX) {
        Map<String, Object> row = new HashMap<>();
        row.put("l", logicalLine);
        row.put("s", start);
        row.put("e", end);
        row.put("x", 0.0);
        row.put("y", y);
        row.put("w", 20.0);
        row.put("h", h);
        if (charX != null) row.put("cx", charX);
        return row;
    }

    private static Map<String, Object> table(int charCount, double height, Object lines) {
        Map<String, Object> table = new HashMap<>();
        table.put("charCount", charCount);
        table.put("contentWidth", 20.0);
        table.put("contentHeight", height);
        table.put("v", 1);
        table.put("lines", lines);
        return table;
    }

    private static List<Double> boundaries(int count) {
        Double[] xs = new Double[count];
        for (int i = 0; i < count; i++) xs[i] = i * 10.0;
        return List.of(xs);
    }

    private boolean resendAsked() {
        return bridge.comm.sent.stream().anyMatch(frame -> frame.event.endsWith("ResendGeometry"));
    }

    private DartStyledText held() {
        DartStyledText st = widget(5);
        StyledTextHelper.applyTextGeometry(st, table(5, 30.0, List.of(
                row(0, 0, 2, 0.0, 15.0, boundaries(3)), row(1, 3, 5, 15.0, 15.0, boundaries(3)))));
        return st;
    }

    @Test
    public void a_row_that_is_not_a_row_discards_the_table() {
        DartStyledText st = widget(5);

        StyledTextHelper.applyTextGeometry(st, table(5, 30.0, List.of("not a row")));

        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
    }

    @Test
    public void a_row_without_its_offsets_discards_the_table() {
        DartStyledText st = widget(5);
        Map<String, Object> noLine = row(-1, 0, 2, 0.0, 15.0, null);
        Map<String, Object> reversed = row(0, 4, 2, 0.0, 15.0, null);

        StyledTextHelper.applyTextGeometry(st, table(5, 30.0, List.of(noLine)));
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
        StyledTextHelper.applyTextGeometry(st, table(5, 30.0, List.of(reversed)));
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
    }

    @Test
    public void character_positions_that_do_not_match_the_row_discard_the_table() {
        DartStyledText st = widget(5);
        Map<String, Object> wrongCount = row(0, 0, 2, 0.0, 15.0, boundaries(5));
        Map<String, Object> wrongStep = row(0, 0, 2, 0.0, 15.0, null);
        wrongStep.put("cxu", List.of(0.0));

        StyledTextHelper.applyTextGeometry(st, table(5, 30.0, List.of(wrongCount)));
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
        StyledTextHelper.applyTextGeometry(st, table(5, 30.0, List.of(wrongStep)));
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
    }

    @Test
    public void a_uniform_row_is_an_origin_and_a_step() {
        DartStyledText st = widget(5);
        Map<String, Object> uniform = row(0, 0, 5, 0.0, 15.0, null);
        uniform.put("cxu", List.of(2.0, 7.0));

        StyledTextHelper.applyTextGeometry(st, table(5, 15.0, List.of(uniform)));

        assertThat(StyledTextHelper.geometryPointAtOffset(st, 0)).isEqualTo(new Point(2, 0));
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 3)).isEqualTo(new Point(23, 0));
        assertThat(StyledTextHelper.geometryPointAtOffset(st, 5)).isEqualTo(new Point(37, 0));
    }

    @Test
    public void a_payload_that_is_not_json_is_ignored() {
        DartStyledText st = widget(5);

        StyledTextHelper.processTextGeometry(st, "this is not json");
        StyledTextHelper.processTextGeometry(st, "null");

        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
    }

    @Test
    public void a_payload_with_a_table_is_applied() {
        DartStyledText st = widget(5);

        StyledTextHelper.processTextGeometry(st, "{\"charCount\":5,\"contentWidth\":20,\"contentHeight\":30,\"v\":1,"
                + "\"lines\":[{\"l\":0,\"s\":0,\"e\":2,\"x\":0,\"y\":0,\"w\":20,\"h\":15,\"cx\":[0,10,20]},"
                + "{\"l\":1,\"s\":3,\"e\":5,\"x\":0,\"y\":15,\"w\":20,\"h\":15,\"cx\":[0,10,20]}]}");

        assertThat(StyledTextHelper.geometryLinePixel(st, 1)).isEqualTo(15);
    }

    @Test
    public void a_change_without_a_table_to_change_asks_for_the_whole_table() {
        DartStyledText st = widget(5);
        Map<String, Object> change = new HashMap<>();
        change.put("charCount", 5);
        change.put("base", 1);
        change.put("rows", List.of(row(0, 0, 2, 0.0, 15.0, boundaries(3))));
        change.put("rowStart", 0);
        change.put("rowsReplaced", 0);

        StyledTextHelper.applyTextGeometry(st, change);

        assertThat(resendAsked()).isTrue();
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
    }

    @Test
    public void a_change_made_against_another_table_asks_for_the_whole_table() {
        DartStyledText st = held();
        Map<String, Object> change = new HashMap<>();
        change.put("charCount", 5);
        change.put("base", 99);
        change.put("rows", List.of(row(0, 0, 2, 0.0, 15.0, boundaries(3))));
        change.put("rowStart", 0);
        change.put("rowsReplaced", 1);

        StyledTextHelper.applyTextGeometry(st, change);

        assertThat(resendAsked()).isTrue();
        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isNull();
    }

    @Test
    public void a_change_with_a_row_that_cannot_be_read_asks_for_the_whole_table() {
        DartStyledText st = held();
        Map<String, Object> change = new HashMap<>();
        change.put("charCount", 5);
        change.put("base", 1);
        change.put("rows", List.of("broken"));
        change.put("rowStart", 1);
        change.put("rowsReplaced", 1);

        StyledTextHelper.applyTextGeometry(st, change);

        assertThat(resendAsked()).isTrue();
    }

    @Test
    public void a_change_replaces_the_rows_it_names_and_shifts_the_ones_below() {
        DartStyledText st = held();
        Map<String, Object> change = new HashMap<>();
        change.put("charCount", 7);
        change.put("base", 1);
        change.put("v", 2);
        change.put("contentHeight", 45.0);
        change.put("rows", List.of(row(1, 3, 5, 15.0, 15.0, boundaries(3)), row(2, 6, 7, 30.0, 15.0, boundaries(2))));
        change.put("rowStart", 1);
        change.put("rowsReplaced", 1);
        when(st.getCharCount()).thenReturn(7);

        StyledTextHelper.applyTextGeometry(st, change);

        assertThat(resendAsked()).isFalse();
        assertThat(StyledTextHelper.geometryLinePixel(st, 2)).isEqualTo(30);
        assertThat(StyledTextHelper.geometryLinePixel(st, 3)).isEqualTo(45);
    }

    @Test
    public void rows_below_a_change_move_by_the_deltas() {
        DartStyledText st = held();
        Map<String, Object> change = new HashMap<>();
        change.put("charCount", 6);
        change.put("base", 1);
        change.put("v", 2);
        change.put("contentHeight", 45.0);
        change.put("rows", List.of(row(0, 0, 3, 0.0, 15.0, boundaries(4))));
        change.put("rowStart", 0);
        change.put("rowsReplaced", 1);
        change.put("lineDelta", 0);
        change.put("offsetDelta", 1);
        change.put("yDelta", 0.0);
        when(st.getCharCount()).thenReturn(6);

        StyledTextHelper.applyTextGeometry(st, change);

        assertThat(StyledTextHelper.geometryPointAtOffset(st, 5)).isEqualTo(new Point(10, 15));
    }

    @Test
    public void the_table_answers_for_the_line_a_point_falls_on_and_the_line_after_the_end() {
        DartStyledText st = held();

        assertThat(StyledTextHelper.geometryLinePixel(st, 0)).isEqualTo(0);
        assertThat(StyledTextHelper.geometryLinePixel(st, 5)).isEqualTo(30);
        assertThat(StyledTextHelper.geometryLineIndex(st, 100)).isEqualTo(1);
    }

    @Test
    public void a_point_beside_the_text_has_no_offset_unless_the_row_wraps_on() {
        DartStyledText st = widget(8);
        StyledTextHelper.applyTextGeometry(st, table(8, 30.0, List.of(
                row(0, 0, 4, 0.0, 15.0, boundaries(5)), row(0, 4, 8, 15.0, 15.0, boundaries(5)))));

        assertThat(StyledTextHelper.geometryOffsetAtPoint(st, 500, 5)).isEqualTo(4);
        assertThat(StyledTextHelper.geometryOffsetAtPoint(st, 500, 20)).isEqualTo(-1);
        assertThat(StyledTextHelper.geometryOffsetAtPoint(st, 12, -5)).isNull();
        assertThat(StyledTextHelper.geometryOffsetAtPoint(st, 12, 500)).isNull();
    }

    @Test
    public void the_bounds_of_a_range_outside_every_row_are_not_answered() {
        DartStyledText st = held();

        assertThat(StyledTextHelper.geometryTextBounds(st, 40, 50)).isNull();
        assertThat(StyledTextHelper.geometryTextBounds(st, 0, 2)).isEqualTo(new Rectangle(0, 0, 20, 15));
    }

    @Test
    public void the_row_height_of_an_offset_comes_from_the_table() {
        DartStyledText st = held();
        when(st.content.getLineCount()).thenReturn(2);
        when(st.content.getOffsetAtLine(0)).thenReturn(0);
        when(st.content.getOffsetAtLine(1)).thenReturn(3);
        when(st.content.getLine(0)).thenReturn("ab");
        when(st.content.getLine(1)).thenReturn("cd");

        assertThat(StyledTextHelper.measuredRowHeight(st, 1, 1)).isEqualTo(15);
        assertThat(StyledTextHelper.measuredRowHeight(null, 1, 1)).isNull();
        assertThat(StyledTextHelper.measuredLineTop(st, 1)).isEqualTo(15);
        assertThat(StyledTextHelper.measuredLineTop(st, 7)).isNull();
        assertThat(StyledTextHelper.measuredContentHeight(st)).isEqualTo(30);
        assertThat(StyledTextHelper.measuredLineHeight(st, 0)).isEqualTo(15);
    }

    @Test
    public void a_vertical_indent_delta_on_a_line_the_table_does_not_hold_changes_nothing() {
        DartStyledText st = held();

        StyledTextHelper.applyLineVerticalIndentDelta(st, 9, 20);
        StyledTextHelper.applyLineVerticalIndentDelta(st, 0, 0);
        StyledTextHelper.applyLineVerticalIndentDelta(widget(5), 0, 20);

        assertThat(StyledTextHelper.geometryLinePixel(st, 1)).isEqualTo(15);
    }
}
