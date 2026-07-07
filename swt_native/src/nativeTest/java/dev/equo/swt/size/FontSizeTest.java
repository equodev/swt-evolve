package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.GenFontMetrics;
import dev.equo.swt.WebFontSubstitutions;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates {@code GenFontMetrics.DATA} (the precomputed Java-side font metrics table) against
 * live Flutter measurements, against both render clients of the native/web backend: the desktop
 * Flutter engine ({@code -Dharness.client=native}, real installed system fonts via
 * {@link Display#getFontList}) and CanvasKit in a browser ({@code -Dharness.client=web}, the
 * bundled Liberation/Inter set {@code WebFontSubstitutions} maps every system font to).
 */
@Tag("metal")
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FontSizeTest extends SizeAssert {

    @RegisterExtension()
    final FontMeasureBridge staticBridge = new FontMeasureBridge();
    private Display display;

    public IntStream sizes() {
        return IntStream.concat(IntStream.of(10), new Random().ints(4,50).filter(i -> i != 10)
                .distinct()
                .limit(4)
                .sorted());
    }

    public Stream<FontVariation> fonts() {
        if (!"native".equals(System.getProperty("harness.client", "web"))) {
            return webFonts();
        }
        display = new Display();
        FontData[] fontList = display.getFontList(null, true);
        System.out.println("#OFFONTS:" + fontList.length);
        // some problematic fonts to review, X or Y are out of range
        return Arrays.stream(fontList)
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
                .filter(f -> !OS.WINDOWS.isCurrentOs() || !f.getName().contains("Ubuntu"))
                .map(FontVariation::from)
                .filter(fv -> GenFontMetrics.DATA.containsKey(fv.getId()))
                .distinct();
    }

    /** Inter isn't a substitution target — nothing in WebFontSubstitutions maps to it, so its
     *  weights aren't reachable through fontSubstitutionKeys() below and need direct coverage. */
    private static final int[] INTER_WEIGHTS = {300, 400, 500, 600, 700};

    /**
     * Web-mode font enumeration: validates {@code GenFontMetrics.DATA}'s Liberation/Inter entries
     * against real CanvasKit measurements. Liberation Sans/Serif/Mono are covered through every
     * system font name {@code WebFontSubstitutions} maps to them (Arial, Times New Roman, ...),
     * crossed with both styles and both weights; Inter is covered directly since no system font
     * substitutes to it.
     */
    protected Stream<FontVariation> webFonts() {
        WebFontSubstitutions.ensureRegistered();
        Stream<FontVariation> substituted = FontMetricsUtil.fontSubstitutionKeys().stream()
                .flatMap(name -> Stream.of(false, true)
                        .flatMap(italic -> Stream.of(400, 700)
                                .map(weight -> new FontVariation(name, italic, weight))));
        Stream<FontVariation> inter = Arrays.stream(INTER_WEIGHTS).boxed()
                .flatMap(weight -> Stream.of(
                        new FontVariation("Inter", false, weight),
                        new FontVariation("Inter", true, weight)));
        return Stream.concat(substituted, inter)
                .filter(fv -> GenFontMetrics.DATA.containsKey(fv.getId()))
                .distinct();
    }

    @AfterAll
    void dispose() {
        if (display != null) display.dispose();
    }

    public Stream<Arguments> sizesTextsAndFonts() {
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

        CompletableFuture<PointD> result = staticBridge
                .measureLineMetrics(FontMetricsUtil.substituteFontName(font.name()), font.italic(), font.weight(), fontSize, text)
                .thenApply(arr -> new PointD(arr[0], arr[1]));

        assertCompletes(staticBridge, result);
        assertThat(javaSize).satisfies(similarTextSize(result.get()));
    }
}
