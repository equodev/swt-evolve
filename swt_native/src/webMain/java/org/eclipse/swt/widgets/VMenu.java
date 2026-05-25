package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VMenu extends VWidget {

    protected VMenu() {
    }

    protected VMenu(DartMenu impl) {
        super(impl);
    }

    public MenuItem getDefaultItem() {
        MenuItem val = ((DartMenu) impl).defaultItem;
        if (val != null && !(val.getImpl() instanceof DartMenuItem))
            return null;
        return val;
    }

    public void setDefaultItem(MenuItem value) {
        ((DartMenu) impl).defaultItem = value;
    }

    public boolean getEnabled() {
        return ((DartMenu) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartMenu) impl).enabled = value;
    }

    public MenuItem[] getItems() {
        MenuItem[] values = ((DartMenu) impl).items;
        if (values == null)
            return null;
        ArrayList<MenuItem> result = new ArrayList<>(values.length);
        for (MenuItem v : values) if (v != null)
            result.add(v);
        return result.toArray(MenuItem[]::new);
    }

    public void setItems(MenuItem[] value) {
        ((DartMenu) impl).items = value;
    }

    public Point getLocation() {
        return ((DartMenu) impl).location;
    }

    public void setLocation(Point value) {
        ((DartMenu) impl).location = value;
    }

    public int getOrientation() {
        return ((DartMenu) impl).getOrientation();
    }

    public void setOrientation(int value) {
        ((DartMenu) impl).orientation = value;
    }

    public Menu getParentMenu() {
        Menu val = ((DartMenu) impl).parentMenu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setParentMenu(Menu value) {
        ((DartMenu) impl).parentMenu = value;
    }

    public boolean getVisible() {
        return ((DartMenu) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartMenu) impl).visible = value;
    }

    @JsonConverter(target = Menu.class)
    public static class MenuJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartMenu.class, (JsonWriter.WriteObject<DartMenu>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartMenu.class, (JsonReader.ReadObject<DartMenu>) reader -> {
                return null;
            });
        }

        public static Menu read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Menu api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
