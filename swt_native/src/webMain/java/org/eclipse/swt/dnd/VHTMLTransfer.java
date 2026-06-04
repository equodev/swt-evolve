package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VHTMLTransfer extends VByteArrayTransfer {

    protected VHTMLTransfer() {
    }

    protected VHTMLTransfer(DartHTMLTransfer impl) {
        super(impl);
    }

    @JsonConverter(target = HTMLTransfer.class)
    public static class HTMLTransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartHTMLTransfer.class, (JsonWriter.WriteObject<DartHTMLTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartHTMLTransfer.class, (JsonReader.ReadObject<DartHTMLTransfer>) reader -> {
                return null;
            });
        }

        public static HTMLTransfer read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, HTMLTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
