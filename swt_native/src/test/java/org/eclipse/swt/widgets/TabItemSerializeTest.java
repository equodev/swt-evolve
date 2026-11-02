package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class TabItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TabItem() {
        TabItem w = new TabItem(tabFolder(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TabItem");
    }

    @Test
    void should_serialize_filled_TabItem() {
        TabItem w = new TabItem(tabFolder(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TabItem")
               .containsEntry("toolTipText", w.getToolTipText())
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_TabItem() throws Exception {
        assertWritesEveryProperty(style -> {
            TabItem w = new TabItem(new TabFolder(shell(), SWT.NONE), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable(), SWT.NONE);
    }

    @Test
    void should_name_every_change_TabItem() {
        TabItem w = new TabItem(tabFolder(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VTabItem value(TabItem w) {
        return ((DartTabItem) w.getImpl()).getValue();
    }
}
