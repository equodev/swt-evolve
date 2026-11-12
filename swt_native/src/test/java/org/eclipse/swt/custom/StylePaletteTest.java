package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;

/**
 * A StyledText's palette keeps every style it was used with, so it starts over once it holds more
 * than it may, or a style whose colour or font the application has disposed, which can no longer
 * be sent.
 */
@ExtendWith(Mocks.class)
class StylePaletteTest {

    private static StyleRange styled(int red) {
        StyleRange style = new StyleRange();
        style.foreground = new Color(device(), red, 0, 0);
        return style;
    }

    @Test
    void aPaletteOfLiveStylesIsKept() {
        StylePalette palette = new StylePalette();
        palette.placeOf(styled(1));
        palette.placeOf(new StyleRange());

        assertThat(palette.stale()).isFalse();
    }

    @Test
    void aPalettePastItsLimitStartsOver() {
        StylePalette palette = new StylePalette();
        for (int i = 0; i < StylePalette.MAX; i++) palette.placeOf(styled(i));
        assertThat(palette.stale()).as("%d styles", StylePalette.MAX).isFalse();

        palette.placeOf(styled(StylePalette.MAX));

        assertThat(palette.stale()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"foreground", "background", "underlineColor", "strikeoutColor", "borderColor"})
    void aStyleWhoseColourWasDisposedMakesItStartOver(String field) throws ReflectiveOperationException {
        Color colour = new Color(device(), 10, 20, 30);
        StyleRange style = new StyleRange();
        TextStyle.class.getField(field).set(style, colour);
        StylePalette palette = new StylePalette();
        palette.placeOf(style);
        assertThat(palette.stale()).isFalse();

        colour.dispose();

        assertThat(palette.stale()).isTrue();
    }

    @Test
    void aStyleWhoseFontWasDisposedMakesItStartOver() {
        Font font = new Font(device(), "Inter", 11, SWT.NORMAL);
        StyleRange style = new StyleRange();
        style.font = font;
        StylePalette palette = new StylePalette();
        palette.placeOf(style);
        assertThat(palette.stale()).isFalse();

        font.dispose();

        assertThat(palette.stale()).isTrue();
    }
}
