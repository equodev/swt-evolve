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

    public static final int BORDER_VISIBLE = 36;

    public static final int CHEVRON_VISIBLE = 37;

    public static final int HIGHLIGHT = 38;

    public static final int HIGHLIGHT_ENABLED = 39;

    public static final int ITEMS = 40;

    public static final int MAXIMIZE_VISIBLE = 41;

    public static final int MAXIMIZED = 42;

    public static final int MINIMIZE_VISIBLE = 43;

    public static final int MINIMIZED = 44;

    public static final int MINIMUM_CHARACTERS = 45;

    public static final int SELECTED_IMAGE_VISIBLE = 46;

    public static final int SELECTION = 47;

    public static final int SELECTION_BACKGROUND = 48;

    public static final int SELECTION_BAR_THICKNESS = 49;

    public static final int SELECTION_BG_IMAGE = 50;

    public static final int SELECTION_FOREGROUND = 51;

    public static final int SHOW_CHEVRON = 52;

    public static final int SHOW_LIST_POPUP_SEQ = 53;

    public static final int SINGLE = 54;

    public static final int TAB_HEIGHT = 55;

    public static final int TAB_POSITION = 56;

    public static final int TOP_RIGHT = 57;

    public static final int TOP_RIGHT_ALIGNMENT = 58;

    public static final int UNSELECTED_CLOSE_VISIBLE = 59;

    public static final int UNSELECTED_IMAGE_VISIBLE = 60;

    @Override
    public String wireName(int key) {
        switch(key) {
            case BORDER_VISIBLE:
                return "borderVisible";
            case CHEVRON_VISIBLE:
                return "chevronVisible";
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
            case 36:
                w.write(getBorderVisible());
                return true;
            case 37:
                w.write(getChevronVisible());
                return true;
            case 38:
                w.write(getHighlight());
                return true;
            case 39:
                w.write(getHighlightEnabled());
                return true;
            case 40:
                w.refs(getItems());
                return true;
            case 41:
                w.write(getMaximizeVisible());
                return true;
            case 42:
                w.write(getMaximized());
                return true;
            case 43:
                w.write(getMinimizeVisible());
                return true;
            case 44:
                w.write(getMinimized());
                return true;
            case 45:
                w.write((int) getMinimumCharacters());
                return true;
            case 46:
                w.write(getSelectedImageVisible());
                return true;
            case 47:
                w.write((int) getSelection());
                return true;
            case 48:
                w.write(getSelectionBackground());
                return true;
            case 49:
                w.write((int) getSelectionBarThickness());
                return true;
            case 50:
                w.image(getSelectionBgImage());
                return true;
            case 51:
                w.write(getSelectionForeground());
                return true;
            case 52:
                w.write(getShowChevron());
                return true;
            case 53:
                w.write((int) getShowListPopupSeq());
                return true;
            case 54:
                w.write(getSingle());
                return true;
            case 55:
                w.write(getTabHeight());
                return true;
            case 56:
                w.write((int) getTabPosition());
                return true;
            case 57:
                w.ref(getTopRight());
                return true;
            case 58:
                w.write((int) getTopRightAlignment());
                return true;
            case 59:
                w.write(getUnselectedCloseVisible());
                return true;
            case 60:
                w.write(getUnselectedImageVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 36:
                {
                    boolean v_ = getBorderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(36);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 37:
                {
                    boolean v_ = getChevronVisible();
                    w.key(37);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 38:
                {
                    boolean v_ = getHighlight();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(38);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 39:
                {
                    boolean v_ = getHighlightEnabled();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(39);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 40:
                {
                    CTabItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(40);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 41:
                {
                    boolean v_ = getMaximizeVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(41);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 42:
                {
                    boolean v_ = getMaximized();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(42);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 43:
                {
                    boolean v_ = getMinimizeVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(43);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 44:
                {
                    boolean v_ = getMinimized();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(44);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 45:
                {
                    int v_ = getMinimumCharacters();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(45);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 46:
                {
                    boolean v_ = getSelectedImageVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(46);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 47:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(47);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 48:
                {
                    Color v_ = getSelectionBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(48);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 49:
                {
                    int v_ = getSelectionBarThickness();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(49);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 50:
                {
                    Image v_ = getSelectionBgImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(50);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 51:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(51);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 52:
                {
                    boolean v_ = getShowChevron();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(52);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 53:
                {
                    int v_ = getShowListPopupSeq();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(53);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 54:
                {
                    boolean v_ = getSingle();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(54);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 55:
                {
                    Integer v_ = getTabHeight();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(55);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 56:
                {
                    int v_ = getTabPosition();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(56);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 57:
                {
                    Control v_ = getTopRight();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(57);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 58:
                {
                    int v_ = getTopRightAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(58);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 59:
                {
                    boolean v_ = getUnselectedCloseVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(59);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 60:
                {
                    boolean v_ = getUnselectedImageVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(60);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60 });

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
            case 37:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
