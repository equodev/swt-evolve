package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VTracker extends VWidget {

    protected VTracker() {
    }

    protected VTracker(DartTracker impl) {
        super(impl);
    }

    public Cursor getCursor() {
        Cursor val = ((DartTracker) impl).clientCursor;
        if (val != null && !(val.getImpl() instanceof DartCursor))
            return null;
        return val;
    }

    public void setCursor(Cursor value) {
        ((DartTracker) impl).clientCursor = value;
    }

    public Rectangle[] getRectangles() {
        Rectangle[] values = ((DartTracker) impl).rectangles;
        if (values == null)
            return null;
        ArrayList<Rectangle> result = new ArrayList<>(values.length);
        for (Rectangle v : values) if (v != null)
            result.add(v);
        return result.toArray(Rectangle[]::new);
    }

    public void setRectangles(Rectangle[] value) {
        ((DartTracker) impl).rectangles = value;
    }

    public boolean getStippled() {
        return ((DartTracker) impl).getStippled();
    }

    public void setStippled(boolean value) {
        ((DartTracker) impl).stippled = value;
    }

    @JsonConverter(target = Tracker.class)
    public static class TrackerJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTracker.class, (JsonWriter.WriteObject<DartTracker>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTracker.class, (JsonReader.ReadObject<DartTracker>) reader -> {
                return null;
            });
        }

        public static Tracker read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Tracker api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
