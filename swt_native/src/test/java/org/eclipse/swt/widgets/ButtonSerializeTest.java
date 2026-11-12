package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class ButtonSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Button() {
        Button w = new Button(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Button");
    }

    @Test
    void should_serialize_filled_Button() {
        Button w = new Button(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Button")
               .containsEntry("text", w.getText())
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alignment").equalsTo(w.getAlignment(), orAbsentIf0));
        assertJ.satisfies(node("grayed").equalsTo(w.getGrayed(), orAbsentIfFalse));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
        assertJ.satisfies(node("selection").equalsTo(w.getSelection(), orAbsentIfFalse));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(value(w).getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("cursor").equalsTo(w.getCursor(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_Button() throws Exception {
        assertWritesEveryProperty(style -> {
            Button w = new Button(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("hasOwnBackground", "the client never reads it off a Button", "inheritsBackground", "the client never reads it off a Button", "tabStop", "the client never reads it off a Button"), SWT.NONE, SWT.ARROW, SWT.CHECK, SWT.PUSH, SWT.RADIO, SWT.TOGGLE, SWT.FLAT, SWT.WRAP, SWT.UP, SWT.DOWN, SWT.LEFT, SWT.RIGHT, SWT.CENTER, SWT.BORDER, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_Button() {
        Button w = new Button(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VButton value(Button w) {
        return ((DartButton) w.getImpl()).getValue();
    }
}
