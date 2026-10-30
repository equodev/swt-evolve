package org.eclipse.swt.graphics;

import dev.equo.swt.WidgetWire;
import dev.equo.swt.WireDump;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A native font can reach the wire where no setter converts it: a StyleRange's font is the
 * application's own object, read at serialization. It is written as its Dart copy instead of
 * failing the whole payload.
 */
class NativeFontOnTheWireTest {

    private static Font nativeFont(Display display, FontData data) {
        Font font = mock(Font.class);
        when(font.getImpl()).thenReturn(mock(SwtFont.class));
        when(font.getDevice()).thenReturn(display);
        when(font.getFontData()).thenReturn(new FontData[] {data});
        return font;
    }

    /** Whether some record at any depth of {@code tree} names this font. */
    private static boolean describesFont(Object tree, String name, int height) {
        if (tree instanceof Map<?, ?> map) {
            if (name.equals(map.get("name")) && map.get("height") instanceof Number h && h.intValue() == height) return true;
            return map.values().stream().anyMatch(v -> describesFont(v, name, height));
        }
        if (tree instanceof List<?> list) return list.stream().anyMatch(v -> describesFont(v, name, height));
        return false;
    }

    private static Map<String, Object> written(Font font) {
        TextStyle style = new TextStyle();
        style.font = font;
        byte[] fields = WidgetWire.fieldsOf(style);
        return WireDump.fields(fields, 0, fields.length);
    }

    @Test
    void aNativeFontIsWrittenAsItsDartCopy() {
        Display display = org.eclipse.swt.widgets.Mocks.display();
        FontData data = new FontData((IFontData) null);
        data.setImpl(new DartFontData("Courier", 13, SWT.BOLD, data));

        assertThat(describesFont(written(nativeFont(display, data)), "Courier", 13)).isTrue();
    }

    @Test
    void aNativeFontDataIsWrittenAsItsDartCopy() {
        Display display = org.eclipse.swt.widgets.Mocks.display();
        FontData data = mock(FontData.class);
        when(data.getImpl()).thenReturn(mock(SwtFontData.class));
        when(data.getName()).thenReturn("Courier");
        when(data.getHeight()).thenReturn(13);
        when(data.getStyle()).thenReturn(SWT.ITALIC);

        assertThat(describesFont(written(nativeFont(display, data)), "Courier", 13)).isTrue();
    }
}
