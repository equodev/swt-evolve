package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class TreeColumnSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TreeColumn() {
        TreeColumn w = new TreeColumn(tree(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TreeColumn");
    }

    @Test
    void should_serialize_filled_TreeColumn() {
        TreeColumn w = new TreeColumn(tree(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TreeColumn")
               .containsEntry("resizable", w.getResizable())
               .containsEntry("width", w.getWidth())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alignment").equalsTo(w.getAlignment(), orAbsentIf0));
        assertJ.satisfies(node("moveable").equalsTo(w.getMoveable(), orAbsentIfFalse));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_TreeColumn() throws Exception {
        assertWritesEveryProperty(style -> {
            TreeColumn w = new TreeColumn(new Tree(shell(), SWT.CHECK), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE, SWT.LEFT, SWT.RIGHT, SWT.CENTER);
    }

    @Test
    void should_name_every_change_TreeColumn() {
        TreeColumn w = new TreeColumn(tree(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VTreeColumn value(TreeColumn w) {
        return ((DartTreeColumn) w.getImpl()).getValue();
    }
}
