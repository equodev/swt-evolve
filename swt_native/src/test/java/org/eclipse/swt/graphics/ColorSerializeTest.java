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
        Color w = new Color(rGB());
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Color() {
        Color w = new Color(rGB());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
        assertJ.satisfies(node("alpha").equalsTo(w.getAlpha(), orAbsentIf0));
        assertJ.satisfies(node("blue").equalsTo(w.getBlue(), orAbsentIf0));
        assertJ.satisfies(node("green").equalsTo(w.getGreen(), orAbsentIf0));
        assertJ.satisfies(node("red").equalsTo(w.getRed(), orAbsentIf0));
    }

    VColor value(Color w) {
        return ((DartColor) w.getImpl()).getValue();
    }
}
