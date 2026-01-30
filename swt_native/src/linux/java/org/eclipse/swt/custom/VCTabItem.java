package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

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

    public String getToolTipText() {
        return ((DartCTabItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartCTabItem) impl).toolTipText = value;
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

        public static CTabItem read(JsonReader<?> reader) {
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
