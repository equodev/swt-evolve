package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class CComboSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CCombo() {
        CCombo w = new CCombo(shell(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CCombo");
    }

    @Test
    void should_serialize_filled_CCombo() {
        CCombo w = new CCombo(shell(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CCombo")
               .containsEntry("text", w.getText())
               .containsEntry("enabled", w.getEnabled())
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("visible", w.getVisible())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("alignment").equalsTo(w.getAlignment(), orAbsentIf0));
        assertJ.satisfies(node("editable").equalsTo(w.getEditable(), orAbsentIfFalse));
        assertJ.satisfies(node("items").sameSizeAs(w.getItems()));
        assertJ.satisfies(node("listVisible").equalsTo(w.getListVisible(), orAbsentIfFalse));
        assertJ.satisfies(node("selection").equalsTo(value(w).getSelection(), orAbsentIfNull));
        assertJ.satisfies(node("textLimit").equalsTo(w.getTextLimit(), orAbsentIf0));
        assertJ.satisfies(node("backgroundMode").equalsTo(w.getBackgroundMode(), orAbsentIf0));
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
    void should_write_every_property_CCombo() throws Exception {
        assertWritesEveryProperty(style -> {
            CCombo w = new CCombo(shell(), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("horizontalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars", "verticalBar", "checkStyle drops H_SCROLL and V_SCROLL, so the widget never has scroll bars", "menu", "setMenu hands the menu to the inner Text and never stores it on the CCombo", "children", "getChildren answers none, as upstream: the field, list and arrow are its own", "hasOwnBackground", "the client never reads it off a CCombo"), SWT.NONE, SWT.BORDER, SWT.READ_ONLY, SWT.FLAT, SWT.LEAD, SWT.LEFT, SWT.CENTER, SWT.TRAIL, SWT.RIGHT, SWT.NO_BACKGROUND, SWT.NO_FOCUS, SWT.NO_MERGE_PAINTS, SWT.NO_REDRAW_RESIZE, SWT.NO_RADIO_GROUP, SWT.EMBEDDED, SWT.DOUBLE_BUFFERED, SWT.H_SCROLL, SWT.V_SCROLL, SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, SWT.FLIP_TEXT_DIRECTION);
    }

    @Test
    void should_name_every_change_CCombo() {
        CCombo w = new CCombo(shell(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VCCombo value(CCombo w) {
        return ((DartCCombo) w.getImpl()).getValue();
    }
}
