package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VMenuItem extends VItem {

    protected VMenuItem() {
    }

    protected VMenuItem(DartMenuItem impl) {
        super(impl);
    }

    @JsonAttribute(name = "ID")
    public int getID() {
        return ((DartMenuItem) impl).getID();
    }

    public void setID(int value) {
        ((DartMenuItem) impl).userId = value;
    }

    public int getAccelerator() {
        return ((DartMenuItem) impl).getAccelerator();
    }

    public void setAccelerator(int value) {
        ((DartMenuItem) impl).accelerator = value;
    }

    public boolean getEnabled() {
        return ((DartMenuItem) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartMenuItem) impl).enabled = value;
    }

    public Menu getMenu() {
        Menu val = ((DartMenuItem) impl).menu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setMenu(Menu value) {
        ((DartMenuItem) impl).menu = value;
    }

    public boolean getSelection() {
        return ((DartMenuItem) impl).getSelection();
    }

    public void setSelection(boolean value) {
        ((DartMenuItem) impl).selection = value;
    }

    public String getToolTipText() {
        return ((DartMenuItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartMenuItem) impl).toolTipText = value;
    }

    @JsonConverter(target = MenuItem.class)
    public static class MenuItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartMenuItem.class, (JsonWriter.WriteObject<DartMenuItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartMenuItem.class, (JsonReader.ReadObject<DartMenuItem>) reader -> {
                return null;
            });
        }

        public static MenuItem read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, MenuItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
