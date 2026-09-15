package dev.equo.swt.delivery;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the same contract the Dart canonicalizer runs, from the same file, so the two
 * implementations cannot drift apart unnoticed - a case that passes on one side and fails on the
 * other fails the build on that side.
 *
 * <p>The canonicalizer is an oracle: convergence probes report what they report because this code
 * said two states were the same. So it is tested against the shared cases rather than against
 * itself, and its willingness to call things <em>different</em> is asserted explicitly - an oracle
 * that only ever says "equal" would turn the whole delivery suite green and prove nothing.
 */
class CanonTest {

    /** The contract, shared with Dart. Test working directory is the {@code swt_native} project. */
    private static final Path CASES =
            Paths.get("..", "flutter-lib", "test", "delivery", "canon_cases.json");

    @TestFactory
    List<DynamicTest> sharedContract() throws IOException {
        JsonObject cases = JsonParser.parseString(Files.readString(CASES)).getAsJsonObject();
        List<DynamicTest> tests = new ArrayList<>();

        for (JsonElement element : cases.getAsJsonArray("equal")) {
            JsonObject c = element.getAsJsonObject();
            String why = c.get("why").getAsString();
            String a = c.get("a").toString();
            String b = c.get("b").toString();
            tests.add(DynamicTest.dynamicTest("equal: " + why, () ->
                    assertThat(Canon.canon(a))
                            .as("these two payloads carry the same state and must canonicalize alike")
                            .isEqualTo(Canon.canon(b))));
        }

        for (JsonElement element : cases.getAsJsonArray("differ")) {
            JsonObject c = element.getAsJsonObject();
            String why = c.get("why").getAsString();
            String a = c.get("a").toString();
            String b = c.get("b").toString();
            tests.add(DynamicTest.dynamicTest("differ: " + why, () ->
                    assertThat(Canon.canon(a))
                            .as("these two payloads carry different state; collapsing them would hide a "
                                    + "real divergence from every probe that relies on this comparison")
                            .isNotEqualTo(Canon.canon(b))));
        }
        return tests;
    }

    @Test
    void isIdempotent() {
        String once = Canon.canon("{\"swt\":\"Label\",\"id\":1,\"_s\":3,\"text\":\"x\",\"enabled\":false}");
        assertThat(Canon.canon(once)).isEqualTo(once);
    }

    @Test
    void recursesToArbitraryDepth() {
        StringBuilder nested = new StringBuilder("{\"swt\":\"Label\",\"id\":1,\"_s\":9}");
        for (int depth = 1; depth <= 20; depth++) {
            nested = new StringBuilder("{\"swt\":\"Composite\",\"id\":" + depth + ",\"_s\":" + depth
                    + ",\"children\":[" + nested + "]}");
        }
        assertThat(Canon.canon(nested.toString()))
                .as("a write stamp buried 20 levels down is still not state")
                .doesNotContain("_s");
    }

    @Test
    void aDeeplyBuriedChangeSurvivesTheReduction() {
        String a = nest("a");
        String b = nest("b");
        assertThat(Canon.canon(a))
                .as("the reduction must not flatten away a nested difference")
                .isNotEqualTo(Canon.canon(b));
    }

    private static String nest(String leaf) {
        return "{\"swt\":\"Composite\",\"id\":1,\"children\":[{\"swt\":\"Composite\",\"id\":2,"
                + "\"children\":[{\"swt\":\"Label\",\"id\":3,\"text\":\"" + leaf + "\"}]}]}";
    }

    /**
     * The bytes, not just the verdict. Both languages emit compact JSON with sorted keys so an
     * end-to-end probe can compare Java's view of a widget against the client's directly; if either
     * side changed its spacing or escaping, that comparison would fail on formatting rather than on
     * state, and the failure would look like a delivery bug.
     */
    @Test
    void emitsTheAgreedByteFormat() {
        assertThat(Canon.canon("{\"text\":\"x\",\"id\":1,\"swt\":\"Label\",\"_s\":4,\"enabled\":false}"))
                .isEqualTo("{\"id\":1,\"swt\":\"Label\",\"text\":\"x\"}");
        assertThat(Canon.canon("{\"a\":\"q\\\"q\",\"b\":\"tab\\there\"}"))
                .isEqualTo("{\"a\":\"q\\\"q\",\"b\":\"tab\\there\"}");
        assertThat(Canon.canon("{\"n\":3.0,\"m\":2.5}")).isEqualTo("{\"m\":2.5,\"n\":3}");
    }
}
