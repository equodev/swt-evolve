package dev.equo.swt;

import com.dslplatform.json.*;

import java.io.IOException;

@JsonConverter(target = int.class)
public class DecimalToIntConfig {
    public static int read(JsonReader<?> reader) throws IOException {
        return (int) com.dslplatform.json.NumberConverter.deserializeDouble(reader); // ToDo: remove me with gson
    }

    public static void write(JsonWriter writer, int value) {
        com.dslplatform.json.NumberConverter.serialize(value, writer);
    }
}