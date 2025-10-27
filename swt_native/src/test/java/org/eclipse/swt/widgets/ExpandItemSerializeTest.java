package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ExpandItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_ExpandItem() {
        ExpandItem w = new ExpandItem(expandBar(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ExpandItem");
    }

    @Test
    void should_serialize_filled_ExpandItem() {
        ExpandItem w = new ExpandItem(expandBar(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "ExpandItem")
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("expanded").equalsTo(w.getExpanded(), orAbsentIfFalse));
        assertJ.satisfies(node("height").equalsTo(w.getHeight(), orAbsentIf0));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    VExpandItem value(ExpandItem w) {
        return ((DartExpandItem) w.getImpl()).getValue();
    }
}
