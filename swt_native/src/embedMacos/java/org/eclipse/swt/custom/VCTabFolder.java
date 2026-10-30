package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.ArrayList;

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
        ArrayList<Color> result = new ArrayList<>(values.length);
        for (Color v : values) if (v != null)
            result.add(v);
        return result.toArray(new Color[0]);
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
        ArrayList<CTabItem> result = new ArrayList<>(values.length);
        for (CTabItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new CTabItem[0]);
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
        ArrayList<Color> result = new ArrayList<>(values.length);
        for (Color v : values) if (v != null)
            result.add(v);
        return result.toArray(new Color[0]);
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

    public static final int BORDER_VISIBLE = 248;

    public static final int CHEVRON_VISIBLE = 249;

    public static final int HIGHLIGHT = 250;

    public static final int HIGHLIGHT_ENABLED = 251;

    public static final int ITEMS = 252;

    public static final int MAXIMIZE_VISIBLE = 253;

    public static final int MAXIMIZED = 254;

    public static final int MINIMIZE_VISIBLE = 255;

    public static final int MINIMIZED = 256;

    public static final int MINIMUM_CHARACTERS = 257;

    public static final int SELECTED_IMAGE_VISIBLE = 258;

    public static final int SELECTION = 259;

    public static final int SELECTION_BACKGROUND = 260;

    public static final int SELECTION_BAR_THICKNESS = 261;

    public static final int SELECTION_BG_IMAGE = 262;

    public static final int SELECTION_FOREGROUND = 263;

    public static final int SHOW_CHEVRON = 264;

    public static final int SHOW_LIST_POPUP_SEQ = 265;

    public static final int SINGLE = 266;

    public static final int TAB_HEIGHT = 1374;

    public static final int TAB_POSITION = 267;

    public static final int TOP_RIGHT = 268;

    public static final int TOP_RIGHT_ALIGNMENT = 269;

    public static final int UNSELECTED_CLOSE_VISIBLE = 270;

    public static final int UNSELECTED_IMAGE_VISIBLE = 271;

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
            case 248:
                w.write(getBorderVisible());
                return true;
            case 249:
                w.write(getChevronVisible());
                return true;
            case 250:
                w.write(getHighlight());
                return true;
            case 251:
                w.write(getHighlightEnabled());
                return true;
            case 252:
                w.refs(getItems());
                return true;
            case 253:
                w.write(getMaximizeVisible());
                return true;
            case 254:
                w.write(getMaximized());
                return true;
            case 255:
                w.write(getMinimizeVisible());
                return true;
            case 256:
                w.write(getMinimized());
                return true;
            case 257:
                w.write((int) getMinimumCharacters());
                return true;
            case 258:
                w.write(getSelectedImageVisible());
                return true;
            case 259:
                w.write((int) getSelection());
                return true;
            case 260:
                w.write(getSelectionBackground());
                return true;
            case 261:
                w.write((int) getSelectionBarThickness());
                return true;
            case 262:
                w.image(getSelectionBgImage());
                return true;
            case 263:
                w.write(getSelectionForeground());
                return true;
            case 264:
                w.write(getShowChevron());
                return true;
            case 265:
                w.write((int) getShowListPopupSeq());
                return true;
            case 266:
                w.write(getSingle());
                return true;
            case 1374:
                w.write(getTabHeight());
                return true;
            case 267:
                w.write((int) getTabPosition());
                return true;
            case 268:
                w.ref(getTopRight());
                return true;
            case 269:
                w.write((int) getTopRightAlignment());
                return true;
            case 270:
                w.write(getUnselectedCloseVisible());
                return true;
            case 271:
                w.write(getUnselectedImageVisible());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 248:
                {
                    boolean v_ = getBorderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(248);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 249:
                {
                    boolean v_ = getChevronVisible();
                    w.key(249);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 250:
                {
                    boolean v_ = getHighlight();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(250);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 251:
                {
                    boolean v_ = getHighlightEnabled();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(251);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 252:
                {
                    CTabItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(252);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 253:
                {
                    boolean v_ = getMaximizeVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(253);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 254:
                {
                    boolean v_ = getMaximized();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(254);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 255:
                {
                    boolean v_ = getMinimizeVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(255);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 256:
                {
                    boolean v_ = getMinimized();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(256);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 257:
                {
                    int v_ = getMinimumCharacters();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(257);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 258:
                {
                    boolean v_ = getSelectedImageVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(258);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 259:
                {
                    int v_ = getSelection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(259);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 260:
                {
                    Color v_ = getSelectionBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(260);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 261:
                {
                    int v_ = getSelectionBarThickness();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(261);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 262:
                {
                    Image v_ = getSelectionBgImage();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(262);
                    w.image(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 263:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(263);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 264:
                {
                    boolean v_ = getShowChevron();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(264);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 265:
                {
                    int v_ = getShowListPopupSeq();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(265);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 266:
                {
                    boolean v_ = getSingle();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(266);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1374:
                {
                    Integer v_ = getTabHeight();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1374);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 267:
                {
                    int v_ = getTabPosition();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(267);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 268:
                {
                    Control v_ = getTopRight();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(268);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 269:
                {
                    int v_ = getTopRightAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(269);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 270:
                {
                    boolean v_ = getUnselectedCloseVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(270);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 271:
                {
                    boolean v_ = getUnselectedImageVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(271);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 1374, 267, 268, 269, 270, 271 });

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
            case 249:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
