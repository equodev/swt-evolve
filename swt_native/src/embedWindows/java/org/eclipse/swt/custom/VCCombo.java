package org.eclipse.swt.custom;

import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.ArrayList;

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
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
    }

    public void setItemTooltips(String[] value) {
        ((DartCCombo) impl).itemTooltips = value;
    }

    public String[] getItems() {
        String[] values = ((DartCCombo) impl).items;
        if (values == null)
            return null;
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
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

    public static final int ALIGNMENT = 501;

    public static final int ITEM_TOOLTIPS = 502;

    public static final int ITEMS = 503;

    public static final int LIST_VISIBLE = 504;

    public static final int SELECTION = 505;

    public static final int TEXT = 506;

    public static final int TEXT_LIMIT = 507;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
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
            case 501:
                w.write((int) getAlignment());
                return true;
            case 502:
                w.write(getItemTooltips());
                return true;
            case 503:
                w.write(getItems());
                return true;
            case 504:
                w.write(getListVisible());
                return true;
            case 505:
                w.write(getSelection());
                return true;
            case 506:
                w.write(getText());
                return true;
            case 507:
                w.write((int) getTextLimit());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 501:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(501);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 502:
                {
                    String[] v_ = getItemTooltips();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(502);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 503:
                {
                    String[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(503);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 504:
                {
                    boolean v_ = getListVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(504);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 505:
                {
                    Point v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(505);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 506:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(506);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 507:
                {
                    int v_ = getTextLimit();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(507);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 501, 502, 503, 504, 505, 506, 507 });

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
