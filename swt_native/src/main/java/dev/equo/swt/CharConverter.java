package dev.equo.swt;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.NumberConverter;

import java.io.IOException;

@JsonConverter(target = char.class)
public class CharConverter {
    public static void write(JsonWriter writer, char value) {
        writer.writeString(String.valueOf(value));
    }

    public static char read(JsonReader reader) throws IOException {
        if (reader.last() == '"') {
            String str = reader.readString();
            return str.isEmpty() ? '\0' : str.charAt(0);
        }
        return (char) (int) NumberConverter.deserializeDouble(reader);
    }
}