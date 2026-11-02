package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class ControlEditorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ControlEditor() {
        ControlEditor w = new ControlEditor(shell());
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_ControlEditor() {
        ControlEditor w = new ControlEditor(shell());
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
        assertJ.satisfies(node("editor").equalsTo(w.getEditor(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_ControlEditor() throws Exception {
        assertWritesEveryProperty(style -> {
            ControlEditor w = new ControlEditor(new Composite(shell(), SWT.NONE));
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    VControlEditor value(ControlEditor w) {
        return ((DartControlEditor) w.getImpl()).getValue();
    }
}
