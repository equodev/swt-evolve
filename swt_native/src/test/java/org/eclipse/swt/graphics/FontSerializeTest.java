package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class FontSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Font() {
        Font w = new Font(device(), fontData());
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Font() {
        Font w = new Font(device(), fontData());
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    VFont value(Font w) {
        return ((DartFont) w.getImpl()).getValue();
    }
}
