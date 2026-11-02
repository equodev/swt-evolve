package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class CoolItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CoolItem() {
        CoolItem w = new CoolItem(coolBar(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CoolItem");
    }

    @Test
    void should_serialize_filled_CoolItem() {
        CoolItem w = new CoolItem(coolBar(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CoolItem")
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("preferredSize").equalsTo(value(w).getPreferredSize(), orAbsentIfNull));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_CoolItem() throws Exception {
        assertWritesEveryProperty(style -> {
            CoolItem w = new CoolItem(new CoolBar(shell(), SWT.NONE), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE, SWT.DROP_DOWN);
    }

    @Test
    void should_name_every_change_CoolItem() {
        CoolItem w = new CoolItem(coolBar(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VCoolItem value(CoolItem w) {
        return ((DartCoolItem) w.getImpl()).getValue();
    }
}
