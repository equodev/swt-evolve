package org.eclipse.swt.custom;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.widgets.*;

public class VStyledText extends VCanvas {

    protected VStyledText() {
    }

    protected VStyledText(DartStyledText impl) {
        super(impl);
    }

    public int getAlignment() {
        return ((DartStyledText) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartStyledText) impl).alignment = value;
    }

    public boolean getAlwaysShowScrollBars() {
        return ((DartStyledText) impl).getAlwaysShowScrollBars();
    }

    public void setAlwaysShowScrollBars(boolean value) {
        ((DartStyledText) impl).alwaysShowScroll = value;
    }

    public boolean getBidiColoring() {
        return ((DartStyledText) impl).getBidiColoring();
    }

    public void setBidiColoring(boolean value) {
        ((DartStyledText) impl).bidiColoring = value;
    }

    public boolean getBlockSelection() {
        return ((DartStyledText) impl).getBlockSelection();
    }

    public void setBlockSelection(boolean value) {
        ((DartStyledText) impl).blockSelection = value;
    }

    public Rectangle getBlockSelectionBounds() {
        return ((DartStyledText) impl).blockSelectionBounds;
    }

    public void setBlockSelectionBounds(Rectangle value) {
        ((DartStyledText) impl).blockSelectionBounds = value;
    }

    public int getBottomMargin() {
        return ((DartStyledText) impl).getBottomMargin();
    }

    public void setBottomMargin(int value) {
        ((DartStyledText) impl).bottomMargin = value;
    }

    public int getCaretOffset() {
        return ((DartStyledText) impl).getCaretOffset();
    }

    public void setCaretOffset(int value) {
        ((DartStyledText) impl).setCaretOffset(value);
    }

    public int getColumnX() {
        return ((DartStyledText) impl).columnX;
    }

    public void setColumnX(int value) {
        ((DartStyledText) impl).columnX = value;
    }

    public StyledTextContent getContent() {
        return ((DartStyledText) impl).content;
    }

    public void setContent(StyledTextContent value) {
        ((DartStyledText) impl).content = value;
    }

    public boolean getDoubleClickEnabled() {
        return ((DartStyledText) impl).getDoubleClickEnabled();
    }

    public void setDoubleClickEnabled(boolean value) {
        ((DartStyledText) impl).doubleClickEnabled = value;
    }

    public boolean getEditable() {
        return ((DartStyledText) impl).getEditable();
    }

    public void setEditable(boolean value) {
        ((DartStyledText) impl).editable = value;
    }

    public FontMetrics getFixedLineMetrics() {
        FontMetrics val = ((DartStyledText) impl).fixedLineMetrics;
        if (val != null && !(val.getImpl() instanceof DartFontMetrics))
            return null;
        return val;
    }

    public void setFixedLineMetrics(FontMetrics value) {
        ((DartStyledText) impl).fixedLineMetrics = value;
    }

    public int getHorizontalIndex() {
        return ((DartStyledText) impl).getHorizontalIndex();
    }

    public void setHorizontalIndex(int value) {
        ((DartStyledText) impl).horizontalIndex = value;
    }

    public int getHorizontalPixel() {
        return ((DartStyledText) impl).getHorizontalPixel();
    }

    public void setHorizontalPixel(int value) {
        ((DartStyledText) impl).horizontalScrollOffset = value;
    }

    public int getIndent() {
        return ((DartStyledText) impl).getIndent();
    }

    public void setIndent(int value) {
        ((DartStyledText) impl).indent = value;
    }

    public boolean getJustify() {
        return ((DartStyledText) impl).getJustify();
    }

    public void setJustify(boolean value) {
        ((DartStyledText) impl).justify = value;
    }

    public int getLeftMargin() {
        return ((DartStyledText) impl).getLeftMargin();
    }

    public void setLeftMargin(int value) {
        ((DartStyledText) impl).leftMargin = value;
    }

    public int getLineSpacing() {
        return ((DartStyledText) impl).getLineSpacing();
    }

    public void setLineSpacing(int value) {
        ((DartStyledText) impl).lineSpacing = value;
    }

    public Color getMarginColor() {
        return ((DartStyledText) impl).marginColor;
    }

    public void setMarginColor(Color value) {
        ((DartStyledText) impl).marginColor = value;
    }

    public boolean getMouseNavigatorEnabled() {
        return ((DartStyledText) impl).getMouseNavigatorEnabled();
    }

    public void setMouseNavigatorEnabled(boolean value) {
        ((DartStyledText) impl).mouseNavigatorEnabled = value;
    }

    public int[] getRanges() {
        return ((DartStyledText) impl).ranges;
    }

    public void setRanges(int[] value) {
        ((DartStyledText) impl).ranges = value;
    }

    public int getRightMargin() {
        return ((DartStyledText) impl).getRightMargin();
    }

    public void setRightMargin(int value) {
        ((DartStyledText) impl).rightMargin = value;
    }

    public Point getSelection() {
        return ((DartStyledText) impl).getSelection();
    }

    public void setSelection(Point value) {
        ((DartStyledText) impl).setSelection(value);
    }

    public Color getSelectionBackground() {
        return ((DartStyledText) impl).selectionBackground;
    }

    public void setSelectionBackground(Color value) {
        ((DartStyledText) impl).selectionBackground = value;
    }

    public Color getSelectionForeground() {
        return ((DartStyledText) impl).selectionForeground;
    }

    public void setSelectionForeground(Color value) {
        ((DartStyledText) impl).selectionForeground = value;
    }

    public Point getSelectionRange() {
        return ((DartStyledText) impl).selectionRange;
    }

    public void setSelectionRange(Point value) {
        ((DartStyledText) impl).selectionRange = value;
    }

    public int getStyleRange() {
        return ((DartStyledText) impl).columnX;
    }

    public void setStyleRange(int value) {
        ((DartStyledText) impl).columnX = value;
    }

    public StyleRange[] getStyleRanges() {
        StyleRange[] values = ((DartStyledText) impl).getStyleRanges();
        if (values == null)
            return null;
        ArrayList<StyleRange> result = new ArrayList<>(values.length);
        for (StyleRange v : values) if (v != null)
            result.add(v);
        return result.toArray(new StyleRange[0]);
    }

    public void setStyleRanges(StyleRange[] value) {
        ((DartStyledText) impl).setStyleRanges(value);
    }

    public int[] getTabStops() {
        return ((DartStyledText) impl).tabs;
    }

    public void setTabStops(int[] value) {
        ((DartStyledText) impl).tabs = value;
    }

    public int getTabs() {
        return ((DartStyledText) impl).getTabs();
    }

    public void setTabs(int value) {
        ((DartStyledText) impl).tabLength = value;
    }

    public String getText() {
        return ((DartStyledText) impl).getText();
    }

    public void setText(String value) {
        ((DartStyledText) impl).text = value;
    }

    public int getTextLimit() {
        return ((DartStyledText) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartStyledText) impl).textLimit = value;
    }

    public int getTopIndex() {
        return ((DartStyledText) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartStyledText) impl).topIndex = value;
    }

    public int getTopMargin() {
        return ((DartStyledText) impl).getTopMargin();
    }

    public void setTopMargin(int value) {
        ((DartStyledText) impl).topMargin = value;
    }

    public int getTopPixel() {
        return ((DartStyledText) impl).getTopPixel();
    }

    public void setTopPixel(int value) {
        ((DartStyledText) impl).topPixel = value;
    }

    public int getVerticalScrollOffset() {
        return ((DartStyledText) impl).verticalScrollOffset;
    }

    public void setVerticalScrollOffset(int value) {
        ((DartStyledText) impl).verticalScrollOffset = value;
    }

    public boolean getWordWrap() {
        return ((DartStyledText) impl).getWordWrap();
    }

    public void setWordWrap(boolean value) {
        ((DartStyledText) impl).wordWrap = value;
    }

    public int getWrapIndent() {
        return ((DartStyledText) impl).getWrapIndent();
    }

    public void setWrapIndent(int value) {
        ((DartStyledText) impl).wrapIndent = value;
    }

    public VStyledTextRenderer getRenderer() {
        DartStyledText dartImpl = (DartStyledText) impl;
        if (dartImpl.renderer == null)
            return null;
        if (!(dartImpl.renderer.getImpl() instanceof DartStyledTextRenderer))
            return null;
        return new VStyledTextRenderer((DartStyledTextRenderer) dartImpl.renderer.getImpl());
    }

    public int[] getSelectionRanges() {
        return ((DartStyledText) impl).getSelectionRanges();
    }

    public int[] getCaretOffsets() {
        return ((DartStyledText) impl).caretOffsets;
    }

    public StyleRange[] getStyles() {
        return ((DartStyledText) impl).wireStyles();
    }

    public int[] getStyleIndex() {
        return ((DartStyledText) impl).wireStyleIndex();
    }

    public boolean getFixedLineHeight() {
        return ((DartStyledText) impl).isFixedLineHeight();
    }

    public static final int ALIGNMENT = 179;

    public static final int ALWAYS_SHOW_SCROLL_BARS = 180;

    public static final int BLOCK_SELECTION = 181;

    public static final int BLOCK_SELECTION_BOUNDS = 182;

    public static final int BOTTOM_MARGIN = 183;

    public static final int CARET_OFFSET = 184;

    public static final int CARET_OFFSETS = 185;

    public static final int DOUBLE_CLICK_ENABLED = 186;

    public static final int EDITABLE = 187;

    public static final int FIXED_LINE_HEIGHT = 1364;

    public static final int HORIZONTAL_PIXEL = 188;

    public static final int INDENT = 189;

    public static final int JUSTIFY = 190;

    public static final int LEFT_MARGIN = 191;

    public static final int LINE_SPACING = 192;

    public static final int MARGIN_COLOR = 193;

    public static final int RENDERER = 194;

    public static final int RIGHT_MARGIN = 195;

    public static final int SELECTION = 196;

    public static final int SELECTION_BACKGROUND = 197;

    public static final int SELECTION_FOREGROUND = 198;

    public static final int SELECTION_RANGES = 199;

    public static final int STYLE_INDEX = 200;

    public static final int STYLES = 201;

    public static final int TAB_STOPS = 202;

    public static final int TABS = 203;

    public static final int TEXT = 204;

    public static final int TOP_MARGIN = 205;

    public static final int TOP_PIXEL = 206;

    public static final int WORD_WRAP = 207;

    public static final int WRAP_INDENT = 208;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
            case ALWAYS_SHOW_SCROLL_BARS:
                return "alwaysShowScrollBars";
            case BLOCK_SELECTION:
                return "blockSelection";
            case BLOCK_SELECTION_BOUNDS:
                return "blockSelectionBounds";
            case BOTTOM_MARGIN:
                return "bottomMargin";
            case CARET_OFFSET:
                return "caretOffset";
            case DOUBLE_CLICK_ENABLED:
                return "doubleClickEnabled";
            case EDITABLE:
                return "editable";
            case HORIZONTAL_PIXEL:
                return "horizontalPixel";
            case INDENT:
                return "indent";
            case JUSTIFY:
                return "justify";
            case LEFT_MARGIN:
                return "leftMargin";
            case LINE_SPACING:
                return "lineSpacing";
            case MARGIN_COLOR:
                return "marginColor";
            case RIGHT_MARGIN:
                return "rightMargin";
            case SELECTION:
                return "selection";
            case SELECTION_BACKGROUND:
                return "selectionBackground";
            case SELECTION_FOREGROUND:
                return "selectionForeground";
            case TAB_STOPS:
                return "tabStops";
            case TABS:
                return "tabs";
            case TEXT:
                return "text";
            case TOP_MARGIN:
                return "topMargin";
            case TOP_PIXEL:
                return "topPixel";
            case WORD_WRAP:
                return "wordWrap";
            case WRAP_INDENT:
                return "wrapIndent";
            case RENDERER:
                return "renderer";
            case SELECTION_RANGES:
                return "selectionRanges";
            case CARET_OFFSETS:
                return "caretOffsets";
            case STYLES:
                return "styles";
            case STYLE_INDEX:
                return "styleIndex";
            case FIXED_LINE_HEIGHT:
                return "fixedLineHeight";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 179:
                w.write((int) getAlignment());
                return true;
            case 180:
                w.write(getAlwaysShowScrollBars());
                return true;
            case 181:
                w.write(getBlockSelection());
                return true;
            case 182:
                w.write(getBlockSelectionBounds());
                return true;
            case 183:
                w.write((int) getBottomMargin());
                return true;
            case 184:
                w.write((int) getCaretOffset());
                return true;
            case 186:
                w.write(getDoubleClickEnabled());
                return true;
            case 187:
                w.write(getEditable());
                return true;
            case 188:
                w.write((int) getHorizontalPixel());
                return true;
            case 189:
                w.write((int) getIndent());
                return true;
            case 190:
                w.write(getJustify());
                return true;
            case 191:
                w.write((int) getLeftMargin());
                return true;
            case 192:
                w.write((int) getLineSpacing());
                return true;
            case 193:
                w.write(getMarginColor());
                return true;
            case 195:
                w.write((int) getRightMargin());
                return true;
            case 196:
                w.write(getSelection());
                return true;
            case 197:
                w.write(getSelectionBackground());
                return true;
            case 198:
                w.write(getSelectionForeground());
                return true;
            case 202:
                w.write(getTabStops());
                return true;
            case 203:
                w.write((int) getTabs());
                return true;
            case 204:
                w.write(getText());
                return true;
            case 205:
                w.write((int) getTopMargin());
                return true;
            case 206:
                w.write((int) getTopPixel());
                return true;
            case 207:
                w.write(getWordWrap());
                return true;
            case 208:
                w.write((int) getWrapIndent());
                return true;
            case 194:
                w.object(getRenderer());
                return true;
            case 199:
                w.write(getSelectionRanges());
                return true;
            case 185:
                w.write(getCaretOffsets());
                return true;
            case 201:
                {
                    StyleRange[] a_ = getStyles();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (StyleRange e_ : a_) w.object(e_);
                    }
                }
                return true;
            case 200:
                w.write(getStyleIndex());
                return true;
            case 1364:
                w.write(getFixedLineHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 179:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(179);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 180:
                {
                    boolean v_ = getAlwaysShowScrollBars();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(180);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 181:
                {
                    boolean v_ = getBlockSelection();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(181);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 182:
                {
                    Rectangle v_ = getBlockSelectionBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(182);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 183:
                {
                    int v_ = getBottomMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(183);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 184:
                {
                    int v_ = getCaretOffset();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(184);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 186:
                {
                    boolean v_ = getDoubleClickEnabled();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(186);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 187:
                {
                    boolean v_ = getEditable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(187);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 188:
                {
                    int v_ = getHorizontalPixel();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(188);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 189:
                {
                    int v_ = getIndent();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(189);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 190:
                {
                    boolean v_ = getJustify();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(190);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 191:
                {
                    int v_ = getLeftMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(191);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 192:
                {
                    int v_ = getLineSpacing();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(192);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 193:
                {
                    Color v_ = getMarginColor();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(193);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 195:
                {
                    int v_ = getRightMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(195);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 196:
                {
                    Point v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(196);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 197:
                {
                    Color v_ = getSelectionBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(197);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 198:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(198);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 202:
                {
                    int[] v_ = getTabStops();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(202);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 203:
                {
                    int v_ = getTabs();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(203);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 204:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(204);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 205:
                {
                    int v_ = getTopMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(205);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 206:
                {
                    int v_ = getTopPixel();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(206);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 207:
                {
                    boolean v_ = getWordWrap();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(207);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 208:
                {
                    int v_ = getWrapIndent();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(208);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 194:
                {
                    VStyledTextRenderer v_ = getRenderer();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(194);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 199:
                {
                    int[] v_ = getSelectionRanges();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(199);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 185:
                {
                    int[] v_ = getCaretOffsets();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(185);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 201:
                {
                    StyleRange[] v_ = getStyles();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(201);
                    {
                        StyleRange[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (StyleRange e_ : a_) w.object(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 200:
                {
                    int[] v_ = getStyleIndex();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(200);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1364:
                {
                    boolean v_ = getFixedLineHeight();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1364);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VCanvas.allWireKeys_(), new int[] { 179, 180, 181, 182, 183, 184, 186, 187, 188, 189, 190, 191, 192, 193, 195, 196, 197, 198, 202, 203, 204, 205, 206, 207, 208, 194, 199, 185, 201, 200, 1364 });

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
