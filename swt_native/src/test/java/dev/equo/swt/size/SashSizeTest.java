package dev.equo.swt.size;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartSash;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;

@DisabledOnOs(OS.LINUX)
@Tag("metal")
class SashSizeTest extends SizeTestBase {

    @RegisterExtension
    static final WidgetSizeBridge flutter = new WidgetSizeBridge();

    @ParameterizedTest
    @MethodSource("allCases")
    void java_size_should_equals_flutter(int style) {
        DartSash w = createSash(style);
        ConfigFlags config = ConfigFlags.use_swt_fonts(false);
        //
        Measure javaSize = SashSizes.computeSizes(w, SWT.DEFAULT, SWT.DEFAULT, true);;
        //
        CompletableFuture<Measure> result = flutter.measure(w, config);;
        Measure measure = assertCompletes(result);
        assertSoftly(soft -> {
            soft.assertThat(javaSize)
                .as("widget size")
                .satisfies(similarSize(measure));
        });
    }

    @ParameterizedTest
    @MethodSource("basicCases")
    void flutter_size_should_be_minimal(int style) {
        DartSash w = createSash(style);
        //
        CompletableFuture<Measure> result = flutter.measure(w);;
        //
        Measure measure = assertCompletes(result);;
        assertThat(measure.widget.x).as("width >= window width")
                                    .isLessThan(flutter.windowSize.x);
        assertThat(measure.widget.y).as("height >= window height")
                                    .isLessThan(flutter.windowSize.y);
    }

    @ParameterizedTest
    @MethodSource("basicCases")
    void flutter_size_should_equals_bounds(int style) {
        DartSash w = createSash(style);
        w.setBounds(10, 15, 200, 150);
        //
        CompletableFuture<Measure> result = flutter.measure(w);;
        //
        Measure measure = assertCompletes(result);;
        assertThat(measure.widget).isEqualTo(new Point(200, 150));
    }

    static DartSash createSash(int style) {
        DartSash w = new DartSash(shell(), style, null);
        return w;
    }

    static Stream<Style> getStyles() {
        return Stream.of(//
        s(SWT.HORIZONTAL, "HORIZONTAL"), //
        s(SWT.HORIZONTAL | SWT.SMOOTH, "HORIZONTAL|SMOOTH"), //
        s(SWT.VERTICAL, "VERTICAL"), //
        s(SWT.VERTICAL | SWT.SMOOTH, "VERTICAL|SMOOTH"));
    }

    static Stream<Arguments> basicCases() {
        return buildCases(getStyles());
    }

    static Stream<Arguments> allCases() {
        return buildCases(getStyles());
    }
}
