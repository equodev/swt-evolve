package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class ColorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Color() {
        Color w = new Color(rGB());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Color() {
        Color w = new Color(rGB());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
        assertJ.satisfies(node("a").equalsTo(w.getAlpha(), orAbsentIf0));
        assertJ.satisfies(node("b").equalsTo(w.getBlue(), orAbsentIf0));
        assertJ.satisfies(node("g").equalsTo(w.getGreen(), orAbsentIf0));
        assertJ.satisfies(node("r").equalsTo(w.getRed(), orAbsentIf0));
    }

    @Test
    void should_write_every_property_Color() throws Exception {
        assertWritesEveryProperty(style -> {
            Color w = new Color(rGB());
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    VColor value(Color w) {
        return ((DartColor) w.getImpl()).getValue();
    }
}
