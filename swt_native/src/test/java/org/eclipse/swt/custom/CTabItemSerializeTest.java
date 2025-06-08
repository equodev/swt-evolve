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
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.containsEntry("id", w.hashCode())
                  .containsEntry("swt", "CTabItem")
                  .containsEntry("style", w.getStyle());
    }

    @Test
    void should_serialize_filled_CTabItem() {
        CTabItem w = new CTabItem(cTabFolder(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.containsEntry("id", w.hashCode())
                  .containsEntry("swt", "CTabItem")
                  .containsEntry("style", w.getStyle());
        assertJson.node("control")
                  .satisfies(isEqualTo(w.getControl(), ifNotNull()));
        assertJson.node("foreground")
                  .satisfies(isEqualTo(w.getForeground(), ifNotNull()));
        assertJson.node("selectionForeground")
                  .satisfies(isEqualTo(w.getSelectionForeground(), ifNotNull()));
        assertJson.node("showClose")
                  .satisfies(isEqualTo(w.getShowClose(), orAbsentIfFalse()));
        assertJson.node("toolTipText")
                  .satisfies(isEqualTo(w.getToolTipText(), ifNotNull()));
        assertJson.node("shortenedText")
                  .satisfies(isEqualTo(value(w).getShortenedText(), ifNotNull()));
        assertJson.node("text")
                  .satisfies(isEqualTo(w.getText(), ifNotNull()));
    }

    VCTabItem value(CTabItem w) {
        return ((DartCTabItem) w.getImpl()).getValue();
    }
}
