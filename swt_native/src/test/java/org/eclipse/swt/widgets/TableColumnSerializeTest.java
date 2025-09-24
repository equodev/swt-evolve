package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class TableColumnSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TableColumn() {
        TableColumn w = new TableColumn(table(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TableColumn");
    }

    @Test
    void should_serialize_filled_TableColumn() {
        TableColumn w = new TableColumn(table(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TableColumn")
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alignment").equalsTo(w.getAlignment(), orAbsentIf0));
        assertJ.satisfies(node("moveable").equalsTo(w.getMoveable(), orAbsentIfFalse));
        assertJ.satisfies(node("resizable").equalsTo(w.getResizable(), orAbsentIfFalse));
        assertJ.satisfies(node("width").equalsTo(w.getWidth(), orAbsentIf0));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    VTableColumn value(TableColumn w) {
        return ((DartTableColumn) w.getImpl()).getValue();
    }
}
