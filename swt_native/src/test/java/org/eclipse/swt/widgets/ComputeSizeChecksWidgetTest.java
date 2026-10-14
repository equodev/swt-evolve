package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

/**
 * {@code computeSize} on a disposed control throws {@code ERROR_WIDGET_DISPOSED}, as SWT's does: it
 * starts with {@code checkWidget()}. The sizing behind it is our own and does not always read a checked
 * getter (an ARROW button ignores its text, a separator has none), so every Dart-backed control with a
 * computeSize of its own is tried: the list is {@link DartControlFactories#withOwnComputeSize()}.
 */
@Tag("native-unit")
class ComputeSizeChecksWidgetTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        // A shell being disposed asks the display for the shells it parents; the mock display answers null.
        when(DartMocks.dartDisplay().getShells()).thenReturn(new Shell[0]);
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        when(DartMocks.dartDisplay().getShells()).thenReturn(null);
        FlutterBridge.set(null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("org.eclipse.swt.widgets.DartControlFactories#withOwnComputeSize")
    void computeSizeMeasuresALiveControlAndRefusesADisposedOne(String name, Function<Shell, Control> factory) {
        Control control = factory.apply(DartMocks.dartShell());

        Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        assertThat(size.x).as(name + " width").isNotNegative();
        assertThat(size.y).as(name + " height").isNotNegative();

        control.dispose();
        Throwable thrown = catchThrowable(() -> control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
        assertThat(thrown).as(name + " measured after dispose").isInstanceOf(SWTException.class);
        assertThat(((SWTException) thrown).code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED);
    }
}
