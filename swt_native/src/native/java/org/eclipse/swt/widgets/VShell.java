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
        int kept = 0;
        for (Shell v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Shell[] result = new Shell[kept];
        kept = 0;
        for (Shell v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setShells(Shell[] value) {
        ((DartShell) impl).shells = value;
    }

    public int getPresentation() {
        return ((DartShell) impl).wirePresentation();
    }

    public Rectangle getPageHost() {
        return ((DartShell) impl).wirePageHost();
    }

    public Shell getPageOwner() {
        return ((DartShell) impl).wirePageOwner();
    }

    public int getPageStrip() {
        return ((DartShell) impl).wirePageStrip();
    }

    public boolean getPageMaximized() {
        return ((DartShell) impl).wirePageMaximized();
    }

    public int getPageHeading() {
        return ((DartShell) impl).wirePageHeading();
    }

    public String getPageTitle() {
        return ((DartShell) impl).wirePageTitle();
    }

    public String getPageMessage() {
        return ((DartShell) impl).wirePageMessage();
    }

    public int getPageMessageType() {
        return ((DartShell) impl).wirePageMessageType();
    }

    public String[] getPageSteps() {
        return ((DartShell) impl).wirePageSteps();
    }

    public int getPageStep() {
        return ((DartShell) impl).wirePageStep();
    }

    public static final int ALPHA = 4608;

    public static final int DIALOGS = 4609;

    public static final int FULL_SCREEN = 4610;

    public static final int MAXIMUM_SIZE = 4611;

    public static final int MINIMUM_SIZE = 4612;

    public static final int MODIFIED = 4613;

    public static final int PAGE_HEADING = 4617;

    public static final int PAGE_HOST = 4615;

    public static final int PAGE_MAXIMIZED = 4618;

    public static final int PAGE_MESSAGE = 4619;

    public static final int PAGE_MESSAGE_TYPE = 4620;

    public static final int PAGE_OWNER = 4616;

    public static final int PAGE_STEP = 4621;

    public static final int PAGE_STEPS = 4622;

    public static final int PAGE_STRIP = 4623;

    public static final int PAGE_TITLE = 4624;

    public static final int PRESENTATION = 4614;

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
            case PRESENTATION:
                return "presentation";
            case PAGE_HOST:
                return "pageHost";
            case PAGE_OWNER:
                return "pageOwner";
            case PAGE_STRIP:
                return "pageStrip";
            case PAGE_MAXIMIZED:
                return "pageMaximized";
            case PAGE_HEADING:
                return "pageHeading";
            case PAGE_TITLE:
                return "pageTitle";
            case PAGE_MESSAGE:
                return "pageMessage";
            case PAGE_MESSAGE_TYPE:
                return "pageMessageType";
            case PAGE_STEPS:
                return "pageSteps";
            case PAGE_STEP:
                return "pageStep";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1161:
                return false;
            case 4608:
                w.write((int) getAlpha());
                return true;
            case 4609:
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
            case 4610:
                w.write(getFullScreen());
                return true;
            case 4611:
                w.write(getMaximumSize());
                return true;
            case 4612:
                w.write(getMinimumSize());
                return true;
            case 4613:
                w.write(getModified());
                return true;
            case 4614:
                w.write((int) getPresentation());
                return true;
            case 4615:
                w.write(getPageHost());
                return true;
            case 4616:
                w.ref(getPageOwner());
                return true;
            case 4623:
                w.write((int) getPageStrip());
                return true;
            case 4618:
                w.write(getPageMaximized());
                return true;
            case 4617:
                w.write((int) getPageHeading());
                return true;
            case 4624:
                w.write(getPageTitle());
                return true;
            case 4619:
                w.write(getPageMessage());
                return true;
            case 4620:
                w.write((int) getPageMessageType());
                return true;
            case 4622:
                w.write(getPageSteps());
                return true;
            case 4621:
                w.write((int) getPageStep());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1161:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 4608:
                {
                    int v_ = getAlpha();
                    w.key(4608);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4609:
                {
                    Dialog[] v_ = getDialogs();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4609);
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
            case 4610:
                {
                    boolean v_ = getFullScreen();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4610);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4611:
                {
                    Point v_ = getMaximumSize();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4611);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4612:
                {
                    Point v_ = getMinimumSize();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4612);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4613:
                {
                    boolean v_ = getModified();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4613);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4614:
                {
                    int v_ = getPresentation();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4614);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4615:
                {
                    Rectangle v_ = getPageHost();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4615);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4616:
                {
                    Shell v_ = getPageOwner();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4616);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4623:
                {
                    int v_ = getPageStrip();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4623);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4618:
                {
                    boolean v_ = getPageMaximized();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4618);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4617:
                {
                    int v_ = getPageHeading();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4617);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4624:
                {
                    String v_ = getPageTitle();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4624);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4619:
                {
                    String v_ = getPageMessage();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4619);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4620:
                {
                    int v_ = getPageMessageType();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4620);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4622:
                {
                    String[] v_ = getPageSteps();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4622);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4621:
                {
                    int v_ = getPageStep();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4621);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VDecorations.allWireKeys_(), new int[] { 4608, 4609, 4610, 4611, 4612, 4613, 4614, 4615, 4616, 4623, 4618, 4617, 4624, 4619, 4620, 4622, 4621 });

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
            case 4608:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
