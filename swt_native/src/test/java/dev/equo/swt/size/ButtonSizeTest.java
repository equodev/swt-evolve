package dev.equo.swt.size;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartButton;
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
class ButtonSizeTest extends SizeTestBase {

    @RegisterExtension
    static final WidgetSizeBridge flutter = new WidgetSizeBridge();

    @ParameterizedTest
    @MethodSource("allCases")
    void java_size_should_equals_flutter(int style, String image, String text, int size) {
        DartButton w = createButton(style, image, text, size);
        ConfigFlags config = ConfigFlags.use_swt_fonts(size != FromTheme);
        //
        Measure javaSize = ButtonSizes.computeSizes(w, SWT.DEFAULT, SWT.DEFAULT, true);;
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
            soft.assertThat(javaSize)
                .as("image size")
                .satisfiesAnyOf(similarImageSize(measure), isEmptyImage(image));
        });
    }

    @ParameterizedTest
    @MethodSource("basicCases")
    void flutter_size_should_be_minimal(int style, String image, String text, int size) {
        DartButton w = createButton(style, image, text, size);
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
    void flutter_size_should_equals_bounds(int style, String image, String text, int size) {
        DartButton w = createButton(style, image, text, size);
        w.setBounds(10, 15, 200, 150);
        //
        CompletableFuture<Measure> result = flutter.measure(w);;
        //
        Measure measure = assertCompletes(result);;
        assertThat(measure.widget).isEqualTo(new Point(200, 150));
    }

    static DartButton createButton(int style, String image, String text, int size) {
        DartButton w = new DartButton(shell(), style, null);
        if (!NoImg.equals(image))
            w.setImage(createImage(image));
        if (!NoTxt.equals(text))
            w.setText(text);
        if (size != FromTheme)
            w.setFont(createFont(size));
        return w;
    }

    static Stream<Style> getStyles() {
        return Stream.of(//
        s(SWT.ARROW, "ARROW"), //
        s(SWT.ARROW | SWT.FLAT, "ARROW|FLAT"), //
        s(SWT.ARROW | SWT.WRAP, "ARROW|WRAP"), //
        s(SWT.CHECK, "CHECK"), //
        s(SWT.CHECK | SWT.FLAT, "CHECK|FLAT"), //
        s(SWT.CHECK | SWT.WRAP, "CHECK|WRAP"), //
        s(SWT.PUSH, "PUSH"), //
        s(SWT.PUSH | SWT.FLAT, "PUSH|FLAT"), //
        s(SWT.PUSH | SWT.WRAP, "PUSH|WRAP"), //
        s(SWT.RADIO, "RADIO"), //
        s(SWT.RADIO | SWT.FLAT, "RADIO|FLAT"), //
        s(SWT.RADIO | SWT.WRAP, "RADIO|WRAP"), //
        s(SWT.TOGGLE, "TOGGLE"), //
        s(SWT.TOGGLE | SWT.FLAT, "TOGGLE|FLAT"), //
        s(SWT.TOGGLE | SWT.WRAP, "TOGGLE|WRAP"));
    }

    static Stream<Arguments> basicCases() {
        return buildCases(getStyles(), 2, 2, 1);
    }

    static Stream<Arguments> allCases() {
        return buildCases(getStyles(), All, All, All);
    }
}
