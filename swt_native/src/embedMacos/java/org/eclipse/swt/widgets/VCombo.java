package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.util.ArrayList;

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
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String v : values) if (v != null)
            result.add(v);
        return result.toArray(new String[0]);
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

    public static final int CARET_POSITION = 1410;

    public static final int ITEMS = 287;

    public static final int LIST_VISIBLE = 288;

    public static final int TEXT = 289;

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
            case 1410:
                w.write((int) getCaretPosition());
                return true;
            case 287:
                w.write(getItems());
                return true;
            case 288:
                w.write(getListVisible());
                return true;
            case 289:
                w.write(getText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1410:
                {
                    int v_ = getCaretPosition();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1410);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 287:
                {
                    String[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(287);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 288:
                {
                    boolean v_ = getListVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(288);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 289:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(289);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 1410, 287, 288, 289 });

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
