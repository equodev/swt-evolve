package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VTreeEditor extends VControlEditor {

    protected VTreeEditor() {
    }

    protected VTreeEditor(DartTreeEditor impl) {
        super(impl);
    }

    public int getColumn() {
        return ((DartTreeEditor) impl).getColumn();
    }

    public void setColumn(int value) {
        ((DartTreeEditor) impl).column = value;
    }

    public Control getEditor() {
        Control val = ((DartTreeEditor) impl).editor;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setEditor(Control value) {
        ((DartTreeEditor) impl).editor = value;
    }

    public TreeItem getItem() {
        TreeItem val = ((DartTreeEditor) impl).item;
        if (val != null && !(val.getImpl() instanceof DartTreeItem))
            return null;
        return val;
    }

    public void setItem(TreeItem value) {
        ((DartTreeEditor) impl).item = value;
    }

    @JsonConverter(target = TreeEditor.class)
    public static class TreeEditorJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTreeEditor.class, (JsonWriter.WriteObject<DartTreeEditor>) (writer, impl) -> {
                Serializer.writeEditorWithId(json, writer, impl);
            });
            json.registerReader(DartTreeEditor.class, (JsonReader.ReadObject<DartTreeEditor>) reader -> {
                return null;
            });
        }

        public static TreeEditor read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, TreeEditor api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
