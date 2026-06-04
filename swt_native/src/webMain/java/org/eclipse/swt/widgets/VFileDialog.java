package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VFileDialog extends VDialog {

    protected VFileDialog() {
    }

    protected VFileDialog(DartFileDialog impl) {
        super(impl);
    }

    public String getFileName() {
        return ((DartFileDialog) impl).getFileName();
    }

    public void setFileName(String value) {
        ((DartFileDialog) impl).fileName = value;
    }

    public String[] getFileNames() {
        String[] values = ((DartFileDialog) impl).fileNames;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setFileNames(String[] value) {
        ((DartFileDialog) impl).fileNames = value;
    }

    public String[] getFilterExtensions() {
        String[] values = ((DartFileDialog) impl).filterExtensions;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setFilterExtensions(String[] value) {
        ((DartFileDialog) impl).filterExtensions = value;
    }

    public int getFilterIndex() {
        return ((DartFileDialog) impl).getFilterIndex();
    }

    public void setFilterIndex(int value) {
        ((DartFileDialog) impl).filterIndex = value;
    }

    public String[] getFilterNames() {
        String[] values = ((DartFileDialog) impl).filterNames;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(String[]::new);
    }

    public void setFilterNames(String[] value) {
        ((DartFileDialog) impl).filterNames = value;
    }

    public String getFilterPath() {
        return ((DartFileDialog) impl).getFilterPath();
    }

    public void setFilterPath(String value) {
        ((DartFileDialog) impl).filterPath = value;
    }

    public boolean getOverwrite() {
        return ((DartFileDialog) impl).getOverwrite();
    }

    public void setOverwrite(boolean value) {
        ((DartFileDialog) impl).overwrite = value;
    }

    @JsonConverter(target = FileDialog.class)
    public static class FileDialogJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartFileDialog.class, (JsonWriter.WriteObject<DartFileDialog>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartFileDialog.class, (JsonReader.ReadObject<DartFileDialog>) reader -> {
                return null;
            });
        }

        public static FileDialog read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, FileDialog api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
