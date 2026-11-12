package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class CTabItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CTabItem() {
        CTabItem w = new CTabItem(cTabFolder(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabItem");
    }

    @Test
    void should_serialize_filled_CTabItem() {
        CTabItem w = new CTabItem(cTabFolder(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabItem")
               .containsEntry("toolTipText", w.getToolTipText());
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("selectionForeground").equalsTo(w.getSelectionForeground(), orAbsentIfNull));
        assertJ.satisfies(node("showClose").equalsTo(w.getShowClose(), orAbsentIfFalse));
        assertJ.satisfies(node("showDirty").equalsTo(w.getShowDirty(), orAbsentIfFalse));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_CTabItem() throws Exception {
        assertWritesEveryProperty(style -> {
            CTabItem w = new CTabItem(new CTabFolder(shell(), SWT.NONE), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("style", "the client never reads it off a CTabItem"), SWT.NONE);
    }

    @Test
    void should_name_every_change_CTabItem() {
        CTabItem w = new CTabItem(cTabFolder(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VCTabItem value(CTabItem w) {
        return ((DartCTabItem) w.getImpl()).getValue();
    }
}
