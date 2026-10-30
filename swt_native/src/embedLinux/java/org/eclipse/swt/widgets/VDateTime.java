package org.eclipse.swt.widgets;

import java.text.*;
import java.text.AttributedCharacterIterator.*;
import java.text.DateFormat.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
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

    public static final int DAY = 281;

    public static final int HOURS = 282;

    public static final int MINUTES = 283;

    public static final int MONTH = 284;

    public static final int SECONDS = 285;

    public static final int YEAR = 286;

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
            case 281:
                w.write((int) getDay());
                return true;
            case 282:
                w.write((int) getHours());
                return true;
            case 283:
                w.write((int) getMinutes());
                return true;
            case 284:
                w.write((int) getMonth());
                return true;
            case 285:
                w.write((int) getSeconds());
                return true;
            case 286:
                w.write((int) getYear());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 281:
                {
                    int v_ = getDay();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(281);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 282:
                {
                    int v_ = getHours();
                    w.key(282);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 283:
                {
                    int v_ = getMinutes();
                    w.key(283);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 284:
                {
                    int v_ = getMonth();
                    w.key(284);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 285:
                {
                    int v_ = getSeconds();
                    w.key(285);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 286:
                {
                    int v_ = getYear();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(286);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 281, 282, 283, 284, 285, 286 });

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
            case 282:
            case 283:
            case 284:
            case 285:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
