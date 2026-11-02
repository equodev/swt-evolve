package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VCombo extends VComposite {

    protected VCombo() {
    }

    protected VCombo(DartCombo impl) {
        super(impl);
    }

    public int getCaretPosition() {
        return ((DartCombo) impl).getCaretPosition();
    }

    public void setCaretPosition(int value) {
        ((DartCombo) impl).caretPosition = value;
    }

    public String[] getItems() {
        String[] values = ((DartCombo) impl).items;
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
        ((DartCombo) impl).items = value;
    }

    public boolean getListVisible() {
        return ((DartCombo) impl).getListVisible();
    }

    public void setListVisible(boolean value) {
        ((DartCombo) impl).listVisible = value;
    }

    public Point getSelection() {
        return ((DartCombo) impl).selection;
    }

    public void setSelection(Point value) {
        ((DartCombo) impl).selection = value;
    }

    public String getText() {
        return ((DartCombo) impl).getText();
    }

    public void setText(String value) {
        ((DartCombo) impl).text = value;
    }

    public int getTextLimit() {
        return ((DartCombo) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartCombo) impl).textLimit = value;
    }

    public int getVisibleItemCount() {
        return ((DartCombo) impl).getVisibleItemCount();
    }

    public void setVisibleItemCount(int value) {
        ((DartCombo) impl).visibleItemCount = value;
    }

    public static final int CARET_POSITION = 81;

    public static final int ITEMS = 82;

    public static final int LIST_VISIBLE = 83;

    public static final int TEXT = 84;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CARET_POSITION:
                return "caretPosition";
            case ITEMS:
                return "items";
            case LIST_VISIBLE:
                return "listVisible";
            case TEXT:
                return "text";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 81:
                w.write((int) getCaretPosition());
                return true;
            case 82:
                w.write(getItems());
                return true;
            case 83:
                w.write(getListVisible());
                return true;
            case 84:
                w.write(getText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 81:
                {
                    int v_ = getCaretPosition();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(81);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 82:
                {
                    String[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(82);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 83:
                {
                    boolean v_ = getListVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(83);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 84:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(84);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 81, 82, 83, 84 });

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
