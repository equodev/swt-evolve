package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VDirectoryDialog extends VDialog {

    protected VDirectoryDialog() {
    }

    protected VDirectoryDialog(DartDirectoryDialog impl) {
        super(impl);
    }

    public String getFilterPath() {
        return ((DartDirectoryDialog) impl).getFilterPath();
    }

    public void setFilterPath(String value) {
        ((DartDirectoryDialog) impl).filterPath = value;
    }

    public String getMessage() {
        return ((DartDirectoryDialog) impl).getMessage();
    }

    public void setMessage(String value) {
        ((DartDirectoryDialog) impl).message = value;
    }

    @JsonConverter(target = DirectoryDialog.class)
    public static class DirectoryDialogJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartDirectoryDialog.class, (JsonWriter.WriteObject<DartDirectoryDialog>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartDirectoryDialog.class, (JsonReader.ReadObject<DartDirectoryDialog>) reader -> {
                return null;
            });
        }

        public static DirectoryDialog read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, DirectoryDialog api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
