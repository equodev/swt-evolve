package dev.equo.swt;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;

import static com.dslplatform.json.NumberConverter.*;

/**
 * Serializes char[] as a JSON array of code units. dsl-json's built-in char[] writer emits a JSON
 * string when the array is non-empty but [] when it is empty; the Dart side types every char[]
 * property as List&lt;int&gt;, so the string form fails to deserialize and the whole state push is
 * dropped. Reading accepts both forms.
 */
public class CharArrayConverter {

    public static void write(JsonWriter writer, char[] value) {
        if (value == null) {
            writer.writeNull();
            return;
        }
        writer.writeByte((byte) '[');
        for (int i = 0; i < value.length; i++) {
            if (i > 0)
                writer.writeByte((byte) ',');
            serialize((int) value[i], writer);
        }
        writer.writeByte((byte) ']');
    }

    public static char[] read(JsonReader reader) throws IOException {
        if (reader.wasNull())
            return null;
        if (reader.last() == '"') {
            String str = reader.readString();
            return str.toCharArray();
        }
        if (reader.last() == '[') {
            reader.getNextToken();
        }
        if (reader.last() == ']') {
            return new char[0];
        }
        char[] buffer = new char[4];
        buffer[0] = (char) (int) deserializeDouble(reader);
        int i = 1;
        while (reader.getNextToken() == ',') {
            reader.getNextToken();
            if (i == buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length << 1);
            }
            buffer[i++] = (char) (int) deserializeDouble(reader);
        }
        reader.checkArrayEnd();
        return Arrays.copyOf(buffer, i);
    }
}
