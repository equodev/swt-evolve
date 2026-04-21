package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VItem extends VWidget {

    protected VItem() {
    }

    protected VItem(IItem impl) {
        super(impl);
    }

    @JsonAttribute(nullable = true)
    public Image getImage() {
        Image val = ((DartItem) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartItem) impl).image = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartItem) impl).getText();
    }

    public void setText(String value) {
        ((DartItem) impl).text = value;
    }

    @JsonConverter(target = Item.class)
    public static class ItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartItem.class, (JsonWriter.WriteObject<DartItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartItem.class, (JsonReader.ReadObject<DartItem>) reader -> {
                return null;
            });
        }

        public static Item read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Item api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
