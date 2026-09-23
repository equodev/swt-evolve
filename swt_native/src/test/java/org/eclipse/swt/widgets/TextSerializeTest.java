package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class TextSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Text() {
        Text w = new Text(shell(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Text");
    }

    @Test
    void should_serialize_filled_Text() {
        Text w = new Text(shell(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Text")
               .containsEntry("echoCharacter", (int) w.getEchoChar())
               .containsEntry("message", json(w.getMessage()))
               .containsEntry("text", json(w.getText()))
               .containsEntry("toolTipText", json(w.getToolTipText()))
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("editable").equalsTo(w.getEditable(), orAbsentIfFalse));
        assertJ.satisfies(node("selection").equalsTo(value(w).getSelection(), orAbsentIfNull));
        assertJ.satisfies(node("textLimit").equalsTo(w.getTextLimit(), orAbsentIf0));
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(value(w).getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("cursor").equalsTo(w.getCursor(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
    }

    @Test
    void should_name_every_change_Text() {
        Text w = new Text(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VText value(Text w) {
        return ((DartText) w.getImpl()).getValue();
    }
}
