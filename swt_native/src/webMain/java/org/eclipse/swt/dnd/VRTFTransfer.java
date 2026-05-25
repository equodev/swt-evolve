package org.eclipse.swt.dnd;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VRTFTransfer extends VByteArrayTransfer {

    protected VRTFTransfer() {
    }

    protected VRTFTransfer(DartRTFTransfer impl) {
        super(impl);
    }

    @JsonConverter(target = RTFTransfer.class)
    public static class RTFTransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartRTFTransfer.class, (JsonWriter.WriteObject<DartRTFTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartRTFTransfer.class, (JsonReader.ReadObject<DartRTFTransfer>) reader -> {
                return null;
            });
        }

        public static RTFTransfer read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, RTFTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
