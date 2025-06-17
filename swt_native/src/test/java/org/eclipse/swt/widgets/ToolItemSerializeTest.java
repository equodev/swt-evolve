package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ToolItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ToolItem() {
        ToolItem w = new ToolItem(toolBar(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolItem");
        assertJ.satisfies(node("width").equalsTo(w.getWidth(), orAbsentIf0));
    }

    @Test
    void should_serialize_filled_ToolItem() {
        ToolItem w = new ToolItem(toolBar(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolItem")
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("text", json(value(w).getText()))
               .containsEntry("text", json(w.getText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIfFalse));
        assertJ.satisfies(node("width").equalsTo(w.getWidth(), orAbsentIf0));
    }

    VToolItem value(ToolItem w) {
        return ((DartToolItem) w.getImpl()).getValue();
    }
}
