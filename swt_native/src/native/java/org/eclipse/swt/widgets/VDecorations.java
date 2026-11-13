package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VDecorations extends VCanvas {

    protected VDecorations() {
    }

    protected VDecorations(DartDecorations impl) {
        super(impl);
    }

    public Button getDefaultButton() {
        Button val = ((DartDecorations) impl).defaultButton;
        if (val != null && !(val.getImpl() instanceof DartButton))
            return null;
        return val;
    }

    public void setDefaultButton(Button value) {
        ((DartDecorations) impl).defaultButton = value;
    }

    public Image getImage() {
        Image val = ((DartDecorations) impl).image;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setImage(Image value) {
        ((DartDecorations) impl).image = value;
    }

    public Image[] getImages() {
        Image[] values = ((DartDecorations) impl).images;
        if (values == null)
            return null;
        int kept = 0;
        for (Image v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Image[] result = new Image[kept];
        kept = 0;
        for (Image v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setImages(Image[] value) {
        ((DartDecorations) impl).images = value;
    }

    public boolean getMaximized() {
        return ((DartDecorations) impl).getMaximized();
    }

    public void setMaximized(boolean value) {
        ((DartDecorations) impl).maximized = value;
    }

    public Menu getMenuBar() {
        Menu val = ((DartDecorations) impl).menuBar;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setMenuBar(Menu value) {
        ((DartDecorations) impl).menuBar = value;
    }

    public boolean getMinimized() {
        return ((DartDecorations) impl).getMinimized();
    }

    public void setMinimized(boolean value) {
        ((DartDecorations) impl).minimized = value;
    }

    public String getText() {
        return ((DartDecorations) impl).getText();
    }

    public void setText(String value) {
        ((DartDecorations) impl).text = value;
    }

    public int getMenuStripHeight() {
        return ((DartDecorations) impl).wireMenuStripHeight();
    }

    public boolean getOwnMenuStrip() {
        return ((DartDecorations) impl).wireOwnMenuStrip();
    }

    public static final int MENU_BAR = 173;

    public static final int MENU_STRIP_HEIGHT = 174;

    public static final int OWN_MENU_STRIP = 175;

    public static final int TEXT = 176;

    @Override
    public String wireName(int key) {
        switch(key) {
            case MENU_BAR:
                return "menuBar";
            case TEXT:
                return "text";
            case MENU_STRIP_HEIGHT:
                return "menuStripHeight";
            case OWN_MENU_STRIP:
                return "ownMenuStrip";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return false;
            case 173:
                w.ref(getMenuBar());
                return true;
            case 176:
                w.write(getText());
                return true;
            case 174:
                w.write((int) getMenuStripHeight());
                return true;
            case 175:
                w.write(getOwnMenuStrip());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 147:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 173:
                {
                    Menu v_ = getMenuBar();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(173);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 176:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(176);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 174:
                {
                    int v_ = getMenuStripHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(174);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 175:
                {
                    boolean v_ = getOwnMenuStrip();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(175);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VCanvas.allWireKeys_(), new int[] { 173, 176, 174, 175 });

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
