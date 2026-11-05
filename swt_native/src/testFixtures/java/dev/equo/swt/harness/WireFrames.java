package dev.equo.swt.harness;

import dev.equo.swt.WidgetWire;
import dev.equo.swt.WireDump;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A binary widget frame ({@link WidgetWire}) as records a test can assert on by channel and property
 * name (see {@link WireValues}): a widget's record on {@code Type/id}, a change naming what it changed
 * in {@code _d}, write stamps in {@code _s} and {@code _b}, a widget it names as
 * {@code {id, swt, _r: true}}.
 */
public final class WireFrames {

    private WireFrames() {
    }

    /** One record of a frame: the channel it is for and what it says. */
    public record Record(String channel, Map<String, Object> state, int size) {
    }

    /**
     * {@code properties} as decoded by the wire definitions; fails on any the definitions could not
     * read, which is the writer and the definitions disagreeing.
     */
    private static Map<String, Object> readable(Object of, Map<String, Object> properties) {
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            if (e.getKey().startsWith("?")) {
                throw new AssertionError(of + ": property " + e.getKey().substring(1) + " is " + e.getValue());
            }
        }
        return properties;
    }

    /** The records of the frame body, in order. */
    @SuppressWarnings("unchecked")
    public static List<Record> records(byte[] body) {
        // Read once first, so a reference to a widget described further down the frame has its type.
        WireDump.records(body, 0, body.length);
        List<Record> out = new ArrayList<>();
        for (WireDump.Record r : WireDump.records(body, 0, body.length)) {
            Map<String, Object> state = new java.util.LinkedHashMap<>();
            if (r.kind == WidgetWire.WHOLE || r.kind == WidgetWire.CHANGE) {
                state.put("id", r.id);
                state.put("swt", r.type);
            }
            state.putAll((Map<String, Object>) WireValues.plain(readable(r.channel(), r.properties)));
            if (r.kind == WidgetWire.CHANGE) {
                state.put("_d", new ArrayList<>(r.properties.keySet()));
                state.put("_b", r.base);
            }
            if (r.kind != WidgetWire.IMAGE) state.put("_s", r.stamp);
            String channel = r.kind != WidgetWire.HOLDER ? r.channel()
                    : "ShellPopups".equals(r.type) ? "Shell/" + r.id + "/Popups"
                    : "DisplayPopups".equals(r.type) ? "Display/" + r.id + "/Popups" : r.channel();
            out.add(new Record(channel, state, r.size));
        }
        return out;
    }

    /** A whole frame as it travelled: every channel it carries, a batch's entries included, and its widget records. */
    public record Travelled(List<String> channels, List<Record> records) {

        /** The state of the record for {@code widget}, or null when the frame carries none. */
        public Map<String, Object> stateOf(Object widget) {
            String id = "/" + widget.hashCode();
            for (Record record : records) if (record.channel().endsWith(id)) return record.state();
            return null;
        }

        /** Whether some record says something about {@code key}, at any depth. */
        public boolean mentions(String key) {
            for (Record record : records) if (WireValues.mentions(record.state(), key)) return true;
            return false;
        }
    }

    /** {@code frame}, channel header and body, decoded. */
    public static Travelled travelled(byte[] frame) {
        dev.equo.swt.comm.ChannelHeader.Decoded header = dev.equo.swt.comm.ChannelHeader.decode(frame, 0, frame.length);
        byte[] body = java.util.Arrays.copyOfRange(frame, header.payloadStart(), frame.length);
        if (WidgetWire.CHANNEL.equals(header.channel())) return new Travelled(List.of(header.channel()), records(body));
        List<String> channels = new ArrayList<>();
        channels.add(header.channel());
        GcFrames.Decoded decoded = GcFrames.decode(header.channel(), body);
        if (decoded.value() instanceof List<?> entries) {
            for (Object e : entries) if (e instanceof GcFrames.Entry entry) channels.add(entry.channel());
        } else if (!decoded.event().equals(header.channel())) {
            channels.add(decoded.event());
        }
        return new Travelled(channels, List.of());
    }

    /**
     * A whole frame as it travelled (channel header and body) as text: a binary frame of widget records
     * as its channel followed by each record's channel and state, any other frame as its UTF-8 bytes.
     */
    public static String readable(byte[] frame) {
        dev.equo.swt.comm.ChannelHeader.Decoded header = dev.equo.swt.comm.ChannelHeader.decode(frame, 0, frame.length);
        String channel = header.channel();
        if (!WidgetWire.CHANNEL.equals(channel)) {
            return channel + new String(frame, header.payloadStart(), frame.length - header.payloadStart(),
                    java.nio.charset.StandardCharsets.UTF_8);
        }
        byte[] body = java.util.Arrays.copyOfRange(frame, header.payloadStart(), frame.length);
        StringBuilder text = new StringBuilder(channel);
        for (Record record : records(body)) text.append(' ').append(record.channel()).append(' ').append(record.state());
        return text.toString();
    }

    /**
     * {@code subject} as the wire describes it, as {@link #records} gives a record: a widget or an
     * editor by its record, a resource or another object by its fields.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> describe(Object subject) {
        if (subject instanceof org.eclipse.swt.widgets.Widget
                && ((org.eclipse.swt.widgets.Widget) subject).getImpl() instanceof org.eclipse.swt.widgets.DartWidget) {
            return whole((org.eclipse.swt.widgets.Widget) subject);
        }
        if (subject instanceof org.eclipse.swt.custom.ControlEditor) {
            org.eclipse.swt.custom.ControlEditor editor = (org.eclipse.swt.custom.ControlEditor) subject;
            dev.equo.swt.Serializer.Tape tape = new dev.equo.swt.Serializer.Tape(0);
            tape.describeEditor(editor);
            dev.equo.swt.Serializer.discardWritten();
            String id = "/" + editor.getImpl().hashCode();
            for (Record record : records(tape.frame().payload())) {
                if (record.channel().endsWith(id)) return record.state();
            }
            throw new AssertionError("no record for " + editor);
        }
        Object value = subject;
        // A data class (a FontData) travels as its impl's value class.
        try {
            Object impl = subject.getClass().getMethod("getImpl").invoke(subject);
            if (impl != null && !(subject instanceof org.eclipse.swt.graphics.Resource)) {
                Object implValue = impl.getClass().getMethod("getValue").invoke(impl);
                if (implValue instanceof dev.equo.swt.WireObject) value = implValue;
            }
        } catch (ReflectiveOperationException none) {
            // Not a data class with a value class of its own.
        }
        if (subject instanceof org.eclipse.swt.graphics.Resource
                && ((org.eclipse.swt.graphics.Resource) subject).getImpl() instanceof org.eclipse.swt.graphics.DartResource) {
            value = ((org.eclipse.swt.graphics.DartResource) ((org.eclipse.swt.graphics.Resource) subject).getImpl()).getValue();
        }
        if (value instanceof org.eclipse.swt.graphics.VResource) {
            byte[] fields = dev.equo.swt.Serializer.wireFields((org.eclipse.swt.graphics.VResource) value);
            return (Map<String, Object>) WireValues.plain(readable(subject, WireDump.fields(fields, 0, fields.length)));
        }
        if (value instanceof dev.equo.swt.WireObject) {
            byte[] fields = WidgetWire.fieldsOf((dev.equo.swt.WireObject) value);
            return (Map<String, Object>) WireValues.plain(readable(subject, WireDump.fields(fields, 0, fields.length)));
        }
        throw new IllegalArgumentException("no wire form for " + subject.getClass().getName());
    }

    /**
     * {@code impl}'s record for the client {@code connection}, as {@link #records} gives it: what
     * changed, or the whole widget when that client holds nothing of it; null when there is nothing
     * to send. Credited as delivered, as a flush credits it.
     */
    public static Map<String, Object> record(org.eclipse.swt.widgets.DartWidget impl, int connection) {
        dev.equo.swt.Serializer.Tape tape = new dev.equo.swt.Serializer.Tape(connection);
        try {
            tape.describe(impl);
            String id = "/" + impl.getApi().hashCode();
            for (Record record : records(tape.frame().payload())) {
                if (record.channel().endsWith(id)) return record.state();
            }
            return null;
        } finally {
            tape.release();
        }
    }

    /** Every record of the frame describing {@code impl} for {@code connection}. */
    public static List<Record> frame(org.eclipse.swt.widgets.DartWidget impl, int connection) {
        dev.equo.swt.Serializer.Tape tape = new dev.equo.swt.Serializer.Tape(connection);
        try {
            tape.describe(impl);
            return records(tape.frame().payload());
        } finally {
            tape.release();
        }
    }

    /**
     * The names of every property a record describing {@code subject} can hold: its value class's
     * {@code wireKeys()} by the names {@code wireName} gives them, or, for a value written by hand,
     * the properties the generated wire table gives its class and the classes it extends.
     */
    public static java.util.Set<String> propertiesOf(Object subject) throws ReflectiveOperationException {
        Object value = valueOf(subject);
        java.util.Set<String> names = new java.util.TreeSet<>();
        try {
            java.lang.reflect.Method wireName = value.getClass().getMethod("wireName", int.class);
            for (int key : (int[]) value.getClass().getMethod("wireKeys").invoke(value)) {
                names.add((String) wireName.invoke(value, key));
            }
            return names;
        } catch (NoSuchMethodException written) {
            // A value written by hand.
        }
        java.util.Set<String> classes = new java.util.HashSet<>();
        for (Class<?> c = value.getClass(); c != null; c = c.getSuperclass()) {
            String n = c.getSimpleName();
            classes.add(n.length() > 1 && n.charAt(0) == 'V' && Character.isUpperCase(n.charAt(1)) ? n.substring(1) : n);
        }
        for (int i = 1; i < dev.equo.swt.WireTable.PROPERTIES.length; i++) {
            if (dev.equo.swt.WireTable.KINDS[i] == null) continue;
            String qualified = dev.equo.swt.WireTable.PROPERTIES[i];
            int dot = qualified.lastIndexOf('.');
            if (classes.contains(qualified.substring(0, dot))) names.add(qualified.substring(dot + 1));
        }
        return names;
    }

    /** Property names by number. */
    private static final Map<String, String> NAMES = new java.util.HashMap<>();

    static {
        for (int i = 1; i < dev.equo.swt.WireTable.PROPERTIES.length; i++) {
            String qualified = dev.equo.swt.WireTable.PROPERTIES[i];
            NAMES.put(String.valueOf(i), qualified.substring(qualified.lastIndexOf('.') + 1));
        }
    }

    /** The number of the property {@code qualified} ({@code Class.name}). */
    public static int property(String qualified) {
        for (int i = 1; i < dev.equo.swt.WireTable.PROPERTIES.length; i++) {
            if (dev.equo.swt.WireTable.PROPERTIES[i].equals(qualified)) return i;
        }
        throw new IllegalArgumentException("no property " + qualified);
    }

    /**
     * {@code state} as the client describes it (keyed by property number, see the client's
     * {@code describeState}) with each number replaced by the property's name, at any depth: a
     * number is one property of one class, so it names one thing wherever it appears.
     */
    @SuppressWarnings("unchecked")
    public static Object named(Object state) {
        if (state instanceof Map) {
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, Object> e : ((Map<String, Object>) state).entrySet()) {
                out.put(NAMES.getOrDefault(e.getKey(), e.getKey()), named(e.getValue()));
            }
            return out;
        }
        if (state instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object e : (List<Object>) state) out.add(named(e));
            return out;
        }
        return state;
    }

    /** The value object the wire describes {@code subject} by, or {@code subject} when it is one. */
    private static Object valueOf(Object subject) throws ReflectiveOperationException {
        if (subject instanceof org.eclipse.swt.widgets.Widget || subject instanceof org.eclipse.swt.custom.ControlEditor
                || subject instanceof org.eclipse.swt.graphics.Resource) {
            Object impl = subject.getClass().getMethod("getImpl").invoke(subject);
            return impl.getClass().getMethod("getValue").invoke(impl);
        }
        try {
            Object impl = subject.getClass().getMethod("getImpl").invoke(subject);
            Object value = impl == null ? null : impl.getClass().getMethod("getValue").invoke(impl);
            if (value instanceof dev.equo.swt.WireObject) return value;
        } catch (NoSuchMethodException none) {
            // Not a data class with a value class of its own.
        }
        return subject;
    }

    /** {@code widget} described whole, as {@link #records} gives its record. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> whole(org.eclipse.swt.widgets.Widget widget) {
        dev.equo.swt.Serializer.Tape tape = new dev.equo.swt.Serializer.Tape(0);
        tape.describe((org.eclipse.swt.widgets.DartWidget) widget.getImpl());
        dev.equo.swt.Serializer.discardWritten();
        String id = "/" + widget.hashCode();
        List<Record> records = records(tape.frame().payload());
        // What the record names and the client lacks (an image, a new child) is described by a
        // record of its own in the same frame: shown in place of the name, as what the client
        // resolves it to.
        Map<Long, Map<String, Object>> described = new java.util.HashMap<>();
        for (Record record : records) {
            int slash = record.channel().lastIndexOf('/');
            if (record.channel().endsWith(id) || slash < 0) continue;
            try {
                described.put(Long.parseLong(record.channel().substring(slash + 1)), record.state());
            } catch (NumberFormatException notAnId) {
                // A holder's channel, which nothing names.
            }
        }
        for (Record record : records) {
            if (record.channel().endsWith(id)) {
                return described.isEmpty() ? record.state() : (Map<String, Object>) resolved(record.state(), described);
            }
        }
        throw new AssertionError("no record for " + widget);
    }

    /** {@code v} with each name of something {@code described} replaced by its description, one level deep. */
    @SuppressWarnings("unchecked")
    private static Object resolved(Object v, Map<Long, Map<String, Object>> described) {
        if (v instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object item : (List<Object>) v) out.add(resolved(item, described));
            return out;
        }
        if (!(v instanceof Map)) return v;
        Map<String, Object> o = (Map<String, Object>) v;
        if (o.containsKey("_r") && o.get("id") instanceof Number
                && described.containsKey(((Number) o.get("id")).longValue())) {
            Map<String, Object> out = new java.util.LinkedHashMap<>(o);
            out.putAll(described.get(((Number) o.get("id")).longValue()));
            return out;
        }
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        o.forEach((k, f) -> out.put(k, resolved(f, described)));
        return out;
    }
}
