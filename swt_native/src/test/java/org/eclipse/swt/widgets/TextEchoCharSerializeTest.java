package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The "Show Password" toggle scenario: a plain single-line Text re-masked at runtime via
 * setEchoChar after text was typed. echoCharacter must serialize as a number - a string
 * makes the whole state push fail to deserialize and the re-mask silently never happens.
 * The masked text itself does not travel: the far side already has the real text and the
 * character to mask it with, and derives the masked form where it draws it.
 */
class TextEchoCharSerializeTest extends SerializeTestBase {

    @Test
    void setEchoChar_on_a_populated_text_serializes_code_units() {
        Text w = new Text(shell(), SWT.SINGLE | SWT.BORDER);
        w.setText("secret");
        w.setEchoChar((char) 8226);

        Map<String, Object> wire = serialize(w);

        assertThatWire(wire).isObject()
                .containsEntry("text", "secret")
                .containsEntry("echoCharacter", 8226);
        assertThat(WireValues.find(wire, m -> m.get("hiddenText") instanceof String))
                .as("char[] state must never serialize as a string")
                .isEmpty();
        assertThat(WireValues.mentions(wire, "hiddenText"))
                .as("the masked form is derived where it is drawn, so sending it is redundant")
                .isFalse();
    }

    @Test
    void clearing_the_echo_char_keeps_the_real_text() {
        Text w = new Text(shell(), SWT.SINGLE | SWT.BORDER);
        w.setEchoChar((char) 8226);
        w.setText("secret");
        w.setEchoChar((char) 0);

        Map<String, Object> wire = serialize(w);

        assertThatWire(wire).isObject().containsEntry("text", "secret");
    }
}
