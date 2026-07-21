/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 455263
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

/**
 * Instances of this class provide all of the measuring and drawing functionality
 * required by <code>CTabFolder</code>. This class can be subclassed in order to
 * customize the look of a CTabFolder.
 *
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 * @since 3.6
 */
public class DartCTabFolderRenderer implements ICTabFolderRenderer {

    Color fillColor;

    private Font chevronFont = null;

    static final RGB CLOSE_FILL = new RGB(240, 64, 64);

    static final int BUTTON_SIZE = 16;

    static final int BUTTON_TRIM = 1;

    static final int BUTTON_BORDER = SWT.COLOR_WIDGET_DARK_SHADOW;

    static final int BUTTON_FILL = SWT.COLOR_LIST_BACKGROUND;

    static final int BORDER1_COLOR = SWT.COLOR_WIDGET_NORMAL_SHADOW;

    static final int ITEM_TOP_MARGIN = 3;

    static final int ITEM_BOTTOM_MARGIN = 3;

    static final int ITEM_LEFT_MARGIN = 4;

    static final int ITEM_RIGHT_MARGIN = 4;

    static final int INTERNAL_SPACING = 4;

    static final int TABS_WITHOUT_ICONS_PADDING = 14;

    static final int FLAGS = SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC | SWT.DRAW_DELIMITER;

    //$NON-NLS-1$
    static final String ELLIPSIS = "...";

    //$NON-NLS-1$
    private static final String CHEVRON_ELLIPSIS = "99+";

    private static final float CHEVRON_FONT_SIZE_FACTOR = 0.7f;

    private static final int CHEVRON_BOTTOM_INDENT = 4;

    //Part constants
    /**
     * Constructs a new instance of this class given its parent.
     *
     * @param parent CTabFolder
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
     * </ul>
     *
     * @see Widget#getStyle
     */
    protected DartCTabFolderRenderer(CTabFolder parent, CTabFolderRenderer api) {
        setApi(api);
        if (parent == null)
            return;
        if (parent.isDisposed())
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        this.getApi().parent = parent;
    }

    /**
     * Returns the preferred size of a part.
     * <p>
     * The <em>preferred size</em> of a part is the size that it would
     * best be displayed at. The width hint and height hint arguments
     * allow the caller to ask a control questions such as "Given a particular
     * width, how high does the part need to be to show all of the contents?"
     * To indicate that the caller does not wish to constrain a particular
     * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
     * </p><p>
     * The <code>part</code> value indicated what component the preferred size is
     * to be calculated for. Valid values are any of the part constants:
     * </p>
     * <ul>
     * <li>PART_BODY</li>
     * <li>PART_HEADER</li>
     * <li>PART_BORDER</li>
     * <li>PART_BACKGROUND</li>
     * <li>PART_MAX_BUTTON</li>
     * <li>PART_MIN_BUTTON</li>
     * <li>PART_CHEVRON_BUTTON</li>
     * <li>PART_CLOSE_BUTTON</li>
     * <li>A positive integer which is the index of an item in the CTabFolder.</li>
     * </ul>
     * <p>
     * The <code>state</code> parameter may be one of the following:
     * </p>
     * <ul>
     * <li>SWT.NONE</li>
     * <li>SWT.SELECTED - whether the part is selected</li>
     * </ul>
     * @param part a part constant
     * @param state current state
     * @param gc the gc to use for measuring
     * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
     * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
     * @return the preferred size of the part
     *
     * @since 3.6
     */
    public Point computeSize(int part, int state, GC gc, int wHint, int hHint) {
        return CTabFolderHelper.computeSize(this, (DartCTabFolder) getApi().parent.getImpl(), getApi().parent, part, state, gc, wHint, hHint);
    }

    private boolean shouldDrawCloseIcon(CTabItem item) {
        CTabFolder folder = item.getParent();
        boolean showClose = ((DartCTabFolder) folder.getImpl()).showClose || ((DartCTabItem) item.getImpl()).showClose;
        boolean isSelectedOrShowCloseForUnselected = (item.state & SWT.SELECTED) != 0 || ((DartCTabFolder) folder.getImpl()).showUnselectedClose;
        return showClose && isSelectedOrShowCloseForUnselected;
    }

    private boolean shouldDrawDirtyIndicator(CTabItem item) {
        CTabFolder folder = item.getParent();
        return ((DartCTabFolder) folder.getImpl()).dirtyIndicatorStyle && ((DartCTabItem) item.getImpl()).showDirty;
    }

    private boolean shouldAllocateCloseRect(CTabItem item) {
        return shouldDrawCloseIcon(item) || shouldDrawDirtyIndicator(item);
    }

    /**
     * Returns padding for the text of a tab item when showing images is disabled for the tab folder.
     */
    private int getLargeTextPadding(CTabItem item) {
        CTabFolder parent = item.getParent();
        String text = item.getText();
        if (text != null && parent.getMinimumCharacters() != 0) {
            return TABS_WITHOUT_ICONS_PADDING;
        }
        return 0;
    }

    private boolean shouldApplyLargeTextPadding(CTabFolder tabFolder) {
        return !((DartCTabFolder) tabFolder.getImpl()).showSelectedImage && !((DartCTabFolder) tabFolder.getImpl()).showUnselectedImage;
    }

    /**
     * Given a desired <em>client area</em> for the part
     * (as described by the arguments), returns the bounding
     * rectangle which would be required to produce that client
     * area.
     * <p>
     * In other words, it returns a rectangle such that, if the
     * part's bounds were set to that rectangle, the area
     * of the part which is capable of displaying data
     * (that is, not covered by the "trimmings") would be the
     * rectangle described by the arguments (relative to the
     * receiver's parent).
     * </p>
     *
     * @param part one of the part constants
     * @param state the state of the part
     * @param x the desired x coordinate of the client area
     * @param y the desired y coordinate of the client area
     * @param width the desired width of the client area
     * @param height the desired height of the client area
     * @return the required bounds to produce the given client area
     *
     * @see CTabFolderRenderer#computeSize(int, int, GC, int, int) valid part and state values
     *
     * @since 3.6
     */
    public Rectangle computeTrim(int part, int state, int x, int y, int width, int height) {
        int borderLeft = ((DartCTabFolder) getApi().parent.getImpl()).borderVisible ? 1 : 0;
        int borderRight = borderLeft;
        int borderTop = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? borderLeft : 0;
        int borderBottom = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? 0 : borderLeft;
        int tabHeight = ((DartCTabFolder) getApi().parent.getImpl()).tabHeight;
        switch(part) {
            case CTabFolderRenderer.PART_BODY:
                int style = getApi().parent.getStyle();
                int highlight_header = (style & SWT.FLAT) != 0 ? 1 : 3;
                int highlight_margin = (style & SWT.FLAT) != 0 ? 0 : 2;
                if (((DartCTabFolder) getApi().parent.getImpl()).fixedTabHeight == 0 && (style & SWT.FLAT) != 0 && (style & SWT.BORDER) == 0) {
                    highlight_header = 0;
                }
                int marginWidth = getApi().parent.marginWidth;
                int marginHeight = getApi().parent.marginHeight;
                x = x - marginWidth - highlight_margin - borderLeft;
                width = width + borderLeft + borderRight + 2 * marginWidth + 2 * highlight_margin;
                if (((DartCTabFolder) getApi().parent.getImpl()).minimized) {
                    y = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? y - borderTop : y - highlight_header - tabHeight - borderTop;
                    height = borderTop + borderBottom + tabHeight + highlight_header;
                } else {
                    y = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? y - marginHeight - highlight_margin - borderTop : y - marginHeight - highlight_header - tabHeight - borderTop;
                    height = height + borderTop + borderBottom + 2 * marginHeight + tabHeight + highlight_header + highlight_margin;
                }
                break;
            case CTabFolderRenderer.PART_HEADER:
                //no trim
                break;
            case CTabFolderRenderer.PART_MAX_BUTTON:
            case CTabFolderRenderer.PART_MIN_BUTTON:
            case CTabFolderRenderer.PART_CLOSE_BUTTON:
            case CTabFolderRenderer.PART_CHEVRON_BUTTON:
                x -= BUTTON_TRIM;
                y -= BUTTON_TRIM;
                width += BUTTON_TRIM * 2;
                height += BUTTON_TRIM * 2;
                break;
            case CTabFolderRenderer.PART_BORDER:
                x = x - borderLeft;
                width = width + borderLeft + borderRight;
                y = y - borderTop;
                height = height + borderTop + borderBottom;
                break;
            default:
                if (0 <= part && part < getApi().parent.getItemCount()) {
                    x = x - ITEM_LEFT_MARGIN;
                    width = width + ITEM_LEFT_MARGIN + ITEM_RIGHT_MARGIN;
                    y = y - ITEM_TOP_MARGIN;
                    height = height + ITEM_TOP_MARGIN + ITEM_BOTTOM_MARGIN;
                }
                break;
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Dispose of any operating system resources associated with
     * the renderer. Called by the CTabFolder parent upon receiving
     * the dispose event or when changing the renderer.
     *
     * @since 3.6
     */
    public void dispose() {
        fillColor = null;
        if (chevronFont != null) {
            chevronFont.dispose();
            chevronFont = null;
        }
    }

    /**
     * Draw a specified <code>part</code> of the CTabFolder using the provided <code>bounds</code> and <code>GC</code>.
     * <p>The valid CTabFolder <code>part</code> constants are:
     * </p>
     * <ul>
     * <li>PART_BODY - the entire body of the CTabFolder</li>
     * <li>PART_HEADER - the upper tab area of the CTabFolder</li>
     * <li>PART_BORDER - the border of the CTabFolder</li>
     * <li>PART_BACKGROUND - the background of the CTabFolder</li>
     * <li>PART_MAX_BUTTON</li>
     * <li>PART_MIN_BUTTON</li>
     * <li>PART_CHEVRON_BUTTON</li>
     * <li>PART_CLOSE_BUTTON</li>
     * <li>A positive integer which is the index of an item in the CTabFolder.</li>
     * </ul>
     * <p>
     * The <code>state</code> parameter may be a combination of:
     * </p>
     * <ul>
     * <li>SWT.BACKGROUND - whether the background should be drawn</li>
     * <li>SWT.FOREGROUND - whether the foreground should be drawn</li>
     * <li>SWT.SELECTED - whether the part is selected</li>
     * <li>SWT.HOT - whether the part is hot (i.e. mouse is over the part)</li>
     * </ul>
     *
     * @param part part to draw
     * @param state state of the part
     * @param bounds the bounds of the part
     * @param gc the gc to draw the part on
     *
     * @since 3.6
     */
    public void draw(int part, int state, Rectangle bounds, GC gc) {
        switch(part) {
            case CTabFolderRenderer.PART_BACKGROUND:
                this.drawBackground(gc, bounds, state);
                break;
            case CTabFolderRenderer.PART_BODY:
                drawBody(gc, bounds, state);
                break;
            case CTabFolderRenderer.PART_HEADER:
                drawTabArea(gc, bounds, state);
                break;
            case CTabFolderRenderer.PART_MAX_BUTTON:
                drawMaximize(gc, bounds, state);
                break;
            case CTabFolderRenderer.PART_MIN_BUTTON:
                drawMinimize(gc, bounds, state);
                break;
            case CTabFolderRenderer.PART_CHEVRON_BUTTON:
                drawChevron(gc, bounds, state);
                break;
            default:
                if (0 <= part && part < getApi().parent.getItemCount()) {
                    if (bounds.width == 0 || bounds.height == 0)
                        return;
                    if ((state & SWT.SELECTED) != 0) {
                        drawSelected(part, gc, bounds, state);
                    } else {
                        drawUnselected(part, gc, bounds, state);
                    }
                }
                break;
        }
    }

    void drawBackground(GC gc, Rectangle bounds, int state) {
        boolean selected = (state & SWT.SELECTED) != 0;
        Color defaultBackground = selected && ((DartCTabFolder) getApi().parent.getImpl()).shouldHighlight() ? ((DartCTabFolder) getApi().parent.getImpl()).selectionBackground : getApi().parent.getBackground();
        Image image = selected ? ((DartCTabFolder) getApi().parent.getImpl()).selectionBgImage : null;
        Color[] colors = selected & ((DartCTabFolder) getApi().parent.getImpl()).shouldHighlight() ? ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientColors : ((DartCTabFolder) getApi().parent.getImpl()).gradientColors;
        int[] percents = selected ? ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientPercents : ((DartCTabFolder) getApi().parent.getImpl()).gradientPercents;
        boolean vertical = selected ? ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientVertical : ((DartCTabFolder) getApi().parent.getImpl()).gradientVertical;
        drawBackground(gc, null, bounds.x, bounds.y, bounds.width, bounds.height, defaultBackground, image, colors, percents, vertical);
    }

    void drawBackground(GC gc, int[] shape, boolean selected) {
        Color defaultBackground = selected && ((DartCTabFolder) getApi().parent.getImpl()).shouldHighlight() ? ((DartCTabFolder) getApi().parent.getImpl()).selectionBackground : getApi().parent.getBackground();
        Image image = selected ? ((DartCTabFolder) getApi().parent.getImpl()).selectionBgImage : null;
        Color[] colors = selected && ((DartCTabFolder) getApi().parent.getImpl()).shouldHighlight() ? ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientColors : ((DartCTabFolder) getApi().parent.getImpl()).gradientColors;
        int[] percents = selected ? ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientPercents : ((DartCTabFolder) getApi().parent.getImpl()).gradientPercents;
        boolean vertical = selected ? ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientVertical : ((DartCTabFolder) getApi().parent.getImpl()).gradientVertical;
        Point size = getApi().parent.getSize();
        int width = size.x;
        int height = ((DartCTabFolder) getApi().parent.getImpl()).tabHeight + ((getApi().parent.getStyle() & SWT.FLAT) != 0 ? 1 : 3);
        int x = 0;
        int borderLeft = ((DartCTabFolder) getApi().parent.getImpl()).borderVisible ? 1 : 0;
        int borderTop = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? borderLeft : 0;
        int borderBottom = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? 0 : borderLeft;
        if (borderLeft > 0) {
            x += 1;
            width -= 2;
        }
        int y = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? size.y - borderBottom - height : borderTop;
        drawBackground(gc, shape, x, y, width, height, defaultBackground, image, colors, percents, vertical);
    }

    void drawBackground(GC gc, int[] shape, int x, int y, int width, int height, Color defaultBackground, Image image, Color[] colors, int[] percents, boolean vertical) {
        Region clipping = null, region = null;
        if (shape != null) {
            clipping = new Region();
            region = new Region();
            region.add(shape);
            region.intersect(clipping);
        }
        if (image != null) {
        } else if (colors != null) {
            // draw gradient
            if (colors.length == 1) {
            } else {
                if (vertical) {
                    if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
                        int pos = 0;
                        if (percents[percents.length - 1] < 100) {
                            pos = (100 - percents[percents.length - 1]) * height / 100;
                        }
                        Color lastColor = colors[colors.length - 1];
                        if (lastColor == null)
                            lastColor = defaultBackground;
                        for (int i = percents.length - 1; i >= 0; i--) {
                            lastColor = colors[i];
                            if (lastColor == null)
                                lastColor = defaultBackground;
                            int percentage = i > 0 ? percents[i] - percents[i - 1] : percents[i];
                            int gradientHeight = percentage * height / 100;
                            pos += gradientHeight;
                        }
                    } else {
                        Color lastColor = colors[0];
                        if (lastColor == null)
                            lastColor = defaultBackground;
                        int pos = 0;
                        for (int i = 0; i < percents.length; i++) {
                            lastColor = colors[i + 1];
                            if (lastColor == null)
                                lastColor = defaultBackground;
                            int percentage = i > 0 ? percents[i] - percents[i - 1] : percents[i];
                            int gradientHeight = percentage * height / 100;
                            pos += gradientHeight;
                        }
                        if (pos < height) {
                        }
                    }
                } else {
                    //horizontal gradient
                    y = 0;
                    height = getApi().parent.getSize().y;
                    Color lastColor = colors[0];
                    if (lastColor == null)
                        lastColor = defaultBackground;
                    int pos = 0;
                    for (int i = 0; i < percents.length; ++i) {
                        lastColor = colors[i + 1];
                        if (lastColor == null)
                            lastColor = defaultBackground;
                        int gradientWidth = (percents[i] * width / 100) - pos;
                        pos += gradientWidth;
                    }
                    if (pos < width) {
                    }
                }
            }
        } else {
            // draw a solid background using default background in shape
            if ((getApi().parent.getStyle() & SWT.NO_BACKGROUND) != 0 || !defaultBackground.equals(getApi().parent.getBackground())) {
            }
        }
        if (shape != null) {
            clipping.dispose();
            region.dispose();
        }
    }

    void drawBody(GC gc, Rectangle bounds, int state) {
        Point size = new Point(bounds.width, bounds.height);
        int selectedIndex = ((DartCTabFolder) getApi().parent.getImpl()).selectedIndex;
        int tabHeight = ((DartCTabFolder) getApi().parent.getImpl()).tabHeight;
        int borderLeft = ((DartCTabFolder) getApi().parent.getImpl()).borderVisible ? 1 : 0;
        int borderRight = borderLeft;
        int borderTop = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? borderLeft : 0;
        int borderBottom = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? 0 : borderLeft;
        int style = getApi().parent.getStyle();
        int highlight_header = (style & SWT.FLAT) != 0 ? 1 : 3;
        int highlight_margin = (style & SWT.FLAT) != 0 ? 0 : 2;
        // fill in body
        if (!((DartCTabFolder) getApi().parent.getImpl()).minimized) {
            int width = size.x - borderLeft - borderRight - 2 * highlight_margin;
            int height = size.y - borderTop - borderBottom - tabHeight - highlight_header - highlight_margin;
            // Draw highlight margin
            if (highlight_margin > 0) {
                int[] shape = null;
                if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
                    int x1 = borderLeft;
                    int y1 = borderTop;
                    int x2 = size.x - borderRight;
                    int y2 = size.y - borderBottom - tabHeight - highlight_header;
                    shape = new int[] { x1, y1, x2, y1, x2, y2, x2 - highlight_margin, y2, x2 - highlight_margin, y1 + highlight_margin, x1 + highlight_margin, y1 + highlight_margin, x1 + highlight_margin, y2, x1, y2 };
                } else {
                    int x1 = borderLeft;
                    int y1 = borderTop + tabHeight + highlight_header;
                    int x2 = size.x - borderRight;
                    int y2 = size.y - borderBottom;
                    shape = new int[] { x1, y1, x1 + highlight_margin, y1, x1 + highlight_margin, y2 - highlight_margin, x2 - highlight_margin, y2 - highlight_margin, x2 - highlight_margin, y1, x2, y1, x2, y2, x1, y2 };
                }
                // If horizontal gradient, show gradient across the whole area
                if (selectedIndex != -1 && ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientColors != null && ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientColors.length > 1 && !((DartCTabFolder) getApi().parent.getImpl()).selectionGradientVertical) {
                    drawBackground(gc, shape, true);
                } else if (selectedIndex == -1 && ((DartCTabFolder) getApi().parent.getImpl()).gradientColors != null && ((DartCTabFolder) getApi().parent.getImpl()).gradientColors.length > 1 && !((DartCTabFolder) getApi().parent.getImpl()).gradientVertical) {
                    drawBackground(gc, shape, false);
                } else {
                }
            }
            //Draw client area
            if ((getApi().parent.getStyle() & SWT.NO_BACKGROUND) != 0) {
                int marginWidth = getApi().parent.marginWidth;
                int marginHeight = getApi().parent.marginHeight;
                int yClient;
                if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
                    yClient = borderTop + highlight_margin + marginHeight;
                } else {
                    yClient = borderTop + tabHeight + highlight_header + marginHeight;
                }
            }
        } else {
            if ((getApi().parent.getStyle() & SWT.NO_BACKGROUND) != 0) {
                int height = borderTop + tabHeight + highlight_header + borderBottom;
                if (size.y > height) {
                }
            }
        }
        //draw 1 pixel border around outside
        if (borderLeft > 0) {
            int x1 = borderLeft - 1;
            int x2 = size.x - borderRight;
            int y1 = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? borderTop - 1 : borderTop + tabHeight;
            int y2 = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? size.y - tabHeight - borderBottom - 1 : size.y - borderBottom;
            if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
            } else {
            }
        }
    }

    void drawClose(GC gc, Rectangle closeRect, int closeImageState) {
        drawClose(gc, closeRect, closeImageState, false, false);
    }

    void drawClose(GC gc, Rectangle closeRect, int closeImageState, boolean showDirtyIndicator, boolean selected) {
        if (closeRect.width == 0 || closeRect.height == 0)
            return;
        // When dirty and not hovered/pressed, draw bullet instead of X
        if (showDirtyIndicator) {
            int maskedState = closeImageState & (SWT.HOT | SWT.SELECTED | SWT.BACKGROUND);
            if (maskedState != SWT.HOT && maskedState != SWT.SELECTED) {
                drawDirtyIndicator(gc, closeRect, selected);
                return;
            }
        }
        // draw X with length of this constant
        final int lineLength = 8;
        int x = closeRect.x + Math.max(1, (closeRect.width - lineLength) / 2);
        int y = closeRect.y + Math.max(1, (closeRect.height - lineLength) / 2);
        y += ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? -1 : 1;
        switch(closeImageState & (SWT.HOT | SWT.SELECTED | SWT.BACKGROUND)) {
            case SWT.NONE:
                {
                    drawCloseLines(gc, x, y, lineLength, false);
                    break;
                }
            case SWT.HOT:
                {
                    drawCloseLines(gc, x, y, lineLength, true);
                    break;
                }
            case SWT.SELECTED:
                {
                    drawCloseLines(gc, x, y, lineLength, true);
                    break;
                }
            case SWT.BACKGROUND:
                {
                    int[] shape = new int[] { x, y, x + 10, y, x + 10, y + 10, x, y + 10 };
                    drawBackground(gc, shape, false);
                    break;
                }
        }
    }

    private void drawDirtyIndicator(GC gc, Rectangle closeRect, boolean selected) {
        int diameter = 8;
        if (!selected) {
        }
    }

    private void drawCloseLines(GC gc, int x, int y, int lineLength, boolean hot) {
        if (hot) {
        }
    }

    void drawChevron(GC gc, Rectangle chevronRect, int chevronImageState) {
        if (chevronRect.width == 0 || chevronRect.height == 0)
            return;
        // draw chevron (10x7)
        Display display = getApi().parent.getDisplay();
        Font font = getChevronFont(display);
        int fontHeight = font.getFontData()[0].getHeight();
        int indent = Math.max(2, (chevronRect.height - fontHeight - CHEVRON_BOTTOM_INDENT) / 2);
        int x = chevronRect.x + 2;
        int y = chevronRect.y + indent;
        int count = ((DartCTabFolder) getApi().parent.getImpl()).getChevronCount();
        String chevronString = count > 99 ? CHEVRON_ELLIPSIS : String.valueOf(count);
        switch(chevronImageState & (SWT.HOT | SWT.SELECTED)) {
            case SWT.NONE:
                {
                    drawChevronContent(gc, x, y, chevronString);
                    break;
                }
            case SWT.HOT:
                {
                    drawRoundRectangle(gc, chevronRect);
                    drawChevronContent(gc, x, y, chevronString);
                    break;
                }
            case SWT.SELECTED:
                {
                    drawRoundRectangle(gc, chevronRect);
                    drawChevronContent(gc, x + 1, y + 1, chevronString);
                    break;
                }
        }
    }

    private void drawRoundRectangle(GC gc, Rectangle chevronRect) {
    }

    private void drawChevronContent(GC gc, int x, int y, String chevronString) {
    }

    /*
	 * Draw the unselected border for the receiver on the left.
	 */
    void drawLeftUnselectedBorder(GC gc, Rectangle bounds, int state) {
        if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
        } else {
        }
    }

    void drawMaximize(GC gc, Rectangle maxRect, int maxImageState) {
        if (maxRect.width == 0 || maxRect.height == 0)
            return;
        switch(maxImageState & (SWT.HOT | SWT.SELECTED)) {
            case SWT.NONE:
                {
                    if (!getApi().parent.getMaximized()) {
                    } else {
                    }
                    break;
                }
            case SWT.HOT:
                {
                    drawRoundRectangle(gc, maxRect);
                    if (!getApi().parent.getMaximized()) {
                    } else {
                    }
                    break;
                }
            case SWT.SELECTED:
                {
                    drawRoundRectangle(gc, maxRect);
                    if (!getApi().parent.getMaximized()) {
                    } else {
                    }
                    break;
                }
        }
    }

    void drawMinimize(GC gc, Rectangle minRect, int minImageState) {
        if (minRect.width == 0 || minRect.height == 0)
            return;
        switch(minImageState & (SWT.HOT | SWT.SELECTED)) {
            case SWT.NONE:
                {
                    if (!getApi().parent.getMinimized()) {
                    } else {
                    }
                    break;
                }
            case SWT.HOT:
                {
                    drawRoundRectangle(gc, minRect);
                    if (!getApi().parent.getMinimized()) {
                    } else {
                    }
                    break;
                }
            case SWT.SELECTED:
                {
                    drawRoundRectangle(gc, minRect);
                    if (!getApi().parent.getMinimized()) {
                    } else {
                    }
                    break;
                }
        }
    }

    void drawHighlight(GC gc, Rectangle bounds, int state, int rightEdge) {
    }

    /*
	 * Draw the unselected border for the receiver on the right.
	 */
    void drawRightUnselectedBorder(GC gc, Rectangle bounds, int state) {
        if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
        } else {
        }
    }

    void drawSelected(int itemIndex, GC gc, Rectangle bounds, int state) {
        CTabItem item = ((DartCTabFolder) getApi().parent.getImpl()).items[itemIndex];
        int x = bounds.x;
        int y = bounds.y;
        int height = bounds.height;
        int width = bounds.width;
        int borderLeft = ((DartCTabFolder) getApi().parent.getImpl()).borderVisible ? 1 : 0;
        int borderRight = borderLeft;
        int borderTop = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? borderLeft : 0;
        int borderBottom = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? 0 : borderLeft;
        Point size = getApi().parent.getSize();
        int rightEdge = Math.min(x + width, ((DartCTabFolder) getApi().parent.getImpl()).getRightItemEdge(gc));
        //	 Draw selection border across all tabs
        if ((state & SWT.BACKGROUND) != 0) {
            int highlight_header = (getApi().parent.getStyle() & SWT.FLAT) != 0 ? 1 : 3;
            int xx = borderLeft;
            int yy = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? size.y - borderBottom - ((DartCTabFolder) getApi().parent.getImpl()).tabHeight - highlight_header : borderTop + ((DartCTabFolder) getApi().parent.getImpl()).tabHeight + 1;
            int ww = size.x - borderLeft - borderRight;
            int hh = highlight_header - 1;
            int[] shape = new int[] { xx, yy, xx + ww, yy, xx + ww, yy + hh, xx, yy + hh };
            if (((DartCTabFolder) getApi().parent.getImpl()).selectionGradientColors != null && !((DartCTabFolder) getApi().parent.getImpl()).selectionGradientVertical) {
                drawBackground(gc, shape, ((DartCTabFolder) getApi().parent.getImpl()).shouldHighlight());
            } else {
            }
            if (((DartCTabFolder) getApi().parent.getImpl()).single) {
                if (!((DartCTabItem) item.getImpl()).showing)
                    return;
            } else {
                // if selected tab scrolled out of view or partially out of view
                // just draw bottom line
                if (!((DartCTabItem) item.getImpl()).showing) {
                    return;
                }
                // draw selected tab background and outline
                shape = new int[12];
                if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
                    int index = 0;
                    // first point repeated here because below we reuse shape to draw outline
                    shape[index++] = x;
                    shape[index++] = y - 1;
                    shape[index++] = x;
                    shape[index++] = y - 1;
                    shape[index++] = x;
                    shape[index++] = y + height;
                    if (borderLeft == 0 && itemIndex == ((DartCTabFolder) getApi().parent.getImpl()).firstIndex) {
                        shape[index - 2] += x;
                        shape[index - 1] += y + height;
                    }
                    shape[index++] = rightEdge - 1;
                    shape[index++] = y + height - 1;
                    shape[index++] = rightEdge - 1;
                    shape[index++] = y - 1;
                    shape[index++] = rightEdge - 1;
                    shape[index++] = y - 1;
                } else {
                    int index = 0;
                    // first point repeated here because below we reuse shape to draw outline
                    shape[index++] = x;
                    shape[index++] = y + height + 1;
                    shape[index++] = x;
                    shape[index++] = y + height + 1;
                    shape[index++] = x;
                    shape[index++] = y;
                    if (borderLeft == 0 && itemIndex == ((DartCTabFolder) getApi().parent.getImpl()).firstIndex) {
                        shape[index - 2] += x;
                        shape[index - 1] += y;
                    }
                    shape[index++] = rightEdge - 1;
                    shape[index++] = y;
                    shape[index++] = rightEdge - 1;
                    shape[index++] = y + height + 1;
                    shape[index++] = rightEdge - 1;
                    shape[index++] = y + height + 1;
                }
                Rectangle clipBounds = item.getBounds();
                clipBounds.height += 1;
                if (((DartCTabFolder) getApi().parent.getImpl()).onBottom)
                    clipBounds.y -= 1;
                //Highlight colors MUST be drawn before the outline so that outline can cover it in the right spots (start of swoop)
                //otherwise the curve looks jagged
                drawHighlight(gc, bounds, state, rightEdge);
                // draw highlight marker of selected tab
                if (((DartCTabFolder) getApi().parent.getImpl()).selectionHighlightBarThickness > 0) {
                    int[] highlightShape = Arrays.copyOf(shape, shape.length);
                    // Update Y coordinates in shape to apply highlight thickness
                    int thickness = ((DartCTabFolder) getApi().parent.getImpl()).selectionHighlightBarThickness;
                    boolean onBottom = ((DartCTabFolder) getApi().parent.getImpl()).onBottom;
                    int bottomY = y + height - 1;
                    int highlightY = onBottom ? bottomY - thickness : thickness + 1;
                    highlightShape[1] = highlightShape[3] = highlightShape[highlightShape.length - 1] = highlightShape[highlightShape.length - 3] = highlightY;
                }
                // draw outline
                shape[0] = Math.max(0, borderLeft - 1);
                if (borderLeft == 0 && itemIndex == ((DartCTabFolder) getApi().parent.getImpl()).firstIndex) {
                    shape[1] = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? y + height - 1 : y;
                    shape[5] = shape[3] = shape[1];
                }
                shape[shape.length - 2] = size.x - borderRight + 1;
                for (int i = 0; i < shape.length / 2; i++) {
                    if (shape[2 * i + 1] == y + height + 1)
                        shape[2 * i + 1] -= 1;
                }
            }
        }
        if ((state & SWT.FOREGROUND) != 0) {
            // draw Image
            Rectangle trim = computeTrim(itemIndex, SWT.NONE, 0, 0, 0, 0);
            int xDraw = x - trim.x;
            if (((DartCTabFolder) getApi().parent.getImpl()).single && shouldAllocateCloseRect(item))
                xDraw += ((DartCTabItem) item.getImpl()).closeRect.width;
            Image image = item.getImage();
            if (image != null && !image.isDisposed() && ((DartCTabFolder) getApi().parent.getImpl()).showSelectedImage) {
                Rectangle imageBounds = image.getBounds();
                // only draw image if it won't overlap with close button
                int maxImageWidth = rightEdge - xDraw - (trim.width + trim.x);
                if (!((DartCTabFolder) getApi().parent.getImpl()).single && ((DartCTabItem) item.getImpl()).closeRect.width > 0)
                    maxImageWidth -= ((DartCTabItem) item.getImpl()).closeRect.width + INTERNAL_SPACING;
                if (imageBounds.width < maxImageWidth) {
                    int imageY = y + (height - imageBounds.height) / 2;
                    imageY += ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? -1 : 1;
                    xDraw += imageBounds.width + INTERNAL_SPACING;
                }
            }
            // draw Text
            xDraw += getLeftTextMargin(item);
            int textWidth = rightEdge - xDraw - (trim.width + trim.x);
            if (!((DartCTabFolder) getApi().parent.getImpl()).single && ((DartCTabItem) item.getImpl()).closeRect.width > 0)
                textWidth -= ((DartCTabItem) item.getImpl()).closeRect.width + INTERNAL_SPACING;
            if (textWidth > 0) {
                if (((DartCTabItem) item.getImpl()).shortenedText == null || ((DartCTabItem) item.getImpl()).shortenedTextWidth != textWidth) {
                    ((DartCTabItem) item.getImpl()).shortenedText = shortenText(gc, item.getText(), textWidth);
                    ((DartCTabItem) item.getImpl()).shortenedTextWidth = textWidth;
                }
                // draw a Focus rectangle
                if (getApi().parent.isFocusControl()) {
                }
            }
            if (shouldAllocateCloseRect(item)) {
                drawClose(gc, ((DartCTabItem) item.getImpl()).closeRect, ((DartCTabItem) item.getImpl()).closeImageState, shouldDrawDirtyIndicator(item), true);
            }
        }
    }

    private int getLeftTextMargin(CTabItem item) {
        int margin = 0;
        if (shouldApplyLargeTextPadding(getApi().parent)) {
            margin += getLargeTextPadding(item);
            if (shouldAllocateCloseRect(item)) {
                margin -= ((DartCTabItem) item.getImpl()).closeRect.width / 2;
            }
        }
        return margin;
    }

    void drawTabArea(GC gc, Rectangle bounds, int state) {
        Point size = getApi().parent.getSize();
        int tabHeight = ((DartCTabFolder) getApi().parent.getImpl()).tabHeight;
        int style = getApi().parent.getStyle();
        int borderLeft = ((DartCTabFolder) getApi().parent.getImpl()).borderVisible ? 1 : 0;
        int borderRight = borderLeft;
        int borderTop = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? borderLeft : 0;
        int borderBottom = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? 0 : borderLeft;
        int selectedIndex = ((DartCTabFolder) getApi().parent.getImpl()).selectedIndex;
        int highlight_header = (style & SWT.FLAT) != 0 ? 1 : 3;
        if (tabHeight == 0) {
            if ((style & SWT.FLAT) != 0 && (style & SWT.BORDER) == 0)
                return;
            int x1 = borderLeft - 1;
            int x2 = size.x - borderRight;
            int y1 = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? size.y - borderBottom - highlight_header - 1 : borderTop + highlight_header;
            int y2 = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? size.y - borderBottom : borderTop;
            if (borderLeft > 0 && ((DartCTabFolder) getApi().parent.getImpl()).onBottom)
                y2 -= 1;
            int[] shape = new int[] { x1, y1, x1, y2, x2, y2, x2, y1 };
            // If horizontal gradient, show gradient across the whole area
            if (selectedIndex != -1 && ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientColors != null && ((DartCTabFolder) getApi().parent.getImpl()).selectionGradientColors.length > 1 && !((DartCTabFolder) getApi().parent.getImpl()).selectionGradientVertical) {
                drawBackground(gc, shape, true);
            } else if (selectedIndex == -1 && ((DartCTabFolder) getApi().parent.getImpl()).gradientColors != null && ((DartCTabFolder) getApi().parent.getImpl()).gradientColors.length > 1 && !((DartCTabFolder) getApi().parent.getImpl()).gradientVertical) {
                drawBackground(gc, shape, false);
            } else {
            }
            //draw 1 pixel border
            if (borderLeft > 0) {
            }
            return;
        }
        int x = Math.max(0, borderLeft - 1);
        int y = ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? size.y - borderBottom - tabHeight : borderTop;
        int width = size.x - borderLeft - borderRight + 1;
        int height = tabHeight - 1;
        int[] shape = new int[8];
        // Draw Tab Header
        if (((DartCTabFolder) getApi().parent.getImpl()).onBottom) {
            int index = 0;
            shape[index++] = x;
            shape[index++] = y - highlight_header;
            shape[index++] = x;
            shape[index++] = y + height;
            if (borderLeft == 0)
                shape[index - 1] += 1;
            shape[index++] = x + width;
            shape[index++] = y + height;
            if (borderLeft == 0)
                shape[index - 1] += 1;
            shape[index++] = x + width;
            shape[index++] = y - highlight_header;
        } else {
            int index = 0;
            shape[index++] = x;
            shape[index++] = y + height + highlight_header + 1;
            shape[index++] = x;
            shape[index++] = y;
            shape[index++] = x + width;
            shape[index++] = y;
            shape[index++] = x + width;
            shape[index++] = y + height + highlight_header + 1;
        }
        // Fill in background
        boolean single = ((DartCTabFolder) getApi().parent.getImpl()).single;
        boolean bkSelected = single && selectedIndex != -1;
        drawBackground(gc, shape, bkSelected);
        // Draw selected tab
        if (selectedIndex == -1) {
        }
        // Draw border line
        if (borderLeft > 0) {
        }
    }

    void drawUnselected(int index, GC gc, Rectangle bounds, int state) {
        CTabItem item = ((DartCTabFolder) getApi().parent.getImpl()).items[index];
        int x = bounds.x;
        int y = bounds.y;
        int height = bounds.height;
        int width = bounds.width;
        // Do not draw partial items
        if (!((DartCTabItem) item.getImpl()).showing)
            return;
        if ((state & SWT.BACKGROUND) != 0) {
            if (index > 0 && index < ((DartCTabFolder) getApi().parent.getImpl()).selectedIndex)
                drawLeftUnselectedBorder(gc, bounds, state);
            // If it is the last one then draw a line
            if (index > ((DartCTabFolder) getApi().parent.getImpl()).selectedIndex)
                drawRightUnselectedBorder(gc, bounds, state);
        }
        if ((state & SWT.FOREGROUND) != 0) {
            // draw Image
            Rectangle trim = computeTrim(index, SWT.NONE, 0, 0, 0, 0);
            int xDraw = x - trim.x;
            Image image = item.getImage();
            if (image != null && !image.isDisposed() && ((DartCTabFolder) getApi().parent.getImpl()).showUnselectedImage) {
                Rectangle imageBounds = image.getBounds();
                // only draw image if it won't overlap with close button
                int maxImageWidth = x + width - xDraw - (trim.width + trim.x);
                if (shouldAllocateCloseRect(item)) {
                    maxImageWidth -= ((DartCTabItem) item.getImpl()).closeRect.width + INTERNAL_SPACING;
                }
                if (imageBounds.width < maxImageWidth) {
                    int imageHeight = imageBounds.height;
                    int imageY = y + (height - imageHeight) / 2;
                    imageY += ((DartCTabFolder) getApi().parent.getImpl()).onBottom ? -1 : 1;
                    int imageWidth = imageBounds.width * imageHeight / imageBounds.height;
                    xDraw += imageWidth + INTERNAL_SPACING;
                }
            }
            // draw Text
            xDraw += getLeftTextMargin(item);
            int textWidth = x + width - xDraw - (trim.width + trim.x);
            if (shouldAllocateCloseRect(item)) {
                textWidth -= ((DartCTabItem) item.getImpl()).closeRect.width + INTERNAL_SPACING;
            }
            if (textWidth > 0) {
                if (((DartCTabItem) item.getImpl()).shortenedText == null || ((DartCTabItem) item.getImpl()).shortenedTextWidth != textWidth) {
                    ((DartCTabItem) item.getImpl()).shortenedText = shortenText(gc, item.getText(), textWidth);
                    ((DartCTabItem) item.getImpl()).shortenedTextWidth = textWidth;
                }
            }
            // draw close or dirty indicator
            if (shouldAllocateCloseRect(item)) {
                drawClose(gc, ((DartCTabItem) item.getImpl()).closeRect, ((DartCTabItem) item.getImpl()).closeImageState, shouldDrawDirtyIndicator(item), false);
            }
        }
    }

    Color getFillColor() {
        if (fillColor == null) {
            fillColor = new Color(CLOSE_FILL);
        }
        return fillColor;
    }

    private Font getChevronFont(Display display) {
        if (chevronFont == null) {
            FontData fd = getApi().parent.getFont().getFontData()[0];
            int fontHeight = (int) (fd.height * CHEVRON_FONT_SIZE_FACTOR);
            fd.setHeight(fontHeight);
            chevronFont = new Font(display, fd);
        }
        return chevronFont;
    }

    void resetChevronFont() {
        if (chevronFont != null) {
            chevronFont.dispose();
            chevronFont = null;
        }
    }

    String shortenText(GC gc, String text, int width) {
        return shortenText(gc, text, width, ELLIPSIS);
    }

    String shortenText(GC gc, String text, int width, String ellipses) {
        int length = text.length();
        TextLayout layout = new TextLayout(getApi().parent.getDisplay());
        layout.setText(text);
        int end = layout.getPreviousOffset(length, SWT.MOVEMENT_CLUSTER);
        while (end > 0) {
            text = text.substring(0, end);
            end = layout.getPreviousOffset(end, SWT.MOVEMENT_CLUSTER);
        }
        layout.dispose();
        return end == 0 ? text.substring(0, 1) : text + ellipses;
    }

    public Color _fillColor() {
        return fillColor;
    }

    public CTabFolderRenderer getApi() {
        if (api == null)
            api = CTabFolderRenderer.createApi(this);
        return (CTabFolderRenderer) api;
    }

    protected CTabFolderRenderer api;

    public void setApi(CTabFolderRenderer api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }
}
