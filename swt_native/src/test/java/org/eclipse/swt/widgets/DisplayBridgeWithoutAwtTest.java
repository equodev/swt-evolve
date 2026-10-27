package org.eclipse.swt.widgets;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** Every Display loads DisplayBridge, so it cannot name an AWT type: a runtime may lack java.desktop. */
class DisplayBridgeWithoutAwtTest {

    @Test
    void displayBridgeNamesNoAwtType() throws Exception {
        try (InputStream in = DisplayBridge.class.getResourceAsStream("DisplayBridge.class")) {
            assertThat(in).isNotNull();
            String constants = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertThat(constants).doesNotContain("java/awt/");
        }
    }
}
