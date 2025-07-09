package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ComboSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Combo() {
        Combo w = new Combo(composite(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Combo");
    }

    @Test
    void should_serialize_filled_Combo() {
        Combo w = new Combo(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Combo")
               .containsEntry("text", json(w.getText()))
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("ignoreSelection").equalsTo(value(w).getIgnoreSelection(), orAbsentIfFalse));
        assertJ.satisfies(node("listVisible").equalsTo(w.getListVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIfNull));
        assertJ.satisfies(node("textLimit").equalsTo(w.getTextLimit(), orAbsentIf0));
        assertJ.satisfies(node("visibleItemCount").equalsTo(w.getVisibleItemCount(), orAbsentIf0));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
        assertJ.satisfies(node("layoutDeferred").equalsTo(w.getLayoutDeferred(), orAbsentIfFalse));
        assertJ.satisfies(node("scrollbarsMode").equalsTo(w.getScrollbarsMode(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(w.getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("capture").equalsTo(value(w).getCapture(), orAbsentIfFalse));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("orientation").equalsTo(w.getOrientation(), orAbsentIf0));
        assertJ.satisfies(node("redraw").equalsTo(value(w).getRedraw(), orAbsentIfFalse));
        assertJ.satisfies(node("textDirection").equalsTo(w.getTextDirection(), orAbsentIf0));
        assertJ.satisfies(node("touchEnabled").equalsTo(w.getTouchEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("visible").equalsTo(w.getVisible(), orAbsentIfFalse));
    }

    VCombo value(Combo w) {
        return ((DartCombo) w.getImpl()).getValue();
    }
}
