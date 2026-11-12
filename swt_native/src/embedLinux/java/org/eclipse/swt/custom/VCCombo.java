package org.eclipse.swt.custom;

import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VCCombo extends VComposite {

    protected VCCombo() {
    }

    protected VCCombo(DartCCombo impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartCCombo) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartCCombo) impl).alignment = value;
    }

    public boolean getEditable() {
        return ((DartCCombo) impl).getEditable();
    }

    public void setEditable(boolean value) {
        ((DartCCombo) impl).editable = value;
    }

    public String[] getItemTooltips() {
        String[] values = ((DartCCombo) impl).itemTooltips;
        if (values == null)
            return null;
        int kept = 0;
        for (String v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        String[] result = new String[kept];
        kept = 0;
        for (String v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItemTooltips(String[] value) {
        ((DartCCombo) impl).itemTooltips = value;
    }

    public String[] getItems() {
        String[] values = ((DartCCombo) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (String v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        String[] result = new String[kept];
        kept = 0;
        for (String v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(String[] value) {
        ((DartCCombo) impl).items = value;
    }

    public boolean getListVisible() {
        return ((DartCCombo) impl).getListVisible();
    }

    public void setListVisible(boolean value) {
        ((DartCCombo) impl).listVisible = value;
    }

    public Point getSelection() {
        return ((DartCCombo) impl).selection;
    }

    public void setSelection(Point value) {
        ((DartCCombo) impl).selection = value;
    }

    public String getText() {
        return ((DartCCombo) impl).getText();
    }

    public void setText(String value) {
        ((DartCCombo) impl).setText(value);
    }

    public int getTextLimit() {
        return ((DartCCombo) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartCCombo) impl).textLimit = value;
    }

    public int getVisibleItemCount() {
        return ((DartCCombo) impl).getVisibleItemCount();
    }

    public void setVisibleItemCount(int value) {
        ((DartCCombo) impl).visibleItemCount = value;
    }

    public static final int ALIGNMENT = 18;

    public static final int EDITABLE = 19;

    public static final int ITEM_TOOLTIPS = 20;

    public static final int ITEMS = 21;

    public static final int LIST_VISIBLE = 22;

    public static final int SELECTION = 23;

    public static final int TEXT = 24;

    public static final int TEXT_LIMIT = 25;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
            case EDITABLE:
                return "editable";
            case ITEM_TOOLTIPS:
                return "itemTooltips";
            case ITEMS:
                return "items";
            case LIST_VISIBLE:
                return "listVisible";
            case SELECTION:
                return "selection";
            case TEXT:
                return "text";
            case TEXT_LIMIT:
                return "textLimit";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return false;
            case 18:
                w.write((int) getAlignment());
                return true;
            case 19:
                w.write(getEditable());
                return true;
            case 20:
                w.write(getItemTooltips());
                return true;
            case 21:
                w.write(getItems());
                return true;
            case 22:
                w.write(getListVisible());
                return true;
            case 23:
                w.write(getSelection());
                return true;
            case 24:
                w.write(getText());
                return true;
            case 25:
                w.write((int) getTextLimit());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 18:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(18);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 19:
                {
                    boolean v_ = getEditable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(19);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 20:
                {
                    String[] v_ = getItemTooltips();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(20);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 21:
                {
                    String[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(21);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 22:
                {
                    boolean v_ = getListVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(22);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 23:
                {
                    Point v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(23);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 24:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(24);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 25:
                {
                    int v_ = getTextLimit();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(25);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 18, 19, 20, 21, 22, 23, 24, 25 });

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
