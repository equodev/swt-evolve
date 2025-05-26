package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;

@CompiledJson()
public class VControl extends VWidget {

    public Color background;

    @JsonAttribute(ignore = true)
    public Image backgroundImage;

    public Rectangle bounds;

    public boolean dragDetect;

    @JsonAttribute(ignore = true)
    public Cursor cursor;

    public boolean enabled;

    @JsonAttribute(ignore = true)
    public Font font;

    public Color foreground;

    @JsonAttribute(ignore = true)
    public Object layoutData;

    @JsonAttribute(ignore = true)
    public Menu menu;

    public int orientation;

    @JsonAttribute(ignore = true)
    public Region region;

    public int textDirection;

    public String toolTipText;

    public boolean touchEnabled;

    public boolean visible;

    @JsonConverter(target = Control.class)
    public abstract static class ControlJson {

        public static Control read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Control api) {
            if (api == null)
                writer.writeNull();
            else {
                VControl value = ((DartControl) api.getImpl()).getValue();
                writer.serializeObject(value);
            }
        }
    }
}
