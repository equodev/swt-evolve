package dev.equo.swt.harness;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Assertions on a record as the wire carried it (see {@link WireValues}): a property present with a
 * value, absent, or satisfying a check. Numbers compare by value whatever type carried them.
 */
public final class WireAssert {

    private final Object tree;
    private final String path;

    private WireAssert(Object tree, String path) {
        this.tree = tree;
        this.path = path;
    }

    public static WireAssert assertThatWire(Object tree) {
        return new WireAssert(WireValues.plain(tree), "");
    }

    /** The value at {@code path} (see {@link WireValues#at}). */
    public WireAssert node(String path) {
        return new WireAssert(tree, this.path.isEmpty() ? path : this.path + "." + path);
    }

    private Object value() {
        return path.isEmpty() ? tree : WireValues.at(tree, path);
    }

    private String where() {
        return path.isEmpty() ? "the record" : "\"" + path + "\"";
    }

    private Map<?, ?> map() {
        Object v = value();
        if (!(v instanceof Map)) throw new AssertionError(where() + " is not a record: " + v + "\n in " + tree);
        return (Map<?, ?>) v;
    }

    public WireAssert isObject() {
        map();
        return this;
    }

    public WireAssert isNotEmpty() {
        if (map().isEmpty()) throw new AssertionError(where() + " is empty");
        return this;
    }

    public WireAssert containsEntry(String key, Object expected) {
        Map<?, ?> m = map();
        if (!m.containsKey(key)) throw new AssertionError(where() + " has no \"" + key + "\"\n in " + m);
        if (!WireValues.same(m.get(key), expected)) {
            throw new AssertionError("\"" + key + "\" is " + m.get(key) + ", expected " + WireValues.written(expected)
                    + "\n in " + m);
        }
        return this;
    }

    public WireAssert containsKey(String key) {
        Map<?, ?> m = map();
        if (!m.containsKey(key)) throw new AssertionError(where() + " has no \"" + key + "\"\n in " + m);
        return this;
    }

    public WireAssert doesNotContainKey(String key) {
        Map<?, ?> m = map();
        if (m.containsKey(key)) throw new AssertionError(where() + " has \"" + key + "\": " + m.get(key));
        return this;
    }

    public WireAssert isEqualTo(Object expected) {
        Object v = value();
        if (v == WireValues.MISSING) throw new AssertionError(where() + " is absent, expected " + expected + "\n in " + tree);
        if (!WireValues.same(v, expected)) {
            throw new AssertionError(where() + " is " + v + ", expected " + WireValues.written(expected));
        }
        return this;
    }

    public WireAssert isPresent() {
        if (value() == WireValues.MISSING) throw new AssertionError(where() + " is absent\n in " + tree);
        return this;
    }

    public WireAssert isAbsent() {
        if (value() != WireValues.MISSING) throw new AssertionError(where() + " is present: " + value());
        return this;
    }

    public WireAssert isNull() {
        if (value() != null) throw new AssertionError(where() + " is " + value() + ", expected null");
        return this;
    }

    public WireAssert hasSize(int size) {
        Object v = value();
        if (!(v instanceof List)) throw new AssertionError(where() + " is not a list: " + v);
        if (((List<?>) v).size() != size) throw new AssertionError(where() + " has " + ((List<?>) v).size() + " elements, expected " + size);
        return this;
    }

    /** Runs {@code check} on the whole tree this assertion started from. */
    public WireAssert satisfies(Consumer<Object> check) {
        check.accept(tree);
        return this;
    }
}
