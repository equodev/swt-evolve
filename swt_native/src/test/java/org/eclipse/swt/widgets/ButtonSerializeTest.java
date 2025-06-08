package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ButtonSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Button() {
        Button w = new Button(composite(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.containsEntry("id", w.hashCode())
                  .containsEntry("swt", "Button")
                  .containsEntry("style", w.getStyle());
    }

    @Test
    void should_serialize_filled_Button() {
        Button w = new Button(composite(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJson = assertThatJson(json).isObject();
        assertJson.containsEntry("id", w.hashCode())
                  .containsEntry("swt", "Button")
                  .containsEntry("style", w.getStyle());
        assertJson.node("alignment")
                  .satisfies(isEqualTo(w.getAlignment(), orAbsentIf0()));
        assertJson.node("grayed")
                  .satisfies(isEqualTo(w.getGrayed(), orAbsentIfFalse()));
        assertJson.node("selection")
                  .satisfies(isEqualTo(w.getSelection(), orAbsentIfFalse()));
        assertJson.node("text")
                  .satisfies(isEqualTo(w.getText(), ifNotNull()));
        assertJson.node("background")
                  .satisfies(isEqualTo(value(w).getBackground(), ifNotNull()));
        assertJson.node("dragDetect")
                  .satisfies(isEqualTo(w.getDragDetect(), orAbsentIfFalse()));
        assertJson.node("enabled")
                  .satisfies(isEqualTo(w.getEnabled(), orAbsentIfFalse()));
        assertJson.node("foreground")
                  .satisfies(isEqualTo(value(w).getForeground(), ifNotNull()));
        assertJson.node("orientation")
                  .satisfies(isEqualTo(w.getOrientation(), orAbsentIf0()));
        assertJson.node("textDirection")
                  .satisfies(isEqualTo(w.getTextDirection(), orAbsentIf0()));
        assertJson.node("toolTipText")
                  .satisfies(isEqualTo(w.getToolTipText(), ifNotNull()));
        assertJson.node("touchEnabled")
                  .satisfies(isEqualTo(w.getTouchEnabled(), orAbsentIfFalse()));
        assertJson.node("visible")
                  .satisfies(isEqualTo(w.getVisible(), orAbsentIfFalse()));
        assertJson.node("capture")
                  .satisfies(isEqualTo(value(w).getCapture(), orAbsentIfFalse()));
        assertJson.node("redraw")
                  .satisfies(isEqualTo(value(w).getRedraw(), orAbsentIfFalse()));
    }

    VButton value(Button w) {
        return ((DartButton) w.getImpl()).getValue();
    }
}
