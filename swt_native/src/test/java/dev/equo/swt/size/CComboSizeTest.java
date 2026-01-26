package dev.equo.swt.size;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartCCombo;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;

@DisabledOnOs(OS.LINUX)
class CComboSizeTest extends SizeTestBase {

    @RegisterExtension
    static final WidgetSizeBridge flutter = new WidgetSizeBridge();

    @ParameterizedTest
    @MethodSource("allCases")
    void java_size_should_equals_flutter(int style, String text, int size) {
        DartCCombo w = createCCombo(style, text, size);
        ConfigFlags config = ConfigFlags.use_swt_fonts(size != FromTheme);
        //
        Measure javaSize = CComboSizes.computeSizes(w, SWT.DEFAULT, SWT.DEFAULT, true);;
        //
        CompletableFuture<Measure> result = flutter.measure(w, config);;
        Measure measure = assertCompletes(result);
        assertSoftly(soft -> {
            soft.assertThat(javaSize)
                .as("widget size")
                .satisfies(similarSize(measure));
            soft.assertThat(javaSize)
                .as("text size")
                .satisfiesAnyOf(similarTextSize(measure), isEmptyText(text));
            soft.assertThat(javaSize.textStyle)
                .as("text style")
                .isEqualTo(measure.textStyle);
        });
    }

    @ParameterizedTest
    @MethodSource("basicCases")
    void flutter_size_should_be_minimal(int style, String text, int size) {
        DartCCombo w = createCCombo(style, text, size);
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
    void flutter_size_should_equals_bounds(int style, String text, int size) {
        DartCCombo w = createCCombo(style, text, size);
        w.setBounds(10, 15, 200, 150);
        //
        CompletableFuture<Measure> result = flutter.measure(w);;
        //
        Measure measure = assertCompletes(result);;
        assertThat(measure.widget).isEqualTo(new Point(200, 150));
    }

    static DartCCombo createCCombo(int style, String text, int size) {
        DartCCombo w = new DartCCombo(shell(), style, null);
        if (!NoTxt.equals(text))
            w.setText(text);
        if (size != FromTheme)
            w.setFont(createFont(size));
        return w;
    }

    static Stream<Style> getStyles() {
        return Stream.of(//
        s(SWT.BORDER, "BORDER"), //
        s(SWT.READ_ONLY, "READ_ONLY"), //
        s(SWT.FLAT, "FLAT"), //
        s(SWT.LEAD, "LEAD"), //
        s(SWT.LEFT, "LEFT"), //
        s(SWT.CENTER, "CENTER"), //
        s(SWT.TRAIL, "TRAIL"), //
        s(SWT.RIGHT, "RIGHT"));
    }

    static Stream<Arguments> basicCases() {
        return buildCases(getStyles(), 2, 1);
    }

    static Stream<Arguments> allCases() {
        return buildCases(getStyles(), All, All);
    }
}
