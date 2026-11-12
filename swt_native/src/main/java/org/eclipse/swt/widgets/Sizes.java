package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.*;
import org.eclipse.swt.SWT;
import java.util.List;
import org.eclipse.swt.custom.DartCCombo;
import org.eclipse.swt.custom.DartCLabel;
import org.eclipse.swt.custom.CTabFolderHelper;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.custom.DartStyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GCHelper;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class Sizes {
    private static final double AVERAGE_CHAR_WIDTH = 7.974;
    private static final double HORIZONTAL_PADDING = 12.0;

    public static Point compute(DartCTabFolder impl) {
        int tabsWidth = impl.getItems().length * 80;
        int contentWidth = 0;
        int contentHeight = 0;
        int selectedIndex = impl.getSelectionIndex();
        if (selectedIndex != -1) {
            CTabItem item = impl.getItem(selectedIndex);
            Control control = item != null ? item.getControl() : null;
            if (control != null && !control.isDisposed()) {
                Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
                contentWidth = size.x;
                contentHeight = size.y;
            }
        }
        // Through computeTrim, so the strip and the frame a folder reserves for its page are the
        // same numbers it asks for when it is sized to that page.
        Rectangle trim = computeTrim(impl, 0, 0, Math.max(tabsWidth, contentWidth), contentHeight);
        return new Point(trim.width, trim.height);
    }

    public static Point computeSize(DartButton c, int wHint, int hHint, boolean changed) {
        Point size = ButtonSizes.computeSize(c, wHint, hHint, changed);
        if (wHint == SWT.DEFAULT)
            size.x += 2 * pushButtonMargin(c.getApi().getStyle());
        return size;
    }

    /** The transparent margin on each side of a bezelled button; the render side paints inside it. */
    private static int pushButtonMargin(int style) {
        if ((style & (SWT.PUSH | SWT.TOGGLE)) == 0 || (style & (SWT.FLAT | SWT.WRAP)) != 0)
            return 0;
        return Config.getConfigFlags().push_button_margin;
    }

    public static Point computeSize(DartTableItem widget, int wHint, int hHint, boolean changed) {
        return TableItemSizes.computeSize(widget, wHint, hHint, changed);
    }

    public static Point computeSize(DartTreeItem widget, int wHint, int hHint, boolean changed) {
        return TreeItemSizes.computeSize(widget, wHint, hHint, changed);
    }

    public static int packWidth(DartTableColumn column) {
        return TableColumnHelper.packWidth(column);
    }

    public static Point computeSize(DartCCombo c, int wHint, int hHint, boolean changed) {
        return CComboSizes.computeSize(c, wHint, hHint, changed);
    }

    /**
     * CLabelSizes is measured with the default margin on each side; the label's own margins take its place.
     * A hint stands as given.
     */
    public static Point computeSize(DartCLabel c, int wHint, int hHint, boolean changed) {
        Point size = CLabelSizes.computeSize(c, wHint, hHint, changed);
        int width = wHint != SWT.DEFAULT ? size.x
                : Math.max(0, size.x + c.getLeftMargin() + c.getRightMargin() - 2 * DartCLabel.DEFAULT_MARGIN);
        int height = hHint != SWT.DEFAULT ? size.y
                : Math.max(0, size.y + c.getTopMargin() + c.getBottomMargin() - 2 * DartCLabel.DEFAULT_MARGIN);
        return new Point(width, height);
    }

    public static Point computeSize(DartCombo c, int wHint, int hHint, boolean changed) {
        return ComboSizes.computeSize(c, wHint, hHint, changed);
    }

    private static boolean inInfoPopup(Control c) {
        try {
            Shell s = c == null ? null : c.getShell();
            return s != null && (s.getStyle() & SWT.ON_TOP) != 0 && (s.getStyle() & SWT.NO_TRIM) != 0;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static Point computeSize(DartComposite composite, int wHint, int hHint, boolean changed) {
        if (wHint != SWT.DEFAULT && hHint == SWT.DEFAULT && inInfoPopup(composite.getApi())) {
            try {
                for (Control child : composite.getApi().getChildren()) {
                    if (child instanceof org.eclipse.swt.custom.StyledText && (child.getStyle() & SWT.WRAP) != 0) {
                        Point cPref = computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, changed);
                        Point stPref = child.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                        int avail = Math.max(1, wHint - Math.max(0, cPref.x - stPref.x));
                        return new Point(wHint, child.computeSize(avail, SWT.DEFAULT).y + Math.max(0, cPref.y - stPref.y));
                    }
                }
            } catch (Throwable ignore) {}
        }
        Point size;
        if (composite.layout != null) {
            if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
                changed |= (composite.getApi().state & DartWidget.LAYOUT_CHANGED) != 0;
                size = composite.layout.computeSize(composite.getApi(), wHint, hHint, changed);
                composite.getApi().state &= ~DartWidget.LAYOUT_CHANGED;
            } else {
                size = new Point(wHint, hHint);
            }
        } else {
            size = Sizes.minimumSize(composite, wHint, hHint, changed);
            if (size.x == 0)
                size.x = DartWidget.DEFAULT_WIDTH;
            if (size.y == 0) {
                size.y = DartWidget.DEFAULT_HEIGHT;
            }
        }
        if (wHint != SWT.DEFAULT)
            size.x = wHint;
        if (hHint != SWT.DEFAULT)
            size.y = hHint;
        // Through the API object: a subclass may override computeTrim to reserve room it paints itself.
        Rectangle trim = composite.getApi().computeTrim(0, 0, size.x, size.y);
        return new Point(trim.width, trim.height);
    }

    /**
     * Counts how many visual lines a single logical line wraps into when laid out at
     * {@code maxWidth} (greedy word-wrap, matching SWT.WRAP). Uses the same
     * {@link GCHelper#textExtent} measurement as the rest of computeSize so the wrap
     * point stays consistent with rendering.
     */
    private static int wrapLineCount(String line, int maxWidth, Font font) {
        if (line == null || line.isEmpty()) return 1;
        if (GCHelper.textExtent(line, SWT.DRAW_TAB, font).x <= maxWidth) return 1;
        int spaceW = GCHelper.textExtent(" ", SWT.DRAW_TAB, font).x;
        String[] words = line.split(" ", -1);
        int lines = 1;
        int cur = 0;
        boolean lineStart = true;
        for (String word : words) {
            if (word.isEmpty()) {
                if (!lineStart) cur += spaceW;
                continue;
            }
            int wW = GCHelper.textExtent(word, SWT.DRAW_TAB, font).x;
            if (lineStart) {
                cur = wW;
                lineStart = false;
            } else {
                int extended = cur + spaceW + wW;
                if (extended > maxWidth) {
                    lines++;
                    cur = wW;
                } else {
                    cur = extended;
                }
            }
        }
        return lines;
    }

    public static Point computeSize(DartStyledText c, int wHint, int hHint, boolean changed) {
        StyledTextContent content = c._content();
        int lc = (c.getApi().getStyle() & SWT.SINGLE) != 0 ? 1 : content.getLineCount();
        Font font = c.getFont();
        boolean wrap = c.getWordWrap();
        // getLeftMargin() subtracts the SINGLE-line alignmentMargin (a right/center render offset that
        // isn't real trim); using the raw leftMargin field inflated computeSize after setAlignment(RIGHT).
        int wTrim = c.getLeftMargin() + c._rightMargin();
        Caret caret = c.getCaret();
        if (caret != null) wTrim += caret.getSize().x;
        int hTrim = c._topMargin() + c._bottomMargin();
        int width = 0;
        int height = 0;
        if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
            // When the widget wraps (SWT.WRAP) and the width is constrained, lay each
            // logical line out to the available width so the height accounts for the
            // extra visual lines (otherwise a long line is clipped, not wrapped).
            int wrapWidth = -1;
            if (wrap && wHint != SWT.DEFAULT) {
                wrapWidth = Math.max(1, wHint - wTrim);
            } else if (wrap && inInfoPopup(c.getApi())) {
                try {
                    Composite parent = c.getApi().getParent();
                    int w = parent == null ? -1 : parent.getClientArea().width;
                    if (parent != null && parent.getLayout() instanceof org.eclipse.swt.layout.GridLayout) {
                        org.eclipse.swt.layout.GridLayout gl = (org.eclipse.swt.layout.GridLayout) parent.getLayout();
                        w -= gl.marginLeft + gl.marginRight + gl.marginWidth * 2;
                    }
                    wrapWidth = w > 0 ? Math.max(1, w - wTrim) : -1;
                } catch (Throwable ignore) {}
            }
            int lineH = c.getLineHeight();
            for (int lineIndex = 0; lineIndex < lc; lineIndex++) {
                String line = content.getLine(lineIndex);
                if (line == null) line = "";
                Point extent = GCHelper.textExtent(line, SWT.DRAW_TAB, font);
                if (wrapWidth > 0) {
                    height += lineH * wrapLineCount(line, wrapWidth, font);
                    width = Math.max(width, Math.min(extent.x, wrapWidth));
                } else {
                    width = Math.max(width, extent.x);
                    height += lineH;
                }
            }
        }
        if (width == 0) width = DartWidget.DEFAULT_WIDTH;
        if (height == 0) height = DartWidget.DEFAULT_HEIGHT;
        if (wHint != SWT.DEFAULT) width = wHint;
        if (hHint != SWT.DEFAULT) height = hHint;
        Rectangle rect = c.getApi().computeTrim(0, 0, width + wTrim, height + hTrim);
        return new Point(rect.width, rect.height);
    }

    public static Point computeSize(DartDateTime c, int wHint, int hHint, boolean changed) {
        int style = c.getApi().style;
        int width, height;
        if ((style & SWT.CALENDAR) != 0) {
            // SWT.CALENDAR_WEEKNUMBERS by value: not every SWT build declares the constant.
            boolean weekNumbers = (style & (1 << 14)) != 0;
            width = (weekNumbers ? 8 : 7) * 28 + 8;
            height = 30 + 28 + 6 * 28 + 8;
        } else if ((style & SWT.TIME) != 0) {
            if ((style & SWT.SHORT) != 0) {
                width = 66;   // HH + : + MM + spinner + border → 21+6+21+20+2 - 4
            } else {
                width = 93;   // HH + : + MM + : + SS + spinner + border → 21+6+21+6+21+20+2 - 4
            }
            height = 32;
        } else {
            // A drop-down date has the calendar button instead of the up/down buttons, and it is wider.
            int buttons = (style & SWT.DROP_DOWN) != 0 ? 24 : 20;
            if ((style & SWT.SHORT) != 0) {
                width = 60 + buttons;  // MM + / + YYYY + border → 21+6+37+2 - 6
            } else {
                width = 87 + buttons;  // MM + / + DD + / + YYYY + border → 21+6+21+6+37+2 - 6
            }
            height = 32;
        }
        if (wHint != SWT.DEFAULT) width = wHint;
        if (hHint != SWT.DEFAULT) height = hHint;
        return new Point(width, height);
    }

    public static Point computeSize(DartControl dartControl, int wHint, int hHint, boolean changed) {
        return new Point(100, 100);
    }

    public static Point computeSize(DartCoolBar w, int wHint, int hHint, boolean changed) {
        return CoolBarSizes.computeSize(w.getApi(), wHint, hHint);
    }

    public static Point computeSize(DartCoolItem dartCoolItem, int wHint, int hHint) {
        // Match SWT's CoolItem.computeSize contract: honor the hints and add the item trim on the
        // layout axis. DEFAULT hint -> 32 like upstream.
        int width = wHint == org.eclipse.swt.SWT.DEFAULT ? 32 : wHint;
        int height = hHint == org.eclipse.swt.SWT.DEFAULT ? 32 : hHint;
        CoolBar parent = dartCoolItem.getApi().getParent();
        boolean isVertical = parent != null && (parent.style & org.eclipse.swt.SWT.VERTICAL) != 0;
        if (isVertical) {
            height += CoolBarSizes.ITEM_TRIM;
        } else {
            width += CoolBarSizes.ITEM_TRIM;
        }
        return new Point(width, height);
    }

    public static Point computeSize(DartExpandBar dartExpandBar, int wHint, int hHint, boolean changed) {
        return dev.equo.swt.size.ExpandBarSizes.computeSize(dartExpandBar.getApi(), wHint, hHint);
    }

    /** SWT's header band: the chevron until the bar is given a font, then the taller of chevron and a line. */
    static int getBandHeight(DartExpandBar dartExpandBar, Font font) {
        if (font == null) {
            return dev.equo.swt.size.ExpandBarSizes.CHEVRON_SIZE;
        }
        return dev.equo.swt.size.ExpandBarSizes.bandHeight(dartExpandBar.getApi());
    }

    /** The hints size the content, and the border goes around it, as on Win32 and GTK. */
    public static Point computeSize(DartLabel c, int wHint, int hHint, boolean changed) {
        Point size = LabelSizes.computeSize(c, wHint, hHint, changed);
        int border = ControlHelper.borderWidth(c);
        return new Point(size.x + 2 * border, size.y + 2 * border);
    }

    /**
     * A negative hint other than {@link SWT#DEFAULT} counts as zero. A Link that shows no text is as wide
     * as the measure says, as on Win32 and GTK (Cocoa makes it 64 wide).
     */
    public static Point computeSize(DartLink c, int wHint, int hHint, boolean changed) {
        if (wHint != SWT.DEFAULT && wHint < 0)
            wHint = 0;
        if (hHint != SWT.DEFAULT && hHint < 0)
            hHint = 0;
        return LinkSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartList c, int wHint, int hHint, boolean changed) {
        return ListSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartProgressBar c, int wHint, int hHint, boolean changed) {
        return ProgressBarSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartSash w, int wHint, int hHint, boolean changed) {
        return SashSizes.computeSize(w, wHint, hHint, changed);
    }

    public static Point computeSize(DartScale c, int wHint, int hHint, boolean changed) {
        return ScaleSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartSlider c, int wHint, int hHint, boolean changed) {
        return SliderSizes.computeSize(c, wHint, hHint, changed);
    }

    // The stepper column, the field border and the text field's own horizontal padding are drawn
    // outside the number: spinner_evolve.dart lays the text field out in what is left of the
    // widget bounds once it has taken them. Mirrors spinner_theme_settings.dart, which has to
    // agree with this for the stepper to land inside the width a layout gives the spinner.
    private static final int SPINNER_BUTTON_WIDTH = 20;
    private static final int SPINNER_BORDER_WIDTH = 1;
    private static final int SPINNER_TEXT_PADDING = 8;
    private static final int SPINNER_MIN_HEIGHT = 32;

    public static Rectangle computeTrim(DartSpinner widget, int x, int y, int width, int height) {
        return new Rectangle(
            x - SPINNER_BORDER_WIDTH - SPINNER_TEXT_PADDING,
            y - SPINNER_BORDER_WIDTH,
            width + SPINNER_BUTTON_WIDTH + 2 * (SPINNER_BORDER_WIDTH + SPINNER_TEXT_PADDING),
            height + 2 * SPINNER_BORDER_WIDTH);
    }

    public static Point computeSize(DartSpinner c, int wHint, int hHint, boolean changed) {
        Point text = GCHelper.textExtent(widestSpinnerValue(c), 0, c.getFont());
        int width = wHint != SWT.DEFAULT ? wHint : text.x;
        int height = hHint != SWT.DEFAULT ? hHint : text.y;
        Rectangle trim = computeTrim(c, 0, 0, width, height);
        if (hHint == SWT.DEFAULT) {
            trim.height = Math.max(trim.height, SPINNER_MIN_HEIGHT);
        }
        return new Point(trim.width, trim.height);
    }

    /** The longest value string the spinner's range can put in its text field. */
    private static String widestSpinnerValue(DartSpinner c) {
        String min = SpinnerHelper.format(c._minimum(), c._digits(), SpinnerHelper.decimalSeparator(c));
        String max = SpinnerHelper.format(c._maximum(), c._digits(), SpinnerHelper.decimalSeparator(c));
        return max.length() >= min.length() ? max : min;
    }

    public static Point computeSize(DartTable c, int wHint, int hHint, boolean changed) {
        return dev.equo.swt.size.TableSizes.computeSize(c, wHint, hHint, changed);
    }

    public static Point computeSize(DartTabFolder w, int wHint, int hHint, boolean flushCache) {
        Control[] children = w._getChildren();
        int width = 0, height = 0;
        for (int i = 0; i < w.children.length; i++) {
            Control child = children[i];
            int index = 0;
            int count = w.getItemCount();
            while (index < count) {
                if (((DartTabItem) w.items[index].getImpl()).control == child)
                    break;
                index++;
            }
            if (index == count) {
                Rectangle rect = child.getBounds();
                width = Math.max(width, rect.x + rect.width);
                height = Math.max(height, rect.y + rect.height);
            } else {
                Point size = child.computeSize(wHint, hHint, flushCache);
                width = Math.max(width, size.x);
                height = Math.max(height, size.y);
            }
        }
        if (width == 0)
            width = DartWidget.DEFAULT_WIDTH;
        if (height == 0)
            height = DartWidget.DEFAULT_HEIGHT;
        if (wHint != SWT.DEFAULT)
            width = wHint;
        if (hHint != SWT.DEFAULT)
            height = hHint;
        // The loop above measures the tab CONTENT. A folder also has to make room for the tab strip
        // it renders above that content, or it is laid out exactly as tall as its content and the
        // renderer clips the bottom of the content away.
        Rectangle trim = w.getApi().computeTrim(0, 0, width, height);
        return new Point(trim.width, trim.height);
    }

    public static Point computeSize(DartToolBar c, int wHint, int hHint, boolean changed) {
        ToolItem[] items = c.getItems();
        int itemCount = items.length;

        if (itemCount == 0) {
            return new Point(DartToolItem.DEFAULT_WIDTH, DartToolItem.DEFAULT_HEIGHT);
        }

        // A wrapping bar is measured in the runs its items wrap into at the hinted extent, as native does.
        boolean isVertical = (c.getApi().style & SWT.VERTICAL) != 0;
        int wrapAt = isVertical ? hHint : wHint;
        if ((c.getApi().style & SWT.WRAP) != 0 && wrapAt != SWT.DEFAULT) {
            Point[] sizes = new Point[itemCount];
            int across = 0;
            for (int i = 0; i < itemCount; i++) {
                sizes[i] = computeSize((DartToolItem) items[i].getImpl());
                across = Math.max(across, isVertical ? sizes[i].x : sizes[i].y);
            }
            // As the layout wraps: any item but the first that would pass the limit starts a run 2px on.
            int runs = 1, along = 0, maxAlong = 0;
            for (int i = 0; i < itemCount; i++) {
                int extent = isVertical ? sizes[i].y : sizes[i].x;
                if (i != 0 && along + extent > wrapAt) {
                    runs++;
                    along = 0;
                }
                along += extent;
                maxAlong = Math.max(maxAlong, along);
            }
            int acrossTotal = runs * across + (runs - 1) * 2;
            int width = isVertical ? acrossTotal : maxAlong;
            int height = isVertical ? maxAlong : acrossTotal;
            return new Point(wHint != SWT.DEFAULT ? wHint : width, hHint != SWT.DEFAULT ? hHint : height);
        }

        int totalWidth = 0;
        int totalHeight = 0;
        int maxWidth = 0;
        int maxHeight = 0;

        for (ToolItem item : items) {
            DartToolItem dartItem = (DartToolItem) item.getImpl();
            Point itemSize = computeSize(dartItem);

            totalWidth += itemSize.x;
            totalHeight += itemSize.y;
            maxWidth = Math.max(maxWidth, itemSize.x);
            maxHeight = Math.max(maxHeight, itemSize.y);
        }

        int width, height;
        if (isVertical) {
            // Vertical toolbar: width = max item width, height = sum of heights
            width = maxWidth;
            height = totalHeight;
        } else {
            // Horizontal toolbar: width = sum of widths, height = max item height
            width = totalWidth;
            height = maxHeight;
        }

        // Apply hints if provided
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }

        return new Point(width, height);
    }

    /**
     * What the platform measures around a ToolItem's content. Asymmetric -- it is not one inset
     * applied twice -- and read off native Win32 rather than chosen: an image of any size comes back
     * that much wider and that much taller.
     */
    private static final int TOOL_ITEM_PAD_W = 7;

    private static final int TOOL_ITEM_PAD_H = 6;

    /** The room a drop-down's arrow takes beyond the item's own padded width. Size-independent. */
    private static final int TOOL_ITEM_ARROW = 15;

    /** A separator's thickness across the bar when the application set no width of its own. */
    private static final int TOOL_ITEM_SEPARATOR = 8;

    public static Point computeSize(DartToolItem w) {
        int width = 0, height = 0;
        boolean isSeparator = (w.getApi().style & SWT.SEPARATOR) != 0;
        boolean hasText = w.text != null && w.text.length() != 0;
        boolean hasImage = w.image != null && !w.image.isDisposed();

        if (isSeparator) {
            // A separator is thick across the bar and thin along it. In a vertical bar it claims no
            // width at all: claiming a full item's width would widen the whole bar, which is as wide
            // as its widest item.
            if ((w.parent.style & SWT.HORIZONTAL) != 0) {
                width = w.getWidth();
                if (width <= 0)
                    width = TOOL_ITEM_SEPARATOR;
                height = DartToolItem.DEFAULT_HEIGHT;
            } else {
                width = Math.max(0, w.getWidth());
                height = TOOL_ITEM_SEPARATOR;
            }
            if (w.control != null) {
                height = Math.max(height, 0);
            }
        } else if (hasText && !hasImage) {
            // Measured the way the render side draws it, so the bar holds the text at the theme's size.
            return ToolItemSizes.computeSize(w, SWT.DEFAULT, SWT.DEFAULT, true);
        } else {
            if (hasImage) {
                int imageWidth, imageHeight;
                // A folder's own buttons and its top-right bar are not enlarged, so they fit its tab row.
                if (Config.getConfigFlags().bigger_icon_size && !CTabFolderHelper.keepsImageSize(w.parent)) {
                    imageWidth = imageHeight = (int) Math.round(ToolItemSizes.ICON_SIZE);
                } else {
                    Rectangle imageBounds = w.image.getBounds();
                    imageWidth = imageBounds.width;
                    imageHeight = imageBounds.height;
                }

                if (hasText) {
                    Point textSize = toolItemTextSize(w);
                    if ((w.parent.style & SWT.RIGHT) != 0) {
                        width = imageWidth + DartToolItem.INSET + textSize.x;
                        height = Math.max(imageHeight, textSize.y);
                    } else {
                        width = Math.max(imageWidth, textSize.x);
                        height = imageHeight + DartToolItem.INSET + textSize.y;
                    }
                } else {
                    width = imageWidth;
                    height = imageHeight;
                }

                // No minimum is applied here: an item that has content is measured from it, and a
                // small icon legitimately yields an item smaller than the one an empty item gets.
                width += TOOL_ITEM_PAD_W;
                height += TOOL_ITEM_PAD_H;
            } else {
                width = DartToolItem.DEFAULT_WIDTH;
                height = DartToolItem.DEFAULT_HEIGHT;
            }

            if ((w.getApi().style & SWT.DROP_DOWN) != 0) {
                width += TOOL_ITEM_ARROW;
            }
        }
        return new Point(width, height);
    }

    /** The item's text in the font {@link ToolItemSizes} measures a text-only item with. */
    private static Point toolItemTextSize(DartToolItem w) {
        DartControl bar = (DartControl) w.parent.getImpl();
        TextStyle style = Config.getConfigFlags().use_swt_fonts && bar.getExplicitFont() != null
                ? TextStyle.from(bar.getFont())
                : ToolItemTheme.get().textStyle().withStyleFrom(bar.getFont());
        PointD size = FontMetricsUtil.getFontSize(w.text, style);
        return new Point((int) Math.ceil(size.x()), (int) Math.ceil(size.y()));
    }

    public static Point computeSize(DartTree t, int wHint, int hHint, boolean changed) {
        return TreeSizes.computeSize(t, wHint, hHint, changed);
    }

    public static Point computeSize(DartText c, int wHint, int hHint, boolean changed) {
        return TextSizes.computeSize(c, wHint, hHint, changed);
    }

    private static final int GROUP_BORDER_WIDTH = 1;
    private static final int GROUP_MARGIN = 8;
    private static final int GROUP_TITLE_PADDING = 4;
    private static final int GROUP_BORDER = GROUP_BORDER_WIDTH + GROUP_MARGIN;

    private static int getGroupTitleHeight(DartGroup widget) {
        String text = widget._text();
        if (!text.isEmpty()) {
            Point textSize = GCHelper.textExtent(text, SWT.DRAW_MNEMONIC | SWT.DRAW_DELIMITER, widget.getFont());
            return textSize.y + GROUP_TITLE_PADDING * 2;
        }
        return 0;
    }

    public static Rectangle computeTrim(DartGroup widget, int x, int y, int width, int height) {
        int titleHeight = getGroupTitleHeight(widget);
        int topMargin = Math.max(GROUP_BORDER, titleHeight);

        return new Rectangle(
            x - GROUP_BORDER,
            y - topMargin,
            width + GROUP_BORDER * 2,
            height + topMargin + GROUP_TITLE_PADDING * 2
        );
    }

    public static Rectangle computeTrim(DartScrollable widget, int x, int y, int width, int height) {
        // Same as getClientArea(DartScrollable): a CoolBar only ever reaches the inherited method.
        if (widget instanceof DartCoolBar) return computeTrim((DartCoolBar) widget, x, y, width, height);
        if (CompositeHelper.framesItsContent(widget)) return CompositeHelper.computeTrim((DartComposite) widget, x, y, width, height);
        int menuBar = menuStripOutsideClientArea(widget);
        return new Rectangle(x, y - menuBar, width, height + menuBar);
    }

    /** A Decorations that draws its own menu strip has it outside its client area, as a native one does. */
    private static int menuStripOutsideClientArea(DartScrollable widget) {
        Scrollable api = widget.getApi();
        return api instanceof Decorations ? ownMenuStripHeight((Decorations) api) : 0;
    }

    /**
     * Height of the menu strip a Decorations draws above its client area, as a native menu bar is
     * drawn: only when its menu bar has something in it and no visible main toolbar hosts it. The
     * client draws the strip on this answer, which it receives as {@code ownMenuStrip}.
     */
    public static int ownMenuStripHeight(Decorations decorations) {
        if (!DartMainToolbar.hasMenuBarItems(decorations)) return 0;
        Menu bar = decorations.getMenuBar();
        if (bar == null || bar.isDisposed()) return 0;
        for (Control child : decorations.getChildren()) {
            if (child.getImpl() instanceof DartMainToolbar && child.getVisible()) return 0;
        }
        return menuStripHeight();
    }

    /**
     * The menu strip's height in layout units, wherever it is drawn. The zoom leaves its text at its
     * true size, so the strip is sized at the monitor's scale, as a native menu bar is: it grows by
     * what the zoom takes off. The client draws it at this height, received as {@code menuStripHeight}.
     */
    public static int menuStripHeight() {
        return Config.logicalToSwt(MenuSizes.MENU_BAR_HEIGHT);
    }

    /**
     * Follows a change that may have given a Dart-backed Decorations its own menu strip or taken it
     * away, given its {@link #ownMenuStripHeight} from before the change. The strip is re-sent, and
     * the client area it comes out of is resized and laid out again, as a native menu bar's
     * appearing does.
     */
    static void menuStripChanged(Decorations decorations, int before) {
        if (decorations == null || decorations.isDisposed()) return;
        if (!(decorations.getImpl() instanceof DartComposite)) return;
        if (ownMenuStripHeight(decorations) == before) return;
        DartComposite impl = (DartComposite) decorations.getImpl();
        markOwnMenuStrip(impl.getValue());
        impl.sendEvent(SWT.Resize);
        if (decorations.isDisposed()) return;
        if (impl.layout != null) {
            impl.markLayout(false, false);
            impl.updateLayout(false);
        }
    }

    /**
     * Re-sends whether a Dart-backed Decorations draws its own menu strip; an unchanged answer is not
     * sent. Without menu items there is no strip for a main toolbar's coming or going to move.
     */
    static void resendMenuStrip(Decorations decorations) {
        if (!DartMainToolbar.hasMenuBarItems(decorations)) return;
        if (decorations.getImpl() instanceof DartWidget)
            markOwnMenuStrip(((DartWidget) decorations.getImpl()).getValue());
    }

    /** {@code ownMenuStrip}'s number; only the desktop and web Decorations have it, so it is looked up. */
    private static final int OWN_MENU_STRIP =
            java.util.Arrays.asList(dev.equo.swt.WireTable.PROPERTIES).indexOf("Decorations.ownMenuStrip");

    /** Marks {@code ownMenuStrip} changed on a value that has it: only the desktop and web Decorations draw their own strip. */
    private static void markOwnMenuStrip(VWidget value) {
        for (int key : value.wireKeys()) {
            if (key == OWN_MENU_STRIP) {
                value.markDirty(key);
                return;
            }
        }
    }

    /** Where the text of the first cell is: after the image, as wide as the text, and no wider than the cell. */
    public static Rectangle getBounds(DartTableItem item) {
        Table parent = item._parent();
        if (parent.indexOf(item.getApi()) == -1)
            return new Rectangle(0, 0, 0, 0);
        Rectangle text = textCell(item, 0);
        int width = Math.min(dev.equo.swt.size.TableSizes.getTextWidth(item.getApi()), text.width);
        return new Rectangle(text.x, text.y, width, text.height);
    }

    public static Rectangle getBounds(DartTableItem item, int index) {
        Table parent = item._parent();
        DartTable dartTable = (DartTable) parent.getImpl();
        int columnCount = dartTable.columnCount;
        if (!(0 <= index && index < Math.max(1, columnCount)))
            return new Rectangle(0, 0, 0, 0);
        int itemIndex = parent.indexOf(item.getApi());
        if (itemIndex == -1)
            return new Rectangle(0, 0, 0, 0);
        int itemHeight = dartTable.getItemHeight();
        if (itemHeight <= 0)
            itemHeight = 20;
        int headerHeight = dartTable.getHeaderHeight();
        // The row index counts from the top of the model; take off however far Flutter has scrolled.
        int y = headerHeight + (itemIndex * itemHeight) - TableHelper.scrollOffsetY(dartTable);
        // The first cell's content starts after its inset (and checkbox); the others fill their column.
        int inset = dev.equo.swt.size.TableSizes.getLeadingInset(dartTable);
        // Without columns the one cell is as wide as the widest row; a backend that keeps no such width
        // (the embedded Win32 and GTK ones) gives each row its own.
        if (columnCount == 0) {
            int width = dartTable.columnlessWidth > 0 ? dartTable.columnlessWidth : TableHelper.contentWidth(item.getApi());
            return new Rectangle(inset, y, width, itemHeight);
        }
        TableColumn[] columns = dartTable.getColumns();
        // In the table's client area: a table scrolled sideways moves its cells left by that much.
        int x = tableColumnLeft(dartTable, index) - TableHelper.scrollOffsetX(dartTable);
        int width = index < columns.length ? columns[index].getWidth() : 0;
        if (index == 0)
            return new Rectangle(x + inset, y, Math.max(0, width - inset), itemHeight);
        return new Rectangle(x, y, width, itemHeight);
    }

    /** Where column {@code index} of a table starts: after the columns shown before it in the table's order. */
    static int tableColumnLeft(DartTable table, int index) {
        TableColumn[] columns = table.getColumns();
        int[] widths = new int[columns.length];
        for (int i = 0; i < columns.length; i++)
            widths[i] = columns[i].getWidth();
        return ColumnHelper.positions(widths, table.getColumnOrder())[index];
    }

    /** Where a cell's image is drawn: at the start of the cell, on the item's row, in the icon's square. */
    public static Rectangle getImageBounds(DartTableItem item, int index) {
        Rectangle cell = tableContentCell(item, index, getBounds(item, index));
        if (cell.width == 0 && cell.height == 0)
            return cell;
        Image[] images = item._images();
        Image image = index == 0 ? item.getImage() : (images != null && index < images.length) ? images[index] : null;
        int width = image != null ? dev.equo.swt.size.TableSizes.getIconSize(item.getApi()) : 0;
        return new Rectangle(cell.x, cell.y, width, cell.height);
    }

    public static Rectangle getTextBounds(DartTableItem item, int index) {
        DartTable dartTable = (DartTable) item._parent().getImpl();
        if (!(0 <= index && index < Math.max(1, dartTable.columnCount)))
            return new Rectangle(0, 0, 0, 0);
        return centredTextLine(textCell(item, index), item.getFont(index));
    }

    /** The part of a cell its text takes, after the image, on the whole row. */
    private static Rectangle textCell(DartTableItem item, int index) {
        Rectangle cellBounds = tableContentCell(item, index, getBounds(item, index));
        Image[] images = item._images();
        Image image = index == 0 ? item.getImage() : (images != null && index < images.length) ? images[index] : null;
        int shift = image != null ? dev.equo.swt.size.TableSizes.getIconSpace(item.getApi()) : 0;
        return new Rectangle(cellBounds.x + shift, cellBounds.y, Math.max(0, cellBounds.width - shift), cellBounds.height);
    }

    /** A column's cell past the padding Flutter starts its content at; the first cell's bounds include it already. */
    private static Rectangle tableContentCell(DartTableItem item, int index, Rectangle cell) {
        if (index == 0 || ((DartTable) item._parent().getImpl()).columnCount == 0) return cell;
        return insetCell(cell, dev.equo.swt.size.TableSizes.getCellInset());
    }

    private static Rectangle insetCell(Rectangle cell, int inset) {
        int shift = Math.min(cell.width, inset);
        return new Rectangle(cell.x + shift, cell.y, cell.width - shift, cell.height);
    }

    /**
     * A line of {@code font} as the GC draws it, centred in {@code cell} as Flutter centres a cell's
     * text: where an owner-drawing listener draws its text through a GC in that font.
     */
    private static Rectangle centredTextLine(Rectangle cell, Font font) {
        int line = Math.min(cell.height, GCHelper.textExtent("Ag", 0, font).y);
        return new Rectangle(cell.x, cell.y + (cell.height - line) / 2, cell.width, line);
    }

    public static List<TreeItem> flattenVisibleTreeItems(DartTree tree) {
        return TreeHelper.visibleItems(tree);
    }

    /** The row an item is shown in, in viewport coordinates; null when a collapsed ancestor hides it. */
    private static Rectangle treeRow(DartTreeItem item) {
        DartTree dartTree = (DartTree) item._parent().getImpl();
        int itemHeight = dartTree.getItemHeight();
        if (itemHeight <= 0) itemHeight = 20;
        int rowIndex = TreeHelper.visibleRow(dartTree, item.getApi());
        if (rowIndex == -1) return null;
        // The row index counts from the top of the model; take off however far Flutter has scrolled.
        int y = dartTree.getHeaderHeight() + rowIndex * itemHeight - TreeHelper.scrollOffsetY(dartTree);
        Rectangle parentBounds = item._parent().getBounds();
        return new Rectangle(0, y, parentBounds != null ? parentBounds.width : 100, itemHeight);
    }

    /**
     * Where cell {@code index}'s content starts and how wide it may be: the first cell after its indent.
     * In the tree's client area, so a tree scrolled sideways moves its cells left by that much.
     */
    private static Rectangle treeCell(DartTreeItem item, int index, Rectangle row) {
        DartTree dartTree = (DartTree) item._parent().getImpl();
        int scrolled = TreeHelper.scrollOffsetX(dartTree);
        int left = dev.equo.swt.size.TreeSizes.getContentLeft(dartTree, item.getApi()) - scrolled;
        TreeColumn[] columns = dartTree.columnCount > 0 ? dartTree.columns : null;
        if (columns == null) {
            Image image = item.getImage();
            int width = (image != null ? dev.equo.swt.size.TreeSizes.getIconSpace() : 0)
                    + dev.equo.swt.size.TreeSizes.getTextWidth(item.getApi(), 0);
            return new Rectangle(left, row.y, width, row.height);
        }
        // Flutter starts a row's cells after the tree's edge gap and the row's padding, each where the
        // column order shows its column.
        int x = dev.equo.swt.size.TreeSizes.getCellsLeft(dartTree) - treeColumnLeft(dartTree, 0) + treeColumnLeft(dartTree, index) - scrolled;
        int width = index < columns.length ? columns[index].getWidth() : 0;
        if (index == 0) return new Rectangle(left, row.y, Math.max(0, x + width - left), row.height);
        return new Rectangle(x, row.y, width, row.height);
    }

    /** Where column {@code index} of a tree starts: after the columns shown before it in the tree's order; 0 without columns. */
    public static int treeColumnLeft(DartTree tree, int index) {
        int count = tree.columnCount;
        if (index >= count) return 0;
        int[] widths = new int[count];
        for (int i = 0; i < count; i++) widths[i] = tree.columns[i].getWidth();
        return ColumnHelper.positions(widths, tree.getColumnOrder())[index];
    }

    private static boolean isTreeCell(DartTreeItem item, int index) {
        return 0 <= index && index < Math.max(1, ((DartTree) item._parent().getImpl()).columnCount);
    }

    /** The first cell's text, as SWT reports an item's bounds. */
    public static Rectangle getBounds(DartTreeItem item) {
        return textCell(item, 0);
    }

    public static Rectangle getBounds(DartTreeItem item, int index) {
        Rectangle row = isTreeCell(item, index) ? treeRow(item) : null;
        return row == null ? new Rectangle(0, 0, 0, 0) : treeCell(item, index, row);
    }

    public static Rectangle getImageBounds(DartTreeItem item, int index) {
        Rectangle row = isTreeCell(item, index) ? treeRow(item) : null;
        if (row == null) return new Rectangle(0, 0, 0, 0);
        Rectangle cell = contentCell(item, index, treeCell(item, index, row));
        Image image = index == 0 ? item.getImage() : item.getImage(index);
        int width = image != null ? dev.equo.swt.size.TreeSizes.getIconSize() : 0;
        return new Rectangle(cell.x, cell.y, width, cell.height);
    }

    /**
     * A column's cell past the inset Flutter starts its content at; the first cell starts after its indent already.
     * Callers have checked {@code index} with isTreeCell, so a non-zero index is a live column.
     */
    private static Rectangle contentCell(DartTreeItem item, int index, Rectangle cell) {
        if (index == 0) return cell;
        DartTree tree = (DartTree) item._parent().getImpl();
        return insetCell(cell, dev.equo.swt.size.TreeSizes.getCellInset(tree.columns[index].getAlignment()));
    }

    /** A line of the item's font, centred in the row as Flutter centres its own text. */
    public static Rectangle getTextBounds(DartTreeItem item, int index) {
        Rectangle cell = textCell(item, index);
        if (cell.height == 0) return cell;
        return centredTextLine(cell, item.getFont(index));
    }

    /** A cell's text area: the cell after its image, the full height of the row. */
    private static Rectangle textCell(DartTreeItem item, int index) {
        Rectangle row = isTreeCell(item, index) ? treeRow(item) : null;
        if (row == null) return new Rectangle(0, 0, 0, 0);
        Rectangle cell = contentCell(item, index, treeCell(item, index, row));
        Image image = index == 0 ? item.getImage() : item.getImage(index);
        int shift = image != null ? dev.equo.swt.size.TreeSizes.getIconSpace() : 0;
        int width = ((DartTree) item._parent().getImpl()).columnCount > 0
                ? Math.max(0, cell.width - shift)
                : dev.equo.swt.size.TreeSizes.getTextWidth(item.getApi(), index);
        return new Rectangle(cell.x + shift, cell.y, width, cell.height);
    }

    public static Rectangle getBounds(DartToolItem item) {
        Point size = computeSize(item);
        ToolBar toolbar = item.parent;
        int x = 0, y = 0;
        if (toolbar != null) {
            boolean isVertical = (toolbar.getStyle() & SWT.VERTICAL) != 0;
            Composite container = toolbar.getParent();
            if (container != null) {
                Rectangle toolbarBounds = toolbar.getBounds();
                if (isVertical) {
                    size.x = Math.min(size.x, Math.max(1, container.getBounds().width - toolbarBounds.x));
                } else {
                    size.y = Math.min(size.y, Math.max(1, container.getBounds().height - toolbarBounds.y));
                }
            }
            // An item sits at the running total of the items before it -- the same run
            // computeSize(DartToolBar) sums the bar's own extent from.
            for (ToolItem sibling : toolbar.getItems()) {
                if (sibling == item.getApi())
                    break;
                if (!(sibling.getImpl() instanceof DartToolItem))
                    continue;
                Point siblingSize = computeSize((DartToolItem) sibling.getImpl());
                if (isVertical) {
                    y += siblingSize.y;
                } else {
                    x += siblingSize.x;
                }
            }
            // The client centres a single run of items across a bar taller than they are.
            if (!isVertical) {
                int barHeight = toolbar.getBounds().height;
                if (barHeight > size.y)
                    y = (barHeight - size.y) / 2;
            }
        }
        return new Rectangle(x, y, size.x, size.y);
    }

    public static Rectangle getClientArea(DartCoolBar widget) {
        return CoolBarSizes.getClientArea(widget.getBounds());
    }

    public static Rectangle computeTrim(DartCoolBar widget, int x, int y, int width, int height) {
        return CoolBarSizes.computeTrim(x, y, width, height);
    }

    public static Rectangle getClientArea(DartCoolItem widget) {
        Rectangle b = widget.getBounds();
        return new Rectangle(0, 28, b.width, b.height-28);
    }

    /**
     * The frame a CTabFolder draws around its page, on the three edges the tab strip does not take.
     * A folder that hands its page the full width lays it — and everything the page positions from
     * its own client area — that much too far left.
     */
    private static int cTabFolderBodyBorder(DartCTabFolder widget) {
        return (widget.getApi().getStyle() & SWT.BORDER) != 0
                ? CTabFolderSizes.BODY_BORDER_STYLED
                : CTabFolderSizes.BODY_BORDER;
    }

    /**
     * The tab height upstream computes for the default font. Tabs keep it while no height is fixed,
     * so a point taken from a tab's bounds lands where it would natively, although the strip drawn
     * around them is taller.
     */
    private static final int CTABFOLDER_DEFAULT_TAB_HEIGHT = 25;

    /**
     * Height of a tab, which places the tabs and decides what a point on them hits. {@code setTabHeight}
     * fixes it as natively: {@code h + 1} (the line across the top of the tabs), and 0 for 0. With no
     * fixed height, a tab image taller than the drawn icon grows it by the difference.
     */
    public static int cTabFolderTabHeight(DartCTabFolder widget) {
        int fixed = widget._fixedTabHeight();
        if (fixed == SWT.DEFAULT) return CTABFOLDER_DEFAULT_TAB_HEIGHT + CTabFolderHelper.imageGrowth(widget);
        return fixed == 0 ? 0 : fixed + 1;
    }

    /**
     * Height of the strip the render side draws and the page is laid out below: the measured
     * default grown by a tall tab image, or the tab height {@code setTabHeight} fixed. The render
     * side receives it and draws exactly this.
     */
    public static int cTabFolderTabStripHeight(DartCTabFolder widget) {
        return widget._fixedTabHeight() == SWT.DEFAULT
                ? CTabFolderSizes.TAB_STRIP_HEIGHT + CTabFolderHelper.imageGrowth(widget)
                : cTabFolderTabHeight(widget);
    }

    /**
     * The frame around the page. With no tab strip the folder draws none, so the page fills it.
     * Read from the strip height {@code updateTabHeight} stores, so the deferred update sees the
     * client area move and lays the page out again.
     */
    private static int cTabFolderFrame(DartCTabFolder widget) {
        return widget._displayedTabHeight() == 0 ? 0 : cTabFolderBodyBorder(widget);
    }

    public static Rectangle getClientArea(DartCTabFolder widget) {
        Rectangle b = widget.getBounds();
        int border = cTabFolderFrame(widget);
        boolean onBottom = widget.getTabPosition() == SWT.BOTTOM;
        int tabs = widget._displayedTabHeight() + CTabFolderHelper.wrappedHeight(widget, new Point(b.width, b.height));
        int top = onBottom ? border : tabs;
        int bottom = onBottom ? tabs : border;
        if (widget._minimized()) return new Rectangle(border, top, 0, 0);
        return new Rectangle(border, top,
                Math.max(0, b.width - border * 2),
                Math.max(0, b.height - top - bottom));
    }

    // Inverse of getClientArea(DartCTabFolder): adds the tab strip and the frame back on.
    public static Rectangle computeTrim(DartCTabFolder widget, int x, int y, int width, int height) {
        int border = cTabFolderFrame(widget);
        boolean onBottom = widget.getTabPosition() == SWT.BOTTOM;
        int tabs = widget._displayedTabHeight() + CTabFolderHelper.wrappedHeight(widget, new Point(width, height));
        int top = onBottom ? border : tabs;
        int bottom = onBottom ? tabs : border;
        return new Rectangle(x - border, y - top,
                width + border * 2, height + top + bottom);
    }

    /** The page gets the folder less the tab strip on one edge and the body's frame on the other three. */
    public static Rectangle getClientArea(DartTabFolder widget) {
        Rectangle b = widget.getBounds();
        int border = TabFolderTheme.get().bodyBorder();
        int strip = TabFolderHelper.stripHeight(widget.getApi());
        return new Rectangle(border, TabFolderHelper.pageTop(widget),
                Math.max(0, b.width - 2 * border), Math.max(0, b.height - strip - border));
    }

    public static Rectangle computeTrim(DartTabFolder widget, int x, int y, int width, int height) {
        int border = TabFolderTheme.get().bodyBorder();
        int strip = TabFolderHelper.stripHeight(widget.getApi());
        return new Rectangle(x - border, y - TabFolderHelper.pageTop(widget),
                width + 2 * border, height + strip + border);
    }

    public static Rectangle getClientArea(DartGroup widget) {
        Rectangle b = widget.getBounds();
        int titleHeight = getGroupTitleHeight(widget);
        int topMargin = Math.max(GROUP_BORDER, titleHeight);
        int yOffset = titleHeight > 0 ? 0 : GROUP_BORDER;
        return new Rectangle(
            GROUP_BORDER,
            yOffset,
            Math.max(0, b.width - GROUP_BORDER * 2),
            Math.max(0, b.height - topMargin - GROUP_TITLE_PADDING * 2)
        );
    }

    public static final int SCROLL_BAR_SIZE = 17;

    public static Rectangle getClientArea(DartScrollable widget) {
        // A CoolBar inherits this method rather than declaring its own, so the call that reaches
        // here was resolved against DartScrollable and has to be handed to the bar's overload.
        if (widget instanceof DartCoolBar) return getClientArea((DartCoolBar) widget);
        if (CompositeHelper.framesItsContent(widget)) return CompositeHelper.getClientArea((DartComposite) widget);
        Rectangle b = widget.getBounds();
        int vBarWidth = takesRoom(widget.verticalBar) ? SCROLL_BAR_SIZE : 0;
        int hBarHeight = takesRoom(widget.horizontalBar) ? SCROLL_BAR_SIZE : 0;
        int menuBar = menuStripOutsideClientArea(widget);
        return new Rectangle(0, 0, Math.max(0, b.width - vBarWidth), Math.max(0, b.height - hBarHeight - menuBar));
    }

    /**
     * Whether a scroll bar takes room from its parent's client area. The client draws a visible bar
     * only while it has a range to scroll, and the content fills the place of one it does not draw.
     */
    static boolean takesRoom(ScrollBar bar) {
        if (bar == null || !bar.isVisible()) return false;
        Object impl = bar.getImpl();
        return !(impl instanceof DartScrollBar) || ((DartScrollBar) impl).hasRange;
    }

    public static Point getSize(DartScrollBar scrollBar) {
        // Win32 measures a bar against its parent's client rect: a horizontal bar spans the
        // client width and is SM_CYHSCROLL tall, a vertical one is the transpose. Callers only
        // read the thickness (ScrolledCompositeLayout takes .y, Forms' SharedScrolledComposite
        // takes .x) but they dereference the result, so this must never return null.
        Scrollable parent = scrollBar.parent;
        Rectangle client = (parent != null && !parent.isDisposed())
            ? parent.getClientArea()
            : new Rectangle(0, 0, 0, 0);
        if ((scrollBar.getApi().style & SWT.HORIZONTAL) != 0) {
            return new Point(Math.max(0, client.width), SCROLL_BAR_SIZE);
        }
        return new Point(SCROLL_BAR_SIZE, Math.max(0, client.height));
    }

    public static Point minimumSize(DartComposite composite, int wHint, int hHint, boolean changed) {
        // Through the API object: a subclass may override getClientArea to reserve room it paints itself.
        Rectangle clientArea = composite.getApi().getClientArea();
        int width = 0, height = 0;
        for (Control child : composite._getChildren()) {
            Rectangle rect = child.getBounds();
            width = Math.max(width, rect.x - clientArea.x + rect.width);
            height = Math.max(height, rect.y - clientArea.y + rect.height);
        }
        return new Point(width, height);
    }

    public static Point minimumSize(DartTabFolder w, int wHint, int hHint, boolean flushCache) {
        Control[] children = w._getChildren();
        int width = 0, height = 0;
        for (int i = 0; i < children.length; i++) {
            Control child = children[i];
            int index = 0;
            int count = w.getItemCount();
            while (index < count) {
                if (((DartTabItem) w.items[index].getImpl()).control == child)
                    break;
                index++;
            }
            if (index == count) {
                Rectangle rect = child.getBounds();
                width = Math.max(width, rect.x + rect.width);
                height = Math.max(height, rect.y + rect.height);
            } else {
                Point size = child.computeSize(wHint, hHint, flushCache);
                width = Math.max(width, size.x);
                height = Math.max(height, size.y);
            }
        }
        return new Point(width, height);
    }

}