package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VCTabItem extends VItem {

    protected VCTabItem() {
    }

    protected VCTabItem(DartCTabItem impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartCTabItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartCTabItem) impl).control = value;
    }

    @JsonAttribute(ignore = true)
    public Image getDisabledImage() {
        Image val = ((DartCTabItem) impl).disabledImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setDisabledImage(Image value) {
        ((DartCTabItem) impl).disabledImage = value;
    }

    public Font getFont() {
        Font val = ((DartCTabItem) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartCTabItem) impl).font = value;
    }

    public Color getForeground() {
        return ((DartCTabItem) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartCTabItem) impl).foreground = value;
    }

    public Color getSelectionForeground() {
        return ((DartCTabItem) impl).selectionForeground;
    }

    public void setSelectionForeground(Color value) {
        ((DartCTabItem) impl).selectionForeground = value;
    }

    public boolean getShowClose() {
        return ((DartCTabItem) impl).getShowClose();
    }

    public void setShowClose(boolean value) {
        ((DartCTabItem) impl).showClose = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getShowDirty() {
        return ((DartCTabItem) impl).getShowDirty();
    }

    public void setShowDirty(boolean value) {
        ((DartCTabItem) impl).showDirty = value;
    }

    public boolean getShowing() {
        return ((DartCTabItem) impl).showing;
    }

    public void setShowing(boolean value) {
    }

    public String getToolTipText() {
        return ((DartCTabItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartCTabItem) impl).toolTipText = value;
    }

    public static final String CONTROL = "control";

    public static final String FONT = "font";

    public static final String FOREGROUND = "foreground";

    public static final String SELECTION_FOREGROUND = "selectionForeground";

    public static final String SHOW_CLOSE = "showClose";

    public static final String SHOWING = "showing";

    public static final String TOOL_TIP_TEXT = "toolTipText";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "control":
                Serializer.writeKeyValue(writer, "control", getControl());
                return;
            case "font":
                Serializer.writeKeyValue(writer, "font", getFont());
                return;
            case "foreground":
                Serializer.writeKeyValue(writer, "foreground", getForeground());
                return;
            case "selectionForeground":
                Serializer.writeKeyValue(writer, "selectionForeground", getSelectionForeground());
                return;
            case "showClose":
                Serializer.writeKeyValue(writer, "showClose", getShowClose());
                return;
            case "showing":
                Serializer.writeKeyValue(writer, "showing", getShowing());
                return;
            case "toolTipText":
                Serializer.writeKeyValue(writer, "toolTipText", getToolTipText());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = CTabItem.class)
    public static class CTabItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCTabItem.class, (JsonWriter.WriteObject<DartCTabItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCTabItem.class, (JsonReader.ReadObject<DartCTabItem>) reader -> {
                return null;
            });
        }

        public static CTabItem read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, CTabItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
