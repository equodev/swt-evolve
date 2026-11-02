package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
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
        Control[] values = ((DartComposite) impl)._displayedChildren();
        if (values == null)
            return null;
        int kept = 0;
        for (Control v : values) if (v != null && v.getImpl() instanceof DartControl)
            kept++;
        if (kept == values.length)
            return values;
        Control[] result = new Control[kept];
        kept = 0;
        for (Control v : values) if (v != null && v.getImpl() instanceof DartControl)
            result[kept++] = v;
        return result;
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
        Control[] values = ((DartComposite) impl).tabList;
        if (values == null)
            return null;
        int kept = 0;
        for (Control v : values) if (v != null && v.getImpl() instanceof DartControl)
            kept++;
        if (kept == values.length)
            return values;
        Control[] result = new Control[kept];
        kept = 0;
        for (Control v : values) if (v != null && v.getImpl() instanceof DartControl)
            result[kept++] = v;
        return result;
    }

    public void setTabList(Control[] value) {
        ((DartComposite) impl).tabList = value;
    }

    public static final int BACKGROUND_MODE = 85;

    public static final int CHILDREN = 86;

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
            case 85:
                w.write((int) getBackgroundMode());
                return true;
            case 86:
                w.refs(getChildren());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 85:
                {
                    int v_ = getBackgroundMode();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(85);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 86:
                {
                    Control[] v_ = getChildren();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(86);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VScrollable.allWireKeys_(), new int[] { 85, 86 });

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
