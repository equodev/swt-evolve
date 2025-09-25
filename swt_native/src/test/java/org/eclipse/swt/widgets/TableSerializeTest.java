package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class TableSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Table() {
        Table w = new Table(composite(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Table");
    }

    @Test
    void should_serialize_filled_Table() {
        Table w = new Table(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Table")
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("columnOrder").equalsTo(w.getColumnOrder(), orAbsentIfNull));
        assertJ.satisfies(node("headerBackground").equalsTo(w.getHeaderBackground(), orAbsentIfNull));
        assertJ.satisfies(node("headerForeground").equalsTo(w.getHeaderForeground(), orAbsentIfNull));
        assertJ.satisfies(node("headerVisible").equalsTo(w.getHeaderVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("linesVisible").equalsTo(w.getLinesVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("selection").equalsTo(value(w).getSelection(), orAbsentIfNull));
        assertJ.satisfies(node("sortColumn").equalsTo(w.getSortColumn(), orAbsentIfNull));
        assertJ.satisfies(node("sortDirection").equalsTo(w.getSortDirection(), orAbsentIf0));
        assertJ.satisfies(node("topIndex").equalsTo(w.getTopIndex(), orAbsentIf0));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
        assertJ.satisfies(node("layoutDeferred").equalsTo(w.getLayoutDeferred(), orAbsentIfFalse));
        assertJ.satisfies(node("scrollbarsMode").equalsTo(w.getScrollbarsMode(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
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

    VTable value(Table w) {
        return ((DartTable) w.getImpl()).getValue();
    }
}
