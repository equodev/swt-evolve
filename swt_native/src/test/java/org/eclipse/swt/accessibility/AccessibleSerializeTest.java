package org.eclipse.swt.accessibility;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

@Disabled
class AccessibleSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Accessible() {
        Accessible w = new Accessible(accessible());
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Accessible() {
        Accessible w = new Accessible(accessible());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
        assertJ.satisfies(node("control").equalsTo(w.getControl(), orAbsentIfNull));
        assertJ.satisfies(node("focus").equalsTo(value(w).getFocus(), orAbsentIf0));
    }

    VAccessible value(Accessible w) {
        return ((DartAccessible) w.getImpl()).getValue();
    }
}
