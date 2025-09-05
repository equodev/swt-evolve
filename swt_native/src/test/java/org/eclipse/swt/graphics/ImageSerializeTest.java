package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class ImageSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Image() {
        Image w = new Image(device(), getClass().getClassLoader()
                                                .getResourceAsStream("collapseall.png"));
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Image() {
        Image w = new Image(device(), getClass().getClassLoader()
                                                .getResourceAsStream("collapseall.png"));
        setAll(w);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.isNotEmpty();
        assertJ.satisfies(node("background").equalsTo(w.getBackground(), orAbsentIfNull));
        assertJ.satisfies(node("imageData").equalsTo(w.getImageData(), orAbsentIfNull));
        assertJ.satisfies(node("imageDataAtCurrentZoom").equalsTo(w.getImageDataAtCurrentZoom(), orAbsentIfNull));
    }

    VImage value(Image w) {
        return ((DartImage) w.getImpl()).getValue();
    }
}
