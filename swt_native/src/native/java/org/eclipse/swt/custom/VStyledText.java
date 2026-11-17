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
        int kept = 0;
        for (StyleRange v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        StyleRange[] result = new StyleRange[kept];
        kept = 0;
        for (StyleRange v : values) if (v != null)
            result[kept++] = v;
        return result;
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

    public Rectangle getBlockSelectionBounds() {
        return ((DartStyledText) impl).wireBlockSelectionBounds();
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

    public static final int ALIGNMENT = 4864;

    public static final int ALWAYS_SHOW_SCROLL_BARS = 4865;

    public static final int BLOCK_SELECTION = 4866;

    public static final int BLOCK_SELECTION_BOUNDS = 4867;

    public static final int BOTTOM_MARGIN = 4868;

    public static final int CARET_OFFSET = 4869;

    public static final int CARET_OFFSETS = 4870;

    public static final int DOUBLE_CLICK_ENABLED = 4871;

    public static final int EDITABLE = 4872;

    public static final int FIXED_LINE_HEIGHT = 4873;

    public static final int HORIZONTAL_PIXEL = 4874;

    public static final int INDENT = 4875;

    public static final int JUSTIFY = 4876;

    public static final int LEFT_MARGIN = 4877;

    public static final int LINE_SPACING = 4878;

    public static final int MARGIN_COLOR = 4879;

    public static final int RENDERER = 4880;

    public static final int RIGHT_MARGIN = 4881;

    public static final int SELECTION = 4882;

    public static final int SELECTION_BACKGROUND = 4883;

    public static final int SELECTION_FOREGROUND = 4884;

    public static final int SELECTION_RANGES = 4885;

    public static final int STYLE_INDEX = 4886;

    public static final int STYLES = 4887;

    public static final int TAB_STOPS = 4888;

    public static final int TABS = 4889;

    public static final int TEXT = 4890;

    public static final int TOP_MARGIN = 4891;

    public static final int TOP_PIXEL = 4892;

    public static final int WORD_WRAP = 4893;

    public static final int WRAP_INDENT = 4894;

    @Override
    public String wireName(int key) {
        switch(key) {
            case ALIGNMENT:
                return "alignment";
            case ALWAYS_SHOW_SCROLL_BARS:
                return "alwaysShowScrollBars";
            case BLOCK_SELECTION:
                return "blockSelection";
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
            case BLOCK_SELECTION_BOUNDS:
                return "blockSelectionBounds";
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
            case 4864:
                w.write((int) getAlignment());
                return true;
            case 4865:
                w.write(getAlwaysShowScrollBars());
                return true;
            case 4866:
                w.write(getBlockSelection());
                return true;
            case 4868:
                w.write((int) getBottomMargin());
                return true;
            case 4869:
                w.write((int) getCaretOffset());
                return true;
            case 4871:
                w.write(getDoubleClickEnabled());
                return true;
            case 4872:
                w.write(getEditable());
                return true;
            case 4874:
                w.write((int) getHorizontalPixel());
                return true;
            case 4875:
                w.write((int) getIndent());
                return true;
            case 4876:
                w.write(getJustify());
                return true;
            case 4877:
                w.write((int) getLeftMargin());
                return true;
            case 4878:
                w.write((int) getLineSpacing());
                return true;
            case 4879:
                w.write(getMarginColor());
                return true;
            case 4881:
                w.write((int) getRightMargin());
                return true;
            case 4882:
                w.write(getSelection());
                return true;
            case 4883:
                w.write(getSelectionBackground());
                return true;
            case 4884:
                w.write(getSelectionForeground());
                return true;
            case 4888:
                w.write(getTabStops());
                return true;
            case 4889:
                w.write((int) getTabs());
                return true;
            case 4890:
                w.write(getText());
                return true;
            case 4891:
                w.write((int) getTopMargin());
                return true;
            case 4892:
                w.write((int) getTopPixel());
                return true;
            case 4893:
                w.write(getWordWrap());
                return true;
            case 4894:
                w.write((int) getWrapIndent());
                return true;
            case 4880:
                w.object(getRenderer());
                return true;
            case 4885:
                w.write(getSelectionRanges());
                return true;
            case 4870:
                w.write(getCaretOffsets());
                return true;
            case 4867:
                w.write(getBlockSelectionBounds());
                return true;
            case 4887:
                {
                    StyleRange[] a_ = getStyles();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (int i_ = 0; i_ < a_.length; i_++) {
                            StyleRange e_ = a_[i_];
                            if (e_ == null) {
                                i_ += w.nulls(a_, i_) - 1;
                                continue;
                            }
                            w.object(e_);
                        }
                    }
                }
                return true;
            case 4886:
                w.runs(getStyleIndex());
                return true;
            case 4873:
                w.write(getFixedLineHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 4864:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4864);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4865:
                {
                    boolean v_ = getAlwaysShowScrollBars();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4865);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4866:
                {
                    boolean v_ = getBlockSelection();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4866);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4868:
                {
                    int v_ = getBottomMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4868);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4869:
                {
                    int v_ = getCaretOffset();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4869);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4871:
                {
                    boolean v_ = getDoubleClickEnabled();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4871);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4872:
                {
                    boolean v_ = getEditable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4872);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4874:
                {
                    int v_ = getHorizontalPixel();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4874);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4875:
                {
                    int v_ = getIndent();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4875);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4876:
                {
                    boolean v_ = getJustify();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4876);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4877:
                {
                    int v_ = getLeftMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4877);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4878:
                {
                    int v_ = getLineSpacing();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4878);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4879:
                {
                    Color v_ = getMarginColor();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4879);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4881:
                {
                    int v_ = getRightMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4881);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4882:
                {
                    Point v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4882);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4883:
                {
                    Color v_ = getSelectionBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4883);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4884:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4884);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4888:
                {
                    int[] v_ = getTabStops();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4888);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4889:
                {
                    int v_ = getTabs();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4889);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4890:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4890);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4891:
                {
                    int v_ = getTopMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4891);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4892:
                {
                    int v_ = getTopPixel();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4892);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4893:
                {
                    boolean v_ = getWordWrap();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4893);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4894:
                {
                    int v_ = getWrapIndent();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4894);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4880:
                {
                    VStyledTextRenderer v_ = getRenderer();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4880);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4885:
                {
                    int[] v_ = getSelectionRanges();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4885);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4870:
                {
                    int[] v_ = getCaretOffsets();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4870);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4867:
                {
                    Rectangle v_ = getBlockSelectionBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4867);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4887:
                {
                    StyleRange[] v_ = getStyles();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4887);
                    {
                        StyleRange[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (int i_ = 0; i_ < a_.length; i_++) {
                                StyleRange e_ = a_[i_];
                                if (e_ == null) {
                                    i_ += w.nulls(a_, i_) - 1;
                                    continue;
                                }
                                w.object(e_);
                            }
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4886:
                {
                    int[] v_ = getStyleIndex();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(4886);
                    w.runs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 4873:
                {
                    boolean v_ = getFixedLineHeight();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(4873);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VCanvas.allWireKeys_(), new int[] { 4864, 4865, 4866, 4868, 4869, 4871, 4872, 4874, 4875, 4876, 4877, 4878, 4879, 4881, 4882, 4883, 4884, 4888, 4889, 4890, 4891, 4892, 4893, 4894, 4880, 4885, 4870, 4867, 4887, 4886, 4873 });

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
