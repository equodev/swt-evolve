package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What {@link DartStyledText} does when it is driven directly rather than through {@link StyledText}. */
@Tag("native-unit")
class StyledTextImplementationNativeTest {

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
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartStyledText impl = new DartStyledText(DartMocks.dartShell(display), SWT.MULTI, null);

        StyledText api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("a renderer built without its API object creates one, once, bound to itself")
    void rendererWithoutApiCreatesOne() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        DartStyledTextRenderer impl = new DartStyledTextRenderer(display, text, null);

        StyledTextRenderer api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("a font whose letters are all as wide as each other makes the renderer fixed pitch")
    void aFontWithOneWidthForEveryLetterIsFixedPitch() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        DartStyledTextRenderer renderer = (DartStyledTextRenderer) ((DartStyledText) text.getImpl()).renderer.getImpl();
        org.eclipse.swt.graphics.Font mono = new org.eclipse.swt.graphics.Font(display, "Courier New", 12, SWT.NORMAL);
        org.eclipse.swt.graphics.Font proportional = new org.eclipse.swt.graphics.Font(display, "Arial", 12, SWT.NORMAL);

        text.setFont(mono);
        boolean monoPitch = renderer.fixedPitch;
        text.setFont(proportional);
        boolean proportionalPitch = renderer.fixedPitch;

        assertThat(monoPitch).isTrue();
        assertThat(proportionalPitch).isFalse();
        assertThat(StyledTextHelper.isFixedPitch(null)).isFalse();
        mono.dispose();
        assertThat(StyledTextHelper.isFixedPitch(mono)).isFalse();
    }

    @Test
    @DisplayName("a line laid out after the editor's font is disposed takes the system font")
    void aDisposedEditorFontFallsBackToTheSystemFont() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("abc");
        DartStyledTextRenderer renderer = (DartStyledTextRenderer) ((DartStyledText) text.getImpl()).renderer.getImpl();
        org.eclipse.swt.graphics.Font font = new org.eclipse.swt.graphics.Font(display, "Arial", 14, SWT.NORMAL);
        text.setFont(font);
        font.dispose();

        org.eclipse.swt.graphics.TextLayout layout = renderer.getTextLayout(0);

        assertThat(layout.getFont()).isSameAs(display.getSystemFont());
        renderer.disposeTextLayout(layout);
    }
}
