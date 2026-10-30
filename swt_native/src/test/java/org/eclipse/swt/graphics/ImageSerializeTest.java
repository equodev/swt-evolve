package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.widgets.*;

class ImageSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_empty_Image() {
        Image w = new Image(device(), getClass().getClassLoader()
                                                .getResourceAsStream("collapseall.png"));
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
    }

    @Test
    void should_serialize_filled_Image() {
        Image w = new Image(device(), getClass().getClassLoader()
                                                .getResourceAsStream("collapseall.png"));
        setAll(w);
        WireAssert assertJ = assertThatWire(serialize(w));
        assertJ.isNotEmpty();
        assertJ.satisfies(node("imageData").equalsTo(w.getImageData(), orAbsentIfNull));
    }

    @Test
    void should_write_every_property_Image() throws Exception {
        assertWritesEveryProperty(unwritable(), () -> {
            Image w = new Image(device(), getClass().getClassLoader()
                                                    .getResourceAsStream("collapseall.png"));
            setAll(w);
            fillUnsettable(w);
            return w;
        }, () -> {
            Image w = new Image(device(), resourcePath("images/16x16.png"));
            setAll(w);
            fillUnsettable(w);
            return w;
        }, () -> {
            Image w = new Image(device(), getClass().getClassLoader()
                                                    .getResourceAsStream("images/16x16.svg"));
            setAll(w);
            fillUnsettable(w);
            return w;
        });
    }

    VImage value(Image w) {
        return ((DartImage) w.getImpl()).getValue();
    }
}
