package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class CoolItemSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_CoolItem() {
        CoolItem w = new CoolItem(coolBar(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CoolItem");
    }

    @Test
    void should_serialize_filled_CoolItem() {
        CoolItem w = new CoolItem(coolBar(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CoolItem")
               .containsEntry("style", w.getStyle());
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("minimumSize").equalsTo(w.getMinimumSize(), orAbsentIfNull));
        assertJ.satisfies(node("preferredSize").equalsTo(w.getPreferredSize(), orAbsentIfNull));
        assertJ.satisfies(node("image").equalsTo(w.getImage(), orAbsentIfNull));
    }

    VCoolItem value(CoolItem w) {
        return ((DartCoolItem) w.getImpl()).getValue();
    }
}
