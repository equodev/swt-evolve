package dev.equo.swt;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.StringConverter;
import com.dslplatform.json.runtime.FormatConverter;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;

import java.io.IOException;

public class Serializer {
    private static final byte[] name_id = "id".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] name_swt = "swt".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] name_style = "style".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] name_seq = "_s".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    /**
     * Marks a widget written as identity only: this is which widget, not what it holds.
     *
     * <p>Underscore like the other protocol keys, so it cannot collide with a property name.
     */
    private static final byte[] name_ref = "_r".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    /**
     * How deep the current walk is inside a frame. Zero is the widget the frame is about, which is
     * always described in full; anything deeper is a widget referenced by it, which need not be.
     */
    private static final ThreadLocal<int[]> depth = ThreadLocal.withInitial(() -> new int[1]);

    /**
     * Values that must be described in full on this pass, whatever their own state says.
     *
     * <p>A widget can be unchanged and still be the only way a change travels: the send path drops
     * a dirty widget whose ancestor is also being sent, because the ancestor's payload contains it.
     * Everything between the two is then load-bearing - naming one of them instead of describing it
     * would cut the path to the change, and the change would simply never arrive.
     *
     * <p>Set by the flush, which is the only thing that knows what it decided to drop.
     */
    private static final ThreadLocal<java.util.Set<VWidget>> describeInFull = new ThreadLocal<>();

    /**
     * Declares the values that have to be written out in full for the rest of this flush, because
     * something they contain is being delivered through them.
     */
    public static void describeInFull(java.util.Set<VWidget> values) {
        if (values == null || values.isEmpty()) describeInFull.remove();
        else describeInFull.set(values);
    }

    /** Ends what {@link #describeInFull(java.util.Set)} began. */
    public static void describeNormally() {
        describeInFull.remove();
    }

    // A widget is serialized both on its own channel and nested inside an ancestor's tree, and
    // the two snapshots can arrive in either order. Bumped at write time, so a lower seq is
    // strictly the older snapshot.
    private static final java.util.concurrent.atomic.AtomicLong writeSeq = new java.util.concurrent.atomic.AtomicLong();

    private final DslJson<Object> dsl;
    // Pooled per thread rather than a single thread-local instance: to() can be re-entered on
    // the same thread (some property getters pump the event loop while waiting on an async
    // render), and a shared writer would have its buffer overwritten mid-write by the inner call.
    private final ThreadLocal<java.util.ArrayDeque<JsonWriter>> writerPool;

    public Serializer() {
        DslJson.Settings<Object> settings = new DslJson.Settings<>()
                .includeServiceLoader(Serializer.class.getClassLoader())
                .skipDefaultValues(true);
        dsl = new DslJson<>(settings);
        // Enable reading arbitrary JSON (arrays/objects/scalars) into Object — used by EWT's callback
        // payloads ([id] / [id, arg]). Additive: Object.class was previously unregistered.
        dsl.registerReader(Object.class, com.dslplatform.json.ObjectConverter::deserializeObject);
        writerPool = ThreadLocal.withInitial(java.util.ArrayDeque::new);
    }

    /**
     * Values written whole by the walk currently in progress, with the stamp each was given.
     *
     * <p>Per thread because a serialize can be re-entered on one: a property getter can pump the
     * event loop while waiting on a render, and the inner walk must not be credited to the outer.
     */
    private static final ThreadLocal<java.util.List<Object[]>> written =
            ThreadLocal.withInitial(java.util.ArrayList::new);

    private static void noteWritten(Object impl, VWidget value, long seq) {
        written.get().add(new Object[]{value, seq, impl});
    }

    /**
     * The client the frames written on this thread are for. Zero when nothing said, which credits
     * nothing: a walk done to look at a state rather than to send it must not tell the next update
     * it can be relative to something nobody received.
     *
     * <p>Per thread because a serialize can be re-entered on one, and the inner walk may be for a
     * different client than the outer.
     */
    private static final ThreadLocal<Integer> target = ThreadLocal.withInitial(() -> 0);

    /** The client currently being written for. */
    public static int targetConnection() {
        return target.get();
    }

    /**
     * Writes and sends {@code body} as addressed to {@code connection}, so that what it writes is
     * credited to that client and named rather than described only where that client has it.
     */
    public static void targeting(int connection, Runnable body) {
        int outer = target.get();
        target.set(connection);
        try {
            body.run();
        } finally {
            target.set(outer);
        }
    }

    /**
     * Addresses everything written from here on to {@code connection}, until {@link
     * #exitConnection()}. For a caller whose scope is not a block - a test's setup and teardown.
     */
    public static void enterConnection(int connection) {
        target.set(connection);
    }

    /** Stops addressing frames, so a walk that names nobody credits nobody again. */
    public static void exitConnection() {
        target.set(0);
    }

    /**
     * Marks everything the last walk wrote as delivered: nothing outstanding, and the stamp the next
     * update is measured from. Called by the send path once the bytes have been handed over.
     */
    public static void markDelivered() {
        int connection = target.get();
        // Nobody named, so nobody was told: a walk made to read a state rather than to send it.
        if (connection == 0) {
            discardWritten();
            return;
        }
        for (Object[] entry : written.get()) {
            VWidget value = (VWidget) entry[0];
            value.sent(connection, (Long) entry[1]);
            // A delivered widget can be named instead of described from here on, and a name is only
            // any use if the far side can ask for the thing behind it. Being asked for is answered
            // by a lookup the widget only ever entered by being scheduled - which a widget written
            // inside an ancestor need never have been. So delivery is what puts it there.
            FlutterBridge.registerDelivered(entry[2]);
        }
        written.get().clear();
    }

    /** Discards what the last walk recorded — for a serialize done only to look at the state. */
    public static void discardWritten() {
        written.get().clear();
    }

    public byte[] to(Object p) throws IOException {
        written.get().clear();
        depth.get()[0] = 0;
        java.util.ArrayDeque<JsonWriter> pool = writerPool.get();
        JsonWriter writer = pool.pollFirst();
        if (writer == null) {
            writer = dsl.newWriter();
        } else {
            writer.reset();
        }
        java.util.Map<Object, Object> outerScope = payloadScope.get();
        payloadScope.set(EMPTY_SCOPE);
        try {
            dsl.serialize(writer, p);
            return writer.toByteArray();
        } finally {
            if (outerScope == null) {
                payloadScope.remove();
            } else {
                payloadScope.set(outerScope);
            }
            pool.addFirst(writer);
        }
    }

    // Null: no payload in flight. EMPTY_SCOPE: one is, and nothing has asked to be cached yet — so a
    // payload with no expensive getter never allocates the map.
    private static final java.util.Map<Object, Object> EMPTY_SCOPE = java.util.Collections.emptyMap();
    private static final ThreadLocal<java.util.Map<Object, Object>> payloadScope = new ThreadLocal<>();

    /**
     * Derives {@code key}'s value once for the payload being written, or on every call when none is.
     * A generated converter reads each field twice (null check, then write), and one derived value
     * can back several fields, so an uncached getter runs many times per payload.
     *
     * <p>The scope belongs to a single {@link #to} invocation rather than being reference-counted: a
     * getter can pump the event loop and re-enter {@code to}, and that inner payload is a later
     * snapshot which must not reuse this one's values.
     */
    public static <T> T oncePerPayload(Object key, java.util.function.Supplier<T> derive) {
        java.util.Map<Object, Object> scope = payloadScope.get();
        if (scope == null) {
            return derive.get();
        }
        Object cached = scope.get(key);
        if (cached == null) {
            T derived = derive.get();
            if (derived == null) {
                return null;
            }
            // Re-read: derive() can pump the event loop and re-enter for this same key.
            scope = payloadScope.get();
            if (scope == EMPTY_SCOPE) {
                scope = new java.util.IdentityHashMap<>();
                payloadScope.set(scope);
            }
            cached = scope.putIfAbsent(key, derived);
            if (cached == null) {
                cached = derived;
            }
        }
        @SuppressWarnings("unchecked")
        T value = (T) cached;
        return value;
    }

    public <T> T from(Class<T> type, byte[] bytes) throws IOException {
        return from(type, bytes, 0, bytes.length);
    }

    /**
     * Deserialize a sub-range of {@code bytes}. DSL-JSON's byte[] reader (a reused thread-local
     * {@link JsonReader}, no per-call stream object) only reads from index 0, but every inbound
     * frame arrives at offset>0 (the {@code [len][actionId]} header precedes the body). So for a
     * non-zero offset we copy the body slice into a 0-based array and use that reader rather than
     * DSL-JSON's {@link ByteArrayInputStream} path — which is up to ~4× slower on large payloads
     * (measured: a 64 KB JSON string decodes in ~20 µs via the byte[] reader vs ~82 µs via stream)
     * and only a couple ns slower at event sizes, so the copy never meaningfully loses. See
     * {@code SerializerDecodePathTest}.
     */
    public <T> T from(Class<T> type, byte[] bytes, int offset, int length) throws IOException {
        if (length <= 0) return null;
        if (offset != 0) {
            byte[] slice = new byte[length];
            System.arraycopy(bytes, offset, slice, 0, length);
            bytes = slice;
        }
        return dsl.deserialize(type, bytes, length);
    }

    public static <T extends DartWidget> void writeWithId(DslJson json, JsonWriter writer, T impl) {
        if (impl == null) {
            writer.writeNull();
            return;
        }
        boolean alwaysSerialize = !json.omitDefaults;
        Widget api = impl.getApi();
        VWidget value = impl.getValue();
        FormatConverter converter = ((FormatConverter) json.tryFindWriter(value.getClass()));
        // A widget can be disposed between the start of a tree walk and reaching this node
        // (e.g. Shell.close()'s disposal cascade racing a serialize of the same shell). Its
        // checkWidget()-guarded getters would throw and abort the whole tree, not just this node.
        boolean disposed = api.isDisposed();
        writer.writeByte((byte)'{');
        writer.writeByte((byte)'"'); writer.writeAscii(name_id); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(FlutterBridge.id(api), writer);
        writer.writeByte((byte)',');
        writer.writeByte((byte)'"'); writer.writeAscii(name_swt); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        StringConverter.serialize(swtWidgetName(impl, api), writer);
        writer.writeByte((byte)',');
        if (canReference(value, converter == null || disposed)) {
            writeReference(writer);
            return;
        }
        // Identity stub. A null here is undecodable where the reference sits inside a widget array
        // (children, items) and would abort the ancestor's payload; style is read off the api
        // field, not a checkWidget()-guarded getter, so it is safe on a disposed widget.
        //
        // No write stamp, for the same reason a reference carries none: this says which widget, not
        // what it holds. Stamping it made the far side record a state it had not been given and
        // this side had never credited - so every later update read as computed from a state it did
        // not hold, and the stub itself, being newer than everything, displaced the real widget.
        if (converter == null || disposed) {
            writer.writeByte((byte)'"'); writer.writeAscii(name_style); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
            NumberConverter.serialize(api.getStyle(), writer);
            writer.writeByte((byte)'}');
            return;
        }
        writer.writeByte((byte)'"'); writer.writeAscii(name_seq); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        long seq = writeSeq.incrementAndGet();
        NumberConverter.serialize(seq, writer);
        writer.writeByte((byte)',');
        // Noted, not applied. Writing a widget is not the same as delivering it: a widget can be
        // serialized to be looked at - by a test, by a debug dump, by the audit that has to read a
        // state without changing it - and marking it delivered there would tell the next update to
        // be relative to a state nobody was ever sent. The send path applies these once the bytes
        // are on their way; nested children are collected here too, since only this walk knows
        // which ones it wrote.
        noteWritten(impl, value, seq);
        depth.get()[0]++;
        try {
            if (alwaysSerialize) { converter.writeContentFull(writer, value); writer.writeByte((byte)'}'); }
            else if (converter.writeContentMinimal(writer, value)) writer.getByteBuffer()[writer.size() - 1] = '}';
            else writer.getByteBuffer()[writer.size() - 1] = '}';
        } finally {
            depth.get()[0]--;
        }
    }

    /**
     * Whether this widget can travel as a name rather than as a description.
     *
     * <p>Four things have to hold. It has to be nested — the widget a frame is about is always
     * described in full, or the frame says nothing. The far side has to already hold it, which its
     * write stamp records: a widget that has been written has been delivered, on its own channel or
     * inside an ancestor. Nothing about it can have changed, or the reference would be the only
     * mention of a change and it would be lost. And nothing beneath it can be travelling through it
     * either — see {@link #describeInFull(java.util.Set)}.
     *
     * <p>{@code stateless} covers the widget disposed mid-walk, whose guarded getters cannot be read
     * at all. That one has always travelled as an identity stub; as a reference it no longer carries
     * a write stamp, so the far side stops mistaking an empty stub for newer state and wiping what
     * it holds.
     */
    private static boolean canReference(VWidget value, boolean stateless) {
        if (!diffEnabled || depth.get()[0] == 0) return false;
        if (value.sentSeq(target.get()) == 0) return false;
        java.util.Set<VWidget> required = describeInFull.get();
        if (required != null && required.contains(value)) return false;
        return stateless || !value.anyDirty();
    }

    /**
     * Closes a widget written as identity only: which widget it is, and nothing about what it holds.
     *
     * <p>No state at all, style included — the far side answers a reference with the object it
     * already holds, so anything written here would be read only to be thrown away. It carries no
     * write stamp either, so a reference reads as older than anything held and cannot displace it
     * even if the marker were missed.
     */
    private static void writeReference(JsonWriter writer) {
        writer.writeByte((byte)'"'); writer.writeAscii(name_ref); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(1, writer);
        writer.writeByte((byte)'}');
    }

    /**
     * On by default. {@code -Dequo.swt.diff=false} goes back to sending every widget whole, which is
     * both the way out if partial updates ever have to be taken out of the picture and the reference
     * the two modes are compared against each other with.
     */
    public static final boolean diffEnabled = !"false".equalsIgnoreCase(System.getProperty("equo.swt.diff"));

    /** Whether {@code impl} can be described by what changed rather than in full. */
    public static boolean canDiff(DartWidget impl) {
        return canDiff(impl, target.get());
    }

    /**
     * Forgets that {@code value} was ever delivered, to anyone.
     *
     * <p>For a client that says it does not hold a widget. Until this, asking for one re-sent it as
     * what had changed since a state the asker did not have, with everything under it named rather
     * than described - so the answer to "I do not have this" was a frame that named more things the
     * asker did not have, and it asked again for each.
     */
    public static void forgetDelivery(VWidget value) {
        if (value != null) value.sent(0, 0L);
    }

    /** Whether {@code impl} can be described to {@code connection} by what changed rather than in full. */
    public static boolean canDiff(DartWidget impl, int connection) {
        if (!diffEnabled || impl == null) return false;
        VWidget value = impl.getValue();
        // Never sent whole, so there is no state on the far side for a change to be relative to.
        return value != null && value.sentSeq(connection) != 0 && value.anyDirty();
    }

    /**
     * Writes {@code impl} as the properties that changed since it was last sent.
     *
     * <p>{@code _d} names them and {@code _b} names the state they were computed from. Both are
     * needed: the first says this is a change rather than a whole widget, the second lets the far
     * side check the change fits what it holds instead of assuming it does.
     *
     * <p>Every named property carries its value, including one that changed back to a default -
     * which the whole-state writer would omit. A change that is invisible on the wire is not a
     * change the far side can apply.
     */
    public byte[] toDiff(DartWidget impl) {
        written.get().clear();
        depth.get()[0] = 0;
        java.util.ArrayDeque<JsonWriter> pool = writerPool.get();
        JsonWriter writer = pool.pollFirst();
        if (writer == null) writer = dsl.newWriter();
        else writer.reset();
        try {
            VWidget value = impl.getValue();
            long seq = writeSeq.incrementAndGet();
            writer.writeByte((byte) '{');
            writeKeyValue(writer, "id", FlutterBridge.id(impl.getApi()));
            writeKeyValue(writer, "swt", swtWidgetName(impl, impl.getApi()));
            writeKeyValue(writer, "_s", seq);
            writeKeyValue(writer, "_b", value.sentSeq(target.get()));
            writeKey(writer, "_d");
            writer.writeByte((byte) '[');
            boolean first = true;
            for (String key : value.changedKeys()) {
                if (!first) writer.writeByte((byte) ',');
                first = false;
                StringConverter.serialize(key, writer);
            }
            writer.writeByte((byte) ']');
            writer.writeByte((byte) ',');
            // The widget being described is this one; everything its properties name is nested.
            depth.get()[0]++;
            try {
                value.writeDiff(writer);
            } finally {
                depth.get()[0]--;
            }
            // Every pair leaves a trailing comma; the last one becomes the closing brace.
            writer.getByteBuffer()[writer.size() - 1] = '}';
            noteWritten(impl, value, seq);
            return writer.toByteArray();
        } finally {
            pool.addFirst(writer);
        }
    }

    /**
     * Writes one {@code "key":value,} pair of a partial update.
     *
     * <p>The trailing comma is deliberate and matches how dsl-json's own minimal writer works: each
     * pair is written unconditionally and the caller overwrites the last comma with the closing
     * brace. Deciding per pair whether a separator is needed would mean every writer knowing
     * whether anything followed it, which nothing at this level can know.
     *
     * <p>Overloaded by type rather than taking Object so the common properties - a string, a
     * number, a flag - go straight to their converter instead of through boxing and a writer
     * lookup, on the path that runs for every changed property of every update.
     */
    public static void writeKeyValue(JsonWriter writer, String key, String value) {
        writeKey(writer, key);
        if (value == null) writer.writeNull();
        else StringConverter.serialize(value, writer);
        writer.writeByte((byte) ',');
    }

    public static void writeKeyValue(JsonWriter writer, String key, long value) {
        writeKey(writer, key);
        NumberConverter.serialize(value, writer);
        writer.writeByte((byte) ',');
    }

    public static void writeKeyValue(JsonWriter writer, String key, double value) {
        writeKey(writer, key);
        NumberConverter.serialize(value, writer);
        writer.writeByte((byte) ',');
    }

    public static void writeKeyValue(JsonWriter writer, String key, boolean value) {
        writeKey(writer, key);
        writer.writeAscii(value ? "true" : "false");
        writer.writeByte((byte) ',');
    }

    /** Anything with a registered writer: value objects, nested widgets, arrays, boxed numbers. */
    /**
     * A {@code char[]} property, written the way the whole-widget path writes it.
     *
     * <p>The generic {@link #writeKeyValue(JsonWriter, String, Object)} would hand this to
     * dsl-json, which writes a JSON string; the whole-widget path routes it through
     * {@link CharArrayConverter} and writes code units, which is what the client's
     * {@code List&lt;int&gt;} reads. Two writers disagreeing about one property means an update
     * that cannot be decoded, and a payload the client drops whole.
     */
    public static void writeKeyValue(JsonWriter writer, String key, char[] value) {
        writeKey(writer, key);
        CharArrayConverter.write(writer, value);
        writer.writeByte((byte) ',');
    }

    /** An {@code int[]} property. See {@link #writeKeyValue(JsonWriter, String, char[])}. */
    public static void writeKeyValue(JsonWriter writer, String key, int[] value) {
        writeKey(writer, key);
        IntArrayConverter.write(writer, value);
        writer.writeByte((byte) ',');
    }

    public static void writeKeyValue(JsonWriter writer, String key, Object value) {
        writeKey(writer, key);
        if (value == null) writer.writeNull();
        else writer.serializeObject(value);
        writer.writeByte((byte) ',');
    }

    private static void writeKey(JsonWriter writer, String key) {
        writer.writeByte((byte) '"');
        writer.writeAscii(key);
        writer.writeByte((byte) '"');
        writer.writeByte((byte) ':');
    }

    private static String swtWidgetName(DartWidget impl, Widget api) {
        Class<? extends Widget> aClass = api.getClass();
        String apiName = aClass.isAnonymousClass() || !isOwnPackage(aClass)
                ? Config.getSwtBaseClassName(aClass)
                : aClass.getSimpleName();
        String implName = impl.getClass().getSimpleName();
        if (implName.startsWith("Dart") && !implName.substring(4).equals(apiName)) {
            return implName.substring(4);
        }
        // A composite whose layout is the e4 SashLayout and whose direct children are the
        // part-stacks is the main workbench area regardless of how deep the perspective nests
        // it -- serialize it as a MainComposite so the parts get the panel treatment
        // (gap/border/shadow). The construction-time path check (Config.isMainComposite)
        // misses perspectives that nest the sash container differently, and the layout is only
        // set after construction, so this resolves it here at serialize time.
        if (api instanceof Composite composite && Config.isMainSashComposite(composite)) {
            return "MainComposite";
        }
        return apiName;
    }

    private static boolean isOwnPackage(Class<? extends Widget> aClass) {
        return aClass.getPackage().getName().startsWith("org.eclipse.swt") || aClass.getPackage().getName().startsWith("com.equo.chromium");
    }

    public static <T extends DartResource> void writeResourceWithId(DslJson json, JsonWriter writer, T impl) {
        if (impl == null) { writer.writeNull(); return; }
        Object value = impl.getValue();
        writeBodyWithId(json, writer, FlutterBridge.id(impl), FlutterBridge.widgetName(impl),
                (FormatConverter) json.tryFindWriter(value.getClass()), value, null);
    }

    public static void writeEditorWithId(DslJson json, JsonWriter writer, DartControlEditor impl) {
        if (impl == null) { writer.writeNull(); return; }
        ControlEditor api = impl.getApi();
        Object value = impl.getValue();
        // Use the actual class name (TreeEditor, TableEditor, etc.) instead of always "ControlEditor"
        Class<? extends ControlEditor> apiClass = api.getClass();
        String editorName = apiClass.isAnonymousClass() ? Config.getSwtBaseClassName(apiClass) : apiClass.getSimpleName();
        // Stamped: an editor is re-sent whenever it moves to another cell or takes another control,
        // and without a stamp the receiver cannot tell that description from the one it already has.
        writeBodyWithId(json, writer, FlutterBridge.id(impl), editorName,
                (FormatConverter) json.tryFindWriter(value.getClass()), value, 0, true);
    }

    private static void writeBodyWithId(DslJson json, JsonWriter writer, long id, String swtName,
                                         FormatConverter converter, Object value, Integer style) {
        writeBodyWithId(json, writer, id, swtName, converter, value, style, false);
    }

    /**
     * @param stamped whether to give this body a write stamp of its own. Something delivered only
     *     inside another's payload has no stamp, and the receiver holds one object per id: with
     *     nothing to order two copies by, it keeps the one it has and every later description of
     *     the same id is dropped. A body that changes after it is first sent therefore needs a
     *     stamp; one that never changes does not, and is cheaper without.
     */
    private static void writeBodyWithId(DslJson json, JsonWriter writer, long id, String swtName,
                                         FormatConverter converter, Object value, Integer style,
                                         boolean stamped) {
        boolean alwaysSerialize = !json.omitDefaults;
        writer.writeByte((byte)'{');
        writer.writeByte((byte)'"'); writer.writeAscii(name_id); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(id, writer);
        writer.writeByte((byte)',');
        writer.writeByte((byte)'"'); writer.writeAscii(name_swt); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        StringConverter.serialize(swtName, writer);
        if (stamped) {
            writer.writeByte((byte)',');
            writer.writeByte((byte)'"'); writer.writeAscii(name_seq); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
            NumberConverter.serialize(writeSeq.incrementAndGet(), writer);
        }
        if (converter == null) {
            writer.writeByte((byte)'}');
            return;
        }
        writer.writeByte((byte)',');
        if (style != null) {
            writer.writeByte((byte)'"'); writer.writeAscii(name_style); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
            NumberConverter.serialize(style, writer);
            writer.writeByte((byte)',');
        }
        if (alwaysSerialize) { converter.writeContentFull(writer, value); writer.writeByte((byte)'}'); }
        else if (converter.writeContentMinimal(writer, value)) writer.getByteBuffer()[writer.size() - 1] = '}';
        else writer.getByteBuffer()[writer.size() - 1] = '}';
    }

}
