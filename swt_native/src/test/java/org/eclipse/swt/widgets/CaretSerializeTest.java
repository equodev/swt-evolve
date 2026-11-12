package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class CaretSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Caret() {
        Caret w = new Caret(canvas(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Caret");
    }

    @Test
    void should_serialize_filled_Caret() {
        Caret w = new Caret(canvas(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Caret")
               .containsEntry("visible", w.getVisible());
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_Caret() throws Exception {
        assertWritesEveryProperty(style -> {
            Caret w = new Caret(new Canvas(shell(), SWT.NONE), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("style", "the client never reads it off a Caret"), SWT.NONE);
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
