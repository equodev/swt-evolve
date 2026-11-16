package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VDateTime extends VComposite {

    protected VDateTime() {
    }

    protected VDateTime(DartDateTime impl) {
        super(impl);
    }

    public int getDay() {
        return ((DartDateTime) impl).getDay();
    }

    public void setDay(int value) {
        ((DartDateTime) impl).day = value;
    }

    public int getHours() {
        return ((DartDateTime) impl).getHours();
    }

    public void setHours(int value) {
        ((DartDateTime) impl).hours = value;
    }

    public int getMinutes() {
        return ((DartDateTime) impl).getMinutes();
    }

    public void setMinutes(int value) {
        ((DartDateTime) impl).minutes = value;
    }

    public int getMonth() {
        return ((DartDateTime) impl).getMonth();
    }

    public void setMonth(int value) {
        ((DartDateTime) impl).month = value;
    }

    public int getSeconds() {
        return ((DartDateTime) impl).getSeconds();
    }

    public void setSeconds(int value) {
        ((DartDateTime) impl).seconds = value;
    }

    public int getYear() {
        return ((DartDateTime) impl).getYear();
    }

    public void setYear(int value) {
        ((DartDateTime) impl).year = value;
    }

    public static final int DAY = 168;

    public static final int HOURS = 169;

    public static final int MINUTES = 170;

    public static final int MONTH = 171;

    public static final int SECONDS = 172;

    public static final int YEAR = 173;

    @Override
    public String wireName(int key) {
        switch(key) {
            case DAY:
                return "day";
            case HOURS:
                return "hours";
            case MINUTES:
                return "minutes";
            case MONTH:
                return "month";
            case SECONDS:
                return "seconds";
            case YEAR:
                return "year";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 150:
                return false;
            case 168:
                w.write((int) getDay());
                return true;
            case 169:
                w.write((int) getHours());
                return true;
            case 170:
                w.write((int) getMinutes());
                return true;
            case 171:
                w.write((int) getMonth());
                return true;
            case 172:
                w.write((int) getSeconds());
                return true;
            case 173:
                w.write((int) getYear());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 150:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 168:
                {
                    int v_ = getDay();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(168);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 169:
                {
                    int v_ = getHours();
                    w.key(169);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 170:
                {
                    int v_ = getMinutes();
                    w.key(170);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 171:
                {
                    int v_ = getMonth();
                    w.key(171);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 172:
                {
                    int v_ = getSeconds();
                    w.key(172);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 173:
                {
                    int v_ = getYear();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(173);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 168, 169, 170, 171, 172, 173 });

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
            case 169:
            case 170:
            case 171:
            case 172:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
