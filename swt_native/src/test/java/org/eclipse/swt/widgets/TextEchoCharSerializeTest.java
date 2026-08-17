package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The "Show Password" toggle scenario: a plain single-line Text re-masked at runtime via
 * setEchoChar after text was typed. The Dart VText types echoCharacter as int and hiddenText
 * as List&lt;int&gt;, so both must serialize as numbers — a JSON string for either makes the
 * whole state push fail to deserialize and the re-mask silently never happens.
 * Whether hiddenText is populated by setEchoChar differs per backend (the GTK/Win32-mirroring
 * ones keep it empty), so it is asserted against the widget's own state, not a literal.
 */
class TextEchoCharSerializeTest extends SerializeTestBase {

    @Test
    void setEchoChar_on_a_populated_text_serializes_code_units() {
        Text w = new Text(swtShell(), SWT.SINGLE | SWT.BORDER);
        w.setText("secret");
        w.setEchoChar((char) 8226);

        String json = serialize(w);

        assertThatJson(json).isObject()
                .containsEntry("text", "secret")
                .containsEntry("echoCharacter", 8226);
        assertThat(json)
                .as("char[] state must never serialize as a JSON string")
                .doesNotContain("\"hiddenText\":\"");
        assertThatJson(json).isObject().satisfies(
                node("hiddenText").equalsTo(codeUnits(value(w).getHiddenText()), orAbsentIfNull));
    }

    private VText value(Text w) {
        return ((DartText) w.getImpl()).getValue();
    }

    @Test
    void clearing_the_echo_char_keeps_the_real_text() {
        Text w = new Text(swtShell(), SWT.SINGLE | SWT.BORDER);
        w.setEchoChar((char) 8226);
        w.setText("secret");
        w.setEchoChar((char) 0);

        String json = serialize(w);

        assertThatJson(json).isObject().containsEntry("text", "secret");
    }
}
