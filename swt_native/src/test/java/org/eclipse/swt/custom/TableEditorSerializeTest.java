package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class TableEditorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TableEditor() {
        TableEditor w = new TableEditor(table());
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_TableEditor() {
        TableEditor w = new TableEditor(table());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
        assertJ.satisfies(node("column").equalsTo(w.getColumn(), orAbsentIf0));
        assertJ.satisfies(node("item").equalsTo(value(w).getItem(), orAbsentIfNull));
        assertJ.satisfies(node("editor").equalsTo(w.getEditor(), orAbsentIfNull));
    }

    VTableEditor value(TableEditor w) {
        return ((DartTableEditor) w.getImpl()).getValue();
    }
}
