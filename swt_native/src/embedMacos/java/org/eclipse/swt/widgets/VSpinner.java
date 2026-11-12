package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class VSpinner extends VComposite {

    protected VSpinner() {
    }

    protected VSpinner(DartSpinner impl) {
        super(impl);
    }

    public int getDigits() {
        return ((DartSpinner) impl).getDigits();
    }

    public void setDigits(int value) {
        ((DartSpinner) impl).digits = value;
    }

    public int getIncrement() {
        return ((DartSpinner) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartSpinner) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartSpinner) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartSpinner) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartSpinner) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartSpinner) impl).minimum = value;
    }

    public int getPageIncrement() {
        return ((DartSpinner) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartSpinner) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartSpinner) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartSpinner) impl).selection = value;
    }

    public int getTextLimit() {
        return ((DartSpinner) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartSpinner) impl).textLimit = value;
    }

    public String getKeptText() {
        return ((DartSpinner) impl).wireKeptText();
    }

    public static final int DIGITS = 471;

    public static final int INCREMENT = 472;

    public static final int KEPT_TEXT = 473;

    public static final int MAXIMUM = 474;

    public static final int MINIMUM = 475;

    public static final int PAGE_INCREMENT = 476;

    public static final int SELECTION = 477;

    public static final int TEXT_LIMIT = 478;

    @Override
    public String wireName(int key) {
        switch(key) {
            case DIGITS:
                return "digits";
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
            case TEXT_LIMIT:
                return "textLimit";
            case KEPT_TEXT:
                return "keptText";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return false;
            case 471:
                w.write((int) getDigits());
                return true;
            case 472:
                w.write((int) getIncrement());
                return true;
            case 474:
                w.write((int) getMaximum());
                return true;
            case 475:
                w.write((int) getMinimum());
                return true;
            case 476:
                w.write((int) getPageIncrement());
                return true;
            case 477:
                w.write((int) getSelection());
                return true;
            case 478:
                w.write((int) getTextLimit());
                return true;
            case 473:
                w.write(getKeptText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 471:
                {
                    int v_ = getDigits();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(471);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 472:
                {
                    int v_ = getIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(472);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 474:
                {
                    int v_ = getMaximum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(474);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 475:
                {
                    int v_ = getMinimum();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(475);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 476:
                {
                    int v_ = getPageIncrement();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(476);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 477:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(477);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 478:
                {
                    int v_ = getTextLimit();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(478);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 473:
                {
                    String v_ = getKeptText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(473);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 471, 472, 474, 475, 476, 477, 478, 473 });

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
