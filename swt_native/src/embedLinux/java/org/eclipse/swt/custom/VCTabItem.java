package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VCTabItem extends VItem {

    protected VCTabItem() {
    }

    protected VCTabItem(DartCTabItem impl) {
        super(impl);
    }

    public Control getControl() {
        Control val = ((DartCTabItem) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartCTabItem) impl).control = value;
    }

    @SuppressWarnings("removal")
    public Image getDisabledImage() {
        Image val = ((DartCTabItem) impl).disabledImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setDisabledImage(Image value) {
        ((DartCTabItem) impl).disabledImage = value;
    }

    public Font getFont() {
        Font val = ((DartCTabItem) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartCTabItem) impl).font = value;
    }

    public Color getForeground() {
        return ((DartCTabItem) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartCTabItem) impl).foreground = value;
    }

    public Color getSelectionForeground() {
        return ((DartCTabItem) impl).selectionForeground;
    }

    public void setSelectionForeground(Color value) {
        ((DartCTabItem) impl).selectionForeground = value;
    }

    public boolean getShowClose() {
        return ((DartCTabItem) impl).getShowClose();
    }

    public void setShowClose(boolean value) {
        ((DartCTabItem) impl).showClose = value;
    }

    public boolean getShowDirty() {
        return ((DartCTabItem) impl).getShowDirty();
    }

    public void setShowDirty(boolean value) {
        ((DartCTabItem) impl).showDirty = value;
    }

    public boolean getShowing() {
        return ((DartCTabItem) impl).showing;
    }

    public void setShowing(boolean value) {
    }

    public String getToolTipText() {
        return ((DartCTabItem) impl).getToolTipText();
    }

    public void setToolTipText(String value) {
        ((DartCTabItem) impl).toolTipText = value;
    }

    public static final int CONTROL = 413;

    public static final int FONT = 414;

    public static final int FOREGROUND = 415;

    public static final int SELECTION_FOREGROUND = 416;

    public static final int SHOW_CLOSE = 417;

    public static final int SHOWING = 418;

    public static final int TOOL_TIP_TEXT = 419;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CONTROL:
                return "control";
            case FONT:
                return "font";
            case FOREGROUND:
                return "foreground";
            case SELECTION_FOREGROUND:
                return "selectionForeground";
            case SHOW_CLOSE:
                return "showClose";
            case SHOWING:
                return "showing";
            case TOOL_TIP_TEXT:
                return "toolTipText";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 413:
                w.ref(getControl());
                return true;
            case 414:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 415:
                w.write(getForeground());
                return true;
            case 416:
                w.write(getSelectionForeground());
                return true;
            case 417:
                w.write(getShowClose());
                return true;
            case 418:
                w.write(getShowing());
                return true;
            case 419:
                w.write(getToolTipText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 413:
                {
                    Control v_ = getControl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(413);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 414:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(414);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 415:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(415);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 416:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(416);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 417:
                {
                    boolean v_ = getShowClose();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(417);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 418:
                {
                    boolean v_ = getShowing();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(418);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 419:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(419);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 413, 414, 415, 416, 417, 418, 419 });

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
