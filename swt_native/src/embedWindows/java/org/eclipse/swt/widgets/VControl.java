package org.eclipse.swt.widgets;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.ole.win32.*;

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
        return ((DartControl) impl).getBackground();
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
        Rectangle b = ((DartControl) impl).bounds;
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
        return ((DartControl) impl).getForeground();
    }

    public void setForeground(Color value) {
        ((DartControl) impl).setForeground(value);
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

    public static final int BACKGROUND = 290;

    public static final int BACKGROUND_IMAGE = 291;

    public static final int BOUNDS = 292;

    public static final int CURSOR = 293;

    public static final int DRAG_DETECT = 294;

    public static final int DRAG_SOURCE = 295;

    public static final int DROP_TARGET_ID = 296;

    public static final int ENABLED = 297;

    public static final int FONT = 298;

    public static final int FOREGROUND = 299;

    public static final int HAS_OWN_BACKGROUND = 300;

    public static final int INHERITS_BACKGROUND = 301;

    public static final int MENU = 302;

    public static final int TOOL_TIP_TEXT = 303;

    public static final int VISIBLE = 304;

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
            case 290:
                w.write(getBackground());
                return true;
            case 291:
                w.image(getBackgroundImage());
                return true;
            case 292:
                w.write(getBounds());
                return true;
            case 293:
                w.object((getCursor() == null || getCursor().isDisposed() ? null : ((DartCursor) getCursor().getImpl()).getValue()));
                return true;
            case 294:
                w.write(getDragDetect());
                return true;
            case 295:
                w.write(getDragSource());
                return true;
            case 296:
                w.write(getDropTargetId());
                return true;
            case 297:
                w.write(getEnabled());
                return true;
            case 298:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 299:
                w.write(getForeground());
                return true;
            case 300:
                w.write(getHasOwnBackground());
                return true;
            case 301:
                w.write(getInheritsBackground());
                return true;
            case 302:
                w.ref(getMenu());
                return true;
            case 303:
                w.write(getToolTipText());
                return true;
            case 304:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 290:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(290);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 291:
                {
                    Image v_ = getBackgroundImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(291);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 292:
                {
                    Rectangle v_ = getBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(292);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 293:
                {
                    Cursor v_ = getCursor();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartCursor) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(293);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 294:
                {
                    boolean v_ = getDragDetect();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(294);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 295:
                {
                    boolean v_ = getDragSource();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(295);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 296:
                {
                    Long v_ = getDropTargetId();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(296);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 297:
                {
                    Boolean v_ = getEnabled();
                    w.key(297);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 298:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(298);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 299:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(299);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 300:
                {
                    boolean v_ = getHasOwnBackground();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(300);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 301:
                {
                    boolean v_ = getInheritsBackground();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(301);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 302:
                {
                    Menu v_ = getMenu();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(302);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 303:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(303);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 304:
                {
                    Boolean v_ = getVisible();
                    w.key(304);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304 });

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
            case 297:
            case 304:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
