package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VMenu extends VWidget {

    protected VMenu() {
    }

    protected VMenu(DartMenu impl) {
        super(impl);
    }

    public MenuItem getDefaultItem() {
        MenuItem val = ((DartMenu) impl).defaultItem;
        if (val != null && !(val.getImpl() instanceof DartMenuItem))
            return null;
        return val;
    }

    public void setDefaultItem(MenuItem value) {
        ((DartMenu) impl).defaultItem = value;
    }

    public boolean getEnabled() {
        return ((DartMenu) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartMenu) impl).enabled = value;
    }

    public MenuItem[] getItems() {
        MenuItem[] values = ((DartMenu) impl).getItems();
        if (values == null)
            return null;
        ArrayList<MenuItem> result = new ArrayList<>(values.length);
        for (MenuItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new MenuItem[0]);
    }

    public void setItems(MenuItem[] value) {
        ((DartMenu) impl)._items = value;
    }

    public Point getLocation() {
        return ((DartMenu) impl).location;
    }

    public void setLocation(Point value) {
        ((DartMenu) impl).location = value;
    }

    public int getOrientation() {
        return ((DartMenu) impl).getOrientation();
    }

    public void setOrientation(int value) {
        ((DartMenu) impl)._setOrientation(value);
    }

    public Menu getParentMenu() {
        Menu val = ((DartMenu) impl).parentMenu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setParentMenu(Menu value) {
        ((DartMenu) impl).parentMenu = value;
    }

    public boolean getVisible() {
        return ((DartMenu) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartMenu) impl).visible = value;
    }

    public static final int ENABLED = 219;

    public static final int ITEMS = 220;

    public static final int LOCATION = 221;

    public static final int ORIENTATION = 222;

    public static final int VISIBLE = 223;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ENABLED:
                return "enabled";
            case ITEMS:
                return "items";
            case LOCATION:
                return "location";
            case ORIENTATION:
                return "orientation";
            case VISIBLE:
                return "visible";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 219:
                w.write(getEnabled());
                return true;
            case 220:
                w.refs(getItems());
                return true;
            case 221:
                w.write(getLocation());
                return true;
            case 222:
                w.write((int) getOrientation());
                return true;
            case 223:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 219:
                {
                    boolean v_ = getEnabled();
                    w.key(219);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 220:
                {
                    MenuItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(220);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 221:
                {
                    Point v_ = getLocation();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(221);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 222:
                {
                    int v_ = getOrientation();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(222);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 223:
                {
                    boolean v_ = getVisible();
                    w.key(223);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 219, 220, 221, 222, 223 });

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
            case 219:
            case 223:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
