package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class SashSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Sash() {
        Sash w = new Sash(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Sash");
    }

    @Test
    void should_serialize_filled_Sash() {
        Sash w = new Sash(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Sash")
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("backgroundImage").equalsTo(w.getBackgroundImage(), orAbsentIfNull));
        assertJ.satisfies(node("bounds").equalsTo(value(w).getBounds(), orAbsentIfNull));
        assertJ.satisfies(node("cursor").equalsTo(w.getCursor(), orAbsentIfNull));
        assertJ.satisfies(node("dragDetect").equalsTo(w.getDragDetect(), orAbsentIfFalse));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("menu").equalsTo(w.getMenu(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_Sash() throws Exception {
        assertWritesEveryProperty(style -> {
            Sash w = new Sash(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("tabStop", "the client never reads it off a Sash", "font", "the client never reads it off a Sash", "hasOwnBackground", "the client never reads it off a Sash", "inheritsBackground", "the client never reads it off a Sash"), SWT.NONE, SWT.HORIZONTAL, SWT.VERTICAL, SWT.SMOOTH, SWT.BORDER, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_Sash() {
        Sash w = new Sash(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VSash value(Sash w) {
        return ((DartSash) w.getImpl()).getValue();
    }
}
