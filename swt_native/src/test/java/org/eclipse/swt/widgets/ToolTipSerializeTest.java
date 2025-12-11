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
        ToolTip w = new ToolTip(shell(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolTip");
    }

    @Test
    void should_serialize_filled_ToolTip() {
        ToolTip w = new ToolTip(shell(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ToolTip")
               .containsEntry("message", json(w.getMessage()))
               .containsEntry("text", json(w.getText()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("autoHide").equalsTo(w.getAutoHide(), orAbsentIfFalse));
        assertJ.satisfies(node("location").equalsTo(value(w).getLocation(), orAbsentIfNull));
        assertJ.satisfies(node("visible").equalsTo(w.getVisible(), orAbsentIfFalse));
    }

    VToolTip value(ToolTip w) {
        return ((DartToolTip) w.getImpl()).getValue();
    }
}
