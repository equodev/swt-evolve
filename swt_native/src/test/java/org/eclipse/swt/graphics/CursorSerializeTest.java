package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class CursorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Cursor() {
        Cursor w = new Cursor(device(), SWT.CURSOR_WAIT);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Cursor() {
        Cursor w = new Cursor(device(), SWT.CURSOR_WAIT);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_write_every_property_Cursor() throws Exception {
        assertWritesEveryProperty(unwritable(), () -> {
            Cursor w = new Cursor(device(), SWT.CURSOR_WAIT);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, () -> {
            Cursor w = new Cursor(device(), new ImageData(4, 4, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)), 1, 2);
            setAll(w);
            fillUnsettable(w);
            return w;
        });
    }

    VCursor value(Cursor w) {
        return ((DartCursor) w.getImpl()).getValue();
    }
}
