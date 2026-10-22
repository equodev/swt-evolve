package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What the render side is told about the bullets of a StyledText. */
@Tag("native-unit")
public class StyledTextWireNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Bullet bullet(int type, String text) {
        StyleRange style = new StyleRange(0, 0, null, null);
        style.metrics = new GlyphMetrics(0, 0, 20);
        Bullet bullet = new Bullet(type, style);
        bullet.text = text;
        return bullet;
    }

    private static DartStyledTextRenderer rendererOf(StyledText text) {
        return (DartStyledTextRenderer) ((DartStyledText) text.getImpl()).renderer.getImpl();
    }

    @Test
    public void a_line_without_a_bullet_has_no_text_and_a_text_without_bullets_has_none_at_all() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("a\nb\nc");

        assertThat(StyledTextHelper.wireBulletTexts(rendererOf(text))).isNull();

        text.setLineBullet(1, 1, bullet(ST.BULLET_DOT, null));
        assertThat(StyledTextHelper.wireBulletTexts(rendererOf(text))).containsExactly(null, "•", null);
    }

    @Test
    public void each_kind_of_bullet_is_the_text_the_renderer_draws() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("a\nb\nc\nd\ne\nf");
        text.setLineBullet(0, 1, bullet(ST.BULLET_DOT, null));
        text.setLineBullet(1, 2, bullet(ST.BULLET_NUMBER, null));
        text.setLineBullet(3, 1, bullet(ST.BULLET_LETTER_LOWER, null));
        text.setLineBullet(4, 1, bullet(ST.BULLET_LETTER_UPPER, null));
        text.setLineBullet(5, 1, bullet(ST.BULLET_NUMBER | ST.BULLET_TEXT, "."));

        assertThat(StyledTextHelper.wireBulletTexts(rendererOf(text))).containsExactly("•", "1", "2", "a", "A", "1.");
    }

    @Test
    public void a_custom_bullet_has_no_text_to_draw() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("a\nb");
        text.setLineBullet(0, 1, bullet(ST.BULLET_CUSTOM, null));

        assertThat(StyledTextHelper.wireBulletTexts(rendererOf(text))).containsExactly("", null);
    }
}
