package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VTableItem extends VItem {

    protected VTableItem() {
    }

    protected VTableItem(DartTableItem impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartTableItem) impl).background;
    }

    public void setBackground(Color value) {
        ((DartTableItem) impl).background = value;
    }

    public Color[] getBackgrounds() {
        Color[] values = ((DartTableItem) impl).cellBackground;
        if (values == null)
            return null;
        Color[] result = new Color[values.length];
        for (int i = 0; i < values.length; i++) {
            Color v = values[i];
            if (v != null && !(v.getImpl() instanceof DartColor))
                v = null;
            result[i] = v;
        }
        return result;
    }

    public void setBackgrounds(Color[] value) {
        ((DartTableItem) impl).cellBackground = value;
    }

    public boolean getChecked() {
        return ((DartTableItem) impl).getChecked();
    }

    public void setChecked(boolean value) {
        ((DartTableItem) impl).checked = value;
    }

    public Font getFont() {
        Font val = ((DartTableItem) impl).font;
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartTableItem) impl).font = value;
    }

    public Font[] getFonts() {
        Font[] values = ((DartTableItem) impl).cellFont;
        if (values == null)
            return null;
        Font[] result = new Font[values.length];
        for (int i = 0; i < values.length; i++) {
            Font v = values[i];
            if (v != null && !(v.getImpl() instanceof DartFont))
                v = null;
            result[i] = v;
        }
        return result;
    }

    public void setFonts(Font[] value) {
        ((DartTableItem) impl).cellFont = value;
    }

    public Color getForeground() {
        return ((DartTableItem) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartTableItem) impl).foreground = value;
    }

    public Color[] getForegrounds() {
        Color[] values = ((DartTableItem) impl).cellForeground;
        if (values == null)
            return null;
        Color[] result = new Color[values.length];
        for (int i = 0; i < values.length; i++) {
            Color v = values[i];
            if (v != null && !(v.getImpl() instanceof DartColor))
                v = null;
            result[i] = v;
        }
        return result;
    }

    public void setForegrounds(Color[] value) {
        ((DartTableItem) impl).cellForeground = value;
    }

    public boolean getGrayed() {
        return ((DartTableItem) impl).getGrayed();
    }

    public void setGrayed(boolean value) {
        ((DartTableItem) impl).grayed = value;
    }

    public int getImageIndent() {
        return ((DartTableItem) impl).getImageIndent();
    }

    public void setImageIndent(int value) {
        ((DartTableItem) impl).imageIndent = value;
    }

    public Image[] getImages() {
        return ((DartTableItem) impl).getImages();
    }

    public void setImages(Image[] value) {
        ((DartTableItem) impl).setImages(value);
    }

    public int[] getPaintedTexts() {
        return ((DartTableItem) impl).getPaintedTexts();
    }

    public void setPaintedTexts(int[] value) {
    }

    public String[] getTexts() {
        return ((DartTableItem) impl).getTexts();
    }

    public void setTexts(String[] value) {
        ((DartTableItem) impl).strings = value;
    }

    public static final int BACKGROUND = 113;

    public static final int BACKGROUNDS = 114;

    public static final int CHECKED = 115;

    public static final int FONT = 116;

    public static final int FONTS = 117;

    public static final int FOREGROUND = 118;

    public static final int FOREGROUNDS = 119;

    public static final int GRAYED = 120;

    public static final int IMAGES = 121;

    public static final int PAINTED_TEXTS = 122;

    public static final int TEXTS = 123;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BACKGROUND:
                return "background";
            case BACKGROUNDS:
                return "backgrounds";
            case CHECKED:
                return "checked";
            case FONT:
                return "font";
            case FONTS:
                return "fonts";
            case FOREGROUND:
                return "foreground";
            case FOREGROUNDS:
                return "foregrounds";
            case GRAYED:
                return "grayed";
            case IMAGES:
                return "images";
            case PAINTED_TEXTS:
                return "paintedTexts";
            case TEXTS:
                return "texts";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 113:
                w.write(getBackground());
                return true;
            case 114:
                {
                    Color[] a_ = getBackgrounds();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Color e_ : a_) w.write(e_);
                    }
                }
                return true;
            case 115:
                w.write(getChecked());
                return true;
            case 116:
                w.object((getFont() == null || getFont().isDisposed() ? null : ((DartFont) getFont().getImpl()).getValue()));
                return true;
            case 117:
                {
                    Font[] a_ = getFonts();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Font e_ : a_) w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
                    }
                }
                return true;
            case 118:
                w.write(getForeground());
                return true;
            case 119:
                {
                    Color[] a_ = getForegrounds();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Color e_ : a_) w.write(e_);
                    }
                }
                return true;
            case 120:
                w.write(getGrayed());
                return true;
            case 121:
                {
                    Image[] a_ = getImages();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Image e_ : a_) w.image(e_);
                    }
                }
                return true;
            case 122:
                w.write(getPaintedTexts());
                return true;
            case 123:
                w.write(getTexts());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 113:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(113);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 114:
                {
                    Color[] v_ = getBackgrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(114);
                    {
                        Color[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Color e_ : a_) w.write(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 115:
                {
                    boolean v_ = getChecked();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(115);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 116:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartFont) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(116);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 117:
                {
                    Font[] v_ = getFonts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(117);
                    {
                        Font[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Font e_ : a_) w.object((e_ == null || e_.isDisposed() ? null : ((DartFont) e_.getImpl()).getValue()));
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 118:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(118);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 119:
                {
                    Color[] v_ = getForegrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(119);
                    {
                        Color[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Color e_ : a_) w.write(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 120:
                {
                    boolean v_ = getGrayed();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(120);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 121:
                {
                    Image[] v_ = getImages();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(121);
                    {
                        Image[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Image e_ : a_) w.image(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 122:
                {
                    int[] v_ = getPaintedTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(122);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 123:
                {
                    String[] v_ = getTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(123);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123 });

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
        switch(key) {
            case 122:
                return true;
        }
        return super.wireKeepsEmpty(key);
    }
}
