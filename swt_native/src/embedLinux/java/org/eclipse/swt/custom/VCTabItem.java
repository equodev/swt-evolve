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

    public static final int CONTROL = 66;

    public static final int FONT = 67;

    public static final int FOREGROUND = 68;

    public static final int SELECTION_FOREGROUND = 69;

    public static final int SHOW_CLOSE = 70;

    public static final int SHOW_DIRTY = 71;

    public static final int SHOWING = 72;

    public static final int TOOL_TIP_TEXT = 73;

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
            case SHOW_DIRTY:
                return "showDirty";
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
            case 676:
                return false;
            case 66:
                w.ref(getControl());
                return true;
            case 67:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 68:
                w.write(getForeground());
                return true;
            case 69:
                w.write(getSelectionForeground());
                return true;
            case 70:
                w.write(getShowClose());
                return true;
            case 71:
                w.write(getShowDirty());
                return true;
            case 72:
                w.write(getShowing());
                return true;
            case 73:
                w.write(getToolTipText());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 66:
                {
                    Control v_ = getControl();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(66);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 67:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(67);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 68:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(68);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 69:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(69);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 70:
                {
                    boolean v_ = getShowClose();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(70);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 71:
                {
                    boolean v_ = getShowDirty();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(71);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 72:
                {
                    boolean v_ = getShowing();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(72);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 73:
                {
                    String v_ = getToolTipText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(73);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 66, 67, 68, 69, 70, 71, 72, 73 });

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
