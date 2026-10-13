package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The positions a StyledText answers before the render side has laid the text out follow the
 * attributes that move text: the width of a tab, the indent of a line and a bullet; and a bidi segment
 * array that does not start at the start of the line is taken on Cocoa and refused elsewhere.
 */
@ExtendWith(Mocks.class)
class StyledTextRowAttributesTest {

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

    private static StyledText styledText(int style, String text) {
        StyledText st = new StyledText(Mocks.shell(), style);
        st.setBounds(0, 0, 80, 200);
        st.setText(text);
        return st;
    }

    @Test
    void the_tab_setting_sets_the_width_of_a_tab() {
        // A tab at the line start: a stop narrower than a character before it would land on the next one.
        StyledText st = styledText(SWT.NONE, "\tb");

        st.setTabs(2);
        int narrow = st.getLocationAtOffset(1).x;
        st.setTabs(4);
        int normal = st.getLocationAtOffset(1).x;
        st.setTabs(8);
        int wide = st.getLocationAtOffset(1).x;

        assertThat(narrow).isLessThan(normal);
        assertThat(normal).isLessThan(wide);
    }

    @Test
    void an_indent_moves_the_text_of_every_line() {
        StyledText st = styledText(SWT.NONE, "abc\ndef");
        int plain = st.getLocationAtOffset(0).x;

        st.setIndent(10);

        assertThat(st.getLocationAtOffset(0).x).isEqualTo(plain + 10);
        assertThat(st.getLocationAtOffset(4).x).isEqualTo(plain + 10);
    }

    @Test
    void a_line_indent_moves_only_its_line() {
        StyledText st = styledText(SWT.NONE, "abc\ndef");
        int plain = st.getLocationAtOffset(0).x;

        st.setLineIndent(1, 1, 25);

        assertThat(st.getLocationAtOffset(0).x).isEqualTo(plain);
        assertThat(st.getLocationAtOffset(4).x).isEqualTo(plain + 25);
    }

    @Test
    void a_bullet_moves_the_text_by_its_width() {
        StyledText st = styledText(SWT.NONE, "abc\ndef");
        int plain = st.getLocationAtOffset(0).x;
        StyleRange style = new StyleRange(0, 0, null, null);
        style.metrics = new GlyphMetrics(0, 0, 20);

        st.setLineBullet(0, 1, new Bullet(ST.BULLET_DOT, style));

        assertThat(st.getLocationAtOffset(0).x).isEqualTo(plain + 20);
        assertThat(st.getLocationAtOffset(4).x).isEqualTo(plain);
    }

    @Test
    void a_bidi_segment_array_may_start_after_the_start_of_the_line() {
        StyledText st = styledText(SWT.NONE, "abc def");
        st.addBidiSegmentListener(event -> event.segments = new int[] {1, 3});

        assertThat(st.getLocationAtOffset(2).x).isGreaterThan(st.getLocationAtOffset(1).x);
    }
}
