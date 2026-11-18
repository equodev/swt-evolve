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
        return ScrolledCompositeHelper.wireOrigin((DartScrolledComposite) impl);
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

    public static final int ALWAYS_SHOW_SCROLL_BARS = 4544;

    public static final int CONTENT = 4545;

    public static final int EXPAND_HORIZONTAL = 4546;

    public static final int EXPAND_VERTICAL = 4547;

    public static final int MIN_HEIGHT = 4548;

    public static final int MIN_WIDTH = 4549;

    public static final int ORIGIN = 4550;

    public static final int ORIGIN_REQUEST = 4551;

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
            case 1161:
                return false;
            case 1163:
                return false;
            case 4544:
                w.write(getAlwaysShowScrollBars());
                return true;
            case 4545:
                w.ref(getContent());
                return true;
            case 4546:
                w.write(getExpandHorizontal());
                return true;
            case 4547:
                w.write(getExpandVertical());
                return true;
            case 4548:
                w.write((int) getMinHeight());
                return true;
            case 4549:
                w.write((int) getMinWidth());
                return true;
            case 4550:
                w.write(getOrigin());
                return true;
            case 4551:
                w.write((int) getOriginRequest());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1161:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 1163:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 4544:
                {
                    boolean v_ = getAlwaysShowScrollBars();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4544);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4545:
                {
                    Control v_ = getContent();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4545);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4546:
                {
                    boolean v_ = getExpandHorizontal();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4546);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4547:
                {
                    boolean v_ = getExpandVertical();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4547);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4548:
                {
                    int v_ = getMinHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4548);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4549:
                {
                    int v_ = getMinWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4549);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4550:
                {
                    Point v_ = getOrigin();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4550);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4551:
                {
                    int v_ = getOriginRequest();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4551);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 4544, 4545, 4546, 4547, 4548, 4549, 4550, 4551 });

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
