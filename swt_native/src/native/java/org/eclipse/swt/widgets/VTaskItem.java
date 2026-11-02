package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VTaskItem extends VItem {

    protected VTaskItem() {
    }

    protected VTaskItem(DartTaskItem impl) {
        super(impl);
    }

    public Menu getMenu() {
        Menu val = ((DartTaskItem) impl).menu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setMenu(Menu value) {
        ((DartTaskItem) impl).menu = value;
    }

    public Image getOverlayImage() {
        Image val = ((DartTaskItem) impl).overlayImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setOverlayImage(Image value) {
        ((DartTaskItem) impl).overlayImage = value;
    }

    public String getOverlayText() {
        return ((DartTaskItem) impl).getOverlayText();
    }

    public void setOverlayText(String value) {
        ((DartTaskItem) impl).overlayText = value;
    }

    public int getProgress() {
        return ((DartTaskItem) impl).getProgress();
    }

    public void setProgress(int value) {
        ((DartTaskItem) impl).progress = value;
    }

    public int getProgressState() {
        return ((DartTaskItem) impl).getProgressState();
    }

    public void setProgressState(int value) {
        ((DartTaskItem) impl).progressState = value;
    }

    @Override
    public String wireName(int key) {
        switch(key) {
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] {});

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
