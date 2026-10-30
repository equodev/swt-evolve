package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VShell extends VDecorations {

    protected VShell() {
    }

    protected VShell(DartShell impl) {
        super(impl);
    }

    public int getAlpha() {
        return ((DartShell) impl).getAlpha();
    }

    public void setAlpha(int value) {
        ((DartShell) impl).alpha = value;
    }

    public boolean getDarkThemePreferred() {
        return ((DartShell) impl).darkThemePreferred;
    }

    public void setDarkThemePreferred(boolean value) {
        ((DartShell) impl).darkThemePreferred = value;
    }

    public Dialog[] getDialogs() {
        return ((DartShell) impl).getDialogs();
    }

    public void setDialogs(Dialog[] value) {
        ((DartShell) impl).dialogs = value;
    }

    public boolean getFullScreen() {
        return ((DartShell) impl).getFullScreen();
    }

    public void setFullScreen(boolean value) {
        ((DartShell) impl).fullScreen = value;
    }

    public int getImeInputMode() {
        return ((DartShell) impl).getImeInputMode();
    }

    public void setImeInputMode(int value) {
        ((DartShell) impl).imeInputMode = value;
    }

    public Point getMaximumSize() {
        return ((DartShell) impl).maximumSize;
    }

    public void setMaximumSize(Point value) {
        ((DartShell) impl).maximumSize = value;
    }

    public Point getMinimumSize() {
        return ((DartShell) impl).minimumSize;
    }

    public void setMinimumSize(Point value) {
        ((DartShell) impl).minimumSize = value;
    }

    public boolean getModified() {
        return ((DartShell) impl).getModified();
    }

    public void setModified(boolean value) {
        ((DartShell) impl).modified = value;
    }

    public Shell[] getShells() {
        Shell[] values = ((DartShell) impl).shells;
        if (values == null)
            return null;
        ArrayList<Shell> result = new ArrayList<>(values.length);
        for (Shell v : values) if (v != null)
            result.add(v);
        return result.toArray(new Shell[0]);
    }

    public void setShells(Shell[] value) {
        ((DartShell) impl).shells = value;
    }

    public static final int ALPHA = 577;

    public static final int DIALOGS = 582;

    public static final int FULL_SCREEN = 578;

    public static final int MAXIMUM_SIZE = 579;

    public static final int MINIMUM_SIZE = 580;

    public static final int MODIFIED = 581;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALPHA:
                return "alpha";
            case DIALOGS:
                return "dialogs";
            case FULL_SCREEN:
                return "fullScreen";
            case MAXIMUM_SIZE:
                return "maximumSize";
            case MINIMUM_SIZE:
                return "minimumSize";
            case MODIFIED:
                return "modified";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 577:
                w.write((int) getAlpha());
                return true;
            case 582:
                {
                    Dialog[] a_ = getDialogs();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Dialog e_ : a_) w.poly(e_ == null ? null : ((DartDialog) e_.getImpl()).getValue());
                    }
                }
                return true;
            case 578:
                w.write(getFullScreen());
                return true;
            case 579:
                w.write(getMaximumSize());
                return true;
            case 580:
                w.write(getMinimumSize());
                return true;
            case 581:
                w.write(getModified());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 577:
                {
                    int v_ = getAlpha();
                    w.key(577);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 582:
                {
                    Dialog[] v_ = getDialogs();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(582);
                    {
                        Dialog[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Dialog e_ : a_) w.poly(e_ == null ? null : ((DartDialog) e_.getImpl()).getValue());
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 578:
                {
                    boolean v_ = getFullScreen();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(578);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 579:
                {
                    Point v_ = getMaximumSize();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(579);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 580:
                {
                    Point v_ = getMinimumSize();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(580);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 581:
                {
                    boolean v_ = getModified();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(581);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VDecorations.allWireKeys_(), new int[] { 577, 582, 578, 579, 580, 581 });

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
            case 577:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
