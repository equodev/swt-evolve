package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class CLabelSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CLabel() {
        CLabel w = new CLabel(composite(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CLabel");
        assertJ.satisfies(node("align").equalsTo(w.getAlignment(), orAbsentIf0));
        assertJ.satisfies(node("bottomMargin").equalsTo(w.getBottomMargin(), orAbsentIf0));
        assertJ.satisfies(node("leftMargin").equalsTo(w.getLeftMargin(), orAbsentIf0));
        assertJ.satisfies(node("rightMargin").equalsTo(w.getRightMargin(), orAbsentIf0));
        assertJ.satisfies(node("topMargin").equalsTo(w.getTopMargin(), orAbsentIf0));
    }

    @Test
    void should_serialize_filled_CLabel() {
        CLabel w = new CLabel(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CLabel")
               .containsEntry("style", w.getStyle())
               .containsEntry("text", json(w.getText()))
               .containsEntry("appToolTipText", json(w.getToolTipText()))
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("align").equalsTo(w.getAlignment(), orAbsentIf0));
        assertJ.satisfies(node("bottomMargin").equalsTo(w.getBottomMargin(), orAbsentIf0));
        assertJ.satisfies(node("leftMargin").equalsTo(w.getLeftMargin(), orAbsentIf0));
        assertJ.satisfies(node("rightMargin").equalsTo(w.getRightMargin(), orAbsentIf0));
        assertJ.satisfies(node("topMargin").equalsTo(w.getTopMargin(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(value(w).getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("caret").equalsTo(w.getCaret(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
        assertJ.satisfies(node("layoutDeferred").equalsTo(w.getLayoutDeferred(), orAbsentIfFalse));
        assertJ.satisfies(node("scrollbarsMode").equalsTo(w.getScrollbarsMode(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(value(w).getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("foreground").equalsTo(value(w).getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("orientation").equalsTo(w.getOrientation(), orAbsentIf0));
        assertJ.satisfies(node("textDirection").equalsTo(w.getTextDirection(), orAbsentIf0));
        assertJ.satisfies(node("touchEnabled").equalsTo(w.getTouchEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("visible").equalsTo(w.getVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("capture").equalsTo(value(w).getCapture(), orAbsentIfFalse));
        assertJ.satisfies(node("redraw").equalsTo(value(w).getRedraw(), orAbsentIfFalse));
    }

    VCLabel value(CLabel w) {
        return ((DartCLabel) w.getImpl()).getValue();
    }
}
