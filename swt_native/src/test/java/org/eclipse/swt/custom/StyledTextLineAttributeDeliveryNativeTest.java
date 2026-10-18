package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every line attribute setter reports the renderer changed, so the render side receives the new
 * line state with the next update instead of with whatever change happens to follow it.
 */
@Tag("native-unit")
public class StyledTextLineAttributeDeliveryNativeTest {

    private Display display;
    private StyledText text;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("a\nb\nc\nd");
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Bullet dot() {
        StyleRange style = new StyleRange();
        style.metrics = new GlyphMetrics(0, 0, 20);
        return new Bullet(ST.BULLET_DOT, style);
    }

    private VStyledText value() {
        return ((DartStyledText) text.getImpl()).getValue();
    }

    private DartStyledTextRenderer renderer() {
        return (DartStyledTextRenderer) ((DartStyledText) text.getImpl()).renderer.getImpl();
    }

    /** Forgets what is pending, as a send does. */
    private void delivered() {
        value().clearDirty();
    }

    private void assertRendererReported() {
        assertThat(value().changedKeys()).as("the properties reported changed").contains(VStyledText.RENDERER);
    }

    @Test
    @DisplayName("a bullet already on another line, set on one more line, is reported")
    void sharedBulletOnAnotherLine() {
        Bullet bullet = dot();
        text.setLineBullet(0, 1, bullet);
        delivered();

        text.setLineBullet(2, 1, bullet);

        assertThat(StyledTextHelper.wireBulletTexts(renderer())).containsExactly("•", null, "•", null);
        assertRendererReported();
    }

    @Test
    @DisplayName("a bullet removed from one of the lines that share it is reported")
    void sharedBulletRemovedFromOneLine() {
        Bullet bullet = dot();
        text.setLineBullet(0, 2, bullet);
        delivered();

        text.setLineBullet(1, 1, null);

        assertThat(StyledTextHelper.wireBulletTexts(renderer())).containsExactly("•", null, null, null);
        assertRendererReported();
    }

    @Test
    @DisplayName("clearing the background of a line that keeps another attribute is reported")
    void backgroundClearedOnALineWithAnIndent() {
        text.setLineIndent(1, 1, 10);
        text.setLineBackground(1, 1, new Color(display, 200, 0, 0));
        delivered();

        text.setLineBackground(1, 1, null);

        assertThat(text.getLineBackground(1)).isNull();
        assertRendererReported();
    }

    @Test
    @DisplayName("alignment, indent, wrap indent, justify, tab stops and background are reported")
    void everyOtherLineAttribute() {
        Runnable[] setters = {
            () -> text.setLineAlignment(1, 1, SWT.RIGHT),
            () -> text.setLineIndent(1, 1, 20),
            () -> text.setLineWrapIndent(1, 1, 30),
            () -> text.setLineJustify(1, 1, true),
            () -> text.setLineTabStops(1, 1, new int[] { 40 }),
            () -> text.setLineBackground(1, 1, new Color(display, 0, 200, 0)),
        };
        for (Runnable setter : setters) {
            delivered();
            setter.run();
            assertRendererReported();
        }
    }
}
