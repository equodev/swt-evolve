package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class MenuSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Menu() {
        Menu w = new Menu(control());
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Menu");
    }

    @Test
    void should_serialize_filled_Menu() {
        Menu w = new Menu(control());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Menu")
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("defaultItem").equalsTo(w.getDefaultItem(), orAbsentIfNull));
        assertJ.satisfies(node("enabled").equalsTo(w.getEnabled(), orAbsentIfFalse));
        assertJ.satisfies(node("location").equalsTo(value(w).getLocation(), orAbsentIfNull));
        assertJ.satisfies(node("orientation").equalsTo(w.getOrientation(), orAbsentIf0));
        assertJ.satisfies(node("parentMenu").equalsTo(w.getParentMenu(), orAbsentIfNull));
        assertJ.satisfies(node("visible").equalsTo(w.getVisible(), orAbsentIfFalse));
    }

    VMenu value(Menu w) {
        return ((DartMenu) w.getImpl()).getValue();
    }
}
