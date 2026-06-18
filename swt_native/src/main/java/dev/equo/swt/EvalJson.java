package dev.equo.swt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.ObjectConverter;

/**
 * Minimal JSON &harr; Java value mapping for the SWT Browser scripting bridge
 * (both {@code Browser.evaluate} and {@code BrowserFunction}). It implements the
 * SWT type contract: JS {@code null}/{@code undefined} &harr; {@code null},
 * boolean &harr; {@link Boolean}, number &harr; {@link Double}, string &harr;
 * {@link String}, array &harr; {@code Object[]} (elements mapped recursively).
 * JS objects, which have no SWT mapping, parse to a {@link java.util.Map}.
 *
 * <p>The actual JSON tokenizing/writing is dsl-json's generic
 * {@link ObjectConverter} (already a project dependency); the only thing here is
 * normalising dsl-json's generic shape (numbers as {@code long}/{@code double},
 * arrays as {@link List}) onto the SWT contract ({@link Double} / {@code Object[]}).
 */
public final class EvalJson {

    private EvalJson() {
    }

    /** A bare dsl-json instance used only as a factory for readers/writers. */
    private static final DslJson<Object> DSL = new DslJson<>();

    /**
     * Parses a JSON value into its SWT-contract Java type. Returns {@code null}
     * for {@code null}/malformed input.
     */
    public static Object parse(String json) {
        if (json == null) return null;
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        try {
            JsonReader<Object> reader = DSL.newReader(bytes);
            reader.getNextToken();
            return toSwt(ObjectConverter.deserializeObject(reader));
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    /**
     * Encodes a Java value (as produced by a {@code BrowserFunction}) to JSON for
     * the JavaScript caller. Handles {@code null}/{@link Boolean}/{@link Number}/
     * {@link String}/{@code Object[]}/{@link java.util.Collection}/{@link java.util.Map}.
     */
    public static String encode(Object value) {
        JsonWriter writer = DSL.newWriter();
        try {
            ObjectConverter.serializeObject(fromSwt(value), writer);
        } catch (IOException ex) {
            return "null";
        }
        return new String(writer.toByteArray(), StandardCharsets.UTF_8);
    }

    /** dsl-json's generic shape &rarr; SWT contract: number &rarr; Double, array &rarr; Object[]. */
    private static Object toSwt(Object v) {
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        if (v instanceof List) {
            List<?> list = (List<?>) v;
            Object[] arr = new Object[list.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = toSwt(list.get(i));
            return arr;
        }
        if (v instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) v;
            LinkedHashMap<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) out.put(String.valueOf(e.getKey()), toSwt(e.getValue()));
            return out;
        }
        return v; // String, Boolean, null
    }

    /** SWT contract &rarr; a shape {@link ObjectConverter#serializeObject} handles: Object[] &rarr; List. */
    private static Object fromSwt(Object v) {
        if (v instanceof Object[]) {
            Object[] a = (Object[]) v;
            ArrayList<Object> list = new ArrayList<>(a.length);
            for (Object e : a) list.add(fromSwt(e));
            return list;
        }
        if (v instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) v;
            LinkedHashMap<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) out.put(String.valueOf(e.getKey()), fromSwt(e.getValue()));
            return out;
        }
        return v;
    }
}
