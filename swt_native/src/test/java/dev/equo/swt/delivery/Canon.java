package dev.equo.swt.delivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The canonical form of a widget's state, Java side.
 *
 * <p>Every convergence check in the delivery suite ends in "are these two states the same", and the
 * two states being compared came from different code paths - a full send against a merged sequence
 * of diffs, or Java's view against the client's. They are equal in meaning long before they are
 * equal as values: one side elides a default the other writes out, key order differs, a write stamp
 * advanced. This reduces both to a form where equal-in-meaning is {@code equals}.
 *
 * <p>The rules are stated once, in {@code flutter-lib/test/delivery/canon_cases.json}, and
 * implemented twice - here and in Dart's {@code test/delivery/support/canon.dart}. Neither
 * implementation may be used to test the other; the shared case file is what keeps them honest.
 *
 * <p>One bias governs every arguable rule: when it is unclear whether a difference is real,
 * <strong>report a difference</strong>. A canonicalizer that normalizes away a real change turns
 * every probe downstream of it into a silent false green. A spurious difference costs a look.
 */
public final class Canon {

    private Canon() {
    }

    /** Canonical form of a state made of maps, lists and scalars: the comparison key. */
    public static Object canon(Object state) {
        return reduce(state);
    }

    /** Canonical form of a record's state. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> canon(Map<String, Object> state) {
        return (Map<String, Object>) reduce(state);
    }

    /** Whether two states are the same. */
    public static boolean equal(Object a, Object b) {
        return reduce(a).equals(reduce(b));
    }

    private static Object reduce(Object value) {
        if (value instanceof Map) {
            Map<?, ?> source = (Map<?, ?>) value;
            List<String> keys = new ArrayList<>();
            for (Object key : source.keySet()) keys.add(String.valueOf(key));
            Collections.sort(keys); // R5
            Map<String, Object> out = new LinkedHashMap<>();
            for (String key : keys) {
                if (isProtocolKey(key)) continue; // R1
                Object reduced = reduce(source.get(key));
                if (isTypeDefault(reduced)) continue; // R2
                out.put(key, reduced);
            }
            return out;
        }
        if (value instanceof List) { // R6 - order preserved
            List<Object> out = new ArrayList<>();
            for (Object item : (List<?>) value) out.add(reduce(item));
            return out;
        }
        if (value instanceof Number) {
            double d = ((Number) value).doubleValue(); // R4
            if (d == Math.rint(d) && !Double.isInfinite(d)) return (long) d;
            return d;
        }
        return value;
    }

    /**
     * Bookkeeping about the message rather than the widget: the write stamp, and anything the
     * underscore convention marks as protocol - which state can never collide with, since a
     * property name never starts with one.
     */
    private static boolean isProtocolKey(String key) {
        return "_s".equals(key) || key.startsWith("_");
    }

    /**
     * Values both codecs elide when writing, so absent and default-valued must compare equal.
     * Deliberately excludes the empty string and the empty object (R3): a sender that writes one
     * means something by it, and the bias rule sends every arguable case the strict way.
     */
    private static boolean isTypeDefault(Object value) {
        if (value == null) return true;
        if (value instanceof List) return ((List<?>) value).isEmpty();
        if (value instanceof Boolean) return !(Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() == 0d;
        return false;
    }
}
