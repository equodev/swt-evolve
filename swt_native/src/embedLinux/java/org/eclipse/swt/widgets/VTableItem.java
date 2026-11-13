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
        Color[] values = ((DartTableItem) impl).backgrounds;
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
        ((DartTableItem) impl).backgrounds = value;
    }

    public boolean getChecked() {
        return ((DartTableItem) impl).getChecked();
    }

    public void setChecked(boolean value) {
        ((DartTableItem) impl).checked = value;
    }

    public Font getFont() {
        Font val = ((DartTableItem) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
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
            if (v != null && v.getImpl() instanceof SwtFont)
                v = GraphicsUtils.copyFont(v);
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
        Color[] values = ((DartTableItem) impl).foregrounds;
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
        ((DartTableItem) impl).foregrounds = value;
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

    public int[] getPaintedImages() {
        return ((DartTableItem) impl).getPaintedImages();
    }

    public void setPaintedImages(int[] value) {
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

    /**
     * Marks every property the SWT.PaintItem capture fills, so the next update captures the cells again.
     */
    public void markOwnerDrawnCellsDirty() {
        markDirty(IMAGES);
        markDirty(TEXTS);
        markDirty(PAINTED_TEXTS);
        markDirty(PAINTED_IMAGES);
    }

    public static final int BACKGROUND = 580;

    public static final int BACKGROUNDS = 581;

    public static final int CHECKED = 582;

    public static final int FONT = 583;

    public static final int FONTS = 584;

    public static final int FOREGROUND = 585;

    public static final int FOREGROUNDS = 586;

    public static final int GRAYED = 587;

    public static final int IMAGES = 588;

    public static final int PAINTED_IMAGES = 589;

    public static final int PAINTED_TEXTS = 590;

    public static final int TEXTS = 591;

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
            case PAINTED_IMAGES:
                return "paintedImages";
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
            case 676:
                return false;
            case 580:
                w.write(getBackground());
                return true;
            case 581:
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
            case 582:
                w.write(getChecked());
                return true;
            case 583:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 584:
                {
                    Font[] a_ = getFonts();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (int i_ = 0; i_ < a_.length; i_++) {
                            Font e_ = a_[i_];
                            if (e_ == null) {
                                i_ += w.nulls(a_, i_) - 1;
                                continue;
                            }
                            w.object((e_ == null || e_.isDisposed() ? null : e_.getImpl() instanceof DartFont ? ((DartFont) e_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(e_).getImpl()).getValue()));
                        }
                    }
                }
                return true;
            case 585:
                w.write(getForeground());
                return true;
            case 586:
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
            case 587:
                w.write(getGrayed());
                return true;
            case 588:
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
            case 589:
                w.write(getPaintedImages());
                return true;
            case 590:
                w.write(getPaintedTexts());
                return true;
            case 591:
                w.write(getTexts());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 676:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 580:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(580);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 581:
                {
                    Color[] v_ = getBackgrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(581);
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
            case 582:
                {
                    boolean v_ = getChecked();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(582);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 583:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(583);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 584:
                {
                    Font[] v_ = getFonts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(584);
                    {
                        Font[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (int i_ = 0; i_ < a_.length; i_++) {
                                Font e_ = a_[i_];
                                if (e_ == null) {
                                    i_ += w.nulls(a_, i_) - 1;
                                    continue;
                                }
                                w.object((e_ == null || e_.isDisposed() ? null : e_.getImpl() instanceof DartFont ? ((DartFont) e_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(e_).getImpl()).getValue()));
                            }
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 585:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(585);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 586:
                {
                    Color[] v_ = getForegrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(586);
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
            case 587:
                {
                    boolean v_ = getGrayed();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(587);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 588:
                {
                    Image[] v_ = getImages();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(588);
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
            case 589:
                {
                    int[] v_ = getPaintedImages();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(589);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 590:
                {
                    int[] v_ = getPaintedTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(590);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 591:
                {
                    String[] v_ = getTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(591);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 580, 581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591 });

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
            case 590:
                return true;
        }
        return super.wireKeepsEmpty(key);
    }
}
