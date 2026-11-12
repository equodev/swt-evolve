package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public class VComposite extends VScrollable {

    protected VComposite() {
    }

    protected VComposite(DartComposite impl) {
        super(impl);
    }

    public int getBackgroundMode() {
        return ((DartComposite) impl).getBackgroundMode();
    }

    public void setBackgroundMode(int value) {
        ((DartComposite) impl).backgroundMode = value;
    }

    public Control[] getChildren() {
        return ((DartComposite) impl)._displayedChildren();
    }

    public void setChildren(Control[] value) {
        ((DartComposite) impl).children = value;
    }

    public boolean getLayoutDeferred() {
        return ((DartComposite) impl).getLayoutDeferred();
    }

    public void setLayoutDeferred(boolean value) {
        ((DartComposite) impl).layoutDeferred = value;
    }

    public Control[] getTabList() {
        return ((DartComposite) impl).tabList;
    }

    public void setTabList(Control[] value) {
        ((DartComposite) impl).tabList = value;
    }

    public static final int BACKGROUND_MODE = 90;

    public static final int CHILDREN = 91;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BACKGROUND_MODE:
                return "backgroundMode";
            case CHILDREN:
                return "children";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 90:
                w.write((int) getBackgroundMode());
                return true;
            case 91:
                w.refs(getChildren());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 90:
                {
                    int v_ = getBackgroundMode();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(90);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 91:
                {
                    Control[] v_ = getChildren();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(91);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VScrollable.allWireKeys_(), new int[] { 90, 91 });

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
