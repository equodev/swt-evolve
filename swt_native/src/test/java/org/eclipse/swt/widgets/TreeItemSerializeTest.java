package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class TreeItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_TreeItem() {
        TreeItem w = new TreeItem(tree(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TreeItem");
    }

    @Test
    void should_serialize_filled_TreeItem() {
        TreeItem w = new TreeItem(tree(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "TreeItem")
               .hasEntrySatisfying("texts", p -> assertThatJson(p).isArray()
                                                                  .hasSameSizeAs(value(w).getTexts()))
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("checked").equalsTo(w.getChecked(), orAbsentIfFalse));
        assertJ.satisfies(node("expanded").equalsTo(w.getExpanded(), orAbsentIfFalse));
        assertJ.satisfies(node("font").equalsTo(w.getFont(), orAbsentIfNull));
        assertJ.satisfies(node("foreground").equalsTo(w.getForeground(), orAbsentIfNull));
        assertJ.satisfies(node("grayed").equalsTo(w.getGrayed(), orAbsentIfFalse));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    VTreeItem value(TreeItem w) {
        return ((DartTreeItem) w.getImpl()).getValue();
    }
}
