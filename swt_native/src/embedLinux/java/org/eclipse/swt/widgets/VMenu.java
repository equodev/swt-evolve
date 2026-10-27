package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VMenu extends VWidget {

    protected VMenu() {
    }

    protected VMenu(DartMenu impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public MenuItem getDefaultItem() {
        MenuItem val = ((DartMenu) impl).defaultItem;
        if (val != null && !(val.getImpl() instanceof DartMenuItem))
            return null;
        return val;
    }

    public void setDefaultItem(MenuItem value) {
        ((DartMenu) impl).defaultItem = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public boolean getEnabled() {
        return ((DartMenu) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartMenu) impl).enabled = value;
    }

    public MenuItem[] getItems() {
        MenuItem[] values = ((DartMenu) impl).getItems();
        if (values == null)
            return null;
        ArrayList<MenuItem> result = new ArrayList<>(values.length);
        for (MenuItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new MenuItem[0]);
    }

    public void setItems(MenuItem[] value) {
        ((DartMenu) impl)._items = value;
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
        ((DartMenu) impl)._setOrientation(value);
    }

    @JsonAttribute(ignore = true)
    public Menu getParentMenu() {
        Menu val = ((DartMenu) impl).parentMenu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setParentMenu(Menu value) {
        ((DartMenu) impl).parentMenu = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public boolean getVisible() {
        return ((DartMenu) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartMenu) impl).visible = value;
    }

    public static final String ENABLED = "enabled";

    public static final String ITEMS = "items";

    public static final String LOCATION = "location";

    public static final String ORIENTATION = "orientation";

    public static final String VISIBLE = "visible";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "enabled":
                Serializer.writeKeyValue(writer, "enabled", getEnabled());
                return;
            case "items":
                Serializer.writeKeyValue(writer, "items", getItems());
                return;
            case "location":
                Serializer.writeKeyValue(writer, "location", getLocation());
                return;
            case "orientation":
                Serializer.writeKeyValue(writer, "orientation", getOrientation());
                return;
            case "visible":
                Serializer.writeKeyValue(writer, "visible", getVisible());
                return;
        }
        super.writeProperty(writer, key);
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

        public static Menu read(JsonReader<?> reader) throws IOException {
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
