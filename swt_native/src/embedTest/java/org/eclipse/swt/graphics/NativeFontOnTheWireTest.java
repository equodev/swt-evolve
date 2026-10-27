package org.eclipse.swt.graphics;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A native font can reach the wire where no setter converts it: a StyleRange's font is the
 * application's own object, read at serialization. It is written as its Dart copy instead of
 * failing the whole payload.
 */
class NativeFontOnTheWireTest {

    private static String write(java.util.function.Consumer<JsonWriter> body) {
        DslJson<Object> dsl = new DslJson<>(new DslJson.Settings<>()
                .includeServiceLoader(Serializer.class.getClassLoader()));
        JsonWriter writer = dsl.newWriter();
        body.accept(writer);
        return new String(writer.getByteBuffer(), 0, writer.size(), StandardCharsets.UTF_8);
    }

    private static Font nativeFont(Display display) {
        Font font = mock(Font.class);
        when(font.getImpl()).thenReturn(mock(SwtFont.class));
        when(font.getDevice()).thenReturn(display);
        FontData data = new FontData((IFontData) null);
        data.setImpl(new DartFontData("Courier", 13, SWT.BOLD, data));
        when(font.getFontData()).thenReturn(new FontData[] {data});
        return font;
    }

    @Test
    void aNativeFontIsWrittenAsItsDartCopy() {
        Display display = org.eclipse.swt.widgets.Mocks.display();
        Font font = nativeFont(display);

        String json = write(w -> VFont.FontJson.write(w, font));

        assertThat(json).contains("Courier").contains("13");
    }

    @Test
    void aNativeFontDataIsWrittenAsItsDartCopy() {
        FontData data = mock(FontData.class);
        when(data.getImpl()).thenReturn(mock(SwtFontData.class));
        when(data.getName()).thenReturn("Courier");
        when(data.getHeight()).thenReturn(13);
        when(data.getStyle()).thenReturn(SWT.ITALIC);

        String json = write(w -> VFontData.FontDataJson.write(w, data));

        assertThat(json).contains("Courier").contains("13");
    }
}
