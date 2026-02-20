package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ControlEditorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ControlEditor() {
        ControlEditor w = new ControlEditor(composite());
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_ControlEditor() {
        ControlEditor w = new ControlEditor(composite());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
        assertJ.satisfies(node("editor").equalsTo(w.getEditor(), orAbsentIfNull));
    }

    VControlEditor value(ControlEditor w) {
        return ((DartControlEditor) w.getImpl()).getValue();
    }
}
