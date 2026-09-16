package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VTreeItem extends VItem {

    protected VTreeItem() {
    }

    protected VTreeItem(DartTreeItem impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartTreeItem) impl).background;
    }

    public void setBackground(Color value) {
        ((DartTreeItem) impl).background = value;
    }

    public boolean getChecked() {
        return ((DartTreeItem) impl).getChecked();
    }

    public void setChecked(boolean value) {
        ((DartTreeItem) impl).checked = value;
    }

    public boolean getExpanded() {
        return ((DartTreeItem) impl).getExpanded();
    }

    public void setExpanded(boolean value) {
        ((DartTreeItem) impl).isExpanded = value;
    }

    public Font getFont() {
        Font val = ((DartTreeItem) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartTreeItem) impl).font = value;
    }

    public Color getForeground() {
        return ((DartTreeItem) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartTreeItem) impl).foreground = value;
    }

    public boolean getGrayed() {
        return ((DartTreeItem) impl).getGrayed();
    }

    public void setGrayed(boolean value) {
        ((DartTreeItem) impl).grayed = value;
    }

    public Image[] getImages() {
        return ((DartTreeItem) impl).getImages();
    }

    public void setImages(Image[] value) {
        ((DartTreeItem) impl).setImages(value);
    }

    public TreeItem[] getItems() {
        TreeItem[] values = ((DartTreeItem) impl).items;
        if (values == null)
            return null;
        ArrayList<TreeItem> result = new ArrayList<>(values.length);
        for (TreeItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new TreeItem[0]);
    }

    public void setItems(TreeItem[] value) {
        ((DartTreeItem) impl).items = value;
    }

    public String[] getTexts() {
        return ((DartTreeItem) impl).getTexts();
    }

    public void setTexts(String[] value) {
        ((DartTreeItem) impl).strings = value;
    }

    public static final String BACKGROUND = "background";

    public static final String CHECKED = "checked";

    public static final String EXPANDED = "expanded";

    public static final String FONT = "font";

    public static final String FOREGROUND = "foreground";

    public static final String GRAYED = "grayed";

    public static final String IMAGES = "images";

    public static final String ITEMS = "items";

    public static final String TEXTS = "texts";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "background":
                Serializer.writeKeyValue(writer, "background", getBackground());
                return;
            case "checked":
                Serializer.writeKeyValue(writer, "checked", getChecked());
                return;
            case "expanded":
                Serializer.writeKeyValue(writer, "expanded", getExpanded());
                return;
            case "font":
                Serializer.writeKeyValue(writer, "font", getFont());
                return;
            case "foreground":
                Serializer.writeKeyValue(writer, "foreground", getForeground());
                return;
            case "grayed":
                Serializer.writeKeyValue(writer, "grayed", getGrayed());
                return;
            case "images":
                Serializer.writeKeyValue(writer, "images", getImages());
                return;
            case "items":
                Serializer.writeKeyValue(writer, "items", getItems());
                return;
            case "texts":
                Serializer.writeKeyValue(writer, "texts", getTexts());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = TreeItem.class)
    public static class TreeItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTreeItem.class, (JsonWriter.WriteObject<DartTreeItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTreeItem.class, (JsonReader.ReadObject<DartTreeItem>) reader -> {
                return null;
            });
        }

        public static TreeItem read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, TreeItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
