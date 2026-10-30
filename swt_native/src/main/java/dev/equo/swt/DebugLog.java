package dev.equo.swt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Formats and prints the {@code send:} (outgoing), {@code recv:} (incoming) and {@code chk:}
 * (control-flow checkpoint) debug lines emitted by {@link FlutterBridge}. Together they give a
 * two-directional trace of the Java↔Flutter bridge on the real app once {@code dev.equo.swt.debug}
 * is on — no per-bug rebuild needed to see whether an event arrived, was dispatched, or took effect.
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
    private static final Set<String> REDACT_KEYS = Java8.set("data", "alphaData", "svgContent");

    /** Explicit field names to strip (from {@code simple} + {@code hide}); color matching is separate. */
    private static final Set<String> HIDE_KEYS = buildHideKeys();

    private DebugLog() {
    }

    /** Prints a {@code send:} line for this event/payload when debugging is on and the event passes filters. */
    static void logSend(String eventName, byte[] bytes, int offset, int length) {
        if (!shouldLog(eventName)) return;
        if (!dev.equo.swt.comm.TaggedValues.isTagged(bytes, offset, length)) {
            // Bytes with a form of their own (an image's, a widget frame's): only their size says anything here.
            System.out.println("send: " + eventName + ": <" + length + " B>");
            return;
        }
        Object tree;
        try {
            tree = dev.equo.swt.comm.TaggedValues.decode(bytes, offset, length);
        } catch (RuntimeException e) {
            // Never let debug logging break a send.
            System.out.println("send: " + eventName + ": <" + length + " B, unreadable: " + e + ">");
            return;
        }
        print("send: " + eventName + (SUMMARY ? " (" + length + " B)" : ""), tree);
    }

    /**
     * A frame of widget records, one {@code send:} line per record: what it is ({@code whole}, a
     * {@code change} with the stamp it was computed from), the widget, and its properties by name.
     * A whole record leaves out every property still at the client's default.
     */
    static void logWidgetFrame(byte[] payload) {
        List<WireDump.Record> records;
        try {
            records = WireDump.records(payload, 0, payload.length);
        } catch (RuntimeException e) {
            System.out.println("send: " + WidgetWire.CHANNEL + ": <" + payload.length + " B, unreadable: " + e + ">");
            return;
        }
        for (WireDump.Record r : records) {
            String channel = r.channel();
            if (!shouldLog(channel)) continue;
            String kind;
            switch (r.kind) {
                case WidgetWire.WHOLE: kind = "whole _s=" + r.stamp; break;
                case WidgetWire.CHANGE: kind = "change _s=" + r.stamp + " _b=" + r.base; break;
                case WidgetWire.IMAGE: kind = "image"; break;
                case WidgetWire.HOLDER: kind = "holder"; break;
                default: kind = "kind " + r.kind;
            }
            print("send: " + channel + " (" + kind + ")", new LinkedHashMap<String, Object>(r.properties));
        }
    }

    private static void print(String header, Object decoded) {
        Object tree = mutableCopy(decoded);
        prune(tree);
        StringBuilder sb = new StringBuilder();
        render(tree, 0, sb);
        String body = sb.toString();
        if (MAX_LEN > 0 && body.length() > MAX_LEN) {
            body = body.substring(0, MAX_LEN) + "…(+" + (body.length() - MAX_LEN) + " chars)";
        }
        System.out.println(header + ": " + body);
    }

    /** A GC's binary frame, one {@code send:} line per record. */
    static void logGcFrame(GcWire frame) {
        if (!shouldLog(GcWire.frameEvent(frame.channel()))) return;
        for (GcWire.Entry entry : frame.entries()) {
            prune(entry.value);
            StringBuilder sb = new StringBuilder();
            render(entry.value, 0, sb);
            System.out.println("send: " + entry.channel + ": " + sb);
        }
    }

    /**
     * Prints a {@code recv:} line for an incoming Flutter→Java event — the symmetric counterpart of
     * {@link #logSend}. Gated behind the master {@code dev.equo.swt.debug} switch and the same name
     * filters, so the two directions line up by event name in the log.
     */
    static void logRecv(String eventName, Object event) {
        if (!shouldLog(eventName)) return;
        System.out.println("recv: " + eventName + ": " + event);
    }

    /** Why a widget travelled whole; in the log a whole frame only lacks {@code _d}. */
    static void logWholeSend(String eventName, String reason) {
        if (!shouldLog(eventName)) return;
        System.out.println("whole: " + eventName + ": " + reason);
    }

    /** {@link #logRecv} for a raw payload receiver (no {@code V*} object to render). */
    static void logRecvPayload(String eventName, String payload) {
        if (!shouldLog(eventName)) return;
        System.out.println("recv: " + eventName + ": " + payload);
    }

    /**
     * Prints a {@code chk:} control-flow checkpoint — a decision point on the incoming path (an early
     * return taken, a setter invoked, a computed value at selection time). Same gating/filtering as the
     * {@code send:}/{@code recv:} lines (so {@code debug.filter}/{@code debug.exclude} narrow it too),
     * and zero cost when debugging is off. On the real app this is what distinguishes "event never
     * arrived" (no {@code recv:}) from "arrived but early-returned" (a {@code chk:} skip) from "ran but
     * a computed value was off" (a {@code chk:} with that value, and no follow-up {@code send:}).
     */
    static void checkpoint(String tag, Object... details) {
        if (!shouldLog(tag)) return;
        StringBuilder sb = new StringBuilder("chk: ").append(tag);
        for (int i = 0; details != null && i < details.length; i++) {
            sb.append(i == 0 ? ": " : " ").append(details[i]);
        }
        System.out.println(sb);
    }

    /** Whether the name filters let {@code eventName} through; reached only from {@link DebugTap}. */
    private static boolean shouldLog(String eventName) {
        if (EXCLUDE != null && EXCLUDE.matcher(eventName).find()) return false;
        if (FILTER != null && !FILTER.matcher(eventName).find()) return false;
        return true;
    }

    private static boolean isHidden(String key) {
        if (HIDE_KEYS.contains(key)) return true;
        if (HIDE_COLORS) {
            String k = key.toLowerCase(java.util.Locale.ROOT);
            return k.contains("color") || k.contains("foreground") || k.contains("background");
        }
        return false;
    }

    // ---- tree pruning ------------------------------------------------------

    /** {@code node} with every map and list in it copied, so pruning leaves what was decoded alone. */
    private static Object mutableCopy(Object node) {
        if (node instanceof Map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) node).entrySet()) copy.put(String.valueOf(e.getKey()), mutableCopy(e.getValue()));
            return copy;
        }
        if (node instanceof List) {
            List<Object> copy = new ArrayList<>();
            for (Object v : (List<?>) node) copy.add(mutableCopy(v));
            return copy;
        }
        return node;
    }

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
            out.append(scalar(node));
        }
    }

    /** A decoded value that is no map or list, as the log prints it. */
    private static String scalar(Object v) {
        if (v == null) return "null";
        if (v instanceof CharSequence) return "\"" + v + "\"";
        if (v instanceof byte[]) return "\"" + WireDump.describe((byte[]) v) + "\"";
        if (v instanceof int[]) return java.util.Arrays.toString((int[]) v);
        if (v instanceof long[]) return java.util.Arrays.toString((long[]) v);
        if (v instanceof double[]) return java.util.Arrays.toString((double[]) v);
        if (v instanceof boolean[]) return java.util.Arrays.toString((boolean[]) v);
        if (v instanceof Object[]) return java.util.Arrays.deepToString((Object[]) v);
        return String.valueOf(v);
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
        if (!(value instanceof Map) && !(value instanceof List) && REDACT_KEYS.contains(key)) {
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
        return Java8.repeat("  ", level);
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
}
