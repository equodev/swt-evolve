package org.eclipse.swt.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VImageTransfer extends VByteArrayTransfer {

    protected VImageTransfer() {
    }

    protected VImageTransfer(DartImageTransfer impl) {
        super(impl);
    }

    @JsonConverter(target = ImageTransfer.class)
    public static class ImageTransferJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartImageTransfer.class, (JsonWriter.WriteObject<DartImageTransfer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartImageTransfer.class, (JsonReader.ReadObject<DartImageTransfer>) reader -> {
                return null;
            });
        }

        public static ImageTransfer read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ImageTransfer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
