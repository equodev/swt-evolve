package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class FontSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Font() {
        Font w = new Font(device(), fontData());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Font() {
        Font w = new Font(device(), fontData());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_write_every_property_Font() throws Exception {
        assertWritesEveryProperty(style -> {
            Font w = new Font(device(), fontData());
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    VFont value(Font w) {
        return ((DartFont) w.getImpl()).getValue();
    }
}
