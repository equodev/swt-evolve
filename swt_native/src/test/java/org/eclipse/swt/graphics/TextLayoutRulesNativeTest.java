package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The text rules the web TextLayout keeps where the platforms' SWT differ: a final delimiter starts an empty line,
 * a cluster keeps its combining marks, a word is letters and digits with a line delimiter as a stop, the text
 * direction is a setting of its own, setAscent and setDescent each raise their own side of the line, and with no
 * tab stops a tab takes no space.
 */
@Tag("native-unit")
class TextLayoutRulesNativeTest {

    private final Display display = DartMocks.dartDisplay();
    private final List<TextLayout> layouts = new ArrayList<>();

    @AfterEach
    void dispose() {
        layouts.forEach(TextLayout::dispose);
    }

    private TextLayout layout(String text) {
        TextLayout layout = new TextLayout(display);
        layouts.add(layout);
        layout.setFont(new DartFont(display, "System", 12, SWT.NORMAL, null).getApi());
        layout.setText(text);
        return layout;
    }

    @Test
    void aFinalDelimiterStartsAnEmptyLine() {
        assertThat(layout("0123456\n890123\n").getLineOffsets()).containsExactly(0, 8, 15, 15);
        assertThat(layout("ab\r").getLineOffsets()).containsExactly(0, 3, 3);
        TextLayout layout = layout("ab\n");
        assertThat(layout.getLineIndex(3)).isEqualTo(1);
        assertThat(layout.getLocation(3, false)).isEqualTo(new Point(0, layout.getLineBounds(1).y));
    }

    @Test
    void aClusterKeepsACharacterWithItsCombiningMarks() {
        TextLayout layout = layout("e\u0301x");
        assertThat(layout.getNextOffset(0, SWT.MOVEMENT_CLUSTER)).isEqualTo(2);
        assertThat(layout.getPreviousOffset(2, SWT.MOVEMENT_CLUSTER)).isZero();
        assertThat(layout("a\ud83d\ude00b").getNextOffset(1, SWT.MOVEMENT_CLUSTER)).isEqualTo(3);
    }

    @Test
    void aWordIsLettersAndDigitsAndADelimiterIsAStop() {
        TextLayout layout = layout("foo, bar_baz qux");
        assertThat(layout.getNextOffset(3, SWT.MOVEMENT_WORD_END)).as("an underscore ends a word").isEqualTo(8);
        assertThat(layout.getNextOffset(5, SWT.MOVEMENT_WORD_START)).isEqualTo(9);
        assertThat(layout.getPreviousOffset(12, SWT.MOVEMENT_WORD_START)).isEqualTo(9);
        assertThat(layout("don't").getNextOffset(0, SWT.MOVEMENT_WORD_END)).as("so does an apostrophe").isEqualTo(3);
        TextLayout lines = layout("ab cd\nef gh");
        assertThat(lines.getNextOffset(3, SWT.MOVEMENT_WORD_START)).isEqualTo(5);
        assertThat(lines.getPreviousOffset(8, SWT.MOVEMENT_WORD_START)).isEqualTo(6);
    }

    @Test
    void theTextDirectionIsASettingOfItsOwn() {
        TextLayout layout = layout("abc");
        layout.setTextDirection(SWT.RIGHT_TO_LEFT);
        assertThat(layout.getTextDirection()).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(layout.getOrientation()).isEqualTo(SWT.LEFT_TO_RIGHT);
        layout.setTextDirection(SWT.AUTO_TEXT_DIRECTION);
        assertThat(layout.getTextDirection()).isEqualTo(SWT.LEFT_TO_RIGHT);
        layout.setOrientation(SWT.RIGHT_TO_LEFT);
        layout.setTextDirection(SWT.LEFT_TO_RIGHT);
        assertThat(layout.getTextDirection()).isEqualTo(SWT.LEFT_TO_RIGHT);
        TextLayout hebrew = layout("\u05d0\u05d1\u05d2 abc");
        hebrew.setTextDirection(SWT.AUTO_TEXT_DIRECTION);
        assertThat(hebrew.getTextDirection()).isEqualTo(SWT.RIGHT_TO_LEFT);
    }

    @Test
    void theAscentAndTheDescentEachRaiseTheirSideOfTheLine() {
        FontMetrics glyphs = layout("abc").getLineMetrics(0);
        TextLayout layout = layout("abc");
        layout.setAscent(1);
        layout.setDescent(40);
        assertThat(layout.getLineMetrics(0).getAscent()).isEqualTo(glyphs.getAscent());
        assertThat(layout.getLineBounds(0).height).isEqualTo(glyphs.getAscent() + 40);
        TextLayout raised = layout("abc");
        raised.setAscent(40);
        assertThat(raised.getLineBounds(0).height).as("the ascent alone").isEqualTo(40 + glyphs.getDescent());
    }

    @Test
    void withNoTabStopsATabTakesNoSpace() {
        TextLayout layout = layout("a\tb");
        assertThat(layout.getLocation(2, false).x).isEqualTo(layout.getLocation(1, false).x);
    }
}
