package dev.equo.swt;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;
import com.dslplatform.json.StringConverter;
import com.dslplatform.json.runtime.FormatConverter;
import com.dslplatform.json.runtime.Settings;
import org.eclipse.swt.widgets.*;

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
            .includeServiceLoader()
            .skipDefaultValues(true);
        dsl = new DslJson<>(settings);
    }

    public void to(Object p, ByteArrayOutputStream out) throws IOException {
        dsl.serialize(p, out);
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
        NumberConverter.serialize(api.hashCode(), writer);
        writer.writeByte((byte)',');
        writer.writeByte((byte)'"'); writer.writeAscii(name_swt); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        StringConverter.serialize(api.getClass().getSimpleName(), writer);
        writer.writeByte((byte)',');
        writer.writeByte((byte)'"'); writer.writeAscii(name_style); writer.writeByte((byte)'"'); writer.writeByte((byte)':');
        NumberConverter.serialize(api.getStyle(), writer);
        writer.writeByte((byte)',');
        if (alwaysSerialize) { converter.writeContentFull(writer, value); writer.writeByte((byte)'}'); }
        else if (converter.writeContentMinimal(writer, value)) writer.getByteBuffer()[writer.size() - 1] = '}';
        else writer.writeByte((byte)'}');
    }

}
