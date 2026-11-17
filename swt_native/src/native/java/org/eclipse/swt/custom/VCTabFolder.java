package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VCTabFolder extends VComposite {

    protected VCTabFolder() {
    }

    protected VCTabFolder(DartCTabFolder impl) {
        super(impl);
    }

    public boolean getMRUVisible() {
        return ((DartCTabFolder) impl).getMRUVisible();
    }

    public void setMRUVisible(boolean value) {
        ((DartCTabFolder) impl).mru = value;
    }

    public boolean getBorderVisible() {
        return ((DartCTabFolder) impl).getBorderVisible();
    }

    public void setBorderVisible(boolean value) {
        ((DartCTabFolder) impl).borderVisible = value;
    }

    public boolean getChevronVisible() {
        return ((DartCTabFolder) impl).chevronVisible;
    }

    public void setChevronVisible(boolean value) {
    }

    public boolean getDirtyIndicatorStyle() {
        return ((DartCTabFolder) impl).getDirtyIndicatorStyle();
    }

    public void setDirtyIndicatorStyle(boolean value) {
        ((DartCTabFolder) impl).dirtyIndicatorStyle = value;
    }

    public Color[] getGradientColors() {
        Color[] values = ((DartCTabFolder) impl).gradientColors;
        if (values == null)
            return null;
        int kept = 0;
        for (Color v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Color[] result = new Color[kept];
        kept = 0;
        for (Color v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setGradientColors(Color[] value) {
        ((DartCTabFolder) impl).gradientColors = value;
    }

    public int[] getGradientPercents() {
        return ((DartCTabFolder) impl).gradientPercents;
    }

    public void setGradientPercents(int[] value) {
        ((DartCTabFolder) impl).gradientPercents = value;
    }

    public boolean getGradientVertical() {
        return ((DartCTabFolder) impl).gradientVertical;
    }

    public void setGradientVertical(boolean value) {
        ((DartCTabFolder) impl).gradientVertical = value;
    }

    public boolean getHighlight() {
        return ((DartCTabFolder) impl).highlight;
    }

    public void setHighlight(boolean value) {
    }

    public boolean getHighlightEnabled() {
        return ((DartCTabFolder) impl).getHighlightEnabled();
    }

    public void setHighlightEnabled(boolean value) {
        ((DartCTabFolder) impl).highlightEnabled = value;
    }

    public CTabItem[] getItems() {
        CTabItem[] values = ((DartCTabFolder) impl).getItems();
        if (values == null)
            return null;
        int kept = 0;
        for (CTabItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        CTabItem[] result = new CTabItem[kept];
        kept = 0;
        for (CTabItem v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setItems(CTabItem[] value) {
        ((DartCTabFolder) impl).items = value;
    }

    public boolean getMaximizeVisible() {
        return ((DartCTabFolder) impl).getMaximizeVisible();
    }

    public void setMaximizeVisible(boolean value) {
        ((DartCTabFolder) impl).showMax = value;
    }

    public boolean getMaximized() {
        return ((DartCTabFolder) impl).getMaximized();
    }

    public void setMaximized(boolean value) {
        ((DartCTabFolder) impl).maximized = value;
    }

    public boolean getMinimizeVisible() {
        return ((DartCTabFolder) impl).getMinimizeVisible();
    }

    public void setMinimizeVisible(boolean value) {
        ((DartCTabFolder) impl).showMin = value;
    }

    public boolean getMinimized() {
        return ((DartCTabFolder) impl).getMinimized();
    }

    public void setMinimized(boolean value) {
        ((DartCTabFolder) impl).minimized = value;
    }

    public int getMinimumCharacters() {
        return ((DartCTabFolder) impl).getMinimumCharacters();
    }

    public void setMinimumCharacters(int value) {
        ((DartCTabFolder) impl).minChars = value;
    }

    public boolean getSelectedImageVisible() {
        return ((DartCTabFolder) impl).getSelectedImageVisible();
    }

    public void setSelectedImageVisible(boolean value) {
        ((DartCTabFolder) impl).showSelectedImage = value;
    }

    public int getSelection() {
        return ((DartCTabFolder) impl).selectedIndex;
    }

    public void setSelection(int value) {
        ((DartCTabFolder) impl).selectedIndex = value;
    }

    public Color getSelectionBackground() {
        return ((DartCTabFolder) impl).selectionBackground;
    }

    public void setSelectionBackground(Color value) {
        ((DartCTabFolder) impl).selectionBackground = value;
    }

    public int getSelectionBarThickness() {
        return ((DartCTabFolder) impl).selectionHighlightBarThickness;
    }

    public void setSelectionBarThickness(int value) {
        ((DartCTabFolder) impl).selectionHighlightBarThickness = value;
    }

    public Image getSelectionBgImage() {
        Image val = ((DartCTabFolder) impl).selectionBgImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setSelectionBgImage(Image value) {
        ((DartCTabFolder) impl).selectionBgImage = value;
    }

    public Color getSelectionForeground() {
        return ((DartCTabFolder) impl).selectionForeground;
    }

    public void setSelectionForeground(Color value) {
        ((DartCTabFolder) impl).selectionForeground = value;
    }

    public Color[] getSelectionGradientColors() {
        Color[] values = ((DartCTabFolder) impl).selectionGradientColors;
        if (values == null)
            return null;
        int kept = 0;
        for (Color v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Color[] result = new Color[kept];
        kept = 0;
        for (Color v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setSelectionGradientColors(Color[] value) {
        ((DartCTabFolder) impl).selectionGradientColors = value;
    }

    public int[] getSelectionGradientPercents() {
        return ((DartCTabFolder) impl).selectionGradientPercents;
    }

    public void setSelectionGradientPercents(int[] value) {
        ((DartCTabFolder) impl).selectionGradientPercents = value;
    }

    public boolean getSelectionGradientVertical() {
        return ((DartCTabFolder) impl).selectionGradientVertical;
    }

    public void setSelectionGradientVertical(boolean value) {
        ((DartCTabFolder) impl).selectionGradientVertical = value;
    }

    public boolean getShowChevron() {
        return ((DartCTabFolder) impl).showChevron;
    }

    public void setShowChevron(boolean value) {
    }

    public int getShowListPopupSeq() {
        return ((DartCTabFolder) impl).showListPopupSeq;
    }

    public void setShowListPopupSeq(int value) {
    }

    @SuppressWarnings("removal")
    public boolean getSimple() {
        return ((DartCTabFolder) impl).getSimple();
    }

    public void setSimple(boolean value) {
        ((DartCTabFolder) impl).simple = value;
    }

    public boolean getSingle() {
        return ((DartCTabFolder) impl).getSingle();
    }

    public void setSingle(boolean value) {
        ((DartCTabFolder) impl).single = value;
    }

    public int getStripLayoutSeq() {
        return ((DartCTabFolder) impl).stripLayoutSeq;
    }

    public void setStripLayoutSeq(int value) {
    }

    public Integer getTabHeight() {
        int value = ((DartCTabFolder) impl)._displayedTabHeight();
        return value == dev.equo.swt.size.CTabFolderSizes.TAB_STRIP_HEIGHT ? null : value;
    }

    public void setTabHeight(Integer value) {
        ((DartCTabFolder) impl).tabHeight = value == null ? dev.equo.swt.size.CTabFolderSizes.TAB_STRIP_HEIGHT : value;
    }

    public int getTabPosition() {
        return ((DartCTabFolder) impl).getTabPosition();
    }

    public void setTabPosition(int value) {
        ((DartCTabFolder) impl).tabPosition = value;
    }

    public Control getTopRight() {
        Control val = ((DartCTabFolder) impl).topRight;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopRight(Control value) {
        ((DartCTabFolder) impl).topRight = value;
    }

    public int getTopRightAlignment() {
        return ((DartCTabFolder) impl).getTopRightAlignment();
    }

    public void setTopRightAlignment(int value) {
        ((DartCTabFolder) impl).topRightAlignment = value;
    }

    public boolean getUnselectedCloseVisible() {
        return ((DartCTabFolder) impl).getUnselectedCloseVisible();
    }

    public void setUnselectedCloseVisible(boolean value) {
        ((DartCTabFolder) impl).showUnselectedClose = value;
    }

    public boolean getUnselectedImageVisible() {
        return ((DartCTabFolder) impl).getUnselectedImageVisible();
    }

    public void setUnselectedImageVisible(boolean value) {
        ((DartCTabFolder) impl).showUnselectedImage = value;
    }

    public static final int BORDER_VISIBLE = 512;

    public static final int CHEVRON_VISIBLE = 513;

    public static final int DIRTY_INDICATOR_STYLE = 538;

    public static final int HIGHLIGHT = 514;

    public static final int HIGHLIGHT_ENABLED = 515;

    public static final int ITEMS = 516;

    public static final int MAXIMIZE_VISIBLE = 517;

    public static final int MAXIMIZED = 518;

    public static final int MINIMIZE_VISIBLE = 519;

    public static final int MINIMIZED = 520;

    public static final int MINIMUM_CHARACTERS = 521;

    public static final int SELECTED_IMAGE_VISIBLE = 522;

    public static final int SELECTION = 523;

    public static final int SELECTION_BACKGROUND = 524;

    public static final int SELECTION_BAR_THICKNESS = 525;

    public static final int SELECTION_BG_IMAGE = 526;

    public static final int SELECTION_FOREGROUND = 527;

    public static final int SHOW_CHEVRON = 528;

    public static final int SHOW_LIST_POPUP_SEQ = 529;

    public static final int SINGLE = 530;

    public static final int STRIP_LAYOUT_SEQ = 537;

    public static final int TAB_HEIGHT = 531;

    public static final int TAB_POSITION = 532;

    public static final int TOP_RIGHT = 533;

    public static final int TOP_RIGHT_ALIGNMENT = 534;

    public static final int UNSELECTED_CLOSE_VISIBLE = 535;

    public static final int UNSELECTED_IMAGE_VISIBLE = 536;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BORDER_VISIBLE:
                return "borderVisible";
            case CHEVRON_VISIBLE:
                return "chevronVisible";
            case DIRTY_INDICATOR_STYLE:
                return "dirtyIndicatorStyle";
            case HIGHLIGHT:
                return "highlight";
            case HIGHLIGHT_ENABLED:
                return "highlightEnabled";
            case ITEMS:
                return "items";
            case MAXIMIZE_VISIBLE:
                return "maximizeVisible";
            case MAXIMIZED:
                return "maximized";
            case MINIMIZE_VISIBLE:
                return "minimizeVisible";
            case MINIMIZED:
                return "minimized";
            case MINIMUM_CHARACTERS:
                return "minimumCharacters";
            case SELECTED_IMAGE_VISIBLE:
                return "selectedImageVisible";
            case SELECTION:
                return "selection";
            case SELECTION_BACKGROUND:
                return "selectionBackground";
            case SELECTION_BAR_THICKNESS:
                return "selectionBarThickness";
            case SELECTION_BG_IMAGE:
                return "selectionBgImage";
            case SELECTION_FOREGROUND:
                return "selectionForeground";
            case SHOW_CHEVRON:
                return "showChevron";
            case SHOW_LIST_POPUP_SEQ:
                return "showListPopupSeq";
            case SINGLE:
                return "single";
            case STRIP_LAYOUT_SEQ:
                return "stripLayoutSeq";
            case TAB_HEIGHT:
                return "tabHeight";
            case TAB_POSITION:
                return "tabPosition";
            case TOP_RIGHT:
                return "topRight";
            case TOP_RIGHT_ALIGNMENT:
                return "topRightAlignment";
            case UNSELECTED_CLOSE_VISIBLE:
                return "unselectedCloseVisible";
            case UNSELECTED_IMAGE_VISIBLE:
                return "unselectedImageVisible";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1163:
                return false;
            case 512:
                w.write(getBorderVisible());
                return true;
            case 513:
                w.write(getChevronVisible());
                return true;
            case 538:
                w.write(getDirtyIndicatorStyle());
                return true;
            case 514:
                w.write(getHighlight());
                return true;
            case 515:
                w.write(getHighlightEnabled());
                return true;
            case 516:
                w.refs(getItems());
                return true;
            case 517:
                w.write(getMaximizeVisible());
                return true;
            case 518:
                w.write(getMaximized());
                return true;
            case 519:
                w.write(getMinimizeVisible());
                return true;
            case 520:
                w.write(getMinimized());
                return true;
            case 521:
                w.write((int) getMinimumCharacters());
                return true;
            case 522:
                w.write(getSelectedImageVisible());
                return true;
            case 523:
                w.write((int) getSelection());
                return true;
            case 524:
                w.write(getSelectionBackground());
                return true;
            case 525:
                w.write((int) getSelectionBarThickness());
                return true;
            case 526:
                w.image(getSelectionBgImage());
                return true;
            case 527:
                w.write(getSelectionForeground());
                return true;
            case 528:
                w.write(getShowChevron());
                return true;
            case 529:
                w.write((int) getShowListPopupSeq());
                return true;
            case 530:
                w.write(getSingle());
                return true;
            case 537:
                w.write((int) getStripLayoutSeq());
                return true;
            case 531:
                w.write(getTabHeight());
                return true;
            case 532:
                w.write((int) getTabPosition());
                return true;
            case 533:
                w.ref(getTopRight());
                return true;
            case 534:
                w.write((int) getTopRightAlignment());
                return true;
            case 535:
                w.write(getUnselectedCloseVisible());
                return true;
            case 536:
                w.write(getUnselectedImageVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1163:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 512:
                {
                    boolean v_ = getBorderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(512);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 513:
                {
                    boolean v_ = getChevronVisible();
                    w.key(513);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 538:
                {
                    boolean v_ = getDirtyIndicatorStyle();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(538);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 514:
                {
                    boolean v_ = getHighlight();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(514);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 515:
                {
                    boolean v_ = getHighlightEnabled();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(515);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 516:
                {
                    CTabItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(516);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 517:
                {
                    boolean v_ = getMaximizeVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(517);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 518:
                {
                    boolean v_ = getMaximized();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(518);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 519:
                {
                    boolean v_ = getMinimizeVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(519);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 520:
                {
                    boolean v_ = getMinimized();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(520);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 521:
                {
                    int v_ = getMinimumCharacters();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(521);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 522:
                {
                    boolean v_ = getSelectedImageVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(522);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 523:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(523);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 524:
                {
                    Color v_ = getSelectionBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(524);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 525:
                {
                    int v_ = getSelectionBarThickness();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(525);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 526:
                {
                    Image v_ = getSelectionBgImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(526);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 527:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(527);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 528:
                {
                    boolean v_ = getShowChevron();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(528);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 529:
                {
                    int v_ = getShowListPopupSeq();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(529);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 530:
                {
                    boolean v_ = getSingle();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(530);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 537:
                {
                    int v_ = getStripLayoutSeq();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(537);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 531:
                {
                    Integer v_ = getTabHeight();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(531);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 532:
                {
                    int v_ = getTabPosition();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(532);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 533:
                {
                    Control v_ = getTopRight();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(533);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 534:
                {
                    int v_ = getTopRightAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(534);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 535:
                {
                    boolean v_ = getUnselectedCloseVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(535);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 536:
                {
                    boolean v_ = getUnselectedImageVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(536);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 512, 513, 538, 514, 515, 516, 517, 518, 519, 520, 521, 522, 523, 524, 525, 526, 527, 528, 529, 530, 537, 531, 532, 533, 534, 535, 536 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        switch(key) {
            case 513:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
