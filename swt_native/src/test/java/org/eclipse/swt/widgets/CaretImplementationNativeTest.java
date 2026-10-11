package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/** What {@link DartCaret} tells Flutter so that the Canvas draws it. */
@Tag("native-unit")
class CaretImplementationNativeTest {

    private Shell shell;
    private DartDisplay display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
        display = (DartDisplay) shell.getDisplay().getImpl();
        when(display.getCaretBlinkTime()).thenReturn(530);
        doCallRealMethod().when(display).setCurrentCaret(any());
    }

    @AfterEach
    void tearDown() {
        display.currentCaret = null;
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("the value carries the bounds, the visibility, the focus and the blink rate of the caret")
    void theValueCarriesWhatTheCanvasDraws() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Caret caret = new Caret(canvas, SWT.NONE);
        caret.setBounds(30, 12, 2, 18);
        DartCaret impl = (DartCaret) caret.getImpl();
        VCaret value = impl.getValue();

        assertThat(value.getBlinkRate()).isEqualTo(530);
        assertThat(value.getVisible()).isTrue();
        assertThat(value.getFocused()).isFalse();

        value.clearDirty();
        impl.setFocus();
        assertThat(value.getFocused()).isTrue();
        assertThat(value.changedKeys()).contains(VCaret.FOCUSED);

        value.clearDirty();
        caret.setVisible(false);
        assertThat(value.getVisible()).isFalse();
        assertThat(value.changedKeys()).contains(VCaret.VISIBLE);

        value.clearDirty();
        impl.killFocus();
        assertThat(value.getFocused()).isFalse();
        assertThat(value.changedKeys()).contains(VCaret.FOCUSED);
    }

    @Test
    @DisplayName("only one caret is the focus caret, and a caret that takes the focus back while still shown stays shown")
    void oneFocusCaretAtATime() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        DartCaret first = (DartCaret) new Caret(canvas, SWT.NONE).getImpl();
        DartCaret second = (DartCaret) new Caret(canvas, SWT.NONE).getImpl();

        first.setFocus();
        assertThat(first._isShowing()).isTrue();
        second.setFocus();
        assertThat(first.isFocusCaret()).isFalse();
        assertThat(second.isFocusCaret()).isTrue();
        first.killFocus();
        assertThat(second.isFocusCaret()).as("killing the focus of a caret that does not have it").isTrue();
        assertThat(first._isShowing()).isTrue();

        first.setFocus();
        first.setFocus();
        assertThat(first.isFocusCaret()).isTrue();
        assertThat(first._isShowing()).isTrue();
    }

    @Test
    @DisplayName("the caret blinks while it is the focus caret, and moving it during the off phase shows it again")
    void theCaretBlinks() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Caret caret = new Caret(canvas, SWT.NONE);
        DartCaret impl = (DartCaret) caret.getImpl();
        impl.setFocus();
        assertThat(impl._isShowing()).isTrue();

        assertThat(impl.blinkCaret()).isTrue();
        assertThat(impl._isShowing()).isFalse();
        caret.setBounds(4, 5, 2, 10);
        assertThat(impl._isShowing()).isTrue();

        caret.setVisible(false);
        assertThat(impl.blinkCaret()).as("a hidden caret keeps blinking without showing").isTrue();
        assertThat(impl._isShowing()).isFalse();
    }

    @Test
    @DisplayName("a caret whose display does not blink stays shown")
    void aCaretThatDoesNotBlink() {
        when(display.getCaretBlinkTime()).thenReturn(0);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        DartCaret impl = (DartCaret) new Caret(canvas, SWT.NONE).getImpl();
        impl.setFocus();
        assertThat(impl.blinkCaret()).isTrue();
        assertThat(impl._isShowing()).isTrue();
        assertThat(impl.getValue().getBlinkRate()).isZero();
    }

    @Test
    @DisplayName("focusing the canvas makes its caret the focus caret, and losing the focus undoes it")
    void theCanvasFocusMovesTheCaret() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Caret caret = new Caret(canvas, SWT.NONE);
        VCaret value = ((DartCaret) caret.getImpl()).getValue();
        DartCanvas impl = (DartCanvas) canvas.getImpl();

        impl.sendFocusEvent(SWT.FocusIn);
        assertThat(value.getFocused()).isTrue();
        impl.sendFocusEvent(SWT.FocusOut);
        assertThat(value.getFocused()).isFalse();
    }
}
