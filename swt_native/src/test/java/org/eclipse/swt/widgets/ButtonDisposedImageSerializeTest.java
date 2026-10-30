package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;

import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.*;

import dev.equo.swt.harness.WireValues;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ButtonDisposedImageSerializeTest extends SerializeTestBase {

    @Test
    void serializing_a_button_with_a_disposed_image_omits_it_instead_of_throwing() {
        Button w = new Button(shell(), SWT.NONE);
        Image image = new Image(w.getDisplay(), getClass().getClassLoader().getResourceAsStream("collapseall.png"));
        w.setImage(image);
        image.dispose();

        Map<String, Object> state = assertDoesNotThrow(() -> serialize(w));

        org.assertj.core.api.Assertions.assertThat(WireValues.find(state, m -> m.get("image") instanceof Map)).isEmpty();
    }
}
