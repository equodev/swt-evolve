package org.eclipse.swt.widgets;

import dev.equo.swt.Serializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The web surface's inbound window payloads, decoded the way the comm really decodes them.
 *
 * <p>This exists because a test double that hands a handler an object it built itself cannot see the
 * step that actually failed: a payload class of our own has no generated reader, so the frame was
 * rejected at deserialization and the handler never ran at all. A browser that refuses to open a
 * window then went unheard, and the shell it was for was drawn in no window and no page — the one
 * outcome the fallback exists to prevent. So the assertion here is on a real decode of the real
 * bytes the client sends.
 */
@Tag("native-unit")
class WebWindowPayloadNativeTest {

    private final Serializer serializer = new Serializer();

    @Test
    @DisplayName("the shell id survives a real decode of what the client sends")
    void windowOpenFailedDecodes() throws Exception {
        long shellId = 195801026L;
        Object decoded = decode("{\"shellId\":" + shellId + "}");

        assertThat(WebDisplayBridge.shellIdOf(decoded))
                .as("a payload that cannot be decoded is a window refusal nobody hears")
                .isEqualTo(shellId);
    }

    @Test
    @DisplayName("a payload with no shell id names no shell, rather than naming shell 0")
    void missingShellIdIsNotZeroShell() throws Exception {
        assertThat(WebDisplayBridge.shellIdOf(decode("{}"))).isZero();
        assertThat(WebDisplayBridge.shellIdOf(decode("{\"other\":1}"))).isZero();
        assertThat(WebDisplayBridge.shellIdOf(null)).isZero();
        assertThat(WebDisplayBridge.shellIdOf("not a payload")).isZero();
    }

    @Test
    @DisplayName("a detached window is opened at the shell, carrying the theme")
    void shellWindowUrlNamesTheShellAndTheTheme() {
        String url = WebDisplayBridge.shellWindowUrl("http://localhost:8080", 4242L, "dark", false);

        assertThat(url)
                .as("rooted at the shell, or the window renders the whole Display a second time")
                .contains("widgetName=Shell")
                .contains("widgetId=4242");
        assertThat(url)
                .as("the client paints before anything reaches it over the socket; with no theme in "
                        + "the URL it opens light and then corrects itself, which against a dark "
                        + "application reads as a broken window rather than as a flash")
                .contains("theme=dark");
        assertThat(url).doesNotContain("enableTestSemantics");
    }

    @Test
    @DisplayName("the semantics toggle reaches a detached window too, so E2E can find it")
    void shellWindowUrlCarriesTheSemanticsToggle() {
        assertThat(WebDisplayBridge.shellWindowUrl("http://localhost:8080", 7L, "light", true))
                .contains("enableTestSemantics=true");
    }

    private Object decode(String json) throws Exception {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return serializer.from(Object.class, bytes, 0, bytes.length);
    }
}
