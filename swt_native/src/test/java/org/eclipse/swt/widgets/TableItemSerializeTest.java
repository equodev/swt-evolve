package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class TableItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TableItem() {
        TableItem w = new TableItem(table(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TableItem");
    }

    @Test
    void should_serialize_filled_TableItem() {
        TableItem w = new TableItem(table(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TableItem")
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("checked").equalsTo(w.getChecked(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("grayed").equalsTo(w.getGrayed(), orAbsentIfFalse));
        assertJ.satisfies(node("texts").sameSizeAs(value(w).getTexts()));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_TableItem() throws Exception {
        assertWritesEveryProperty(style -> {
            TableItem w = new TableItem(new Table(shell(), SWT.CHECK), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    @Test
    void should_name_every_change_TableItem() {
        TableItem w = new TableItem(table(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VTableItem value(TableItem w) {
        return ((DartTableItem) w.getImpl()).getValue();
    }
}
