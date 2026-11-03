package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class MenuSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Menu() {
        Menu w = new Menu(control());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Menu");
    }

    @Test
    void should_serialize_filled_Menu() {
        Menu w = new Menu(control());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Menu")
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("location").equalsTo(value(w).getLocation(), orAbsentIfNull));
        assertJ.satisfies(node("orientation").equalsTo(w.getOrientation(), orAbsentIf0));
    }

    @Test
    void should_write_every_property_Menu() throws Exception {
        assertWritesEveryProperty(style -> {
            Menu w = new Menu(control());
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    @Test
    void should_name_every_change_Menu() {
        Menu w = new Menu(control());
        assertNamesEveryChange(w);
    }

    VMenu value(Menu w) {
        return ((DartMenu) w.getImpl()).getValue();
    }
}
