package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ToolTipSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ToolTip() {
        ToolTip w = new ToolTip(swtShell(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolTip");
    }

    @Test
    void should_serialize_filled_ToolTip() {
        ToolTip w = new ToolTip(swtShell(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolTip")
               .containsEntry("message", json(w.getMessage()))
               .containsEntry("text", json(w.getText()))
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("location").equalsTo(value(w).getLocation(), orAbsentIfNull));
    }

    @Test
    void should_name_every_change_ToolTip() {
        ToolTip w = new ToolTip(swtShell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VToolTip value(ToolTip w) {
        return ((DartToolTip) w.getImpl()).getValue();
    }
}
