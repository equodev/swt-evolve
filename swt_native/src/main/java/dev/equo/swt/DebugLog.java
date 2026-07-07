package dev.equo.swt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Formats and prints the {@code send:} debug lines emitted by {@link FlutterBridge}.
 *
 * <p>The serialized payload is parsed into a generic {@code Map}/{@code List} tree, pruned, and
 * re-rendered — so field filtering is a plain {@code map.remove(key)} and pretty-printing is
 * structural (no regex on the raw string).
 *
 * <p>Controlled by system properties (all gated behind the master {@code dev.equo.swt.debug} switch):
 * <ul>
 *   <li>{@code debug.filter} — regex; only events whose name matches are printed.</li>
 *   <li>{@code debug.exclude} — regex; events whose name matches are suppressed (wins over filter).</li>
 *   <li>{@code debug.maxLen} — truncate the body to N chars (0 = unlimited).</li>
 *   <li>{@code debug.summary} — prepend the payload size in bytes to the header.</li>
 *   <li>{@code debug.simple} — strip a default set of noisy fields (bounds, style, colors, …).</li>
 *   <li>{@code debug.hideColors} — strip every field whose name contains color/foreground/background.</li>
 *   <li>{@code debug.hide} — comma-separated extra field names to strip.</li>
 *   <li>{@code debug.pretty} — indent the tree, keeping same-level scalar fields on one line.</li>
 * </ul>
 */
final class DebugLog {

    private static final Pattern FILTER = compile(System.getProperty("dev.equo.swt.debug.filter"));
    private static final Pattern EXCLUDE = compile(System.getProperty("dev.equo.swt.debug.exclude"));
    private static final int MAX_LEN = Integer.getInteger("dev.equo.swt.debug.maxLen", 0);
    private static final boolean SUMMARY = Boolean.getBoolean("dev.equo.swt.debug.summary");
    private static final boolean SIMPLE = Boolean.getBoolean("dev.equo.swt.debug.simple");
    private static final boolean HIDE_COLORS = Boolean.getBoolean("dev.equo.swt.debug.hideColors");
    private static final boolean PRETTY = Boolean.getBoolean("dev.equo.swt.debug.pretty");

    /** Fields hidden by {@link #SIMPLE}. */
    private static final String[] SIMPLE_KEYS = {
            "bounds", "orientation", "style", "imageData",
            "background", "foreground", "textDirection", "maximum",
            "dragDetect", "redraw", "gradientColors", "linkForeground",
            "alignment", "headerBackground", "increment", "pageIncrement",
            "thumb", "selectionForeground", "topRightAlignment", "MRUVisible",
            "backgroundMode", "selectionBackground", "headerForeground", "fontData"
    };

    /** Binary/heavy string fields whose value is replaced by {@code "-ignore-"} instead of stripped. */
    private static final Set<String> REDACT_KEYS = Set.of("data", "alphaData", "svgContent");

    /** Explicit field names to strip (from {@code simple} + {@code hide}); color matching is separate. */
    private static final Set<String> HIDE_KEYS = buildHideKeys();

    private DebugLog() {
    }

    /** Prints a {@code send:} line for this event/payload when debugging is on and the event passes filters. */
    static void logSend(String eventName, byte[] bytes) {
        if (!shouldLog(eventName)) return;
        String body;
        try {
            Object tree = new JsonParser(new String(bytes, StandardCharsets.UTF_8)).parse();
            prune(tree);
            StringBuilder sb = new StringBuilder();
            render(tree, 0, sb);
            body = sb.toString();
        } catch (RuntimeException e) {
            // Never let debug logging break a send; fall back to the raw payload.
            body = new String(bytes, StandardCharsets.UTF_8);
        }
        if (MAX_LEN > 0 && body.length() > MAX_LEN) {
            body = body.substring(0, MAX_LEN) + "…(+" + (body.length() - MAX_LEN) + " chars)";
        }
        String size = SUMMARY ? " (" + bytes.length + " B)" : "";
        System.out.println("send: " + eventName + size + ": " + body);
    }

    private static boolean shouldLog(String eventName) {
        if (!Config.isDebug()) return false;
        if (EXCLUDE != null && EXCLUDE.matcher(eventName).find()) return false;
        if (FILTER != null && !FILTER.matcher(eventName).find()) return false;
        return true;
    }

    private static boolean isHidden(String key) {
        if (HIDE_KEYS.contains(key)) return true;
        if (HIDE_COLORS) {
            String k = key.toLowerCase();
            return k.contains("color") || k.contains("foreground") || k.contains("background");
        }
        return false;
    }

    // ---- tree pruning ------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static void prune(Object node) {
        if (node instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) node;
            map.keySet().removeIf(DebugLog::isHidden);
            for (Object v : map.values()) prune(v);
        } else if (node instanceof List) {
            for (Object v : (List<Object>) node) prune(v);
        }
    }

    // ---- rendering ---------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static void render(Object node, int indent, StringBuilder out) {
        if (node instanceof Map) {
            renderMap((Map<String, Object>) node, indent, out);
        } else if (node instanceof List) {
            renderList((List<Object>) node, indent, out);
        } else {
            out.append(((Raw) node).token); // verbatim scalar/string token
        }
    }

    private static void renderMap(Map<String, Object> map, int indent, StringBuilder out) {
        if (map.isEmpty()) {
            out.append("{}");
            return;
        }
        boolean block = PRETTY && hasContainer(map.values());
        out.append('{');
        if (!block) {
            boolean first = true;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!first) out.append(PRETTY ? ", " : ",");
                appendEntry(e, indent + 1, out);
                first = false;
            }
            out.append('}');
            return;
        }
        // Block mode: a maximal run of consecutive scalar fields shares one line;
        // each container-valued field goes on its own line.
        String pad = indent(indent + 1);
        boolean first = true;
        boolean inScalarRun = false;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            boolean container = isContainer(e.getValue());
            if (container || !inScalarRun) {
                if (!first) out.append(',');
                out.append('\n').append(pad);
                inScalarRun = !container;
            } else {
                out.append(", ");
            }
            appendEntry(e, indent + 1, out);
            first = false;
        }
        out.append('\n').append(indent(indent)).append('}');
    }

    private static void appendEntry(Map.Entry<String, Object> e, int indent, StringBuilder out) {
        out.append('"').append(e.getKey()).append(PRETTY ? "\": " : "\":");
        appendValue(e.getKey(), e.getValue(), indent, out);
    }

    private static void renderList(List<Object> list, int indent, StringBuilder out) {
        if (list.isEmpty()) {
            out.append("[]");
            return;
        }
        boolean block = PRETTY && hasContainer(list);
        out.append('[');
        String pad = indent(indent + 1);
        boolean first = true;
        for (Object v : list) {
            if (block) {
                if (!first) out.append(',');
                out.append('\n').append(pad);
            } else if (!first) {
                out.append(PRETTY ? ", " : ",");
            }
            render(v, indent + 1, out);
            first = false;
        }
        if (block) out.append('\n').append(indent(indent));
        out.append(']');
    }

    private static void appendValue(String key, Object value, int indent, StringBuilder out) {
        if (value instanceof Raw && REDACT_KEYS.contains(key)) {
            out.append("\"-ignore-\"");
        } else {
            render(value, indent, out);
        }
    }

    private static boolean isContainer(Object v) {
        return (v instanceof Map && !((Map<?, ?>) v).isEmpty())
                || (v instanceof List && !((List<?>) v).isEmpty());
    }

    private static boolean hasContainer(Iterable<Object> values) {
        for (Object v : values) if (isContainer(v)) return true;
        return false;
    }

    private static String indent(int level) {
        return "  ".repeat(level);
    }

    // ---- config helpers ----------------------------------------------------

    private static Set<String> buildHideKeys() {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        if (SIMPLE) {
            for (String k : SIMPLE_KEYS) keys.add(k);
        }
        String extra = System.getProperty("dev.equo.swt.debug.hide");
        if (extra != null && !extra.isEmpty()) {
            for (String k : extra.split(",")) {
                k = k.trim();
                if (!k.isEmpty()) keys.add(k);
            }
        }
        return keys;
    }

    private static Pattern compile(String regex) {
        return (regex == null || regex.isEmpty()) ? null : Pattern.compile(regex);
    }

    // ---- minimal JSON parser ----------------------------------------------
    // Produces LinkedHashMap (objects, order preserved), ArrayList (arrays), and Raw (scalars +
    // strings, kept as their verbatim token so re-emitting needs no re-escaping).

    /** A scalar or string kept as its verbatim JSON token. */
    private static final class Raw {
        final String token;

        Raw(String token) {
            this.token = token;
        }
    }

    private static final class JsonParser {
        private final String s;
        private int pos;

        JsonParser(String s) {
            this.s = s;
        }

        Object parse() {
            skipWs();
            Object v = value();
            return v;
        }

        private Object value() {
            skipWs();
            char c = s.charAt(pos);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return new Raw(string());
            return new Raw(literal());
        }

        private Map<String, Object> object() {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            pos++; // {
            skipWs();
            if (s.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWs();
                String key = unquote(string());
                skipWs();
                pos++; // :
                map.put(key, value());
                skipWs();
                char c = s.charAt(pos++);
                if (c == ',') continue;
                if (c == '}') break;
                throw new IllegalStateException("expected , or } at " + (pos - 1));
            }
            return map;
        }

        private List<Object> array() {
            ArrayList<Object> list = new ArrayList<>();
            pos++; // [
            skipWs();
            if (s.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(value());
                skipWs();
                char c = s.charAt(pos++);
                if (c == ',') continue;
                if (c == ']') break;
                throw new IllegalStateException("expected , or ] at " + (pos - 1));
            }
            return list;
        }

        /** Reads a JSON string, returning the verbatim token including surrounding quotes. */
        private String string() {
            int start = pos;
            pos++; // opening "
            while (true) {
                char c = s.charAt(pos++);
                if (c == '\\') {
                    pos++; // skip escaped char
                } else if (c == '"') {
                    break;
                }
            }
            return s.substring(start, pos);
        }

        /** Reads a number / true / false / null token verbatim. */
        private String literal() {
            int start = pos;
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == ',' || c == '}' || c == ']' || c == ' ' || c == '\n' || c == '\r' || c == '\t') break;
                pos++;
            }
            return s.substring(start, pos);
        }

        private void skipWs() {
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') pos++;
                else break;
            }
        }

        private static String unquote(String token) {
            // Field names in our payloads have no escapes; strip the surrounding quotes.
            return token.substring(1, token.length() - 1);
        }
    }
}
