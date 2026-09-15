package dev.equo.swt.delivery;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The canonical form of a serialized widget payload, Java side.
 *
 * <p>Every convergence check in the delivery suite ends in "are these two states the same", and the
 * two states being compared came from different code paths - a full send against a merged sequence
 * of diffs, or Java's view against the client's. They are equal in meaning long before they are
 * equal as text: one side elides a default the other writes out, key order differs, a write stamp
 * advanced. This reduces both to a form where equal-in-meaning is equal-as-text.
 *
 * <p>The rules are stated once, in {@code flutter-lib/test/delivery/canon_cases.json}, and
 * implemented twice - here and in Dart's {@code test/delivery/support/canon.dart}. Neither
 * implementation may be used to test the other; the shared case file is what keeps them honest, and
 * both emit the same bytes for the same state so an end-to-end probe can compare across languages.
 *
 * <p>One bias governs every arguable rule: when it is unclear whether a difference is real,
 * <strong>report a difference</strong>. A canonicalizer that normalizes away a real change turns
 * every probe downstream of it into a silent false green. A spurious difference costs a look.
 */
public final class Canon {

    private Canon() {
    }

    /** Canonical form of a JSON payload, as compact text - the comparison key. */
    public static String canon(String json) {
        return write(reduce(JsonParser.parseString(json)));
    }

    /** Whether two payloads carry the same state. */
    public static boolean equal(String a, String b) {
        return canon(a).equals(canon(b));
    }

    private static JsonElement reduce(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject source = element.getAsJsonObject();
            List<String> keys = new ArrayList<>(source.keySet());
            Collections.sort(keys); // R5
            JsonObject out = new JsonObject();
            for (String key : keys) {
                if (isProtocolKey(key)) continue; // R1
                JsonElement value = reduce(source.get(key));
                if (isTypeDefault(value)) continue; // R2
                out.add(key, value);
            }
            return out;
        }
        if (element.isJsonArray()) { // R6 - order preserved
            JsonArray out = new JsonArray();
            for (JsonElement item : element.getAsJsonArray()) out.add(reduce(item));
            return out;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            double d = element.getAsDouble(); // R4
            if (d == Math.rint(d) && !Double.isInfinite(d)) return new JsonPrimitive((long) d);
        }
        return element;
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
    private static boolean isTypeDefault(JsonElement value) {
        if (value.isJsonNull()) return true;
        if (value.isJsonArray()) return value.getAsJsonArray().isEmpty();
        if (!value.isJsonPrimitive()) return false;
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) return !primitive.getAsBoolean();
        if (primitive.isNumber()) return primitive.getAsDouble() == 0d;
        return false;
    }

    /**
     * Compact JSON, written here rather than handed to Gson so the bytes match Dart's
     * {@code json.encode} exactly - Gson's own writer escapes a different set of characters, which
     * would make two identical states compare unequal across the language boundary.
     */
    private static String write(JsonElement element) {
        StringBuilder out = new StringBuilder();
        write(element, out);
        return out.toString();
    }

    private static void write(JsonElement element, StringBuilder out) {
        if (element.isJsonNull()) {
            out.append("null");
        } else if (element.isJsonObject()) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (!first) out.append(',');
                first = false;
                writeString(entry.getKey(), out);
                out.append(':');
                write(entry.getValue(), out);
            }
            out.append('}');
        } else if (element.isJsonArray()) {
            out.append('[');
            boolean first = true;
            for (JsonElement item : element.getAsJsonArray()) {
                if (!first) out.append(',');
                first = false;
                write(item, out);
            }
            out.append(']');
        } else {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) writeString(primitive.getAsString(), out);
            else if (primitive.isBoolean()) out.append(primitive.getAsBoolean());
            else out.append(number(primitive));
        }
    }

    private static String number(JsonPrimitive primitive) {
        double d = primitive.getAsDouble();
        return d == Math.rint(d) && !Double.isInfinite(d)
                ? Long.toString((long) d)
                : primitive.getAsNumber().toString();
    }

    /** JSON string escaping as Dart's {@code json.encode} writes it. */
    private static void writeString(String value, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        out.append('"');
    }
}
