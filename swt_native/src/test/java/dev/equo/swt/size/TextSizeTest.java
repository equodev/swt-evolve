package dev.equo.swt.size;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartText;
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
class TextSizeTest extends SizeTestBase {

    @RegisterExtension
    static final WidgetSizeBridge flutter = new WidgetSizeBridge();

    @ParameterizedTest
    @MethodSource("allCases")
    void java_size_should_equals_flutter(int style, String text, int size) {
        DartText w = createText(style, text, size);
        ConfigFlags config = ConfigFlags.use_swt_fonts(size != FromTheme);
        //
        Measure javaSize = TextSizes.computeSizes(w, SWT.DEFAULT, SWT.DEFAULT, true);;
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
        DartText w = createText(style, text, size);
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
        DartText w = createText(style, text, size);
        w.setBounds(10, 15, 200, 150);
        //
        CompletableFuture<Measure> result = flutter.measure(w);;
        //
        Measure measure = assertCompletes(result);;
        assertThat(measure.widget).isEqualTo(new Point(200, 150));
    }

    static DartText createText(int style, String text, int size) {
        DartText w = new DartText(shell(), style, null);
        if (!NoTxt.equals(text))
            w.setText(text);
        if (size != FromTheme)
            w.setFont(createFont(size));
        return w;
    }

    static Stream<Style> getStyles() {
        return Stream.of(//
        s(SWT.LEFT, "LEFT"), //
        s(SWT.LEFT | SWT.PASSWORD, "LEFT|PASSWORD"), //
        s(SWT.LEFT | SWT.SEARCH, "LEFT|SEARCH"), //
        s(SWT.LEFT | SWT.READ_ONLY, "LEFT|READ_ONLY"), //
        s(SWT.LEFT | SWT.WRAP, "LEFT|WRAP"), //
        s(SWT.CENTER, "CENTER"), //
        s(SWT.CENTER | SWT.PASSWORD, "CENTER|PASSWORD"), //
        s(SWT.CENTER | SWT.SEARCH, "CENTER|SEARCH"), //
        s(SWT.CENTER | SWT.READ_ONLY, "CENTER|READ_ONLY"), //
        s(SWT.CENTER | SWT.WRAP, "CENTER|WRAP"), //
        s(SWT.RIGHT, "RIGHT"), //
        s(SWT.RIGHT | SWT.PASSWORD, "RIGHT|PASSWORD"), //
        s(SWT.RIGHT | SWT.SEARCH, "RIGHT|SEARCH"), //
        s(SWT.RIGHT | SWT.READ_ONLY, "RIGHT|READ_ONLY"), //
        s(SWT.RIGHT | SWT.WRAP, "RIGHT|WRAP"), //
        s(SWT.MULTI, "MULTI"), //
        s(SWT.MULTI | SWT.PASSWORD, "MULTI|PASSWORD"), //
        s(SWT.MULTI | SWT.SEARCH, "MULTI|SEARCH"), //
        s(SWT.MULTI | SWT.READ_ONLY, "MULTI|READ_ONLY"), //
        s(SWT.MULTI | SWT.WRAP, "MULTI|WRAP"), //
        s(SWT.MULTI | SWT.LEFT, "MULTI|LEFT"), //
        s(SWT.MULTI | SWT.LEFT | SWT.PASSWORD, "MULTI|LEFT|PASSWORD"), //
        s(SWT.MULTI | SWT.LEFT | SWT.SEARCH, "MULTI|LEFT|SEARCH"), //
        s(SWT.MULTI | SWT.LEFT | SWT.READ_ONLY, "MULTI|LEFT|READ_ONLY"), //
        s(SWT.MULTI | SWT.LEFT | SWT.WRAP, "MULTI|LEFT|WRAP"), //
        s(SWT.MULTI | SWT.CENTER, "MULTI|CENTER"), //
        s(SWT.MULTI | SWT.CENTER | SWT.PASSWORD, "MULTI|CENTER|PASSWORD"), //
        s(SWT.MULTI | SWT.CENTER | SWT.SEARCH, "MULTI|CENTER|SEARCH"), //
        s(SWT.MULTI | SWT.CENTER | SWT.READ_ONLY, "MULTI|CENTER|READ_ONLY"), //
        s(SWT.MULTI | SWT.CENTER | SWT.WRAP, "MULTI|CENTER|WRAP"), //
        s(SWT.MULTI | SWT.RIGHT, "MULTI|RIGHT"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.PASSWORD, "MULTI|RIGHT|PASSWORD"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.SEARCH, "MULTI|RIGHT|SEARCH"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.READ_ONLY, "MULTI|RIGHT|READ_ONLY"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.WRAP, "MULTI|RIGHT|WRAP"), //
        s(SWT.SINGLE, "SINGLE"), //
        s(SWT.SINGLE | SWT.PASSWORD, "SINGLE|PASSWORD"), //
        s(SWT.SINGLE | SWT.SEARCH, "SINGLE|SEARCH"), //
        s(SWT.SINGLE | SWT.READ_ONLY, "SINGLE|READ_ONLY"), //
        s(SWT.SINGLE | SWT.WRAP, "SINGLE|WRAP"), //
        s(SWT.SINGLE | SWT.LEFT, "SINGLE|LEFT"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.PASSWORD, "SINGLE|LEFT|PASSWORD"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.SEARCH, "SINGLE|LEFT|SEARCH"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY, "SINGLE|LEFT|READ_ONLY"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.WRAP, "SINGLE|LEFT|WRAP"), //
        s(SWT.SINGLE | SWT.CENTER, "SINGLE|CENTER"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.PASSWORD, "SINGLE|CENTER|PASSWORD"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.SEARCH, "SINGLE|CENTER|SEARCH"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.READ_ONLY, "SINGLE|CENTER|READ_ONLY"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.WRAP, "SINGLE|CENTER|WRAP"), //
        s(SWT.SINGLE | SWT.RIGHT, "SINGLE|RIGHT"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.PASSWORD, "SINGLE|RIGHT|PASSWORD"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.SEARCH, "SINGLE|RIGHT|SEARCH"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.READ_ONLY, "SINGLE|RIGHT|READ_ONLY"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.WRAP, "SINGLE|RIGHT|WRAP"));
    }

    static Stream<Arguments> basicCases() {
        return buildCases(getStyles(), 2, 1);
    }

    static Stream<Arguments> allCases() {
        return buildCases(getStyles(), All, All);
    }
}
