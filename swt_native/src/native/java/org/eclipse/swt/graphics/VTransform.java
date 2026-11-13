package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public class VTransform extends VResource {

    protected VTransform() {
    }

    protected VTransform(DartTransform impl) {
        super(impl);
    }

    public float[] getElements() {
        float[] result = new float[6];
        ((DartTransform) impl).getElements(result);
        return result;
    }

    public void setElements(float[] value) {
        ((DartTransform) impl).setElements(value[0], value[1], value[2], value[3], value[4], value[5]);
    }

    public static final int ELEMENTS = 636;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ELEMENTS:
                return "elements";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 636:
                w.write(getElements());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 636:
                {
                    float[] v_ = getElements();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(636);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 636 });

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

    @Override
    public int wireTypeId() {
        return 124;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(636);
        w.write(getElements());
    }
}
