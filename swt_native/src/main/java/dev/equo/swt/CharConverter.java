package dev.equo.swt;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;

import java.io.IOException;

@JsonConverter(target = char.class)
public class CharConverter {
    // Written as the code unit, not a one-character string: the Dart side types every char
    // property as int, and a string value makes the whole V* payload fail to deserialize.
    public static void write(JsonWriter writer, char value) {
        NumberConverter.serialize((int) value, writer);
    }

    public static char read(JsonReader reader) throws IOException {
        if (reader.wasNull()) return '\0';
        if (reader.last() == '"') {
            String str = reader.readString();
            return str.isEmpty() ? '\0' : str.charAt(0);
        }
        if (reader.wasNull()) return (char) -1;
        return (char) (int) NumberConverter.deserializeDouble(reader);
    }
}