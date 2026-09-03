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
    private static final byte[] name_seq = "seq".getBytes(java.nio.charset.StandardCharsets.UTF_8);

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

    public byte[] to(Object p) throws IOException {
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
        writer.writeByte((byte)'"'); writer.writeAscii(name_seq); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(writeSeq.incrementAndGet(), writer);
        // Identity stub. A null here is undecodable where the reference sits inside a widget array
        // (children, items) and would abort the ancestor's payload; style is read off the api
        // field, not a checkWidget()-guarded getter, so it is safe on a disposed widget.
        if (converter == null || disposed) {
            writer.writeByte((byte)',');
            writer.writeByte((byte)'"'); writer.writeAscii(name_style); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
            NumberConverter.serialize(api.getStyle(), writer);
            writer.writeByte((byte)'}');
            return;
        }
        writer.writeByte((byte)',');
        if (alwaysSerialize) { converter.writeContentFull(writer, value); writer.writeByte((byte)'}'); }
        else if (converter.writeContentMinimal(writer, value)) writer.getByteBuffer()[writer.size() - 1] = '}';
        else writer.getByteBuffer()[writer.size() - 1] = '}';
//        else writer.writeByte((byte)'}');
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
        writeBodyWithId(json, writer, FlutterBridge.id(impl), editorName,
                (FormatConverter) json.tryFindWriter(value.getClass()), value, 0);
    }

    private static void writeBodyWithId(DslJson json, JsonWriter writer, long id, String swtName,
                                         FormatConverter converter, Object value, Integer style) {
        boolean alwaysSerialize = !json.omitDefaults;
        writer.writeByte((byte)'{');
        writer.writeByte((byte)'"'); writer.writeAscii(name_id); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(id, writer);
        writer.writeByte((byte)',');
        writer.writeByte((byte)'"'); writer.writeAscii(name_swt); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        StringConverter.serialize(swtName, writer);
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
