package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class TableEditorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TableEditor() {
        TableEditor w = new TableEditor(table());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_TableEditor() {
        TableEditor w = new TableEditor(table());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
        assertJ.satisfies(node("column").equalsTo(w.getColumn(), orAbsentIf0));
        assertJ.satisfies(node("item").equalsTo(value(w).getItem(), orAbsentIfNull));
        assertJ.satisfies(node("editor").equalsTo(w.getEditor(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_TableEditor() throws Exception {
        assertWritesEveryProperty(style -> {
            TableEditor w = new TableEditor(new Table(shell(), SWT.CHECK));
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    VTableEditor value(TableEditor w) {
        return ((DartTableEditor) w.getImpl()).getValue();
    }
}
