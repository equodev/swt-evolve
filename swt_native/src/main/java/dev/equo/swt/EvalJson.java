package dev.equo.swt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON as a web page's JavaScript reads and writes it, for the one place that talks to the page
 * directly: a {@code BrowserFunction} call the page makes by XHR. Values follow the SWT scripting
 * contract: JS {@code null}/{@code undefined} &harr; {@code null}, boolean &harr; {@link Boolean},
 * number &harr; {@link Double}, string &harr; {@link String}, array &harr; {@code Object[]} (elements
 * mapped recursively). JS objects, which have no SWT mapping, read as a {@link Map}.
 */
public final class EvalJson {

    private EvalJson() {
    }

    /** {@code json} as its SWT-contract Java value; {@code null} for {@code null} or malformed input. */
    public static Object parse(String json) {
        if (json == null) return null;
        try {
            Reader r = new Reader(json);
            Object v = r.value();
            r.space();
            return r.pos == json.length() ? toSwt(v) : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /**
     * A Java value (as a {@code BrowserFunction} returns it) as JSON for the JavaScript caller:
     * {@code null}, {@link Boolean}, {@link Number}, {@link String}, {@code Object[]},
     * {@link java.util.Collection} and {@link Map}; anything else as its {@code toString()}.
     */
    public static String encode(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    /** Maps, lists and scalars mapped onto the SWT contract: a number is a Double, a list an Object[]. */
    public static Object toSwt(Object v) {
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

    private static void write(Object v, StringBuilder out) {
        if (v == null) {
            out.append("null");
        } else if (v instanceof Boolean) {
            out.append(v);
        } else if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) out.append("null");
            else if (d == Math.rint(d) && Math.abs(d) < 1e15) out.append((long) d);
            else out.append(d);
        } else if (v instanceof Object[]) {
            out.append('[');
            Object[] a = (Object[]) v;
            for (int i = 0; i < a.length; i++) {
                if (i > 0) out.append(',');
                write(a[i], out);
            }
            out.append(']');
        } else if (v instanceof java.util.Collection) {
            write(((java.util.Collection<?>) v).toArray(), out);
        } else if (v instanceof Map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!first) out.append(',');
                first = false;
                string(String.valueOf(e.getKey()), out);
                out.append(':');
                write(e.getValue(), out);
            }
            out.append('}');
        } else {
            string(v.toString(), out);
        }
    }

    private static void string(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20 || c == 0x2028 || c == 0x2029) out.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        out.append('"');
    }

    private static final class Reader {
        final String s;
        int pos;

        Reader(String s) {
            this.s = s;
        }

        void space() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        Object value() {
            space();
            char c = s.charAt(pos);
            switch (c) {
                case '{': return object();
                case '[': return array();
                case '"': return string();
                case 't': return literal("true", Boolean.TRUE);
                case 'f': return literal("false", Boolean.FALSE);
                case 'n': return literal("null", null);
                default: return number();
            }
        }

        Object literal(String word, Object value) {
            if (!s.startsWith(word, pos)) throw new IllegalArgumentException("unexpected token at " + pos);
            pos += word.length();
            return value;
        }

        Map<String, Object> object() {
            Map<String, Object> out = new LinkedHashMap<>();
            pos++;
            space();
            if (s.charAt(pos) == '}') {
                pos++;
                return out;
            }
            while (true) {
                space();
                String key = string();
                space();
                expect(':');
                out.put(key, value());
                space();
                if (s.charAt(pos) == '}') {
                    pos++;
                    return out;
                }
                expect(',');
            }
        }

        List<Object> array() {
            List<Object> out = new ArrayList<>();
            pos++;
            space();
            if (s.charAt(pos) == ']') {
                pos++;
                return out;
            }
            while (true) {
                out.add(value());
                space();
                if (s.charAt(pos) == ']') {
                    pos++;
                    return out;
                }
                expect(',');
            }
        }

        String string() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (true) {
                char c = s.charAt(pos++);
                if (c == '"') return out.toString();
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                char e = s.charAt(pos++);
                switch (e) {
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'u':
                        out.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default: out.append(e);
                }
            }
        }

        Double number() {
            int start = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            if (start == pos) throw new IllegalArgumentException("unexpected token at " + pos);
            return Double.parseDouble(s.substring(start, pos));
        }

        void expect(char c) {
            if (s.charAt(pos) != c) throw new IllegalArgumentException("expected " + c + " at " + pos);
            pos++;
        }
    }
}
