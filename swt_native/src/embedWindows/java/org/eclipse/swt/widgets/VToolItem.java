package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VToolItem extends VItem {

    protected VToolItem() {
    }

    protected VToolItem(DartToolItem impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartToolItem) impl).getBackground();
    }

    public void setBackground(Color value) {
        ((DartToolItem) impl).setBackground(value);
    }

    public Control getControl() {
        Control val = ((DartToolItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartToolItem) impl).control = value;
    }

    public Image getDisabledImage() {
        Image val = ((DartToolItem) impl).disabledImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setDisabledImage(Image value) {
        ((DartToolItem) impl).disabledImage = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public boolean getEnabled() {
        return ((DartToolItem) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartToolItem) impl).enabled = value;
    }

    public Color getForeground() {
        return ((DartToolItem) impl).getForeground();
    }

    public void setForeground(Color value) {
        ((DartToolItem) impl).setForeground(value);
    }

    public Image getHotImage() {
        Image val = ((DartToolItem) impl).hotImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setHotImage(Image value) {
        ((DartToolItem) impl).hotImage = value;
    }

    public boolean getSelection() {
        return ((DartToolItem) impl).getSelection();
    }

    public void setSelection(boolean value) {
        ((DartToolItem) impl).selection = value;
    }

    public String getToolTipText() {
        return ((DartToolItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartToolItem) impl).toolTipText = value;
    }

    public int getWidth() {
        return ((DartToolItem) impl).getWidth();
    }

    public void setWidth(int value) {
        ((DartToolItem) impl).width = value;
    }

    public static final String BACKGROUND = "background";

    public static final String CONTROL = "control";

    public static final String DISABLED_IMAGE = "disabledImage";

    public static final String ENABLED = "enabled";

    public static final String FOREGROUND = "foreground";

    public static final String HOT_IMAGE = "hotImage";

    public static final String SELECTION = "selection";

    public static final String TOOL_TIP_TEXT = "toolTipText";

    public static final String WIDTH = "width";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "background":
                Serializer.writeKeyValue(writer, "background", getBackground());
                return;
            case "control":
                Serializer.writeKeyValue(writer, "control", getControl());
                return;
            case "disabledImage":
                Serializer.writeKeyValue(writer, "disabledImage", getDisabledImage());
                return;
            case "enabled":
                Serializer.writeKeyValue(writer, "enabled", getEnabled());
                return;
            case "foreground":
                Serializer.writeKeyValue(writer, "foreground", getForeground());
                return;
            case "hotImage":
                Serializer.writeKeyValue(writer, "hotImage", getHotImage());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
            case "toolTipText":
                Serializer.writeKeyValue(writer, "toolTipText", getToolTipText());
                return;
            case "width":
                Serializer.writeKeyValue(writer, "width", getWidth());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = ToolItem.class)
    public static class ToolItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartToolItem.class, (JsonWriter.WriteObject<DartToolItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartToolItem.class, (JsonReader.ReadObject<DartToolItem>) reader -> {
                return null;
            });
        }

        public static ToolItem read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, ToolItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
