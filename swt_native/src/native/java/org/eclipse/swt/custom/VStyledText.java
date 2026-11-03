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

    public static final int ALIGNMENT = 474;

    public static final int ALWAYS_SHOW_SCROLL_BARS = 475;

    public static final int BLOCK_SELECTION = 476;

    public static final int BLOCK_SELECTION_BOUNDS = 477;

    public static final int BOTTOM_MARGIN = 478;

    public static final int CARET_OFFSET = 479;

    public static final int CARET_OFFSETS = 480;

    public static final int DOUBLE_CLICK_ENABLED = 481;

    public static final int EDITABLE = 482;

    public static final int FIXED_LINE_HEIGHT = 483;

    public static final int HORIZONTAL_PIXEL = 484;

    public static final int INDENT = 485;

    public static final int JUSTIFY = 486;

    public static final int LEFT_MARGIN = 487;

    public static final int LINE_SPACING = 488;

    public static final int MARGIN_COLOR = 489;

    public static final int RENDERER = 490;

    public static final int RIGHT_MARGIN = 491;

    public static final int SELECTION = 492;

    public static final int SELECTION_BACKGROUND = 493;

    public static final int SELECTION_FOREGROUND = 494;

    public static final int SELECTION_RANGES = 495;

    public static final int STYLE_INDEX = 496;

    public static final int STYLES = 497;

    public static final int TAB_STOPS = 498;

    public static final int TABS = 499;

    public static final int TEXT = 500;

    public static final int TOP_MARGIN = 501;

    public static final int TOP_PIXEL = 502;

    public static final int WORD_WRAP = 503;

    public static final int WRAP_INDENT = 504;

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
            case 474:
                w.write((int) getAlignment());
                return true;
            case 475:
                w.write(getAlwaysShowScrollBars());
                return true;
            case 476:
                w.write(getBlockSelection());
                return true;
            case 478:
                w.write((int) getBottomMargin());
                return true;
            case 479:
                w.write((int) getCaretOffset());
                return true;
            case 481:
                w.write(getDoubleClickEnabled());
                return true;
            case 482:
                w.write(getEditable());
                return true;
            case 484:
                w.write((int) getHorizontalPixel());
                return true;
            case 485:
                w.write((int) getIndent());
                return true;
            case 486:
                w.write(getJustify());
                return true;
            case 487:
                w.write((int) getLeftMargin());
                return true;
            case 488:
                w.write((int) getLineSpacing());
                return true;
            case 489:
                w.write(getMarginColor());
                return true;
            case 491:
                w.write((int) getRightMargin());
                return true;
            case 492:
                w.write(getSelection());
                return true;
            case 493:
                w.write(getSelectionBackground());
                return true;
            case 494:
                w.write(getSelectionForeground());
                return true;
            case 498:
                w.write(getTabStops());
                return true;
            case 499:
                w.write((int) getTabs());
                return true;
            case 500:
                w.write(getText());
                return true;
            case 501:
                w.write((int) getTopMargin());
                return true;
            case 502:
                w.write((int) getTopPixel());
                return true;
            case 503:
                w.write(getWordWrap());
                return true;
            case 504:
                w.write((int) getWrapIndent());
                return true;
            case 490:
                w.object(getRenderer());
                return true;
            case 495:
                w.write(getSelectionRanges());
                return true;
            case 480:
                w.write(getCaretOffsets());
                return true;
            case 477:
                w.write(getBlockSelectionBounds());
                return true;
            case 497:
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
            case 496:
                w.write(getStyleIndex());
                return true;
            case 483:
                w.write(getFixedLineHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 474:
                {
                    int v_ = getAlignment();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(474);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 475:
                {
                    boolean v_ = getAlwaysShowScrollBars();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(475);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 476:
                {
                    boolean v_ = getBlockSelection();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(476);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 478:
                {
                    int v_ = getBottomMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(478);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 479:
                {
                    int v_ = getCaretOffset();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(479);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 481:
                {
                    boolean v_ = getDoubleClickEnabled();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(481);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 482:
                {
                    boolean v_ = getEditable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(482);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 484:
                {
                    int v_ = getHorizontalPixel();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(484);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 485:
                {
                    int v_ = getIndent();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(485);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 486:
                {
                    boolean v_ = getJustify();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(486);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 487:
                {
                    int v_ = getLeftMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(487);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 488:
                {
                    int v_ = getLineSpacing();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(488);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 489:
                {
                    Color v_ = getMarginColor();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(489);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 491:
                {
                    int v_ = getRightMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(491);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 492:
                {
                    Point v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(492);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 493:
                {
                    Color v_ = getSelectionBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(493);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 494:
                {
                    Color v_ = getSelectionForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(494);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 498:
                {
                    int[] v_ = getTabStops();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(498);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 499:
                {
                    int v_ = getTabs();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(499);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 500:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(500);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 501:
                {
                    int v_ = getTopMargin();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(501);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 502:
                {
                    int v_ = getTopPixel();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(502);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 503:
                {
                    boolean v_ = getWordWrap();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(503);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 504:
                {
                    int v_ = getWrapIndent();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(504);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 490:
                {
                    VStyledTextRenderer v_ = getRenderer();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(490);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 495:
                {
                    int[] v_ = getSelectionRanges();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(495);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 480:
                {
                    int[] v_ = getCaretOffsets();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(480);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 477:
                {
                    Rectangle v_ = getBlockSelectionBounds();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(477);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 497:
                {
                    StyleRange[] v_ = getStyles();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(497);
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
            case 496:
                {
                    int[] v_ = getStyleIndex();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(496);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 483:
                {
                    boolean v_ = getFixedLineHeight();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(483);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VCanvas.allWireKeys_(), new int[] { 474, 475, 476, 478, 479, 481, 482, 484, 485, 486, 487, 488, 489, 491, 492, 493, 494, 498, 499, 500, 501, 502, 503, 504, 490, 495, 480, 477, 497, 496, 483 });

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
