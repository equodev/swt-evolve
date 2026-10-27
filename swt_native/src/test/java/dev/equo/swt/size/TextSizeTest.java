package dev.equo.swt.size;

import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartText;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.DartMocks.*;

@Tag("flutter-it")
@Tag("metal")
@Tag("web-known-broken")
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
        Measure measure = assertCompletes(flutter, result);
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
    @MethodSource("boldCases")
    void java_size_should_equals_flutter_with_bold(int style, String text, int size, int fontStyle) {
        DartText w = createText(style, text, size, fontStyle);
        ConfigFlags config = ConfigFlags.use_swt_fonts(size != FromTheme);
        //
        Measure javaSize = TextSizes.computeSizes(w, SWT.DEFAULT, SWT.DEFAULT, true);;
        //
        CompletableFuture<Measure> result = flutter.measure(w, config);;
        Measure measure = assertCompletes(flutter, result);
        assertSoftly(soft -> {
            soft.assertThat(javaSize)
                .as("widget size (bold affects width)")
                .satisfies(similarSize(measure));
            soft.assertThat(javaSize.textStyle)
                .as("text style includes bold from font")
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
        Measure measure = assertCompletes(flutter, result);;
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
        Measure measure = assertCompletes(flutter, result);;
        assertThat(measure.widget).isEqualTo(new Point(200, 150));
    }

    static DartText createText(int style, String text, int size) {
        return createText(style, text, size, SWT.NORMAL);
    }

    static DartText createText(int style, String text, int size, int fontStyle) {
        DartText w = new DartText(dartShell(), style, null);
        if (!NoTxt.equals(text))
            w.setText(text);
        if (size != FromTheme)
            w.setFont(createFont(size, fontStyle));
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
        s(SWT.SINGLE | SWT.RIGHT | SWT.WRAP, "SINGLE|RIGHT|WRAP"), //
        s(SWT.LEFT | SWT.BORDER, "LEFT|BORDER"), //
        s(SWT.LEFT | SWT.PASSWORD | SWT.BORDER, "LEFT|PASSWORD|BORDER"), //
        s(SWT.LEFT | SWT.SEARCH | SWT.BORDER, "LEFT|SEARCH|BORDER"), //
        s(SWT.LEFT | SWT.READ_ONLY | SWT.BORDER, "LEFT|READ_ONLY|BORDER"), //
        s(SWT.LEFT | SWT.WRAP | SWT.BORDER, "LEFT|WRAP|BORDER"), //
        s(SWT.CENTER | SWT.BORDER, "CENTER|BORDER"), //
        s(SWT.CENTER | SWT.PASSWORD | SWT.BORDER, "CENTER|PASSWORD|BORDER"), //
        s(SWT.CENTER | SWT.SEARCH | SWT.BORDER, "CENTER|SEARCH|BORDER"), //
        s(SWT.CENTER | SWT.READ_ONLY | SWT.BORDER, "CENTER|READ_ONLY|BORDER"), //
        s(SWT.CENTER | SWT.WRAP | SWT.BORDER, "CENTER|WRAP|BORDER"), //
        s(SWT.RIGHT | SWT.BORDER, "RIGHT|BORDER"), //
        s(SWT.RIGHT | SWT.PASSWORD | SWT.BORDER, "RIGHT|PASSWORD|BORDER"), //
        s(SWT.RIGHT | SWT.SEARCH | SWT.BORDER, "RIGHT|SEARCH|BORDER"), //
        s(SWT.RIGHT | SWT.READ_ONLY | SWT.BORDER, "RIGHT|READ_ONLY|BORDER"), //
        s(SWT.RIGHT | SWT.WRAP | SWT.BORDER, "RIGHT|WRAP|BORDER"), //
        s(SWT.MULTI | SWT.BORDER, "MULTI|BORDER"), //
        s(SWT.MULTI | SWT.PASSWORD | SWT.BORDER, "MULTI|PASSWORD|BORDER"), //
        s(SWT.MULTI | SWT.SEARCH | SWT.BORDER, "MULTI|SEARCH|BORDER"), //
        s(SWT.MULTI | SWT.READ_ONLY | SWT.BORDER, "MULTI|READ_ONLY|BORDER"), //
        s(SWT.MULTI | SWT.WRAP | SWT.BORDER, "MULTI|WRAP|BORDER"), //
        s(SWT.MULTI | SWT.LEFT | SWT.BORDER, "MULTI|LEFT|BORDER"), //
        s(SWT.MULTI | SWT.LEFT | SWT.PASSWORD | SWT.BORDER, "MULTI|LEFT|PASSWORD|BORDER"), //
        s(SWT.MULTI | SWT.LEFT | SWT.SEARCH | SWT.BORDER, "MULTI|LEFT|SEARCH|BORDER"), //
        s(SWT.MULTI | SWT.LEFT | SWT.READ_ONLY | SWT.BORDER, "MULTI|LEFT|READ_ONLY|BORDER"), //
        s(SWT.MULTI | SWT.LEFT | SWT.WRAP | SWT.BORDER, "MULTI|LEFT|WRAP|BORDER"), //
        s(SWT.MULTI | SWT.CENTER | SWT.BORDER, "MULTI|CENTER|BORDER"), //
        s(SWT.MULTI | SWT.CENTER | SWT.PASSWORD | SWT.BORDER, "MULTI|CENTER|PASSWORD|BORDER"), //
        s(SWT.MULTI | SWT.CENTER | SWT.SEARCH | SWT.BORDER, "MULTI|CENTER|SEARCH|BORDER"), //
        s(SWT.MULTI | SWT.CENTER | SWT.READ_ONLY | SWT.BORDER, "MULTI|CENTER|READ_ONLY|BORDER"), //
        s(SWT.MULTI | SWT.CENTER | SWT.WRAP | SWT.BORDER, "MULTI|CENTER|WRAP|BORDER"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.BORDER, "MULTI|RIGHT|BORDER"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.PASSWORD | SWT.BORDER, "MULTI|RIGHT|PASSWORD|BORDER"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.SEARCH | SWT.BORDER, "MULTI|RIGHT|SEARCH|BORDER"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.READ_ONLY | SWT.BORDER, "MULTI|RIGHT|READ_ONLY|BORDER"), //
        s(SWT.MULTI | SWT.RIGHT | SWT.WRAP | SWT.BORDER, "MULTI|RIGHT|WRAP|BORDER"), //
        s(SWT.SINGLE | SWT.BORDER, "SINGLE|BORDER"), //
        s(SWT.SINGLE | SWT.PASSWORD | SWT.BORDER, "SINGLE|PASSWORD|BORDER"), //
        s(SWT.SINGLE | SWT.SEARCH | SWT.BORDER, "SINGLE|SEARCH|BORDER"), //
        s(SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER, "SINGLE|READ_ONLY|BORDER"), //
        s(SWT.SINGLE | SWT.WRAP | SWT.BORDER, "SINGLE|WRAP|BORDER"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.BORDER, "SINGLE|LEFT|BORDER"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.PASSWORD | SWT.BORDER, "SINGLE|LEFT|PASSWORD|BORDER"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.SEARCH | SWT.BORDER, "SINGLE|LEFT|SEARCH|BORDER"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY | SWT.BORDER, "SINGLE|LEFT|READ_ONLY|BORDER"), //
        s(SWT.SINGLE | SWT.LEFT | SWT.WRAP | SWT.BORDER, "SINGLE|LEFT|WRAP|BORDER"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.BORDER, "SINGLE|CENTER|BORDER"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.PASSWORD | SWT.BORDER, "SINGLE|CENTER|PASSWORD|BORDER"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.SEARCH | SWT.BORDER, "SINGLE|CENTER|SEARCH|BORDER"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.READ_ONLY | SWT.BORDER, "SINGLE|CENTER|READ_ONLY|BORDER"), //
        s(SWT.SINGLE | SWT.CENTER | SWT.WRAP | SWT.BORDER, "SINGLE|CENTER|WRAP|BORDER"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.BORDER, "SINGLE|RIGHT|BORDER"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.PASSWORD | SWT.BORDER, "SINGLE|RIGHT|PASSWORD|BORDER"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.SEARCH | SWT.BORDER, "SINGLE|RIGHT|SEARCH|BORDER"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.READ_ONLY | SWT.BORDER, "SINGLE|RIGHT|READ_ONLY|BORDER"), //
        s(SWT.SINGLE | SWT.RIGHT | SWT.WRAP | SWT.BORDER, "SINGLE|RIGHT|WRAP|BORDER"));
    }

    static Stream<Arguments> basicCases() {
        return buildCases(getStyles(), 2, 1);
    }

    static Stream<Arguments> allCases() {
        return buildCases(getStyles(), All, All);
    }

    static Stream<Arguments> boldCases() {
        return buildBoldCases(getStyles());
    }
}
