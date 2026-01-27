package dev.equo.swt.size;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnOs({OS.LINUX})
public class FontSizeTest extends SizeAssert {

    @RegisterExtension()
    static final FontSizeBridge staticBridge = new FontSizeBridge();
    private static Display display;

    public static IntStream sizes() {
        return IntStream.concat(IntStream.of(10), new Random().ints(4,50).filter(i -> i != 10)
                .distinct()
                .limit(4)
                .sorted());
    }

    public static Stream<FontVariation> fonts() {
        display = new Display();
        FontData[] fontList = display.getFontList(null, true);
        System.out.println("#OFFONTS:" + fontList.length);
        // some problematic fonts to review, X or Y are out of range
        return Arrays.stream(fontList)
                // some problematic fonts to review, X or Y are out of range
                .filter(f -> !f.getName().contains("Emoji"))
                .filter(f -> !f.getName().contains("Braille"))
                .filter(f -> !f.getName().contains("Bitmap"))
                .filter(f -> !f.getName().contains("Academy Engraved"))
                .filter(f -> !f.getName().contains("Euphemia UCAS"))
                .filter(f -> !f.getName().contains("Marker Felt"))
                .filter(f -> !f.getName().contains("Myanmar"))
                .filter(f -> !f.getName().contains("Party LET"))
                .filter(f -> !f.getName().contains("Savoye LET"))
                .filter(f -> !f.getName().contains("Kailasa"))
                .filter(f -> !f.getName().contains("Noto Sans NKo"))
                .filter(f -> !f.getName().contains("Kokonor"))
                .filter(f -> !f.getName().contains("Noto Sans Tagalog"))
                .filter(f -> !f.getName().contains("Noto Sans Batak"))
                .filter(f -> !f.getName().contains("Chalkboard SE"))
                .filter(f -> !f.getName().contains("Specialty"))
                .filter(f -> !f.getName().contains("Zapfino"))
                .map(FontVariation::from)
                .distinct();
    }

    @AfterAll
    static void dispose() {
        FlutterBridge.disposeDisplayAndContinue(display);
    }

    public static Stream<Arguments> sizesTextsAndFonts() {
        List<FontVariation> fontList = fonts().toList();
        Random random = new Random();
        return fontList.stream()
                .flatMap(font ->
                    sizes().boxed().map(size -> {
                        String text = Instancio.gen().text().loremIpsum().words(random.nextInt(1, 20)).get();
                        return Arguments.of(font, size, text);
                    })
                );
    }

    @ParameterizedTest
    @MethodSource("sizesTextsAndFonts")
    void text_size_should_equals_flutter(FontVariation font, int fontSize, String text) throws Exception {
        PointD javaSize = FontMetricsUtil.getFontSize(text, font.getId(), fontSize);

        CompletableFuture<PointD> result = staticBridge.measure(font, fontSize, text);

        assertCompletes(result);
        assertThat(javaSize).satisfies(similarTextSize(result.get()));
    }

    static class FontSizeBridge extends GenericSizeBridge<Map<String, ? extends Serializable>, double[], PointD> {

        public FontSizeBridge() {
            super("fontSize", double[].class);
        }

        public CompletableFuture<PointD> measure(FontVariation font, int fontSize, String text) {
            Map<String, ? extends Serializable> request = Map.of(
                "font", font.name(),
                "style", font.italic(),
                "weight", font.bold(),
                "size", fontSize,
                "text", text
            );
            return super.measure(request);
        }

        @Override
        protected PointD convertResult(double[] serialized) {
            return new PointD(serialized[0], serialized[1]);
        }
    }
}
