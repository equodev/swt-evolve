package org.eclipse.swt.widgets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The web surface's inbound window payloads, decoded from the bytes the client sends: a refusal the
 * handler cannot read leaves the shell drawn in no window and no page.
 */
@Tag("native-unit")
class WebWindowPayloadNativeTest {

    @Test
    @DisplayName("the shell id survives a real decode of what the client sends")
    void windowOpenFailedDecodes() throws Exception {
        long shellId = 195801026L;
        assertThat(WebDisplayBridge.shellIdOf(wire(shellId)))
                .as("a payload that cannot be decoded is a window refusal nobody hears")
                .isEqualTo(shellId);
    }

    @Test
    @DisplayName("a payload with no shell id names no shell, rather than naming shell 0")
    void missingShellIdIsNotZeroShell() throws Exception {
        assertThat(WebDisplayBridge.shellIdOf(ByteBuffer.wrap(new byte[0]))).isZero();
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

    /** {@code v} as the client writes an id: a zigzag varint. */
    private static ByteBuffer wire(long v) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long z = (v << 1) ^ (v >> 63);
        while ((z & ~0x7FL) != 0) {
            out.write((int) ((z & 0x7F) | 0x80));
            z >>>= 7;
        }
        out.write((int) z);
        return ByteBuffer.wrap(out.toByteArray());
    }
}
