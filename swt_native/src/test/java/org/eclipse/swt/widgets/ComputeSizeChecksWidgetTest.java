package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code computeSize} on a disposed control throws {@code ERROR_WIDGET_DISPOSED}, as SWT's does: it
 * starts with {@code checkWidget()}. The sizing behind it is our own, and a widget whose sizing never
 * reads a checked getter (an ARROW button ignores its text, a separator has none) measured a disposed
 * control as if it were alive.
 */
@Tag("native-unit")
class ComputeSizeChecksWidgetTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("computeSize refuses a disposed control whatever its sizing reads")
    void computeSizeRefusesADisposedControl() {
        Shell shell = DartMocks.dartShell();
        List<Function<Shell, Control>> factories = List.of(
                s -> new Button(s, SWT.ARROW),
                s -> new Button(s, SWT.PUSH),
                s -> new Label(s, SWT.SEPARATOR | SWT.HORIZONTAL),
                s -> new ProgressBar(s, SWT.HORIZONTAL),
                s -> new Sash(s, SWT.VERTICAL),
                s -> new Scale(s, SWT.HORIZONTAL),
                s -> new Slider(s, SWT.HORIZONTAL),
                s -> new Composite(s, SWT.NONE));
        List<String> measured = new ArrayList<>();
        for (Function<Shell, Control> factory : factories) {
            Control control = factory.apply(shell);
            String name = control.getClass().getSimpleName() + "/" + control.getStyle();
            control.dispose();
            try {
                control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
                measured.add(name + ": no exception");
            } catch (SWTException e) {
                if (e.code != SWT.ERROR_WIDGET_DISPOSED)
                    measured.add(name + ": code " + e.code);
            }
        }
        assertThat(measured).isEmpty();
    }
}
