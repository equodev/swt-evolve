package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class FontDataSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_FontData() {
        FontData w = new FontData();
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_FontData() {
        FontData w = new FontData();
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("locale", json(w.getLocale()))
               .containsEntry("name", json(w.getName()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("height").equalsTo(w.getHeight(), orAbsentIf0));
    }

    VFontData value(FontData w) {
        return ((DartFontData) w.getImpl()).getValue();
    }
}
