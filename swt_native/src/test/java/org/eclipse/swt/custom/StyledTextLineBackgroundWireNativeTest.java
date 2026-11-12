package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A LineBackgroundListener that colours one line, as an editor's current-line highlight does, sends
 * that line's attributes and none for the lines it leaves alone.
 */
@Tag("native-unit")
public class StyledTextLineBackgroundWireNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void onlyTheLineTheListenerColoursIsSent() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("one\ntwo\nthree\nfour");
        Color highlight = new Color(display, 230, 230, 255);
        text.addLineBackgroundListener(e -> {
            if (e.lineOffset == 0) e.lineBackground = highlight;
        });

        StyledTextRenderer renderer = ((DartStyledText) text.getImpl()).renderer;
        VStyledTextRenderer.VLineInfo[] lines;
        try (Mocks.Delivery delivery = Mocks.deliverEvents(display)) {
            lines = StyledTextHelper.wireLines((DartStyledTextRenderer) renderer.getImpl());
        }

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isNotNull();
        assertThat(lines[1]).isNull();
        assertThat(lines[2]).isNull();
        assertThat(lines[3]).isNull();
    }
}
