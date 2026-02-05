package dev.equo.swt;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.StringConverter;
import com.dslplatform.json.runtime.FormatConverter;
import com.dslplatform.json.runtime.Settings;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Serializer {
    private static final byte[] name_id = "id".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    private static final byte[] name_swt = "swt".getBytes(java.nio.charset.StandardCharsets.UTF_8);
//    private static final byte[] name_style = "style".getBytes(java.nio.charset.StandardCharsets.UTF_8);

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
        if (impl == null) {
            writer.writeNull();
            return;
        }
        boolean alwaysSerialize = !json.omitDefaults;
        Object api = impl.getApi();
        Object value = impl.getValue();
        FormatConverter converter = ((FormatConverter) json.tryFindWriter(value.getClass()));
        writer.writeByte((byte)'{');
        writer.writeByte((byte)'"'); writer.writeAscii(name_id); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(FlutterBridge.id(impl), writer);
        writer.writeByte((byte)',');
        writer.writeByte((byte)'"'); writer.writeAscii(name_swt); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        StringConverter.serialize(FlutterBridge.widgetName(impl), writer);
        writer.writeByte((byte)',');
        if (alwaysSerialize) { converter.writeContentFull(writer, value); writer.writeByte((byte)'}'); }
        else if (converter.writeContentMinimal(writer, value)) writer.getByteBuffer()[writer.size() - 1] = '}';
        else writer.getByteBuffer()[writer.size() - 1] = '}';
    }

}
