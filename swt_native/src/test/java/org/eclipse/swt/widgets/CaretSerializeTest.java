package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class CaretSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Caret() {
        Caret w = new Caret(canvas(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Caret");
    }

    @Test
    void should_serialize_filled_Caret() {
        Caret w = new Caret(canvas(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Caret")
               .containsEntry("style", w.getStyle());
    }

    @Test
    void should_name_every_change_Caret() {
        Caret w = new Caret(canvas(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VCaret value(Caret w) {
        return ((DartCaret) w.getImpl()).getValue();
    }
}
