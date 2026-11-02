package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class MenuItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_MenuItem() {
        MenuItem w = new MenuItem(menu(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "MenuItem");
    }

    @Test
    void should_serialize_filled_MenuItem() {
        MenuItem w = new MenuItem(menu(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "MenuItem")
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("accelerator").equalsTo(w.getAccelerator(), orAbsentIf0));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIfFalse));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_MenuItem() throws Exception {
        assertWritesEveryProperty(style -> {
            MenuItem w = new MenuItem(menu(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE, SWT.CHECK, SWT.CASCADE, SWT.PUSH, SWT.RADIO, SWT.SEPARATOR);
    }

    @Test
    void should_name_every_change_MenuItem() {
        MenuItem w = new MenuItem(menu(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VMenuItem value(MenuItem w) {
        return ((DartMenuItem) w.getImpl()).getValue();
    }
}
