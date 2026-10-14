package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

/**
 * {@code computeTrim} on a disposed control throws {@code ERROR_WIDGET_DISPOSED}, as SWT's does: it
 * starts with {@code checkWidget()}. The trim is computed from the size model, which a disposed control
 * still answers.
 */
@Tag("native-unit")
class ComputeTrimChecksWidgetTest {

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

    static Stream<Arguments> controls() {
        return Stream.of(
                control("Composite", s -> new Composite(s, SWT.NONE)),
                control("Text SINGLE", s -> new Text(s, SWT.SINGLE)),
                control("Text MULTI", s -> new Text(s, SWT.MULTI | SWT.V_SCROLL)),
                control("Spinner", s -> new Spinner(s, SWT.NONE)),
                control("Group", s -> new Group(s, SWT.NONE)),
                control("Shell", s -> new Shell(s.getDisplay())),
                control("TabFolder", s -> new TabFolder(s, SWT.TOP)),
                control("ToolBar", s -> new ToolBar(s, SWT.HORIZONTAL)),
                control("List", s -> new List(s, SWT.SINGLE)),
                control("Table", s -> new Table(s, SWT.NONE)),
                control("Tree", s -> new Tree(s, SWT.NONE)),
                control("CTabFolder", s -> new CTabFolder(s, SWT.NONE)),
                control("ViewForm", s -> new ViewForm(s, SWT.NONE)));
    }

    private static Arguments control(String name, Function<Shell, Scrollable> factory) {
        return Arguments.of(name, factory);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("controls")
    void computeTrimMeasuresALiveControlAndRefusesADisposedOne(String name, Function<Shell, Scrollable> factory) {
        Scrollable control = factory.apply(DartMocks.dartShell());

        Rectangle trim = control.computeTrim(0, 0, 100, 50);
        assertThat(trim.width).as(name + " width").isGreaterThanOrEqualTo(100);
        assertThat(trim.height).as(name + " height").isGreaterThanOrEqualTo(50);

        control.dispose();
        Throwable thrown = catchThrowable(() -> control.computeTrim(0, 0, 100, 50));
        assertThat(thrown).as(name + " measured after dispose").isInstanceOf(SWTException.class);
        assertThat(((SWTException) thrown).code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED);
    }
}
