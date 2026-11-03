package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VScrollable extends VControl {

    protected VScrollable() {
    }

    protected VScrollable(IScrollable impl) {
        super(impl);
    }

    public ScrollBar getHorizontalBar() {
        ScrollBar val = ((DartScrollable) impl).horizontalBar;
        if (val != null && !(val.getImpl() instanceof DartScrollBar))
            return null;
        return val;
    }

    public void setHorizontalBar(ScrollBar value) {
        ((DartScrollable) impl).horizontalBar = value;
    }

    public int getScrollbarsMode() {
        return ((DartScrollable) impl).getScrollbarsMode();
    }

    public void setScrollbarsMode(int value) {
        ((DartScrollable) impl).scrollbarsMode = value;
    }

    public ScrollBar getVerticalBar() {
        ScrollBar val = ((DartScrollable) impl).verticalBar;
        if (val != null && !(val.getImpl() instanceof DartScrollBar))
            return null;
        return val;
    }

    public void setVerticalBar(ScrollBar value) {
        ((DartScrollable) impl).verticalBar = value;
    }

    public static final int HORIZONTAL_BAR = 441;

    public static final int VERTICAL_BAR = 442;

    @Override
    public String wireName(int key) {
        switch(key) {
            case HORIZONTAL_BAR:
                return "horizontalBar";
            case VERTICAL_BAR:
                return "verticalBar";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 441:
                w.ref(getHorizontalBar());
                return true;
            case 442:
                w.ref(getVerticalBar());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 441:
                {
                    ScrollBar v_ = getHorizontalBar();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(441);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 442:
                {
                    ScrollBar v_ = getVerticalBar();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(442);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VControl.allWireKeys_(), new int[] { 441, 442 });

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
