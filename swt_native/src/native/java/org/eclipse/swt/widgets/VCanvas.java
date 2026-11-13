package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public class VCanvas extends VComposite {

    protected VCanvas() {
    }

    protected VCanvas(DartCanvas impl) {
        super(impl);
    }

    public IME getIME() {
        IME val = ((DartCanvas) impl).ime;
        if (val != null && !(val.getImpl() instanceof DartIME))
            return null;
        return val;
    }

    public void setIME(IME value) {
        ((DartCanvas) impl).ime = value;
    }

    public Caret getCaret() {
        Caret val = ((DartCanvas) impl).caret;
        if (val != null && !(val.getImpl() instanceof DartCaret))
            return null;
        return val;
    }

    public void setCaret(Caret value) {
        ((DartCanvas) impl).caret = value;
    }

    public static final int CARET = 74;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CARET:
                return "caret";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 74:
                w.ref(getCaret());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 74:
                {
                    Caret v_ = getCaret();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(74);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 74 });

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
