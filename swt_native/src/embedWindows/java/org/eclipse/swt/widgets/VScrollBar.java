package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VScrollBar extends VWidget {

    protected VScrollBar() {
    }

    protected VScrollBar(DartScrollBar impl) {
        super(impl);
    }

    public boolean getEnabled() {
        return ((DartScrollBar) impl).getEnabled();
    }

    public void setEnabled(boolean value) {
        ((DartScrollBar) impl).enabled = value;
    }

    public int getIncrement() {
        return ((DartScrollBar) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartScrollBar) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartScrollBar) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartScrollBar) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartScrollBar) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartScrollBar) impl).minimum = value;
    }

    public int getPageIncrement() {
        return ((DartScrollBar) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartScrollBar) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartScrollBar) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartScrollBar) impl).selection = value;
    }

    public int getThumb() {
        return ((DartScrollBar) impl).getThumb();
    }

    public void setThumb(int value) {
        ((DartScrollBar) impl).thumb = value;
    }

    public boolean getVisible() {
        return ((DartScrollBar) impl).getVisible();
    }

    public void setVisible(boolean value) {
        ((DartScrollBar) impl).visible = value;
    }

    public static final int ENABLED = 443;

    public static final int INCREMENT = 444;

    public static final int MAXIMUM = 445;

    public static final int MINIMUM = 446;

    public static final int PAGE_INCREMENT = 447;

    public static final int SELECTION = 448;

    public static final int THUMB = 449;

    public static final int VISIBLE = 450;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ENABLED:
                return "enabled";
            case INCREMENT:
                return "increment";
            case MAXIMUM:
                return "maximum";
            case MINIMUM:
                return "minimum";
            case PAGE_INCREMENT:
                return "pageIncrement";
            case SELECTION:
                return "selection";
            case THUMB:
                return "thumb";
            case VISIBLE:
                return "visible";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                return false;
            case 443:
                w.write(getEnabled());
                return true;
            case 444:
                w.write((int) getIncrement());
                return true;
            case 445:
                w.write((int) getMaximum());
                return true;
            case 446:
                w.write((int) getMinimum());
                return true;
            case 447:
                w.write((int) getPageIncrement());
                return true;
            case 448:
                w.write((int) getSelection());
                return true;
            case 449:
                w.write((int) getThumb());
                return true;
            case 450:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 443:
                {
                    boolean v_ = getEnabled();
                    w.key(443);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 444:
                {
                    int v_ = getIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(444);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 445:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(445);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 446:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(446);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 447:
                {
                    int v_ = getPageIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(447);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 448:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(448);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 449:
                {
                    int v_ = getThumb();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(449);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 450:
                {
                    boolean v_ = getVisible();
                    w.key(450);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 443, 444, 445, 446, 447, 448, 449, 450 });

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
            case 443:
            case 450:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
