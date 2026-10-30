package dev.equo.swt.delivery;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /** The shared case file, whose format the Dart side reads too. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> cases() throws IOException {
        return new com.google.gson.Gson().fromJson(Files.readString(CASES), Map.class);
    }

    @TestFactory
    @SuppressWarnings("unchecked")
    List<DynamicTest> sharedContract() throws IOException {
        Map<String, Object> cases = cases();
        List<DynamicTest> tests = new ArrayList<>();

        for (Object element : (List<Object>) cases.get("equal")) {
            Map<String, Object> c = (Map<String, Object>) element;
            String why = (String) c.get("why");
            Object a = c.get("a");
            Object b = c.get("b");
            tests.add(DynamicTest.dynamicTest("equal: " + why, () ->
                    assertThat(Canon.canon(a))
                            .as("these two states are the same and must canonicalize alike")
                            .isEqualTo(Canon.canon(b))));
        }

        for (Object element : (List<Object>) cases.get("differ")) {
            Map<String, Object> c = (Map<String, Object>) element;
            String why = (String) c.get("why");
            Object a = c.get("a");
            Object b = c.get("b");
            tests.add(DynamicTest.dynamicTest("differ: " + why, () ->
                    assertThat(Canon.canon(a))
                            .as("these two states differ; collapsing them would hide a real divergence "
                                    + "from every probe that relies on this comparison")
                            .isNotEqualTo(Canon.canon(b))));
        }
        return tests;
    }

    private static Map<String, Object> record(Object... keyThenValue) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyThenValue.length; i += 2) m.put((String) keyThenValue[i], keyThenValue[i + 1]);
        return m;
    }

    @Test
    void isIdempotent() {
        Object once = Canon.canon(record("swt", "Label", "id", 1, "_s", 3, "text", "x", "enabled", false));
        assertThat(Canon.canon(once)).isEqualTo(once);
    }

    @Test
    void recursesToArbitraryDepth() {
        Map<String, Object> nested = record("swt", "Label", "id", 1, "_s", 9);
        for (int depth = 1; depth <= 20; depth++) {
            nested = record("swt", "Composite", "id", depth, "_s", depth, "children", List.of(nested));
        }
        assertThat(Canon.canon((Object) nested).toString())
                .as("a write stamp buried 20 levels down is still not state")
                .doesNotContain("_s");
    }

    @Test
    void aDeeplyBuriedChangeSurvivesTheReduction() {
        assertThat(Canon.canon(nest("a")))
                .as("the reduction must not flatten away a nested difference")
                .isNotEqualTo(Canon.canon(nest("b")));
    }

    private static Object nest(String leaf) {
        return record("swt", "Composite", "id", 1, "children", List.of(record("swt", "Composite", "id", 2,
                "children", List.of(record("swt", "Label", "id", 3, "text", leaf)))));
    }

    /** Keys sorted, protocol keys and defaults gone, a whole number the same whatever type carried it. */
    @Test
    void sortsKeysAndNormalizesNumbers() {
        Object canonical = Canon.canon(record("text", "x", "id", 1, "swt", "Label", "_s", 4, "enabled", false));
        assertThat(new java.util.ArrayList<Object>(((Map<?, ?>) canonical).keySet())).containsExactly("id", "swt", "text");
        assertThat(Canon.canon(record("n", 3.0, "m", 2.5))).isEqualTo(record("m", 2.5, "n", 3L));
        assertThat(Canon.canon(record("n", 3))).isEqualTo(Canon.canon(record("n", 3L)));
    }
}
