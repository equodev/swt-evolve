package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * With fixed line heights a StyledText only knows the widths of the lines it has calculated, so a
 * long line further down does not widen the horizontal range until it has been scrolled to.
 */
// setText routes renderer font metrics into GTK/GDI on the Linux/Windows embed backends, which
// cannot run under the mocked display.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class StyledTextHorizontalRangeTest {

    private static final int LINES = 200;
    private static final int LONG_LINE = 150;
    private static final double SHORT_WIDTH = 60;
    private static final double LONG_WIDTH = 900;
    private static final double LINE_HEIGHT = 15;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static StyledText styledText() {
        StyledText text = new StyledText(new Composite(Mocks.shell(), SWT.NONE), SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < LINES; i++) {
            if (i > 0) content.append('\n');
            content.append(i == LONG_LINE ? "x".repeat(120) : "short");
        }
        text.setText(content.toString());
        text.setBounds(0, 0, 300, 150);
        StyledTextHelper.applyTextGeometry(impl(text), geometry(text.getText()));
        return text;
    }

    /** What the render side would report: every line measured, one row each. */
    private static Map<String, Object> geometry(String content) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String[] lines = content.split("\n", -1);
        int offset = 0;
        for (int i = 0; i < lines.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("l", i);
            row.put("s", offset);
            row.put("e", offset + lines[i].length());
            row.put("x", 0.0);
            row.put("y", i * LINE_HEIGHT);
            row.put("w", i == LONG_LINE ? LONG_WIDTH : SHORT_WIDTH);
            row.put("h", LINE_HEIGHT);
            rows.add(row);
            offset += lines[i].length() + 1;
        }
        return Map.of(
                "charCount", content.length(),
                "contentWidth", LONG_WIDTH,
                "contentHeight", lines.length * LINE_HEIGHT,
                "lines", rows);
    }

    private static DartStyledText impl(StyledText text) {
        return (DartStyledText) text.getImpl();
    }

    private static int width(StyledText text) {
        return ((DartStyledTextRenderer) impl(text).renderer.getImpl()).getWidth();
    }

    @Test
    void a_long_line_not_yet_shown_does_not_widen_the_range() {
        StyledText text = styledText();

        assertThat(width(text)).isEqualTo((int) SHORT_WIDTH);
    }

    @Test
    void scrolling_to_the_long_line_widens_the_range() {
        StyledText text = styledText();

        text.setTopIndex(LONG_LINE - 1);

        assertThat(width(text)).isEqualTo((int) LONG_WIDTH);
    }

    @Test
    void with_variable_line_heights_every_line_counts() {
        StyledText text = styledText();

        text.setLineSpacing(2);

        assertThat(width(text)).isEqualTo((int) LONG_WIDTH);
    }
}
