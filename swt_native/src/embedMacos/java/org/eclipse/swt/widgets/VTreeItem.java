package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class VTreeItem extends VItem {

    protected VTreeItem() {
    }

    protected VTreeItem(DartTreeItem impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartTreeItem) impl).background;
    }

    public void setBackground(Color value) {
        ((DartTreeItem) impl).background = value;
    }

    public Color[] getBackgrounds() {
        Color[] values = ((DartTreeItem) impl).cellBackground;
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
        ((DartTreeItem) impl).cellBackground = value;
    }

    public boolean getChecked() {
        return ((DartTreeItem) impl).getChecked();
    }

    public void setChecked(boolean value) {
        ((DartTreeItem) impl).checked = value;
    }

    public boolean getExpanded() {
        return ((DartTreeItem) impl).getExpanded();
    }

    public void setExpanded(boolean value) {
        ((DartTreeItem) impl).expanded = value;
    }

    public Font getFont() {
        Font val = ((DartTreeItem) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartTreeItem) impl).font = value;
    }

    public Font[] getFonts() {
        Font[] values = ((DartTreeItem) impl).cellFont;
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
        ((DartTreeItem) impl).cellFont = value;
    }

    public Color getForeground() {
        return ((DartTreeItem) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartTreeItem) impl).foreground = value;
    }

    public Color[] getForegrounds() {
        Color[] values = ((DartTreeItem) impl).cellForeground;
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
        ((DartTreeItem) impl).cellForeground = value;
    }

    public boolean getGrayed() {
        return ((DartTreeItem) impl).getGrayed();
    }

    public void setGrayed(boolean value) {
        ((DartTreeItem) impl).grayed = value;
    }

    public Image[] getImages() {
        return ((DartTreeItem) impl).getImages();
    }

    public void setImages(Image[] value) {
        ((DartTreeItem) impl).setImages(value);
    }

    public TreeItem[] getItems() {
        TreeItem[] values = ((DartTreeItem) impl).items;
        if (values == null)
            return null;
        int kept = 0;
        for (TreeItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TreeItem[] result = new TreeItem[kept];
        kept = 0;
        for (TreeItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(TreeItem[] value) {
        ((DartTreeItem) impl).items = value;
    }

    public int[] getPaintedTexts() {
        return ((DartTreeItem) impl).getPaintedTexts();
    }

    public void setPaintedTexts(int[] value) {
    }

    public String[] getTexts() {
        return ((DartTreeItem) impl).getTexts();
    }

    public void setTexts(String[] value) {
        ((DartTreeItem) impl).strings = value;
    }

    public int getItemCount() {
        return ((DartTreeItem) impl)._itemCount();
    }

    public static final int BACKGROUND = 645;

    public static final int BACKGROUNDS = 646;

    public static final int CHECKED = 647;

    public static final int EXPANDED = 648;

    public static final int FONT = 649;

    public static final int FONTS = 650;

    public static final int FOREGROUND = 651;

    public static final int FOREGROUNDS = 652;

    public static final int GRAYED = 653;

    public static final int IMAGES = 654;

    public static final int ITEM_COUNT = 655;

    public static final int ITEMS = 656;

    public static final int PAINTED_TEXTS = 657;

    public static final int TEXTS = 658;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BACKGROUND:
                return "background";
            case BACKGROUNDS:
                return "backgrounds";
            case CHECKED:
                return "checked";
            case EXPANDED:
                return "expanded";
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
            case ITEMS:
                return "items";
            case PAINTED_TEXTS:
                return "paintedTexts";
            case TEXTS:
                return "texts";
            case ITEM_COUNT:
                return "itemCount";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 645:
                w.write(getBackground());
                return true;
            case 646:
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
            case 647:
                w.write(getChecked());
                return true;
            case 648:
                w.write(getExpanded());
                return true;
            case 649:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 650:
                {
                    Font[] a_ = getFonts();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (Font e_ : a_) w.object((e_ == null || e_.isDisposed() ? null : e_.getImpl() instanceof DartFont ? ((DartFont) e_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(e_).getImpl()).getValue()));
                    }
                }
                return true;
            case 651:
                w.write(getForeground());
                return true;
            case 652:
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
            case 653:
                w.write(getGrayed());
                return true;
            case 654:
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
            case 656:
                w.refs(getItems());
                return true;
            case 657:
                w.write(getPaintedTexts());
                return true;
            case 658:
                w.write(getTexts());
                return true;
            case 655:
                w.write((int) getItemCount());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 645:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(645);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 646:
                {
                    Color[] v_ = getBackgrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(646);
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
            case 647:
                {
                    boolean v_ = getChecked();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(647);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 648:
                {
                    boolean v_ = getExpanded();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(648);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 649:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(649);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 650:
                {
                    Font[] v_ = getFonts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(650);
                    {
                        Font[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (Font e_ : a_) w.object((e_ == null || e_.isDisposed() ? null : e_.getImpl() instanceof DartFont ? ((DartFont) e_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(e_).getImpl()).getValue()));
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 651:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(651);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 652:
                {
                    Color[] v_ = getForegrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(652);
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
            case 653:
                {
                    boolean v_ = getGrayed();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(653);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 654:
                {
                    Image[] v_ = getImages();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(654);
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
            case 656:
                {
                    TreeItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(656);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 657:
                {
                    int[] v_ = getPaintedTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(657);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 658:
                {
                    String[] v_ = getTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(658);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 655:
                {
                    int v_ = getItemCount();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(655);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 645, 646, 647, 648, 649, 650, 651, 652, 653, 654, 656, 657, 658, 655 });

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
            case 657:
                return true;
        }
        return super.wireKeepsEmpty(key);
    }
}
