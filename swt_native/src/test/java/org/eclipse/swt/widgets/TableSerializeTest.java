package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class TableSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Table() {
        Table w = new Table(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Table");
    }

    @Test
    void should_serialize_filled_Table() {
        Table w = new Table(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Table")
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
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
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(value(w).getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("cursor").equalsTo(w.getCursor(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_Table() throws Exception {
        assertWritesEveryProperty(style -> {
            Table w = new Table(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("hasOwnBackground", "the client never reads it off a Table"), SWT.NONE, SWT.SINGLE, SWT.MULTI, SWT.CHECK, SWT.FULL_SELECTION, SWT.HIDE_SELECTION, SWT.VIRTUAL, SWT.NO_SCROLL, SWT.NO_SEARCH, SWT.NO_BACKGROUND, SWT.NO_FOCUS, SWT.NO_MERGE_PAINTS, SWT.NO_REDRAW_RESIZE, SWT.NO_RADIO_GROUP, SWT.EMBEDDED, SWT.DOUBLE_BUFFERED, SWT.H_SCROLL, SWT.V_SCROLL, SWT.BORDER, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_Table() {
        Table w = new Table(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VTable value(Table w) {
        return ((DartTable) w.getImpl()).getValue();
    }
}
