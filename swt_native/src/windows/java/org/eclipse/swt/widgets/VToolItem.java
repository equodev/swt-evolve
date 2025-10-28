package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

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

        public static ToolItem read(JsonReader<?> reader) {
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
