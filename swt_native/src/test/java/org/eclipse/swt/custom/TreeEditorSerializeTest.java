package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class TreeEditorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TreeEditor() {
        TreeEditor w = new TreeEditor(tree());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_TreeEditor() {
        TreeEditor w = new TreeEditor(tree());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
        assertJ.satisfies(node("column").equalsTo(w.getColumn(), orAbsentIf0));
        assertJ.satisfies(node("editor").equalsTo(value(w).getEditor(), orAbsentIfNull));
        assertJ.satisfies(node("item").equalsTo(value(w).getItem(), orAbsentIfNull));
        assertJ.satisfies(node("editor").equalsTo(w.getEditor(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_TreeEditor() throws Exception {
        assertWritesEveryProperty(style -> {
            TreeEditor w = new TreeEditor(new Tree(shell(), SWT.CHECK));
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    VTreeEditor value(TreeEditor w) {
        return ((DartTreeEditor) w.getImpl()).getValue();
    }
}
