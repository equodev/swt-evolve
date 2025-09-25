package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class CTabItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CTabItem() {
        CTabItem w = new CTabItem(cTabFolder(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabItem");
    }

    @Test
    void should_serialize_filled_CTabItem() {
        CTabItem w = new CTabItem(cTabFolder(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabItem")
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("disabledImage").equalsTo(w.getDisabledImage(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionForeground").equalsTo(w.getSelectionForeground(), orAbsentIfNull));
        assertJ.satisfies(node("showClose").equalsTo(w.getShowClose(), orAbsentIfFalse));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    VCTabItem value(CTabItem w) {
        return ((DartCTabItem) w.getImpl()).getValue();
    }
}
