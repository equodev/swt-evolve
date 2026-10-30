package org.eclipse.swt.accessibility;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

@Disabled
class AccessibleSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Accessible() {
        Accessible w = new Accessible(accessible());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Accessible() {
        Accessible w = new Accessible(accessible());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("focus").equalsTo(value(w).getFocus(), orAbsentIf0));
    }

    @Test
    void should_write_every_property_Accessible() throws Exception {
        assertWritesEveryProperty(style -> {
            Accessible w = new Accessible(accessible());
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    VAccessible value(Accessible w) {
        return ((DartAccessible) w.getImpl()).getValue();
    }
}
