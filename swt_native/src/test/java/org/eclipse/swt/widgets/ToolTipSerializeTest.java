package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class ToolTipSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ToolTip() {
        ToolTip w = new ToolTip(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolTip");
    }

    @Test
    void should_serialize_filled_ToolTip() {
        ToolTip w = new ToolTip(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolTip")
               .containsEntry("message", w.getMessage())
               .containsEntry("text", w.getText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("location").equalsTo(value(w).getLocation(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_ToolTip() throws Exception {
        assertWritesEveryProperty(style -> {
            ToolTip w = new ToolTip(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE, SWT.BALLOON, SWT.ICON_ERROR, SWT.ICON_INFORMATION, SWT.ICON_WARNING);
    }

    @Test
    void should_name_every_change_ToolTip() {
        ToolTip w = new ToolTip(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VToolTip value(ToolTip w) {
        return ((DartToolTip) w.getImpl()).getValue();
    }
}
