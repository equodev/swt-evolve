package org.eclipse.swt.program;

import java.io.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VProgram {

    protected VProgram() {
    }

    protected VProgram(DartProgram impl) {
        this.impl = impl;
    }

    protected DartProgram impl;

    @JsonConverter(target = Program.class)
    public static class ProgramJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartProgram.class, (JsonWriter.WriteObject<DartProgram>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartProgram.class, (JsonReader.ReadObject<DartProgram>) reader -> {
                return null;
            });
        }

        public static Program read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Program api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
