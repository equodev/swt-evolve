package dev.equo.swt;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.StringConverter;
import com.dslplatform.json.runtime.FormatConverter;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Serializer {
    private static final byte[] name_id = "id".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] name_swt = "swt".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] name_style = "style".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    private final DslJson<Object> dsl;

    public Serializer() {
//        Settings.withAnalyzers(false, false)
//                .with()
        DslJson.Settings<Object> settings = new DslJson.Settings<>()
                .includeServiceLoader(Serializer.class.getClassLoader())
                .skipDefaultValues(true);
        dsl = new DslJson<>(settings);
    }

    public void to(Object p, ByteArrayOutputStream out) throws IOException {
        dsl.serialize(p, out);
    }

    public <T> T from(Class<T> type, ByteArrayInputStream in) throws IOException {
        return dsl.deserialize(type, in);
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
        Class<? extends Widget> aClass = api.getClass();
        String widgetName = aClass.isAnonymousClass() || !aClass.getPackage().getName().startsWith("org.eclipse.swt") ? Config.getSwtBaseClassName(aClass) : aClass.getSimpleName();
        StringConverter.serialize(widgetName, writer);
        writer.writeByte((byte)',');
//        writer.writeByte((byte)'"'); writer.writeAscii(name_style); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
//        NumberConverter.serialize(api.getStyle(), writer);
//        writer.writeByte((byte)',');
        if (alwaysSerialize) { converter.writeContentFull(writer, value); writer.writeByte((byte)'}'); }
        else if (converter.writeContentMinimal(writer, value)) writer.getByteBuffer()[writer.size() - 1] = '}';
        else writer.getByteBuffer()[writer.size() - 1] = '}';
//        else writer.writeByte((byte)'}');
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
