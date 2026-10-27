package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.eclipse.swt.custom.StyledTextFixedLineHeightWireNativeTest.assertSentVaryingAfter;

/** A line spacing provider and a line's vertical indent vary the height, as the other line setters do. */
@Tag("native-unit")
public class StyledTextLineProvidersWireNativeTest {

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

    private StyledText text() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("one\ntwo\nthree");
        return text;
    }

    @Test
    public void a_line_spacing_provider_varies_the_height() {
        StyledText text = text();
        assertSentVaryingAfter(text, () -> text.setLineSpacingProvider(line -> line == 1 ? 4 : null));
    }

    @Test
    public void a_line_vertical_indent_varies_the_height() {
        StyledText text = text();
        assertSentVaryingAfter(text, () -> text.setLineVerticalIndent(1, 10));
    }
}
