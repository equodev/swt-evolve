package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.AnimatedProgress;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
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

/**
 * {@code computeSize} on a disposed control throws {@code ERROR_WIDGET_DISPOSED}, as SWT's does: it
 * starts with {@code checkWidget()}. The sizing behind it is our own and does not always read a checked
 * getter (an ARROW button ignores its text, a separator has none), so every Dart-backed control with a
 * computeSize of its own is listed.
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

    /** One factory per Dart-backed control class that declares its own computeSize. */
    static Stream<Arguments> controls() {
        return Stream.of(
                control("Button ARROW", s -> new Button(s, SWT.ARROW)),
                control("Button PUSH", s -> new Button(s, SWT.PUSH)),
                control("Combo", s -> new Combo(s, SWT.DROP_DOWN)),
                control("Composite", s -> new Composite(s, SWT.NONE)),
                control("CoolBar", s -> new CoolBar(s, SWT.NONE)),
                control("DateTime", s -> new DateTime(s, SWT.DATE)),
                control("ExpandBar", s -> new ExpandBar(s, SWT.NONE)),
                control("Label", s -> new Label(s, SWT.NONE)),
                control("Label SEPARATOR", s -> new Label(s, SWT.SEPARATOR | SWT.HORIZONTAL)),
                control("Link", s -> new Link(s, SWT.NONE)),
                control("List", s -> new List(s, SWT.SINGLE)),
                control("ProgressBar", s -> new ProgressBar(s, SWT.HORIZONTAL)),
                control("Sash", s -> new Sash(s, SWT.VERTICAL)),
                control("Scale", s -> new Scale(s, SWT.HORIZONTAL)),
                control("Slider", s -> new Slider(s, SWT.HORIZONTAL)),
                control("Spinner", s -> new Spinner(s, SWT.NONE)),
                control("TabFolder", s -> new TabFolder(s, SWT.TOP)),
                control("Table", s -> new Table(s, SWT.NONE)),
                control("Text", s -> new Text(s, SWT.SINGLE)),
                control("ToolBar", s -> new ToolBar(s, SWT.HORIZONTAL)),
                control("Tree", s -> new Tree(s, SWT.NONE)),
                control("AnimatedProgress", s -> new AnimatedProgress(s, SWT.HORIZONTAL)),
                control("CCombo", s -> new CCombo(s, SWT.NONE)),
                control("CLabel", s -> new CLabel(s, SWT.NONE)),
                control("StyledText", s -> new StyledText(s, SWT.NONE)));
    }

    private static Arguments control(String name, Function<Shell, Control> factory) {
        return Arguments.of(name, factory);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("controls")
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
