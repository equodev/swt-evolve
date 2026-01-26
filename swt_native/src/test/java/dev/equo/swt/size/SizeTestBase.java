package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(Mocks.class)
public abstract class SizeTestBase extends SizeAssert {

    protected static final int All = Integer.MAX_VALUE;

    protected static final String NoImg = "noimg";
    protected static final String NoTxt = "''";
    protected static final int FromTheme = -1;

    @BeforeAll
    static void config() {
        Config.defaultToEquo();
        Config.useEquo(Font.class);
        Config.useEquo(FontData.class);
    }

    @AfterAll
    static void unconfig() {
        Config.clear(Font.class);
        Config.clear(FontData.class);
        Config.defaultToEclipse();
    }

    protected static IntStream getSizes(int limit) {
        return Arrays.stream(new int[] {
                10,
                14,
                18,
                24,
                FromTheme
        }).limit(limit);
    }

    protected static Stream<String> getTexts(int limit) {
        return Stream.of(
                NoTxt,
                "This is a long widget text",
                "NO",
                "Intermediate"
        ).limit(limit);
    }

    protected static Stream<String> getImages(int limit) {
        return Stream.of(
                NoImg,  // No image
                "48x48",  // Big image
                "16x16"  // Small image
        ).limit(limit);
    }

    protected static Stream<Arguments> buildCases(Stream<Style> styles, int images, int texts, int sizes) {
        return styles.flatMap(style ->
                getImages(images).flatMap(image ->
                        getTexts(texts).flatMap(text ->
                                getSizes(sizes).mapToObj(size ->
                                        Arguments.of(Named.of(style.name, style.value), image, text, Named.of(size == FromTheme ? "theme" : String.valueOf(size), size))
                                )
                        )
                )
        );
    }

    protected static Stream<Arguments> buildCases(Stream<Style> styles, int texts, int sizes) {
        return styles.flatMap(style ->
                getTexts(texts).flatMap(text ->
                        getSizes(sizes).mapToObj(size ->
                                Arguments.of(Named.of(style.name, style.value), text, Named.of(size == FromTheme ? "theme" : String.valueOf(size), size))
                        )
                )
        );
    }

    protected static Stream<Arguments> buildCases(Stream<Style> styles) {
        return styles.map(style ->
           Arguments.of(Named.of(style.name, style.value))
        );
    }

    protected static Font createFont(int fontSize) {
        FontData fontData = new FontData("System", fontSize, SWT.NORMAL);
        return new Font(Display.getCurrent(), fontData);
    }

    protected static Image createImage(String path) {
        return new Image(null, SizeTestBase.class.getResourceAsStream("/images/"+path+".png"));
    }

    protected static Consumer<Measure> isEmptyText(String text) {
        return p -> assertThat(text).isEqualTo(NoTxt);
    }

    protected static Consumer<Measure> isEmptyImage(String img) {
        return p -> assertThat(img).isEqualTo(NoImg);
    }

    protected static Style s(int value, String name) {
        return new Style(value, name);
    }

    public static class Style {
        int value;
        String name;

        private Style(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    static class WidgetSizeBridge extends GenericSizeBridge<Map<String, Object>, Measure, Measure> implements BeforeEachCallback {

        private String caseName;

        public WidgetSizeBridge() {
            super("widgetSize", Measure.class);
        }

        @Override
        protected CompletableFuture<Measure> measure(Map<String, Object> widgetValue) {
            return super.measure(widgetValue);
        }

        protected CompletableFuture<Measure> measure(DartWidget w) {
            return measure(Map.of("widget", w, "name", caseName));
        }

        public CompletableFuture<Measure> measure(DartWidget w, ConfigFlags config) {
            return measure(Map.of("widget", w, "config", config, "name", caseName));
        }

        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            caseName = context.getDisplayName();
        }
    }

}
