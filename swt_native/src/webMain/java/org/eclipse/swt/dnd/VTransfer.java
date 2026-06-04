package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VTransfer {

    protected VTransfer() {
    }

    protected VTransfer(ITransfer impl) {
        this.impl = impl;
    }

    protected ITransfer impl;

    @JsonConverter(target = Transfer.class)
    public static class TransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTransfer.class, (JsonWriter.WriteObject<DartTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartTransfer.class, (JsonReader.ReadObject<DartTransfer>) reader -> {
                return null;
            });
        }

        public static Transfer read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Transfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
