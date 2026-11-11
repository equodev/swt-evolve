package dev.equo.swt;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The page opens the comm socket at navigation (web/index.html), and the client takes it over
 * once main() runs instead of opening one of its own.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebCommAtNavigationFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private JsonObject page;

    @BeforeAll
    void boot() {
        Display display = flutter.boot();
        new Shell(display).open();
        page = JsonParser.parseString(flutter.devTools().evaluate(
                "new Promise(r => window.evolveTest.waitForFrame(() => {"
                        + " const at = n => { const m = performance.getEntriesByName(n); return m.length ? m[0].startTime : -1; };"
                        + " r(JSON.stringify({connect: at('evolve.commConnect'),"
                        + " takenOver: at('evolve.commTakenOver')})); }))")
                .getAsString()).getAsJsonObject();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void thePageOpensTheSocket() {
        assertThat(page.get("connect").getAsDouble()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void theClientTakesThePageSocketOver() {
        // Marked only by the client adopting it, not by one that closes it and connects on its own.
        assertThat(page.get("takenOver").getAsDouble()).isGreaterThanOrEqualTo(0);
    }
}
