package dev.equo.swt;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;

import java.io.IOException;

@JsonConverter(target = double.class)
public class DoubleConverter {
    public static double read(JsonReader reader) throws IOException {
        if (reader.wasNull()) return 0.0;
        return NumberConverter.deserializeDouble(reader);
    }

    public static void write(JsonWriter writer, double value) {
        NumberConverter.serialize(value, writer);
    }
}
