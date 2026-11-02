package dev.equo.swt.harness;

import dev.equo.swt.WireDump;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * What the wire carried, as plain values a test reads: maps of properties by name, lists, strings,
 * booleans and numbers. A widget a record names is {@code {id, swt, _r: true}}; bytes are described,
 * not listed.
 */
public final class WireValues {

    private WireValues() {
    }

    /** What {@link #at} answers for a path that leads nowhere. */
    public static final Object MISSING = new Object() {
        @Override
        public String toString() {
            return "<missing>";
        }
    };

    /** {@code v} decoded from the wire as a test reads it. */
    /**
     * {@code v} as the wire writes and reads it back: an SWT value through its encoding, a value
     * class through its fields; anything else as it is.
     */
    public static Object written(Object v) {
        String kind = v instanceof org.eclipse.swt.graphics.Point ? "point"
                : v instanceof org.eclipse.swt.graphics.Rectangle ? "rect"
                : v instanceof org.eclipse.swt.graphics.RGB ? "rgb"
                : v instanceof org.eclipse.swt.graphics.Color ? "color"
                : null;
        if (kind == null) {
            if (!(v instanceof dev.equo.swt.WireObject)) return v;
            byte[] fields = dev.equo.swt.WidgetWire.fieldsOf((dev.equo.swt.WireObject) v);
            return plain(WireDump.fields(fields, 0, fields.length));
        }
        dev.equo.swt.WidgetWire w = dev.equo.swt.WidgetWire.borrow();
        try {
            w.reset();
            if (v instanceof org.eclipse.swt.graphics.Point) w.write((org.eclipse.swt.graphics.Point) v);
            else if (v instanceof org.eclipse.swt.graphics.Rectangle) w.write((org.eclipse.swt.graphics.Rectangle) v);
            else if (v instanceof org.eclipse.swt.graphics.RGB) w.write((org.eclipse.swt.graphics.RGB) v);
            else w.write((org.eclipse.swt.graphics.Color) v);
            return plain(WireDump.value(w.buffer(), 0, w.size(), kind));
        } finally {
            dev.equo.swt.WidgetWire.giveBack(w);
        }
    }

    public static Object plain(Object v) {
        if (v instanceof WireDump.Ref) {
            WireDump.Ref ref = (WireDump.Ref) v;
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", ref.id);
            if (ref.type != null) o.put("swt", ref.type);
            o.put("_r", true);
            return o;
        }
        if (v instanceof byte[]) return WireDump.describe((byte[]) v);
        if (v instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object e : (List<?>) v) out.add(plain(e));
            return out;
        }
        if (v instanceof Map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) out.put(String.valueOf(e.getKey()), plain(e.getValue()));
            return out;
        }
        if (v != null && v.getClass().isArray()) {
            List<Object> out = new ArrayList<>();
            for (int i = 0, n = Array.getLength(v); i < n; i++) out.add(plain(Array.get(v, i)));
            return out;
        }
        return v;
    }

    /**
     * Whether {@code actual} holds {@code expected}: numbers by value whatever their type, an array
     * the same as a list of its elements, maps and lists element by element.
     */
    public static boolean same(Object actual, Object expected) {
        expected = written(expected);
        if (actual == expected) return true;
        if (actual == null || expected == null) return false;
        if (actual instanceof Number && expected instanceof Number) {
            return ((Number) actual).doubleValue() == ((Number) expected).doubleValue();
        }
        if (actual instanceof Number && expected instanceof Character) return ((Number) actual).intValue() == (Character) expected;
        if (expected.getClass().isArray() || actual.getClass().isArray()) {
            return same(plain(actual), plain(expected));
        }
        if (actual instanceof List && expected instanceof List) {
            List<?> a = (List<?>) actual, b = (List<?>) expected;
            if (a.size() != b.size()) return false;
            for (int i = 0; i < a.size(); i++) if (!same(a.get(i), b.get(i))) return false;
            return true;
        }
        if (actual instanceof Map && expected instanceof Map) {
            Map<?, ?> a = (Map<?, ?>) actual, b = (Map<?, ?>) expected;
            if (!a.keySet().equals(b.keySet())) return false;
            for (Object k : a.keySet()) if (!same(a.get(k), b.get(k))) return false;
            return true;
        }
        return actual.equals(expected);
    }

    /**
     * The value at {@code path} in {@code tree}: property names joined by dots, an index in brackets,
     * as in {@code font.fontData[0].name}. {@link #MISSING} when any step is absent.
     */
    public static Object at(Object tree, String path) {
        Object node = tree;
        for (String step : path.split("\\.")) {
            String name = step;
            List<Integer> indices = new ArrayList<>();
            int bracket = step.indexOf('[');
            if (bracket >= 0) {
                name = step.substring(0, bracket);
                for (String index : step.substring(bracket + 1, step.length() - 1).split("\\]\\[")) {
                    indices.add(Integer.parseInt(index));
                }
            }
            if (!name.isEmpty()) {
                if (!(node instanceof Map) || !((Map<?, ?>) node).containsKey(name)) return MISSING;
                node = ((Map<?, ?>) node).get(name);
            }
            for (int index : indices) {
                if (!(node instanceof List) || index >= ((List<?>) node).size()) return MISSING;
                node = ((List<?>) node).get(index);
            }
        }
        return node;
    }

    /** What a decoded frame's parts hold: a widget record its state, a batch entry its value. */
    private static Object content(Object node) {
        if (node instanceof WireFrames.Record record) return record.state();
        if (node instanceof GcFrames.Entry entry) return entry.value();
        return node;
    }

    /** Every map anywhere in {@code tree}, outermost first, that {@code test} accepts. */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> find(Object tree, Predicate<Map<String, Object>> test) {
        List<Map<String, Object>> out = new ArrayList<>();
        collect(tree, test, out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void collect(Object node, Predicate<Map<String, Object>> test, List<Map<String, Object>> out) {
        node = content(node);
        if (node instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) node;
            if (test.test(map)) out.add(map);
            for (Object v : map.values()) collect(v, test, out);
        } else if (node instanceof List) {
            for (Object v : (List<Object>) node) collect(v, test, out);
        }
    }

    /** Every map anywhere in {@code tree} that describes the widget or resource {@code id}. */
    public static List<Map<String, Object>> describing(Object tree, long id) {
        return find(tree, m -> m.get("id") instanceof Number && ((Number) m.get("id")).longValue() == id);
    }

    /** Whether {@code key} is a property anywhere in {@code tree}. */
    public static boolean mentions(Object tree, String key) {
        return !find(tree, m -> m.containsKey(key)).isEmpty();
    }

    /** Whether any string anywhere in {@code tree}, key or value, contains {@code text}. */
    public static boolean containsText(Object tree, String text) {
        tree = content(tree);
        if (tree instanceof String) return ((String) tree).contains(text);
        if (tree instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) tree).entrySet()) {
                if (String.valueOf(e.getKey()).contains(text) || containsText(e.getValue(), text)) return true;
            }
            return false;
        }
        if (tree instanceof List) {
            for (Object v : (List<?>) tree) if (containsText(v, text)) return true;
        }
        return false;
    }
}
