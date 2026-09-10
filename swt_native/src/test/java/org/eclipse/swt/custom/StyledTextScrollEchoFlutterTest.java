package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The client recognises Java's echo of its own scroll by value and drops it. That only works while
 * the value survives the round trip.
 */
@Tag("flutter-it")
class StyledTextScrollEchoFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private StyledText styledText;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        styledText = new StyledText(shell, SWT.V_SCROLL | SWT.WRAP);
        StringBuilder doc = new StringBuilder();
        for (int i = 0; i < 400; i++) doc.append("line ").append(i).append(" of the document\n");
        styledText.setText(doc.toString());
        styledText.setSize(400, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private VStyledText wire() {
        return (VStyledText) ((DartStyledText) styledText.getImpl()).getValue();
    }

    /** The real inbound route: the render side pushes a StateUpdate payload. */
    private void clientScrolledTo(int topPixel) {
        bridge.comm.fireContaining("StateUpdate",
                ("{\"topPixel\":" + topPixel + "}").getBytes(StandardCharsets.UTF_8));
        while (display.readAndDispatch()) { }
    }

    @Test
    @DisplayName("the offset the client reported is the offset the echo carries")
    void echoCarriesWhatTheClientReported() {
        clientScrolledTo(137);
        assertThat(wire().getTopPixel())
                .as("an echo carrying anything else cannot be recognised by value")
                .isEqualTo(137);
    }

    @Test
    @DisplayName("the offset survives an invalidation of the cached scroll offset")
    void offsetSurvivesInvalidation() {
        clientScrolledTo(137);
        // What resetCache/handleResize do on a wrapped document; all three sites are gated on
        // !isFixedLineHeight().
        ((DartStyledText) styledText.getImpl()).verticalScrollOffset = -1;
        assertThat(wire().getTopPixel())
                .as("recomputed from topIndex, which the handler set to topPixel/lineHeight")
                .isEqualTo(137);
    }
}
