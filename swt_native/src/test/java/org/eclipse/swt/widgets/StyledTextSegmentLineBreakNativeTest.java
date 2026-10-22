package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartStyledText;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VStyledText;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A line break a BidiSegmentListener puts among its segment characters makes the line taller than
 * one row, so the client is told the editor no longer has a fixed line height. The listener needs a
 * display that delivers events, which this base gives.
 */
@Tag("native-unit")
class StyledTextSegmentLineBreakNativeTest extends EventsFromFlutterNativeTestBase {

    @Test
    void a_line_break_among_the_segment_characters_is_sent_as_a_varying_line_height() {
        StyledText st = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        st.setText("one\ntwo");
        st.addBidiSegmentListener(event -> {
            if (event.lineText.equals("two")) {
                event.segments = new int[] { 1 };
                event.segmentsChars = new char[] { '\n' };
            }
        });
        VStyledText value = ((DartStyledText) st.getImpl()).getValue();
        value.clearDirty();

        // The caret moving onto the second line lays it out, which is when its break is found.
        st.invokeAction(ST.LINE_DOWN);

        assertThat(value.changedKeys()).as("queued for the client").contains(VStyledText.FIXED_LINE_HEIGHT);
        assertThat(value.getFixedLineHeight()).isFalse();
    }
}
