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

    public static final int CARET_POSITION = 85;

    public static final int ITEMS = 86;

    public static final int LIST_VISIBLE = 87;

    public static final int TEXT = 88;

    public static final int TEXT_LIMIT = 89;

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
            case TEXT_LIMIT:
                return "textLimit";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 146:
                return false;
            case 85:
                w.write((int) getCaretPosition());
                return true;
            case 86:
                w.write(getItems());
                return true;
            case 87:
                w.write(getListVisible());
                return true;
            case 88:
                w.write(getText());
                return true;
            case 89:
                w.write((int) getTextLimit());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 146:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 85:
                {
                    int v_ = getCaretPosition();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(85);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 86:
                {
                    String[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(86);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 87:
                {
                    boolean v_ = getListVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(87);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 88:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(88);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 89:
                {
                    int v_ = getTextLimit();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(89);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 85, 86, 87, 88, 89 });

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
