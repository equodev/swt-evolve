package dev.equo.swt;

import com.dslplatform.json.BoolConverter;
import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;

@JsonConverter(target = boolean.class)
public class BooleanConverter {
    public static boolean read(JsonReader reader) throws IOException {
        if (reader.wasNull()) return false;
        return BoolConverter.deserialize(reader);
    }

    public static void write(JsonWriter writer, boolean value) {
        BoolConverter.serialize(value, writer);
    }
}
