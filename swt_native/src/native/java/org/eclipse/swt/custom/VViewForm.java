package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VViewForm extends VComposite {

    protected VViewForm() {
    }

    protected VViewForm(DartViewForm impl) {
        super(impl);
    }

    public boolean getBorderVisible() {
        return ((DartViewForm) impl).showBorder;
    }

    public void setBorderVisible(boolean value) {
        ((DartViewForm) impl).showBorder = value;
    }

    public Control getContent() {
        Control val = ((DartViewForm) impl).content;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setContent(Control value) {
        ((DartViewForm) impl).content = value;
    }

    public Control getTopCenter() {
        Control val = ((DartViewForm) impl).topCenter;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopCenter(Control value) {
        ((DartViewForm) impl).topCenter = value;
    }

    public boolean getTopCenterSeparate() {
        return ((DartViewForm) impl).separateTopCenter;
    }

    public void setTopCenterSeparate(boolean value) {
        ((DartViewForm) impl).separateTopCenter = value;
    }

    public Control getTopLeft() {
        Control val = ((DartViewForm) impl).topLeft;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopLeft(Control value) {
        ((DartViewForm) impl).topLeft = value;
    }

    public Control getTopRight() {
        Control val = ((DartViewForm) impl).topRight;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopRight(Control value) {
        ((DartViewForm) impl).topRight = value;
    }

    public int getHighlight() {
        return ((DartViewForm) impl).highlight;
    }

    public int getSeparator() {
        return ((DartViewForm) impl).separator;
    }

    public static final int BORDER_VISIBLE = 674;

    public static final int HIGHLIGHT = 675;

    public static final int SEPARATOR = 676;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BORDER_VISIBLE:
                return "borderVisible";
            case HIGHLIGHT:
                return "highlight";
            case SEPARATOR:
                return "separator";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 148:
                return false;
            case 150:
                return false;
            case 674:
                w.write(getBorderVisible());
                return true;
            case 675:
                w.write((int) getHighlight());
                return true;
            case 676:
                w.write((int) getSeparator());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 148:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 150:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 674:
                {
                    boolean v_ = getBorderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(674);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 675:
                {
                    int v_ = getHighlight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(675);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 676:
                {
                    int v_ = getSeparator();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(676);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 674, 675, 676 });

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
