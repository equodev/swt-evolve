package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VControl extends VWidget {

    protected VControl() {
    }

    protected VControl(IControl impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public AutoscalingMode getAutoscalingMode() {
        return ((DartControl) impl).autoscalingMode;
    }

    public void setAutoscalingMode(AutoscalingMode value) {
        ((DartControl) impl).autoscalingMode = value;
    }

    public Color getBackground() {
        return ((DartControl) impl).getExplicitBackground();
    }

    public void setBackground(Color value) {
        ((DartControl) impl)._background = value;
    }

    public Image getBackgroundImage() {
        Image val = ((DartControl) impl).backgroundImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setBackgroundImage(Image value) {
        ((DartControl) impl).backgroundImage = value;
    }

    @JsonAttribute(nullable = true)
    public Rectangle getBounds() {
        DartControl c = ((DartControl) impl);
        Rectangle b = c.bounds;
        if (!c.laidOut)
            return new Rectangle(b.x, b.y, -1, -1);
        return new Rectangle(b.x, b.y, b.width, b.height);
    }

    public void setBounds(Rectangle value) {
        ((DartControl) impl).bounds = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getCapture() {
        return ((DartControl) impl).capture;
    }

    public void setCapture(boolean value) {
        ((DartControl) impl).capture = value;
    }

    public Cursor getCursor() {
        Cursor val = ((DartControl) impl).cursor;
        if (val != null && !(val.getImpl() instanceof DartCursor))
            return null;
        return val;
    }

    public void setCursor(Cursor value) {
        ((DartControl) impl).cursor = value;
    }

    public boolean getDragDetect() {
        return ((DartControl) impl).getDragDetect();
    }

    public void setDragDetect(boolean value) {
        ((DartControl) impl).dragDetect = value;
    }

    public boolean getDragSource() {
        return ((DartControl) impl).getDragSource();
    }

    public void setDragSource(boolean value) {
    }

    @JsonAttribute(nullable = true)
    public Long getDropTargetId() {
        return ((DartControl) impl).getDropTargetId();
    }

    public void setDropTargetId(Long value) {
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public Boolean getEnabled() {
        return ((DartControl) impl).getEnabled();
    }

    public void setEnabled(Boolean value) {
        ((DartControl) impl).enabled = Boolean.TRUE.equals(value);
    }

    public Font getFont() {
        Font val = ((DartControl) impl).font;
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartControl) impl).font = value;
    }

    public Color getForeground() {
        return ((DartControl) impl).getForeground();
    }

    public void setForeground(Color value) {
        ((DartControl) impl).setForeground(value);
    }

    public boolean getHasOwnBackground() {
        return ((DartControl) impl).getHasOwnBackground();
    }

    public void setHasOwnBackground(boolean value) {
    }

    public boolean getInheritsBackground() {
        return ((DartControl) impl).getInheritsBackground();
    }

    public void setInheritsBackground(boolean value) {
    }

    public Menu getMenu() {
        Menu val = ((DartControl) impl).menu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setMenu(Menu value) {
        ((DartControl) impl).menu = value;
    }

    @JsonAttribute(ignore = true)
    public int getOrientation() {
        return ((DartControl) impl).getOrientation();
    }

    public void setOrientation(int value) {
        ((DartControl) impl).orientation = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getRedraw() {
        return ((DartControl) impl).redraw;
    }

    public void setRedraw(boolean value) {
        ((DartControl) impl).redraw = value;
    }

    public Region getRegion() {
        Region val = ((DartControl) impl).region;
        if (val != null && !(val.getImpl() instanceof DartRegion))
            return null;
        return val;
    }

    public void setRegion(Region value) {
        ((DartControl) impl).region = value;
    }

    @JsonAttribute(ignore = true)
    public int getTextDirection() {
        return ((DartControl) impl).getTextDirection();
    }

    public void setTextDirection(int value) {
        ((DartControl) impl).textDirection = value;
    }

    public String getToolTipText() {
        return ((DartControl) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartControl) impl).toolTipText = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getTouchEnabled() {
        return ((DartControl) impl).getTouchEnabled();
    }

    public void setTouchEnabled(boolean value) {
        ((DartControl) impl).touchEnabled = value;
    }

    public Boolean getVisible() {
        return ((DartControl) impl).getVisible();
    }

    public void setVisible(Boolean value) {
        ((DartControl) impl).getApi().state = Boolean.TRUE.equals(value) ? (((DartControl) impl).getApi().state & ~DartWidget.HIDDEN) : (((DartControl) impl).getApi().state | DartWidget.HIDDEN);
    }

    public static final String BACKGROUND = "background";

    public static final String BACKGROUND_IMAGE = "backgroundImage";

    public static final String BOUNDS = "bounds";

    public static final String CURSOR = "cursor";

    public static final String DRAG_DETECT = "dragDetect";

    public static final String DRAG_SOURCE = "dragSource";

    public static final String DROP_TARGET_ID = "dropTargetId";

    public static final String ENABLED = "enabled";

    public static final String FONT = "font";

    public static final String FOREGROUND = "foreground";

    public static final String HAS_OWN_BACKGROUND = "hasOwnBackground";

    public static final String INHERITS_BACKGROUND = "inheritsBackground";

    public static final String MENU = "menu";

    public static final String REGION = "region";

    public static final String TOOL_TIP_TEXT = "toolTipText";

    public static final String VISIBLE = "visible";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "background":
                Serializer.writeKeyValue(writer, "background", getBackground());
                return;
            case "backgroundImage":
                Serializer.writeKeyValue(writer, "backgroundImage", getBackgroundImage());
                return;
            case "bounds":
                Serializer.writeKeyValue(writer, "bounds", getBounds());
                return;
            case "cursor":
                Serializer.writeKeyValue(writer, "cursor", getCursor());
                return;
            case "dragDetect":
                Serializer.writeKeyValue(writer, "dragDetect", getDragDetect());
                return;
            case "dragSource":
                Serializer.writeKeyValue(writer, "dragSource", getDragSource());
                return;
            case "dropTargetId":
                Serializer.writeKeyValue(writer, "dropTargetId", getDropTargetId());
                return;
            case "enabled":
                Serializer.writeKeyValue(writer, "enabled", getEnabled());
                return;
            case "font":
                Serializer.writeKeyValue(writer, "font", getFont());
                return;
            case "foreground":
                Serializer.writeKeyValue(writer, "foreground", getForeground());
                return;
            case "hasOwnBackground":
                Serializer.writeKeyValue(writer, "hasOwnBackground", getHasOwnBackground());
                return;
            case "inheritsBackground":
                Serializer.writeKeyValue(writer, "inheritsBackground", getInheritsBackground());
                return;
            case "menu":
                Serializer.writeKeyValue(writer, "menu", getMenu());
                return;
            case "region":
                Serializer.writeKeyValue(writer, "region", getRegion());
                return;
            case "toolTipText":
                Serializer.writeKeyValue(writer, "toolTipText", getToolTipText());
                return;
            case "visible":
                Serializer.writeKeyValue(writer, "visible", getVisible());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Control.class)
    public static class ControlJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartControl.class, (JsonWriter.WriteObject<DartControl>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartControl.class, (JsonReader.ReadObject<DartControl>) reader -> {
                return null;
            });
        }

        public static Control read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Control api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
