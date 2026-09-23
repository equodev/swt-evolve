package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;

import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.*;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ButtonDisposedImageSerializeTest extends SerializeTestBase {

    @Test
    void serializing_a_button_with_a_disposed_image_omits_it_instead_of_throwing() {
        Button w = new Button(shell(), SWT.NONE);
        Image image = new Image(w.getDisplay(), getClass().getClassLoader().getResourceAsStream("collapseall.png"));
        w.setImage(image);
        image.dispose();

        String json = assertDoesNotThrow(() -> serialize(w));

        assertThatJson(json).node("image").isEqualTo(null);
    }
}
