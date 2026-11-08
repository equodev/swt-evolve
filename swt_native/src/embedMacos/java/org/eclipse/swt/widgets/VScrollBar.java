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

    public static final int ENABLED = 436;

    public static final int INCREMENT = 437;

    public static final int MAXIMUM = 438;

    public static final int MINIMUM = 439;

    public static final int PAGE_INCREMENT = 440;

    public static final int SELECTION = 441;

    public static final int THUMB = 442;

    public static final int VISIBLE = 443;

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
            case 436:
                w.write(getEnabled());
                return true;
            case 437:
                w.write((int) getIncrement());
                return true;
            case 438:
                w.write((int) getMaximum());
                return true;
            case 439:
                w.write((int) getMinimum());
                return true;
            case 440:
                w.write((int) getPageIncrement());
                return true;
            case 441:
                w.write((int) getSelection());
                return true;
            case 442:
                w.write((int) getThumb());
                return true;
            case 443:
                w.write(getVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 436:
                {
                    boolean v_ = getEnabled();
                    w.key(436);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 437:
                {
                    int v_ = getIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(437);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 438:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(438);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 439:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(439);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 440:
                {
                    int v_ = getPageIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(440);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 441:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(441);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 442:
                {
                    int v_ = getThumb();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(442);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 443:
                {
                    boolean v_ = getVisible();
                    w.key(443);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VWidget.allWireKeys_(), new int[] { 436, 437, 438, 439, 440, 441, 442, 443 });

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
            case 436:
            case 443:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
