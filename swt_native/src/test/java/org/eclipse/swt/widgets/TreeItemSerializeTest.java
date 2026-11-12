package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;

class TreeItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TreeItem() {
        TreeItem w = new TreeItem(tree(), SWT.NONE);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TreeItem");
    }

    @Test
    void should_serialize_filled_TreeItem() {
        TreeItem w = new TreeItem(tree(), SWT.NONE);
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TreeItem");
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("checked").equalsTo(w.getChecked(), orAbsentIfFalse));
        assertJ.satisfies(node("expanded").equalsTo(w.getExpanded(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("grayed").equalsTo(w.getGrayed(), orAbsentIfFalse));
        assertJ.satisfies(node("texts").sameSizeAs(value(w).getTexts()));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_TreeItem() throws Exception {
        assertWritesEveryProperty(style -> {
            TreeItem w = new TreeItem(new Tree(shell(), SWT.CHECK), style);
            setAll(w);
            fillUnsettable(w);
            return w;
        }, unwritable("style", "the client never reads it off a TreeItem"), SWT.NONE);
    }

    @Test
    void should_name_every_change_TreeItem() {
        TreeItem w = new TreeItem(tree(), SWT.NONE);
        assertNamesEveryChange(w);
    }

    VTreeItem value(TreeItem w) {
        return ((DartTreeItem) w.getImpl()).getValue();
    }
}
