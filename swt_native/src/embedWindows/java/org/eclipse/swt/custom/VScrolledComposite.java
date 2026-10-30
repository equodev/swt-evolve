package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VScrolledComposite extends VComposite {

    protected VScrolledComposite() {
    }

    protected VScrolledComposite(DartScrolledComposite impl) {
        super(impl);
    }

    public boolean getAlwaysShowScrollBars() {
        return ((DartScrolledComposite) impl).getAlwaysShowScrollBars();
    }

    public void setAlwaysShowScrollBars(boolean value) {
        ((DartScrolledComposite) impl).alwaysShowScroll = value;
    }

    public Control getContent() {
        Control val = ((DartScrolledComposite) impl).content;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setContent(Control value) {
        ((DartScrolledComposite) impl).content = value;
    }

    public boolean getExpandHorizontal() {
        return ((DartScrolledComposite) impl).getExpandHorizontal();
    }

    public void setExpandHorizontal(boolean value) {
        ((DartScrolledComposite) impl).expandHorizontal = value;
    }

    public boolean getExpandVertical() {
        return ((DartScrolledComposite) impl).getExpandVertical();
    }

    public void setExpandVertical(boolean value) {
        ((DartScrolledComposite) impl).expandVertical = value;
    }

    public int getMinHeight() {
        return ((DartScrolledComposite) impl).getMinHeight();
    }

    public void setMinHeight(int value) {
        ((DartScrolledComposite) impl).minHeight = value;
    }

    public int getMinSize() {
        return ((DartScrolledComposite) impl).minWidth;
    }

    public void setMinSize(int value) {
        ((DartScrolledComposite) impl).minWidth = value;
    }

    public int getMinWidth() {
        return ((DartScrolledComposite) impl).getMinWidth();
    }

    public void setMinWidth(int value) {
        ((DartScrolledComposite) impl).minWidth = value;
    }

    public Point getOrigin() {
        return ((DartScrolledComposite) impl).getOrigin();
    }

    public void setOrigin(Point value) {
        ((DartScrolledComposite) impl).origin = value;
    }

    public boolean getShowFocusedControl() {
        return ((DartScrolledComposite) impl).getShowFocusedControl();
    }

    public void setShowFocusedControl(boolean value) {
        ((DartScrolledComposite) impl).showFocusedControl = value;
    }

    public int getOriginRequest() {
        return ((DartScrolledComposite) impl).wireOriginRequest();
    }

    public static final int ALWAYS_SHOW_SCROLL_BARS = 273;

    public static final int CONTENT = 274;

    public static final int EXPAND_HORIZONTAL = 275;

    public static final int EXPAND_VERTICAL = 276;

    public static final int MIN_HEIGHT = 277;

    public static final int MIN_WIDTH = 278;

    public static final int ORIGIN = 1381;

    public static final int ORIGIN_REQUEST = 1382;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALWAYS_SHOW_SCROLL_BARS:
                return "alwaysShowScrollBars";
            case CONTENT:
                return "content";
            case EXPAND_HORIZONTAL:
                return "expandHorizontal";
            case EXPAND_VERTICAL:
                return "expandVertical";
            case MIN_HEIGHT:
                return "minHeight";
            case MIN_WIDTH:
                return "minWidth";
            case ORIGIN:
                return "origin";
            case ORIGIN_REQUEST:
                return "originRequest";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 273:
                w.write(getAlwaysShowScrollBars());
                return true;
            case 274:
                w.ref(getContent());
                return true;
            case 275:
                w.write(getExpandHorizontal());
                return true;
            case 276:
                w.write(getExpandVertical());
                return true;
            case 277:
                w.write((int) getMinHeight());
                return true;
            case 278:
                w.write((int) getMinWidth());
                return true;
            case 1381:
                w.write(getOrigin());
                return true;
            case 1382:
                w.write((int) getOriginRequest());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 273:
                {
                    boolean v_ = getAlwaysShowScrollBars();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(273);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 274:
                {
                    Control v_ = getContent();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(274);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 275:
                {
                    boolean v_ = getExpandHorizontal();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(275);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 276:
                {
                    boolean v_ = getExpandVertical();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(276);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 277:
                {
                    int v_ = getMinHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(277);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 278:
                {
                    int v_ = getMinWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(278);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1381:
                {
                    Point v_ = getOrigin();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1381);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1382:
                {
                    int v_ = getOriginRequest();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1382);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 273, 274, 275, 276, 277, 278, 1381, 1382 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
