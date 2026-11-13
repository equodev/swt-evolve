package org.eclipse.swt.widgets;

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
        MenuItem[] values = ((DartMenu) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (MenuItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        MenuItem[] result = new MenuItem[kept];
        kept = 0;
        for (MenuItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(MenuItem[] value) {
        ((DartMenu) impl).items = value;
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
        ((DartMenu) impl).orientation = value;
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

    public static final int ENABLED = 376;

    public static final int ITEMS = 377;

    public static final int LOCATION = 378;

    public static final int ORIENTATION = 379;

    public static final int VISIBLE = 380;

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
            case 376:
                w.write(getEnabled());
                return true;
            case 377:
                w.refs(getItems());
                return true;
            case 378:
                w.write(getLocation());
                return true;
            case 379:
                w.write((int) getOrientation());
                return true;
            case 380:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 376:
                {
                    boolean v_ = getEnabled();
                    w.key(376);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 377:
                {
                    MenuItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(377);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 378:
                {
                    Point v_ = getLocation();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(378);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 379:
                {
                    int v_ = getOrientation();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(379);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 380:
                {
                    boolean v_ = getVisible();
                    w.key(380);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 376, 377, 378, 379, 380 });

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
            case 376:
            case 380:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
