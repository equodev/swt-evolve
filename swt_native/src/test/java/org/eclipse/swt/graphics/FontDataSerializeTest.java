package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class FontDataSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_FontData() {
        FontData w = new FontData();
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_FontData() {
        FontData w = new FontData();
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("name", w.getName())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("height").equalsTo(w.getHeight(), orAbsentIf0));
    }

    @Test
    void should_write_every_property_FontData() throws Exception {
        assertWritesEveryProperty(style -> {
            FontData w = new FontData();
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    VFontData value(FontData w) {
        return ((DartFontData) w.getImpl()).getValue();
    }
}
