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
               .containsEntry("text", json(w.getText()));
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionForeground").equalsTo(w.getSelectionForeground(), orAbsentIfNull));
        assertJ.satisfies(node("showClose").equalsTo(w.getShowClose(), orAbsentIfFalse));
        assertJ.satisfies(node("shortenedText").equalsTo(value(w).getShortenedText(), orAbsentIfNull));
        assertJ.satisfies(node("style").equalsTo(w.getStyle(), orAbsentIf0));
    }

    VCTabItem value(CTabItem w) {
        return ((DartCTabItem) w.getImpl()).getValue();
    }
}
