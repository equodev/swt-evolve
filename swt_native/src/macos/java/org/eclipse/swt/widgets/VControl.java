package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

public class VControl extends VWidget {

    protected VControl() {
    }

    protected VControl(DartControl impl) {
        super(impl);
    }

    public double[] getBackground() {
        return ((DartControl) impl).background;
    }

    public void setBackground(double[] value) {
        ((DartControl) impl).background = value;
    }

    @JsonAttribute(ignore = true)
    public Image getBackgroundImage() {
        return ((DartControl) impl).backgroundImage;
    }

    public void setBackgroundImage(Image value) {
        ((DartControl) impl).backgroundImage = value;
    }

    public Rectangle getBounds() {
        return ((DartControl) impl).bounds;
    }

    public void setBounds(Rectangle value) {
        ((DartControl) impl).bounds = value;
    }

    public boolean getDragDetect() {
        return ((DartControl) impl).getDragDetect();
    }

    public void setDragDetect(boolean value) {
        ((DartControl) impl).dragDetect = value;
    }

    @JsonAttribute(ignore = true)
    public Cursor getCursor() {
        return ((DartControl) impl).cursor;
    }

    public void setCursor(Cursor value) {
        ((DartControl) impl).cursor = value;
    }

    public boolean getEnabled() {
        return ((DartControl) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartControl) impl).enabled = value;
    }

    @JsonAttribute(ignore = true)
    public Font getFont() {
        return ((DartControl) impl).font;
    }

    public void setFont(Font value) {
        ((DartControl) impl).font = value;
    }

    public double[] getForeground() {
        return ((DartControl) impl).foreground;
    }

    public void setForeground(double[] value) {
        ((DartControl) impl).foreground = value;
    }

    @JsonAttribute(ignore = true)
    public Menu getMenu() {
        return ((DartControl) impl).menu;
    }

    public void setMenu(Menu value) {
        ((DartControl) impl).menu = value;
    }

    public int getOrientation() {
        return ((DartControl) impl).getOrientation();
    }

    public void setOrientation(int value) {
        ((DartControl) impl).orientation = value;
    }

    @JsonAttribute(ignore = true)
    public Region getRegion() {
        return ((DartControl) impl).region;
    }

    public void setRegion(Region value) {
        ((DartControl) impl).region = value;
    }

    public int getTextDirection() {
        return ((DartControl) impl).getTextDirection();
    }

    public void setTextDirection(int value) {
        ((DartControl) impl).textDirection = value;
    }

    public String getToolTipText() {
        return ((DartControl) impl).toolTipText;
    }

    public void setToolTipText(String value) {
        ((DartControl) impl).toolTipText = value;
    }

    public boolean getTouchEnabled() {
        return ((DartControl) impl).getTouchEnabled();
    }

    public void setTouchEnabled(boolean value) {
        ((DartControl) impl).touchEnabled = value;
    }

    public boolean getVisible() {
        return ((DartControl) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartControl) impl).visible = value;
    }

    public boolean getCapture() {
        return ((DartControl) impl).capture;
    }

    public void setCapture(boolean value) {
        ((DartControl) impl).capture = value;
    }

    public boolean getRedraw() {
        return ((DartControl) impl).redraw;
    }

    public void setRedraw(boolean value) {
        ((DartControl) impl).redraw = value;
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

        public static Control read(JsonReader<?> reader) {
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
