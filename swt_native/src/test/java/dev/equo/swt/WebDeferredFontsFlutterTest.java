package dev.equo.swt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The web engine starts with only the faces the widgets draw with (web/flutter_bootstrap.js): the
 * faces named fonts are substituted with are fetched once text asks for them, and Roboto, the
 * engine's default face, comes from the bundle alone.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebDeferredFontsFlutterTest {

    private static final String PAGE = "JSON.stringify({"
            + "init: performance.getEntriesByName('evolve.engineInitialized')[0].startTime,"
            + "fetches: performance.getEntriesByType('resource').map(e => ({url: e.name, start: e.startTime}))})";

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private double engineInitialized;
    /** Fetched by the time the Shell is painted, with no text in a substituted font. */
    private final List<JsonObject> beforeAsked = new ArrayList<>();
    /** Fetched once a page test has asked for every face ({@code waitForFrame}). */
    private final List<JsonObject> fetches = new ArrayList<>();

    @BeforeAll
    void boot() {
        Display display = flutter.boot();
        new Shell(display).open();
        read(flutter.devTools().evaluate(
                "new Promise(r => requestAnimationFrame(() => requestAnimationFrame(() => r(" + PAGE + "))))"),
                beforeAsked);
        read(flutter.devTools().evaluate(
                "new Promise(r => window.evolveTest.waitForFrame(() => r(" + PAGE + ")))"), fetches);
    }

    private void read(JsonElement page, List<JsonObject> into) {
        JsonObject parsed = JsonParser.parseString(page.getAsString()).getAsJsonObject();
        engineInitialized = parsed.get("init").getAsDouble();
        JsonArray all = parsed.getAsJsonArray("fetches");
        all.forEach(e -> into.add(e.getAsJsonObject()));
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void noSubstitutedFaceIsFetchedUntilTextAsksForIt() {
        assertThat(fetched(beforeAsked, "/assets/assets/fonts/Liberation")).isEmpty();
    }

    @Test
    void theSubstitutedFacesAreFetchedAfterTheEngineHasStarted() {
        List<JsonObject> faces = fetched(fetches, "/assets/assets/fonts/Liberation");

        assertThat(faces).hasSize(12);
        assertThat(faces).allSatisfy(f -> assertThat(f.get("start").getAsDouble())
                .as(f.get("url").getAsString()).isGreaterThan(engineInitialized));
    }

    @Test
    void theThemeFaceIsInBeforeTheEngineHasStarted() {
        List<JsonObject> faces = fetched(fetches, "/assets/assets/fonts/Inter_");

        assertThat(faces).hasSize(5);
        assertThat(faces).allSatisfy(f -> assertThat(f.get("start").getAsDouble())
                .as(f.get("url").getAsString()).isLessThan(engineInitialized));
    }

    @Test
    void robotoIsFetchedOnceFromTheBundleBeforeTheEngineHasStarted() {
        assertThat(fetched(fetches, "fonts.gstatic.com")).isEmpty();
        List<JsonObject> regular = fetched(fetches, "/assets/assets/fonts/Roboto-Regular.ttf");
        List<JsonObject> bold = fetched(fetches, "/assets/assets/fonts/Roboto-Bold.ttf");

        assertThat(regular).hasSize(1);
        assertThat(bold).hasSize(1);
        assertThat(regular.get(0).get("start").getAsDouble()).isLessThan(engineInitialized);
        assertThat(bold.get(0).get("start").getAsDouble()).isLessThan(engineInitialized);
    }

    private static List<JsonObject> fetched(List<JsonObject> from, String... urlParts) {
        List<JsonObject> matching = new ArrayList<>();
        for (JsonObject f : from) {
            String url = f.get("url").getAsString();
            for (String part : urlParts) {
                if (url.contains(part)) {
                    matching.add(f);
                    break;
                }
            }
        }
        return matching;
    }
}
