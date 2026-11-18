package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VControl extends VWidget {

    protected VControl() {
    }

    protected VControl(IControl impl) {
        super(impl);
    }

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

    public Long getDragSourceId() {
        return ((DartControl) impl).getDragSourceId();
    }

    public void setDragSourceId(Long value) {
    }

    public Long getDropTargetId() {
        return ((DartControl) impl).getDropTargetId();
    }

    public void setDropTargetId(Long value) {
    }

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

    public int getOrientation() {
        return ((DartControl) impl).getOrientation();
    }

    public void setOrientation(int value) {
        ((DartControl) impl).orientation = value;
    }

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

    public boolean getTabStop() {
        return ((DartControl) impl).getTabStop();
    }

    public void setTabStop(boolean value) {
    }

    public int getTextDirection() {
        return ((DartControl) impl).getTextDirection();
    }

    public void setTextDirection(int value) {
        ((DartControl) impl).textDirection = value;
    }

    public String getToolTipText() {
        return ((DartControl) impl)._displayedToolTipText();
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

    public Boolean getVisible() {
        return ((DartControl) impl).getVisible();
    }

    public void setVisible(Boolean value) {
        ((DartControl) impl).getApi().state = Boolean.TRUE.equals(value) ? (((DartControl) impl).getApi().state & ~DartWidget.HIDDEN) : (((DartControl) impl).getApi().state | DartWidget.HIDDEN);
    }

    public static final int BACKGROUND = 1152;

    public static final int BACKGROUND_IMAGE = 1153;

    public static final int BOUNDS = 1154;

    public static final int CURSOR = 1155;

    public static final int DRAG_DETECT = 1156;

    public static final int DRAG_SOURCE = 1157;

    public static final int DRAG_SOURCE_ID = 1158;

    public static final int DROP_TARGET_ID = 1159;

    public static final int ENABLED = 1160;

    public static final int FONT = 1161;

    public static final int FOREGROUND = 1162;

    public static final int HAS_OWN_BACKGROUND = 1163;

    public static final int INHERITS_BACKGROUND = 1164;

    public static final int MENU = 1165;

    public static final int REGION = 1166;

    public static final int TAB_STOP = 1167;

    public static final int TOOL_TIP_TEXT = 1168;

    public static final int VISIBLE = 1169;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BACKGROUND:
                return "background";
            case BACKGROUND_IMAGE:
                return "backgroundImage";
            case BOUNDS:
                return "bounds";
            case CURSOR:
                return "cursor";
            case DRAG_DETECT:
                return "dragDetect";
            case DRAG_SOURCE:
                return "dragSource";
            case DRAG_SOURCE_ID:
                return "dragSourceId";
            case DROP_TARGET_ID:
                return "dropTargetId";
            case ENABLED:
                return "enabled";
            case FONT:
                return "font";
            case FOREGROUND:
                return "foreground";
            case HAS_OWN_BACKGROUND:
                return "hasOwnBackground";
            case INHERITS_BACKGROUND:
                return "inheritsBackground";
            case MENU:
                return "menu";
            case REGION:
                return "region";
            case TAB_STOP:
                return "tabStop";
            case TOOL_TIP_TEXT:
                return "toolTipText";
            case VISIBLE:
                return "visible";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1152:
                w.write(getBackground());
                return true;
            case 1153:
                w.image(getBackgroundImage());
                return true;
            case 1154:
                w.write(getBounds());
                return true;
            case 1155:
                w.object((getCursor() == null || getCursor().isDisposed() ? null : ((DartCursor) getCursor().getImpl()).getValue()));
                return true;
            case 1156:
                w.write(getDragDetect());
                return true;
            case 1157:
                w.write(getDragSource());
                return true;
            case 1158:
                w.write(getDragSourceId());
                return true;
            case 1159:
                w.write(getDropTargetId());
                return true;
            case 1160:
                w.write(getEnabled());
                return true;
            case 1161:
                w.object((getFont() == null || getFont().isDisposed() ? null : ((DartFont) getFont().getImpl()).getValue()));
                return true;
            case 1162:
                w.write(getForeground());
                return true;
            case 1163:
                w.write(getHasOwnBackground());
                return true;
            case 1164:
                w.write(getInheritsBackground());
                return true;
            case 1165:
                w.ref(getMenu());
                return true;
            case 1166:
                w.object((getRegion() == null || getRegion().isDisposed() ? null : ((DartRegion) getRegion().getImpl()).getValue()));
                return true;
            case 1167:
                w.write(getTabStop());
                return true;
            case 1168:
                w.write(getToolTipText());
                return true;
            case 1169:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1152:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1152);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1153:
                {
                    Image v_ = getBackgroundImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1153);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1154:
                {
                    Rectangle v_ = getBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1154);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1155:
                {
                    Cursor v_ = getCursor();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartCursor) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1155);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1156:
                {
                    boolean v_ = getDragDetect();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1156);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1157:
                {
                    boolean v_ = getDragSource();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1157);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1158:
                {
                    Long v_ = getDragSourceId();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1158);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1159:
                {
                    Long v_ = getDropTargetId();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1159);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1160:
                {
                    Boolean v_ = getEnabled();
                    w.key(1160);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1161:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartFont) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1161);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1162:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1162);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1163:
                {
                    boolean v_ = getHasOwnBackground();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1163);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1164:
                {
                    boolean v_ = getInheritsBackground();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1164);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1165:
                {
                    Menu v_ = getMenu();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1165);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1166:
                {
                    Region v_ = getRegion();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartRegion) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1166);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1167:
                {
                    boolean v_ = getTabStop();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1167);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1168:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(1168);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1169:
                {
                    Boolean v_ = getVisible();
                    w.key(1169);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 1152, 1153, 1154, 1155, 1156, 1157, 1158, 1159, 1160, 1161, 1162, 1163, 1164, 1165, 1166, 1167, 1168, 1169 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        switch(key) {
            case 1160:
            case 1169:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
