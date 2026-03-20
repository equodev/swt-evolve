package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class CursorSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Cursor() {
        Cursor w = new Cursor(device(), SWT.NONE);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Cursor() {
        Cursor w = new Cursor(device(), SWT.NONE);
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    VCursor value(Cursor w) {
        return ((DartCursor) w.getImpl()).getValue();
    }
}
