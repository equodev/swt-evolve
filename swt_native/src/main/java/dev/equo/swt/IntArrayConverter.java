package dev.equo.swt;

import com.dslplatform.json.JsonConverter;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;

import static com.dslplatform.json.NumberConverter.*;

public class IntArrayConverter {
    public static void write(JsonWriter writer, int[] value) {
        serialize(value, writer);
    }

    public static int[] read(JsonReader reader) throws IOException {
        if (reader.last() == '[') {
            reader.getNextToken();
        }
        if (reader.last() == ']') {
            return INT_EMPTY_ARRAY;
        }
        int[] buffer = new int[4];
        buffer[0] = (int)deserializeDouble(reader);
        int i = 1;
        while (reader.getNextToken() == ',') {
            reader.getNextToken();
            if (i == buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length << 1);
            }
            buffer[i++] = (int)deserializeDouble(reader);
        }
        reader.checkArrayEnd();
        return Arrays.copyOf(buffer, i);
    }

}
