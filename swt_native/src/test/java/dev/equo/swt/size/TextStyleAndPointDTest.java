package dev.equo.swt.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.junit.jupiter.api.Test;

class TextStyleAndPointDTest {

    @Test
    void textStylesWithTheSameFieldsAreEqual() {
        TextStyle style = new TextStyle("Inter", 20, false, 500, 1.25);

        assertThat(style).isEqualTo(style);
        assertThat(style).isEqualTo(new TextStyle("Inter", 20, false, 500, 1.25));
        assertThat(style).hasSameHashCodeAs(new TextStyle("Inter", 20, false, 500, 1.25));
        assertThat(style).isNotEqualTo(new TextStyle("Inter", 20, false, 500, 1.5));
        assertThat(style).isNotEqualTo(new TextStyle("Roboto", 20, false, 500, 1.25));
        assertThat(style).isNotEqualTo(new TextStyle("Inter", 12, false, 500, 1.25));
        assertThat(style).isNotEqualTo(new TextStyle("Inter", 20, true, 500, 1.25));
        assertThat(style).isNotEqualTo(new TextStyle("Inter", 20, false, 700, 1.25));
        assertThat(style).isNotEqualTo("Inter");
        assertThat(style).hasToString("TextStyle[name=Inter, size=20, italic=false, weight=500, height=1.25]");
    }

    @Test
    void aTextStyleKeepsItsStyleForNoFont() {
        TextStyle style = new TextStyle("Inter", 20, false, 500);

        assertThat(style.withStyleFrom(null)).isSameAs(style);
        assertThat(style.withStyleFrom(fontWith((FontData[]) null))).isSameAs(style);
        assertThat(style.withStyleFrom(fontWith())).isSameAs(style);
    }

    @Test
    void aTextStyleTakesItalicAndBoldFromAFontAndKeepsItsOwnFaceAndSize() {
        TextStyle style = new TextStyle("Inter", 20, false, 500, 1.25);

        TextStyle styled = style.withStyleFrom(fontWith(new FontData("Arial", 9, SWT.BOLD | SWT.ITALIC)));

        assertThat(styled).isEqualTo(new TextStyle("Inter", 20, true, 700, 1.25));
        assertThat(style.withStyleFrom(fontWith(new FontData("Arial", 9, SWT.NORMAL)))).isEqualTo(style);
    }

    private static Font fontWith(FontData... data) {
        Font font = mock(Font.class);
        when(font.getFontData()).thenReturn(data);
        return font;
    }

    @Test
    void pointsWithTheSameCoordinatesAreEqual() {
        PointD point = new PointD(1.5, 2);

        assertThat(point).isEqualTo(point);
        assertThat(point).isEqualTo(new PointD(1.5, 2));
        assertThat(point).hasSameHashCodeAs(new PointD(1.5, 2));
        assertThat(point).isNotEqualTo(new PointD(1.5, 3));
        assertThat(point).isNotEqualTo(new PointD(2, 2));
        assertThat(point).isNotEqualTo("1.5, 2");
        assertThat(point).hasToString("PointD[x=1.5, y=2.0]");
    }
}
