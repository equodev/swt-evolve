package org.eclipse.swt.dnd;

import java.util.concurrent.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VClipboard {

    protected VClipboard() {
    }

    protected VClipboard(DartClipboard impl) {
        this.impl = impl;
    }

    protected DartClipboard impl;

    public String[] getAvailableTypeNames() {
        String[] values = ((DartClipboard) impl).availableTypeNames;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setAvailableTypeNames(String[] value) {
        ((DartClipboard) impl).availableTypeNames = value;
    }

    public TransferData[] getAvailableTypes() {
        TransferData[] values = ((DartClipboard) impl).availableTypes;
        if (values == null)
            return null;
        ArrayList<TransferData> result = new ArrayList<>(values.length);
        for (TransferData v : values) if (v != null)
            result.add(v);
        return result.toArray(TransferData[]::new);
    }

    public void setAvailableTypes(TransferData[] value) {
        ((DartClipboard) impl).availableTypes = value;
    }

    @JsonAttribute(ignore = true)
    public Object getContents() {
        return ((DartClipboard) impl).contents;
    }

    public void setContents(Object value) {
        ((DartClipboard) impl).contents = value;
    }

    @JsonConverter(target = Clipboard.class)
    public static class ClipboardJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartClipboard.class, (JsonWriter.WriteObject<DartClipboard>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartClipboard.class, (JsonReader.ReadObject<DartClipboard>) reader -> {
                return null;
            });
        }

        public static Clipboard read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Clipboard api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
