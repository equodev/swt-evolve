package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VTrayItem extends VItem {

    protected VTrayItem() {
    }

    protected VTrayItem(DartTrayItem impl) {
        super(impl);
    }

    public Image getHighlightImage() {
        Image val = ((DartTrayItem) impl).highlightImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setHighlightImage(Image value) {
        ((DartTrayItem) impl).highlightImage = value;
    }

    public ToolTip getToolTip() {
        ToolTip val = ((DartTrayItem) impl).toolTip;
        if (val != null && !(val.getImpl() instanceof DartToolTip))
            return null;
        return val;
    }

    public void setToolTip(ToolTip value) {
        ((DartTrayItem) impl).toolTip = value;
    }

    public String getToolTipText() {
        return ((DartTrayItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartTrayItem) impl).toolTipText = value;
    }

    public boolean getVisible() {
        return ((DartTrayItem) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartTrayItem) impl).visible = value;
    }

    @JsonConverter(target = TrayItem.class)
    public static class TrayItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTrayItem.class, (JsonWriter.WriteObject<DartTrayItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTrayItem.class, (JsonReader.ReadObject<DartTrayItem>) reader -> {
                return null;
            });
        }

        public static TrayItem read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, TrayItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
