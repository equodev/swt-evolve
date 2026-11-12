package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class ExpandItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ExpandItem() {
        ExpandItem w = new ExpandItem(expandBar(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ExpandItem");
    }

    @Test
    void should_serialize_filled_ExpandItem() {
        ExpandItem w = new ExpandItem(expandBar(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ExpandItem");
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("expanded").equalsTo(w.getExpanded(), orAbsentIfFalse));
        assertJ.satisfies(node("height").equalsTo(w.getHeight(), orAbsentIf0));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_ExpandItem() throws Exception {
        assertWritesEveryProperty(style -> {
            ExpandItem w = new ExpandItem(new ExpandBar(shell(), SWT.NONE), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("style", "the client never reads it off a ExpandItem"), SWT.NONE);
    }

    @Test
    void should_name_every_change_ExpandItem() {
        ExpandItem w = new ExpandItem(expandBar(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VExpandItem value(ExpandItem w) {
        return ((DartExpandItem) w.getImpl()).getValue();
    }
}
