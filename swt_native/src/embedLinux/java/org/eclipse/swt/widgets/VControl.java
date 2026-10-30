package org.eclipse.swt.widgets;

import java.lang.reflect.*;
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
        return ((DartControl) impl).background;
    }

    public void setBackground(Color value) {
        ((DartControl) impl).background = value;
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
        return ((DartControl) impl).bounds;
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
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartControl) impl).font = value;
    }

    public Color getForeground() {
        return ((DartControl) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartControl) impl).foreground = value;
    }

    public boolean getHasOwnBackground() {
        return true;
    }

    public void setHasOwnBackground(boolean value) {
    }

    public boolean getInheritsBackground() {
        return false;
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
        ((DartControl) impl).setOrientation(value);
    }

    public boolean getRedraw() {
        return ((DartControl) impl).redraw;
    }

    public void setRedraw(boolean value) {
        ((DartControl) impl).redraw = value;
    }

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
        return ((DartControl) impl).getToolTipText();
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

    public static final int BACKGROUND = 128;

    public static final int BACKGROUND_IMAGE = 129;

    public static final int BOUNDS = 130;

    public static final int CURSOR = 131;

    public static final int DRAG_DETECT = 132;

    public static final int DRAG_SOURCE = 133;

    public static final int DRAG_SOURCE_ID = 134;

    public static final int DROP_TARGET_ID = 135;

    public static final int ENABLED = 136;

    public static final int FONT = 137;

    public static final int FOREGROUND = 138;

    public static final int HAS_OWN_BACKGROUND = 139;

    public static final int INHERITS_BACKGROUND = 140;

    public static final int MENU = 141;

    public static final int TOOL_TIP_TEXT = 143;

    public static final int VISIBLE = 144;

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
            case 128:
                w.write(getBackground());
                return true;
            case 129:
                w.image(getBackgroundImage());
                return true;
            case 130:
                w.write(getBounds());
                return true;
            case 131:
                w.object((getCursor() == null || getCursor().isDisposed() ? null : ((DartCursor) getCursor().getImpl()).getValue()));
                return true;
            case 132:
                w.write(getDragDetect());
                return true;
            case 133:
                w.write(getDragSource());
                return true;
            case 134:
                w.write(getDragSourceId());
                return true;
            case 135:
                w.write(getDropTargetId());
                return true;
            case 136:
                w.write(getEnabled());
                return true;
            case 137:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 138:
                w.write(getForeground());
                return true;
            case 139:
                w.write(getHasOwnBackground());
                return true;
            case 140:
                w.write(getInheritsBackground());
                return true;
            case 141:
                w.ref(getMenu());
                return true;
            case 143:
                w.write(getToolTipText());
                return true;
            case 144:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 128:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(128);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 129:
                {
                    Image v_ = getBackgroundImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(129);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 130:
                {
                    Rectangle v_ = getBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(130);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 131:
                {
                    Cursor v_ = getCursor();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartCursor) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(131);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 132:
                {
                    boolean v_ = getDragDetect();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(132);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 133:
                {
                    boolean v_ = getDragSource();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(133);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 134:
                {
                    Long v_ = getDragSourceId();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(134);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 135:
                {
                    Long v_ = getDropTargetId();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(135);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 136:
                {
                    Boolean v_ = getEnabled();
                    w.key(136);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 137:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(137);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 138:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(138);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 139:
                {
                    boolean v_ = getHasOwnBackground();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(139);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 140:
                {
                    boolean v_ = getInheritsBackground();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(140);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 141:
                {
                    Menu v_ = getMenu();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(141);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 143:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(143);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 144:
                {
                    Boolean v_ = getVisible();
                    w.key(144);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 143, 144 });

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
            case 136:
            case 144:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
