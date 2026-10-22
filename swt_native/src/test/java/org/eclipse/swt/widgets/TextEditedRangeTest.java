package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which range of the old text a client edit replaced, from the old text, the new one, the selection
 * Java last heard and the client's caret. The selection wins when the new text agrees with it; then
 * the text after the caret, which the edit left alone; then the common prefix and suffix.
 */
class TextEditedRangeTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
        typing at the selection               | abc  | aXbc | 1  | 1 | -1 | 1 | 1
        replacing the selected range          | abc  | aXc  | 1  | 2 | -1 | 1 | 2
        no selection: prefix and suffix       | abc  | abXc | -9 | 0 | -1 | 2 | 2
        selection starting before the text    | abc  | abXc | -1 | 0 | -1 | 2 | 2
        selection ending past the text        | abc  | abXc | 0  | 9 | -1 | 2 | 2
        selection reversed                    | abc  | abXc | 2  | 1 | -1 | 2 | 2
        new text too short for the selection  | abc  | a    | 1  | 1 | -1 | 1 | 3
        new text disagrees before it          | abc  | Xabc | 2  | 2 | -1 | 0 | 0
        new text disagrees after it           | abc  | abcX | 1  | 1 | -1 | 3 | 3
        a removal among repeats, by the caret | aaa  | aa   | -9 | 0 | 1  | 1 | 2
        caret past the new text               | abc  | abXc | -9 | 0 | 9  | 2 | 2
        caret leaving more than the old text  | ab   | XXab | -9 | 0 | 0  | 0 | 0
        text after the caret not in the old   | abc  | abXc | -9 | 0 | 2  | 2 | 2
        an append                             | abc  | abcd | -9 | 0 | -1 | 3 | 3
        a removal at the end                  | abc  | ab   | -9 | 0 | -1 | 2 | 3
        """)
    @DisplayName("the edited range")
    void editedRange(String what, String old, String proposed, int selX, int selY, int caret, int x, int y) {
        Point selection = selX == -9 ? null : new Point(selX, selY);

        assertThat(TextHelper.editedRange(old, proposed, selection, caret)).isEqualTo(new Point(x, y));
    }
}
