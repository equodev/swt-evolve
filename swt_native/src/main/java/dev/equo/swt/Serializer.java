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
        try {
            dsl.serialize(writer, p);
            return writer.toByteArray();
        } finally {
            pool.addFirst(writer);
        }
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
        writer.writeByte((byte)'{');
        writer.writeByte((byte)'"'); writer.writeAscii(name_id); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(FlutterBridge.id(api), writer);
        writer.writeByte((byte)',');
        writer.writeByte((byte)'"'); writer.writeAscii(name_swt); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        StringConverter.serialize(swtWidgetName(impl, api), writer);
        writer.writeByte((byte)',');
//        writer.writeByte((byte)'"'); writer.writeAscii(name_style); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
//        NumberConverter.serialize(api.getStyle(), writer);
//        writer.writeByte((byte)',');
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
