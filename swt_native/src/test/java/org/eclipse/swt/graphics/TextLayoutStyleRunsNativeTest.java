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
 * A run of one style is found by stepping over the style ranges, not over the characters: where it ends is
 * where a walk over the characters would have stopped, whether the next range carries the same style or
 * starts past the limit.
 */
@Tag("native-unit")
class TextLayoutStyleRunsNativeTest {

    private final Display display = DartMocks.dartDisplay();
    private final List<TextLayout> layouts = new ArrayList<>();

    @AfterEach
    void dispose() {
        layouts.forEach(TextLayout::dispose);
    }

    @Test
    void aRunEndsWhereACharacterWalkWouldStop() {
        String text = "org.example.bundle (1.2.3) - /a/path/to/org.example.bundle_1.2.3.jar\tend";
        TextStyle bold = new TextStyle();
        TextStyle grey = new TextStyle();
        TextStyle blue = new TextStyle();
        TextLayout layout = layout(text);
        layout.setStyle(bold, 0, 17);
        // The same style again right after its own range: one run with it.
        layout.setStyle(grey, 19, 25);
        layout.setStyle(grey, 26, 27);
        layout.setStyle(blue, 29, 40);
        layout.setStyle(grey, 41, text.length() - 1);
        assertRunsMatchACharacterWalk(layout, text.length());
    }

    @Test
    void anUnstyledLayoutIsOneRun() {
        TextLayout layout = layout("plain text");
        assertRunsMatchACharacterWalk(layout, 10);
    }

    private TextLayout layout(String text) {
        TextLayout layout = new TextLayout(display);
        layouts.add(layout);
        layout.setFont(new DartFont(display, "System", 12, SWT.NORMAL, null).getApi());
        layout.setText(text);
        return layout;
    }

    private static void assertRunsMatchACharacterWalk(TextLayout layout, int length) {
        DartTextLayout impl = (DartTextLayout) layout.getImpl();
        for (int limit = 1; limit <= length; limit++) {
            for (int offset = 0; offset < limit; offset++) {
                TextStyle style = impl._styleFor(offset);
                int walked = offset + 1;
                while (walked < limit && impl._styleFor(walked) == style) walked++;
                assertThat(impl._styleRunEnd(offset, limit)).as("run from %d below %d", offset, limit).isEqualTo(walked);
            }
        }
    }
}
