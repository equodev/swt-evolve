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
        return ((DartTreeItem) impl).getBackground();
    }

    public void setBackground(Color value) {
        ((DartTreeItem) impl).setBackground(value);
    }

    public Color[] getBackgrounds() {
        Color[] values = ((DartTreeItem) impl).backgrounds;
        if (values == null)
            return null;
        Color[] result = new Color[values.length];
        for (int i = 0; i < values.length; i++) {
            Color v = values[i];
            if (v != null && !(v.getImpl() instanceof DartColor))
                v = null;
            result[i] = v;
        }
        return result;
    }

    public void setBackgrounds(Color[] value) {
        ((DartTreeItem) impl).backgrounds = value;
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
        ((DartTreeItem) impl).expanded = value;
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

    public Font[] getFonts() {
        Font[] values = ((DartTreeItem) impl).cellFont;
        if (values == null)
            return null;
        Font[] result = new Font[values.length];
        for (int i = 0; i < values.length; i++) {
            Font v = values[i];
            if (v != null && v.getImpl() instanceof SwtFont)
                v = GraphicsUtils.copyFont(v);
            if (v != null && !(v.getImpl() instanceof DartFont))
                v = null;
            result[i] = v;
        }
        return result;
    }

    public void setFonts(Font[] value) {
        ((DartTreeItem) impl).cellFont = value;
    }

    public Color getForeground() {
        return ((DartTreeItem) impl).getForeground();
    }

    public void setForeground(Color value) {
        ((DartTreeItem) impl).setForeground(value);
    }

    public Color[] getForegrounds() {
        Color[] values = ((DartTreeItem) impl).foregrounds;
        if (values == null)
            return null;
        Color[] result = new Color[values.length];
        for (int i = 0; i < values.length; i++) {
            Color v = values[i];
            if (v != null && !(v.getImpl() instanceof DartColor))
                v = null;
            result[i] = v;
        }
        return result;
    }

    public void setForegrounds(Color[] value) {
        ((DartTreeItem) impl).foregrounds = value;
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

    public int[] getPaintedTexts() {
        return ((DartTreeItem) impl).getPaintedTexts();
    }

    public void setPaintedTexts(int[] value) {
    }

    public String[] getTexts() {
        return ((DartTreeItem) impl).getTexts();
    }

    public void setTexts(String[] value) {
        ((DartTreeItem) impl).strings = value;
    }

    public int getItemCount() {
        return ((DartTreeItem) impl).getItemCount();
    }

    public void setItemCount(int value) {
    }

    public static final String BACKGROUND = "background";

    public static final String BACKGROUNDS = "backgrounds";

    public static final String CHECKED = "checked";

    public static final String EXPANDED = "expanded";

    public static final String FONT = "font";

    public static final String FONTS = "fonts";

    public static final String FOREGROUND = "foreground";

    public static final String FOREGROUNDS = "foregrounds";

    public static final String GRAYED = "grayed";

    public static final String IMAGES = "images";

    public static final String ITEM_COUNT = "itemCount";

    public static final String ITEMS = "items";

    public static final String PAINTED_TEXTS = "paintedTexts";

    public static final String TEXTS = "texts";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "background":
                Serializer.writeKeyValue(writer, "background", getBackground());
                return;
            case "backgrounds":
                Serializer.writeKeyValue(writer, "backgrounds", getBackgrounds());
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
            case "fonts":
                Serializer.writeKeyValue(writer, "fonts", getFonts());
                return;
            case "foreground":
                Serializer.writeKeyValue(writer, "foreground", getForeground());
                return;
            case "foregrounds":
                Serializer.writeKeyValue(writer, "foregrounds", getForegrounds());
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
            case "paintedTexts":
                Serializer.writeKeyValue(writer, "paintedTexts", getPaintedTexts());
                return;
            case "texts":
                Serializer.writeKeyValue(writer, "texts", getTexts());
                return;
            case "itemCount":
                Serializer.writeKeyValue(writer, "itemCount", getItemCount());
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
