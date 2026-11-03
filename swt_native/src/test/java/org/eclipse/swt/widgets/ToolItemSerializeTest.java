package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class ToolItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ToolItem() {
        ToolItem w = new ToolItem(toolBar(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolItem");
    }

    @Test
    void should_serialize_filled_ToolItem() {
        ToolItem w = new ToolItem(toolBar(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolItem")
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("disabledImage").equalsTo(w.getDisabledImage(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("hotImage").equalsTo(w.getHotImage(), orAbsentIfNull));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIfFalse));
        assertJ.satisfies(node("width").equalsTo(w.getWidth(), orAbsentIf0));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_ToolItem() throws Exception {
        assertWritesEveryProperty(style -> {
            ToolItem w = new ToolItem(new ToolBar(shell(), SWT.NONE), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE, SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.SEPARATOR, SWT.DROP_DOWN);
    }

    @Test
    void should_name_every_change_ToolItem() {
        ToolItem w = new ToolItem(toolBar(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VToolItem value(ToolItem w) {
        return ((DartToolItem) w.getImpl()).getValue();
    }
}
