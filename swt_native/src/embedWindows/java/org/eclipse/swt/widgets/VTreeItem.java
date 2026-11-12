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
        return ((DartTreeItem) impl).getBackground();
    }

    public void setBackground(Color value) {
        ((DartTreeItem) impl).setBackground(value);
    }

    public Color[] getBackgrounds() {
        Color[] values = ((DartTreeItem) impl).backgrounds;
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
        ((DartTreeItem) impl).backgrounds = value;
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
        return ((DartTreeItem) impl).getForeground();
    }

    public void setForeground(Color value) {
        ((DartTreeItem) impl).setForeground(value);
    }

    public Color[] getForegrounds() {
        Color[] values = ((DartTreeItem) impl).foregrounds;
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
        ((DartTreeItem) impl).foregrounds = value;
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

    public int[] getPaintedImages() {
        return ((DartTreeItem) impl).getPaintedImages();
    }

    public void setPaintedImages(int[] value) {
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
        return ((DartTreeItem) impl).getItemCount();
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

    public static final int BACKGROUND = 655;

    public static final int BACKGROUNDS = 656;

    public static final int CHECKED = 657;

    public static final int EXPANDED = 658;

    public static final int FONT = 659;

    public static final int FONTS = 660;

    public static final int FOREGROUND = 661;

    public static final int FOREGROUNDS = 662;

    public static final int GRAYED = 663;

    public static final int IMAGES = 664;

    public static final int ITEM_COUNT = 665;

    public static final int ITEMS = 666;

    public static final int PAINTED_IMAGES = 667;

    public static final int PAINTED_TEXTS = 668;

    public static final int TEXTS = 669;

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
            case PAINTED_IMAGES:
                return "paintedImages";
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
            case 670:
                return false;
            case 655:
                w.write(getBackground());
                return true;
            case 656:
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
            case 657:
                w.write(getChecked());
                return true;
            case 658:
                w.write(getExpanded());
                return true;
            case 659:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 660:
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
            case 661:
                w.write(getForeground());
                return true;
            case 662:
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
            case 663:
                w.write(getGrayed());
                return true;
            case 664:
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
            case 666:
                w.items(getItems(), dev.equo.swt.WireTable.TYPE_TREE_ITEM);
                return true;
            case 667:
                w.write(getPaintedImages());
                return true;
            case 668:
                w.write(getPaintedTexts());
                return true;
            case 669:
                w.write(getTexts());
                return true;
            case 665:
                w.write((int) getItemCount());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 670:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 655:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(655);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 656:
                {
                    Color[] v_ = getBackgrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(656);
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
            case 657:
                {
                    boolean v_ = getChecked();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(657);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 658:
                {
                    boolean v_ = getExpanded();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(658);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 659:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(659);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 660:
                {
                    Font[] v_ = getFonts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(660);
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
            case 661:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(661);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 662:
                {
                    Color[] v_ = getForegrounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(662);
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
            case 663:
                {
                    boolean v_ = getGrayed();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(663);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 664:
                {
                    Image[] v_ = getImages();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(664);
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
            case 666:
                {
                    TreeItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(666);
                    w.items(v_, dev.equo.swt.WireTable.TYPE_TREE_ITEM);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 667:
                {
                    int[] v_ = getPaintedImages();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(667);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 668:
                {
                    int[] v_ = getPaintedTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(668);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 669:
                {
                    String[] v_ = getTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(669);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 665:
                {
                    int v_ = getItemCount();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(665);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VItem.allWireKeys_(), new int[] { 655, 656, 657, 658, 659, 660, 661, 662, 663, 664, 666, 667, 668, 669, 665 });

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
            case 668:
                return true;
        }
        return super.wireKeepsEmpty(key);
    }
}
