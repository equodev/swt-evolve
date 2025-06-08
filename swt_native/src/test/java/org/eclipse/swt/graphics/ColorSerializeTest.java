package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ColorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Color() {
        Color w = new Color(device(), red(), green(), blue());
        String json = serialize(w);
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Color() {
        Color w = new Color(device(), red(), green(), blue());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.isNotEmpty();
    }

    VColor value(Color w) {
        return ((DartColor) w.getImpl()).getValue();
    }
}
